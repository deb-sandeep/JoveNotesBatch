package com.sandy.jovenotes.jnbatch.job.revision.vo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Card {

    private transient Chapter chapter = null ;
    
    private final int    cardId ;
    private final String cardType ;
    private final int    difficulty ;
    private final float  avgTimeSpent ;
    private final int    numAttempts ;
    private final int    gapDuration ;
    private final int    absLE ;
    private final String currentLevel ;
    
    private float retentionProbability = 0 ;
    private boolean predictedTestOutcome = false ;
    private String resurrectionLevel = null ;
    
    private boolean dirty = false ;
    
    public Card( Chapter chapter, int cardId, String cardType,
                 int difficulty, float avgTimeSpent, int numAttempts,
                 int gapDuration, int absLE, String currentLevel ) {
        
        this.chapter      = chapter ;
        this.cardId       = cardId ;
        this.cardType     = cardType ;
        this.difficulty   = difficulty ;
        this.avgTimeSpent = avgTimeSpent ;
        this.numAttempts  = numAttempts ;
        this.gapDuration  = gapDuration ;
        this.absLE        = absLE ;
        this.currentLevel = currentLevel ;
    }
    
    public Chapter getChapter()      { return chapter ;      }
    public int     getCardId()       { return cardId ;       }
    public String  getCardType()     { return cardType ;     }
    public int     getDifficulty()   { return difficulty ;   }
    public float   getAvgTimeSpent() { return avgTimeSpent ; }
    public int     getNumAttempts()  { return numAttempts ;  }
    public int     getGapDuration()  { return gapDuration ;  }
    public int     getAbsLE()        { return absLE ;        }
    public String  getCurrentLevel() { return currentLevel;  }
    
    public void setRetentionProbability( float p ) {
        this.retentionProbability = p ;
        this.dirty = true ;
    }
    
    public float getRetentionProbability() {
        return this.retentionProbability ;
    }
    
    public void setPredictedTestOutcome( boolean o ) {
        this.predictedTestOutcome = o ;
        this.dirty = true ;
    }
    
    public boolean getPredictedTestOutcome() {
        return this.predictedTestOutcome ;
    }
    
    public void setResurrectionLevel( String level ) {
        this.resurrectionLevel = level ;
        this.dirty = true ;
    }

    public String getResurrectionLevel() {
        return resurrectionLevel;
    }
    
    public boolean isDirty() { return this.dirty; }
    
    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create() ;
        return gson.toJson( this ) ;
    }
}
