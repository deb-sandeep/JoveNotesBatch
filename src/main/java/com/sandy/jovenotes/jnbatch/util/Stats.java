package com.sandy.jovenotes.jnbatch.util;

import org.apache.commons.lang.StringUtils ;
import org.apache.log4j.Logger ;

public class Stats {

    private static final Logger log = Logger.getLogger( Stats.class ) ;
    
    private static Stats instance = new Stats() ;
    
    private static int LOG_LINE_SIZE = 80 ;
    private static int INDENT_L0     = 0 ;
    private static int INDENT_L1     = 4 ;
    
    private static int LG_KEY        = 40 ;
    private static int INT_PAD       = 5 ;
    
    private int currentIndent = 0 ;
    
    public static Stats getInstance() {
        return instance ;
    }

    public static void printStats() {
    }
    
    private void setIndent( int indent ) {
        currentIndent = indent ;
    }
    
    private void log( String text ) {
        log.info( StringUtils.repeat( " ", currentIndent ) + text ) ;
    }
    
    private void log( int keySz, String key, int value ) {
        log( keySz, key, StringUtils.leftPad( "" + value, INT_PAD ) ) ;
    }
    
    private void log( int keySz, String key, String value ) {
        log( StringUtils.rightPad( key, keySz ) + " = " + value ) ;
    }
}
