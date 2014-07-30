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

public class ApproveDelayPopulatePreviousStage extends AbstractPopulatePreviousStageNecessary {

   public ApproveDelayPopulatePreviousStage(WebContentCustomWorkflowService p_customWorkflowService) {
      // TODO Auto-generated constructor stub
      super(p_customWorkflowService);

   }

   private static String s_delayCmpntName = "ApproveReadyReminderDelay";

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(ApproveDelayPopulatePreviousStage.class.getName());

   @Override
   String getDelayComponentName() {
      return s_delayCmpntName;
   }
}
