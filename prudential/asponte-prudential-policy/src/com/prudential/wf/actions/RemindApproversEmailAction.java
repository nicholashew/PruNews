package com.prudential.wf.actions;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;

import com.aptrix.pluto.event.AddedEvent;
import com.ibm.portal.um.Group;
import com.ibm.portal.um.User;
import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.ContentComponent;
import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.DocumentId;
import com.ibm.workplace.wcm.api.DocumentIdIterator;
import com.ibm.workplace.wcm.api.DocumentLibrary;
import com.ibm.workplace.wcm.api.TextComponent;
import com.ibm.workplace.wcm.api.UserSelectionComponent;
import com.ibm.workplace.wcm.api.WebContentCustomWorkflowService;
import com.ibm.workplace.wcm.api.WorkflowedDocument;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowActionResult;
import com.ibm.workplace.wcm.api.custom.Directive;
import com.ibm.workplace.wcm.api.custom.Directives;
import com.ibm.workplace.wcm.api.exceptions.PropertyRetrievalException;
import com.prudential.utils.Utils;
import com.prudential.wcm.WCMUtils;
import com.prudential.wcm.wf.BaseCustomWorkflowAction;
import com.ibm.websphere.scheduler.BeanTaskInfo;
import com.ibm.websphere.scheduler.Scheduler;
import com.ibm.websphere.scheduler.TaskHandlerHome;
import com.ibm.websphere.scheduler.TaskStatus;

/**
 * An action to retrieve approvers from parent site areas as well as the content itself
 */
public class RemindApproversEmailAction extends BaseCustomWorkflowAction {
   private static Logger s_log = Logger.getLogger(RemindApproversEmailAction.class.getName());
   private String s_componentName = "Approve Ready Reminder Delay";

   public RemindApproversEmailAction(WebContentCustomWorkflowService customWorkflowService) {
      super(customWorkflowService);
   }
   @Override
   public CustomWorkflowActionResult execute(Document doc) {

      boolean isDebug = s_log.isLoggable(Level.FINEST);
      
      if (isDebug) {
         s_log.entering("RemindApproversEmailAction", "execute");
      }
      
      Directive directive = Directives.CONTINUE;
      String actionMessage = this.getClass().getName()+" starting reminder email";      
      Workspace ws = doc.getSourceWorkspace();
      try {         
         // get the approvers
         ArrayList approverEmails = getRecipients(doc);
         Timer timer = new Timer();
         Content theContent = (Content)doc;
         Calendar calendar = Calendar.getInstance();
         // get the default days.  If its empty, set to 3 days.
         int delayDays = 3;
         if(theContent.hasComponent(s_componentName)) {
            TextComponent tx = (TextComponent)theContent.getComponentByReference(s_componentName);
            try {
               int tempDays = Integer.parseInt(tx.getText());
               delayDays = tempDays;
            } 
            catch (Exception e) {
               if (isDebug) {
                  s_log.log(Level.FINEST, "Exception getting date");
                  e.printStackTrace();
               }
            }
           
         }
         if (isDebug) {
            s_log.log(Level.FINEST, "setting delay "+delayDays);
         }
         calendar.add(Calendar.DATE, delayDays);
         Date theDate = calendar.getTime();
         
         // now get the scheduler
         
         
      }
      catch (Exception e) {
         // TODO: error handling
         return createResult(Directives.ROLLBACK_DOCUMENT, "Rollback_Document");
      }
      finally {
         
      }
      
      if (isDebug) {
         s_log.exiting("RemindApproversEmailAction", "execute");
      }
      

      // All went well, return normally
      return createResult(directive, actionMessage);
   }
   
   /**
   *
   * in both cases it's the content approvers. Any subclass should override if necessary.
   */
  ArrayList getRecipients(Document doc) {
     // TODO Auto-generated method stub
     boolean isDebug = s_log.isLoggable(Level.FINEST);
     if (isDebug) {
        s_log.entering("RemindApproversEmailAction", "getRecipients for doc "+doc.getName());
     }
     
     ArrayList recipientList = new ArrayList();
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
                    recipientList.addAll(Utils.getEmailsUser(theUser));
                 } 
                 else {
                    if (isDebug) {
                       s_log.log(Level.FINEST, "theUser was null, try group");
                    }
                    Group theGroup = Utils.getGroupByDistinguishedName(dn);
                    if(theGroup != null) {
                       recipientList.addAll(Utils.getEmailsGroup(theGroup));
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
        s_log.exiting("RemindApproversEmailAction", "getRecipients");
     }
     
     return recipientList;

  }

}
