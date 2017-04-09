package com.sandy.jovenotes.jnbatch.job.preparedness;

import java.util.concurrent.Callable ;

import org.apache.log4j.Logger ;

public class ChapterPreparednessComputer implements Callable<Void> {
    
    private static Logger log = Logger.getLogger( ChapterPreparednessComputer.class ) ;
    
    private PrepRequest request = null ;
    
    public ChapterPreparednessComputer( PrepRequest request ) {
        this.request = request ;
    }

    @Override
    public Void call() throws Exception {
        log.debug( "Computing preparedness for chapter " + 
                   request.getStudentName() + "::" + request.getChapterId() ) ;
        return null ;
    }
}
