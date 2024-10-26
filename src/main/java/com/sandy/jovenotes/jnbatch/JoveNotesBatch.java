package com.sandy.jovenotes.jnbatch;

import static org.quartz.CronScheduleBuilder.cronSchedule ;
import static org.quartz.JobBuilder.newJob ;
import static org.quartz.TriggerBuilder.newTrigger ;

import java.util.Date ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.http.client.CookieStore ;
import org.apache.http.client.HttpClient ;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.cookie.ClientCookie ;
import org.apache.http.impl.client.BasicCookieStore ;
import org.apache.http.impl.client.HttpClientBuilder ;
import org.apache.http.impl.cookie.BasicClientCookie ;
import org.apache.log4j.Logger;
import org.quartz.JobDetail ;
import org.quartz.JobExecutionContext ;
import org.quartz.JobKey ;
import org.quartz.Scheduler ;
import org.quartz.Trigger ;
import org.quartz.impl.StdSchedulerFactory ;

import com.sandy.jovenotes.jnbatch.config.ConfigManager ;
import com.sandy.jovenotes.jnbatch.config.JobConfig ;
import com.sandy.jovenotes.jnbatch.util.Database ;
import com.sandy.jovenotes.jnbatch.util.ManualTriggerWatchdog ;

public class JoveNotesBatch {
    
    private static final Logger log = Logger.getLogger( JoveNotesBatch.class ) ;
    
    public static ConfigManager config = null ;
    public static Database db = null ;
    public static HttpClient httpClient = null ;
    
    private Scheduler scheduler = null ;
    private ManualTriggerWatchdog manualTriggerWatchdog = null ;
    private Map<String, JobKey> jobKeyMap = new HashMap<String, JobKey>() ;
    
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
        log.info( "\tExecuting JoveNotes batch in " + (config.isDevMode() ? "dev" : "prod") + " mode." );
        
        initializeDatabase() ;
        log.debug( "\tDatabase initialized." ) ;
        
        createHttpClient() ;
        log.debug( "\tBatch robot initialized." ) ;
        
        scheduler = StdSchedulerFactory.getDefaultScheduler() ;
        log.debug( "\tScheduler initiatlized." ) ;
        
        manualTriggerWatchdog = new ManualTriggerWatchdog( this ) ;
        manualTriggerWatchdog.start() ;
        log.debug( "\tManual trigger watchdog started." ) ;
        
        log.info( "JoveNotes batch - initialized. " + new Date() ) ;
        log.info( "" ) ;
    }
    
    private void initializeDatabase() throws Exception {
        
        int attemptCount = 0 ;
        boolean continueTrying = true ;
        
        while( continueTrying ) {
            try {
                attemptCount++ ;
                db = new Database( config.getDatabaseDriverName(), 
                        config.getDatabaseURL(), 
                        config.getDatabaseUser(), 
                        config.getDatabasePassword() ) ;
                
                db.returnConnection( db.getConnection() ) ;
                continueTrying = false  ;
            }
            catch( Exception e ) {
                if( attemptCount >= 10 ) {
                    throw new Exception( "Database unavailable.", e ) ; 
                }
                else {
                    log.info( "Database connection not established. Will try again" ) ;
                    Thread.sleep( 30000 ) ;
                }
            }
        }
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
        
        RequestConfig requestConfig = RequestConfig.custom()
                                                   .setConnectionRequestTimeout( 30*1000 )
                                                   .setConnectTimeout( 30*1000 )
                                                   .setSocketTimeout( 30*1000 )
                                                   .build() ;
        
        httpClient = HttpClientBuilder.create()
                                      .setDefaultCookieStore(cookieStore)
                                      .setDefaultRequestConfig( requestConfig )
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
            
            registerJob( job, trigger ) ;

            log.info( "Scheduled job " + jobId ) ;
            log.info( "\tSchedule = " + jobCfg.getCron() ) ;
        }
        scheduler.start() ;
    }
    
    
    private void registerJob( JobDetail jobDetail, Trigger trigger ) 
        throws Exception {

        JobKey jobKey = jobDetail.getKey() ;
        
        this.scheduler.scheduleJob( jobDetail, trigger ) ;
        this.jobKeyMap.put( jobKey.getName(), jobKey ) ;
    }
        
    public void triggerJob( String jobName ) throws Exception {
        JobKey key = this.jobKeyMap.get( jobName ) ;
        if( key != null ) {
            this.scheduler.triggerJob( key ) ;
        }
        else {
            throw new IllegalArgumentException( "Job with the name " + 
                                                jobName + 
                                                " is not registered." ) ;
        }
    }
    
    public boolean isJobRunning( String jobName ) 
        throws Exception {
        
        List<JobExecutionContext> currentJobs = null ;
        
        currentJobs = scheduler.getCurrentlyExecutingJobs() ;
        
        for( JobExecutionContext ctx : currentJobs ) {
            String thisJobName = ctx.getJobDetail().getKey().getName() ;
            if( jobName.equalsIgnoreCase( thisJobName ) ) {
                return true ;
            }
        }
        
        return false;    
    }
    
    public static void main( String[] args ) throws Exception {
        log.info( "--------------------------------------------------------" ) ;
        log.info( new Date() ) ;
        log.info( "Starting JoveNotes batch." ) ;
        
        JoveNotesBatch processor = new JoveNotesBatch( args ) ;
        processor.start() ;
    }
}
