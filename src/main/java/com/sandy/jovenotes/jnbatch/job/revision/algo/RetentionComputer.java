package com.sandy.jovenotes.jnbatch.job.revision.algo;

import com.sandy.jovenotes.jnbatch.job.revision.vo.Card;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class RetentionComputer {
    
    private static final Logger log = Logger.getLogger( RetentionComputer.class ) ;
    
    // This map is copied from the way the model is created.
    // Refer to JNRegressionModel > df_enricher.py for the original map
    private static final Map<String, Integer> SUB_NAME_MAP = new HashMap<>() ;
    {
        SUB_NAME_MAP.put( "Biology",         1  ) ;
        SUB_NAME_MAP.put( "Chemistry",       2  ) ;
        SUB_NAME_MAP.put( "Civics",          3  ) ;
        SUB_NAME_MAP.put( "English",         4  ) ;
        SUB_NAME_MAP.put( "English Grammar", 5  ) ;
        SUB_NAME_MAP.put( "Geography",       6  ) ;
        SUB_NAME_MAP.put( "Hindi",           7  ) ;
        SUB_NAME_MAP.put( "History",         8  ) ;
        SUB_NAME_MAP.put( "Mathematics",     9  ) ;
        SUB_NAME_MAP.put( "Physics",         10 ) ;
        SUB_NAME_MAP.put( "Rapid Reader",    11 ) ;
        SUB_NAME_MAP.put( "Computers",       12 ) ;
        SUB_NAME_MAP.put( "Julius Caesar",   13 ) ;
    }

    private final Map<String, RetentionModel> models ;
    
    private final int futureProjectionDays ;
    
    public RetentionComputer( int numProjectionDays ) throws Exception {
        this.models = new RetentionModelLoader().loadRetentionModels() ;
        this.futureProjectionDays = numProjectionDays ;
    }
    
    public float getProbabilityOfRightAnswer( Card card ) {
        
        // The model is computed for a minimum of two attempts. So if
        // its a fresh card, lets assume that it will be answered correctly.
        if( card.getNumAttempts() == 0 ) return 1.0F ;
        
        RetentionModel model = getModel( card ) ;
        
        // Note that numAttempts is being increased by 1 to calculate for
        // the next attempt of this card - future prediction.
        float x = ( model.coeffSubjectNum * getSubjectNum( card ) ) +
                  ( model.coeffDifficultyLevel * card.getDifficulty() ) +
                  ( model.coeffTimeSpent * card.getAvgTimeSpent() ) +
                  ( model.coeffAttemptNum * (card.getNumAttempts()+1) ) +
                  ( model.coeffGapDuration * (card.getGapDuration()+futureProjectionDays) ) +
                  ( model.coeffLearningEff * card.getAbsLE() ) +
                  model.intercept ;
        
        return (float)(1 / ( 1 + Math.exp( -x ) ));
    }
    
    public boolean getPredictedTestOutcome( Card card ) {
        RetentionModel model = getModel( card ) ;
        return getProbabilityOfRightAnswer( card ) >= model.threshold ;
    }
    
    // Returns true, if the card should be resurrected irrespective of the
    // prediction model's outcome. This is a subjective decision.
    public boolean getManualResurrectionOverride( Card card ) {

        if( card.getAbsLE() <= 60 ||
            card.getNumAttempts() == 2 ||
            card.getGapDuration() > 120 ) {
            return true ;
        }
        return false ;
    }
    
    public boolean activateForRevision( Card card ) {
        return true ;
    }
    
    private RetentionModel getModel( Card card ) {
        
        RetentionModel model = models.get( card.getCardType() ) ;
        if( model == null ) {
            // This implies we have  a card for which a prediction model does
            // not exist. Fall back to one of the known and heavily sampled types - say "fib"
            // This is a judgement call without any objective bases. This
            // scenario should be in the extreme minority.
            log.info( "Card " + card.getCardId() + " type=" + card.getCardType() + " does not have a model" ) ;
            log.info( "\treverting to 'fib' model for this card." ) ;
            model = models.get( "fib" ) ;
        }
        return model ;
    }
    
    private int getSubjectNum( Card card ) {
        
        int subjectNum = 1 ;
        String subjectName = card.getChapter().getSubjectName() ;
        
        if( SUB_NAME_MAP.containsKey( subjectName ) ) {
            subjectNum = SUB_NAME_MAP.get( subjectName ) ;
        }
        else {
            log.info( "Subject " + subjectName + " not modelled." );
        }
        return subjectNum ;
    }
}
