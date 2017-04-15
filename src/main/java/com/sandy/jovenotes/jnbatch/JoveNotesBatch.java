package com.sandy.jovenotes.jnbatch;

import static org.quartz.CronScheduleBuilder.cronSchedule ;
import static org.quartz.JobBuilder.newJob ;
import static org.quartz.TriggerBuilder.newTrigger ;

import java.util.Map ;
import java.util.logging.Level;

import org.apache.log4j.Logger;
import org.quartz.JobDetail ;
import org.quartz.Scheduler ;
import org.quartz.Trigger ;
import org.quartz.impl.StdSchedulerFactory ;

import com.sandy.jovenotes.jnbatch.config.ConfigManager ;
import com.sandy.jovenotes.jnbatch.config.JobConfig ;
import com.sandy.jovenotes.jnbatch.util.Database ;

public class JoveNotesBatch {
    
    private static final Logger log = Logger.getLogger( JoveNotesBatch.class ) ;
    
    public static ConfigManager config = null ;
    public static Database db = null ;
    
    private Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler() ;
    
    public JoveNotesBatch( String[] args ) throws Exception {
        initialize( args ) ;
    }
    
    private void initialize( String[] args ) throws Exception {
        log.debug( "Initializing JoveNotes batch." ) ;
        
        java.util.logging.Logger.getLogger("").setLevel( Level.WARNING ) ;
        
        config = new ConfigManager( args ) ;
        if( config.isShowUsage() ) {
            config.printUsage() ;
            System.exit( 0 );
        }
        log.debug( "\tConfigManager initialized." ) ;
        log.info( "\tExecuting JoveNotes batch in " + config.getRunMode() + " mode." );
        
        db = new Database( config.getDatabaseDriverName(), 
                           config.getDatabaseURL(), 
                           config.getDatabaseUser(), 
                           config.getDatabasePassword() ) ;
        db.returnConnection( db.getConnection() ) ;
        log.debug( "\tDatabase initialized." ) ;
        
        log.info( "JoveNotes batch - initialized." ) ;
        log.info( "" ) ;
    }
    
    private void start() throws Exception {
        
        Map<String, JobConfig> jobConfigs = config.getJobConfigMap() ;
        for( String jobId : jobConfigs.keySet() ) {
            JobConfig jobCfg = jobConfigs.get( jobId ) ;
            
            JobDetail job = newJob( jobCfg.getJobClass() )
                            .withIdentity( jobCfg.getJobId() )
                            .withDescription( jobCfg.getJobId() )
                            .usingJobData( jobCfg.getDataMap() )
                            .build() ;
            
            Trigger trigger = newTrigger()
                             .withIdentity( jobId + "Trigger" )
                             .withSchedule( cronSchedule( jobCfg.getCron() ) )
                             .build() ;
            
            scheduler.scheduleJob( job, trigger ) ;
        }
        scheduler.start() ;
    }
    
    public static void main( String[] args ) throws Exception {
        log.info( "Starting JoveNotes processor." ) ;
        
        JoveNotesBatch processor = new JoveNotesBatch( args ) ;
        processor.start() ;
    }
}
