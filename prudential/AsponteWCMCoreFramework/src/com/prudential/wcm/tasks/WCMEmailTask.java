package com.prudential.wcm.tasks;

import java.util.logging.Logger;

import java.util.ArrayList;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.internet.AddressException;

import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.DocumentLibrary;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.exceptions.AuthorizationException;
import com.ibm.workplace.wcm.api.exceptions.PropertyRetrievalException;
import java.net.URL;
import java.security.Principal;
import java.util.Date;

import com.ibm.workplace.wcm.api.*;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowAction;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowActionResult;
import com.ibm.workplace.wcm.api.custom.Directive;
import com.ibm.workplace.wcm.api.custom.Directives;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.util.*;

import javax.activation.DataHandler;
import javax.activation.URLDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import com.ibm.portal.um.*;
import com.prudential.utils.Utils;
import com.prudential.wcm.DocumentHandle;
import com.prudential.wcm.WCMUtils;
import com.prudential.wcm.renderer.WCMEmailRenderer;
import com.prudential.wcm.renderer.WCMRenderResults;
import com.prudential.wcm.renderer.WCMRenderer;

/**
 * Runnable Task for sending an email
 * 
 * @author Luke Carpenter
 */
public class WCMEmailTask extends WCMDocumentTaskImpl {
   private static Logger log = Logger.getLogger(WCMEmailTask.class.getName());

   /**
    * Renderer to generate the email content
    */
   private WCMEmailRenderer renderer;

   /**
    * Email session properties
    */
   private Properties emailProperties;

   /**
    * From Email Address
    */
   private String fromEmailAddress = "prudentialalert@gmail.com";

   /**
    * Email Body Content Type
    */
   private String emailBodyType;

   /** hard coded for now, but can pass setter */
   private String email_username = "prudentialalert@gmail.com";
   /** */
   private String email_password = "jk78uijk";   
   /** hard coded for now, but can pass setter */
   private ArrayList recipientList;   

   public String getEmail_username() {
      return email_username;
   }

   public void setEmail_username(String p_email_username) {
      email_username = p_email_username;
   }

   public String getEmail_password() {
      return email_password;
   }

   public void setEmail_password(String p_email_password) {
      email_password = p_email_password;
   }   

   public ArrayList getRecipientList() {
      return recipientList;
   }

   public void setRecipientList(ArrayList p_recipientList) {
      recipientList = p_recipientList;
   }

   public void setRenderer(WCMEmailRenderer renderer) {
      this.renderer = renderer;
   }

   public void setEmailProperties(Properties emailProperties) {
      this.emailProperties = emailProperties;
   }

   public void setFromEmailAddress(String fromEmailAddress) {
      this.fromEmailAddress = fromEmailAddress;
   }

   public void setEmailBodyType(String emailBodyType) {
      this.emailBodyType = emailBodyType;
   }
 

   public boolean isSendEnabled(Document doc) throws AuthorizationException, PropertyRetrievalException {
      return true;
   }

   /**
    * Get the list of recipients to send the email
    */
   protected ArrayList<String> getRecipients(Document doc) throws AuthorizationException, PropertyRetrievalException {
      if(recipientList != null) {
         return recipientList;
      }
      else {
         Content theContent = (Content) doc;
         String[] approvers = theContent.getCurrentApprovers();
         ArrayList<String> toEmailList = new ArrayList<String>();
         if (approvers[0].equalsIgnoreCase("[owners]")) {
            String[] owners = theContent.getOwners();
            toEmailList = WCMUtils.getEmails(owners);
         }
         else {
            toEmailList = WCMUtils.getEmails(approvers);
         }
         return toEmailList;
      }
      
   }

   @Override
   public void process(Document doc) {
      log.entering("WCMEmailTask", "process");
      DocumentLibrary currentdoclib = null;
      Workspace ws = doc.getSourceWorkspace();
      try {
         currentdoclib = ws.getCurrentDocumentLibrary();
         if (this.isSendEnabled(doc)) {
            WCMRenderResults results = renderer.render(doc);
            String subject = String.valueOf(results.getResultSegment(WCMEmailRenderer.SUBJECT_KEY));
            String body = String.valueOf(results.getResultSegment(WCMEmailRenderer.BODY_KEY));
            ArrayList<String> toEmailList = getRecipients(doc);

            WCMUtils.sendMessage(emailProperties, email_username, email_password, fromEmailAddress, toEmailList, subject, body,
               emailBodyType);
         }
      }
      catch (Exception ex) {
         // TODO: error handling
      }
      finally {
         if (currentdoclib != null) {
            ws.setCurrentDocumentLibrary(currentdoclib);
         }
      }
   }

}
