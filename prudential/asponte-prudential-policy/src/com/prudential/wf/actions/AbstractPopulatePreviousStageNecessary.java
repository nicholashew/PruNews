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
import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.DocumentId;
import com.ibm.workplace.wcm.api.DocumentIdIterator;
import com.ibm.workplace.wcm.api.DocumentTypes;
import com.ibm.workplace.wcm.api.LibraryDateComponent;
import com.ibm.workplace.wcm.api.ShortTextComponent;
import com.ibm.workplace.wcm.api.WebContentCustomWorkflowService;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowAction;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowActionResult;
import com.ibm.workplace.wcm.api.custom.Directive;
import com.ibm.workplace.wcm.api.custom.Directives;
import com.ibm.workplace.wcm.api.exceptions.ComponentNotFoundException;
import com.ibm.workplace.wcm.api.exceptions.DocumentCreationException;
import com.ibm.workplace.wcm.api.exceptions.DuplicateComponentException;
import com.ibm.workplace.wcm.api.exceptions.IllegalDocumentTypeException;
import com.ibm.workplace.wcm.api.exceptions.IllegalTypeChangeException;
import com.ibm.workplace.wcm.api.exceptions.PropertyRetrievalException;
import com.prudential.utils.Utils;
import com.prudential.wcm.WCMUtils;
import com.prudential.wcm.wf.*;

public abstract class AbstractPopulatePreviousStageNecessary extends BaseCustomWorkflowAction {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(AbstractPopulatePreviousStageNecessary.class.getName());

   public AbstractPopulatePreviousStageNecessary(WebContentCustomWorkflowService customWorkflowService) {
      super(customWorkflowService);
   }

   @Override
   public CustomWorkflowActionResult execute(Document theDoc) {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      Directive directive = Directives.CONTINUE;
      String actionMessage = this.getClass().getName() + " applying PreviousStageIfNecessary";
      if (theDoc instanceof Content && shouldSet(theDoc)) {
         // get the pub date, add the # of days 
         Content theContent = (Content) theDoc;
         Workspace ws = Utils.getSystemWorkspace();

         try {
            ShortTextComponent stc = theContent.createComponent(Utils.p_prevStageCmpnt, DocumentTypes.ShortTextComponent);
            stc.setText("Needs Previous Stage");
            theContent.setComponent(Utils.p_prevStageCmpnt, stc);
            directive = Directives.NEXT_WORKFLOW_STAGE;
            actionMessage = this.getClass().getName() + " Applying Previous Stage and Moving Document";
         }
         catch (DuplicateComponentException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST)) {
               s_log.log(Level.FINEST, "", e);
            }
         }
         catch (IllegalDocumentTypeException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST)) {
               s_log.log(Level.FINEST, "", e);
            }
         }
         catch (DocumentCreationException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST)) {
               s_log.log(Level.FINEST, "", e);
            }
         }
         //theContent.addComponent(arg0, arg1)
         catch (ComponentNotFoundException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST)) {
               s_log.log(Level.FINEST, "", e);
            }
         }
         catch (IllegalTypeChangeException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST)) {
               s_log.log(Level.FINEST, "", e);
            }
         }

      }
      else {
         if (isDebug) {
            s_log.log(Level.FINEST, "Document wasn't content, do not do anything");
         }
      }

      CustomWorkflowActionResult result = createResult(directive, actionMessage);
      return result;

   }


   /**
    * getDelayedDate return an offset delay
    */
   public Date getDelayedDate(Document theDoc, Date initialDate) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering(this.getClass().getName(), "getDelayedDate " + theDoc.getName() + ", date " + initialDate);
      }
      Date theDate = initialDate;

      Workspace ws = Utils.getSystemWorkspace();
      try {
         ws.login();
         Content theContent = (Content) theDoc;
         // get the review delay
         int days = 3;
         try {
            String delayCmpntName = getDelayComponentName();
            if (theContent.hasComponent(delayCmpntName)) {
               ShortTextComponent stc = (ShortTextComponent) theContent.getComponentByReference(delayCmpntName);
               days = Integer.parseInt(stc.getText());
            }

         }
         catch (Exception e) {
            if (isDebug) {
               s_log.log(Level.FINEST, "Exception " + e.getMessage());
               e.printStackTrace();
            }
         }

         Date updatedDate = new Date();
         theDate = Utils.addDays(updatedDate, days);
      }
      finally {
         if (ws != null) {
            ws.logout();
         }
      }

      if (isDebug) {
         s_log.exiting("BaseEmailAction", "updateDate " + theDoc.getName());
      }

      return theDate;

   }
   
   @Override
   public Date getExecuteDate(Document p_arg0) {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      Date theDate = new Date();
      if (isDebug) {
         s_log.entering("ApproveDelayPopulatePreviousStage", "getExecuteDate");
      }

      if (p_arg0 instanceof Content) {
         Content theContent = (Content) p_arg0;
         try {
            Date dateToExpand = theContent.getDateEnteredStage();
            dateToExpand = getDelayedDate(theContent, dateToExpand);
            theDate = dateToExpand;
         }
         catch (PropertyRetrievalException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST)) {
               s_log.log(Level.FINEST, "", e);
            }
         }
      }

      if (isDebug) {
         s_log.exiting("ApproveDelayPopulatePreviousStage", "getExecuteDate");
      }

      return theDate;

   }

   boolean shouldSet(Document p_arg0) {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      boolean shouldSet = false;
      if (isDebug) {
         s_log.entering("ApproveDelayPopulatePreviousStage", "shouldSet");
      }

      Date now = new Date();
      Date executeDate = getExecuteDate(p_arg0);

      if (executeDate.after(now)) {
         shouldSet = true;
      }

      if (isDebug) {
         s_log.exiting("ApproveDelayPopulatePreviousStage", "getDelayComponentName");
      }

      return shouldSet;

   }


   abstract String getDelayComponentName();
}
