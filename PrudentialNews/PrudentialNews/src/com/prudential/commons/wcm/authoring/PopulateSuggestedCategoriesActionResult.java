/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.commons.wcm.authoring;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.portal.Localized;
import com.ibm.workplace.wcm.api.extensions.authoring.ActionResult;
import com.ibm.workplace.wcm.api.extensions.authoring.directive.AuthoringDirective;

public class PopulateSuggestedCategoriesActionResult implements ActionResult {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(PopulateSuggestedCategoriesActionResult.class.getName());
   private boolean success = false;
   private String title = null;
   private String description = null;
   
   public PopulateSuggestedCategoriesActionResult(boolean success) {
       this.success = success;
   }

   public boolean isSuccess() {
       return success;
   }

   public void setSuccess(boolean success) {
       this.success = success;
   }

   public String getTitle() {
       return title;
   }

   public void setTitle(String title) {
       this.title = title;
   }

   public String getDescription() {
       return description;
   }

   public void setDescription(String description) {
       this.description = description;
   }

   @Override
   public AuthoringDirective directive() {
       // TODO Auto-generated method stub
       return null;
   }

   @Override
   public Localized errorMessage() {
       // TODO Auto-generated method stub
       return null;
   }

   @Override
   public Localized successMessage() {
       // TODO Auto-generated method stub
       return null;
   }

   @Override
   public Localized warningMessage() {
       // TODO Auto-generated method stub
       return null;
   }}

