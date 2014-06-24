package com.prudential.wcm.workflow.actions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.Project;
import com.ibm.workplace.wcm.api.WebContentCustomWorkflowService;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowAction;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowActionResult;
import com.ibm.workplace.wcm.api.custom.Directives;
import com.ibm.workplace.wcm.api.exceptions.WCMException;

/**
 * This class implements the {@link com.ibm.worksplace.wcm.api.custom.CustomWorkflowAction} interface
 * to provide a custom workflow action that will update the content's effective date (publish date) to the current date.
 * <p>
 * Note that the methods that take a {@link java.util.Locale} ignore the Locale and return only English 
 * titles and descriptions.  Translating the action and action factory titles and descriptions is really 
 * only necessary if the IT staff building the workflow assets require this level of translation.  Generally 
 * this is not the case and translation and the accompanying logic is overkill.  However, if desired, it is 
 * a fairly simple exercise to add this behavior.
 * </p>
 * 
 * @see com.ibm.workplace.wcm.api.custom.CustomWorkflowAction
 * @see CustomWorkflowActionFactoryImpl
 * @author dewittsc@asponte.com
 */
public class UpdatePublishDateAction implements CustomWorkflowAction {
	/** Logger class name */
	private static final String CLASS_NAME=UpdatePublishDateAction.class.getName();
	/** Logger object */
	private static final Logger LOG=LogManager.getLogManager().getLogger(CLASS_NAME);
	/** Version */
	private static final String VERSION="2.0";
	
	/** Custom workflow action name */
	static final String NAME="UpdatePublishDateAction";
	/** Custom workflow action english title */
	static final String TITLE="Update Publish Date Action";
	/** Custom workflow action english description */
	static final String DESCRIPTION="This action updates the publish date of the current content item.";
	static WebContentCustomWorkflowService customWorkflowService;
	
	/* 
	 * The static init block performs the JNDI lookup for the {@link com.ibm.workplace.wcm.api.WebContentCustomWorkflowService}
	 * that is used to create the {@link com.ibm.workplace.wcm.api.custom.CustomWorkflowActionResult} instances.  We can rather
	 * safely ignore any exceptions here b/c if one is thrown, then something much larger is wrong with the portal environment.
	 */
	static{
		 try {
		    // Construct an initial Context
		    InitialContext ctx = new InitialContext();
		    // Retrieve WebContentCustomWorkflowService using JNDI name
		    customWorkflowService = (WebContentCustomWorkflowService) ctx.lookup("portal:service/wcm/WebContentCustomWorkflowService");
		 }
		 catch (NamingException ignore) {
			 // If this happens, something is really wrong with portal in general
			 ignore.printStackTrace();
		 }
	}

	/**
	 * Default constructor.
	 */
	public UpdatePublishDateAction() {
	}

	
	/**
	 * Get the Date that this action should execute. This method is always called prior to running the execute method.
	 * In this case, we always wat the action to execute immediately so we return {@link 
	 * com.ibm.workplace.wcm.api.custom.CustomWorkflowAction#DATE_EXECUTE_NOW}.
	 * 
	 * @param doc Target document. Custom code must not modify the document in this method.
	 * @return Execute date. If date is in the past, the action will be executed immediately. Use the DATE_EXECUTE_NOW 
	 * constant to execute immediately. If the date is in the future, the action will be scheduled for this date. 
	 * The returned execute date must be the same when run on any server where the action is syndicated. 
	 * If the execute date is different, the scheduled action will run at different times on different servers.
	 * @see com.ibm.workplace.wcm.api.custom.CustomWorkflowAction#getExecuteDate(com.ibm.workplace.wcm.api.Document)
	 * @see com.ibm.workplace.wcm.api.custom.CustomWorkflowAction#DATE_EXECUTE_NOW
	 */
	@Override
	public Date getExecuteDate(Document doc) {
		return DATE_EXECUTE_NOW;
	}

	/**
	 * Execute the action against the supplied document. Changes to the document will be saved if the result does not indicate a failure. 
	 * Changes to the document will be ignored if the result indicates a failure.
	 * <p>
	 * This implementation of the custom workflow action will update the effective
	 * date (publish date) of the content to be the publish date of the associated project, if the content is in a project
	 * and the project is using the DATE publish option, or to be the current date.
	 * </p>
	 * 
	 * @param doc Target document. Custom code must not save or delete this document inside the execute method. Custom code must not call 
	 * any workflow methods against this document inside the execute method. Use the approriate return code to trigger a workflow action.
	 * @return Result providing access to the outcome of the action and a message. Special result codes can be used to trigger workflow 
	 * actions against the target document. Returning null will be treated as a failure result.
	 * @throws java.lang.Throwable Any throwable that is thrown by this method will be treated as a failure result.
	 * @see com.ibm.workplace.wcm.api.custom.CustomWorkflowAction#execute(com.ibm.workplace.wcm.api.Document)
	 */
	@Override
	public CustomWorkflowActionResult execute(Document doc) {
		final String METHOD_NAME="execute";
		boolean isTraceEnabled=LOG.isLoggable(Level.FINER);
		boolean isDebugEnabled=LOG.isLoggable(Level.FINEST);
		boolean isErrorEnabled=LOG.isLoggable(Level.SEVERE);
		if(isTraceEnabled){LOG.entering(CLASS_NAME, METHOD_NAME, new Object[]{doc});}		
		if(isDebugEnabled){LOG.finest(METHOD_NAME+" UpdatePublishDateAction version "+VERSION);}
		/* 
		 * We assume the item is a content item - to use this action on workflowed site areas or components wouldn't
		 * make much sense.
		 */
		Content content=(Content)doc;
		/* Grab the workspace */
		try{
			if(isDebugEnabled){LOG.finest(METHOD_NAME+" Updating effective date to be the project publish date...");}
			Project proj=content.getProject();
			if(proj!=null&&proj.getPublishOption()==Project.PublishOptions.DATE){
				Date date=new Date(proj.getPublishDate());
				if(isDebugEnabled){
					LOG.finest(METHOD_NAME+" Item is in project and project is using DATE publish option...");
					LOG.finest(METHOD_NAME+" Setting effective date to project publish date: "+new SimpleDateFormat().format(date)+"...");
				}
				content.setEffectiveDate(date);
			}else{
				if(isDebugEnabled){LOG.finest(METHOD_NAME+" Item is not in project or project is not using DATE publish option, setting date to now...");}
				content.setEffectiveDate(new Date());
			}
		}catch(WCMException e){
			if(isErrorEnabled){LOG.log(Level.SEVERE,"An error occurred while updating the published date for content item "+content.getId()+"!  The content item will not be updated.",e);}
			if(isTraceEnabled){LOG.exiting(CLASS_NAME,METHOD_NAME);}
			return null;
		}
		if(isDebugEnabled){LOG.finest(METHOD_NAME+" Content item "+content.getId()+" successfully updated publish date!");}
		if(isTraceEnabled){LOG.exiting(CLASS_NAME,METHOD_NAME,Directives.CONTINUE);}
		return customWorkflowService.createResult(Directives.CONTINUE, "Continue");
	}
}
