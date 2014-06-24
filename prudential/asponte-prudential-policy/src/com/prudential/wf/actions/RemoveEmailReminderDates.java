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
      
       Workspace ws = Utils.getSystemWorkspace();
       DocumentLibrary currentLib = ws.getCurrentDocumentLibrary();
       try {
          
          ws.login();
          String uuid = doc.getId().getId();
          
          // find the library date component by that name
          ws.setCurrentDocumentLibrary(ws.getDocumentLibrary("PruPolicyDesign"));
          DocumentIdIterator ldcIt = ws.findByName(DocumentTypes.LibraryDateComponent, uuid);
          while(ldcIt.hasNext()) {
             DocumentId tempId = (DocumentId)ldcIt.next();             
             try {
                ws.delete(tempId);
             } catch (Exception e) {
                if (isDebug) {
                  s_log.log(Level.FINEST, "Exception occured deleting "+e.getMessage());
                  e.printStackTrace();
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
       finally {
          if(ws != null) {
             ws.setCurrentDocumentLibrary(currentLib);
             ws.logout();
          }
       }
       return result;
    } 
    
}
