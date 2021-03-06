/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.wf.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.portal.um.Group;
import com.ibm.portal.um.User;
import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.DocumentId;
import com.ibm.workplace.wcm.api.DocumentIdIterator;
import com.ibm.workplace.wcm.api.DocumentLibrary;
import com.ibm.workplace.wcm.api.HTMLComponent;
import com.ibm.workplace.wcm.api.LibraryComponent;
import com.ibm.workplace.wcm.api.LibraryHTMLComponent;
import com.ibm.workplace.wcm.api.LibraryShortTextComponent;
import com.ibm.workplace.wcm.api.OptionSelectionComponent;
import com.ibm.workplace.wcm.api.ShortTextComponent;
import com.ibm.workplace.wcm.api.SiteArea;
import com.ibm.workplace.wcm.api.TextComponent;
import com.ibm.workplace.wcm.api.WebContentCustomWorkflowService;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowAction;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowActionResult;
import com.ibm.workplace.wcm.api.custom.Directive;
import com.ibm.workplace.wcm.api.custom.Directives;
import com.ibm.workplace.wcm.api.exceptions.AuthorizationException;
import com.ibm.workplace.wcm.api.exceptions.PropertyRetrievalException;
import com.prudential.shouldact.ShouldActPolicyEmails;
import com.prudential.utils.Utils;
import com.prudential.wcm.WCMUtils;
import com.prudential.wcm.wf.BaseCustomWorkflowAction;

public class NotifyAllPolicyAdmins extends BaseEmailAction {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(NotifyAllPolicyAdmins.class.getName());

   private String p_componentName = "NotifyAllAdmins";

   private String p_designLibraryName = "PruPolicyDesign";

   private String p_contentLibraryName = "PruPolicyContent";

   public NotifyAllPolicyAdmins(WebContentCustomWorkflowService customWorkflowService) {
      super(customWorkflowService);
   }

   @Override
   public Date getExecuteDate(Document p_arg0) {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      return new Date();

   }

   /**
    * 
    * getEmailBody helper method to get the email body
    * @param doc
    * @return
    */
   String getEmailBody(Document doc) {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);

      if (isDebug) {
         s_log.entering("NotifyAllPolicyAdmins", "getEmailBody");
      }

      Content theContent = (Content) doc;
      // retrieve from WCM      
      StringBuilder sb = new StringBuilder();

      // get it from the component if possible
      String componentText = "";
      // try to get the component
      String cmpntName = WCMUtils.p_pendingAvailableEmailTextCmpnt;
      try {
         if (theContent.isPublished()) {
            cmpntName = WCMUtils.p_availableEmailTextCmpnt;
            String expiredStage = theContent.getWorkflowStageId().getId();
            if(expiredStage.equals("5d2653c0-fc47-4f38-8ca3-acf9c4670b32")) {
               cmpntName = WCMUtils.p_retiringEmailTextCmpnt;
            }
         }
         if(theContent.isExpired()) {
            cmpntName = WCMUtils.p_retiringEmailTextCmpnt;
         }
         if (theContent.isDraft()) {
            if (!theContent.isDraftOfPublishedDocument()) {
               //cmpntName = WCMUtils.p_pendingAvailableEmailTextCmpnt;
               cmpntName = WCMUtils.p_pendingAvailableNewEmailTextCmpnt;
            }
            else {
               //cmpntName = WCMUtils.p_pendingAvailableNewEmailTextCmpnt;
               cmpntName = WCMUtils.p_pendingAvailableEmailTextCmpnt;
            }
         }
      }
      catch (PropertyRetrievalException e1) {
         // TODO Auto-generated catch block
         if (s_log.isLoggable(Level.FINEST)) {
            s_log.log(Level.FINEST, "", e1);
         }
      }
      catch (AuthorizationException e) {
         // TODO Auto-generated catch block
         if (s_log.isLoggable(Level.FINEST))
         {
            s_log.log(Level.FINEST, "", e);
         }
      }
      LibraryComponent bodyComponent = Utils.getLibraryComponentByName(Utils.getSystemWorkspace(), cmpntName, "PruPolicyDesign");
      if (bodyComponent != null) {
         LibraryHTMLComponent stc = (LibraryHTMLComponent) bodyComponent;
         componentText = stc.getHTML();
         componentText = componentText.replaceAll("DOCUMENTNAME", doc.getTitle());
         componentText = componentText.replaceAll("DOCUMENTURL", Utils.getPreviewURL(doc));
      }

