/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.wf.actions;

import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;

import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.HistoryLogIterator;
import com.ibm.workplace.wcm.api.ShortTextComponent;
import com.ibm.workplace.wcm.api.WebContentCustomWorkflowService;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowAction;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowActionResult;
import com.ibm.workplace.wcm.api.custom.Directive;
import com.ibm.workplace.wcm.api.custom.Directives;
import com.ibm.workplace.wcm.api.custom.RollbackDirectiveParams;
import com.ibm.workplace.wcm.api.exceptions.ComponentNotFoundException;
import com.prudential.wcm.WCMUtils;
import com.prudential.wcm.wf.*;

public class RejectApproveIfCreator extends BaseCustomWorkflowAction {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(RejectApproveIfCreator.class.getName());

   // the name of the field holding the number of days
   private static String s_dayField = "ReviewDateDelay";

   public RejectApproveIfCreator(WebContentCustomWorkflowService customWorkflowService) {
      super(customWorkflowService);
   }

   @Override
   public CustomWorkflowActionResult execute(Document theDoc) {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      Directive directive = Directives.CONTINUE;
      String actionMessage = this.getClass().getName() + " checking if approver is creator";
      RollbackDirectiveParams params = null;
      // get the number of days, add to the publish date, and then just set the gendateone field if its not empty
      if (theDoc instanceof Content) {
         // get the pub date, add the # of days 
         Content theContent = (Content) theDoc;
         
         // get the creatory
         // set to use full dn first
         Workspace ws = theDoc.getSourceWorkspace();
         boolean isDN = ws.isDistinguishedNamesUsed();
         ws.useDistinguishedNames(true);
         String creator = theContent.getCreator();
         ws.useDistinguishedNames(isDN);
         // get the last mod
         String lastModifier = theContent.getLastModifier();
         // if the creator matches last mod, means that the creator moved theh content to this stage so reject
         if(creator.equalsIgnoreCase(lastModifier)) {
            if (isDebug) {
               s_log.log(Level.FINEST, "Creator matches last mod, reject");
               s_log.log(Level.FINEST, "Creator = "+creator);
               s_log.log(Level.FINEST, "lastModifier = "+lastModifier);
            }
            directive = Directives.ROLLBACK_DOCUMENT;      
            params = (RollbackDirectiveParams) Directives.ROLLBACK_DOCUMENT.createDirectiveParams();
            params.setCustomErrorMsg("Creator cannot approve");
            actionMessage = theDoc.getName()+" rejected because creator cannot approve";
         }
         else {
            if (isDebug) {
               s_log.log(Level.FINEST, "Creator doesn't match last mod, allow");
               s_log.log(Level.FINEST, "Creator = "+creator);
               s_log.log(Level.FINEST, "lastModifier = "+lastModifier);
            }
         }
      
      }
      else {
         if (isDebug) {
            s_log.log(Level.FINEST, "Document wasn't content, do not do anything");
         }
      }

      CustomWorkflowActionResult result = createResult(directive, actionMessage);
      if(params != null) {
         result = createResult(directive, actionMessage,params);
      }
      return result;

   }

   @Override
   public Date getExecuteDate(Document p_arg0) {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      return new Date();

   }
}
