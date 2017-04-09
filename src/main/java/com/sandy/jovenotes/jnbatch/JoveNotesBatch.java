package com.sandy.jovenotes.jnbatch;

import java.util.List ;
import java.util.logging.Level;

import org.apache.log4j.Logger;

import com.sandy.jovenotes.jnbatch.dao.ChapterPreparednessRequestDBO ;
import com.sandy.jovenotes.jnbatch.job.preparedness.PrepRequest ;
import com.sandy.jovenotes.jnbatch.util.ConfigManager ;
import com.sandy.jovenotes.jnbatch.util.Database ;
import com.sandy.jovenotes.jnbatch.util.Stats ;

/**
 * Main class for the JoveNotes batch application.
 * 
 * @author Sandeep
 */
public class JoveNotesBatch {
    
    private static final Logger log = Logger.getLogger( JoveNotesBatch.class ) ;
    
    public static ConfigManager config = null ;
    public static Database db = null ;
    
    private JoveNotesBatch( String[] args ) throws Exception {
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
        List<PrepRequest> requests = new ChapterPreparednessRequestDBO().getRequests() ;
        for( PrepRequest req : requests ) {
            log.debug( req ) ;
        }
    }
    
    public static void main( String[] args ) throws Exception {
        log.info( "Starting JoveNotes processor." ) ;
        
        JoveNotesBatch processor = new JoveNotesBatch( args ) ;
        processor.start() ;
    }
}
