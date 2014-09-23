/********************************************************************/
/* Asponte 
/* cmknight 
/********************************************************************/

package com.prudential.wf.actions;

import javax.naming.InitialContext;

import com.ibm.workplace.wcm.api.*;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowAction;
import static com.ibm.workplace.wcm.api.custom.CustomWorkflowAction.DATE_EXECUTE_NOW;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowActionResult;
import com.ibm.workplace.wcm.api.custom.Directive;
import com.ibm.workplace.wcm.api.custom.Directives;
import com.ibm.workplace.wcm.api.custom.RollbackDirectiveParams;
import com.ibm.workplace.wcm.api.exceptions.AuthorizationException;
import com.ibm.workplace.wcm.api.exceptions.OperationFailedException;
import com.prudential.utils.Utils;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;

/** 
 * 
 * @author Chris Knight
 */
public class ExpireAdoptedPolicies implements CustomWorkflowAction {
   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(ExpireAdoptedPolicies.class.getName());

   private static List<String> policyTemplateList = Arrays.asList("PP-Policy Link-AT");

   // This specifies when the custom action will be executed 
   @Override
   public Date getExecuteDate(Document document) {
      return DATE_EXECUTE_NOW;
   }

   // This method contains the code that will run when the custom action is executed. 
   @Override
   /**
    * this action will find the references to a model policy
    * if the reference is an adopted policy, retire that one as well.
    * @see com.ibm.workplace.wcm.api.custom.CustomWorkflowAction#execute(com.ibm.workplace.wcm.api.Document)
    */
   public CustomWorkflowActionResult execute(Document doc) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("ExpireAdoptedPolicies", "execute");
      }

      Directive directive = Directives.CONTINUE;
      RollbackDirectiveParams params = (RollbackDirectiveParams) Directives.ROLLBACK_DOCUMENT.createDirectiveParams();
      String message = "";
      WebContentCustomWorkflowService webContentCustomWorkflowService = null;
      try {
         Workspace ws = Utils.getSystemWorkspace();
         // Construct and inital Context 
         InitialContext ctx = new InitialContext();
         // Retrieve WebContentCustomWorkflowService using JNDI name 
         webContentCustomWorkflowService = (WebContentCustomWorkflowService) ctx
            .lookup("portal:service/wcm/WebContentCustomWorkflowService");

         // get the references to the current document
         // only if we should bother which means this is a model policy
         if (shouldExecute(doc)) {
            // get the references
            Reference[] refs = ws.getReferences(doc.getId());
            for (Reference ref : refs) {
               // Get the document referring to the current document
               DocumentId refId = ref.getRefererDocumentId();

               try {
                  Document refDoc = ws.getById(refId);
                  if (isDebug) {
                     s_log.log(Level.FINEST, "Processing " + refId);
                  }
                  // Only look for Content items
                  if (refDoc instanceof Content) {
                     Content refCont = (Content) refDoc;

                     // Only look for Content items - which are Published
                     // don't exclude drafts.
                     DocumentId refATid = refCont.getAuthoringTemplateID();
                     String refATname = refATid.getName();

                     // Check if the Referenced Content item is a Policy document
                     if (policyTemplateList.contains(refATname)) {
                        // expire the document
                        try {
                           if(ws.isLocked(refCont.getId())) {
                              if (isDebug) {
                                 s_log.log(Level.FINEST, "Content is locked, unlock");
                              }
                              ws.unlock(refCont.getId());
                           }
                           
                           // now set the expire date
                           refCont.setExpiryDate(new Date());
                           String[] errors = ws.save(refCont);
                           if (isDebug) {
                              for(int x=0;x<errors.length;x++) {
                                 s_log.log(Level.FINEST, "error saving "+errors[x]);
                              }                              
                           }
                           
                           refCont = (Content)ws.getById(refCont.getId());
                           DocumentId wfStageId = null;
                           String stageName = "";
                           while(!refCont.isExpired()) {
                              wfStageId = refCont.getWorkflowStageId();
                              stageName = wfStageId.getName().toLowerCase();
                              // if we're already in retire stage, exit
                              if(stageName.contains("retire")) {
                                 break;
                              }
                              // means it's right before retire so we want to execute actions
                              else if(stageName.contains("pending retire")) {
                                 // 
                                 refCont.nextWorkflowStage(true, true, "Model Policy is Retired, retiring adopted");
                              }
                              else {
                                 refCont.nextWorkflowStage(false, false, "Model Policy is Retired, retiring adopted");
                              }
                           }
                           
                        } catch (Exception e) {
                           if (isDebug) {
                              s_log.log(Level.FINEST, "Exception expiring document");
                              e.printStackTrace();
                           }
                        }
                     }
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
      catch (NamingException ex) {
         return null;
      }
      catch (OperationFailedException e) {
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
      if (directive == Directives.CONTINUE) {
         if (isDebug) {
            s_log.exiting("ExpireAdoptedPolicies", "execute " + message);
         }
         return webContentCustomWorkflowService.createResult(directive, message);
      }
      message = "ERROR: An error has occurred - contact your System Administrator";
      if (isDebug) {
         s_log.log(Level.FINEST, message);
      }
      params.setCustomErrorMsg(message);

      if (isDebug) {
         s_log.exiting("ExpireAdoptedPolicies", "execute " + message);
      }

      return webContentCustomWorkflowService.createResult(directive, "Rolling back document.", params);
   }

   private boolean shouldExecute(Document theDoc) {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("ExpireAdoptedPolicies", "shouldExecute " + theDoc.getName());
      }

      boolean shouldExecute = false;
      // first check the service
      try {
         if (theDoc instanceof Content) {
            Content theContent = (Content) theDoc;

            // check to ensure this is a Model policy
            DocumentId parentId = theContent.getDirectParent();
            Workspace ws = Utils.getSystemWorkspace();
            // the /Model Policy/Content site area is the parent for all model policies.
            String modelPolicyContentUUID = "13f11a12-5251-44dd-a9ee-9cc84c00a878";
            DocumentId modelPolicyID = ws.createDocumentId(modelPolicyContentUUID);

            if (parentId.equals(modelPolicyID)) {
               if (isDebug) {
                  s_log.log(Level.FINEST, "Content is a model policy");
               }
               shouldExecute = true;
            }
            else {
               if (isDebug) {
                  s_log.log(Level.FINEST, "Content is not a model policy");
                  s_log.log(Level.FINEST, "parentId = " + parentId);
                  s_log.log(Level.FINEST, "modelPolicyID = " + modelPolicyID);
               }
            }
         }

      }
      catch (Exception e) {
         if (isDebug) {
            s_log.log(Level.FINEST, "Exception " + e.getMessage());
            e.printStackTrace();
         }
      }
      if (isDebug) {
         s_log.exiting("ExpireAdoptedPolicies", "shouldExecute " + shouldExecute);
      }

      return shouldExecute;

   }
}