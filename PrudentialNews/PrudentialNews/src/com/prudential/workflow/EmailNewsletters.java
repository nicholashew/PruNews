/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.workflow;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.Date;

import com.ibm.workplace.wcm.api.*;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowAction;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowActionResult;
import com.ibm.workplace.wcm.api.custom.Directive;
import com.ibm.workplace.wcm.api.custom.Directives;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import com.ibm.portal.um.*;
import com.prudential.utils.Utils;
import com.prudential.wcm.WCMUtils;

public class EmailNewsletters implements CustomWorkflowAction {
   private static final Logger s_log = Logger.getLogger(EmailNewsletters.class.getName());

   private final String username = "prudentialalert@gmail.com";

   private final String password = "jk78uijk";

   private static String presentationTemplate = "emailTemplate";

   private static String sendMailComponent = "Send Mail";

   @Override
   /**
    * method to send 
    */
   public CustomWorkflowActionResult execute(Document document) {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("CustomEmailAction", "execute called for document " + document);
      }
      WebContentCustomWorkflowService webContentCustomWorkflowService = null;
      Workspace ws = null;
      CustomWorkflowActionResult result = null;
      Directive directive = Directives.CONTINUE;
      String message = "";
      boolean successful = true;
      boolean shouldSend = true;
      try {
         InitialContext ctx = new InitialContext();
         WebContentService webContentService = (WebContentService) ctx.lookup("portal:service/wcm/WebContentService");
         //ws = webContentService.getRepository().getSystemWorkspace();
         ws = Utils.getSystemWorkspace();
         // Retrieve Custom Workflow Service
         webContentCustomWorkflowService = (WebContentCustomWorkflowService) ctx
            .lookup("portal:service/wcm/WebContentCustomWorkflowService");
         result = webContentCustomWorkflowService.createResult(directive, message);

         Content theContent = (Content) document;
         if (theContent.hasComponent(sendMailComponent)) {
            OptionSelectionComponent osc = (OptionSelectionComponent) theContent.getComponent(sendMailComponent);
            String[] selections = osc.getSelections();
            if (selections.length > 0) {
               String selected = selections[0];
               if (isDebug) {
                  s_log.log(Level.FINEST, "Value from selection " + selected);
               }
               if(selected.equalsIgnoreCase("no")) {
                  shouldSend = false;
               }               
            }
         }
         else {
            if (isDebug) {
               s_log.log(Level.FINEST, "Content doesn't have the component to determine whether to send or not, sending mail");
            }
         }
         if (shouldSend) {
            // get the email addresses
            ArrayList emailAddresses = getEmailAddresses(theContent, ws);
            email(emailAddresses, theContent);
         }
         else {
            if (isDebug) {
               s_log.log(Level.FINEST, "Email should not send, not emailing newsletter");
            }
         }

      }
      catch (Exception e) {
         if (isDebug) {
            e.printStackTrace();
         }
      }

