package com.prudential.wcm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NamingException;

import com.ibm.portal.um.Group;
import com.ibm.portal.um.Principal;
import com.ibm.portal.um.PumaHome;
import com.ibm.portal.um.PumaLocator;
import com.ibm.portal.um.User;
import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.ContentComponent;
import com.ibm.workplace.wcm.api.ContentComponentContainer;
import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.DocumentId;
import com.ibm.workplace.wcm.api.DocumentIdIterator;
import com.ibm.workplace.wcm.api.HTMLComponent;
import com.ibm.workplace.wcm.api.HistoryLogEntry;
import com.ibm.workplace.wcm.api.HistoryLogIterator;
import com.ibm.workplace.wcm.api.WebContentCustomWorkflowService;
import com.ibm.workplace.wcm.api.WebContentService;
import com.ibm.workplace.wcm.api.Workflow;
import com.ibm.workplace.wcm.api.WorkflowStage;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.exceptions.ComponentNotFoundException;
import com.ibm.workplace.wcm.api.exceptions.OperationFailedException;
import com.ibm.workplace.wcm.api.exceptions.ServiceNotAvailableException;
import com.ibm.workplace.wcm.services.config.WCMConfig;
import com.prudential.utils.Utils;

public class WCMUtils {
   private static final Logger s_log = Logger.getLogger(WCMUtils.class.getName());

   private static final String WCM_SERVICE_JNDI_NAME = "portal:service/wcm/WebContentService";

   private static final String WCM_CWF_SERVICE_JNDI_NAME = "portal:service/wcm/WebContentCustomWorkflowService";

   private static final Timer TIMER_SERVICE = new Timer();
   
   public static String p_reviewEmailSubjectCmpnt = "ReviewEmailSubject";
   public static String p_approveEmailSubjectCmpnt = "ApproveEmailSubject";
   public static String p_reviewEmailTextCmpnt = "ReviewEmailText";
   public static String p_approveEmailTextCmpnt = "ApproveEmailText";
   
   public static String p_retiringEmailSubjectCmpnt = "RetiringEmailSubject";
   public static String p_pendingAvailableEmailSubjectCmpnt = "PendingAvailableEmailSubject";
   public static String p_pendingAvailableNewEmailSubjectCmpnt = "PendingAvailableNewEmailSubject";
   public static String p_pendingRetireEmailSubjectCmpnt = "PendingRetireEmailSubject";
   public static String p_availableEmailSubjectCmpnt = "AvailableEmailSubject";
   
   public static String p_pendingAvailableEmailTextCmpnt = "PendingAvailableEmailText";
   public static String p_pendingAvailableNewEmailTextCmpnt = "PendingAvailableNewEmailText";
   public static String p_retiringEmailTextCmpnt = "RetiringEmailText";
   public static String p_pendingRetireEmailTextCmpnt = "PendingRetireEmailText";   
   public static String p_availableEmailTextCmpnt = "AvailableEmailText";   
   

   private static PumaHome pumaHome = null;

   public static Timer getGlobalTimerService() {
      s_log.entering("WCMUtils", "getGlobalTimerService");
      return TIMER_SERVICE;
   }

   public static WebContentService getWebContentService() throws NamingException {
      s_log.entering("WCMUtils", "getWebContentService");
      InitialContext ctx = new InitialContext();
      return (WebContentService) ctx.lookup(WCM_SERVICE_JNDI_NAME);
   }

   public static WebContentCustomWorkflowService getWebContentCustomWorkflowService() throws NamingException {
      s_log.entering("WCMUtils", "getWebContentCustomWorkflowService");
      InitialContext ctx = new InitialContext();
      return (WebContentCustomWorkflowService) ctx.lookup(WCM_CWF_SERVICE_JNDI_NAME);
   }

