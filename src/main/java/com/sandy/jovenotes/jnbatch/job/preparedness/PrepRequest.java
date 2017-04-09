package com.sandy.jovenotes.jnbatch.job.preparedness ;

import java.util.Date ;

import org.apache.commons.lang.StringUtils ;

public class PrepRequest {

    private String  studentName  = null ;
    private int     chapterId    = -1 ;
    private String  subjectName  = null ;
    private boolean isInSyllabus = false ;
    private Date    examDate     = null ;
    
    public String getStudentName() {
        return studentName ;
    }
    
    public void setStudentName( String studentName ) {
        this.studentName = studentName ;
    }
    
    public int getChapterId() {
        return chapterId ;
    }
    
    public void setChapterId( int chapterId ) {
        this.chapterId = chapterId ;
    }
    
    public String getSubjectName() {
        return subjectName ;
    }
    
    public void setSubjectName( String subjectName ) {
        this.subjectName = subjectName ;
    }
    
    public boolean isInSyllabus() {
        return isInSyllabus ;
    }
    
    public void setInSyllabus( boolean isInSyllabus ) {
        this.isInSyllabus = isInSyllabus ;
    }
    
    public Date getExamDate() {
        return examDate ;
    }
    
    public void setExamDate( Date examDate ) {
        this.examDate = examDate ;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder() ;
        builder.append( StringUtils.rightPad( studentName,       10 ) )
               .append( StringUtils.rightPad( "" + chapterId,     5 ) )
               .append( StringUtils.rightPad( subjectName,       20 ) )
               .append( StringUtils.rightPad( "" + isInSyllabus, 10 ) )
               .append( StringUtils.rightPad( "" + examDate,     12 ) ) ;
        return builder.toString() ;
    }
}