      if (!componentText.isEmpty()) {
         sb.append(componentText);
      }
      else {
         String message = "The model policy " + doc.getTitle() + " has been updated.";

         try {
            if (theContent.isPublished()) {
               message = "The model policy " + doc.getTitle() + " is ready to be adopted.";
            }
            if (theContent.isDraft()) {
               if (!theContent.isDraftOfPublishedDocument()) {
                  message = "The new policy " + doc.getTitle() + " has been created and will become available to adopt.";
               }
               else {
                  message = "The policy " + doc.getTitle() + " has been updated and will become available to adopt.";
               }
            }
         }
         catch (PropertyRetrievalException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST)) {
               s_log.log(Level.FINEST, "", e);
            }
         }

         sb.append(message);
         sb.append("<br><a href='" + Utils.getPreviewURL(doc) + "'>" + doc.getTitle() + "</a><br>");
      }

      // now check for the field                     
      String body = sb.toString();

      if (isDebug) {
         s_log.exiting("NotifyAllPolicyAdmins", "getEmailBody returning " + body);
      }

      return body;

   }

   /**
    * 
    * getEmailBody helper method to get the email body
    * @param doc
    * @return
    */
   String getEmailSubject(Document doc) {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);

      if (isDebug) {
         s_log.entering("NotifyAllPolicyAdmins", "getEmailSubject");
      }
      StringBuilder sb = new StringBuilder();
      String componentText = "";
      Content theContent = (Content) doc;
      // try to get the component
      String cmpntName = WCMUtils.p_pendingAvailableEmailSubjectCmpnt;
      try {
         if (theContent.isPublished()) {
            cmpntName = WCMUtils.p_availableEmailSubjectCmpnt;
            // check if this is in the retired stage
            String expiredStage = theContent.getWorkflowStageId().getId();
            if(expiredStage.equals("5d2653c0-fc47-4f38-8ca3-acf9c4670b32")) {
               cmpntName = WCMUtils.p_retiringEmailSubjectCmpnt;
            }
         }
         if(theContent.isExpired()) {
            cmpntName = WCMUtils.p_retiringEmailTextCmpnt;
         }
         if (theContent.isDraft()) {
            if (!theContent.isDraftOfPublishedDocument()) {
               cmpntName = WCMUtils.p_pendingAvailableEmailSubjectCmpnt;
            }
            else {
               cmpntName = WCMUtils.p_pendingAvailableNewEmailSubjectCmpnt;
            }
         }
      }
      catch (PropertyRetrievalException e1) {
         // TODO Auto-generated catch block
         if (s_log.isLoggable(Level.FINEST)) {
            s_log.log(Level.FINEST, "", e1);
         }
      }
      catch (AuthorizationException e) {
         // TODO Auto-generated catch block
         if (s_log.isLoggable(Level.FINEST))
         {
            s_log.log(Level.FINEST, "", e);
         }
      }
      LibraryComponent bodyComponent = Utils.getLibraryComponentByName(Utils.getSystemWorkspace(), cmpntName, "PruPolicyDesign");
      if (bodyComponent != null) {
         LibraryShortTextComponent stc = (LibraryShortTextComponent) bodyComponent;
         componentText = stc.getText();
         componentText = componentText.replaceAll("DOCUMENTNAME", doc.getTitle());
      }
      if (!componentText.isEmpty()) {
         sb.append(componentText);
      }
      else {
         String message = "The model policy " + doc.getTitle() + " has been updated.";
         // retrieve from WCM            
         // if its published, state that its published

         try {
            if (theContent.isPublished()) {
               message = "The model policy " + doc.getTitle() + " is ready to be adopted.";
            }
            if (theContent.isDraft()) {
               if (!theContent.isDraftOfPublishedDocument()) {
                  message = "The new policy " + doc.getTitle() + " has been created and will become available to adopt.";
               }
               else {
                  message = "The policy " + doc.getTitle() + " has been updated and will become available to adopt.";
               }
            }
         }
         catch (PropertyRetrievalException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST)) {
               s_log.log(Level.FINEST, "", e);
            }
         }
         sb.append(message);
      }

      // now check for the field                     
      String body = sb.toString();

      if (isDebug) {
         s_log.exiting("NotifyAllPolicyAdmins", "getEmailSubject returning " + body);
      }

      return body;

   }

   @Override
   boolean shouldSend(Document theDoc) {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      boolean shouldSend = false;
      // first check the service
      ShouldActPolicyEmails shouldAct = new ShouldActPolicyEmails();
      if (shouldAct.shouldAct()) {
         try {
            if (theDoc instanceof Content) {
               Content theContent = (Content) theDoc;
               if (((Content) theDoc).isWorkflowMovingBackward()) {
                  if (isDebug) {
                     s_log.log(Level.FINEST, "not sending because content is moving backwards");
                  }
               }
               else {
                  // check to ensure this is a Model policy
                  DocumentId parentId = theContent.getDirectParent();
                  Workspace ws = Utils.getSystemWorkspace();
                  // the /Model Policy/Content site area is the parent for all model policies.
                  String modelPolicyContentUUID = "13f11a12-5251-44dd-a9ee-9cc84c00a878";
                  String usExpenseContentUUID = "a5d337e5-bd53-4561-93a3-8f968dff729c";
                  DocumentId modelPolicyID = ws.createDocumentId(modelPolicyContentUUID);
                  DocumentId usExpensePolicyId = ws.createDocumentId(usExpenseContentUUID);

                  if (parentId.equals(modelPolicyID) || parentId.equals(usExpensePolicyId)) {
                     if (isDebug) {
                        s_log.log(Level.FINEST, "Content is a model policy or US Expense Policy");
                     }
                     if (theContent.hasComponent(p_componentName)) {
                        if (isDebug) {
                           s_log.log(Level.FINEST, "Content has component " + p_componentName);
                        }
                        OptionSelectionComponent osc = (OptionSelectionComponent) theContent.getComponentByReference(p_componentName);
                        String[] values = osc.getSelections();
                        if (values != null) {
                           if (values[0].equalsIgnoreCase("yes")) {
                              if (isDebug) {
                                 s_log.log(Level.FINEST, "should send alerts");
                              }
                              shouldSend = true;
                           }
                        }

                     }
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
         }
         catch (Exception e) {
            if (isDebug) {
               s_log.log(Level.FINEST, "Exception " + e.getMessage());
               e.printStackTrace();
            }
         }
      }
      else if (isDebug) {
         s_log.log(Level.FINEST, "not sending as the service is returning false");
      }
      return shouldSend;
   }

   ArrayList getRecipients(Document doc) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);

      Set recipientSet = new HashSet();
      ArrayList recipientList = null;

      try {
         Workspace ws = Utils.getSystemWorkspace();

         String templateUUID = "9aebb85f-9ddf-4dfd-ac78-d7be346d998b";
         ws.setCurrentDocumentLibrary(ws.getDocumentLibrary(p_designLibraryName));
         DocumentId atID = ws.createDocumentId(templateUUID);
         ws.setCurrentDocumentLibrary(ws.getDocumentLibrary(p_contentLibraryName));
         DocumentIdIterator theSAs = ws.findContentByAuthoringTemplate(atID);
         Set emailUserSet = new HashSet();
         while (theSAs.hasNext()) {
            DocumentId tempId = (DocumentId) theSAs.next();
            if (isDebug) {
               s_log.log(Level.FINEST, "processing " + tempId);
            }
            Document tempDoc = (Document) ws.getById(tempId);
            String[] managers = tempDoc.getManagerAccessMembers();
            for (int x = 0; x < managers.length; x++) {
               String dn = managers[x];
               if (isDebug) {
                  s_log.log(Level.FINEST, "dn = " + dn + ", retrieve email");
               }
               User theUser = Utils.getUserByDN(dn);
               if (theUser != null) {
                  emailUserSet.addAll(Utils.getEmailsUser(theUser));
               }
               else {
                  if (isDebug) {
                     s_log.log(Level.FINEST, "theUser was null, try group");
                  }
                  Group theGroup = Utils.getGroupByDistinguishedName(dn);
                  if (theGroup != null) {
                     emailUserSet.addAll(Utils.getEmailsGroup(theGroup));
                  }
                  else {
                     if (isDebug) {
                        s_log.log(Level.FINEST, "theGroup was null");
                     }
                  }
               }

            }
         }

         recipientList = new ArrayList(Arrays.asList(emailUserSet.toArray()));

      }
      catch (Exception e) {
         if (isDebug) {
            s_log.log(Level.FINEST, "Exception " + e.getMessage());
            e.printStackTrace();
         }
      }

      return recipientList;
   }

}
