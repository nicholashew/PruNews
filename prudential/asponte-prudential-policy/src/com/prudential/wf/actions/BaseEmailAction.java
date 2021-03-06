package com.prudential.wf.actions;

import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import com.ibm.portal.um.*;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.ContentComponent;
import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.DocumentId;
import com.ibm.workplace.wcm.api.DocumentIdIterator;
import com.ibm.workplace.wcm.api.DocumentLibrary;
import com.ibm.workplace.wcm.api.DocumentTypes;
import com.ibm.workplace.wcm.api.HTMLComponent;
import com.ibm.workplace.wcm.api.LibraryDateComponent;
import com.ibm.workplace.wcm.api.ShortTextComponent;
import com.ibm.workplace.wcm.api.UserSelectionComponent;
import com.ibm.workplace.wcm.api.WebContentCustomWorkflowService;
import com.ibm.workplace.wcm.api.WorkflowedDocument;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowActionResult;
import com.ibm.workplace.wcm.api.custom.Directive;
import com.ibm.workplace.wcm.api.custom.Directives;
import com.ibm.workplace.wcm.api.exceptions.AuthorizationException;
import com.ibm.workplace.wcm.api.exceptions.ComponentNotFoundException;
import com.ibm.workplace.wcm.api.exceptions.DocumentRetrievalException;
import com.ibm.workplace.wcm.api.exceptions.DocumentSaveException;
import com.ibm.workplace.wcm.api.exceptions.DuplicateChildException;
import com.ibm.workplace.wcm.api.exceptions.OperationFailedException;
import com.ibm.workplace.wcm.api.exceptions.PropertyRetrievalException;
import com.prudential.utils.Utils;
import com.prudential.wcm.DocumentHandle;
import com.prudential.wcm.WCMUtils;
import com.prudential.wcm.tasks.WCMDocumentTask;
import com.prudential.wcm.tasks.WCMDocumentTimerTask;
import com.prudential.wcm.wf.BaseCustomWorkflowAction;

/**
 * BaseEmailAction will act as base class.  The subclasses will just change the 
 * getter values for getting body, which content it is, recipient, list, etc
 */
public abstract class BaseEmailAction extends BaseCustomWorkflowAction {
   private static Logger s_log = Logger.getLogger(BaseEmailAction.class.getName());
   
   public BaseEmailAction() {
      this(null);
   }
   
   @Override
   public Date getExecuteDate(Document arg0) {
      return new Date();
   }

   public BaseEmailAction(WebContentCustomWorkflowService customWorkflowService) {
      super(customWorkflowService);     
   }

   
   
   @Override
   public CustomWorkflowActionResult execute(Document doc) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("BaseEmailAction", "execute");
      }
      
      Directive directive = Directives.CONTINUE;
      CustomWorkflowActionResult result = null;
      String message = "BaseEmailAction executing";
      WebContentCustomWorkflowService webContentCustomWorkflowService = null;
            
      try {
         webContentCustomWorkflowService = WCMUtils.getWebContentCustomWorkflowService();
         result = webContentCustomWorkflowService.createResult(directive, message);
         if(shouldSend(doc)) {
            email(getRecipients(doc), doc);     
         }
             
      }
      catch (Exception e) {
         if (isDebug) {
            e.printStackTrace();
         }
      }   
      return result;
   } 
   
   /**
    *
    * in both cases it's the content approvers. Any subclass should override if necessary.
    */
   ArrayList getRecipients(Document doc) {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("BaseEmailAction", "getRecipients for doc "+doc.getName());
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
            String[] approvers = theContent.getCurrentApprovers();
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
         catch (PropertyRetrievalException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST))
            {
               s_log.log(Level.FINEST, "", e);
            }
         }
         finally {
            if(ws != null) {
               ws.useDistinguishedNames(dnUsed);
            }
         }
         
      }
      
      if (isDebug) {
         s_log.exiting("BaseEmailAction", "getRecipients");
      }
      
      recipientList = new ArrayList(Arrays.asList(recipientSet.toArray()));
      return recipientList;

   }
   
      
   /**
    * 
    * getEmailBody return the email body
    * @return String for the email body
    */
   abstract String getEmailBody(Document doc);
   
   /**
    * 
    * getEmailSubject get the subject for the email
    * @return String the subject for the email
    */
   abstract String getEmailSubject(Document doc);
   
   abstract boolean shouldSend(Document doc);
   
   /**
    * 
    * email description
    * @param p_emailAddresses ArrayList of email addresses
    * @param p_contentuuid the UUID of the newsletter to email
    * @param ws
    */
   public void email(ArrayList p_emailAddresses, Document theDoc) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("BaseEmailAction", "email for " + p_emailAddresses + " " + theDoc);
      }

      try {

         Properties props = WCMUtils.getStandardMailProperties();
        
         StringBuffer emailMessage = new StringBuffer();

         emailMessage.append(getEmailBody(theDoc));     

         String fromEmailAddress = props.getProperty("prudential.mail.fromaddress");
         String subject = getEmailSubject(theDoc);
         String emailBody = emailMessage.toString();
         String emailUser = props.getProperty("prudential.mail.username");
         //String emailPassword = props.getProperty("prudential.mail.password");
         String emailPassword = props.getProperty("prudential.mail.pass");
         String emailBodyType = "text/html";

         WCMUtils.sendMessage(props, emailUser, emailPassword, fromEmailAddress, p_emailAddresses, subject, emailBody, emailBodyType);

      }
      catch (Exception e) {
         if (isDebug) {
            e.printStackTrace();
         }

      }
      if (isDebug) {
         s_log.exiting("CustomEmailAction", "email");
      }
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
