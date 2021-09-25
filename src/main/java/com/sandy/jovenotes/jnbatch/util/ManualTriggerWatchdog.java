package com.sandy.jovenotes.jnbatch.util;

import java.util.List ;

import org.apache.log4j.Logger ;

import com.sandy.jovenotes.jnbatch.JoveNotesBatch ;
import com.sandy.jovenotes.jnbatch.dao.ManualTriggerDBO ;
import com.sandy.jovenotes.jnbatch.dao.ManualTriggerDBO.ManualTrigger ;

public class ManualTriggerWatchdog extends Thread {
    
    private static final Logger log = Logger.getLogger( ManualTriggerWatchdog.class ) ;

    private JoveNotesBatch batch = null ;
    private ManualTriggerDBO triggerDBO = null ;
    
    public ManualTriggerWatchdog( JoveNotesBatch batch ) {
        super.setDaemon( true ) ;
        this.batch = batch ;
        this.triggerDBO = new ManualTriggerDBO() ;
    }
    
    @Override
    public void run() {
        
        log.info( "Initiating maual trigger watchdog" ) ;
        List<ManualTrigger> activeTriggers = null ;
        
        try {
            while( true ) {
                //log.debug( "Polling for manual trigger." ) ;
                activeTriggers = triggerDBO.getActiveTriggers() ;
                for( ManualTrigger trigger : activeTriggers ) {
                    if( trigger.getTriggerFlag() == ManualTrigger.ACTIVE ) {
                        
                        log.info( "Manually triggering job " + trigger.getJobName() ) ;
                        
                        if( !batch.isJobRunning( trigger.getJobName() ) ) {
                            
                            triggerDBO.flagTriggerAsInProgress( trigger.getJobName() ) ;
                            batch.triggerJob( trigger.getJobName() ) ;
                        }
                        else {
                            log.info( "Skipping manual trigger. Job is executing now" ) ;
                        }
                    }
                }
                Thread.sleep( 60*1000 ) ;
            }
        }
        catch( Exception e ) {
            log.error( "Error in batch watchdog.", e ) ;
        }
    }
}
