/**
 * company: asponte
 * author: pete raleigh
 * description: creates a draft policy document and moves to review
 */

package com.prudential.wf.actions;

import javax.naming.InitialContext;
import com.ibm.workplace.wcm.api.*;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowAction;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowActionResult;
import com.ibm.workplace.wcm.api.custom.Directive;
import com.ibm.workplace.wcm.api.custom.Directives;
import com.ibm.workplace.wcm.api.custom.RollbackDirectiveParams;
import com.ibm.workplace.wcm.api.exceptions.*;
import com.prudential.utils.Utils;
import com.prudential.vp.RetrieveReminderTaskAction;
import com.prudential.wcm.WCMUtils;

import java.util.*;
import java.util.logging.*;

import javax.naming.NamingException;

/**
 *
 * @version 1.0
 */
public class RemoveEmailReminderDates implements CustomWorkflowAction {

    private static final Logger s_log = Logger.getLogger(RemoveEmailReminderDates.class.getName());
    
    @Override
    public Date getExecuteDate(Document doc) {
        return DATE_EXECUTE_NOW;
    }
    
    @Override
    public CustomWorkflowActionResult execute(Document doc) {
       boolean isDebug = s_log.isLoggable(Level.FINEST);
       if (isDebug) {
          s_log.entering("RemoveEmailReminderDates", "execute");
       }
       
       Directive directive = Directives.CONTINUE;
       CustomWorkflowActionResult result = null;
       String message = "RemoveEmailReminderDates executing";
       WebContentCustomWorkflowService webContentCustomWorkflowService = null;
             
       try {
          
          if(doc instanceof Content) {
             Content theContent = (Content)doc;
             if(theContent.hasComponent(RetrieveReminderTaskAction.p_lastRunComponent)) {
                theContent.removeComponent(RetrieveReminderTaskAction.p_lastRunComponent) ;
                if (isDebug) {
                  s_log.log(Level.FINEST, "Removed last run component");
               }
             }
          }
          webContentCustomWorkflowService = WCMUtils.getWebContentCustomWorkflowService();
          result = webContentCustomWorkflowService.createResult(directive, message);
          
                  
       }
       catch (Exception e) {
          if (isDebug) {
             e.printStackTrace();
          }
       }    
       return result;
    } 
    
}
