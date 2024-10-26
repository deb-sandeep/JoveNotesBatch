package com.sandy.jovenotes.jnbatch.job.revision.dao;

import java.sql.Connection ;
import java.sql.PreparedStatement ;
import java.sql.ResultSet ;
import java.util.ArrayList ;
import java.util.List ;

import com.sandy.jovenotes.jnbatch.dao.AbstractDBO;
import com.sandy.jovenotes.jnbatch.job.revision.vo.Chapter;

public class PreparednessComputeRequestDBO extends AbstractDBO {

    public List<Chapter> getChapters()
        throws Exception {
        
        final String queryStr = 
            "select ucp.student_name, ucp.chapter_id, " +
            "       c.syllabus_name, c.subject_name, c.chapter_num, " +
            "       c.sub_chapter_num, c.chapter_name, cp.practice_level " +
            "from " +
            "    jove_notes.user_chapter_preferences ucp  " +
            "left outer join " +
            "    jove_notes.chapter_preparedness cp " +
            "    on " +
            "    ucp.student_name = cp.student_name and " +
            "    ucp.chapter_id = cp.chapter_id " +
            "left outer join " +
            "    jove_notes.chapter_preparedness_request_queue cprq " +
            "    on " +
            "    ucp.student_name = cprq.student_name and " +
            "    ucp.chapter_id = cprq.chapter_id " +
            "left outer join " +
            "    jove_notes.chapter c " +
            "    on " +
            "    ucp.chapter_id = c.chapter_id " +
            "where " +
            "( " +
            "    (  " +
            "        ucp.is_hidden = 0 or " +
            "        ucp.is_in_syllabus = 1 " +
            "    )  " +
            "    and " +
            "    ( " +
            "        cp.last_computed_time is NULL or " +
            "        TIMESTAMPDIFF(HOUR, cp.last_computed_time, CURRENT_TIMESTAMP) >= 6 " +
            "    ) " +
            "    and " +
            "    ( " +
            "        c.is_exercise_bank = 0 " +
            "    ) " +
            ") " +
            "or " +
            "( " +
            "    cprq.request_time is NOT NULL " +
            ") " +
            "order by " +
            "   ucp.chapter_id ";
        
        List<Chapter> requests = new ArrayList<Chapter>() ;
        
        Connection        c    = null ;
        PreparedStatement psmt = null ;
        
        try {
            c = super.getConnection() ;
            psmt = c.prepareStatement( queryStr ) ;
            
            ResultSet rs = psmt.executeQuery() ;
            while( rs.next() ) {
                requests.add(
                    new Chapter(
                        rs.getString(  "student_name"    ),
                        rs.getInt(     "chapter_id"      ),
                        rs.getString(  "syllabus_name"   ),
                        rs.getString(  "subject_name"    ),
                        rs.getInt(     "chapter_num"     ),
                        rs.getInt(     "sub_chapter_num" ),
                        rs.getString(  "chapter_name"    ),
                        rs.getString(  "practice_level"  )
                    )
                ) ;
            }
        }
        finally {
            if( c != null ) {
                super.releaseConnection( c ) ;
            }
        }
        return requests ;
    }
    
    public void deleteRequest( Chapter chapter )
        throws Exception {
        
        final String queryStr = 
            "delete from " +
            "jove_notes.chapter_preparedness_request_queue " +
            "where " +
            "    chapter_id=? and " +
            "    student_name=? " ;
        
        Connection c = null ;
        PreparedStatement psmt = null ;
        
        try {
            c = super.getConnection() ;
            psmt = c.prepareStatement( queryStr ) ;
            
            psmt.setInt   ( 1, chapter.getChapterId() ) ;
            psmt.setString( 2, chapter.getStudentName() ) ;
            
            psmt.executeUpdate() ;
        }
        finally {
            if( c != null ) {
                super.releaseConnection( c ) ;
            }
        }
    }
}
