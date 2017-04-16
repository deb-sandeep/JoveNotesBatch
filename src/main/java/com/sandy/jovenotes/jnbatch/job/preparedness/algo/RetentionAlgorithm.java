package com.sandy.jovenotes.jnbatch.job.preparedness.algo;

import static com.sandy.jovenotes.jnbatch.util.CardType.FIB ;
import static com.sandy.jovenotes.jnbatch.util.CardType.IMGLABEL ;
import static com.sandy.jovenotes.jnbatch.util.CardType.MATCHING ;
import static com.sandy.jovenotes.jnbatch.util.CardType.MULTI_CHOICE ;
import static com.sandy.jovenotes.jnbatch.util.CardType.QA ;
import static com.sandy.jovenotes.jnbatch.util.CardType.SPELLBEE ;
import static com.sandy.jovenotes.jnbatch.util.CardType.TF ;
import static com.sandy.jovenotes.jnbatch.util.CardType.VOICE2TEXT ;
import static java.lang.Math.exp ;
import static java.lang.Math.pow ;

import java.util.ArrayList ;
import java.util.Date ;
import java.util.HashMap ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Map ;

import org.apache.commons.lang.time.DateUtils ;
import org.apache.log4j.Logger ;

import com.sandy.jovenotes.jnbatch.job.preparedness.vo.Card ;
import com.sandy.jovenotes.jnbatch.job.preparedness.vo.CardRating ;
import com.sandy.jovenotes.jnbatch.util.CardType ;

public class RetentionAlgorithm {

    static final Logger log = Logger.getLogger( RetentionAlgorithm.class ) ;
    
    private static double RET_K = Math.log( 0.35 ) ;
    private static Map<String, long[]> retentionPeriods = new HashMap<>() ;

    static {
        retentionPeriods.put( IMGLABEL    , toSeconds( new long[]{ 1, 2,  4,  8, 16, 32 } ) ) ;
        retentionPeriods.put( QA          , toSeconds( new long[]{ 1, 2,  4,  8, 16, 32 } ) ) ;
        retentionPeriods.put( VOICE2TEXT  , toSeconds( new long[]{ 1, 3,  6, 12, 30, 50 } ) ) ;
        retentionPeriods.put( MATCHING    , toSeconds( new long[]{ 2, 4,  8, 10, 20, 40 } ) ) ;
        retentionPeriods.put( FIB         , toSeconds( new long[]{ 3, 7, 14, 35, 40, 60 } ) ) ;
        retentionPeriods.put( SPELLBEE    , toSeconds( new long[]{ 4, 8, 20, 45, 50, 70 } ) ) ;
        retentionPeriods.put( MULTI_CHOICE, toSeconds( new long[]{ 5, 8, 20, 45, 50, 70 } ) ) ;
        retentionPeriods.put( TF          , toSeconds( new long[]{ 5,10, 25, 50, 60, 70 } ) ) ;
    }
    
    private static long[][] QA_RET = {
        toSeconds( new long[]{ 4,10, 20, 45, 50, 70 } ),
        toSeconds( new long[]{ 4, 7, 16, 35, 40, 65 } ),
        toSeconds( new long[]{ 3, 6, 14, 30, 35, 60 } ),
        toSeconds( new long[]{ 2, 4,  8, 16, 30, 50 } ),
    } ;
    
    private static long[] toSeconds( long[] days ) {
        for( int i=0; i<days.length; i++ ) {
            days[i] *= 86400 ;
        }
        return days ;
    }
    
    private static long delta( Date d1, Date d2 ) {
        return ( d1.getTime() - d2.getTime() )/1000 ;
    }
    
    private List<RetentionAlgorithmListener> listeners = 
            new ArrayList<RetentionAlgorithmListener>() ;
    
    public void addListener( RetentionAlgorithmListener listener ) {
        this.listeners.add( listener ) ;
    }
    
    public void clearListeners() {
        this.listeners.clear() ;
    }
    
    public double getCurrentRetentionValue( Card card ) {
        if( card == null ) {
            throw new IllegalStateException( "Card not set." ) ;
        }
        else if( card.getRatings().isEmpty() ) {
            return 0 ;
        }
        card.setCurrentRetentionValue( compute( card, false ) ) ;
        return card.getCurrentRetentionValue() ;
    }
    
    public void projectRetentionTrajectory( Card card ) {
        if( card == null ) {
            throw new IllegalStateException( "Card not set." ) ;
        }
        
        if( !card.getRatings().isEmpty() ) {
            compute( card, true ) ;
        }
    }
    
