/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.wf.actions;

import com.prudential.objects.JSON;
import javax.naming.InitialContext;
import com.ibm.workplace.wcm.api.*;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowAction;
import static com.ibm.workplace.wcm.api.custom.CustomWorkflowAction.DATE_EXECUTE_NOW;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowActionResult;
import com.ibm.workplace.wcm.api.custom.Directive;
import com.ibm.workplace.wcm.api.custom.Directives;
import com.ibm.workplace.wcm.api.custom.RollbackDirectiveParams;
import com.ibm.workplace.wcm.api.exceptions.*;
import com.ibm.workplace.wcm.api.security.Access;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import org.json.JSONObject;

/**
 *
 * @author Pete Raleigh
 */
public class CreateMyActionsMessage implements CustomWorkflowAction {
   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(CreateMyActionsMessage.class.getName());

   private static Workspace wksp;

   private String message = "";

   // This specifies when the custom action will be executed
   @Override
   public Date getExecuteDate(Document document) {
      return DATE_EXECUTE_NOW;
   }

   // This method contains the code that will run when the custom action is executed.
   @Override
   public CustomWorkflowActionResult execute(Document doc) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);

      Directive directive = Directives.CONTINUE;
      RollbackDirectiveParams params = (RollbackDirectiveParams) Directives.ROLLBACK_DOCUMENT.createDirectiveParams();

      if (doc instanceof Content) {
         Content cont = (Content) doc;
         if (isDebug) {
            s_log.log(Level.FINEST, "Content: " + cont.getTitle() + " (" + cont.getName() + ")");
         }

         try {
            if (wksp == null) {
               wksp = WCM_API.getRepository().getSystemWorkspace();
               wksp.useDistinguishedNames(false);
            }
            DocumentId wfsid = cont.getWorkflowStageId();
            // Get the approvers...
            WorkflowStage wfs = (WorkflowStage) wksp.getById(wfsid);
            String[] approvers = wfs.getMembersForWorkflowDefinedAccess(Access.APPROVER);

            // Create a message and send to MyActions for each Approver
            JSON j = new JSON();
            JSONObject jsonMsg = j.createMyAction(cont, approvers);

            // Send the message - get a response
            String groupTransId = j.createMyActionRequest("messages", jsonMsg);

            if (groupTransId != null) {
               // Write the response to the History Object
               String history = "MyActions:" + groupTransId;
               cont.addHistoryLogEntry(history);
            }
            else {
               if (isDebug) {
                  s_log.log(Level.FINEST, "groupTransId was null or an error occurred");
               }

            }
         }
         catch (Exception ex) {
            if (isDebug) {
               s_log.log(Level.FINEST, "Exception occured " + ex.getMessage());
               ex.printStackTrace();
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
         message = "OK - MyActions createMessage generated";
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
