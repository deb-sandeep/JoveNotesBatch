package com.sandy.jovenotes.jnbatch.job.revision;

import java.util.HashMap ;
import java.util.List;
import java.util.Map ;
import java.util.concurrent.Callable ;

import com.sandy.jovenotes.jnbatch.job.revision.algo.RetentionComputer;
import com.sandy.jovenotes.jnbatch.job.revision.dao.CardDBO;
import com.sandy.jovenotes.jnbatch.job.revision.vo.Card;
import org.apache.http.HttpResponse ;
import org.apache.http.client.methods.HttpPost ;
import org.apache.http.entity.ContentType ;
import org.apache.http.entity.StringEntity ;
import org.apache.http.util.EntityUtils ;
import org.apache.log4j.Logger ;

import com.google.gson.Gson ;
import com.sandy.jovenotes.jnbatch.JoveNotesBatch ;
import com.sandy.jovenotes.jnbatch.job.revision.dao.ChapterDBO;
import com.sandy.jovenotes.jnbatch.job.revision.dao.PreparednessComputeRequestDBO;
import com.sandy.jovenotes.jnbatch.job.revision.vo.Chapter;

import static com.sandy.jovenotes.jnbatch.util.StringUtil.round ;

public class PreparednessComputeTask implements Callable<Void> {
    
    private static final Logger log = Logger.getLogger( PreparednessComputeTask.class ) ;
    
    private static final String PRACTICE_LEVEL_CURRENT = "CUR" ;
    private static final String PRACTICE_LEVEL_REVISION_PREFIX = "R-" ;
    
    private final String updateLearningStatsAPIUrl ;
    
    private final Chapter chapter ;
    
    private final CardDBO                       cardDBO        = new CardDBO() ;
    private final ChapterDBO                    chapterDbo     = new ChapterDBO() ;
    private final PreparednessComputeRequestDBO prepProcReqDbo = new PreparednessComputeRequestDBO() ;
    
    private final RetentionComputer retentionComputer ;
    
    public PreparednessComputeTask( Chapter chapter,
                                    RetentionComputer retentionComputer,
                                    String updateLearningStatsAPIUrl ) {
        this.chapter = chapter ;
        this.retentionComputer = retentionComputer ;
        this.updateLearningStatsAPIUrl = updateLearningStatsAPIUrl ;
    }

    @Override
    public Void call() {
        log.info( "Computing preparedness for chapter : " + chapter.getDisplayName() ) ;
        
        try {
            cardDBO.populateCards( chapter ) ;
            
            List<Card> cards = chapter.getCards() ;
            log.debug( "  Found " + cards.size() + " cards for evaluation." ) ;
            
            if( !cards.isEmpty() ) {
                populateCardRetentionsAndPredictedOutcomes() ;
                populateChapterRetentionScore() ;

                if( chapter.isInCurrentMode() ) {
                    log.debug( "  Chapter is in current mode." ) ;
                    if( chapter.areAllCardsMastered() ) {
                        log.debug( "    All cards are mastered." ) ;
                        resurrectNegativeOutcomeCardsAndChangePracticeLevel( "L2" ) ;
                    }
                }
                else {
                    log.debug( "  Chapter is in " + chapter.getPracticeLevel() + " revision mode." ) ;
                    if( chapter.areAllCardsMastered() ) {
                        log.debug( "    All cards are mastered." ) ;
                        resurrectNegativeOutcomeCardsAndChangePracticeLevel( "L3" ) ;
                    }
                    else {
                        String targetCardLevel = chapter.getPracticeLevel()
                                                        .equalsIgnoreCase( "R-1" ) ? "L2" : "L3" ;
                        resurrectNegativeOutcomeCards( targetCardLevel ) ;
                    }
                }
                
                log.debug( "  Persisting changes." ) ;
                persistChanges() ;
                
                if( chapter.getNumResurrectedCards() > 0 ) {
                    log.info( "    Recomputing learning statistics." );
                    invokeUpdateLearningStatusAPI() ;
                }
            }
            prepProcReqDbo.deleteRequest( chapter ) ;
        }
        catch( Exception e ) {
            log.error( "Error computing chapter " + chapter.getChapterId() +
                       " for user " + chapter.getStudentName(), e );
        }
        return null ;
    }
    
