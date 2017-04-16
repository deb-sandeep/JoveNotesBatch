package com.sandy.jovenotes.jnbatch.poc.memalgo ;

import java.awt.BorderLayout ;
import java.awt.Container ;
import java.awt.FlowLayout ;
import java.awt.event.ActionEvent ;
import java.awt.event.ActionListener ;
import java.util.Calendar ;
import java.util.List ;

import javax.swing.Box ;
import javax.swing.JButton ;
import javax.swing.JFrame ;
import javax.swing.JLabel ;
import javax.swing.JPanel ;

import org.apache.log4j.Logger ;

import com.sandy.jovenotes.jnbatch.JoveNotesBatch ;
import com.sandy.jovenotes.jnbatch.dao.ChapterDBO ;
import com.sandy.jovenotes.jnbatch.job.preparedness.algo.RetentionAlgorithm ;
import com.sandy.jovenotes.jnbatch.job.preparedness.vo.Card ;
import com.sandy.jovenotes.jnbatch.job.preparedness.vo.Chapter ;

@SuppressWarnings( "serial" )
public class MemoryRetentionAlgoPOC extends JFrame 
    implements ActionListener {
    
    static final Logger log = Logger.getLogger( MemoryRetentionAlgoPOC.class ) ;
    
    private JNChartPanel       chart   = new JNChartPanel() ;
    private JButton            nextBtn = new JButton( ">>" ) ;
    private JButton            prevBtn = new JButton( "<<" ) ;
    private JLabel             typeLbl = new JLabel() ;
    private RetentionAlgorithm algo    = null ;
    private Chapter            chapter = null ;
    private List<Card>         cards   = null ;
    private int                curIndex= 0 ;

    public MemoryRetentionAlgoPOC() 
        throws Exception {
        
        super( "Memory retention algorithm POC" ) ;
        setUpUI() ;
        
        algo = new RetentionAlgorithm() ;
        algo.addListener( chart ) ;
        
        createAndPopulateChapter() ;
        
        super.setBounds( 0, 0, 1800, 500 );
    }
    
    private void createAndPopulateChapter() 
        throws Exception {
        
        Calendar examDt = Calendar.getInstance() ;
        examDt.set( 2017, Calendar.MAY, 9 ) ;
        
        chapter = new Chapter() ;
        chapter.setChapterId( 650 ) ;
        chapter.setStudentName( "Deba" ) ;
        chapter.setExamDate( examDt.getTime() );
        
        ChapterDBO dbo = new ChapterDBO() ;
        dbo.populateCardDetails( chapter ) ;
        
        cards = chapter.getCards() ;
    }
    
    private void setUpUI() {
        Container container = super.getContentPane() ;
        container.setLayout( new BorderLayout() ) ;
        container.add( getControlPanel(), BorderLayout.NORTH ) ;
        container.add( chart, BorderLayout.CENTER ) ;
        
        super.setDefaultCloseOperation( EXIT_ON_CLOSE ) ;
    }
    
    private JPanel getControlPanel() {
        JPanel panel = new JPanel() ;
        panel.setLayout( new FlowLayout( FlowLayout.LEFT ) );
        panel.add( prevBtn ) ;
        panel.add( Box.createHorizontalStrut( 10 ) ) ;
        panel.add( nextBtn ) ;
        panel.add( Box.createHorizontalStrut( 10 ) ) ;
        panel.add( typeLbl ) ;
        
        prevBtn.addActionListener( this ) ;
        nextBtn.addActionListener( this ) ;
        
        return panel ;
    }
    
    public void simulate() {
        super.setVisible( true ) ;
        simulateCard() ;
    }
    
    @Override
    public void actionPerformed( ActionEvent e ) {
        if( e.getSource() == prevBtn ) {
            if( curIndex > 0 ) {
                curIndex-- ;
            }
        }
        else {
            if( curIndex < cards.size()-1 ) {
                curIndex++ ;
            }
        }
        
        Thread t = new Thread( new Runnable() {
            @Override
            public void run() {
                simulateCard() ;
            }
        }) ;
        t.start() ;
    }
    
    private void simulateCard() {
        
        prevBtn.setEnabled( false ) ;
        nextBtn.setEnabled( false ) ;
        chart.clear() ;
        
        Card curCard = cards.get( curIndex ) ;
        typeLbl.setText( curCard.getCardType()   + "  ( " + 
                         curCard.getDifficulty() + " )" ) ;
        
        algo.projectRetentionTrajectory( curCard ) ;
        
        prevBtn.setEnabled( curIndex > 0 ) ;
        nextBtn.setEnabled( curIndex < cards.size()-1 ) ;
        
        Card card = cards.get( curIndex ) ;
        algo.getExamPreparedness( card ) ;
        log.debug( card.getExamPreparedness() );
        
        try{ Thread.sleep( 1000 ); }catch( Exception e ){}
        if( curIndex < cards.size()-1 ) {
            curIndex++ ;
            simulateCard() ;
        }
    }
    
    public static void main( String[] args ) throws Exception {
        new JoveNotesBatch( args ) ;
        MemoryRetentionAlgoPOC poc = new MemoryRetentionAlgoPOC() ;
        poc.simulate() ;
    }
}
