/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.wf.actions;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.WebContentCustomWorkflowService;
import com.ibm.workplace.wcm.api.exceptions.PropertyRetrievalException;
public class ReviewExpiringEmailAction extends ReviewApproveEmailAction {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(ReviewExpiringEmailAction.class.getName());
   
   
   public ReviewExpiringEmailAction(WebContentCustomWorkflowService customWorkflowService) {
      super(customWorkflowService);
   }
   /**
    * for 
    * @see com.prudential.wf.actions.ReviewApproveEmailAction#shouldSend(com.ibm.workplace.wcm.api.Document)
    */
   boolean shouldSend(Document doc) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      
      if (isDebug) {
         s_log.entering("ReviewExpiringEmailAction", "shouldSend "+doc.getName());
      }
      
      Content theContent = (Content)doc;
      boolean shouldSend = false;
      // get the gendateone.  If it's populated and after now, don't send
      Date genDateOne;
      try {
         genDateOne = theContent.getGeneralDateOne();
         if(genDateOne == null || genDateOne.after(new Date())) {
            shouldSend = true;
         }
      }
      catch (PropertyRetrievalException e) {
         // TODO Auto-generated catch block
         if (s_log.isLoggable(Level.FINEST))
         {
            s_log.log(Level.FINEST, "", e);
         }
      }
      
      if (isDebug) {
         s_log.exiting("ReviewExpiringEmailAction", "shouldSend: "+shouldSend);
      }
      
      return shouldSend;
   }
}

