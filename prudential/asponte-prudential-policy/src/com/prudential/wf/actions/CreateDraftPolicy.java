/**
 * company: asponte
 * author: pete raleigh
 * description: creates a draft policy document and moves to review
 */

package com.prudential.wf.actions;

import javax.naming.InitialContext;
import com.ibm.workplace.wcm.api.*;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowAction;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowActionResult;
import com.ibm.workplace.wcm.api.custom.Directive;
import com.ibm.workplace.wcm.api.custom.Directives;
import com.ibm.workplace.wcm.api.custom.RollbackDirectiveParams;
import com.ibm.workplace.wcm.api.exceptions.*;

import java.util.*;
import java.util.logging.*;
import javax.naming.NamingException;

/**
 *
 * @version 1.0
 */
public class CreateDraftPolicy implements CustomWorkflowAction {

   private static final Logger s_log = Logger.getLogger(CreateDraftPolicy.class.getName());

   // This specifies when the custom action will be executed
   @Override
   public Date getExecuteDate(Document doc) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      
      Content theContent = (Content)doc;
      Date returnDate = DATE_EXECUTE_NOW;
      try {
         returnDate = theContent.getGeneralDateOne();
      }
      catch (PropertyRetrievalException e) {
         // TODO Auto-generated catch block
         if (s_log.isLoggable(Level.FINEST))
         {
            s_log.log(Level.FINEST, "", e);
         }
      }
      if (isDebug) {
         s_log.exiting("CreateDraftPolicy", "getExecuteDate" +returnDate);
      }
      
      return returnDate;
   }

   // This method contains the code that will run when the custom action is executed.
   @Override
   public CustomWorkflowActionResult execute(Document doc) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("CreateDraftPolicy", "execute "+doc.getName());
      }
      
      // don't create if the date hasn't been reached
      boolean shouldCreate = true;
      Directive directive = Directives.CONTINUE;
      RollbackDirectiveParams params = (RollbackDirectiveParams) Directives.ROLLBACK_DOCUMENT.createDirectiveParams();
      Date now = new Date();
      Date execute = this.getExecuteDate(doc);
      // if execute is null, means blank general date one
      if(execute == null || execute.after(now)) {
         shouldCreate = false;
      }
      if (doc instanceof Content && shouldCreate) {
         Content cont = (Content) doc;
         if (isDebug)
            s_log.log(Level.FINEST, "Content: " + cont.getTitle() + " (" + cont.getName() + ")");

         try {
            Content draft = (Content) cont.createDraftDocument();
            draft.setEffectiveDate(new Date());
            // check for gendateone
            Date genOne = cont.getGeneralDateOne();
            if (genOne != null) {
               draft.setGeneralDateTwo(genOne);
            }
            // now clear the genDateOne 
            draft.setGeneralDateOne(null);
            // Run both the Exit Actions and the Entry Actions of the next stage
            if (isDebug) {
               s_log.log(Level.FINEST, "draft documents stage is "+draft.getWorkflowStageId());
            }
            draft.nextWorkflowStage(true, true, "Moved automatically - via " + CreateDraftPolicy.class.getName());
            if (isDebug) {
               s_log.log(Level.FINEST, "after next stage, draft documents stage is "+draft.getWorkflowStageId());
            }
            // if the draft was created, roll back to Published stage so that the content is not in pending retire
            
         }
         catch (AuthorizationException e) {
            // Exception thrown...
            if (isDebug)
               s_log.log(Level.FINEST, "Exception: " + cont.getTitle() + " (" + cont.getName() + ")");
         }
         catch (WorkflowException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST)) {
               s_log.log(Level.FINEST, "", e);
            }
         }
         catch (PropertyRetrievalException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST)) {
               s_log.log(Level.FINEST, "", e);
            }
         }
      }
      WebContentCustomWorkflowService webContentCustomWorkflowService;
      try {
         // Construct and inital Context
         InitialContext ctx = new InitialContext();
         // Retrieve WebContentCustomWorkflowService using JNDI name
         webContentCustomWorkflowService = (WebContentCustomWorkflowService) ctx
            .lookup("portal:service/wcm/WebContentCustomWorkflowService");
      }
      catch (NamingException ex) {
         if (isDebug) {
            s_log.log(Level.FINEST, " " + ex.getMessage());
            ex.printStackTrace();
         }
         return null;
      }
      String message;
      if (directive == Directives.CONTINUE) {
         message = "Draft Policy successfully created.";
         if (isDebug) {
            s_log.log(Level.FINEST, " message = "+message);
         }
         if(!shouldCreate) {
            message = "Draft Policy not created because review date not reached.";
         }
         CustomWorkflowActionResult result = webContentCustomWorkflowService.createResult(directive, message);
         return result;
      }
      message = "Something went wrong. A draft content item may not have been successfully created. Check the logs";      
      params.setCustomErrorMsg(message);
      if (isDebug) {
         s_log.log(Level.FINEST, "Issue occured.");
         s_log.exiting("CreateDraftPolicy", "execute");
      }
      CustomWorkflowActionResult result = webContentCustomWorkflowService.createResult(directive, message, params);
      return result;
   }
}
