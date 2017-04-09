package com.sandy.jovenotes.jnbatch.dao ;

import java.sql.Connection ;

import org.apache.log4j.Logger ;

import com.sandy.jovenotes.jnbatch.JoveNotesBatch ;

public abstract class AbstractDBO {

    private static final Logger log = Logger.getLogger( AbstractDBO.class ) ;
    
    private static boolean logQuery = false ;

    protected static void logQuery( String marker, String query ) {
        if( logQuery ) {
            log.debug( marker + " :: " + query ) ;
        }
    }
    
    protected Connection getConnection() throws Exception {
        return JoveNotesBatch.db.getConnection() ;
    }
    
    protected void releaseConnection( Connection c ) {
        JoveNotesBatch.db.returnConnection( c ) ;
    }
}
