package com.prudential.wf.actions;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import com.aptrix.pluto.event.AddedEvent;
import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.ContentComponent;
import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.DocumentId;
import com.ibm.workplace.wcm.api.DocumentIdIterator;
import com.ibm.workplace.wcm.api.DocumentLibrary;
import com.ibm.workplace.wcm.api.UserSelectionComponent;
import com.ibm.workplace.wcm.api.WebContentCustomWorkflowService;
import com.ibm.workplace.wcm.api.WorkflowedDocument;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowActionResult;
import com.ibm.workplace.wcm.api.custom.Directive;
import com.ibm.workplace.wcm.api.custom.Directives;
import com.prudential.utils.Utils;
import com.prudential.wcm.WCMUtils;
import com.prudential.wcm.wf.BaseCustomWorkflowAction;

/**
 * An action to retrieve approvers from parent site areas as well as the content itself
 */
public class ApplyApproversAction extends BaseCustomWorkflowAction {
   private static Logger s_log = Logger.getLogger(ApplyApproversAction.class.getName());

   private String componentName = "PolicyApprovers";
   private String componentNameSiteArea = "DefaultPolicyApprover";

   public ApplyApproversAction(WebContentCustomWorkflowService customWorkflowService) {
      super(customWorkflowService);
   }
   @Override
   public CustomWorkflowActionResult execute(Document doc) {

      boolean isDebug = s_log.isLoggable(Level.FINEST);
      
      if (isDebug) {
         s_log.entering("ApplyApproversAction", "execute");
      }
      
      Directive directive = Directives.CONTINUE;
      String actionMessage = this.getClass().getName() + " action directive is Continue - applying " + componentName + " went OK.";
      DocumentLibrary currentdoclib = null;
      Workspace ws = doc.getSourceWorkspace();
      try {
         currentdoclib = ws.getCurrentDocumentLibrary();

         Set<String> approvers = new HashSet<String>();

         // extract approvers from site area
         // no need any longer because of inline editing
         /*
         if (doc instanceof Content) {
            DocumentIdIterator itr = ((Content) doc).getParents();
            if (itr.hasNext()) {
               DocumentId id = itr.next();
               extractApprovers(ws.getById(id), approvers,componentNameSiteArea);
            }
         }
         */

         // extract approvers from content
         extractApprovers(doc, approvers);

         // apply approvers to WorkflowedDocument
         if (!approvers.isEmpty()) {
            WorkflowedDocument wfd = (WorkflowedDocument) doc;
            boolean isWorkspaceDN = ws.isDistinguishedNamesUsed();
            ws.useDistinguishedNames(true);
            if (isDebug) {
               //s_log.log(Level.FINEST, "removing approvers "+wfd.getCurrentApprovers());
               s_log.log(Level.FINEST, "adding approvers "+approvers);
            }
            // cmk don't remove
            //wfd.removeApprovers(wfd.getCurrentApprovers());
            wfd.addApprovers(approvers.toArray(new String[approvers.size()]));
            if (isDebug) {
               s_log.log(Level.FINEST, "after adding approvers "+wfd.getCurrentApprovers());               
            }
            ws.useDistinguishedNames(isWorkspaceDN);
         }

      }
      catch (Exception e) {
         // TODO: error handling
         return createResult(Directives.ROLLBACK_DOCUMENT, "Rollback_Document");
      }
      finally {
         if (currentdoclib != null) {
            ws.setCurrentDocumentLibrary(currentdoclib);
         }
      }
      
      if (isDebug) {
         s_log.exiting("ApplyApproversAction", "execute");
      }
      

      // All went well, return normally
      return createResult(directive, actionMessage);
   }

   /**
    * 
    * extractApprovers helper method to retrieve the approvers from the userselectioncmpnt
    * @param doc the content to retrieve from
    * @param approvers the set to add the approvers to
    */
   protected void extractApprovers(Document doc, Set<String> approvers) {
      extractApprovers(doc, approvers, componentName);
   }
   
   /**
    * 
    * extractApprovers helper method to retrieve the approvers from the userselectioncmpnt
    * @param doc the content to retrieve from
    * @param approvers the set to add the approvers to
    */
   protected void extractApprovers(Document doc, Set<String> approvers, String p_componentName) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      ContentComponent cmpnt = WCMUtils.getContentComponent(doc, p_componentName);
      if (cmpnt instanceof UserSelectionComponent) {
         for (Principal p : ((UserSelectionComponent) cmpnt).getSelections()) {
            if (isDebug) {
               s_log.log(Level.FINEST, "adding principal "+p.getName());
            }
            com.ibm.portal.um.Principal thePrincipal = null;
            thePrincipal = Utils.getPrincipalById(p.getName());
            approvers.add(Utils.getDnForPrincipal(thePrincipal));
         }
      }
   }

}
