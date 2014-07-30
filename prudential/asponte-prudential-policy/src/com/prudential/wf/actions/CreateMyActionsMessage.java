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
import com.ibm.workplace.wcm.api.security.Access; 
import com.prudential.objects.MyActionsResponse; 
import com.prudential.utils.Utils;
import com.prudential.wcm.WCMUtils;

import java.security.Principal;
import java.util.*; 
import java.lang.Math; 
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
   
   private String reviewerComponent = "PolicyReviewers";
   private String approverComponent = "PolicyApprovers";

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

            // Check to see if an existing "MyActions" Element already exists on the Content item 
            // If it does, then the "old" message may need to be removed... 
            boolean hasMyActions = false; 
            ShortTextComponent tc; 
            if (cont.hasComponent("MyActions")) { 
               hasMyActions = true; 
               tc = (ShortTextComponent)cont.getComponent("MyActions"); 
               String oldGroupTransId = tc.getText(); 
               if (!"".equals(oldGroupTransId)) { 
                  // Attempt to delete the old MyActions message 
                  JSON j = new JSON(); 
                  int responseCode = j.deleteMyActionRequest("messages", oldGroupTransId); 

                  // We don't really care if the request was successful or not... 
                  if (isDebug) { 
                     s_log.log(Level.FINEST, "Response code from DELETE (" + oldGroupTransId + "): " + responseCode); 
                  } 
               } 
            } 

            DocumentId wfsid = cont.getWorkflowStageId(); 
            // Get the approvers... 
            //WorkflowStage wfs = (WorkflowStage) wksp.getById(wfsid);
            String currentStage = wfsid.getName();
            String componentToRetrieve = approverComponent;
            // pull the users from the content
            // if we're in the review stage send to reviewers. If we're in approve stage send to the approvers.
            if(currentStage.contains("Review")) {
               componentToRetrieve = reviewerComponent;
            }
            
            Set<String> approverSet = new HashSet<String>();
            extractApprovers(doc, approverSet, componentToRetrieve);
            String[] approvers = approverSet.toArray(new String[approverSet.size()]);
            
            //String[] approvers = wfs.getMembersForWorkflowDefinedAccess(Access.APPROVER); 
            //String[] approvers = cont.getCurrentApprovers(); 

            // Create a message and send to MyActions for each Approver 
            JSON j = new JSON(); 
            JSONObject jsonMsg = j.createMyAction(cont, approvers); 
            if (isDebug) s_log.log(Level.FINEST, "JSON: " + jsonMsg.toString()); 

            // Send the message - get a response 
            MyActionsResponse myResponse = j.createMyActionRequest("messages", jsonMsg);             
            message = myResponse.getResponse(); 
            message = message.substring(0, Math.min(message.length(), 200)); 

            if (myResponse.isSuccess()) { 
               // Write the response to a new MyActions Element... 
               if (!hasMyActions) { 
                  // Create a MyActions Text Component... 
                  tc = cont.createComponent("MyActions", DocumentTypes.ShortTextComponent); 
               } else { 
                  tc = (ShortTextComponent)cont.getComponent("MyActions"); 
               } 
               tc.setText(myResponse.getGroupTransId()); 
               cont.setComponent("MyActions", tc); 
               cont.addHistoryLogEntry(myResponse.getResponse()); 
            } else { 
               System.err.print("MyActions Response: [" + myResponse.getResponseCode() + "] " + myResponse.getResponse()); 
            } 
         } 
         catch (Exception ex) { 
            if (isDebug) { 
               s_log.log(Level.FINEST, "Exception occurred " + ex.getMessage()); 
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
   
   protected void extractApprovers(Document doc, Set<String> approvers, String p_componentName) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      ContentComponent cmpnt = WCMUtils.getContentComponent(doc, p_componentName);
      if (cmpnt instanceof UserSelectionComponent) {
         for (Principal p : ((UserSelectionComponent) cmpnt).getSelections()) {
            if (isDebug) {
               s_log.log(Level.FINEST, "adding principal "+p.getName());
            }
            //com.ibm.portal.um.Principal thePrincipal = null;
            // get the xid
            //thePrincipal = Utils.getPrincipalById(p.getName());
            //approvers.add(Utils.getDnForPrincipal(thePrincipal));
            approvers.add(p.getName());
         }
      }
   }
}