package com.prudential.wcm.wf;

import java.util.logging.Logger;

import java.util.Date;

import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.WebContentCustomWorkflowService;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowActionResult;
import com.ibm.workplace.wcm.api.custom.Directive;
import com.ibm.workplace.wcm.api.custom.DirectiveParams;

public abstract class BaseCustomWorkflowAction implements com.ibm.workplace.wcm.api.custom.CustomWorkflowAction {
	private static Logger log = Logger.getLogger(BaseCustomWorkflowAction.class.getName());
	WebContentCustomWorkflowService customWorkflowService = null;

	public BaseCustomWorkflowAction() {
	}

	public BaseCustomWorkflowAction(
			WebContentCustomWorkflowService customWorkflowService) {
		super();
		this.customWorkflowService = customWorkflowService;
	}

	public void setCustomWorkflowService(
			WebContentCustomWorkflowService customWorkflowService) {
		this.customWorkflowService = customWorkflowService;
	}

	public Date getExecuteDate(Document doc) {
		return new Date();
	}

	protected final CustomWorkflowActionResult createResult(Directive directive, String message) {
		log.entering("BaseCustomWorkflowAction", "createResult");
		return customWorkflowService.createResult(directive, message);
	}
	
	protected final CustomWorkflowActionResult createResult(Directive directive, String message, DirectiveParams dParams) {
       log.entering("BaseCustomWorkflowAction", "createResult");       
       return customWorkflowService.createResult(directive, message,dParams);
   }
}
