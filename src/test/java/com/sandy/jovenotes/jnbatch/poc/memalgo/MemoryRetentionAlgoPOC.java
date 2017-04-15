package com.sandy.jovenotes.jnbatch.poc.memalgo ;

import java.awt.BorderLayout ;
import java.awt.Container ;
import java.text.SimpleDateFormat ;
import java.util.Date ;

import javax.swing.JFrame ;

import org.apache.log4j.Logger ;

import com.sandy.jovenotes.jnbatch.job.preparedness.algo.RetentionAlgorithm ;
import com.sandy.jovenotes.jnbatch.job.preparedness.vo.Card ;
import com.sandy.jovenotes.jnbatch.job.preparedness.vo.CardRating ;
import com.sandy.jovenotes.jnbatch.job.preparedness.vo.Chapter ;
import com.sandy.jovenotes.jnbatch.util.CardType ;

@SuppressWarnings( "serial" )
public class MemoryRetentionAlgoPOC extends JFrame {
    
    static final Logger log = Logger.getLogger( MemoryRetentionAlgoPOC.class ) ;
    
    private static final String DP = "yyyy-MM-dd HH:mm:ss" ;
    public  static final SimpleDateFormat SDF = new SimpleDateFormat( DP ) ;
    
    private static final String[][] data = {
            { "2016-07-21 20:35:06", "H", "-85", "97"  }, 
            { "2016-07-21 20:39:00", "E", "0"  , "58"  }, 
            { "2016-07-30 23:14:48", "P", "-52", "101" },  
            { "2016-08-02 20:31:09", "H", "-87", "116" },  
            { "2016-08-05 20:49:04", "H", "-59", "40"  }, 
            { "2016-08-05 21:00:53", "H", "-42", "102" },  
            { "2016-08-05 21:03:49", "E", "0"  , "31"  }, 
            { "2016-08-13 20:19:36", "E", "-52", "108" },  
            { "2016-08-16 20:57:02", "E", "32" , "47"  }, 
            { "2016-08-18 12:10:55", "E", "38" , "89"  }, 
            { "2016-08-22 21:08:51", "E", "26" , "46"  }
    } ;
    
    private JNChartPanel             chart   = new JNChartPanel() ;
    private RetentionAlgorithm algo    = null ;

    public MemoryRetentionAlgoPOC() 
        throws Exception {
        
        super( "Memory retention algorithm POC" ) ;
        setUpUI() ;
        
        Chapter chapter = new Chapter() ;
        Card    card    = new Card( chapter, 12345, CardType.FIB ) ;
        
        CardRating prevRating = null ;
        for( String[] ratingInputs : data ) {
            CardRating rating = createCardRating( card, ratingInputs, prevRating ) ;
            card.addRating( rating ) ;
            prevRating = rating ;
        }
        
        algo = new RetentionAlgorithm( card ) ;
        algo.addListener( chart ) ;
        
        super.setBounds( 0, 0, 1800, 500 );
        super.setVisible( true ) ;
    }
    
    private void setUpUI() {
        Container container = super.getContentPane() ;
        container.setLayout( new BorderLayout() ) ;
        container.add( chart ) ;
        
        super.setDefaultCloseOperation( EXIT_ON_CLOSE ) ;
    }
    
    private CardRating createCardRating( Card card, String[] inputs, CardRating prevRating ) 
        throws Exception {
        
        Date date      = SDF.parse( inputs[0] ) ; 
        char rating    = inputs[1].charAt( 0 ) ;
        int  score     = Integer.parseInt( inputs[2] ) ;
        int  timeTaken = Integer.parseInt( inputs[3] ) ;
        
        return new CardRating( card, date, rating, score, timeTaken ) ;
    }
    
    public void simulate() {
        algo.projectRetentionTrajectory() ;
    }
    
    public static void main( String[] args ) throws Exception {
        MemoryRetentionAlgoPOC poc = new MemoryRetentionAlgoPOC() ;
        poc.simulate() ;
    }
}
