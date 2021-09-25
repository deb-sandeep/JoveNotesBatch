package com.sandy.jovenotes.jnbatch.dao ;

import java.sql.Connection ;
import java.sql.PreparedStatement ;
import java.sql.ResultSet ;
import java.util.ArrayList ;
import java.util.List ;

public class ManualTriggerDBO extends AbstractDBO {
    
    public static class ManualTrigger {
        
        public static final int DORMANT = 0 ;
        public static final int ACTIVE = 1 ;
        public static final int IN_PROCESS = 2 ;
        
        private int id = 0 ;
        private String jobName = null ;
        private int triggerFlag = 0 ;
        
        public int getId() {
            return id ;
        }
        
        public String getJobName() {
            return jobName ;
        }

        public int getTriggerFlag() {
            return triggerFlag ;
        }
    }

    public List<ManualTrigger> getActiveTriggers() 
        throws Exception {
        
        final String queryStr = 
                "select " +
                "      t.id, " +
                "      t.job_name,  " +
                "      t.trigger_flag  " +
                "from " +
                "      jove_notes.batch_manual_trigger t " +
                "where " +
                "      t.trigger_flag > 0 " ;
            
        Connection c = null ;
        PreparedStatement psmt = null ;
        ManualTrigger trigger = null ;
        
        List<ManualTrigger> activeTriggers = new ArrayList<>() ;
        
        try {
            c = super.getConnection() ;
            psmt = c.prepareStatement( queryStr ) ;
            
            ResultSet rs = psmt.executeQuery() ;
            
            while( rs.next() ) {
                trigger = processResultRow( rs ) ;
                activeTriggers.add( trigger ) ;
                
            }
        }
        finally {
            if( c != null ) {
                super.releaseConnection( c ) ;
            }
        }
        
        return activeTriggers ;
    }
    
    private ManualTrigger processResultRow( ResultSet rs )
        throws Exception {
        
        int    id          = rs.getInt    ( "id"    ) ;
        String jobName     = rs.getString ( "job_name"  ) ;
        int    triggerFlag = rs.getInt    ( "trigger_flag" ) ;

        ManualTrigger trigger = new ManualTrigger() ;
        trigger.id = id ;
        trigger.jobName = jobName ;
        trigger.triggerFlag = triggerFlag ;
        
        return trigger ;
    }

    public void unarmTrigger( String jobName ) 
        throws Exception {
        
        String queryStr = 
            "update jove_notes.batch_manual_trigger " +
            "set " +
            "    trigger_flag = " + ManualTrigger.DORMANT + " " + 
            "where " +
            "    job_name = ? " ;
        
        Connection conn = null ;
        PreparedStatement psmt = null ;
        
        try {
            conn = super.getConnection() ;
            psmt = conn.prepareStatement( queryStr ) ;
            
            psmt.setString( 1, jobName ) ;
            
            psmt.executeUpdate() ;
        }
        finally {
            if( conn != null ) {
                super.releaseConnection( conn ) ;
            }
        }
    }
    
    public int getJobExecutionState( String jobName ) 
        throws Exception {
        
        final String queryStr = 
                "select " +
                "      t.id, " +
                "      t.job_name,  " +
                "      t.trigger_flag  " +
                "from " +
                "      jove_notes.batch_manual_trigger t " +
                "where " +
                "      t.job_name = ? " ;
            
        Connection c = null ;
        PreparedStatement psmt = null ;
        ManualTrigger trigger = null ;
        int executionState = -1 ;
        
        try {
            c = super.getConnection() ;
            psmt = c.prepareStatement( queryStr ) ;
            
            psmt.setString( 1, jobName ) ;
            
            ResultSet rs = psmt.executeQuery() ;
            
            while( rs.next() ) {
                trigger = processResultRow( rs ) ;
            }
            
            if( trigger != null ) {
                executionState = trigger.getTriggerFlag() ;
            }
        }
        finally {
            if( c != null ) {
                super.releaseConnection( c ) ;
            }
        }
        
        return executionState ;
    }

    public void flagTriggerAsInProgress( String jobName ) 
        throws Exception {
        
        String queryStr = 
            "update jove_notes.batch_manual_trigger " +
            "set " +
            "    trigger_flag = " + ManualTrigger.IN_PROCESS + " " + 
            "where " +
            "    job_name = ? " ;
        
        Connection conn = null ;
        PreparedStatement psmt = null ;
        
        try {
            conn = super.getConnection() ;
            psmt = conn.prepareStatement( queryStr ) ;
            
            psmt.setString( 1, jobName ) ;
            
            psmt.executeUpdate() ;
        }
        finally {
            if( conn != null ) {
                super.releaseConnection( conn ) ;
            }
        }
    }
}
