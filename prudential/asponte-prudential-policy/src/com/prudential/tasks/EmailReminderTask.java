/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.tasks;

import java.util.ArrayList;
import java.util.Properties;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.portal.um.Group;
import com.ibm.portal.um.User;
import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.DocumentId;
import com.ibm.workplace.wcm.api.HTMLComponent;
import com.ibm.workplace.wcm.api.Identity;
import com.ibm.workplace.wcm.api.LibraryComponent;
import com.ibm.workplace.wcm.api.LibraryHTMLComponent;
import com.ibm.workplace.wcm.api.LibraryShortTextComponent;
import com.ibm.workplace.wcm.api.Repository;
import com.ibm.workplace.wcm.api.ShortTextComponent;
import com.ibm.workplace.wcm.api.SiteArea;
import com.ibm.workplace.wcm.api.TextComponent;
import com.ibm.workplace.wcm.api.VirtualPortalContext;
import com.ibm.workplace.wcm.api.WCM_API;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.exceptions.PropertyRetrievalException;
import com.prudential.utils.Utils;
import com.prudential.vp.RetrieveEmailReminderByUuidScopedAction;
import com.prudential.wcm.WCMUtils;

public class EmailReminderTask extends TimerTask {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(EmailReminderTask.class.getName());
  
   private static String s_uuid;

   private static String s_defaultMessage = "";
   
   private static String p_messageComponentNameReviewer = "ReviewerMessage";
   private static String p_messageComponentNameApprover = "ApproverMessage";
   
   public static String getDefaultMessage() {
      return s_defaultMessage;
   }

   public static void setDefaultMessage(String p_defaultMessage) {
      s_defaultMessage = p_defaultMessage;
   }

   public static String getUuid() {
      return s_uuid;
   }

   public static void setUuid(String p_uuid) {
      s_uuid = p_uuid;
   }  

   public ArrayList getEmailAddresses() {
      return emailAddresses;
   }

   public void setEmailAddresses(ArrayList p_emailAddresses) {
      emailAddresses = p_emailAddresses;
   }

   private ArrayList emailAddresses;

   @Override
   public void run() {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);

      if (isDebug) {
         s_log.entering("EmailReminderTask", "run");
      }

      Repository repo = WCM_API.getRepository();
      try {
         VirtualPortalContext vctx = repo.generateVPContextFromContextPath(Utils.getVPName());
         RetrieveEmailReminderByUuidScopedAction vpA = new RetrieveEmailReminderByUuidScopedAction(s_uuid);
         repo.executeInVP(vctx, vpA);
         Document theResult = vpA.getResult();
         setEmailAddresses(vpA.getRecipients());
         String theId = theResult.getId().toString();
         boolean isDraft = true;

         Properties emailProps = WCMUtils.getStandardMailProperties();

         StringBuffer emailMessage = new StringBuffer();
         Content theContent = (Content)theResult;
         DocumentId stageId = theContent.getWorkflowStageId();
         String stageName = stageId.getName().toLowerCase();
         String subject = "";
         boolean isReview = true;
         if(stageName.contains("approve")) {
            isReview = false;
         }
         if(isReview) {
            emailMessage.append(getEmailBodyReview(theContent));
            subject = getEmailSubjectReview(theContent);
         } else {
            emailMessage.append(getEmailBodyApprove(theContent));
            subject = getEmailSubjectApprove(theContent);
         }
         
         // set the email addresses to the approvers

         String fromEmailAddress = emailProps.getProperty("prudential.mail.fromaddress");
         //String subject = "Awaiting your approval";
         String emailBody = emailMessage.toString();
         String emailUser = emailProps.getProperty("prudential.mail.username");
         String emailPassword = emailProps.getProperty("prudential.mail.pass");
         String emailBodyType = "text/html";

         WCMUtils.sendMessage(emailProps, emailUser, emailPassword, fromEmailAddress, emailAddresses, subject, emailBody, emailBodyType);

      }
      catch (Exception e) {
         if (isDebug) {
            s_log.log(Level.FINEST, "Exception " + e.getMessage());
            e.printStackTrace();
         }
      }
      if (isDebug) {
         s_log.exiting("EmailReminderTask", "run");
      }

   }   
   
   String getEmailBodyReview(Document doc) {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      // retrieve from WCM      
   // retrieve from WCM      
      StringBuilder sb = new StringBuilder();
      String componentText = "";
      // try to get the component
      LibraryComponent bodyComponent = Utils.getLibraryComponentByName(Utils.getSystemWorkspace(), WCMUtils.p_reviewEmailTextCmpnt, "PruPolicyDesign");
      if(bodyComponent != null) {
         LibraryHTMLComponent stc = (LibraryHTMLComponent)bodyComponent;
         componentText = stc.getHTML();
         componentText = componentText.replaceAll("DOCUMENTNAME", doc.getTitle());
         componentText = componentText.replaceAll("DOCUMENTURL", Utils.getPreviewURL(doc));
      }
      
      if(componentText.isEmpty()) {
         sb.append("A document " + doc.getTitle() + " is awaiting your review.");
         sb.append("<br><a href='" + Utils.getPreviewURL(doc) + "'>" + doc.getTitle() + "</a><br>");
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
            if (parent.hasComponent(p_messageComponentNameReviewer)) {
               TextComponent tc = (TextComponent) parent.getComponentByReference(p_messageComponentNameReviewer);
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
      
      return body;

   }
   
   String getEmailBodyApprove(Document doc) {
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
         LibraryHTMLComponent stc = (LibraryHTMLComponent)bodyComponent;
         componentText = stc.getHTML();
         componentText = componentText.replaceAll("DOCUMENTNAME", doc.getTitle());
         componentText = componentText.replaceAll("DOCUMENTURL", Utils.getPreviewURL(doc));
      }
      
      if(componentText.isEmpty()) {
         sb.append("A document " + doc.getTitle() + " is awaiting your approval.");
         sb.append("<br><a href='" + Utils.getPreviewURL(doc) + "'>" + doc.getTitle() + "</a><br>");
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
            if (parent.hasComponent(p_messageComponentNameApprover)) {
               TextComponent tc = (TextComponent) parent.getComponentByReference(p_messageComponentNameApprover);
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
   
   String getEmailSubjectReview(Document doc) {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      String subject = "Item "+doc.getTitle()+" is awaiting your review";
      
      // try to get the component
      LibraryComponent subjectComponent = Utils.getLibraryComponentByName(Utils.getSystemWorkspace(), WCMUtils.p_reviewEmailSubjectCmpnt, "PruPolicyDesign");
      if(subjectComponent != null) {
         LibraryShortTextComponent stc = (LibraryShortTextComponent)subjectComponent;
         subject = stc.getText();
         subject = subject.replaceAll("DOCUMENTNAME", doc.getTitle());
      }
      
      return subject;

   }
   
   String getEmailSubjectApprove(Document doc) {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      String subject = "Item "+doc.getTitle()+" is awaiting your approval";
      
      // try to get the component
      LibraryComponent subjectComponent = Utils.getLibraryComponentByName(Utils.getSystemWorkspace(), WCMUtils.p_approveEmailSubjectCmpnt, "PruPolicyDesign");
      if(subjectComponent != null) {
         LibraryShortTextComponent stc = (LibraryShortTextComponent)subjectComponent;
         subject = stc.getText();
         subject = subject.replaceAll("DOCUMENTNAME", doc.getTitle());
      }
      
      return subject;

   }
   
   
   
}
