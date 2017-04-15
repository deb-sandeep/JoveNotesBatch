package com.sandy.jovenotes.jnbatch.job.preparedness.algo;

import java.util.HashMap ;
import java.util.Map ;

public class Level {
    
    private static Map<Integer, Map<Character, Level>> globalTransitionMap = new HashMap<>() ;
    
    public  static final Level NS = new Level(-1) ;
    private static final Level L0 = new Level(0) ;
    private static final Level L1 = new Level(1) ;
    private static final Level L2 = new Level(2) ;
    private static final Level L3 = new Level(3) ;
    private static final Level L4 = new Level(4) ;
    private static final Level L5 = new Level(5) ;
    
    static {
        NS.addTransition( "EA", L1 ) ;
        NS.addTransition( "PH", L0 ) ;
        
        L0.addTransition( "EA", L1 ) ;
        L0.addTransition( "PH", L0 ) ;
        
        L1.addTransition( "E",  L2 ) ;
        L1.addTransition( "A",  L1 ) ;
        L1.addTransition( "PH", L0 ) ;
        
        L2.addTransition( "E", L3 ) ;
        L2.addTransition( "A", L2 ) ;
        L2.addTransition( "P", L1 ) ;
        L2.addTransition( "H", L0 ) ;
        
        L3.addTransition( "E", L4 ) ;
        L3.addTransition( "A", L3 ) ;
        L3.addTransition( "P", L1 ) ;
        L3.addTransition( "H", L0 ) ;
        
        L4.addTransition( "E", L5 ) ;
        L4.addTransition( "A", L3 ) ;
        L4.addTransition( "P", L1 ) ;
        L4.addTransition( "H", L0 ) ;
        
        L5.addTransition( "E",  L5 ) ;
        L5.addTransition( "A",  L2 ) ;
        L5.addTransition( "PH", L0 ) ;
    }
    
    private int level = 0 ;
    
    private Level( int level ) {
        this.level = level ;
    }
    
    private void addTransition( String ratings, Level nextLevel ) {
        for( int i=0; i<ratings.length(); i++ ) {
            addTransition( ratings.charAt( i ), nextLevel ) ;
        }
    }
    
    private void addTransition( char rating, Level nextLevel ) {
        
        Map<Character, Level> localTransitionMap = null ;
        localTransitionMap = globalTransitionMap.get( this.level ) ;
        
        if( localTransitionMap == null ) {
            localTransitionMap = new HashMap<Character, Level>() ;
            globalTransitionMap.put( this.level, localTransitionMap ) ;
        }
        
        localTransitionMap.put( rating, nextLevel ) ;
    }
    
    public int getLevel() {
        return this.level ;
    }
    
    public Level getNextLevel( char rating ) {
        return globalTransitionMap.get( this.level ).get( rating ) ;
    }
}
