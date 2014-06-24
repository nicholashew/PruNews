/********************************************************************/
/* Asponte
 /* cmknight
 /********************************************************************/

package com.prudential.tasks;

import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.workplace.wcm.api.Repository;
import com.ibm.workplace.wcm.api.VirtualPortalContext;
import com.ibm.workplace.wcm.api.WCM_API;
import com.ibm.workplace.wcm.api.exceptions.VirtualPortalNotFoundException;
import com.ibm.workplace.wcm.api.exceptions.WCMException;
import com.prudential.utils.Utils;
import com.prudential.vpactions.ProcessNewNewsContentScopedAction;

public class LinkNewsToNewsletters extends TimerTask {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(LinkNewsToNewsletters.class.getName());
   
   /** the content that needs to be moved **/
   private String contentuuid = "";
      
   public LinkNewsToNewsletters(String p_contentuuid) {
      
      contentuuid = p_contentuuid;
   }

   @Override
   public void run() {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);

      if (isDebug) {
         s_log.entering("LinkNewsToNewsletters", "run");
      }           
      
      
      // this is just a wrapper to do the 
      Repository repo = WCM_API.getRepository();
      try {
         VirtualPortalContext vctx = repo.generateVPContextFromContextPath(Utils.getVPName());
         ProcessNewNewsContentScopedAction vpA = new ProcessNewNewsContentScopedAction(contentuuid);
         if (isDebug) {
            s_log.log(Level.FINEST, "about to execute "+vpA);
         }
         repo.executeInVP(vctx, vpA);
         if (isDebug) {
            s_log.log(Level.FINEST, "done executing "+vpA);
         }
         //content = vpA.getReturnedValue();
         
     } catch (VirtualPortalNotFoundException e) {
        e.printStackTrace();
     } catch (WCMException e) {
        e.printStackTrace();
     }

      if (isDebug) {
         s_log.exiting("LinkNewsToNewsletters", "run");
      }

   }
}
