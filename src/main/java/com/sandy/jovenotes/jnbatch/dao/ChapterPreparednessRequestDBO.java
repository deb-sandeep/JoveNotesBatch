package com.sandy.jovenotes.jnbatch.dao ;

import java.sql.Connection ;
import java.sql.PreparedStatement ;
import java.sql.ResultSet ;
import java.util.ArrayList ;
import java.util.Date ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import com.sandy.jovenotes.jnbatch.job.preparedness.PrepRequest ;

public class ChapterPreparednessRequestDBO extends AbstractDBO {

    public List<PrepRequest> getRequests() 
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
            "        TIMESTAMPDIFF(HOUR, cp.last_computed_time, CURRENT_TIMESTAMP) > 5 " +
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
        
        List<PrepRequest> requests = new ArrayList<PrepRequest>() ;
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
    
    private PrepRequest createRequest( ResultSet rs ) 
        throws Exception {
        
        PrepRequest req = new PrepRequest() ;
        
        req.setStudentName( rs.getString(  "student_name"   ) ) ;
        req.setChapterId  ( rs.getInt(     "chapter_id"     ) ) ;
        req.setSubjectName( rs.getString(  "subject_name"   ) ) ;
        req.setInSyllabus ( rs.getBoolean( "is_in_syllabus" ) ) ;
        
        return req ;
    }
    
    private void populateExamDates( List<PrepRequest> requests ) 
        throws Exception {
        
        // StudentName ->(*) Subject Name ->(*) Request
        Map<String, Map<String, List<PrepRequest>>> 
        map = organizeRequestsForExamAssociation( requests ) ;
        
        Connection c = null ;
        PreparedStatement psmt = null ;
        
        final String queryStr = 
            "select student_name, subject, date " +
            "from jove_notes.calendar_event " +
            "where " +
            "    type=\"Exam\"" ;
        
        try {
            c = super.getConnection() ;
            psmt = c.prepareStatement( queryStr ) ;
            
            ResultSet rs = psmt.executeQuery() ;
            while( rs.next() ) {
                String stuName = rs.getString( "student_name" ) ;
                String subName = rs.getString( "subject"      ) ;
                Date   date    = rs.getDate  ( "date"         ) ;
                
                Map<String, List<PrepRequest>> stuMap = null ;
                List<PrepRequest> subChapterList = null ;
                
                stuMap = map.get( stuName ) ;
                if( stuMap != null ) {
                    subChapterList = stuMap.get( subName ) ;
                    if( subChapterList != null ) {
                        for( PrepRequest req : subChapterList ) {
                            req.setExamDate( date ) ;
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
    private Map<String, Map<String, List<PrepRequest>>>
        organizeRequestsForExamAssociation( List<PrepRequest> requests ) {
        
        Map<String, Map<String, List<PrepRequest>>> map = 
                new HashMap<String, Map<String,List<PrepRequest>>>() ;
        
        Map<String, List<PrepRequest>> stuMap = null ;
        List<PrepRequest> subChapterList = null ;
        
        for( PrepRequest req : requests ) {
            
            String subName = req.getSubjectName() ;
            String stuName = req.getStudentName() ;
            
            stuMap = map.get( stuName ) ;
            if( stuMap == null ) {
                stuMap = new HashMap<String, List<PrepRequest>>() ;
                map.put( stuName, stuMap ) ;
            }
            
            subChapterList = stuMap.get( subName ) ;
            if( subChapterList == null ) {
                subChapterList = new ArrayList<PrepRequest>() ;
                stuMap.put( subName, subChapterList ) ;
            }

            subChapterList.add( req ) ;
        }
        
        return map ;
    }
}
