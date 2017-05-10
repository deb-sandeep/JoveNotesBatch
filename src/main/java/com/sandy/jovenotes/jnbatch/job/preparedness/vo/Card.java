package com.sandy.jovenotes.jnbatch.job.preparedness.vo;

import java.util.ArrayList ;
import java.util.Date ;
import java.util.List ;

import com.sandy.jovenotes.jnbatch.job.preparedness.algo.Level ;

public class Card {

    private Chapter chapter      = null ;
    private int     cardId       = 0 ;
    private String  cardType     = null ;
    private int     difficulty   = 0 ;
    private String  curLevel     = null ;
    private String  revisedLevel = null ;
    
    private List<CardRating> ratings = new ArrayList<CardRating>() ;
    
    private double  currentRetentionValue = -1 ;
    private double  examPreparedness      = Double.MIN_VALUE ;
    private boolean examPreparednessSet   = false ;
    private Level   proficiencyLevel      = null ;
    private long    secsSinceLastAttempt  = Integer.MAX_VALUE ;
    
    
    public Card( Chapter chapter, int cardId, String cardType, 
                 int difficulty, String curLevel ) {
        
        this.chapter    = chapter ;
        this.cardId     = cardId ;
        this.cardType   = cardType ;
        this.difficulty = difficulty ;
        this.curLevel   = curLevel ;
    }
    
    public void postCreate() {
        if( !ratings.isEmpty() ) {
            Date now = new Date() ;
            CardRating lastRating = ratings.get( ratings.size()-1 ) ;
            
            secsSinceLastAttempt = now.getTime() - lastRating.getDate().getTime() ;
            secsSinceLastAttempt /= 1000 ;
        }
    }
    
    public long getSecondsSinceLastAttempt() {
        return this.secsSinceLastAttempt ;
    }
    
    public String getCurrentLevel() {
        return this.curLevel ;
    }
    
    public Chapter getChapter() {
        return this.chapter ;
    }
    
    public void addRating( CardRating rating ) {
        ratings.add( rating ) ;
    }
    
    public List<CardRating> getRatings() {
        return this.ratings ;
    }
    
    public int getNumAttempts() {
        return this.ratings.size() ;
    }
    
    public int getCardId() {
        return this.cardId ;
    }
    
    public String getCardType() {
        return this.cardType ;
    }
    
    public int getDifficulty() {
        return this.difficulty ;
    }
    
    CardRating getLastRating() {
        if( ratings.size() > 0 ) {
            return ratings.get( ratings.size()-1 ) ;
        }
        return null ;
    }
    
    public void setCurrentRetentionValue( double val ) {
        this.currentRetentionValue = val ;
    }
    
    public double getCurrentRetentionValue() {
        if( currentRetentionValue == -1 ) {
            throw new IllegalStateException( "Retention value has not been calculated" ) ;
        }
        return this.currentRetentionValue ;
    }
    
    public void setExamPreparedness( double val ) {
        this.examPreparedness = val ;
        this.examPreparednessSet = true ;
    }
    
    public double getExamPreparedness() {
        if( !examPreparednessSet ) {
            throw new IllegalStateException( "Exam preparedness not commputed." ) ;
        }
        return this.examPreparedness ;
    }
    
    public void setProficiencyLevel( Level level ) {
        this.proficiencyLevel = level ;
    }
    
    public Level getProficiencyLevel() {
        if( this.proficiencyLevel == null ) {
            throw new IllegalStateException( "Proficiency level not computed." ) ;
        }
        return this.proficiencyLevel ;
    }
    
    public void setRevisedLevel( String level ) {
        this.revisedLevel = level ;
    }
    
    public String getRevisedLevel() {
        return this.revisedLevel ;
    }
}
