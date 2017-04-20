package com.sandy.jovenotes.jnbatch.util;

import java.util.ArrayList ;
import java.util.List ;

public class CardLevel {

    public static final String NS  = "NS" ;
    public static final String L0  = "L0" ;
    public static final String L1  = "L1" ;
    public static final String L2  = "L2" ;
    public static final String L3  = "L3" ;
    public static final String MAS = "MAS" ;
    
    private static final List<String> LEVELS = new ArrayList<String>() ;
    static {
        LEVELS.add( NS  ) ;
        LEVELS.add( L0  ) ;
        LEVELS.add( L1  ) ;
        LEVELS.add( L2  ) ;
        LEVELS.add( L3  ) ;
        LEVELS.add( MAS ) ;
    }
    
    public static String getPreviousLevel( String level ) {
        
        int index = LEVELS.indexOf( level ) ;
        if( index == -1 ) {
            throw new IllegalArgumentException( "Invalid level " + level ) ;
        }
        
        if( index == 0 ) {
            return LEVELS.get( 0 ) ;
        }
        else {
            return LEVELS.get( index-1 ) ;
        }
    }
}
