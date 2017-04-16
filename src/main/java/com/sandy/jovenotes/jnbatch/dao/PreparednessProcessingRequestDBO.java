package com.sandy.jovenotes.jnbatch.dao ;

import java.sql.Connection ;
import java.sql.PreparedStatement ;
import java.sql.ResultSet ;
import java.util.ArrayList ;
import java.util.Date ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import com.sandy.jovenotes.jnbatch.job.preparedness.vo.Chapter ;

public class PreparednessProcessingRequestDBO extends AbstractDBO {

    public List<Chapter> getProcessingRequests() 
        throws Exception {
        
        final String queryStr = 
            "select ucp.student_name, ucp.chapter_id, " +
            "       c.subject_name, ucp.is_in_syllabus " +
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
            "        TIMESTAMPDIFF(HOUR, cp.last_computed_time, CURRENT_TIMESTAMP) > 6 " +
            "    ) " +
            "    and " +
            "    ( " +
            "        c.is_exercise_bank = 0 " +
            "    ) " +
            ") " +
            "or " +
            "( " +
            "    cprq.request_time is NOT NULL " +
            ") " ;
        
        List<Chapter> requests = new ArrayList<Chapter>() ;
        Connection c = null ;
        PreparedStatement psmt = null ;
        
        try {
            c = super.getConnection() ;
            psmt = c.prepareStatement( queryStr ) ;
            
            ResultSet rs = psmt.executeQuery() ;
            while( rs.next() ) {
                requests.add( createRequest( rs ) ) ;
            }
            
            populateExamDates( requests ) ;
        }
        finally {
            if( c != null ) {
                super.releaseConnection( c ) ;
            }
        }
        
        return requests ; 
    }
    
    private Chapter createRequest( ResultSet rs ) 
        throws Exception {
        
        Chapter req = new Chapter() ;
        
        req.setStudentName( rs.getString(  "student_name"   ) ) ;
        req.setChapterId  ( rs.getInt(     "chapter_id"     ) ) ;
        req.setSubjectName( rs.getString(  "subject_name"   ) ) ;
        req.setInSyllabus ( rs.getBoolean( "is_in_syllabus" ) ) ;
        
        return req ;
    }
    
    private void populateExamDates( List<Chapter> requests ) 
        throws Exception {
        
        // StudentName ->(*) Subject Name ->(*) Request
        Map<String, Map<String, List<Chapter>>> 
        map = organizeRequestsForExamAssociation( requests ) ;
        
        Connection c = null ;
        PreparedStatement psmt = null ;
        
        final String queryStr = 
            "select student_name, subject, date " +
            "from jove_notes.calendar_event " +
            "where " +
            "    type=\"Exam\" and " +
            "    date > NOW() " +
            "order by " +
            "    date asc" ;
        
        try {
            c = super.getConnection() ;
            psmt = c.prepareStatement( queryStr ) ;
            
            ResultSet rs = psmt.executeQuery() ;
            while( rs.next() ) {
                String stuName = rs.getString( "student_name" ) ;
                String subName = rs.getString( "subject"      ) ;
                Date   date    = rs.getDate  ( "date"         ) ;
                
                Map<String, List<Chapter>> stuMap = null ;
                List<Chapter> subChapterList = null ;
                
                stuMap = map.get( stuName ) ;
                if( stuMap != null ) {
                    subChapterList = stuMap.get( subName ) ;
                    if( subChapterList != null ) {
                        for( Chapter req : subChapterList ) {
                            if( req.getExamDate() == null ) {
                                // Why the null check? If we have two exams
                                // for the same subject marked in the calendar
                                // we pick up the first one.
                                // Prepare for the class test first before
                                // preparing for the terminals.
                                req.setExamDate( date ) ;
                            }
                        }
                    }
                }
            }
        }
        finally {
            if( c != null ) {
                super.releaseConnection( c ) ;
            }
        }
    }
    
    // StudentName ->(*) Subject Name ->(*) Request
    private Map<String, Map<String, List<Chapter>>>
        organizeRequestsForExamAssociation( List<Chapter> requests ) {
        
        Map<String, Map<String, List<Chapter>>> map = 
                new HashMap<String, Map<String,List<Chapter>>>() ;
        
        Map<String, List<Chapter>> stuMap = null ;
        List<Chapter> subChapterList = null ;
        
        for( Chapter req : requests ) {
            
            String subName = req.getSubjectName() ;
            String stuName = req.getStudentName() ;
            
            stuMap = map.get( stuName ) ;
            if( stuMap == null ) {
                stuMap = new HashMap<String, List<Chapter>>() ;
                map.put( stuName, stuMap ) ;
            }
            
            subChapterList = stuMap.get( subName ) ;
            if( subChapterList == null ) {
                subChapterList = new ArrayList<Chapter>() ;
                stuMap.put( subName, subChapterList ) ;
            }

            subChapterList.add( req ) ;
        }
        
        return map ;
    }
}
