package com.sandy.jovenotes.jnbatch.job.preparedness;

import static org.apache.commons.lang.StringUtils.leftPad ;
import static org.apache.commons.lang.StringUtils.rightPad ;

import java.text.DecimalFormat ;
import java.util.ArrayList ;
import java.util.Date ;
import java.util.List ;
import java.util.concurrent.ExecutorService ;
import java.util.concurrent.Executors ;

import org.apache.commons.lang.StringUtils ;
import org.apache.log4j.Logger ;
import org.quartz.DisallowConcurrentExecution ;
import org.quartz.Job ;
import org.quartz.JobDataMap ;
import org.quartz.JobExecutionContext ;
import org.quartz.JobExecutionException ;

import com.sandy.jovenotes.jnbatch.JoveNotesBatch ;
import com.sandy.jovenotes.jnbatch.config.ConfigManager ;
import com.sandy.jovenotes.jnbatch.dao.ManualTriggerDBO ;
import com.sandy.jovenotes.jnbatch.dao.PrepProcRequestDBO ;
import com.sandy.jovenotes.jnbatch.job.preparedness.vo.Chapter ;

@DisallowConcurrentExecution
public class PreparednessComputeJob implements Job {

    private static final Logger log = Logger.getLogger( PreparednessComputeJob.class ) ;
    private static final DecimalFormat DF = new DecimalFormat( "#.##" ) ;
    
    private ExecutorService executor = null ;
    
    @Override
    public void execute( JobExecutionContext context )
            throws JobExecutionException {
        
        String jobName = context.getJobDetail().getKey().getName() ;
        List<ChapterPreparednessComputer> tasks = null ; 
        PrepProcRequestDBO dbo = null ;
        ManualTriggerDBO manualTriggerDBO = null ;
        
        log.debug( "Executing " + jobName + "@" + new Date() ) ;
        
        try {
            dbo = new PrepProcRequestDBO() ;
            manualTriggerDBO = new ManualTriggerDBO() ;
            
            List<Chapter> requests = dbo.getProcessingRequests() ;

            if( requests != null && !requests.isEmpty() ) {
                
                log.info( "Job " + jobName + " @ " + new Date() ) ;
                
                tasks = new ArrayList<ChapterPreparednessComputer>() ;
                for( Chapter request : requests ) {
                    tasks.add( new ChapterPreparednessComputer( request ) ) ;
                }
                
                createExecutor( context ) ;
                if( executor != null ) {
                    executor.invokeAll( tasks ) ;
                    executor.shutdown() ;
                    executor = null ;
                }
                else {
                    log.debug( "Working in development mode. " + 
                               "Running tasks in sequence" ) ;
                    for( ChapterPreparednessComputer task : tasks ) {
                        task.call() ;
                    }
                }
                logResults( requests ) ;
            }
            else {
                log.info( "  No requests for processing found." ) ;
            }
        }
        catch( Exception e ) {
            log.error( "Error in executing " + jobName, e ) ;
            throw new JobExecutionException( e ) ;
        }
        finally {
            if( manualTriggerDBO != null ) {
                try {
                    manualTriggerDBO.unarmTrigger( jobName ) ;
                }
                catch( Exception e ) {
                    log.error( "Could not unarm manual trigger.", e ) ;
                }
            }
        }
    }
    
    private void createExecutor( JobExecutionContext context ) {
        
        if( this.executor == null ) {
            JobDataMap dataMap = context.getJobDetail().getJobDataMap() ;
            int poolSize = dataMap.getInt( ConfigManager.CSK_TP_SIZE ) ;
            
            if( !JoveNotesBatch.config.getRunMode().equals( "development" ) ) {
                executor = Executors.newFixedThreadPool( poolSize ) ;
            }
        }
    }
    
    private void logResults( List<Chapter> chapters ) {
        
        StringBuilder buffer = new StringBuilder() ;
        buffer.append( rightPad( "User", 10 ) )
              .append( rightPad( "Syllabus", 15 ) )
              .append( rightPad( "Subject", 20 ) )
              .append( rightPad( "Chapter", 60 ) )
              .append( leftPad( "Retn", 7 ) )
              .append( leftPad( "Prep", 7 ) )
              .append( leftPad( "#LC", 5 ) ) ;
        
        log.info( buffer ) ;
        log.info( StringUtils.repeat( "-", 135 ) ) ;
        
        for( Chapter chapter : chapters ) {

            String chapterTitle = chapter.getChapterNum() + "." +
                                  chapter.getSubChapterNum() + "::" +
                                  chapter.getChapterName() ;
            
            buffer = new StringBuilder() ;
            buffer.append( rightPad( chapter.getStudentName(), 10 ) )
                  .append( rightPad( StringUtils.left( chapter.getSyllabusName(), 14 ), 15 ) )
                  .append( rightPad( chapter.getSubjectName(), 20 ) )
                  .append( rightPad( chapterTitle, 60 ) )
                  .append( leftPad( DF.format( chapter.getRetention() ), 7 ) )
                  .append( leftPad( DF.format( chapter.getExamPreparedness() ), 7 ) )
                  .append( leftPad( DF.format( chapter.getNumCardsWithLevelChanges() ), 5 ) ) ;
            
            log.info( buffer ) ;
        }
    }
}
