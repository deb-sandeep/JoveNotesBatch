package com.sandy.jovenotes.jnbatch.job.preparedness;

import java.text.DecimalFormat ;
import java.util.ArrayList ;
import java.util.Date ;
import java.util.List ;
import java.util.concurrent.ExecutorService ;
import java.util.concurrent.Executors ;

import org.apache.commons.lang.StringUtils ;
import org.apache.log4j.Logger ;
import org.quartz.Job ;
import org.quartz.JobDataMap ;
import org.quartz.JobExecutionContext ;
import org.quartz.JobExecutionException ;

import com.sandy.jovenotes.jnbatch.config.ConfigManager ;
import com.sandy.jovenotes.jnbatch.dao.PrepProcRequestDBO ;
import com.sandy.jovenotes.jnbatch.job.preparedness.vo.Chapter ;

import static org.apache.commons.lang.StringUtils.* ;

public class PreparednessComputeJob implements Job {

    private static final Logger log = Logger.getLogger( PreparednessComputeJob.class ) ;
    private static final DecimalFormat DF = new DecimalFormat( "#.##" ) ;
    
    private ExecutorService executor = null ;
    
    @Override
    public void execute( JobExecutionContext context )
            throws JobExecutionException {
        
        String jobName = context.getJobDetail().getDescription() ;
        List<ChapterPreparednessComputer> tasks = null ; 
        PrepProcRequestDBO dbo = null ;
        
        log.debug( "Executing " + jobName + "@" + new Date() ) ;
        
        try {
            dbo = new PrepProcRequestDBO() ;
            List<Chapter> requests = dbo.getProcessingRequests() ;

            if( requests != null && !requests.isEmpty() ) {
                
                log.info( "Job " + jobName + " @ " + new Date() ) ;
                
                createExecutor( context ) ;
                tasks = new ArrayList<ChapterPreparednessComputer>() ;
                
                for( Chapter request : requests ) {
                    tasks.add( new ChapterPreparednessComputer( request ) ) ;
                }
                executor.invokeAll( tasks ) ;
                executor.shutdown() ;
                executor = null ;

                logResults( requests ) ;
            }
        }
        catch( Exception e ) {
            log.error( "Error in executing " + jobName, e ) ;
            throw new JobExecutionException( e ) ;
        }
    }
    
    private void logResults( List<Chapter> chapters ) {
        
        StringBuilder buffer = new StringBuilder() ;
        buffer.append( rightPad( "User", 10 ) )
              .append( rightPad( "Syllabus", 10 ) )
              .append( rightPad( "Subject", 20 ) )
              .append( rightPad( "Chapter", 60 ) )
              .append( leftPad( "Retn", 7 ) )
              .append( leftPad( "Prep", 7 ) ) ;
        
        log.info( buffer ) ;
        log.info( StringUtils.repeat( "-", 115 ) ) ;
        
        for( Chapter chapter : chapters ) {

            String chapterTitle = chapter.getChapterNum() + "." +
                                  chapter.getSubChapterNum() + "::" +
                                  chapter.getChapterName() ;
            
            buffer = new StringBuilder() ;
            buffer.append( rightPad( chapter.getStudentName(), 10 ) )
                  .append( rightPad( chapter.getSyllabusName(), 10 ) )
                  .append( rightPad( chapter.getSubjectName(), 20 ) )
                  .append( rightPad( chapterTitle, 60 ) )
                  .append( leftPad( DF.format( chapter.getRetention() ), 7 ) )
                  .append( leftPad( DF.format( chapter.getExamPreparedness() ), 7 ) ) ;
            
            log.info( buffer ) ;
        }
    }
    
    private void createExecutor( JobExecutionContext context ) {
        
        if( this.executor == null ) {
            JobDataMap dataMap = context.getJobDetail().getJobDataMap() ;
            int poolSize = dataMap.getInt( ConfigManager.CSK_TP_SIZE ) ;
            executor = Executors.newFixedThreadPool( poolSize ) ;
        }
    }
}
