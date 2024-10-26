package com.sandy.jovenotes.jnbatch.util ;

public final class StringUtil {
    
    public static String round( float f ) {
        return Float.toString( ( float )(Math.round( f * 100 )/100.0) ) ;
    }

    public static boolean isEmptyOrNull( final String str ) {
        return ( str == null || "".equals( str.trim() ) ) ;
    }

    public static boolean isNotEmptyOrNull( final String str ) {
        return !isEmptyOrNull( str ) ;
    }
}
