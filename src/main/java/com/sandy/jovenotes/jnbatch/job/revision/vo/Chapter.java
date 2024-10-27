package com.sandy.jovenotes.jnbatch.job.revision.vo ;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang.StringUtils ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Chapter {

    private final String studentName ;
    private final int    chapterId ;
    private final String syllabusName ;
    private final String subjectName ;
    private final int    chapterNum ;
    private final int    subChapterNum ;
    private final String chapterName ;
    
    private String practiceLevel ;
    private float retentionScore ;
    
    private boolean dirty = false ;
    
    private transient final List<Card> cards = new ArrayList<>() ;
    private transient int numMasteredCards = 0 ;
    
    public Chapter( String studentName,
                    int chapterId,
                    String syllabusName,
                    String subjectName,
                    int chapterNum,
                    int subChapterNum,
                    String chapterName,
                    String practiceLevel ) {
        
        this.studentName   = studentName ;
        this.chapterId     = chapterId ;
        this.syllabusName  = syllabusName ;
        this.subjectName   = subjectName ;
        this.chapterNum    = chapterNum ;
        this.subChapterNum = subChapterNum ;
        this.chapterName   = chapterName ;
        this.practiceLevel = practiceLevel ;
        
        if( this.practiceLevel == null ) {
            setPracticeLevel( "CUR" ) ;
        }
    }
    
    public String getStudentName()  { return studentName ;   }
    public int    getChapterId()    { return chapterId ;     }
    public String getSyllabusName() { return syllabusName ;  }
    public String getSubjectName()  { return subjectName ;   }
    public int    getChapterNum()   { return chapterNum ;    }
    public int    getSubChapterNum(){ return subChapterNum ; }
    public String getChapterName()  { return chapterName ;   }
    
    public boolean isDirty() { return this.dirty ; }
    
    public void setPracticeLevel( String newLevel ) {
        this.practiceLevel = newLevel ;
        this.dirty = true ;
    }
    
    public String getPracticeLevel() { return this.practiceLevel ; }
    
    public boolean isInCurrentMode() { return this.practiceLevel.equals( "CUR" ) ; }
    
    public boolean isInRevisionMode() { return this.practiceLevel.startsWith( "R-" ) ; }
    
    public void setRetentionScore( float score ) {
        this.retentionScore = score ;
        this.dirty = true ;
    }
    
    public float getRetentionScore() { return this.retentionScore ; }
    
    public void addCard( Card card ) {
        cards.add( card ) ;
        if( card.getCurrentLevel().equalsIgnoreCase( "MAS" ) ) {
            numMasteredCards++ ;
        }
    }
    
    public List<Card> getCards() { return this.cards ; }
    
    public int getNumCards() { return this.cards.size() ; }
    
    public int getNumMasteredCards() { return this.numMasteredCards ; }
    
    public boolean areAllCardsMastered() { return  this.numMasteredCards == cards.size() ; }
    
    public int getNumResurrectedCards() {
        int numResurrectedCards = 0 ;
        for( Card card : cards ) {
            if( card.getResurrectionLevel() != null ) {
                numResurrectedCards++ ;
            }
        }
        return numResurrectedCards ;
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create() ;
        return gson.toJson( this ) ;
    }
    
    public String getDisplayName() {
        return "Chapter [id=" + chapterId + "] " +
                this.syllabusName + " > " +
                this.subjectName + " > " +
                this.chapterName ;
    }
}
