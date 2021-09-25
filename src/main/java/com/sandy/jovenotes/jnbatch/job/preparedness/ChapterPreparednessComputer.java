package com.sandy.jovenotes.jnbatch.job.preparedness;

import java.util.HashMap ;
import java.util.Map ;
import java.util.concurrent.Callable ;

import org.apache.http.HttpEntity ;
import org.apache.http.HttpResponse ;
import org.apache.http.client.methods.HttpPost ;
import org.apache.http.entity.ContentType ;
import org.apache.http.entity.StringEntity ;
import org.apache.http.util.EntityUtils ;
import org.apache.log4j.Logger ;

import com.google.gson.Gson ;
import com.sandy.jovenotes.jnbatch.JoveNotesBatch ;
import com.sandy.jovenotes.jnbatch.dao.ChapterDBO ;
import com.sandy.jovenotes.jnbatch.dao.PrepProcRequestDBO ;
import com.sandy.jovenotes.jnbatch.job.preparedness.algo.RetentionAlgorithm ;
import com.sandy.jovenotes.jnbatch.job.preparedness.vo.Card ;
import com.sandy.jovenotes.jnbatch.job.preparedness.vo.Chapter ;

public class ChapterPreparednessComputer implements Callable<Void> {
    
    private static Logger log = Logger.getLogger( ChapterPreparednessComputer.class ) ;
    
    private static final String API_URL =
            "http://localhost/jove_notes/api/BatchRobot/UpdateLearningStats" ;
    
    private Chapter            chapter = null ;
    private ChapterDBO         dbo     = new ChapterDBO() ;
    private PrepProcRequestDBO reqDbo  = new PrepProcRequestDBO() ;
    private RetentionAlgorithm algo    = new RetentionAlgorithm() ;
    
    public ChapterPreparednessComputer( Chapter request ) {
        this.chapter = request ;
    }

    @Override
    public Void call() throws Exception {
        log.debug( "Computing preparedness for chapter " + 
                   chapter.getChapterId() + " for student " + 
                   chapter.getStudentName() + "\n\tChapter name = " +
                   chapter.getChapterName() ) ;
        
        try {
            double examPreparedness         = 0 ;
            double retention                = 0 ;
            int    numCardsWithLevelChanges = 0 ;
            
            dbo.populateCardDetails( chapter ) ;
            for( Card card : chapter.getCards() ) {
                algo.getExamPreparedness( card ) ;
                examPreparedness += card.getExamPreparedness() ;
                retention += card.getCurrentRetentionValue() ;
                
                if( card.getRevisedLevel() != null ) {
                    numCardsWithLevelChanges++ ;
                }
            }
            
            if( !chapter.getCards().isEmpty() ) {
                int numCards = chapter.getCards().size() ;
                examPreparedness /= numCards ;
                retention /= numCards ;
            }
            
            log.debug( "\t preparedness = " + examPreparedness ) ;
            log.debug( "\t retention    = " + retention ) ;
            
            chapter.setNumCardsWithLevelChanges( numCardsWithLevelChanges ) ;
            chapter.setExamPreparedness( examPreparedness ) ;
            chapter.setRetention( retention ) ;
            
            dbo.updateComputedValues( chapter ) ;
            if( numCardsWithLevelChanges > 0 ) {
                log.debug( "\t num cards cycled = " + numCardsWithLevelChanges ) ;
                recomputeLearningStatusForChapter() ;
            }
            
            reqDbo.deleteRequest( chapter ) ;
        }
        catch( Exception e ) {
            log.error( "Error computing chapter " + chapter.getChapterId() + 
                       " for user " + chapter.getStudentName(), e );
        }
        
        return null ;
    }
    
    private void recomputeLearningStatusForChapter() 
        throws Exception {
        
        String jsonPayload = getAPIRequestPayload() ;
        StringEntity payload = new StringEntity( jsonPayload, 
                                                 ContentType.APPLICATION_JSON ) ;
        
        HttpPost request = new HttpPost( API_URL ) ;
        request.setEntity( payload ) ;
        
        HttpResponse response = JoveNotesBatch.httpClient.execute( request ) ;
        
        HttpEntity entity = response.getEntity() ;
        if( entity != null ) {
            System.out.println( EntityUtils.toString( entity ) ) ;
        }
    }
    
    private String getAPIRequestPayload() {
        
        Map<String, Object> dataMap = new HashMap<String, Object>() ;
        dataMap.put( "studentName", chapter.getStudentName() ) ;
        dataMap.put( "chapterId", chapter.getChapterId() ) ;
        
        Gson gson = new Gson() ;
        return gson.toJson( dataMap ) ;
    }
}
