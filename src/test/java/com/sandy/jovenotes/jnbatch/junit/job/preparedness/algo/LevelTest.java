package com.sandy.jovenotes.jnbatch.junit.job.preparedness.algo;

import static org.junit.Assert.assertEquals ;

import org.apache.log4j.Logger ;
import org.junit.Test ;

import com.sandy.jovenotes.jnbatch.job.preparedness.algo.Level ;

public class LevelTest {

    static final Logger log = Logger.getLogger( LevelTest.class ) ;
    
    @Test
    public void getLevel() {
        Level level = Level.NS ;
        String ratings = "HEPHHHEPEE" ;
        
        for( int i=0; i<ratings.length(); i++ ) {
            level = level.getNextLevel( ratings.charAt( i ) ) ;
        }
        assertEquals( 2, level.getLevel() ) ;
    }
}
