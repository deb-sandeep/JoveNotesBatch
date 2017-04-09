package com.sandy.jovenotes.jnbatch.job.preparedness;

import org.quartz.Job ;
import org.quartz.JobDataMap ;

public class JobConfig {

    private String   jobId = null ;
    private String   cron  = null ;
    
    private Class<? extends Job> clazz = null ;
    private JobDataMap dataMap = new JobDataMap() ;
    
    public JobConfig( String id ) {
        this.jobId = id ;
    }
    
    public String getJobId() {
        return this.jobId ;
    }
    
    public void setJobClass( Class<? extends Job> cls ) {
        this.clazz = cls ;
    }
    
    public Class<? extends Job> getJobClass() {
        return this.clazz ;
    }
    
    public void setCron( String cron ) {
        this.cron = cron ;
    }
    
    public String getCron() {
        return this.cron ;
    }
    
    public JobDataMap getDataMap() {
        return this.dataMap ;
    }
}