      if (isDebug) {
         s_log.exiting("CustomEmailAction", "execute returning " + result);
      }
      return result;
   }

   @Override
   public Date getExecuteDate(Document p_arg0) {
      // TODO Auto-generated method stub
      return new Date();
   }

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
         s_log.entering("CustomEmailAction", "email for " + p_emailAddresses + " " + theDoc);
      }

      try {

         Properties props = WCMUtils.getStandardMailProperties();

         // Create your new message part
         //BodyPart messageBodyPart = new MimeBodyPart();
         // for now do nothing, will put email code in here
         StringBuffer emailMessage = new StringBuffer();

         emailMessage.append("Newsletter Content");
         // get the newsletter content
         Content theContent = (Content) theDoc;
         String theUuid = theContent.getId().getId();
         // make an http request to get the content
         String urlParameters = "newsletter_uuid=" + URLEncoder.encode(theUuid, "UTF-8");
         String url = "http://wcmwidgets.com:12222/wps/wcm/connect/prudential/prudentialnewsdesign/jspassets/rendernewsletter.jsp";
         String response = this.excutePost(url, urlParameters);
         if (isDebug) {
            s_log.log(Level.FINEST, "response =" + response);
         }
         emailMessage.append(response);
         /*
         if (theContent.hasComponent("IncludedNews")) {
            HTMLComponent includeNews = (HTMLComponent) theContent.getComponent("IncludedNews");
            String currentHTML = includeNews.getHTML();
            emailMessage.append(currentHTML);
         }
         */
         //messageBodyPart.setContent(emailMessage.toString(), "text/html");

         // Create a related multi-part to combine the parts
         //MimeMultipart multipart = new MimeMultipart("related");

         // Add body part to multipart
         //multipart.addBodyPart(messageBodyPart);

         // Associate multi-part with message
         //message.setContent(multipart);

         //message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(p_emailAddresses));
         //message.setFrom(new InternetAddress("chrisknightibm@gmail.com"));
         //Transport.send(message);

         String fromEmailAddress = props.getProperty("prudential.mail.fromaddress");
         String subject = "CMK Test email";
         String emailBody = emailMessage.toString();
         String emailUser = props.getProperty("prudential.mail.username");
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
    * getEmailAddresses get all the recipients.  Have to retrieve the distribution list content
    * @param theContent
    * @param ws
    * @return
    */
   public ArrayList getEmailAddresses(Content theContent, Workspace ws) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      ArrayList returnList = new ArrayList();
      StringBuffer returnAddresses = new StringBuffer();
      try {
         // have to get the distribution list contents included in the list
         if (theContent.hasComponent("Distribution Lists")) {
            HTMLComponent distList = (HTMLComponent) theContent.getComponent("Distribution Lists");
            // this will contain ; delimited list of dist lists for the newsletter
            String list = distList.getHTML();
            String[] contents = list.split(";");
            if (isDebug) {
               s_log.log(Level.FINEST, "Retrieved Distribution Lists from content " + list);
            }
            for (int x = 0; x < contents.length; x++) {
               if (isDebug) {
                  s_log.log(Level.FINEST, "Checking for content " + contents[x]);
               }
               DocumentId distListContentId = Utils.getContentIdByName(ws, contents[x], "PrudentialNewsContent");
               if (distListContentId != null) {
                  // now, get the emails from the content
                  Content distContent = (Content) ws.getById(distListContentId);
                  if (distContent != null) {
                     // get the user
                     if (distContent.hasComponent("Users")) {
                        if (isDebug) {
                           s_log.log(Level.FINEST, "Checking Users");
                        }
                        UserSelectionComponent users = (UserSelectionComponent) distContent.getComponentByReference("Users");
                        Principal[] principals = users.getSelections();
                        boolean oldValue = ws.isDistinguishedNamesUsed();
                        if (principals != null) {
                           for (int p = 0; p < principals.length; p++) {
                              // get the principal, and get the unique email addresses from it.  May be multiples
                              // check for authors/owners group
                              Principal currentPrincipal = principals[p];
                              if (currentPrincipal.getName().equalsIgnoreCase("[authors]")) {
                                 // set workspace to return full dn values so we can
                                 // retrieve the user by dn                              
                                 ws.useDistinguishedNames(true);
                                 String authors[] = theContent.getAuthors();
                                 for (int a = 0; a < authors.length; a++) {
                                    User theUser = Utils.getUserByDN(authors[a]);
                                    if (theUser != null) {
                                       returnList.addAll(Utils.getEmailsUser(theUser));
                                    }
                                    // else, try group
                                    else {
                                       // try to get a group
                                       Group theGroup = Utils.getGroupByDistinguishedName(authors[a]);
                                       returnList.addAll(Utils.getEmailsGroup(theGroup));
                                    }
                                 }
                                 ws.useDistinguishedNames(oldValue);
                              }
                              else if (currentPrincipal.getName().equalsIgnoreCase("[owners]")) {
                                 ws.useDistinguishedNames(true);
                                 String owners[] = theContent.getOwners();
                                 for (int a = 0; a < owners.length; a++) {
                                    User theUser = Utils.getUserByDN(owners[a]);
                                    returnList.add(Utils.getEmailAddressFromUser(theUser));
                                 }
                                 ws.useDistinguishedNames(oldValue);
                              }
                              else {
                                 //User theUser = Utils.getUserByDN(currentPrincipal.getName());
                                 returnList.addAll(Utils.getEmails(currentPrincipal));
                              }

                           }
                        }

                     }
                     if (distContent.hasComponent("Additional Email Addresses")) {
                        if (isDebug) {
                           s_log.log(Level.FINEST, "Checking for Additional Email Addresses");
                        }

                        ShortTextComponent distListEmailAddys = (ShortTextComponent) distContent.getComponent("Additional Email Addresses");
                        String addresses[] = distListEmailAddys.getText().split(";");
                        for (int y = 0; y < addresses.length; y++) {
                           returnList.add(addresses[y]);
                           if (isDebug) {
                              s_log.log(Level.FINEST, "Adding email address " + addresses[y]);
                           }
                        }
                     }

                  }
               }
            }
         }
      }
      catch (Exception e) {
         if (isDebug) {
            s_log.log(Level.FINEST, "Exception occured " + e);
            e.printStackTrace();
         }
      }

      if (returnList.isEmpty()) {
         returnList.add("chris.knight@asponte.com");
      }
      return returnList;
   }

   public static String excutePost(String targetURL, String urlParameters) {
      URL url;
      HttpURLConnection connection = null;
      try {
         //Create connection
         url = new URL(targetURL);
         connection = (HttpURLConnection) url.openConnection();
         connection.setRequestMethod("POST");
         connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

         connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
         connection.setRequestProperty("Content-Language", "en-US");

         connection.setUseCaches(false);
         connection.setDoInput(true);
         connection.setDoOutput(true);

         //Send request
         DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
         wr.writeBytes(urlParameters);
         wr.flush();
         wr.close();

         //Get Response    
         InputStream is = connection.getInputStream();
         BufferedReader rd = new BufferedReader(new InputStreamReader(is));
         String line;
         StringBuffer response = new StringBuffer();
         while ((line = rd.readLine()) != null) {
            response.append(line);
            response.append('\r');
         }
         rd.close();
         return response.toString();

      }
      catch (Exception e) {

         e.printStackTrace();
         return null;

      }
      finally {

         if (connection != null) {
            connection.disconnect();
         }
      }
   }
}