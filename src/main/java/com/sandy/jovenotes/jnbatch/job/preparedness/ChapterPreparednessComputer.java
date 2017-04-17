package com.sandy.jovenotes.jnbatch.job.preparedness;

import java.util.concurrent.Callable ;

import org.apache.log4j.Logger ;

import com.sandy.jovenotes.jnbatch.dao.ChapterDBO ;
import com.sandy.jovenotes.jnbatch.dao.PrepProcRequestDBO ;
import com.sandy.jovenotes.jnbatch.job.preparedness.algo.RetentionAlgorithm ;
import com.sandy.jovenotes.jnbatch.job.preparedness.vo.Card ;
import com.sandy.jovenotes.jnbatch.job.preparedness.vo.Chapter ;

public class ChapterPreparednessComputer implements Callable<Void> {
    
    private static Logger log = Logger.getLogger( ChapterPreparednessComputer.class ) ;
    
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
                   chapter.getStudentName() ) ;
        
        try {
            double examPreparedness = 0 ;
            
            dbo.populateCardDetails( chapter ) ;
            for( Card card : chapter.getCards() ) {
                double p = algo.getExamPreparedness( card ) ;
                examPreparedness += p ;
            }
            
            if( !chapter.getCards().isEmpty() ) {
                examPreparedness /= chapter.getCards().size() ;
            }
            else {
                examPreparedness = 100 ;
            }
            
            log.debug( "\t preparedness = " + examPreparedness ) ;
            
            chapter.setExamPreparedness( examPreparedness ) ;
            dbo.updateChapterPreparedness( chapter ) ;
            reqDbo.deleteRequest( chapter ) ;
        }
        catch( Exception e ) {
            log.error( "Error computing chapter " + chapter.getChapterId() + 
                       " for user " + chapter.getStudentName(), e );
        }
        
        return null ;
    }
}
