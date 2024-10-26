package com.sandy.jovenotes.jnbatch.job.revision;

import java.text.DecimalFormat ;
import java.util.ArrayList ;
import java.util.Date ;
import java.util.List ;
import java.util.concurrent.ExecutorService ;
import java.util.concurrent.Executors ;

import com.sandy.jovenotes.jnbatch.job.revision.algo.RetentionComputer;
import org.apache.log4j.Logger ;
import org.quartz.DisallowConcurrentExecution ;
import org.quartz.Job ;
import org.quartz.JobDataMap ;
import org.quartz.JobExecutionContext ;
import org.quartz.JobExecutionException ;

import com.sandy.jovenotes.jnbatch.JoveNotesBatch ;
import com.sandy.jovenotes.jnbatch.dao.ManualJobTriggerDBO;
import com.sandy.jovenotes.jnbatch.job.revision.dao.PreparednessComputeRequestDBO;
import com.sandy.jovenotes.jnbatch.job.revision.vo.Chapter;

@DisallowConcurrentExecution
public class PreparednessComputeJob implements Job {

    private static final Logger log = Logger.getLogger( PreparednessComputeJob.class ) ;
    private static final DecimalFormat DF = new DecimalFormat( "#.##" ) ;
    
    private static final String CK_TP_SIZE = "threadPoolSize" ;
    private static final String CK_PROJ_DAYS = "futureProjectionDays" ;
    private static final String CK_API_URL = "updateLearningStatsAPIUrl" ;
    
    private static final String DEFAULT_API_URL =
            "http://localhost/jove_notes/api/BatchRobot/UpdateLearningStats" ;
    
    private int threadPoolSize = 1 ;
    private int futureProjectionDays = 0 ;
    private String updateLearningStatsAPIUrl = DEFAULT_API_URL ;
    
    @Override
    public void execute( JobExecutionContext context )
            throws JobExecutionException {
        
        PreparednessComputeRequestDBO requestDBO = new PreparednessComputeRequestDBO() ;
        ManualJobTriggerDBO           triggerDBO = new ManualJobTriggerDBO() ;

        List<PreparednessComputeTask> computeTasks = new ArrayList<>() ;
        
        String jobName = context.getJobDetail().getKey().getName() ;
        log.info( "Executing " + jobName + "@" + new Date() ) ;
        
        try {
            extractConfigurationValues( context ) ;
            
            List<Chapter> chapters = requestDBO.getChapters() ;
            RetentionComputer retentionComputer = new RetentionComputer( futureProjectionDays ) ;

            if( chapters != null && !chapters.isEmpty() ) {
                log.debug( chapters.size() + " chapters being processed for preparedness computation." ) ;
                for( Chapter chapter : chapters ) {
                    computeTasks.add( new PreparednessComputeTask(
                            chapter,
                            retentionComputer,
                            updateLearningStatsAPIUrl
                    ) ) ;
                }
                
                ExecutorService executor = createExecutor( context ) ;
                if( executor != null ) {
                    executor.invokeAll( computeTasks ) ;
                    executor.shutdown() ;
                }
                else {
                    log.debug( "Working in development mode. " + 
                               "Running computeTasks in sequence" ) ;
                    for( PreparednessComputeTask task : computeTasks ) {
                        task.call() ;
                    }
                }
            }
            else {
                log.info( "  No chapters for processing found." ) ;
            }
        }
        catch( Exception e ) {
            log.error( "Error in executing " + jobName, e ) ;
            throw new JobExecutionException( e ) ;
        }
        finally {
            try {
                log.debug( "Disarming manual triggers if any." ) ;
                triggerDBO.disarmTrigger( jobName );
            }
            catch( Exception e ) {
                log.error( "Could not disarm manual trigger.", e );
            }
        }
    }
    
    private void extractConfigurationValues( JobExecutionContext context ) {
        
        JobDataMap dataMap = context.getJobDetail().getJobDataMap() ;
        if( dataMap.containsKey( CK_TP_SIZE ) ) {
            threadPoolSize = dataMap.getInt( CK_TP_SIZE ) ;
        }
        
        if( dataMap.containsKey( CK_PROJ_DAYS ) ) {
            futureProjectionDays = dataMap.getInt( CK_PROJ_DAYS ) ;
        }
        
        if( dataMap.containsKey( CK_API_URL ) ) {
            updateLearningStatsAPIUrl = dataMap.getString( CK_API_URL ) ;
        }
    }
    
    private ExecutorService createExecutor( JobExecutionContext context ) {
        
        ExecutorService executor = null ;
        if( JoveNotesBatch.config.isProdMode() ) {
            
            log.debug( "Creating a fixed thread pool executor of size " + threadPoolSize ) ;
            executor = Executors.newFixedThreadPool( threadPoolSize ) ;
        }
        return executor ;
    }
}
