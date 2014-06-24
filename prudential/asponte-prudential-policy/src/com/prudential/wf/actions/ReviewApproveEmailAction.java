package com.prudential.wf.actions;

import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.ArrayList;
import java.util.Date;
import com.ibm.portal.um.*;

import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.DocumentId;
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
      sb.append("A document "+doc.getName()+" is awaiting your approval.");
      sb.append("<br><a href='"+Utils.getPreviewURL(doc)+"'>"+doc.getName()+"</a><br>");
      // now check for the field
      if(doc instanceof Content) {
         // get the parent sa
         try {
            Content theContent = (Content)doc;
            DocumentId parentID = theContent.getDirectParent();
            Workspace ws = doc.getSourceWorkspace();
            SiteArea parent = (SiteArea)ws.getById(parentID);
            if(parent.hasComponent(p_messageComponentName)) {
               TextComponent tc = (TextComponent)parent.getComponentByReference(p_messageComponentName);
               sb.append(tc.getText());
            }
         } catch (Exception e) {
            if (isDebug) {
               s_log.log(Level.FINEST, "Exception "+e.getMessage());
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
      String subject = "Item "+doc.getName()+" is awaiting your review";
      
      return subject;

   }

   @Override
   String getDelayComponentName() {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      
      return s_delayCmpntName;
      
   }     
}
