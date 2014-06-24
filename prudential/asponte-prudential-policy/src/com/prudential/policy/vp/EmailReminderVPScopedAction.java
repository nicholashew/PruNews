/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.policy.vp;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.DocumentId;
import com.ibm.workplace.wcm.api.VirtualPortalScopedAction;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.exceptions.WCMException;
import com.prudential.utils.Utils;
public class EmailReminderVPScopedAction implements VirtualPortalScopedAction {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(EmailReminderVPScopedAction.class.getName());
   private static String s_uuid;   
   private Content s_content;
   
   public Content getContent() {
      return s_content;
   }

   public void setContent(Content p_content) {
      s_content = p_content;
   }

   public EmailReminderVPScopedAction(String p_uuid) {
      s_uuid = p_uuid;
   }
   
   public static String getUuid() {
      return s_uuid;
   }

   public static void setUuid(String p_uuid) {
      s_uuid = p_uuid;
   }

 
   @Override
   public void run() throws WCMException {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);

      if (isDebug) {
         s_log.entering("EmailReminderVPScopedAction", "enclosing_method");
      }
      try {
         // get system workspace, login, then process the move
         // try different workspace
         Workspace ws = Utils.getSystemWorkspace();      
         if (ws != null && s_uuid != null) {
            DocumentId contentId = ws.createDocumentId(s_uuid);
            if(contentId == null) {
               throw new Exception("Could not create document id");
            }
            
            Content theContent = (Content)ws.getById(contentId);
            setContent(theContent);
         }
      } catch (Exception e) {
         if (isDebug) {
            s_log.log(Level.FINEST, "Exception "+e.getMessage());
            e.printStackTrace();
         }
      }
      
      if (isDebug) {
         s_log.exiting("EmailReminderVPScopedAction", "run");
      }
      
   }
}

