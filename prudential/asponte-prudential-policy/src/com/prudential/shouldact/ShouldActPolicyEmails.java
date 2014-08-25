/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.shouldact;
import java.util.logging.Level;
import java.util.logging.Logger;
public class ShouldActPolicyEmails implements ShouldActInterface {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(ShouldActPolicyEmails.class.getName());
   private static String resourceKey = "wcm.authoring.active";

   @Override
   public boolean shouldAct() {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      return shouldAct(null);

   }

   @Override
   public boolean shouldAct(Object p_key) {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      return shouldAct(null,null);

   }

   @Override
   /**
    * in this implementation, check for singleton value to true.  If it's not set or it's false
    * check resource environment entry
    * @see com.prudential.shouldact.ShouldActInterface#shouldAct(java.lang.Object, java.lang.Object)
    */
   public boolean shouldAct(Object p_key, Object p_param) {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("ShouldActPolicyEmails", "shouldAct");
      }
      
      boolean shouldAct;
      shouldAct = getDynamicIsActive();
      if(!shouldAct) {
         shouldAct = getResourceIsActive();
      }
      
      if (isDebug) {
         s_log.exiting("ShouldActPolicyEmails", "shouldAct returning "+shouldAct);
      }
      
      return shouldAct;

   }
   
   /**
    * 
    * getDynamicIsActive helper method to get boolean from the singleton
    * @return
    */
   private boolean getDynamicIsActive()
   {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("ShouldActPolicyEmails", "getDynamicIsActive");
      }
      
      boolean isActive = false;
      DynamicShouldEmail dse = DynamicShouldEmail.getInstance();
      isActive = dse.isShouldSendMail();
      
      if (isDebug) {
         s_log.exiting("ShouldActPolicyEmails", "getDynamicIsActive returning "+isActive);
      }
      
      return isActive;
   }
   
   private boolean getResourceIsActive() {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      boolean isActive = false;
      if (isDebug) {
         s_log.entering("ShouldActPolicyEmails", "getResourceIsActive");
      }
      
      if (isDebug) {
         s_log.exiting("ShouldActPolicyEmails", "getResourceIsActive returning "+isActive);
      }
      
      return isActive;
   }
}

