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
import com.prudential.shouldact.ShouldActPolicyEmails;
import com.prudential.utils.Utils;
import com.prudential.wcm.WCMUtils;

import java.util.*;
import java.util.logging.*;

import javax.naming.NamingException;

/**
 *
 * @version 1.0
 */
public class NotifyReferences extends BaseEmailAction {

   private static final Logger s_log = Logger.getLogger(NotifyReferences.class.getName());

   private static List<String> policyTemplateList = Arrays.asList("PP-Policy-AT", "PP-Policy Link-AT");

   // This specifies when the custom action will be executed
   @Override
   public Date getExecuteDate(Document doc) {
      return DATE_EXECUTE_NOW;
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
      LibraryComponent bodyComponent = Utils.getLibraryComponentByName(Utils.getSystemWorkspace(), WCMUtils.p_pendingRetireEmailTextCmpnt,
         "PruPolicyDesign");
      if (bodyComponent != null) {
         LibraryHTMLComponent stc = (LibraryHTMLComponent) bodyComponent;
         componentText = stc.getHTML();
         componentText = componentText.replaceAll("DOCUMENTNAME", doc.getTitle());
         componentText = componentText.replaceAll("DOCUMENTURL", Utils.getPreviewURL(doc));
      }

      if (componentText.isEmpty()) {
         sb.append("The model policy " + doc.getTitle() + " has been marked for retire.");
         sb.append("<br><a href='" + Utils.getPreviewURL(doc) + "'>" + doc.getTitle() + "</a><br>");
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
      LibraryComponent subjectComponent = Utils.getLibraryComponentByName(Utils.getSystemWorkspace(), WCMUtils.p_pendingRetireEmailSubjectCmpnt,
         "PruPolicyDesign");
      if (subjectComponent != null) {
         LibraryShortTextComponent stc = (LibraryShortTextComponent) subjectComponent;
         subject = stc.getText();
         subject = subject.replaceAll("DOCUMENTNAME", doc.getTitle());
      }

      // retrieve from WCM      
      if (subject.isEmpty()) {
         sb.append("The model policy " + doc.getTitle() + " has been marked for retire.");
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

   /**
    * for 
    * @see com.prudential.wf.actions.ReviewApproveEmailAction#shouldSend(com.ibm.workplace.wcm.api.Document)
    */
   boolean shouldSend(Document doc) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);

      if (isDebug) {
         s_log.entering("NotifyReferences", "shouldSend " + doc.getName());
      }
      ShouldActPolicyEmails shouldAct = new ShouldActPolicyEmails();
      boolean shouldSend = false;
      if (shouldAct.shouldAct()) {
         Content theContent = (Content) doc;

         // get the gendateone.  If it's populated and after now, don't send
         Date genDateOne;
         try {
            genDateOne = theContent.getGeneralDateOne();
            if (genDateOne == null || genDateOne.after(new Date())) {
               shouldSend = true;
            }
         }
         catch (PropertyRetrievalException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST)) {
               s_log.log(Level.FINEST, "", e);
            }
         }
      } else if (isDebug) {
         s_log.log(Level.FINEST, "not sending as ");
      }
      if (isDebug) {
         s_log.exiting("NotifyReferences", "shouldSend: " + shouldSend);
      }

      return shouldSend;
   }

   ArrayList getRecipients(Document doc) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      Set targetListSet = new HashSet();
      ArrayList targetList = new ArrayList();
      try {
         Workspace ws = Utils.getSystemWorkspace();
         boolean dnUsed = ws.isDistinguishedNamesUsed();
         ws.useDistinguishedNames(true);
         // Get the references to the current docuemnt
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
                  }

               }
            }
            catch (Exception ex) {
               if (isDebug) {
                  s_log.log(Level.FINEST, ex.toString() + ": Unable to retrieve referencing document (Referer)");
                  ex.printStackTrace();
               }
            }
         }
         ws.useDistinguishedNames(dnUsed);

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

      }
      catch (Exception e) {
         if (isDebug) {
            s_log.log(Level.FINEST, "Exception " + e.getMessage());
            e.printStackTrace();
         }
      }
      return targetList;
   }

}
