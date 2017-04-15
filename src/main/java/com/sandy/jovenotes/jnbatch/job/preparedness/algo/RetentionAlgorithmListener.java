package com.sandy.jovenotes.jnbatch.job.preparedness.algo;

import java.util.Date ;

public interface RetentionAlgorithmListener {

    public void handleRetentionLevelChange( String series, Date date, double level ) ;
    
    public void addAnnotation( String text, Date date, double level ) ;
}
