package com.sandy.jovenotes.jnbatch.dao ;

import java.sql.Connection ;
import java.sql.PreparedStatement ;
import java.sql.ResultSet ;
import java.util.Date ;

import com.sandy.jovenotes.jnbatch.job.preparedness.vo.Card ;
import com.sandy.jovenotes.jnbatch.job.preparedness.vo.CardRating ;
import com.sandy.jovenotes.jnbatch.job.preparedness.vo.Chapter ;

public class ChapterDBO extends AbstractDBO {

    public void populateCardDetails( Chapter chapter ) 
        throws Exception {
        
        final String queryStr = 
            "select " +
            "    c.card_id, " +
            "    c.card_type, " +
            "    c.difficulty_level, " +
            "    cr.timestamp, " +
            "    cr.rating, " +
            "    cr.score, " +
            "    cr.time_spent " +
            "from " +
            "    jove_notes.card_rating cr, " +
            "    jove_notes.card c " +
            "where " +
            "    cr.card_id = c.card_id and " +
            "    c.chapter_id = ? and " +
            "    student_name = ? " +
            "order by " +
            "    c.card_id asc, " +
            "    cr.timestamp asc " ;
            
        Connection c = null ;
        PreparedStatement psmt = null ;
        
        try {
            c = super.getConnection() ;
            psmt = c.prepareStatement( queryStr ) ;
            
            psmt.setInt   ( 1, chapter.getChapterId()   ) ;
            psmt.setString( 2, chapter.getStudentName() ) ;
            
            ResultSet rs = psmt.executeQuery() ;
            
            while( rs.next() ) {
                processResultRow( chapter, rs ) ;
            }
        }
        finally {
            if( c != null ) {
                super.releaseConnection( c ) ;
            }
        }
    }
    
    private void processResultRow( Chapter chapter, ResultSet rs )
        throws Exception {
        
        int    cardId    = rs.getInt    ( "card_id"    ) ;
        String cardType  = rs.getString ( "card_type"  ) ;
        int    difficulty= rs.getInt    ( "difficulty_level" ) ;
        Date   date      = rs.getDate   ( "timestamp"  ) ;
        char   rating    = rs.getString ( "rating"     ).charAt( 0 ) ;
        int    score     = rs.getInt    ( "score"      ) ;
        int    timeSpent = rs.getInt    ( "time_spent" ) ;

        Card card = chapter.getCard( cardId ) ;
        if( card == null ) {
            card = new Card( chapter, cardId, cardType, difficulty ) ;
            chapter.addCard( card ) ;
        }
        
        card.addRating( new CardRating( card, date, rating, score, timeSpent ) ) ;
    }
}
