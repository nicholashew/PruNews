/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.wf.actions;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;

import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.DateComponent;
import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.ShortTextComponent;
import com.ibm.workplace.wcm.api.WebContentCustomWorkflowService;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowAction;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowActionResult;
import com.ibm.workplace.wcm.api.custom.Directive;
import com.ibm.workplace.wcm.api.custom.Directives;
import com.ibm.workplace.wcm.api.exceptions.ComponentNotFoundException;
import com.prudential.wcm.WCMUtils;
import com.prudential.wcm.wf.*;
public class PopulateLastRevisedDate extends BaseCustomWorkflowAction {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(PopulateLastRevisedDate.class.getName());
   // the name of the field holding the number of days
   private static String s_dayField = "LastRevisedDate";

   
   public PopulateLastRevisedDate(WebContentCustomWorkflowService customWorkflowService) {
      super(customWorkflowService);
   }
   
   @Override
   public CustomWorkflowActionResult execute(Document theDoc) {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      Directive directive = Directives.CONTINUE;
      String actionMessage = this.getClass().getName() + " applying Last Revised Date";
      // get the number of days, add to the publish date, and then just set the gendateone field if its not empty
      if(theDoc instanceof Content) {
         // get the pub date, add the # of days 
         Content theContent =(Content)theDoc;
         // only do it if not moving backwards
         if(theContent.hasComponent(s_dayField) && !theContent.isWorkflowMovingBackward()) {            
            try {               
               DateComponent theDate = (DateComponent)theContent.getComponent(s_dayField);
               try {
                  Date now = new Date();
                  theDate.setDate(now);
                  theContent.setComponent(s_dayField, theDate);
                  if (isDebug) {
                     s_log.log(Level.FINEST, "Last Revised Date set to "+now);
                  }
               }
               catch (Exception e) {
                  if (isDebug) {
                     s_log.log(Level.FINEST, "exception setting the date field");
                     e.printStackTrace();
                  }               
               }
            }
            catch (Exception e1) {
               // TODO Auto-generated catch block
               if (isDebug) {
                  s_log.log(Level.FINEST, "exception setting the date field");
                  e1.printStackTrace();
               }  
            }
            
         }
      } else {
         if (isDebug) {
            s_log.log(Level.FINEST, "Document wasn't content, do not do anything");
         }
      }
      
      CustomWorkflowActionResult result = createResult(directive, actionMessage);
      return result;
      
   }

   @Override
   public Date getExecuteDate(Document p_arg0) {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      return new Date();

   }   
}

