/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.wf.actions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.portal.um.Group;
import com.ibm.portal.um.User;
import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.exceptions.PropertyRetrievalException;
import com.prudential.shouldact.ShouldActPolicyEmails;
import com.prudential.utils.Utils;
/**
 * 
 */
public class EmailContentAuthors extends BaseEmailAction {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(EmailContentAuthors.class.getName());

   /**
    * @see com.prudential.wf.actions.BaseEmailAction#getEmailBody(com.ibm.workplace.wcm.api.Document)
    */
   String getEmailBody(Document p_doc) {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      StringBuilder sb = new StringBuilder();
      String returnString = "";
      
      sb.append("A document " + p_doc.getTitle() + " is awaiting your attention.");
      sb.append("<br><a href='" + Utils.getAuthoringURL(p_doc) + "'>" + p_doc.getTitle() + "</a><br>");
      
      returnString = sb.toString();
      
      return returnString;

   }
   String getEmailSubject(Document p_doc) {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      StringBuilder sb = new StringBuilder();
      String returnString = "";
      sb.append("Item " + p_doc.getTitle() + " is awaiting your attention");

      returnString = sb.toString();

      return returnString;

   }

   /**
    * @see com.prudential.wf.actions.BaseEmailAction#shouldSend(com.ibm.workplace.wcm.api.Document)
    */
   @Override
   boolean shouldSend(Document doc) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      boolean shouldSend = false;
      
      ShouldActPolicyEmails shouldAct = new ShouldActPolicyEmails();
      shouldSend = shouldAct.shouldAct();
      if (isDebug) {
         s_log.exiting("EmailContentAuthors", "shouldSend "+shouldSend);
      }
      
      return shouldSend;
   }
   
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
            String[] authors = theContent.getAuthors();
            if(authors != null) {
               for(int x=0;x<authors.length;x++) {
                  String dn = authors[x];
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
         catch (Exception e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST))
            {
               s_log.log(Level.FINEST, "", e);
               e.printStackTrace();
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
}

