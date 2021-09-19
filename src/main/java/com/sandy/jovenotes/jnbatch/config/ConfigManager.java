package com.sandy.jovenotes.jnbatch.config;

import java.io.File ;
import java.net.URL ;
import java.util.HashMap ;
import java.util.Iterator ;
import java.util.Map ;

import org.apache.commons.cli.CommandLine ;
import org.apache.commons.cli.DefaultParser ;
import org.apache.commons.cli.HelpFormatter ;
import org.apache.commons.cli.Options ;
import org.apache.commons.configuration.PropertiesConfiguration ;
import org.apache.log4j.Logger ;
import org.quartz.Job ;

import com.sandy.jovenotes.jnbatch.util.StringUtil ;

/**
 * The configuration manager for JoveNotes processor. All the configuration 
 * entities are accessible by getter methods.
 * 
 * @author Sandeep
 */
public class ConfigManager{

    private static Logger log = Logger.getLogger(ConfigManager.class);
    
    private static String CK_DB_DRIVER     = "db.driver" ;
    private static String CK_DB_URL        = "db.url" ;
    private static String CK_DB_USER       = "db.user" ;
    private static String CK_DB_PWD        = "db.password" ;
    private static String CK_JOB_ID_LIST   = "job.id.list" ;
    private static String CK_BR_AUTH_KEY   = "batch.robot.auth.key" ;
    
    private static String CSK_JOB_CLASS    = "class" ;
    private static String CSK_JOB_CRON     = "cron" ;
    public  static String CSK_TP_SIZE      = "threadPoolSize" ;
    
    private boolean showUsage          = false ;
    private File    wkspDir            = null ;
    private String  databaseURL        = null ;
    private String  databaseDriverName = null ;
    private String  databaseUser       = null ;
    private String  databasePassword   = null ;
    private String  brAuthKey          = null ;
    private String  runMode            = null ;
    
    private Map<String, JobConfig> jobConfigMap = new HashMap<String, JobConfig>() ;

    public boolean isShowUsage()          { return this.showUsage; }
    public File    getWorkspaceDir()      { return this.wkspDir ; }
    public String  getDatabaseURL()       { return this.databaseURL; }
    public String  getDatabaseDriverName(){ return this.databaseDriverName; }
    public String  getDatabaseUser()      { return this.databaseUser; }
    public String  getDatabasePassword()  { return this.databasePassword; }
    public String  getBatchRobotAuthKey() { return this.brAuthKey; }
    public String  getRunMode()           { return this.runMode; }
    
    public Map<String, JobConfig> getJobConfigMap() {
        return this.jobConfigMap ;
    }
    
    // ------------------------------------------------------------------------
    private Options clOptions = null ;
    private boolean logCLP    = false ;
    
    public ConfigManager( String[] args ) throws Exception {
        
        this.clOptions = prepareOptions() ;
        parseCLP( args ) ;
        if( this.showUsage )return ;
        
        PropertiesConfiguration propCfg = new PropertiesConfiguration() ;
        propCfg.setListDelimiter( ':' );
        URL cfgURL = ConfigManager.class.getResource( "/config.properties" ) ;
        if( cfgURL == null ) {
            throw new Exception( "config.properties not found in classpath." ) ;
        }
        propCfg.load( cfgURL );
        parseDatabaseConfig( propCfg ) ;
        parseJobConfig( propCfg ) ;
    }
    
    private void parseDatabaseConfig( PropertiesConfiguration config ) 
        throws Exception {
        
        this.databaseDriverName = getMandatoryConfig( CK_DB_DRIVER, config ) ;
        this.databaseURL        = getMandatoryConfig( CK_DB_URL, config ) ;
        
        if( StringUtil.isEmptyOrNull( this.databaseUser ) ) {
            this.databaseUser = config.getString( CK_DB_USER ) ;
        }
        
        if( StringUtil.isEmptyOrNull( this.databasePassword ) ) {
            this.databasePassword = config.getString( CK_DB_PWD ) ;
            if( StringUtil.isEmptyOrNull( this.databasePassword ) ) {
                this.databasePassword = System.getenv( "DB_PASSWORD" ) ;
            }
        }
        
        if( StringUtil.isEmptyOrNull( this.brAuthKey ) ) {
            this.brAuthKey = config.getString( CK_BR_AUTH_KEY ) ;
            if( StringUtil.isEmptyOrNull( this.brAuthKey ) ) {
                this.brAuthKey = System.getenv( "BR_AUTH_KEY" ) ;
                
                if( StringUtil.isEmptyOrNull( this.brAuthKey ) ) {
                    throw new Exception( "Batch robot autentication key not provided." ) ;
                }
            }
        }
    }
    
