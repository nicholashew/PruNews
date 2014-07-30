/**
 * company: asponte
 * author: pete raleigh
 * description: creates a draft policy document and moves to review
 */

package com.prudential.wf.actions;

import javax.naming.InitialContext;

import com.ibm.portal.um.Group;
import com.ibm.portal.um.User;
import com.ibm.workplace.wcm.api.*;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowAction;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowActionResult;
import com.ibm.workplace.wcm.api.custom.Directive;
import com.ibm.workplace.wcm.api.custom.Directives;
import com.ibm.workplace.wcm.api.custom.RollbackDirectiveParams;
import com.ibm.workplace.wcm.api.exceptions.*;
import com.prudential.utils.Utils;
import com.prudential.wcm.WCMUtils;

import java.util.*;
import java.util.logging.*;

import javax.naming.NamingException;

/**
 *
 * @version 1.0
 */
public class NotifyReferences implements CustomWorkflowAction {

   private static final Logger s_log = Logger.getLogger(NotifyReferences.class.getName());

   private static Workspace wksp = null;

   private static List<String> policyTemplateList = Arrays.asList("PP-Policy-AT", "PP-Policy Link-AT");

   // This specifies when the custom action will be executed
   @Override
   public Date getExecuteDate(Document doc) {
      return DATE_EXECUTE_NOW;
   }

   // This method contains the code that will run when the custom action is executed.
   @Override
   public CustomWorkflowActionResult execute(Document doc) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      Directive directive = Directives.CONTINUE;
      RollbackDirectiveParams params = (RollbackDirectiveParams) Directives.ROLLBACK_DOCUMENT.createDirectiveParams();

      if (isDebug) {
         s_log.log(Level.FINEST, "Processing Document: " + doc.getTitle() + " (" + doc.getName() + ")");
      }

