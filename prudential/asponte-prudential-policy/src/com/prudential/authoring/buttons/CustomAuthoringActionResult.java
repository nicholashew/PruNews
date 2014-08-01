/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.authoring.buttons;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.portal.Localized;
import com.ibm.workplace.wcm.api.extensions.authoring.ActionResult;
import com.ibm.workplace.wcm.api.extensions.authoring.directive.AuthoringDirective;
import com.ibm.workplace.wcm.api.extensions.authoring.directive.AuthoringDirectiveType;
public class CustomAuthoringActionResult implements ActionResult {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(CustomAuthoringActionResult.class.getName());

   private AuthoringDirective directiveResult;
   private Localized p_errorMessage = null;  
   private Localized p_successMessage = null;
   private Localized p_warningMessage = null;
   
   public CustomAuthoringActionResult(AuthoringDirective theDirective) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("CustomAuthoringActionResult", "CustomAuthoringActionResult "+theDirective);
      }
      
      directiveResult = theDirective;
   }
   @Override
   public AuthoringDirective directive() {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      return directiveResult;

   }

   @Override
   public Localized errorMessage() {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      return p_errorMessage;

   }

   @Override
   public Localized successMessage() {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      return p_successMessage;

   }

   @Override
   public Localized warningMessage() {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      return p_warningMessage;
   }
   
   public void setErrorMessage(Localized p_p_errorMessage) {
      p_errorMessage = p_p_errorMessage;
   }
   public void setSuccessMessage(Localized p_p_successMessage) {
      p_successMessage = p_p_successMessage;
   }
   public void setWarningMessage(Localized p_p_warningMessage) {
      p_warningMessage = p_p_warningMessage;
   }

}

