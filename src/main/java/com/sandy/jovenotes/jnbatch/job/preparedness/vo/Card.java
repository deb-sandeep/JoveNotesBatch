package com.sandy.jovenotes.jnbatch.job.preparedness.vo;

import java.util.ArrayList ;
import java.util.List ;

import com.sandy.jovenotes.jnbatch.job.preparedness.algo.Level ;

public class Card {

    private Chapter chapter    = null ;
    private int     cardId     = 0 ;
    private String  cardType   = null ;
    private int     difficulty = 0 ;
    
    private List<CardRating> ratings = new ArrayList<CardRating>() ;
    
    private double  currentRetentionValue = -1 ;
    private double  examPreparedness      = Double.MIN_VALUE ;
    private boolean examPreparednessSet   = false ;
    private Level   proficiencyLevel      = null ;
    
    public Card( Chapter chapter, int cardId, String cardType, int difficulty ) {
        this.chapter    = chapter ;
        this.cardId     = cardId ;
        this.cardType   = cardType ;
        this.difficulty = difficulty ;
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
}
