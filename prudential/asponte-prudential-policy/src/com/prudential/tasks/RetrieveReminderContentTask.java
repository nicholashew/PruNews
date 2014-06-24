/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.tasks;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import com.ibm.workplace.wcm.api.*;
import com.ibm.workplace.wcm.api.exceptions.AuthorizationException;
import com.ibm.workplace.wcm.api.exceptions.DocumentCreationException;
import com.ibm.workplace.wcm.api.exceptions.DocumentRetrievalException;
import com.ibm.workplace.wcm.api.exceptions.DocumentSaveException;
import com.ibm.workplace.wcm.api.exceptions.DuplicateChildException;
import com.ibm.workplace.wcm.api.exceptions.IllegalDocumentTypeException;
import com.ibm.workplace.wcm.api.exceptions.OperationFailedException;
import com.ibm.workplace.wcm.api.exceptions.VirtualPortalNotFoundException;
import com.ibm.workplace.wcm.api.exceptions.WCMException;
import com.ibm.workplace.wcm.api.query.Selectors;
import com.prudential.utils.Utils;
import com.prudential.vp.RetrieveEmailReminderByUuidScopedAction;
import com.prudential.vp.RetrieveReminderTaskAction;

import java.util.logging.Level;
import java.util.logging.Logger;
public class RetrieveReminderContentTask extends TimerTask {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(RetrieveReminderContentTask.class.getName());
    
   @Override
   public void run() {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("RetrieveReminderContentTask", "run");
      }
      // get the vpscoped action and run it
      Repository repo = WCM_API.getRepository();
      try {
         VirtualPortalContext vctx = repo.generateVPContextFromContextPath(Utils.getVPName());
         RetrieveReminderTaskAction vpA = new RetrieveReminderTaskAction();
         repo.executeInVP(vctx, vpA);
      }
      catch (VirtualPortalNotFoundException e) {
         // TODO Auto-generated catch block
         if (s_log.isLoggable(Level.FINEST))
         {
            s_log.log(Level.FINEST, "", e);
         }
      }
      catch (WCMException e) {
         // TODO Auto-generated catch block
         if (s_log.isLoggable(Level.FINEST))
         {
            s_log.log(Level.FINEST, "", e);
         }
      }
      finally {
         
      }
      
      if (isDebug) {
         s_log.exiting("RetrieveReminderContentTask", "run");
      }
      
   }
   
}

