package com.prudential.wf.actions;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.security.Principal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.ContentComponent;
import com.ibm.workplace.wcm.api.DateComponent;
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
public class ApplyReviewersAction extends BaseCustomWorkflowAction {
   private static Logger s_log = Logger.getLogger(ApplyReviewersAction.class.getName());

   private String componentName = "PolicyReviewers";
   private String componentNameSiteArea = "DefaultPolicyReviewer";
   private String componentNameReviewDate = "DateSubmittedForReview";
   
   public ApplyReviewersAction(WebContentCustomWorkflowService customWorkflowService) {
      super(customWorkflowService);
   }

   @Override
   public CustomWorkflowActionResult execute(Document doc) {

      boolean isDebug = s_log.isLoggable(Level.FINEST);
      
      if (isDebug) {
         s_log.entering("ApplyReviewersAction", "execute");
      }
      
      Directive directive = Directives.CONTINUE;
      String actionMessage = this.getClass().getName() + " action directive is Continue - applying " + componentName + " went OK.";
      DocumentLibrary currentdoclib = null;
      Workspace ws = doc.getSourceWorkspace();
      try {
         currentdoclib = ws.getCurrentDocumentLibrary();

         Set<String> reviewers = new HashSet<String>();

         // extract approvers from site area
         // no need any longer because of inline editing
         /*
         if (doc instanceof Content) {
            DocumentIdIterator itr = ((Content) doc).getParents();
            if (itr.hasNext()) {
               DocumentId id = itr.next();
               extractReviewers(ws.getById(id), reviewers,componentNameSiteArea);
            }
         }
         */

         // extract approvers from content
         extractReviewers(doc, reviewers);

         // apply approvers to WorkflowedDocument
         if (!reviewers.isEmpty()) {
            WorkflowedDocument wfd = (WorkflowedDocument) doc;
            boolean isWorkspaceDN = ws.isDistinguishedNamesUsed();
            ws.useDistinguishedNames(true);
            if (isDebug) {
               //s_log.log(Level.FINEST, "removing approvers "+wfd.getCurrentApprovers());
               s_log.log(Level.FINEST, "adding approvers "+reviewers);
            }
            // don't remove current approvers
            //wfd.removeApprovers(wfd.getCurrentApprovers());            
            wfd.addApprovers(reviewers.toArray(new String[reviewers.size()]));
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
         s_log.exiting("ApplyReviewersAction", "execute");
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
   protected void extractReviewers(Document doc, Set<String> approvers) {
      this.extractReviewers(doc, approvers, componentName);
      
   }
   
   /**
    * 
    * extractApprovers helper method to retrieve the approvers from the userselectioncmpnt
    * @param doc the content to retrieve from
    * @param approvers the set to add the approvers to
    */
   protected void extractReviewers(Document doc, Set<String> approvers, String p_componentName) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("ApplyReviewersAction", "extractReviewers "+doc.getName());
      }
      
      ContentComponent cmpnt = WCMUtils.getContentComponent(doc, p_componentName);
      if (cmpnt instanceof UserSelectionComponent) {
         if (isDebug) {
            s_log.log(Level.FINEST, "a user selection cmpnt was found");
         }
         UserSelectionComponent theCmpnt = (UserSelectionComponent) cmpnt;
         Principal[] users = theCmpnt.getSelections();
         if(users != null) {
            for (int x = 0;x<users.length;x++) {
               Principal p = users[x];
               if (isDebug) {
                  s_log.log(Level.FINEST, "adding principal "+p.getName());
               }
               com.ibm.portal.um.Principal thePrincipal = null;
               thePrincipal = Utils.getPrincipalById(p.getName());
               approvers.add(Utils.getDnForPrincipal(thePrincipal));
            }
         }         
         
      }
      
      if (isDebug) {
         s_log.exiting("ApplyReviewersAction", "extractReviewers");
      }
      
   }

}
