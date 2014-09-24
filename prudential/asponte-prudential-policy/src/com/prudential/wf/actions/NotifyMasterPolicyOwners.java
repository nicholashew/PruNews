/**
 * company: asponte
 * author: Chris Knight
 * description: When copied policy is published, notify the master policy admins
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

import java.security.Principal;
import java.util.*;
import java.util.logging.*;

import javax.naming.NamingException;

/**
 *
 * @version 1.0
 */
public class NotifyMasterPolicyOwners extends BaseEmailAction {

   private static final Logger s_log = Logger.getLogger(NotifyMasterPolicyOwners.class.getName());

   private static String linkCmpntName = "ModelPolicy";
   private static String contactCmpntName = "ModelPolicyChangeContact";

   private static List<String> policyTemplateList = Arrays.asList("PP-Policy-AT");

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
         s_log.entering("NotifyMasterPolicyOwners", "getEmailBody");
      }

      // retrieve from WCM      
      StringBuilder sb = new StringBuilder();

      String componentText = "";
      // try to get the component
      LibraryComponent bodyComponent = Utils.getLibraryComponentByName(Utils.getSystemWorkspace(), WCMUtils.p_copiedPublishEmailTextCmpnt,
         "PruPolicyDesign");
      if (bodyComponent != null) {
         LibraryHTMLComponent stc = (LibraryHTMLComponent) bodyComponent;
         componentText = stc.getHTML();
         componentText = componentText.replaceAll("DOCUMENTNAME", doc.getTitle());
         componentText = componentText.replaceAll("DOCUMENTURL", Utils.getPreviewURL(doc));
      }

      if (componentText.isEmpty()) {
         sb.append("A copy <a href='" + Utils.getPreviewURL(doc) + "'>" + doc.getTitle() + "</a> of a master policy has been published.");
      }
      else {
         sb.append(componentText);
      }

      String body = sb.toString();

      if (isDebug) {
         s_log.exiting("NotifyMasterPolicyOwners", "getEmailBody returning " + body);
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
         s_log.entering("NotifyMasterPolicyOwners", "getEmailSubject");
      }

      // try to get the component
      String subject = "";
      StringBuilder sb = new StringBuilder();
      LibraryComponent subjectComponent = Utils.getLibraryComponentByName(Utils.getSystemWorkspace(),
         WCMUtils.p_copiedPublishEmailSubjectCmpnt, "PruPolicyDesign");
      if (subjectComponent != null) {
         LibraryShortTextComponent stc = (LibraryShortTextComponent) subjectComponent;
         subject = stc.getText();
         subject = subject.replaceAll("DOCUMENTNAME", doc.getTitle());
      }

      // retrieve from WCM      
      if (subject.isEmpty()) {
         sb.append("A master policy has been copied.");
      }
      else {
         sb.append(subject);
      }
      String body = sb.toString();

      if (isDebug) {
         s_log.exiting("NotifyMasterPolicyOwners", "getEmailSubject returning " + body);
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
         s_log.entering("NotifyMasterPolicyOwners", "shouldSend " + doc.getName());
      }
      ShouldActPolicyEmails shouldAct = new ShouldActPolicyEmails();
      boolean shouldSend = false;
      if (shouldAct.shouldAct()) {
         // check and make sure there's a master policy
         // first make sure the authoring template is not an adopted one
         Content refCont = (Content) doc;
         DocumentId refATid;
         try {
            refATid = refCont.getAuthoringTemplateID();
            String refATname = refATid.getName();
            if (policyTemplateList.contains(refATname)) {
               ContentComponent cc = WCMUtils.getContentComponent(doc, linkCmpntName);
               if (cc != null) {
                  shouldSend = true;
               }
            }
         }
         catch (AuthorizationException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST)) {
               s_log.log(Level.FINEST, "", e);
            }
         }
         catch (PropertyRetrievalException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST)) {
               s_log.log(Level.FINEST, "", e);
            }
         }

      }
      else if (isDebug) {
         s_log.log(Level.FINEST, "not sending as component not existing on the content");
      }
      if (isDebug) {
         s_log.exiting("NotifyMasterPolicyOwners", "shouldSend: " + shouldSend);
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
         try {
            ContentComponent cc = WCMUtils.getContentComponent(doc, linkCmpntName);
            if(cc != null) {
               LinkComponent lc = (LinkComponent)cc;
               DocumentId masterPolicyID = lc.getDocumentReference();
               if (isDebug) {
                  s_log.log(Level.FINEST, "masterPolicyID "+masterPolicyID);
               }
               if(masterPolicyID != null) {
                  Content masterPolicy = (Content)ws.getById(masterPolicyID);
                  // get the authors
                  String[] owners = masterPolicy.getOwners();
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
                  // now check the ModelPolicyChangeContact
                  Set<String> contactSet = new HashSet<String>();
                  extractContacts(masterPolicy, contactSet, contactCmpntName);
                  String[] contacts = contactSet.toArray(new String[contactSet.size()]);
                  if(contacts != null) {
                     for(int x=0;x<contacts.length;x++) {
                        String dn = contacts[x];
                        if (isDebug) {
                           s_log.log(Level.FINEST, "dn = "+dn+", retrieve email");
                        }
                        User theUser = Utils.getUserByDN(dn);
                        if(theUser != null) {
                           targetListSet.addAll(Utils.getEmailsUser(theUser));
                        } 
                        else {
                           if (isDebug) {
                              s_log.log(Level.FINEST, "theUser was null, try group");
                           }
                           Group theGroup = Utils.getGroupByDistinguishedName(dn);
                           if(theGroup != null) {
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
   
   protected void extractContacts(Document doc, Set<String> approvers, String p_componentName) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("NotifyMasterPolicyOwners", "extractContacts "+p_componentName);
      }
      
      ContentComponent cmpnt = WCMUtils.getContentComponent(doc, p_componentName);
      if (cmpnt != null && cmpnt instanceof UserSelectionComponent) {
         if (isDebug) {
            s_log.log(Level.FINEST, "Content had component "+p_componentName);
         }
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
