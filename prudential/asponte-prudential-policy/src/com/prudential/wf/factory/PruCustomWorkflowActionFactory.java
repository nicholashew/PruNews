package com.prudential.wf.factory;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.naming.NamingException;

import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.WebContentCustomWorkflowService;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowAction;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowActionFactory;
import com.prudential.wcm.WCMUtils;
import com.prudential.wf.actions.ApplyApproversAction;
import com.prudential.wf.actions.ApplyReviewersAction;
import com.prudential.wf.actions.ApplySiteAreaManagers;
import com.prudential.wf.actions.ApproveDelayPopulatePreviousStage;
import com.prudential.wf.actions.CreateMyActionsMessage;
import com.prudential.wf.actions.GenDateOnePopulatePreviousStage;
import com.prudential.wf.actions.NotifyAllPolicyAdmins;
import com.prudential.wf.actions.PopulateFutureReviewDate;
import com.prudential.wf.actions.PopulateLastRevisedDate;
import com.prudential.wf.actions.PreviousStageIfNecessary;
import com.prudential.wf.actions.RejectApproveIfCreator;
import com.prudential.wf.actions.RemoveEmailReminderDates;
import com.prudential.wf.actions.RemoveMyActionsMessage;
import com.prudential.wf.actions.ReviewApproveEmailAction;
import com.prudential.wf.actions.NotifyReferences;
import com.prudential.wf.actions.ReviewDelayPopulatePreviousStage;
import com.prudential.wf.actions.ReviewExpiringEmailAction;
import com.prudential.wf.actions.ScheduleReviewEmailAction;
import com.prudential.wf.actions.CreateDraftPolicy;


public class PruCustomWorkflowActionFactory implements
		CustomWorkflowActionFactory {
	private static Logger log = Logger.getLogger(PruCustomWorkflowActionFactory.class.getName());
	private Map<String, CustomWorkflowAction> actions = new HashMap<String, CustomWorkflowAction>();
	

	public PruCustomWorkflowActionFactory() {
		WebContentCustomWorkflowService customWorkflowService;
		try {
			customWorkflowService = WCMUtils.getWebContentCustomWorkflowService();
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
		actions.put("ReviewApproveEmailAction", new ReviewApproveEmailAction(customWorkflowService));
		actions.put("ApplyApproversAction", new ApplyApproversAction(customWorkflowService));
		actions.put("ApplyReviewersAction", new ApplyReviewersAction(customWorkflowService));
		actions.put("ScheduleReviewEmailAction", new ScheduleReviewEmailAction(customWorkflowService));
		actions.put("PopulateFutureReviewDate", new PopulateFutureReviewDate(customWorkflowService));
		actions.put("NotifyReferences", new NotifyReferences());
		actions.put("CreateDraftPolicy", new CreateDraftPolicy());
		actions.put("RemoveEmailReminderDates", new RemoveEmailReminderDates());
		actions.put("RejectApproveIfCreator", new RejectApproveIfCreator(customWorkflowService));
		actions.put("NotifyAllPolicyAdmins", new NotifyAllPolicyAdmins(customWorkflowService));
		actions.put("CreateMyActionsMessage", new CreateMyActionsMessage());
		actions.put("RemoveMyActionsMessage", new RemoveMyActionsMessage());
		actions.put("ApplySiteAreaManagers", new ApplySiteAreaManagers());
		actions.put("GenDateOnePopulatePreviousStage", new GenDateOnePopulatePreviousStage(customWorkflowService));
		actions.put("ReviewDelayPopulatePreviousStage", new ReviewDelayPopulatePreviousStage(customWorkflowService));
		actions.put("ApproveDelayPopulatePreviousStage", new ApproveDelayPopulatePreviousStage(customWorkflowService));
		actions.put("PopulateLastRevisedDate", new PopulateLastRevisedDate(customWorkflowService));
		actions.put("PreviousStageIfNecessary", new PreviousStageIfNecessary(customWorkflowService));
		actions.put("ReviewExpiringEmailAction", new ReviewExpiringEmailAction(customWorkflowService));
		//ReviewDelayPopulatePreviousStage
		//ApproveDelayPopulatePreviousStage
	}

	@Override
	public CustomWorkflowAction getAction(String name, Document document) {
		log.entering(this.getClass().getSimpleName(), "getAction");
		return actions.get(name);
	}

	@Override
	public String getActionDescription(Locale locale, String name) {
		log.entering(this.getClass().getSimpleName(), "getActionDescription");
		return name;
	}

	@Override
	public String[] getActionNames() {
		log.entering(this.getClass().getSimpleName(), "getActionNames");
		return actions.keySet().toArray(new String[actions.size()]);
	}

	@Override
	public String getActionTitle(Locale locale, String name) {
		log.entering(this.getClass().getSimpleName(), "getActionTitle");
		return name;
	}

	@Override
	public String getName() {
		log.entering(this.getClass().getSimpleName(), "getName");
		return "PruPolicyFramework";
	}

	@Override
	public String getTitle(Locale arg0) {
		log.entering(this.getClass().getSimpleName(), "getTitle");
		return this.getName();
	}

}
