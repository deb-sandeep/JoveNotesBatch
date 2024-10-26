package com.sandy.jovenotes.jnbatch.job.revision.dao;

import java.sql.Connection ;
import java.sql.PreparedStatement ;

import com.sandy.jovenotes.jnbatch.dao.AbstractDBO;
import com.sandy.jovenotes.jnbatch.job.revision.vo.Chapter;

public class ChapterDBO extends AbstractDBO {

    public void saveChanges( Chapter chapter )
        throws Exception {

        String queryStr =
            "insert into jove_notes.chapter_preparedness ( " +
            "    student_name, " +
            "    chapter_id, " +
            "    practice_level, " +
            "    retention_score, " +
            "    last_computed_time " +
            ") " +
            "values " +
            "( ?, ?, ?, ?, NOW() ) " +
            "on duplicate key update " +
            "    practice_level = values ( practice_level ), " +
            "    retention_score = values ( retention_score ), " +
            "    last_computed_time = NOW()" ;

        Connection c = null ;
        PreparedStatement psmt = null ;

        try {
            c = super.getConnection() ;
            psmt = c.prepareStatement( queryStr ) ;

            psmt.setString( 1, chapter.getStudentName() ) ;
            psmt.setInt   ( 2, chapter.getChapterId()   ) ;
            psmt.setString( 3, chapter.getPracticeLevel() ) ;
            psmt.setDouble( 4, chapter.getRetentionScore() ) ;

            psmt.executeUpdate() ;
        }
        finally {
            if( c != null ) {
                super.releaseConnection( c ) ;
            }
        }
    }
}
