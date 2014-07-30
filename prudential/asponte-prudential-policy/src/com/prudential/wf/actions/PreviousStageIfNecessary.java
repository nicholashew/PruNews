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
import com.ibm.workplace.wcm.api.DocumentId;
import com.ibm.workplace.wcm.api.HistoryLogIterator;
import com.ibm.workplace.wcm.api.ShortTextComponent;
import com.ibm.workplace.wcm.api.WebContentCustomWorkflowService;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowAction;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowActionResult;
import com.ibm.workplace.wcm.api.custom.Directive;
import com.ibm.workplace.wcm.api.custom.Directives;
import com.ibm.workplace.wcm.api.custom.RollbackDirectiveParams;
import com.ibm.workplace.wcm.api.exceptions.AuthorizationException;
import com.ibm.workplace.wcm.api.exceptions.ComponentNotFoundException;
import com.ibm.workplace.wcm.api.exceptions.DocumentIdCreationException;
import com.ibm.workplace.wcm.api.exceptions.PropertyRetrievalException;
import com.prudential.utils.Utils;
import com.prudential.wcm.WCMUtils;
import com.prudential.wcm.wf.*;

public class PreviousStageIfNecessary extends BaseCustomWorkflowAction {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(PreviousStageIfNecessary.class.getName());

   // the name of the field holding the number of days
   private static String s_dayField = "ReviewDateDelay";

   public PreviousStageIfNecessary(WebContentCustomWorkflowService customWorkflowService) {
      super(customWorkflowService);
   }

   @Override
   public CustomWorkflowActionResult execute(Document theDoc) {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      Directive directive = Directives.CONTINUE;
      String actionMessage = this.getClass().getName() + " checking if Previous Stage Necessary";
      RollbackDirectiveParams params = null;
      // get the number of days, add to the publish date, and then just set the gendateone field if its not empty
      // if its not the pa workflow, don't reject

      if (theDoc instanceof Content) {
         // get the pub date, add the # of days 
         Content theContent = (Content) theDoc;
         DocumentId wfid = null;
         Workspace ws = theDoc.getSourceWorkspace();
         //Directives.PREVIOUS_WORKFLOW_STAGE
         if(theContent.hasComponent(Utils.p_prevStageCmpnt)) {
            // remove the component and send back to the previous stage.
            try {
               if (isDebug) {
                  s_log.log(Level.FINEST, "The content has the "+Utils.p_prevStageCmpnt+", send to previous stage");
               }               
               theContent.removeComponent(Utils.p_prevStageCmpnt);
               actionMessage = this.getClass().getName() + " Previous Stage Necessary";
               directive = Directives.PREVIOUS_WORKFLOW_STAGE;
            }
            catch (ComponentNotFoundException e) {
               // TODO Auto-generated catch block
               if (s_log.isLoggable(Level.FINEST))
               {
                  s_log.log(Level.FINEST, "", e);
               }
            }            
         }
      }
      else {
         if (isDebug) {
            s_log.log(Level.FINEST, "Document wasn't content, do not do anything");
         }
      }

      CustomWorkflowActionResult result = createResult(directive, actionMessage);
      if (params != null) {
         result = createResult(directive, actionMessage, params);
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
