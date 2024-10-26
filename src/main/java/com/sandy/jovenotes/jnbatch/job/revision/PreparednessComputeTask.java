package com.sandy.jovenotes.jnbatch.job.revision;

import java.util.HashMap ;
import java.util.List;
import java.util.Map ;
import java.util.concurrent.Callable ;

import com.sandy.jovenotes.jnbatch.job.revision.algo.RetentionComputer;
import com.sandy.jovenotes.jnbatch.job.revision.dao.CardDBO;
import com.sandy.jovenotes.jnbatch.job.revision.vo.Card;
import org.apache.http.HttpEntity ;
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
            if( cards.isEmpty() ) {
                prepProcReqDbo.deleteRequest( chapter ) ;
                return null ;
            }
            
            int numMasteredCards = 0 ;
            float totalChapterRetentionScore = 0 ;
            
            for( Card card : cards ) {
                float probability = retentionComputer.getProbabilityOfRightAnswer( card ) ;
                boolean predictedTestOutcome = retentionComputer.getPredictedTestOutcome( card ) ;
                
                card.setRetentionProbability( (int)(probability*100) ) ;
                card.setPredictedTestOutcome( predictedTestOutcome ) ;
                
                totalChapterRetentionScore += (probability*100) ;
                
                if( card.getCurrentLevel().equalsIgnoreCase( "MAS" ) ) {
                    numMasteredCards++ ;
                }
            }
            
            chapter.setRetentionScore( totalChapterRetentionScore/cards.size() ) ;
            log.info( "  Retention score = " + round( chapter.getRetentionScore() ) ) ;
            
            // If all the cards are mastered, this chapter is eligible for
            // revision resurrection.
            int numCardsResurrected = 0 ;
            if( numMasteredCards < cards.size() ) {
                if( chapter.getPracticeLevel() == null ) {
                    chapter.setPracticeLevel( PRACTICE_LEVEL_CURRENT ) ;
                }
            }
            else {
                log.info( "  All cards are mastered. Enabling for next level revision." );
                numCardsResurrected = enableChapterForRevision() ;
            }
            
            log.debug( "  Persisting changes." ) ;
            persistChanges() ;
            
            if( numCardsResurrected > 0 ) {
                // Call the server to update the learning statistics so that
                // the dashboard shows the card distributions according to
                // revised levels.
                log.info( "    Recomputing learning statistics." );
                recomputeLearningStatusForChapter() ;
            }
            
            prepProcReqDbo.deleteRequest( chapter ) ;
        }
        catch( Exception e ) {
            log.error( "Error computing chapter " + chapter.getChapterId() +
                       " for user " + chapter.getStudentName(), e );
        }
        return null ;
    }
    
    private int enableChapterForRevision() {
        
        String nextPracticeLevel = getNextPracticeLevel() ;

        int numCardsResurrected = 0 ;
        for( Card card : chapter.getCards() ) {
            if( !card.getPredictedTestOutcome() ) {
                String newLevel = nextPracticeLevel.equals( "R-1" ) ? "L2" : "L3" ;
                card.setResurrectionLevel( newLevel ) ;
                numCardsResurrected++ ;
            }
        }
        
        // Change the revision lap only if we have cards resurrected, else
        // we will end up with a higher lap and no active cards.
        if( numCardsResurrected > 0 ) {
            chapter.setPracticeLevel( nextPracticeLevel ) ;
            log.info( "    Revision level " + chapter.getPracticeLevel() ) ;
            log.info( "    " + numCardsResurrected + " resurrected cards." ) ;
        }
        else {
            log.info( "    No resurrected cards, hence no change in revision lap." ) ;
        }

        return numCardsResurrected ;
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
    
    private void recomputeLearningStatusForChapter() 
        throws Exception {
        
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
