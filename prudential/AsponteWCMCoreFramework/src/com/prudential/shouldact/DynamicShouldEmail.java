/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.shouldact;
import java.util.logging.Level;
import java.util.logging.Logger;
public class DynamicShouldEmail {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(DynamicShouldEmail.class.getName());
   
   private static DynamicShouldEmail instance = null;
   
   private boolean shouldSendMail = false;
   
   public boolean isShouldSendMail() {
      return shouldSendMail;
   }

   public void setShouldSendMail(boolean p_shouldSendMail) {
      shouldSendMail = p_shouldSendMail;
   }

   protected DynamicShouldEmail() {
      // Exists only to defeat instantiation.
   }
   
   public static DynamicShouldEmail getInstance() {
      if(instance == null) {
         instance = new DynamicShouldEmail();
      }
      return instance;
   }
   
   
}