      try {
         if (wksp == null) {
            if (isDebug) {
               s_log.log(Level.FINEST, "Retrieving System Workspace");
            }
            wksp = WCM_API.getRepository().getSystemWorkspace();
         }
         if (wksp != null) {
            Set targetListSet = new HashSet();
            ArrayList targetList = new ArrayList();
            boolean dnUsed = wksp.isDistinguishedNamesUsed();
            wksp.useDistinguishedNames(true);
            // Get the references to the current docuemnt
            Reference[] refs = wksp.getReferences(doc.getId());
            for (Reference ref : refs) {
               // Get the document referring to the current document
               DocumentId refId = ref.getRefererDocumentId();

               try {
                  Document refDoc = wksp.getById(refId);
                  if (isDebug) {
                     s_log.log(Level.FINEST, "Processing " + refId);
                  }
                  // Only look for Content items
                  if (refDoc instanceof Content) {
                     Content refCont = (Content) refDoc;

                     // Only look for Content items - which are Published
                     // don't exclude drafts.
                     //if (refCont.isPublished()) {
                     DocumentId refATid = refCont.getAuthoringTemplateID();
                     String refATname = refATid.getName();

                     // Check if the Referenced Content item is a Policy document
                     if (policyTemplateList.contains(refATname)) {

                        /**********************************************
                         ** Perform necessary email notifications to **
                         ** Authors / Owners of referring documents  **
                         **********************************************/
                        String[] authors = refDoc.getAuthors();
                        String[] owners = refDoc.getOwners();
                        targetListSet.addAll(Arrays.asList(authors));
                        targetListSet.addAll(Arrays.asList(owners));
                        if (authors != null) {
                           for (int x = 0; x < authors.length; x++) {
                              String dn = authors[x];
                              if (isDebug) {
                                 s_log.log(Level.FINEST, "dn = " + dn + ", retrieve email");
                              }
                              User theUser = Utils.getUserByDN(dn);
                              if (theUser != null) {
                                 targetListSet.addAll(Utils.getEmailsUser(theUser));
                              }
                              else {
                                 if (isDebug) {
                                    s_log.log(Level.FINEST, "theUser was null, try group");
                                 }
                                 Group theGroup = Utils.getGroupByDistinguishedName(dn);
                                 if (theGroup != null) {
                                    targetListSet.addAll(Utils.getEmailsGroup(theGroup));
                                 }
                                 else {
                                    if (isDebug) {
                                       s_log.log(Level.FINEST, "theGroup was null");
                                    }

                                 }
                              }

                           }
                        }
                        if (owners != null) {
                           for (int x = 0; x < owners.length; x++) {
                              String dn = owners[x];
                              if (isDebug) {
                                 s_log.log(Level.FINEST, "dn = " + dn + ", retrieve email");
                              }
                              User theUser = Utils.getUserByDN(dn);
                              if (theUser != null) {
                                 targetListSet.addAll(Utils.getEmailsUser(theUser));
                              }
                              else {
                                 if (isDebug) {
                                    s_log.log(Level.FINEST, "theUser was null, try group");
                                 }
                                 Group theGroup = Utils.getGroupByDistinguishedName(dn);
                                 if (theGroup != null) {
                                    targetListSet.addAll(Utils.getEmailsGroup(theGroup));
                                 }
                                 else {
                                    if (isDebug) {
                                       s_log.log(Level.FINEST, "theGroup was null");
                                    }

                                 }
                              }

                           }
                        }
                        // Email the "targetList"...

                     }
                     //}
                  }
               }
               catch (Exception ex) {
                  if (isDebug) {
                     s_log.log(Level.FINEST, ex.toString() + ": Unable to retrieve referencing document (Referer)");
                     ex.printStackTrace();
                  }
               }
            }
            wksp.useDistinguishedNames(dnUsed);

            Properties props = WCMUtils.getStandardMailProperties();

            StringBuffer emailMessage = new StringBuffer();

            emailMessage.append(getEmailBody(doc));

            String fromEmailAddress = props.getProperty("prudential.mail.fromaddress");
            String subject = getEmailSubject(doc);
            String emailBody = emailMessage.toString();
            String emailUser = props.getProperty("prudential.mail.username");
            //String emailPassword = props.getProperty("prudential.mail.password");
            String emailPassword = props.getProperty("prudential.mail.pass");
            String emailBodyType = "text/html";

            targetList = new ArrayList(Arrays.asList(targetListSet.toArray()));
            if (isDebug) {
               s_log.log(Level.FINEST, "targetListSet is ");
               Iterator targetListIterator = targetListSet.iterator();
               while (targetListIterator.hasNext()) {
                  s_log.log(Level.FINEST, "targetListIterator contains " + targetListIterator.next().toString());
               }
               targetListIterator = targetList.iterator();
               while (targetListIterator.hasNext()) {
                  s_log.log(Level.FINEST, "targetListIterator from targetList contains " + targetListIterator.next().toString());
               }
            }

            WCMUtils.sendMessage(props, emailUser, emailPassword, fromEmailAddress, targetList, subject, emailBody, emailBodyType);

            // now create the library date component if necessary

         }
      }
      catch (ServiceNotAvailableException ex) {
         if (isDebug) {
            s_log.log(Level.FINEST, ex.toString() + ": Unable to retrieve System Workspace");
            ex.printStackTrace();
         }
      }
      catch (Exception ex) {
         if (isDebug) {
            s_log.log(Level.FINEST, ex.toString() + ": Unable to retrieve references from current document");
            ex.printStackTrace();
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
      String message;
      if (directive == Directives.CONTINUE) {
         message = "Referer Authors / Owners notified";
         if (isDebug) {
            s_log.log(Level.FINEST, message);
         }
         return webContentCustomWorkflowService.createResult(directive, message);
      }
      message = "An error has occurred. Email notifications may not have been sent. Check the logs.";
      s_log.log(Level.WARNING, message);
      params.setCustomErrorMsg(message);
      return webContentCustomWorkflowService.createResult(directive, "Rolling back document.", params);
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
         s_log.entering("NotifyReferences", "getEmailBody");
      }

      // retrieve from WCM      
      StringBuilder sb = new StringBuilder();
      
      String componentText = "";
      // try to get the component
      LibraryComponent bodyComponent = Utils.getLibraryComponentByName(Utils.getSystemWorkspace(), WCMUtils.p_retiringEmailTextCmpnt, "PruPolicyDesign");
      if(bodyComponent != null) {
         HTMLComponent stc = (HTMLComponent)bodyComponent;
         componentText = stc.getHTML();
         componentText.replaceAll("[DOCUMENTNAME]", doc.getName());
         componentText.replaceAll("[DOCUMENTURL]", Utils.getPreviewURL(doc));
      }
      
      if(componentText.isEmpty()) {
         sb.append("The model policy " + doc.getName() + " has been marked for retire.");
         sb.append("<br><a href='" + Utils.getPreviewURL(doc) + "'>" + doc.getName() + "</a><br>");
      }
      else {
         sb.append(componentText);
      }
      
      String body = sb.toString();

      if (isDebug) {
         s_log.exiting("NotifyReferences", "getEmailBody returning " + body);
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
         s_log.entering("NotifyReferences", "getEmailSubject");
      }

      // try to get the component
      String subject = "";
      StringBuilder sb = new StringBuilder();
      LibraryComponent subjectComponent = Utils.getLibraryComponentByName(Utils.getSystemWorkspace(), WCMUtils.p_retiringEmailSubjectCmpnt, "PruPolicyDesign");
      if(subjectComponent != null) {
         ShortTextComponent stc = (ShortTextComponent)subjectComponent;
         subject = stc.getText();
         subject.replaceAll("[DOCUMENTNAME]", doc.getName());
      }
      
      // retrieve from WCM      
      if(subject.isEmpty()) {
         sb.append("The model policy " + doc.getName() + " has been marked for retire.");   
      }
      else {
         sb.append(subject);
      }
      String body = sb.toString();

      
      if (isDebug) {
         s_log.exiting("NotifyReferences", "getEmailSubject returning " + body);
      }

      return body;

   }

}
