package com.sandy.jovenotes.jnbatch.util;

import org.apache.commons.lang.StringUtils ;
import org.apache.log4j.Logger ;

public class Stats {

    private static final Logger log = Logger.getLogger( Stats.class ) ;
    
    private static Stats instance = new Stats() ;
    
    private static int LG_KEY = 40 ;
    
    private int currentIndent = 0 ;
    
    public static Stats getInstance() {
        return instance ;
    }

    public static void printStats() {
        instance.setIndent( 4 ) ;
        instance.log( LG_KEY, "Hello", "Zing" ) ;
    }
    
    private void setIndent( int indent ) {
        currentIndent = indent ;
    }
    
    private void log( String text ) {
        log.info( StringUtils.repeat( " ", currentIndent ) + text ) ;
    }
    
    private void log( int keySz, String key, String value ) {
        log( StringUtils.rightPad( key, keySz ) + " = " + value ) ;
    }
}
