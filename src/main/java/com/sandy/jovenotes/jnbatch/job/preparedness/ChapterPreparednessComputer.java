package com.sandy.jovenotes.jnbatch.job.preparedness;

import java.util.concurrent.Callable ;

import org.apache.log4j.Logger ;

import com.sandy.jovenotes.jnbatch.job.preparedness.vo.Chapter ;

public class ChapterPreparednessComputer implements Callable<Void> {
    
    private static Logger log = Logger.getLogger( ChapterPreparednessComputer.class ) ;
    
    private Chapter request = null ;
    
    public ChapterPreparednessComputer( Chapter request ) {
        this.request = request ;
    }

    @Override
    public Void call() throws Exception {
        log.debug( "Computing preparedness for chapter " + 
                   request.getStudentName() + "::" + request.getChapterId() ) ;
        return null ;
    }
}
