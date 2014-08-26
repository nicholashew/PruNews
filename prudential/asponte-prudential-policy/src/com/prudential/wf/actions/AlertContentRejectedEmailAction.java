/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.wf.actions;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.LibraryComponent;
import com.ibm.workplace.wcm.api.LibraryHTMLComponent;
import com.ibm.workplace.wcm.api.LibraryShortTextComponent;
import com.prudential.utils.Utils;
import com.prudential.wcm.WCMUtils;
public class AlertContentRejectedEmailAction extends EmailContentAuthors{

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(AlertContentRejectedEmailAction.class.getName());
   
   /**
    * @see com.prudential.wf.actions.BaseEmailAction#getEmailBody(com.ibm.workplace.wcm.api.Document)
    */
   String getEmailBody(Document doc) {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);      
           
      // retrieve from WCM      
      StringBuilder sb = new StringBuilder();

      String componentText = "";
      // try to get the component
      LibraryComponent bodyComponent = Utils.getLibraryComponentByName(Utils.getSystemWorkspace(), WCMUtils.p_rejectedEmailTextCmpnt,
         "PruPolicyDesign");
      if (bodyComponent != null) {
         LibraryHTMLComponent stc = (LibraryHTMLComponent) bodyComponent;
         componentText = stc.getHTML();
         componentText = componentText.replaceAll("DOCUMENTNAME", doc.getTitle());
         componentText = componentText.replaceAll("DOCUMENTURL", Utils.getPreviewURL(doc));
      }

      if (componentText.isEmpty()) {
         sb.append("The model policy " + doc.getTitle() + " has been rejected.");
         sb.append("<br><a href='" + Utils.getPreviewURL(doc) + "'>" + doc.getTitle() + "</a><br>");
      }
      else {
         sb.append(componentText);
      }

      String body = sb.toString();

      
      return body;

   }
   String getEmailSubject(Document doc) {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
   // try to get the component
      String subject = "";
      StringBuilder sb = new StringBuilder();
      LibraryComponent subjectComponent = Utils.getLibraryComponentByName(Utils.getSystemWorkspace(), WCMUtils.p_rejectedEmailSubjectCmpnt,
         "PruPolicyDesign");
      if (subjectComponent != null) {
         LibraryShortTextComponent stc = (LibraryShortTextComponent) subjectComponent;
         subject = stc.getText();
         subject = subject.replaceAll("DOCUMENTNAME", doc.getTitle());
      }

      // retrieve from WCM      
      if (subject.isEmpty()) {
         sb.append("The policy " + doc.getTitle() + " has been rejected.");
      }
      else {
         sb.append(subject);
      }
      String body = sb.toString();

      return body;

   }

   
}

