/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.wf.actions;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.workplace.wcm.api.Document;
import com.prudential.utils.Utils;
public class AlertContentRejectedEmailAction extends EmailContentAuthors{

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(AlertContentRejectedEmailAction.class.getName());
   
   /**
    * @see com.prudential.wf.actions.BaseEmailAction#getEmailBody(com.ibm.workplace.wcm.api.Document)
    */
   String getEmailBody(Document p_doc) {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      StringBuilder sb = new StringBuilder();
      String returnString = "";
      
      sb.append("A document " + p_doc.getTitle() + " is awaiting your attention.");
      sb.append("<br><a href='" + Utils.getAuthoringURL(p_doc) + "'>" + p_doc.getTitle() + "</a><br>");
      
      returnString = sb.toString();
      
      return returnString;

   }
   String getEmailSubject(Document p_doc) {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      StringBuilder sb = new StringBuilder();
      String returnString = "";
      sb.append("Item " + p_doc.getTitle() + " is awaiting your attention");

      returnString = sb.toString();

      return returnString;

   }

   
}

