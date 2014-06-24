package com.prudential.wcm.tasks;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.Collections;
import java.util.List;
import java.util.TimerTask;

import com.ibm.workplace.wcm.api.WorkflowedDocument;
import com.prudential.wcm.DocumentHandle;

/**
 * Timer Task for executing a runnable task
 * 
 * @author Luke Carpenter
 */
public class WCMDocumentTimerTask extends TimerTask {
   private static Logger s_log = Logger.getLogger(WCMDocumentTimerTask.class.getName());

   /**
    * Handle to a WCM Document
    */
   private DocumentHandle handle;

   /**
    * Runnable task to execute when timer task fires
    */
   private Runnable task;

   /**
    * List of valid stages for time task to execute task
    * If the document is not in a valid stage the task is canceled
    * If the list is empty no validation is done
    */
   private List<String> validRunStages;

   /**
    * Counter to track number of times the timer has triggered.
    * This counter is incremented regardless of the outcome of the runnable task
    */
   private int counter = 0;

   /**
    * Task's maximum number of runs before being canceled if -1 then there is no maximum.
    * 
    * @see counter
    */
   private int maxRuns = -1;

   public WCMDocumentTimerTask(DocumentHandle handle, Runnable task, List<String> validRunStages) {
      super();
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("WCMDocumentTimerTask", "WCMDocumentTimerTask " + handle + " task = " + task + " validRunStages " + validRunStages);
      }

      this.handle = handle;
      this.task = task;
      this.validRunStages = validRunStages;
   }

   public WCMDocumentTimerTask(DocumentHandle handle, Runnable task) {
      super();
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      
      this.handle = handle;
      this.task = task;
      this.validRunStages = Collections.emptyList();
      if (isDebug) {
         s_log.entering("WCMDocumentTimerTask", "WCMDocumentTimerTask " + handle + " task = " + task + " validRunStages " + validRunStages);
      }
   }

   public WCMDocumentTimerTask(DocumentHandle handle, Runnable task, List<String> validRunStages, int maxRuns) {
      super();
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      
      this.handle = handle;
      this.task = task;
      this.validRunStages = validRunStages;
      this.maxRuns = maxRuns;
      if (isDebug) {
         s_log.entering("WCMDocumentTimerTask", "WCMDocumentTimerTask " + handle + " task = " + task + " validRunStages " + validRunStages+" maxRuns = "+maxRuns);
      }
   }

   public WCMDocumentTimerTask(DocumentHandle handle, Runnable task, int maxRuns) {
      super();
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      this.handle = handle;
      this.task = task;
      this.validRunStages = Collections.emptyList();
      this.maxRuns = maxRuns;
      if (isDebug) {
         s_log.entering("WCMDocumentTimerTask", "WCMDocumentTimerTask " + handle + " task = " + task + " validRunStages " + validRunStages+" maxRuns = "+maxRuns);
      }
   }

   public int getMaxRuns() {
      return maxRuns;
   }

   public int getCounter() {
      return counter;
   }

   @Override
   public void run() {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      
      if (isDebug) {
         s_log.entering("WCMDocumentTimerTask", "run");
      }
           
      counter += 1;
      try {
         handle.init();
         boolean runTask = true;
         
         /* don't need to worry about valid stages
         if (validRunStages != null && !validRunStages.isEmpty()) {
            WorkflowedDocument wfd = (WorkflowedDocument) handle.getDocument();
            String stage = wfd.getWorkflowStageId().getId();
            if (!validRunStages.contains(stage)) {
               //runTask = false;
            }
         }
         */

         boolean cancel = false;
         if (runTask && task != null) {
            if (isDebug) {
               s_log.log(Level.FINEST, "runTask && task != null");
            }
            task.run();
         }
         else {
            if (isDebug) {
               s_log.log(Level.FINEST, " setting cancel to true as runTask was false or the task was null");
            }
            cancel = true;
         }

         if (maxRuns > -1 && counter >= maxRuns) {
            cancel = true;
         }
         else if (maxRuns == -1) {
            cancel = true;
         }

         if (cancel) {
            this.cancel();
         }
      }
      catch (Exception ex) {
         // TODO: error handling
         if (isDebug) {
            s_log.log(Level.FINEST, " ex "+ex.getMessage());
            ex.printStackTrace();
         }
      }
      finally {
         handle.close();
      }
   }
}
