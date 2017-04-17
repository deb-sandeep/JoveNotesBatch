package com.sandy.jovenotes.jnbatch.job.preparedness.vo ;

import java.util.ArrayList ;
import java.util.Date ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.commons.lang.StringUtils ;

public class Chapter {

    private String  studentName  = null ;
    private int     chapterId    = -1 ;
    private String  subjectName  = null ;
    private boolean isInSyllabus = false ;
    private Date    examDate     = null ;
    
    private Map<Integer, Card> cardMap = new HashMap<Integer, Card>() ;
    private List<Card>         cards   = new ArrayList<Card>() ;
    
    private double examPreparedness = 0 ;
    
    public void addCard( Card card ) {
        cards.add( card ) ;
        cardMap.put( card.getCardId(), card ) ;
    }
    
    public List<Card> getCards() {
        return cards ;
    }
    
    public Card getCard( int id ) {
        return cardMap.get( id ) ;
    }
    
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
    
    public void setExamPreparedness( double v ) {
        this.examPreparedness = v ;
    }
    
    public double getExamPreparedness() {
        return this.examPreparedness ;
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
