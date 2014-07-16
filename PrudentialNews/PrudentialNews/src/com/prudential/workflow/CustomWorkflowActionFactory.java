/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.workflow;

import java.util.Locale;
import java.util.logging.Logger;

import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowAction;

public class CustomWorkflowActionFactory implements com.ibm.workplace.wcm.api.custom.CustomWorkflowActionFactory
{
   private static String FACTORY_TITLE = "CustomWorkflowActionFactory";
   private static String FACTORY_NAME = "CustomWorkflowActionFactory";
   private static String[] FACTORY_ACTIONNAMES = {"EmailNewsletters","ProcessCreateNewsletterProfile","NewsCreatedAction","PopulateCategories","PopulateExpireDate"};

   /** Logger for the class */
   private static final Logger s_log = Logger.getLogger(CustomWorkflowActionFactory.class.getName());


   /**
    * @see com.ibm.workplace.wcm.api.custom.CustomWorkflowActionFactory#getAction(java.lang.String, com.ibm.workplace.wcm.api.Document)
    */
   @Override
   public CustomWorkflowAction getAction(String p_arg0, Document p_arg1)
   {
      // TODO Auto-generated method stub
      if(p_arg0.equalsIgnoreCase(FACTORY_ACTIONNAMES[0]))
      {
         return new EmailNewsletters();
      }     
      else if(p_arg0.equalsIgnoreCase(FACTORY_ACTIONNAMES[1]))
      {
         return new ProcessCreateNewsletterProfile();
      } 
      else if(p_arg0.equalsIgnoreCase(FACTORY_ACTIONNAMES[2]))
      {
         return new NewsCreatedAction();
      }       
      else if(p_arg0.equalsIgnoreCase(FACTORY_ACTIONNAMES[3]))
      {
         return new PopulateCategories();
      }  
      else if(p_arg0.equalsIgnoreCase(FACTORY_ACTIONNAMES[4]))
      {
         return new PopulateExpireDate();
      } 
      
      return null;
   }

   /**
    * @see com.ibm.workplace.wcm.api.custom.CustomWorkflowActionFactory#getActionDescription(java.util.Locale, java.lang.String)
    */
   @Override
   public String getActionDescription(Locale p_arg0, String p_arg1)
   {
      // TODO Auto-generated method stub
      return FACTORY_ACTIONNAMES[0];
   }

   /**
    * @see com.ibm.workplace.wcm.api.custom.CustomWorkflowActionFactory#getActionNames()
    */
   @Override
   public String[] getActionNames()
   {
      // TODO Auto-generated method stub
      return FACTORY_ACTIONNAMES;
   }

   /**
    * @see com.ibm.workplace.wcm.api.custom.CustomWorkflowActionFactory#getActionTitle(java.util.Locale, java.lang.String)
    */
   @Override
   public String getActionTitle(Locale p_arg0, String p_arg1)
   {
      // TODO Auto-generated method stub
      return p_arg1;
   }

   /**
    * @see com.ibm.workplace.wcm.api.custom.CustomWorkflowActionFactory#getName()
    */
   @Override
   public String getName()
   {
      // TODO Auto-generated method stub
      return FACTORY_NAME;
   }

   /**
    * @see com.ibm.workplace.wcm.api.custom.CustomWorkflowActionFactory#getTitle(java.util.Locale)
    */
   @Override
   public String getTitle(Locale p_arg0)
   {
      // TODO Auto-generated method stub
      return FACTORY_TITLE;
   }
}