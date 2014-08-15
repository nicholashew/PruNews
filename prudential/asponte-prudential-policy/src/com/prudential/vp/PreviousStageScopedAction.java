/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.vp;

import java.security.Principal;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.portal.um.Group;
import com.ibm.portal.um.User;
import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.ContentComponent;
import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.DocumentId;
import com.ibm.workplace.wcm.api.UserSelectionComponent;
import com.ibm.workplace.wcm.api.VirtualPortalScopedAction;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.exceptions.AuthorizationException;
import com.ibm.workplace.wcm.api.exceptions.PropertyRetrievalException;
import com.ibm.workplace.wcm.api.exceptions.WCMException;
import com.prudential.utils.Utils;
import com.prudential.wcm.WCMUtils;

public class PreviousStageScopedAction implements VirtualPortalScopedAction {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(PreviousStageScopedAction.class.getName());

   private static Document result = null;

   private static String s_uuid;
   

   public static String getUuid() {
      return s_uuid;
   }

   public static void setUuid(String p_uuid) {
      s_uuid = p_uuid;
   }

   public static Document getResult() {
      return result;
   }

   public static void setResult(Document p_result) {
      result = p_result;
   }

   public PreviousStageScopedAction(String p_uuid) {
      s_uuid = p_uuid;
   }

   @Override
   public void run() throws WCMException {
      // TODO Auto-generated method stub

      PrivilegedExceptionAction runAction = new PrivilegedExceptionAction() {
         @Override
         public Object run() throws Exception {
            boolean isDebug = s_log.isLoggable(Level.FINEST);
           
            try {
               // get system workspace, login, then process the move
               // try different workspace
               Workspace ws = Utils.getSystemWorkspace();
               if (ws != null && s_uuid != null) {
                  DocumentId contentId = ws.createDocumentId(s_uuid);
                  if (contentId == null) {
                     throw new Exception("Could not create document id");
                  }
                  else {
                     if (isDebug) {
                        s_log.log(Level.FINEST, "contentId " + contentId);
                     }
                  }

                  Content theResult = (Content) ws.getById(contentId);
                  // move back
                  // unlock
                  if(ws.isLocked(contentId)) {
                     ws.unlock(contentId);
                  }
                  theResult.previousWorkflowStage(true);
               }
            }
            catch (Exception e) {
               if (isDebug) {
                  s_log.log(Level.FINEST, "Exception " + e.getMessage());
                  e.printStackTrace();
               }
            }
            return null;
         }
      };
      // now run it
      try {
         Utils.getPumaHome().getEnvironment().runUnrestricted(runAction);
      }
      catch (PrivilegedActionException e) {
         // TODO Auto-generated catch block
         if (s_log.isLoggable(Level.FINEST)) {
            s_log.log(Level.FINEST, "", e);
         }
      }

   }
}
