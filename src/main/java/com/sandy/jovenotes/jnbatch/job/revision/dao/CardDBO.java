package com.sandy.jovenotes.jnbatch.job.revision.dao;

import com.sandy.jovenotes.jnbatch.dao.AbstractDBO;
import com.sandy.jovenotes.jnbatch.job.revision.vo.Card;
import com.sandy.jovenotes.jnbatch.job.revision.vo.Chapter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CardDBO extends AbstractDBO {

    public void populateCards( Chapter chapter )
        throws Exception {
        
        final String queryStr =
                "select " +
                "      c.card_id, " +
                "      c.card_type, " +
                "      c.difficulty_level, " +
                "      cls.total_time_spent / cls.num_attempts as av_time, " +
                "      cls.num_attempts, " +
                "      DATEDIFF(CURDATE(), cls.last_attempt_time) as gap_duration, " +
                "      cls.abs_learning_efficiency, " +
                "      cls.learning_efficiency, " +
                "      cls.current_level " +
                "from " +
                "      jove_notes.card c " +
                "left outer join " +
                "      jove_notes.card_learning_summary cls " +
                "      on " +
                "      cls.card_id = c.card_id " +
                "where " +
                "      c.chapter_id = ? and " +
                "      cls.student_name = ? " +
                "order by " +
                "    c.card_id asc" ;
        
        Connection c = null ;
        PreparedStatement psmt ;
        
        try {
            c = super.getConnection() ;
            psmt = c.prepareStatement( queryStr ) ;
            
            psmt.setInt   ( 1, chapter.getChapterId() ) ;
            psmt.setString( 2, chapter.getStudentName() ) ;
            
            ResultSet rs = psmt.executeQuery() ;
            
            while( rs.next() ) {
                chapter.addCard( createCardFromResultSet( chapter, rs ) ) ;
            }
        }
        finally {
            if( c != null ) {
                super.releaseConnection( c ) ;
            }
        }
    }
    
    private Card createCardFromResultSet( Chapter chapter, ResultSet rs )
        throws Exception {
        
        int    cardId       = rs.getInt    ( "card_id"                 ) ;
        String cardType     = rs.getString ( "card_type"               ) ;
        int    difficulty   = rs.getInt    ( "difficulty_level"        ) ;
        float  avgTimeSpent = rs.getFloat  ( "av_time"                 ) ;
        int    numAttempts  = rs.getInt    ( "num_attempts"            ) ;
        int    gapDuration  = rs.getInt    ( "gap_duration"            ) ;
        int    absLE        = rs.getInt    ( "abs_learning_efficiency" ) ;
        int    le           = rs.getInt    ( "learning_efficiency"     ) ;
        String currentLevel = rs.getString ( "current_level"           ) ;
        
        return new Card(
                chapter, cardId, cardType, difficulty,
                avgTimeSpent, numAttempts, gapDuration, absLE, le, currentLevel
        ) ;
    }
    
    public void saveChanges( Chapter chapter )
        throws Exception {

        String queryStr =
            "update jove_notes.card_learning_summary " +
            "set " +
            "    retention_value = ?, " +
            "    predicted_outcome_next_attempt = ?, " +
            "    current_level = ? " +
            "where " +
            "    chapter_id = ? and " +
            "    card_id = ? and " +
            "    student_name = ? " ;
        
        Connection conn = null ;
        PreparedStatement psmt  ;
        
        try {
            conn = super.getConnection() ;
            conn.setAutoCommit( false ) ;
            psmt = conn.prepareStatement( queryStr ) ;
            
            for( Card card : chapter.getCards() ) {
                if( card.isDirty() ) {
                    String level = card.getResurrectionLevel() == null ?
                                     card.getCurrentLevel() :
                                     card.getResurrectionLevel() ;
                    
                    psmt.setDouble ( 1, card.getRetentionProbability() ) ;
                    psmt.setBoolean( 2, card.getPredictedTestOutcome() ) ;
                    psmt.setString ( 3, level ) ;
                    psmt.setInt    ( 4, chapter.getChapterId() ) ;
                    psmt.setInt    ( 5, card.getCardId() ) ;
                    psmt.setString ( 6, chapter.getStudentName() ) ;
                    
                    psmt.addBatch() ;
                }
            }
            psmt.executeBatch() ;
            conn.commit() ;
            conn.setAutoCommit( true ) ;
        }
        finally {
            if( conn != null ) {
                super.releaseConnection( conn ) ;
            }
        }
    }
}
