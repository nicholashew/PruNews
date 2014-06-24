package com.prudential.wcm.workflow.actions;

import java.util.Locale;

import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowAction;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowActionFactory;

/**
 * This class implements the {@link com.ibm.workplace.wcm.api.custom.CustomWorkflowActionFactory} class
 * to register the {@link MoveContentAction} as a custom workflow action to WCM.  
 * <p>
 * Note that the methods that take a {@link java.util.Locale} ignore the Locale and return only English 
 * titles and descriptions.  Translating the action and action factory titles and descriptions is really 
 * only necessary if the IT staff building the workflow assets require this level of translation.  Generally 
 * this is not the case and translation and the accompanying logic is overkill.  However, if desired, it is
 * a fairly simple exercise to add this behavior.
 * </p>
 *  
 * @author dewittsc@asponte.com
 * @see com.ibm.workplace.wcm.api.custom.CustomWorkflowActionFactory
 * @see MoveContentAction
 */
public class CustomWorkflowActionFactoryImpl implements CustomWorkflowActionFactory {

	/** Custom workflow action factory name */
	private static final String NAME="PrudentialCustomWorkflowActionFactory";
	/** Custom workflow action factory english title (seen in the authoring UI) */
	private static final String TITLE="Prudential Custom Workflow Action Factory";
	/** The names of the actions this factory supports */
	private static final String []ACTION_NAMES={MoveContentAction.NAME,UpdatePublishDateAction.NAME};
	
	/**
	 * Default constructor
	 */
	public CustomWorkflowActionFactoryImpl() {
	}

	
	/**
	 * Get the name for this factory. Used as a unique identifier.
	 * 
	 * @return Factory name. The factory name must be smaller than 50 characters long.
	 * @see com.ibm.workplace.wcm.api.custom.CustomWorkflowActionFactory#getName()
	 */
	@Override
	public String getName() {
		return CustomWorkflowActionFactoryImpl.NAME;
	}

	/**
	 * Get the display title for this factory.
	 * 
	 * @param locale Locale to display title
	 * @return Title for the factory
	 * @see com.ibm.workplace.wcm.api.custom.CustomWorkflowActionFactory#getTitle(java.util.Locale)
	 */
	@Override
	public String getTitle(Locale locale) {
		return CustomWorkflowActionFactoryImpl.TITLE;
	}

	/**
	 * Get an array of action names supported by this factory.
	 * 
	 * @return Action names. Each action name must be smaller than 200 characters long.
	 * @see com.ibm.workplace.wcm.api.custom.CustomWorkflowActionFactory#getActionNames()
	 */
	@Override
	public String[] getActionNames() {
		return ACTION_NAMES;
	}

	/**
	 * Get the display title for the supplied action name.
	 * 
	 * @param locale       Locale to display title.
	 * @param actionName   Action name.
	 * @return Title for the action name.
	 * @see com.ibm.workplace.wcm.api.custom.CustomWorkflowActionFactory#getActionTitle(java.util.Locale, java.lang.String)
	 */
	@Override
	public String getActionTitle(Locale locale, String actionName) {
		if(actionName.equals(MoveContentAction.NAME)){
			return MoveContentAction.TITLE;
		}else if(actionName.equals(UpdatePublishDateAction.NAME)){
			return UpdatePublishDateAction.TITLE;
		}else{
			return null;
		}
	}
	
	/**
	 * Get the description for the supplied action name
	 * 
	 * @param locale       Locale to display the description.
	 * @param actionName   Action name.
	 * @return Description for the action name.
	 * @see com.ibm.workplace.wcm.api.custom.CustomWorkflowActionFactory#getActionDescription(java.util.Locale, java.lang.String)
	 */
	@Override
	public String getActionDescription(Locale locale, String actionName) {
		if(actionName.equals(MoveContentAction.NAME)){
			return MoveContentAction.DESCRIPTION;
		}else if(actionName.equals(UpdatePublishDateAction.NAME)){
			return UpdatePublishDateAction.DESCRIPTION;
		}else{
			return null;
		}
	}

	/**
	 * Get an instance of the custom workflow action to execute.
	 * 
	 * @param actionName   Action name.
	 * @param document     Target document. Custom code must not modify this document.
	 * @see com.ibm.workplace.wcm.api.custom.CustomWorkflowActionFactory#getAction(java.lang.String, com.ibm.workplace.wcm.api.Document)
	 */
	@Override
	public CustomWorkflowAction getAction(String actionName, Document document) {
		if(actionName.equals(MoveContentAction.NAME)){
			return new MoveContentAction();
		}else if(actionName.equals(UpdatePublishDateAction.NAME)){
			return new UpdatePublishDateAction();
		}else{
			return null;
		}
	}
}