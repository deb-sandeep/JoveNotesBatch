package com.sandy.jovenotes.jnbatch.util ;

/**
 * Utility methods on String.
 * 
 * @author Sandeep
 */
public final class StringUtil {

    public static boolean isEmptyOrNull( final String str ) {
        return ( str == null || "".equals( str.trim() ) ) ;
    }

    public static boolean isNotEmptyOrNull( final String str ) {
        return !isEmptyOrNull( str ) ;
    }
}