package com.sandy.jovenotes.jnbatch;

import static org.quartz.CronScheduleBuilder.cronSchedule ;
import static org.quartz.JobBuilder.newJob ;
import static org.quartz.TriggerBuilder.newTrigger ;

import java.util.Date ;
import java.util.Map ;

import org.apache.http.client.CookieStore ;
import org.apache.http.client.HttpClient ;
import org.apache.http.cookie.ClientCookie ;
import org.apache.http.impl.client.BasicCookieStore ;
import org.apache.http.impl.client.HttpClientBuilder ;
import org.apache.http.impl.cookie.BasicClientCookie ;
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
    public static HttpClient httpClient = null ;
    
    private Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler() ;
    
    public JoveNotesBatch( String[] args ) throws Exception {
        initialize( args ) ;
    }
    
    private void initialize( String[] args ) throws Exception {
        log.debug( "Initializing JoveNotes batch." ) ;
        
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
        
        createHttpClient() ;
        log.debug( "\tBatch robot initialized." ) ;
        
        log.info( "JoveNotes batch - initialized." ) ;
        log.info( "" ) ;
    }
    
    private void createHttpClient() throws Exception {
        
        BasicClientCookie cookie = null ;
        cookie = new BasicClientCookie( "auth_token", 
                                        config.getBatchRobotAuthKey() ) ;
        cookie.setPath( "/" ) ;
        cookie.setAttribute( ClientCookie.DOMAIN_ATTR, "true" ) ;
        cookie.setDomain( "localhost" ) ;
        
        CookieStore cookieStore = new BasicCookieStore() ;
        cookieStore.addCookie( cookie );
        
        httpClient = HttpClientBuilder.create()
                                      .setDefaultCookieStore(cookieStore)
                                      .build() ;
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
            log.info( "Scheduled job " + jobId ) ;
            log.info( "\tSchedule = " + jobCfg.getCron() ) ;
        }
        scheduler.start() ;
    }
    
    public static void main( String[] args ) throws Exception {
        log.info( "--------------------------------------------------------" ) ;
        log.info( new Date() ) ;
        log.info( "Starting JoveNotes processor." ) ;
        
        JoveNotesBatch processor = new JoveNotesBatch( args ) ;
        processor.start() ;
    }
}
