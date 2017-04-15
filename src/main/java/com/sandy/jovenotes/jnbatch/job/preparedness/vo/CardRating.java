package com.sandy.jovenotes.jnbatch.job.preparedness.vo;

import java.util.Date ;

import org.apache.commons.lang.StringUtils ;

import com.sandy.jovenotes.jnbatch.poc.memalgo.MemoryRetentionAlgoPOC ;

public class CardRating {

    private Date   date                = null ;
    private char   rating              = 0 ;
    private int    score               = 0 ;
    private int    timeTaken           = 0 ;
    private long   secsSincePrevRating = 0 ;
    
    private Card       card       = null ;
    private CardRating prevRating = null ;
    
    public CardRating( Card card, Date time, char rating, int score, 
                       int timeTaken ) {
        
        this.card      = card ;
        this.date      = time ;
        this.rating    = rating ;
        this.score     = score ;
        this.timeTaken = timeTaken ;
        this.prevRating= card.getLastRating() ;
        initialize() ;
    }
    
    private void initialize() {
        if( this.prevRating != null ) {
            long prevMillis = prevRating.getDate().getTime() ;
            this.secsSincePrevRating = ( date.getTime() - prevMillis ) /1000 ;
        }
    }
    
    public Card getCard() {
        return this.card ;
    }
    
    public Date getDate() {
        return date ;
    }

    public char getRating() {
        return rating ;
    }

    public int getScore() {
        return score ;
    }

    public int getTimeTaken() {
        return timeTaken ;
    }

    public CardRating getPrevRating() {
        return prevRating ;
    }
    
    public long getSecsSincePrevRating() {
        return this.secsSincePrevRating ;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder() ;
        builder.append( StringUtils.rightPad( MemoryRetentionAlgoPOC.SDF.format( date ), 20 ) )
               .append( StringUtils.rightPad( "" + rating, 4 ) )
               .append( StringUtils.rightPad( "" + score, 4 ) )
               .append( StringUtils.rightPad( "" + secsSincePrevRating/3600, 10 ) ) ;
        return builder.toString() ;
    }
}