   public static ContentComponent getContentComponent(Document doc, String cmpntName) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("WCMUtils", "getContentComponent "+cmpntName+" in content "+doc.getName());
      }
            
      ContentComponent cmpnt = null;
      if (doc instanceof ContentComponentContainer) {
         cmpnt = getContentComponent((ContentComponentContainer) doc, cmpntName);
      }
      else {
         if (isDebug) {
            s_log.log(Level.FINEST, "doc instanceof ContentComponentContainer was false");
         }
      }
      if (isDebug) {
         s_log.exiting("WCMUtils", "getContentComponent");
      }
      
     
      return cmpnt;
   }

   public static ContentComponent getContentComponent(ContentComponentContainer c, String cmpntName) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("WCMUtils", "getContentComponent for content "+c.getName()+" cmpnt "+cmpntName);
      }
      
      s_log.entering("WCMUtils", "getContentComponent");
      ContentComponent cmpnt = null;
      if (c.hasComponent(cmpntName)) {
         try {
            cmpnt = c.getComponent(cmpntName);
         }
         catch (ComponentNotFoundException e) {
            // TODO: log exception
            if (isDebug) {
               s_log.log(Level.FINEST, "Exception");
               e.printStackTrace();
            }
         }
      }
      if (isDebug) {
         s_log.exiting("WCMUtils", "getContentComponent returning "+cmpnt);
      }
      
      return cmpnt;
   }

   public static boolean stageIsBefore(Workspace wkspc, String thisStage, String otherStage, DocumentId wflowID) {
      s_log.entering("WCMUtils", "stageIsBefore");
      int index = 0;
      int thisIndex = 0;
      int otherIndex = 0;
      try {
         Workflow wflow = (Workflow) wkspc.getById(wflowID);

         DocumentIdIterator itr = wflow.getStagesIterator();
         while (itr.hasNext()) {
            index++;
            DocumentId stageDocId = (DocumentId) itr.next();
            WorkflowStage stage = null;

            stage = (WorkflowStage) wkspc.getById(stageDocId);

            String stageName = stage.getName();
            if (stageName.equals(thisStage)) {
               thisIndex = index;
            }
            else if (stageName.equals(otherStage)) {
               otherIndex = index;
            }
         }
      }
      catch (Exception e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      // Check to see if this one is before the other one.
      boolean result = thisIndex < otherIndex;

      return result;
   }

   /*
    * This code looks at the history log and pulls out and returns the last
    * numItems entries.
    */
   public static String getDocHistoryEntries(Content p_document, int numItems) {
      s_log.entering("WCMUtils", "getDocHistoryEntries");
      HistoryLogIterator historyIterator = p_document.getHistoryLog();

      // List listHistory = new ArrayList();
      ArrayList<HistoryLogEntry> listHistory = new ArrayList<HistoryLogEntry>();
      while (historyIterator.hasNext()) {

         HistoryLogEntry logEntry = (HistoryLogEntry) historyIterator.nextLogEntry();
         listHistory.add(logEntry);
      }
      StringBuffer strUserComments = new StringBuffer("");
      int lastEntry = listHistory.size() - 1;
      int count = 0;
      while (!listHistory.isEmpty()) {
         // Start from the end
         HistoryLogEntry logEntry = (HistoryLogEntry) listHistory.get(lastEntry);
         strUserComments = strUserComments.append(logEntry.getDate() + ": ").append(logEntry.getName() + ": ")
            .append(logEntry.getMessage()).append("<BR />");
         if (count >= numItems) {
            // Abort at this point
            listHistory.clear();
         }
         else {
            // Remove the last entry
            listHistory.remove(lastEntry);
            lastEntry--;
            count++;
         }
      }
      return strUserComments.toString();
   }

   /*
    * This code gets the email address(es) for the input users or groups
    */
   public static ArrayList<String> getEmails(String[] emailList) {
      s_log.entering("WCMUtils", "getEmails");
      ArrayList<String> outputEmails = new ArrayList<String>();

      try {
         for (int i = 0; i < emailList.length; i++) {
            // See if this is a user
            // if user is found
            List<User> userList = getPumaHome().getLocator().findUsersByAttribute("cn", emailList[i]);
            if (!userList.isEmpty()) {
               // This is a user, get the email attribute
               // Add a single attribute name to get
               List<String> attributeNames = new ArrayList<String>(1);
               attributeNames.add("mail");
               // Get the attribute value
               Map<String, Object> attributes = getPumaHome().getProfile().getAttributes(userList.get(0), attributeNames);
               // Look at attributes returned
               String email = (String) attributes.get("mail");
               outputEmails.add(email);
            }
            else {
               // It wasn't a user, let's see if it is a group.
               List<Group> groups = new ArrayList<Group>();
               groups = getPumaHome().getLocator().findGroupsByAttribute("cn", emailList[i]);
               if (!groups.isEmpty()) {
                  // The group was found, now get the members and their
                  // email
                  List<Principal> members = null;
                  members = getPumaHome().getLocator().findMembersByGroup(groups.get(0), true);
                  ListIterator<Principal> litr = members.listIterator();
                  while (litr.hasNext()) {
                     Principal member = litr.next();
                     // Add a single attribute name to get
                     List<String> attributeNames = new ArrayList<String>(1);
                     attributeNames.add("mail");
                     // Get the attribute value for this member
                     Map<String, Object> attributes = getPumaHome().getProfile().getAttributes(member, attributeNames);
                     // Look at attributes returned
                     String email = (String) attributes.get("mail");
                     outputEmails.add(email);
                  }
               }
            }
         }
      }
      catch (Exception e) {
         e.printStackTrace();
         // TODO: Log Exception
      }

      return outputEmails;
   }

   protected static PumaHome getPumaHome() throws NamingException, InvalidNameException {
      if (pumaHome == null) {
         Context ctx = new InitialContext();
         pumaHome = (PumaHome) ctx.lookup(new CompositeName(PumaHome.JNDI_NAME));
      }
      return pumaHome;
   }

   public static User getUserById(final String p_dn) {
      User user = null;
      try {
         List<User> users = Collections.emptyList();
         boolean retrieved = false;
         try {
            users = getPumaHome().getLocator().findUsersByDefaultAttribute(p_dn);
            if (!users.isEmpty()) {
               user = users.get(0);
               retrieved = true;
            }
         }
         catch (Exception e) {
            e.printStackTrace();
         }
         // if havent retrieved, try passing cn
         if (!retrieved) {
            try {
               users = getPumaHome().getLocator().findUsersByAttribute("cn", p_dn);
               if (!users.isEmpty()) {
                  user = users.get(0);
                  retrieved = true;
               }
            }
            catch (Exception e) {
               e.printStackTrace();
            }
         }
         // if havent retrieved, try passing cn
         if (!retrieved) {
            try {
               users = getPumaHome().getLocator().findUsersByAttribute("uid", p_dn);
               if (!users.isEmpty()) {
                  user = users.get(0);
                  retrieved = true;
               }
            }
            catch (Exception e) {
               e.printStackTrace();
            }
         }
      }
      catch (Exception e) {
         e.printStackTrace();
      }

      return user;
   }

   /**
    * 
    * sendMessage description
    * @param emailProperties
    * @param email_user
    * @param email_password
    * @param fromEmailAddress
    * @param toEmailList
    * @param subject
    * @param emailBody
    * @param emailBodyType
    * @throws NoSuchProviderException
    * @throws MessagingException
    * @throws AddressException
    */
   public static void sendMessage(Properties emailProperties, final String email_user, final String email_password,
      String fromEmailAddress, ArrayList<String> toEmailList, String subject, String emailBody, String emailBodyType)
      throws NoSuchProviderException, MessagingException, AddressException {

      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("WCMUtils", "sendMessage "+emailProperties);
      }

      Session mailSession = null;
      Transport transport = null;
      //final String email_password2 = Utils.email_password;
      try {

         if (email_user != null && email_user.length() > 0) {
            mailSession = Session.getInstance(emailProperties, new javax.mail.Authenticator() {
               protected PasswordAuthentication getPasswordAuthentication() {
                  return new PasswordAuthentication(email_user, email_password);
               }
            });
         }
         else {
            mailSession = javax.mail.Session.getDefaultInstance(emailProperties, null);
         }
         transport = mailSession.getTransport();
         transport.connect();
         Message message = new MimeMessage(mailSession);

         message.setSubject(subject);

         // Create your new message part
         BodyPart messageBodyPart = new MimeBodyPart();
         // for now do nothing, will put email code in here
         StringBuffer emailMessage = new StringBuffer();

         emailMessage.append(emailBody);

         messageBodyPart.setContent(emailMessage.toString(), "text/html");

         // Create a related multi-part to combine the parts
         MimeMultipart multipart = new MimeMultipart("related");

         // Add body part to multipart
         multipart.addBodyPart(messageBodyPart);

         // Associate multi-part with message
         message.setContent(multipart);
         ArrayList internetAddresses = new ArrayList();
         
         for (int i = 0; i < toEmailList.size(); i++) {
            String addressString = (String)toEmailList.get(i);
            if (isDebug) {
               s_log.log(Level.FINEST, "adding address "+addressString);
            }
            try {
               InternetAddress tempAddress = new InternetAddress(addressString);   
               if(tempAddress != null) {
                  internetAddresses.add(tempAddress);
               }
            }
            catch(Exception e) {
               if (isDebug) {
                  s_log.log(Level.FINEST, " Exception creating address "+e.getMessage());
                  e.printStackTrace();
               }               
            }            
         }
         InternetAddress[] address = (InternetAddress[]) internetAddresses.toArray(new InternetAddress[internetAddresses.size()]);
         //InternetAddress[] address = new InternetAddress[toEmailList.size()];

         message.setRecipients(Message.RecipientType.TO, address);
         message.setFrom(new InternetAddress(fromEmailAddress));
         Transport.send(message);
      } catch (Exception e) {
         if (isDebug) {
            s_log.log(Level.FINEST, "Exception occured sending email");
            e.printStackTrace();
         }
      }
      finally {
         if (transport != null && transport.isConnected()) {
            transport.close();
         }
      }
      if (isDebug) {
         s_log.exiting("WCMUtils", "sendMessage");
      }

   }

   /**
    * helper method to retrieve default properties for sending mail
    */
   public static Properties getStandardMailProperties() {

      boolean isDebug = s_log.isLoggable(Level.FINEST);
      
      Properties props = new Properties();
      String smtphost = WCMConfig.getString("connect.connector.mailconnector.defaultsmtpserver", "smtp.gmail.com");
      String smtpport = WCMConfig.getString("connect.connector.mailconnector.DefaultSMTPPort", "25");
      //String smtpport = WCMConfig.getString("connect.connector.mailconnector.DefaultSMTPPort", "587");

      // check for username/password
      String mailUsername = WCMConfig.getString("connect.connector.mailconnector.defaultusername", "");
      boolean isWcmWidgets = WCMConfig.getBoolean("connect.connector.mailconnector.iswcmwidgets",false);
      String mailPassword = WCMConfig.getString("connect.connector.mailconnector.defaultpassword", "");
      // for local deployment testing
      if(isWcmWidgets) {
         mailPassword = Utils.email_password;
         smtpport = "587";
      }
      String fromAddress = WCMConfig.getString("connect.connector.mailconnector.defaultfromaddress", "");
      String replytoAddress = WCMConfig.getString("connect.connector.mailconnector.defaultreplytoaddress", "");
      String starttls = WCMConfig.getString("connect.connector.mailconnector.isssl", "false");
      
      if(isDebug) {
         props.put("mail.debug", "true");
      }
      if (mailUsername != null && mailUsername.length() > 0) {
         props.put("mail.smtp.auth", "true");
      }
      props.put("mail.transport.protocol", "smtp");
      props.put("mail.smtp.sendpartial", "true");
      props.put("mail.smtp.starttls.enable", starttls);
      props.put("mail.smtp.host", smtphost);
      props.put("mail.smtp.port", smtpport);
      props.put("prudential.mail.username", mailUsername);
      props.put("prudential.mail.fromaddress", fromAddress);
      props.put("prudential.mail.pass", mailPassword);

      if (isDebug) {
         s_log.log(Level.FINEST, "smtphost = "+smtphost);
         s_log.log(Level.FINEST, "smtpport = "+smtpport);
         s_log.log(Level.FINEST, "mailUsername = "+mailUsername);
         s_log.log(Level.FINEST, "mailPassword = "+mailPassword);
         s_log.log(Level.FINEST, "fromAddress = "+fromAddress);
      }
      return props;
   }
   
   /**
    * helper method to determine if this is wcmwidgets environment
    * isWCMWidgets description
    * @return
    */
   public static boolean isWCMWidgets() {
      boolean isWcmWidgets = WCMConfig.getBoolean("connect.connector.mailconnector.iswcmwidgets",false);
      
      return isWcmWidgets;
   }
}
