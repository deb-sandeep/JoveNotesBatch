package com.sandy.jovenotes.jnbatch.job.preparedness;

import java.util.ArrayList ;
import java.util.Date ;
import java.util.List ;
import java.util.concurrent.ExecutorService ;
import java.util.concurrent.Executors ;

import org.apache.log4j.Logger ;
import org.quartz.Job ;
import org.quartz.JobDataMap ;
import org.quartz.JobExecutionContext ;
import org.quartz.JobExecutionException ;

import com.sandy.jovenotes.jnbatch.dao.ChapterPreparednessRequestDBO ;
import com.sandy.jovenotes.jnbatch.util.ConfigManager ;

public class PreparednessComputeJob implements Job {

    private static final Logger log = Logger.getLogger( PreparednessComputeJob.class ) ;
    
    private ExecutorService executor = null ;
    
    @Override
    public void execute( JobExecutionContext context )
            throws JobExecutionException {
        
        String jobName = context.getJobDetail().getDescription() ;
        List<ChapterPreparednessComputer> tasks = null ; 
        ChapterPreparednessRequestDBO dbo = null ;
        
        log.debug( "Executing " + jobName + "@" + new Date() ) ;
        
        try {
            dbo = new ChapterPreparednessRequestDBO() ;
            List<PrepRequest> requests = dbo.getRequests() ;

            if( requests != null && !requests.isEmpty() ) {
                
                createExecutor( context ) ;
                tasks = new ArrayList<ChapterPreparednessComputer>() ;
                
                for( PrepRequest request : requests ) {
                    tasks.add( new ChapterPreparednessComputer( request ) ) ;
                }
                executor.invokeAll( tasks ) ;
                executor.shutdown() ;
                executor = null ;
            }
        }
        catch( Exception e ) {
            log.error( "Error in executing " + jobName, e ) ;
            throw new JobExecutionException( e ) ;
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
