/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.wf.actions;

import com.ibm.workplace.wcm.api.Content;
import java.util.logging.Logger;

import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.DocumentId;
import com.ibm.workplace.wcm.api.Hierarchical;
import com.ibm.workplace.wcm.api.WCM_API;
import com.ibm.workplace.wcm.api.WebContentCustomWorkflowService;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowAction;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowActionResult;
import com.ibm.workplace.wcm.api.custom.Directive;
import com.ibm.workplace.wcm.api.custom.Directives;
import com.ibm.workplace.wcm.api.custom.RollbackDirectiveParams;
import com.ibm.workplace.wcm.api.exceptions.AuthorizationException;
import com.ibm.workplace.wcm.api.exceptions.DocumentRetrievalException;
import com.ibm.workplace.wcm.api.exceptions.OperationFailedException;
import com.ibm.workplace.wcm.api.exceptions.ServiceNotAvailableException;
import com.ibm.workplace.wcm.api.security.Access;
import com.prudential.tasks.ApplyManagersAsOwnersTask;
import com.prudential.tasks.PreviousStageTask;
import com.prudential.utils.Utils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.logging.Level;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author Pete Raleigh
 */
public class ApplySiteAreaManagers implements CustomWorkflowAction {

   private static final Logger s_log = Logger.getLogger(ApplySiteAreaManagers.class.getName());

   private static Workspace wksp;

   private String message = "";

   // This specifies when the custom action will be executed
   @Override
   public Date getExecuteDate(Document document) {
      // execute 3 seconds	   
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.SECOND, 3);
      Date theDate = calendar.getTime();
      //return theDate;
      return new Date();
   }

   // This method contains the code that will run when the custom action is executed.
   @Override
   public CustomWorkflowActionResult execute(Document doc) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      Directive directive = Directives.CONTINUE;
      RollbackDirectiveParams params = (RollbackDirectiveParams) Directives.ROLLBACK_DOCUMENT.createDirectiveParams();

      Workspace ws = Utils.getSystemWorkspace();
      if (doc instanceof Content) {
         Content theResult = (Content) doc;
         if (isDebug) {
            s_log.log(Level.FINEST, "Content: {0} ({1})", new Object[] {theResult.getTitle(), theResult.getName()});
         }

         /**
         ApplyManagersAsOwnersTask thisTask = new ApplyManagersAsOwnersTask();
            thisTask.setUuid(cont.getId().getId());               
            Timer timer = new Timer("PREVSTAGETIME");
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, 3);
            Date theDate = calendar.getTime();
            // run immediately
            timer.schedule(thisTask, theDate);
            **
            */

         String[] managersContent = theResult.getMembersForInheritedAccess(Access.MANAGER);
         if (managersContent != null && managersContent.length > 0) {
            if (isDebug) {
               s_log.log(Level.FINEST, "found content managers, using to populate the authors");
               for (int y = 0; y < managersContent.length; y++) {
                  s_log.log(Level.FINEST, "managers included " + managersContent[y]);
               }

            }
            theResult.addOwners(managersContent);
            if (isDebug) {
               s_log.log(Level.FINEST, "Owners field set to: {0}", Arrays.toString(managersContent));
            }

         }
         else {
            DocumentId parId = ((Hierarchical) theResult).getParentId();
            boolean foundManagers = false;
            if (isDebug) {
               s_log.log(Level.FINEST, "Content security not inherited, check parent "+parId);
            }
            try {
               while (parId != null && !foundManagers) {
                  Document parent;
                  if (isDebug) {
                     s_log.log(Level.FINEST, "checking "+parId);
                  }
                  parent = ws.getById(parId);
                  String[] managers = parent.getMembersForAccess(Access.MANAGER);
                  if(managers == null || managers.length < 1) {
                     managers = parent.getMembersForInheritedAccess(Access.MANAGER);
                  }
                  if (managers != null) {
                     for (int y = 0; y < managers.length; y++) {
                        s_log.log(Level.FINEST, "managers included " + managers[y]);
                     }
                     foundManagers = true;
                     theResult.addOwners(managers);
                     if (isDebug) {
                        s_log.log(Level.FINEST, "Owners field set to: {0}", Arrays.toString(managers));
                     }

                  }
                  else {
                     parId = ((Hierarchical) parent).getParentId();
                  }
               }
            }
            catch (DocumentRetrievalException e) {
               // TODO Auto-generated catch block
               if (s_log.isLoggable(Level.FINEST)) {
                  s_log.log(Level.FINEST, "", e);
               }
            }
            catch (AuthorizationException e) {
               // TODO Auto-generated catch block
               if (s_log.isLoggable(Level.FINEST)) {
                  s_log.log(Level.FINEST, "", e);
               }
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
         return null;
      }
      if (directive == Directives.CONTINUE) {
         message = "OK - Owners fields set";
         if (isDebug) {
            s_log.log(Level.FINEST, message);
         }
         return webContentCustomWorkflowService.createResult(directive, message);
      }
      message = "ERROR: An error has occurred - contact your System Administrator";
      if (isDebug) {
         s_log.log(Level.FINEST, message);
      }
      params.setCustomErrorMsg(message);
      return webContentCustomWorkflowService.createResult(directive, "Rolling back document.", params);
   }
}