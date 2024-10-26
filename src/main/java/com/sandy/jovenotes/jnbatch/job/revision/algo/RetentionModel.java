package com.sandy.jovenotes.jnbatch.job.revision.algo;

public class RetentionModel {
    
    String cardType = null ;
    
    float coeffSubjectNum      = 0 ;
    float coeffDifficultyLevel = 0 ;
    float coeffTimeSpent       = 0 ;
    float coeffAttemptNum      = 0 ;
    float coeffGapDuration     = 0 ;
    float coeffLearningEff     = 0 ;
    float intercept            = 0 ;
    float threshold            = 0 ;
    
    RetentionModel( String cardType ) {
        this.cardType = cardType ;
    }
}
