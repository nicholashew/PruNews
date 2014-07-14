/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.workflow;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.DocumentId;
import com.ibm.workplace.wcm.api.TextComponent;
import com.ibm.workplace.wcm.api.WebContentCustomWorkflowService;
import com.ibm.workplace.wcm.api.WebContentService;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowAction;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowActionResult;
import com.ibm.workplace.wcm.api.custom.Directive;
import com.ibm.workplace.wcm.api.custom.Directives;
import com.prudential.commons.wcm.authoring.ObjectWrapper;
import com.prudential.utils.Utils;

public class PopulateExpireDate implements CustomWorkflowAction {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(PopulateExpireDate.class.getName());

   @Override
   public CustomWorkflowActionResult execute(Document p_theDoc) {
      // TODO Auto-generated method stub

      boolean isDebug = s_log.isLoggable(Level.FINEST);

      WebContentCustomWorkflowService webContentCustomWorkflowService = null;
      Workspace ws = null;
      CustomWorkflowActionResult result = null;
      Directive directive = Directives.CONTINUE;
      String message = "Successfully populated expire date";
      boolean successful = true;

      try {
         InitialContext ctx = new InitialContext();
         WebContentService webContentService = (WebContentService) ctx.lookup("portal:service/wcm/WebContentService");
         //ws = webContentService.getRepository().getSystemWorkspace();
         //ws = Utils.getSystemWorkspace();
         // Retrieve Custom Workflow Service
         webContentCustomWorkflowService = (WebContentCustomWorkflowService) ctx
            .lookup("portal:service/wcm/WebContentCustomWorkflowService");
         result = webContentCustomWorkflowService.createResult(directive, message);

         // get the suggested cats
         Content theContent = (Content) p_theDoc;
         Date expireDate = theContent.getExpiryDate();
         Date now = new Date();
         if(expireDate != null && expireDate.after(now)) {
            if (isDebug) {
               s_log.log(Level.FINEST, "Expire date already set "+expireDate);
            }
            message = "No need to populate expire date";
         } else {
            // add a year
            int offset = 365;            
            Calendar tempCal = Calendar.getInstance();
            tempCal.setTime(now);
            tempCal.add(Calendar.DATE, offset);
            now = tempCal.getTime();
            theContent.setExpiryDate(now);
            if (isDebug) {
               s_log.log(Level.FINEST, "set expire date to "+now);
            }
         }      

      }
      catch (Exception e) {
         // TODO Auto-generated catch block
         if (s_log.isLoggable(Level.FINEST)) {
            s_log.log(Level.FINEST, "", e);
            e.printStackTrace();
         }
         message = "Exception in PopulateExpireDate";
         directive = Directives.ROLLBACK_DOCUMENT;
         // Create a result object
         result = webContentCustomWorkflowService.createResult(directive, message);
         successful = false;
      }
      finally {
         if (ws != null) {
            ws.logout();
         }
      }

      if (isDebug) {
         s_log.exiting("PopulateExpireDate", "execute returning " + result + " successful " + successful);
      }

      return result;
   }

   @Override
   public Date getExecuteDate(Document p_arg0) {
      // TODO Auto-generated method stub
      return new Date();
   }
}
