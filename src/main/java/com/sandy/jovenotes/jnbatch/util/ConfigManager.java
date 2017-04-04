package com.sandy.jovenotes.jnbatch.util;

import java.io.File ;
import java.net.URL ;

import org.apache.commons.cli.CommandLine ;
import org.apache.commons.cli.DefaultParser ;
import org.apache.commons.cli.HelpFormatter ;
import org.apache.commons.cli.Options ;
import org.apache.commons.configuration.PropertiesConfiguration ;
import org.apache.log4j.Logger ;

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
    
    private boolean showUsage          = false ;
    private File    wkspDir            = null ;
    private String  databaseURL        = null ;
    private String  databaseDriverName = null ;
    private String  databaseUser       = null ;
    private String  databasePassword   = null ;
    private String  runMode            = null ;

    public boolean isShowUsage()          { return this.showUsage; }
    public File    getWorkspaceDir()      { return this.wkspDir ; }
    public String  getDatabaseURL()       { return this.databaseURL; }
    public String  getDatabaseDriverName(){ return this.databaseDriverName; }
    public String  getDatabaseUser()      { return this.databaseUser; }
    public String  getDatabasePassword()  { return this.databasePassword; }
    public String  getRunMode()           { return this.runMode; }
    
    // ------------------------------------------------------------------------
    private Options clOptions = null ;
    private boolean logCLP    = false ;
    
    public ConfigManager( String[] args ) throws Exception {
        
        this.clOptions = prepareOptions() ;
        parseCLP( args ) ;
        if( this.showUsage )return ;
        
        PropertiesConfiguration propCfg = new PropertiesConfiguration() ;
        URL cfgURL = ConfigManager.class.getResource( "/config.properties" ) ;
        if( cfgURL == null ) {
            throw new Exception( "config.properties not found in classpath." ) ;
        }
        propCfg.load( cfgURL );
        parseDatabaseConfig( propCfg ) ;
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
        }
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
                          "[--runMode development | production] " ;
        
        HelpFormatter helpFormatter = new HelpFormatter() ;
        helpFormatter.printHelp( 80, usageStr, null, this.clOptions, null ) ;
    }

    private Options prepareOptions() {

        final Options options = new Options() ;
        options.addOption( "h", "Print this usage and exit." ) ;
        options.addOption( null, "dbUser",     true, "The database user name" ) ;
        options.addOption( null, "dbPassword", true, "The database password" ) ;
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
