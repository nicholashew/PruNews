package com.prudential.wf.actions;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.aptrix.pluto.event.AddedEvent;
import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.ContentComponent;
import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.DocumentId;
import com.ibm.workplace.wcm.api.DocumentIdIterator;
import com.ibm.workplace.wcm.api.DocumentLibrary;
import com.ibm.workplace.wcm.api.Hierarchical;
import com.ibm.workplace.wcm.api.SiteArea;
import com.ibm.workplace.wcm.api.UserSelectionComponent;
import com.ibm.workplace.wcm.api.WebContentCustomWorkflowService;
import com.ibm.workplace.wcm.api.WorkflowedDocument;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowActionResult;
import com.ibm.workplace.wcm.api.custom.Directive;
import com.ibm.workplace.wcm.api.custom.Directives;
import com.ibm.workplace.wcm.api.security.Access;
import com.prudential.utils.Utils;
import com.prudential.wcm.WCMUtils;
import com.prudential.wcm.wf.BaseCustomWorkflowAction;

/**
 * An action to retrieve approvers from parent site areas as well as the content itself
 */
public class SetApproversReviewers extends BaseCustomWorkflowAction {
   private static Logger s_log = Logger.getLogger(SetApproversReviewers.class.getName());

   private String componentNameApprovers = "PolicyApprovers";

   private String componentNameSiteAreaApprovers = "DefaultPolicyApprover";

   private String componentNameReviewers = "PolicyReviewers";

   private String componentNameSiteAreaReviewers = "DefaultPolicyReviewer";

   public SetApproversReviewers(WebContentCustomWorkflowService customWorkflowService) {
      super(customWorkflowService);
   }

   @Override
   public CustomWorkflowActionResult execute(Document doc) {

      boolean isDebug = s_log.isLoggable(Level.FINEST);

      if (isDebug) {
         s_log.entering("SetApproversReviewers", "execute");
      }

      Directive directive = Directives.CONTINUE;
      String actionMessage = this.getClass().getName() + " set PolicyApprovers/PolicyReviewers";
      DocumentLibrary currentdoclib = null;
      Workspace ws = Utils.getSystemWorkspace();
      try {
         currentdoclib = ws.getCurrentDocumentLibrary();
         // if the content value for approvers is empty, set it
         extractValue(doc, componentNameApprovers, componentNameSiteAreaApprovers);
         extractValue(doc, componentNameReviewers, componentNameSiteAreaReviewers);
         // if the content value for reviewers is empty, set it
      }
      catch (Exception e) {
         // TODO: error handling
         if (isDebug) {
            s_log.log(Level.FINEST, "Exception " + e.getMessage());
            e.printStackTrace();
         }

         return createResult(Directives.ROLLBACK_DOCUMENT, "Rollback_Document");
      }
      finally {
         if (currentdoclib != null) {
            ws.setCurrentDocumentLibrary(currentdoclib);
         }
      }

      if (isDebug) {
         s_log.exiting("SetApproversReviewers", "execute");
      }

      // All went well, return normally
      return createResult(directive, actionMessage);
   }

   /**
    * 
    * extractValue helper method to retrieve the approvers/reviewers from the userselectioncmpnt on the parent
    * @param doc the content to retrieve from
    * @param p_componentName the name of the component on the content
    * @param p_componentNameSiteArea the name of the component on the sitearea
    */
   protected void extractValue(Document doc, String p_componentName, String p_componentNameSiteArea) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (doc instanceof Content) {
         Content theResult = (Content) doc;
         ContentComponent cmpnt = WCMUtils.getContentComponent(theResult, p_componentName);
         if (cmpnt instanceof UserSelectionComponent) {
            if (isDebug) {
               s_log.log(Level.FINEST, "a user selection cmpnt was found");
            }
            UserSelectionComponent theCmpnt = (UserSelectionComponent) cmpnt;
            Principal[] users = theCmpnt.getSelections();
            // if its empty
            if (users == null || users.length < 1) {
               // get the parents
               Workspace ws = Utils.getSystemWorkspace();
               DocumentId parId = ((Hierarchical) theResult).getParentId();
               boolean foundItem = false;
               if (isDebug) {
                  s_log.log(Level.FINEST, "Component " + p_componentName + " present on the content, but empty");
               }
               try {
                  while (parId != null && !foundItem) {
                     SiteArea parent;
                     if (isDebug) {
                        s_log.log(Level.FINEST, "checking " + parId.getName());
                     }
                     parent = (SiteArea) ws.getById(parId);
                     ContentComponent siteAreaCmpnt = WCMUtils.getContentComponent(parent, p_componentNameSiteArea);
                     if (siteAreaCmpnt instanceof UserSelectionComponent) {
                        if (isDebug) {
                           s_log.log(Level.FINEST, "a user selection cmpnt was found");
                        }
                        UserSelectionComponent siteAreaUserCmpnt = (UserSelectionComponent) siteAreaCmpnt;

                        // now get the values
                        Principal[] saUsers = siteAreaUserCmpnt.getSelections();
                        if (saUsers != null && saUsers.length > 0) {
                           foundItem = true;
                           theCmpnt.setSelections(saUsers);
                           theResult.setComponent(p_componentName, theCmpnt);
                           if (isDebug) {
                              for (int x = 0; x < saUsers.length; x++) {
                                 s_log.log(Level.FINEST, "saUsers contains " + saUsers[x]);
                              }

                           }
                        }
                     }

                     parId = ((Hierarchical) parent).getParentId();

                  }
               }
               catch (Exception e) {
                  if (isDebug) {
                     s_log.log(Level.FINEST, "Exception " + e.getMessage());
                     e.printStackTrace();
                  }
               }
            }
         }
      }
   }
}