    private void populateCardRetentionsAndPredictedOutcomes() {
        List<Card> cards = chapter.getCards() ;
        for( Card card : cards ) {
            
            float probability = retentionComputer.getProbabilityOfRightAnswer( card ) ;
            boolean regressionTestOutcome = retentionComputer.getPredictedTestOutcome( card ) ;
            
            card.setRetentionProbability( (int)(probability*100) ) ;
            card.setPredictedTestOutcome( regressionTestOutcome ) ;
        }
    }
    
    private void populateChapterRetentionScore() {
        float totalChapterRetentionScore = 0 ;
        for( Card card : chapter.getCards() ) {
            totalChapterRetentionScore += card.getRetentionProbability() ;
        }
        chapter.setRetentionScore( totalChapterRetentionScore/chapter.getNumCards() ) ;
        log.info( "  Retention score = " + round( chapter.getRetentionScore() ) ) ;
    }
    
    private int resurrectNegativeOutcomeCards( String targetCardLevel ) {
        
        int numCardsResurrected = 0 ;
        for( Card card : chapter.getCards() ) {
            
            boolean regressionTestOutcome = card.getPredictedTestOutcome() ;
            boolean resurrectionOverride  = retentionComputer.getManualResurrectionOverride( card ) ;
            
            boolean predictedOutcome = regressionTestOutcome ;
            if( !resurrectionOverride ) {
                // If subjective test outcome is false, it overrides the
                // regression test outcome.
                predictedOutcome = false ;
            }

            if( card.getCurrentLevel().equals( "MAS" ) &&
                !predictedOutcome ) {
                
                card.setResurrectionLevel( targetCardLevel ) ;
                numCardsResurrected++ ;
            }
        }
        log.debug( "    " + numCardsResurrected + " cards resurrected at " + targetCardLevel + " level." ) ;
        return numCardsResurrected ;
    }
    
    private void resurrectNegativeOutcomeCardsAndChangePracticeLevel( String targetCardLevel ) {
        int numResurrectedCards = resurrectNegativeOutcomeCards( targetCardLevel ) ;
        log.debug( "    " + numResurrectedCards + " cards resurrected at L3 level." ) ;
        if( numResurrectedCards > 0 ) {
            chapter.setPracticeLevel( getNextPracticeLevel() ) ;
            log.debug( "    Changed practice level to " + chapter.getPracticeLevel() ) ;
        }
    }
    
    private String getNextPracticeLevel() {
        
        String curPracticeLevel = chapter.getPracticeLevel() ;
        String nextPracticeLevel = null ;
        
        if( curPracticeLevel == null || curPracticeLevel.equalsIgnoreCase( PRACTICE_LEVEL_CURRENT ) ) {
            nextPracticeLevel = PRACTICE_LEVEL_REVISION_PREFIX + 1 ;
        }
        else if ( curPracticeLevel.startsWith( PRACTICE_LEVEL_REVISION_PREFIX ) ) {
            int revisionLevel = Integer.parseInt( curPracticeLevel.substring( 2 ) ) ;
            nextPracticeLevel = PRACTICE_LEVEL_REVISION_PREFIX + (revisionLevel+1) ;
        }
        return nextPracticeLevel ;
    }
    
    private void persistChanges() throws Exception {
        if( chapter.isDirty() ) {
            log.debug( "    Persisting changes to chapter." ) ;
            chapterDbo.saveChanges( chapter ) ;
        }
        log.debug( "    Persisting changes to cards." ) ;
        cardDBO.saveChanges( chapter ) ;
    }
    
    private void invokeUpdateLearningStatusAPI() throws Exception {
        
        String jsonPayload = getAPIRequestPayload() ;
        StringEntity payload = new StringEntity( jsonPayload, 
                                                 ContentType.APPLICATION_JSON ) ;
        
        HttpPost request = new HttpPost( updateLearningStatsAPIUrl ) ;
        request.setEntity( payload ) ;
        
        log.debug( "      Initiating API request." ) ;
        HttpResponse response = JoveNotesBatch.httpClient.execute( request ) ;
        log.debug( "      API response " + response.getStatusLine().toString() ) ;
        
        EntityUtils.consumeQuietly( response.getEntity() ) ;
        request.releaseConnection() ;
    }
    
    private String getAPIRequestPayload() {
        
        Map<String, Object> dataMap = new HashMap<>() ;
        dataMap.put( "studentName", chapter.getStudentName() ) ;
        dataMap.put( "chapterId", chapter.getChapterId() ) ;
        
        Gson gson = new Gson() ;
        return gson.toJson( dataMap ) ;
    }
}