    private double compute( Card card, boolean projectTrajectory ) {
        
        double     retentionVal = 0 ;
        Level      level        = Level.NS ;
        CardRating r            = null ;
        long       elapsedTime  = 0 ;
        
        Date             lastTrajectoryDate = null ;
        List<CardRating> ratings            = null ;
        
        ratings = filterNonContributingAttempts( card ) ;
        
        for( int i=0; i<ratings.size(); i++ ) {
            
            r = ratings.get( i ) ;
            
            if( i==0 ) {
                // If this is the first attempt the retention boost is 1 and
                // hence the retention score is 100.
                retentionVal = 100 ;
            }
            else {
                
                elapsedTime = r.getSecsSincePrevRating() ;
                double expectedRetentionVal = 0 ;
                double boostMultiplier = 1.0 ;
                
                expectedRetentionVal = getProjectedRetentionValue( 
                                                    r.getCard(), 
                                                    level,
                                                    retentionVal, 
                                                    elapsedTime ) ;
                if( expectedRetentionVal >= 35 ) {
                    double fall = (double)( expectedRetentionVal-35 ) ;
                    boostMultiplier = exp( -pow( fall, 2 )/( 2*25*25 ) ) ;
                }
                
                double boost = (100-expectedRetentionVal)*boostMultiplier ;
                retentionVal = expectedRetentionVal + boost ;
            }
            
            level = level.getNextLevel( r.getRating() ) ;
            
            if( projectTrajectory ) {
                lastTrajectoryDate = publishProjectedTrajectory( 
                                                      i, retentionVal, 
                                                      level, ratings ) ;
            }
        }
        
        elapsedTime = delta( new Date(), r.getDate() ) ;
        retentionVal = getProjectedRetentionValue( r.getCard(),
                                                   level,
                                                   retentionVal, 
                                                   elapsedTime ) ;
        if( projectTrajectory ) {
            publishAnnotation( "X", lastTrajectoryDate, retentionVal ) ;
        }
        return retentionVal ;
    }
    
    // If we have a zero score and zero time, it implies we are dealing
    // either with an APMNS card or a repetition of the card in the 
    // same session. In either case, these attempts don't contribute
    // towards retention projection, ergo, we remove them.
    private List<CardRating> filterNonContributingAttempts( Card card ) {
        
        List<CardRating> ratings = new ArrayList<>() ;
        ratings.addAll( card.getRatings() ) ;
        
        for( Iterator<CardRating> iter = ratings.iterator(); iter.hasNext(); ) {
            CardRating r = iter.next() ;
            if( r.getScore() == 0 || r.getTimeTaken() == 0 ) {
                iter.remove() ;
            }
        }
        return ratings ;
    }
    
    private Date publishProjectedTrajectory( int curRatingIndex, 
                                             double initialRetVal, 
                                             Level level,
                                             List<CardRating> ratings ) {
        
        CardRating r = null ;
        CardRating nextRating = null ;
        
        r = ratings.get( curRatingIndex ) ;
        
        if( curRatingIndex<ratings.size()-1 ) {
            nextRating = ratings.get( curRatingIndex+1 ) ;
        }
        
        publishAnnotation( "" + r.getRating(), r.getDate(), initialRetVal ) ;
        
        Date   now    = new Date() ;
        Date   date   = DateUtils.addSeconds( r.getDate(), 1 ) ;
        double retVal = getProjectedRetentionValue( r.getCard(), 
                                                    level, initialRetVal,
                                                    delta( date, r.getDate() )) ;
        while( true ) {
            
            if( nextRating != null ) {
                if( date.after( nextRating.getDate() ) ) {
                    break ;
                }
            }
            else if( retVal < 35 ) {
                break ;
            }
            else if( date.after( now ) ) {
                break ;
            }
            
            publishOutput( "Forecast", date, retVal ) ; 
            
            date =  DateUtils.addHours( date, 6 ) ;
            retVal = getProjectedRetentionValue( r.getCard(), level, 
                                                 initialRetVal, 
                                                 delta( date, r.getDate() ) ) ;
            try { Thread.sleep( 5 ) ; } catch( Exception e ) {}
        }
        
        return date ;
    }
    
    /**
     * Given a card type and an associated level, this method looks up the
     * retention period.
     * 
     * Retention period is defined as the duration of time, in seconds,
     * it would take for the retention level to drop to 35%, starting from a
     * 100%. 
     * 
     * These values are heuristically computed and burnt into a static matrix.
     */
    private long getAbsRetentionPeriod( Card card, Level level ) {
        if( level.getLevel() == -1 ) {
            // If we are at the NS level, there is no concept of retention
            // since the card has never been attempted before. Hence we 
            // return a 0
            return 0 ;
        }
        else if( card.getCardType().equals( QA ) && 
                 card.getDifficulty() < 40 ) {
            int index = card.getDifficulty() / 10 ;
            return QA_RET[index][level.getLevel()] ;
        }
        
        return retentionPeriods.get( card.getCardType() )[level.getLevel()] ;
    }
    
    /**
     * Computes the projected retention value for a card which is of a certain
     * type {@link CardType}, at a certain Level and having a current retention
     * value (retLevel) - into the future after numSeconds. 
     * 
     * @return A value which is always between the current retention value
     *         and 0. Retention value turns to 0 exponentially with time.
     */
    private double getProjectedRetentionValue( Card card, Level level, 
                                               double startRetVal, long numSecs ) {
        
        long absRetPeriod = getAbsRetentionPeriod( card, level ) ;
        double retSlope  = -((double)(absRetPeriod))/RET_K ;
        
        return startRetVal*Math.exp( -(numSecs/retSlope ) ) ;
    }
    
    private void publishOutput( String series, Date date, double rating ) {
        for( RetentionAlgorithmListener l : listeners ) {
            l.handleRetentionLevelChange( series, date, rating ) ;
        }
    }
    
    private void publishAnnotation( String text, Date date, double rating ) {
        for( RetentionAlgorithmListener l : listeners ) {
            l.addAnnotation( text, date, rating ) ;
        }
    }
}
