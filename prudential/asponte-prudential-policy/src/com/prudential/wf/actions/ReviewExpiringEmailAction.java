/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.wf.actions;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.DocumentId;
import com.ibm.workplace.wcm.api.LibraryComponent;
import com.ibm.workplace.wcm.api.LibraryHTMLComponent;
import com.ibm.workplace.wcm.api.LibraryShortTextComponent;
import com.ibm.workplace.wcm.api.SiteArea;
import com.ibm.workplace.wcm.api.TextComponent;
import com.ibm.workplace.wcm.api.WebContentCustomWorkflowService;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.exceptions.PropertyRetrievalException;
import com.prudential.shouldact.ShouldActPolicyEmails;
import com.prudential.utils.Utils;
import com.prudential.wcm.WCMUtils;

public class ReviewExpiringEmailAction extends ReviewApproveEmailAction {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(ReviewExpiringEmailAction.class.getName());

   private static String p_messageComponentName = "ApproverMessage";

   public ReviewExpiringEmailAction(WebContentCustomWorkflowService customWorkflowService) {
      super(customWorkflowService);
   }

   /**
    * for 
    * @see com.prudential.wf.actions.ReviewApproveEmailAction#shouldSend(com.ibm.workplace.wcm.api.Document)
    */
   boolean shouldSend(Document doc) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);

      if (isDebug) {
         s_log.entering("ReviewExpiringEmailAction", "shouldSend " + doc.getName());
      }

      Content theContent = (Content) doc;
      boolean shouldSend = false;
      ShouldActPolicyEmails shouldAct = new ShouldActPolicyEmails();
      if (shouldAct.shouldAct()) {
         Date genDateOne;
         try {
            genDateOne = theContent.getGeneralDateOne();
            if (genDateOne == null || genDateOne.after(new Date())) {
               shouldSend = true;
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
         s_log.log(Level.FINEST, "not sending as the service is returning false");
      }
      // get the gendateone.  If it's populated and after now, don't send

      if (isDebug) {
         s_log.exiting("ReviewExpiringEmailAction", "shouldSend: " + shouldSend);
      }

      return shouldSend;
   }

   @Override
   String getEmailBody(Document doc) {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);

      if (isDebug) {
         s_log.entering("ReviewExpiringEmailAction", "getEmailBody");
      }

      // retrieve from WCM      
      StringBuilder sb = new StringBuilder();
      String componentText = "";
      // try to get the component
      LibraryComponent bodyComponent = Utils.getLibraryComponentByName(Utils.getSystemWorkspace(), WCMUtils.p_approveRetireEmailTextCmpnt,
         "PruPolicyDesign");
      if (bodyComponent != null) {
         LibraryHTMLComponent stc = (LibraryHTMLComponent) bodyComponent;
         componentText = stc.getHTML();
         componentText = componentText.replaceAll("DOCUMENTNAME", doc.getTitle());
         componentText = componentText.replaceAll("DOCUMENTURL", Utils.getPreviewURL(doc));
      }

      if (componentText.isEmpty()) {
         sb.append("A document " + doc.getTitle() + " is awaiting your approval.");
         sb.append("<br><a href='" + Utils.getPreviewURL(doc) + "'>" + doc.getTitle() + "</a><br>");
         // now check for the field         
      }
      else {
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
         s_log.exiting("ReviewExpiringEmailAction", "getEmailBody returning " + body);
      }

      return body;

   }

   @Override
   String getEmailSubject(Document doc) {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      String subject = "Item " + doc.getTitle() + " is awaiting your approval";

      // try to get the component
      LibraryComponent subjectComponent = Utils.getLibraryComponentByName(Utils.getSystemWorkspace(), WCMUtils.p_approveRetireEmailSubjectCmpnt,
         "PruPolicyDesign");
      if (subjectComponent != null) {
         LibraryShortTextComponent stc = (LibraryShortTextComponent) subjectComponent;
         subject = stc.getText();
         subject = subject.replaceAll("DOCUMENTNAME", doc.getTitle());
      }

      return subject;

   }
}
