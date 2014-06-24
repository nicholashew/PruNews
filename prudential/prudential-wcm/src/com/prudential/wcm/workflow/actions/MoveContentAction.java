package com.prudential.wcm.workflow.actions;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.DocumentId;
import com.ibm.workplace.wcm.api.DocumentIdIterator;
import com.ibm.workplace.wcm.api.MoveOptions;
import com.ibm.workplace.wcm.api.Placement;
import com.ibm.workplace.wcm.api.PlacementLocation;
import com.ibm.workplace.wcm.api.SiteArea;
import com.ibm.workplace.wcm.api.TextComponent;
import com.ibm.workplace.wcm.api.WebContentCustomWorkflowService;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowAction;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowActionResult;
import com.ibm.workplace.wcm.api.custom.Directives;
import com.ibm.workplace.wcm.api.exceptions.WCMException;

/**
 * This class implements the {@link com.ibm.worksplace.wcm.api.custom.CustomWorkflowAction} interface
 * to provide a custom workflow action that will move the subject content item from its current direct
 * parent site area to a site area specified by a path on a short text component on the parent site area.
 * In addition, this action will update the content's effective date (publish date) to the current date.
 * <p>
 * Note that the methods that take a {@link java.util.Locale} ignore the Locale and return only English 
 * titles and descriptions.Translating the action and action factory titles and descriptions is really 
 * only necessary if the IT staff building the workflow assets require this level of translation.  Generally 
 * this is not the case and translation and the accompanying logic is overkill.  However, if desired, it is 
 * a fairly simple exercise to add this behavior.
 * </p>
 * 
 * @see com.ibm.workplace.wcm.api.custom.CustomWorkflowAction
 * @see CustomWorkflowActionFactoryImpl
 * @author dewittsc@asponte.com
 */
public class MoveContentAction implements CustomWorkflowAction {
	/** Logger class name */
	private static final String CLASS_NAME=MoveContentAction.class.getName();
	/** Logger object */
	private static final Logger LOG=LogManager.getLogManager().getLogger(CLASS_NAME);
	/** Version */
	private static final String VERSION="2.0";
	/** 
	 * The target site area element name.  This is the name of the element that on the parent
	 * site area that will contain the target site area.
	 */
	private static final String TARGET_SA_ELEMENT_NAME="WorkflowMoveToSiteArea";

	/** Custom workflow action name */
	static final String NAME="MoveContentAction";
	/** Custom workflow action english title */
	static final String TITLE="Move Content Action";
	/** Custom workflow action english description */
	static final String DESCRIPTION="This action moves content from a source site area to a target site area.";
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
	public MoveContentAction() {
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
	 * This implementation of the custom workflow action will move the current document from it's current direct parent to
	 * the site area specified on the site area element named {@link #TARGET_SA_ELEMENT_NAME}.  It also will update the effective
	 * date (publish date) of the content to be the current date.
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
		if(isDebugEnabled){LOG.finest(METHOD_NAME+" MoveContentAction version "+VERSION);}
		/* 
		 * We assume the item is a content item - to use this action on workflowed site areas or components wouldn't
		 * make much sense.
		 */
		Content content=(Content)doc;
		/* Grab the workspace */
		Workspace wkspc=content.getSourceWorkspace();
		try{
			if(isDebugEnabled){LOG.finest(METHOD_NAME+" Executing move content action for content item "+content.getId()+"...");}
			/* Get the current content's direct parent site area */
			DocumentId pid=content.getDirectParent();
			if(isDebugEnabled){LOG.finest(METHOD_NAME+" Current contents' direct parent item is "+pid+"...");}
			SiteArea currParent=(SiteArea)wkspc.getById(pid,true);
			if(isDebugEnabled){LOG.finest(METHOD_NAME+" Checking parent site area for "+MoveContentAction.TARGET_SA_ELEMENT_NAME+"...");}
			if(currParent.hasComponent(MoveContentAction.TARGET_SA_ELEMENT_NAME)){
				TextComponent tc=(TextComponent)currParent.getComponentByReference(MoveContentAction.TARGET_SA_ELEMENT_NAME);
				String saPath=tc.getText();
				if(isDebugEnabled){LOG.finest(METHOD_NAME+" Parent site area has element "+MoveContentAction.TARGET_SA_ELEMENT_NAME+" with value "+saPath+". Locating site area by path...");}
				DocumentIdIterator itr=wkspc.findByPath(saPath, Workspace.WORKFLOWSTATUS_PUBLISHED);
				if(itr.hasNext()){				
					if(isDebugEnabled){LOG.finest(METHOD_NAME+" Located site area "+saPath+".");}
					PlacementLocation loc=new PlacementLocation(itr.nextId(),Placement.END);
					MoveOptions opts=new MoveOptions();
					if(isDebugEnabled){LOG.finest(METHOD_NAME+" Moving content item "+content.getId()+" to site area "+saPath+"...");}
					wkspc.move(content,loc,opts);
					//if(isDebugEnabled){LOG.finest(METHOD_NAME+" Updating effective date...");}
					//content.setEffectiveDate(new Date());
				}
			}
		}catch(WCMException e){
			if(isErrorEnabled){LOG.log(Level.SEVERE,"An error occurred while moving/updating the content item "+content.getId()+"!  The content item will not be moved or updated.",e);}
			if(isTraceEnabled){LOG.exiting(CLASS_NAME,METHOD_NAME);}
			return null;
		}
		if(isDebugEnabled){LOG.finest(METHOD_NAME+" Content item "+content.getId()+" successfully moved and updated!");}
		if(isTraceEnabled){LOG.exiting(CLASS_NAME,METHOD_NAME,Directives.CONTINUE);}
		return customWorkflowService.createResult(Directives.CONTINUE, "Continue");
	}
}
