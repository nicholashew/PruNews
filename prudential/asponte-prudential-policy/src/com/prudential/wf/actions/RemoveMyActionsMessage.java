/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.wf.actions;

import com.prudential.objects.JSON;
import javax.naming.InitialContext;
import com.ibm.workplace.wcm.api.*;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowAction;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowActionResult;
import com.ibm.workplace.wcm.api.custom.Directive;
import com.ibm.workplace.wcm.api.custom.Directives;
import com.ibm.workplace.wcm.api.custom.RollbackDirectiveParams;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;

/**
 *
 * @author Pete Raleigh
 */
public class RemoveMyActionsMessage implements CustomWorkflowAction {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(RemoveMyActionsMessage.class.getName());

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

      if (isDebug) {
         s_log.entering("RemoveMyActionsMessage", "execute");
      }

      Directive directive = Directives.CONTINUE;
      RollbackDirectiveParams params = (RollbackDirectiveParams) Directives.ROLLBACK_DOCUMENT.createDirectiveParams();

      if (doc instanceof Content) {
         Content cont = (Content) doc;
         if (isDebug) {
            s_log.log(Level.FINEST, "Content: " + cont.getTitle() + " (" + cont.getName() + ")");
         }
         HistoryLogIterator hli = cont.getHistoryLog();
         boolean found = false;
         while (hli.hasNext() && !found) {
            HistoryLogEntry entry = (HistoryLogEntry) hli.next();
            String msg = entry.getMessage();
            if (msg.startsWith("MyActions:")) {
               // Found a MyActions response
               String groupTransId = msg.substring("MyActions:".length(), msg.length());

               // Delete a message associated with the Content
               JSON j = new JSON();
               int response = j.deleteMyActionRequest("messages", groupTransId);
               if (response != 200) {
                  if (isDebug) {
                     s_log.log(Level.FINEST, "An error occurred removing the message. It may no longer exist.");
                  }
               }
               found = true;
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
         message = "OK - MyActions message removed";
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
