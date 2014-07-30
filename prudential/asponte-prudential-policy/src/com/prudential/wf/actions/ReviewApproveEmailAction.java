package com.prudential.wf.actions;

import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.ibm.portal.um.*;

import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.DocumentId;
import com.ibm.workplace.wcm.api.HTMLComponent;
import com.ibm.workplace.wcm.api.LibraryComponent;
import com.ibm.workplace.wcm.api.ShortTextComponent;
import com.ibm.workplace.wcm.api.SiteArea;
import com.ibm.workplace.wcm.api.TextComponent;
import com.ibm.workplace.wcm.api.UserSelectionComponent;
import com.ibm.workplace.wcm.api.WebContentCustomWorkflowService;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowActionResult;
import com.ibm.workplace.wcm.api.exceptions.ComponentNotFoundException;
import com.ibm.workplace.wcm.api.exceptions.PropertyRetrievalException;
import com.prudential.renderer.ReviewApproveEmailRenderer;
import com.prudential.utils.Utils;
import com.prudential.wcm.WCMUtils;

public class ReviewApproveEmailAction extends BaseEmailAction {

   private static String m_usercmpntname = "PolicyApprovers";
   private static Logger s_log = Logger.getLogger(ReviewApproveEmailAction.class.getName());
   private static String m_emailcmpntname = "DefaultEmailContents";
   private static String s_delayCmpntName = "ApproveReadyReminderDelay";
   private static String p_messageComponentName = "ApproverMessage";
   
   public ReviewApproveEmailAction() {
      super();
   }

   public ReviewApproveEmailAction(WebContentCustomWorkflowService customWorkflowService) {
      super(customWorkflowService);
   }

   @Override
   String getEmailBody(Document doc) {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      
      if (isDebug) {
         s_log.entering("ReviewApproveEmailAction", "getEmailBody");
      }
      
      // retrieve from WCM      
      StringBuilder sb = new StringBuilder();
      String componentText = "";
      // try to get the component
      LibraryComponent bodyComponent = Utils.getLibraryComponentByName(Utils.getSystemWorkspace(), WCMUtils.p_approveEmailTextCmpnt, "PruPolicyDesign");
      if(bodyComponent != null) {
         HTMLComponent stc = (HTMLComponent)bodyComponent;
         componentText = stc.getHTML();
         componentText.replaceAll("[DOCUMENTNAME]", doc.getName());
         componentText.replaceAll("[DOCUMENTURL]", Utils.getPreviewURL(doc));
      }
      
      if(componentText.isEmpty()) {
         sb.append("A document " + doc.getName() + " is awaiting your approval.");
         sb.append("<br><a href='" + Utils.getPreviewURL(doc) + "'>" + doc.getName() + "</a><br>");
         // now check for the field         
      } else {
         sb.append(componentText);
      }
      
      // include additional text
      if (doc instanceof Content) {
         // get the parent sa
         try {
            Content theContent = (Content) doc;
            DocumentId parentID = theContent.getDirectParent();
            Workspace ws = doc.getSourceWorkspace();
            SiteArea parent = (SiteArea) ws.getById(parentID);
            if (parent.hasComponent(p_messageComponentName)) {
               TextComponent tc = (TextComponent) parent.getComponentByReference(p_messageComponentName);
               sb.append(tc.getText());
            }
         }
         catch (Exception e) {
            if (isDebug) {
               s_log.log(Level.FINEST, "Exception " + e.getMessage());
               e.printStackTrace();
            }
         }

      }
      
      String body = sb.toString();
      
      if (isDebug) {
         s_log.exiting("ReviewApproveEmailAction", "getEmailBody returning "+body);
      }
      
      return body;

   }

   @Override
   String getEmailSubject(Document doc) {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      String subject = "Item "+doc.getName()+" is awaiting your approval";
      
      // try to get the component
      LibraryComponent subjectComponent = Utils.getLibraryComponentByName(Utils.getSystemWorkspace(), WCMUtils.p_approveEmailSubjectCmpnt, "PruPolicyDesign");
      if(subjectComponent != null) {
         ShortTextComponent stc = (ShortTextComponent)subjectComponent;
         subject = stc.getText();
         subject.replaceAll("[DOCUMENTNAME]", doc.getName());
      }
      
      return subject;

   }

   @Override
   String getDelayComponentName() {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      
      return s_delayCmpntName;
      
   }     
   
   /**
    * instead of sending to wcm approvers, send to the PolicyApprovers
    * @see com.prudential.wf.actions.BaseEmailAction#getRecipients(com.ibm.workplace.wcm.api.Document)
    */
   ArrayList getRecipients(Document doc) {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("ReviewApproveEmailAction", "getRecipients for doc "+doc.getName());
      }
      Set recipientSet = new HashSet();
      ArrayList recipientList = null;
      // get the approvers from the doc
      if(doc instanceof Content) {
         Content theContent = (Content)doc;
         // set the workspace to use dn
         Workspace ws = doc.getSourceWorkspace();
         boolean dnUsed = ws.isDistinguishedNamesUsed();
         ws.useDistinguishedNames(true);
         try {
            //String[] approvers = theContent.getCurrentApprovers();
            Set<String> approverSet = new HashSet<String>();
            extractApprovers(doc, approverSet, m_usercmpntname);
            String[] approvers = approverSet.toArray(new String[approverSet.size()]);
            // need string[] of dn values
            if(approvers != null) {
               for(int x=0;x<approvers.length;x++) {
                  String dn = approvers[x];
                  if (isDebug) {
                     s_log.log(Level.FINEST, "dn = "+dn+", retrieve email");
                  }
                  User theUser = Utils.getUserByDN(dn);
                  if(theUser != null) {
                     recipientSet.addAll(Utils.getEmailsUser(theUser));
                  } 
                  else {
                     if (isDebug) {
                        s_log.log(Level.FINEST, "theUser was null, try group");
                     }
                     Group theGroup = Utils.getGroupByDistinguishedName(dn);
                     if(theGroup != null) {
                        recipientSet.addAll(Utils.getEmailsGroup(theGroup));
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
         finally {
            if(ws != null) {
               ws.useDistinguishedNames(dnUsed);
            }
         }
         
      }
      
      if (isDebug) {
         s_log.exiting("ReviewApproveEmailAction", "getRecipients");
      }
      
      recipientList = new ArrayList(Arrays.asList(recipientSet.toArray()));
      return recipientList;

   }
}
