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
import com.ibm.workplace.wcm.api.Identity;
import com.ibm.workplace.wcm.api.Repository;
import com.ibm.workplace.wcm.api.VirtualPortalContext;
import com.ibm.workplace.wcm.api.WCM_API;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.exceptions.PropertyRetrievalException;
import com.prudential.policy.vp.EmailReminderVPScopedAction;
import com.prudential.utils.Utils;
import com.prudential.vp.RetrieveEmailReminderByUuidScopedAction;
import com.prudential.wcm.WCMUtils;

public class EmailReminderTask extends TimerTask {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(EmailReminderTask.class.getName());

   private static String emailText;
   
   private static String s_host = "https://inside-dev.prudential.com/wps/myportal";
  
   private static String s_uuid;

   private static String s_defaultMessage = "";
   
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

   public static String getEmailText(String s_id, boolean isDraft) {
      StringBuilder sb = new StringBuilder();
      sb.append("An item is waiting your review.<br>");
      sb.append("<a href='");
      sb.append(getItemURL(s_id,isDraft));
      sb.append("'>Open Item</a>");
      sb.append("<br>");
      sb.append(s_defaultMessage);
      sb.append("<br>");
      emailText = sb.toString();
      return emailText;
   }

   public static void setEmailText(String p_emailText) {
      emailText = p_emailText;
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

         emailMessage.append(getEmailText(theId,isDraft));
         // set the email addresses to the approvers

         String fromEmailAddress = emailProps.getProperty("prudential.mail.fromaddress");
         String subject = "Awaiting your approval";
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
   /**
    * Retrieve the URL for a given IdentityReference
    * @param p_idr the IdentityReference
    * @param p_isDraft flag whether item is draft of not
    * @return the URL as String
    */
   private static String getItemURL(String p_idr, boolean p_isDraft)
   {
      String baseURL = s_host;
      StringBuilder urlStr = new StringBuilder(baseURL);

      // Modified the email action to use remote action URLs instead of the old OpenContent
      // and OpenObject URL commands.
      // generate a preview URL
      urlStr.append("?wcmAuthoringAction=preview&docid=com.ibm.workplace.wcm.api.WCM_Content/"+p_idr);
      if (p_isDraft)
      {
         urlStr.append("&isdraft=true");
      }
      return urlStr.toString();
   }   
   
}