    private void parseJobConfig( PropertiesConfiguration config ) 
        throws Exception {
        
        String[] jobIdList = config.getStringArray( CK_JOB_ID_LIST ) ;
        if( jobIdList == null || jobIdList.length == 0 ) {
            throw new Exception( "Job id list is missing." ) ; 
        }
        else {
            for( String jobId : jobIdList ) {
                jobConfigMap.put( jobId, getJobConfig( jobId, config ) ) ;
            }
        }
    }
    
    @SuppressWarnings( "unchecked" )
    private JobConfig getJobConfig( String jobId, PropertiesConfiguration config ) 
        throws Exception {
        
        JobConfig jobConfig = new JobConfig( jobId ) ;
        
        PropertiesConfiguration subCfg = getNestedConfig( config, "job." + jobId ) ;
        subCfg.setListDelimiter( ':' );
        
        String clsName = getMandatoryConfig( CSK_JOB_CLASS, subCfg ) ;
        jobConfig.setJobClass( (Class<? extends Job>)Class.forName( clsName ) ) ;
        jobConfig.setCron( subCfg.getString( CSK_JOB_CRON ) );
        
        Iterator<String> iter = subCfg.getKeys() ;
        while( iter.hasNext() ) {
            String key = iter.next() ;
            if( key.equals( CSK_JOB_CLASS ) || 
                key.equals( CSK_JOB_CRON  ) ) {
                continue ;
            }
            jobConfig.getDataMap().put( key, subCfg.getString( key ) ) ;
        }
        
        return jobConfig ;
    }
    
    @SuppressWarnings( "unchecked" )
    public PropertiesConfiguration getNestedConfig( PropertiesConfiguration config, 
                                                    String prefix ) {
        
        PropertiesConfiguration cfg = new PropertiesConfiguration() ;
        cfg.setListDelimiter( ':' );
        
        Iterator<String> iter = config.getKeys( prefix ) ;
        
        while( iter.hasNext() ) {
            String key = iter.next() ;
            String newKey = key.substring( prefix.length() ) ;
            
            if( newKey.startsWith( "." ) ) {
                newKey = newKey.substring( 1 ) ;
            }
            cfg.addProperty( newKey, config.getProperty( key ) ) ;
        }
        
        return cfg ;
    }
    
    private String getMandatoryConfig( String key, PropertiesConfiguration config ) 
        throws Exception {
        
        String value = config.getString( key ) ;
        if( StringUtil.isEmptyOrNull( value ) ) {
            throw new Exception( key + " configuration is missing." ) ;
        }
        return value ;
    }
    
    /**
     * NOTE:
     * 
     * 1. --runMode defaults to development
     * 2. If the same configuration exists in configuration file, then 
     *    command line parameters override the configuration values.
     */
    public void printUsage() {
        
        String usageStr = "JoveNotes [h] [--dbUser <database user>] " + 
                          "[--dbPassword <database password>] " +
                          "[--brAuthKey <batch robot auth key>] " +
                          "[--runMode development | production] " ;
        
        HelpFormatter helpFormatter = new HelpFormatter() ;
        helpFormatter.printHelp( 80, usageStr, null, this.clOptions, null ) ;
    }

    private Options prepareOptions() {

        final Options options = new Options() ;
        options.addOption( "h", "Print this usage and exit." ) ;
        options.addOption( null, "dbUser",     true, "The database user name" ) ;
        options.addOption( null, "dbPassword", true, "The database password" ) ;
        options.addOption( null, "brAuthKey",  true, "The batch robot authorization key" ) ;
        options.addOption( null, "runMode",    true, "Run mode, either 'development' or 'production'" ) ;

        return options ;
    }
    
    private void parseCLP( String[] args ) throws Exception {

        if( this.logCLP ) {
            StringBuilder str = new StringBuilder() ;
            for( String arg : args ) {
                str.append( arg ).append( " " ) ;
            }
            log.debug( "Parsing CL args = " + str ) ;
        }
        
        try {
            CommandLine cmdLine = new DefaultParser().parse( this.clOptions, args ) ;
            
            if( cmdLine.hasOption( 'h' ) ) { this.showUsage = true ; }
            
            this.databaseUser     = cmdLine.getOptionValue( "dbUser" ) ;
            this.databasePassword = cmdLine.getOptionValue( "dbPassword" ) ;
            this.brAuthKey        = cmdLine.getOptionValue( "brAuthKey" ) ;
            this.runMode          = cmdLine.getOptionValue( "runMode" ) ;

            if( this.runMode == null ) {
                log.warn( "runMode is not specified, defaulting to development" ) ;
                this.runMode = "development" ;
            }
            else {
                if( !( this.runMode.equals( "development" ) || 
                       this.runMode.equals( "production" ) ) ) {
                    throw new Exception ( 
                            "Invalid runMode value specified. " + 
                            "Possible values are either 'development' or 'production'" 
                    ) ;
                }
            }
        }
        catch ( Exception e ) {
            log.error( "Error parsing command line arguments.", e ) ;
            printUsage() ;
            throw e ;
        }
    }
}
