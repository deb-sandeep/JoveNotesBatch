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
            "    cls.current_level, " +
            "    cr.timestamp, " +
            "    cr.rating, " +
            "    cr.score, " +
            "    cr.time_spent " +
            "from " +
            "    jove_notes.card_rating cr, " +
            "    jove_notes.card c, " +
            "    jove_notes.card_learning_summary cls " +
            "where " +
            "    cr.card_id = c.card_id and " +
            "    cr.card_id = cls.card_id and " +
            "    cr.student_name = cls.student_name and " +
            "    c.chapter_id = ? and " +
            "    cr.student_name = ? " +
            "order by " +
            "    c.card_id asc, " +
            "    cr.timestamp asc " ;
            
        Connection c = null ;
        PreparedStatement psmt = null ;
        Card curCard = null ;
        
        try {
            c = super.getConnection() ;
            psmt = c.prepareStatement( queryStr ) ;
            
            psmt.setInt   ( 1, chapter.getChapterId()   ) ;
            psmt.setString( 2, chapter.getStudentName() ) ;
            
            ResultSet rs = psmt.executeQuery() ;
            
            while( rs.next() ) {
                curCard = processResultRow( chapter, rs, curCard ) ;
            }
        }
        finally {
            if( c != null ) {
                super.releaseConnection( c ) ;
            }
        }
    }
    
    private Card processResultRow( Chapter chapter, ResultSet rs, Card curCard )
        throws Exception {
        
        int    cardId    = rs.getInt    ( "card_id"    ) ;
        String cardType  = rs.getString ( "card_type"  ) ;
        int    difficulty= rs.getInt    ( "difficulty_level" ) ;
        String curLevel  = rs.getString ( "current_level" ) ;
        Date   date      = rs.getDate   ( "timestamp"  ) ;
        char   rating    = rs.getString ( "rating"     ).charAt( 0 ) ;
        int    score     = rs.getInt    ( "score"      ) ;
        int    timeSpent = rs.getInt    ( "time_spent" ) ;

        Card card = chapter.getCard( cardId ) ;
        if( card == null ) {
            card = new Card( chapter, cardId, cardType, difficulty, curLevel ) ;
            chapter.addCard( card ) ;
            
            if( curCard != null ) {
                curCard.postCreate() ;
            }
        }
        
        card.addRating( new CardRating( card, date, rating, score, timeSpent ) ) ;
        return card ;
    }
    
    public void updateComputedValues( Chapter chapter ) 
        throws Exception {
        
        String queryStr = 
            "insert into jove_notes.chapter_preparedness ( " +
            "    student_name, " + 
            "    chapter_id, " + 
            "    preparedness_score, " + 
            "    retention_score, " + 
            "    last_computed_time " +
            ") " +
            "values " +
            "( ?, ?, ?, ?, NOW() ) " +
            "on duplicate key update " +
            "    preparedness_score = values ( preparedness_score ), " +
            "    retention_score = values ( retention_score ), " +
            "    last_computed_time = NOW()" ;
        
        Connection c = null ;
        PreparedStatement psmt = null ;
        
        try {
            c = super.getConnection() ;
            psmt = c.prepareStatement( queryStr ) ;
            
            psmt.setString( 1, chapter.getStudentName() ) ;
            psmt.setInt   ( 2, chapter.getChapterId()   ) ;
            psmt.setDouble( 3, chapter.getExamPreparedness() ) ;
            psmt.setDouble( 4, chapter.getRetention() ) ;
            
            psmt.executeUpdate() ;
            
            updateCards( chapter, c ) ;
        }
        finally {
            if( c != null ) {
                super.releaseConnection( c ) ;
            }
        }
    }
    
    private void updateCards( Chapter chapter, Connection conn ) 
        throws Exception {
        
        String queryStr = 
            "update jove_notes.card_learning_summary " +
            "set " +
            "    retention_value = ?, " +
            "    exam_preparedness_value = ?, " +
            "    current_level = ? " +
            "where " +
            "    chapter_id = ? and " +
            "    card_id = ? and " +
            "    student_name = ? " ;
        
        PreparedStatement psmt = conn.prepareStatement( queryStr ) ;
        
        for( Card card : chapter.getCards() ) {
            
            String level = card.getRevisedLevel() == null ? 
                           card.getCurrentLevel() : card.getRevisedLevel() ;
                    
            psmt.setDouble( 1, card.getCurrentRetentionValue() ) ;
            psmt.setDouble( 2, card.getExamPreparedness() ) ;
            psmt.setString( 3, level ) ;
            psmt.setInt   ( 4, chapter.getChapterId() ) ;
            psmt.setInt   ( 5, card.getCardId() ) ;
            psmt.setString( 6, chapter.getStudentName() ) ;
            
            psmt.addBatch() ;
        }
        psmt.executeBatch() ;
    }
}
