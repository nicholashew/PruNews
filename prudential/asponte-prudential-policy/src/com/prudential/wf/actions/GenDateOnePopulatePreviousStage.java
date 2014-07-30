/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.wf.actions;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.WebContentCustomWorkflowService;
import com.ibm.workplace.wcm.api.exceptions.PropertyRetrievalException;
public class GenDateOnePopulatePreviousStage extends AbstractPopulatePreviousStageNecessary {

   private static String s_delayCmpntName = "ApproveReadyReminderDelay";
   
   public GenDateOnePopulatePreviousStage(WebContentCustomWorkflowService p_customWorkflowService) {
      // TODO Auto-generated constructor stub
      super(p_customWorkflowService);
      
   }

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(GenDateOnePopulatePreviousStage.class.getName());

   @Override
   public Date getExecuteDate(Document p_arg0) {
      // TODO Auto-generated method stub      
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("GenDateOnePopulatePreviousStage", "getExecuteDate");
      }
      
      Date theDate = new Date();
      if(p_arg0 instanceof Content) {
         Content theContent = (Content)p_arg0;
         try {
            Date genDateOne = theContent.getGeneralDateOne();
            if(genDateOne != null) {
               theDate = genDateOne;
            }
         }
         catch (PropertyRetrievalException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST))
            {
               s_log.log(Level.FINEST, "", e);
            }
         }
      }
      
      if (isDebug) {
         s_log.exiting("GenDateOnePopulatePreviousStage", "getExecuteDate returning "+theDate);
      }
      
      return theDate;

   }

   @Override
   /**
    * for GeneralDateOne, if the date isn't set we shouldn't set the flag
    * @see com.prudential.wf.actions.AbstractPopulatePreviousStageNecessary#shouldSet(com.ibm.workplace.wcm.api.Document)
    */
   boolean shouldSet(Document p_arg0) {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      boolean shouldSet = false;
      if (isDebug) {
         s_log.entering("GenDateOnePopulatePreviousStage", "shouldSet");
      }
      
      if(p_arg0 instanceof Content) {
         Content theContent = (Content)p_arg0;
         try {
            Date genDateOne = theContent.getGeneralDateOne();
            if(genDateOne != null) {
               if (isDebug) {
                  s_log.log(Level.FINEST, "General Date One not null, setting true");
                  s_log.log(Level.FINEST, "General Date One: "+genDateOne);                  
               }
              shouldSet = true;
            }
         }
         catch (PropertyRetrievalException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST))
            {
               s_log.log(Level.FINEST, "", e);
            }
         }
      }
      
      if (isDebug) {
         s_log.exiting("GenDateOnePopulatePreviousStage", "shouldSet returning "+shouldSet);
      }
      
      return shouldSet;

   }

   @Override
   String getDelayComponentName() {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      return null;
      
   }
}

