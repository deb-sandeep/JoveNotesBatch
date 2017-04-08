package com.sandy.jovenotes.jnbatch.job.preparedness;

import org.quartz.Job ;
import org.quartz.JobExecutionContext ;
import org.quartz.JobExecutionException ;

/**
 * This job is used to compute the chapters whose preparedness score needs to
 * be recomputed. Once the identification of the chapters is done, this job
 * makes an entry for recomputation in the rt_chap_prep_proc_req_q (Real time
 * Chapter Preparedness Processing Request Queue) table. This table is 
 * monitored by another job which does the actual recomputation.
 * 
 * Why the separation (identification versus computation)?
 *     Preparedness computation requests can originate from multiple sources. 
 * For example, when a chapter study finishes, request for recomputation is
 * automatically registered. However, preparedness scoring is also done 
 * without user interation (as the score goes stale with time) by this batch.
 * 
 * @author Sandeep
 */
public class ChapterSelectionJob implements Job {

    @Override
    public void execute( JobExecutionContext context )
            throws JobExecutionException {
        
        
    }
}
