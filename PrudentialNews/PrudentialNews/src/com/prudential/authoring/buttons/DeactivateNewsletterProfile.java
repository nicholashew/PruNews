package com.prudential.authoring.buttons;

import com.ibm.portal.ListModel;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;

import com.ibm.workplace.wcm.api.*;
import com.ibm.workplace.wcm.api.exceptions.AuthorizationException;
import com.ibm.workplace.wcm.api.exceptions.DocumentRetrievalException;
import com.ibm.workplace.wcm.api.exceptions.DuplicateChildException;
import com.ibm.workplace.wcm.api.exceptions.IllegalDocumentTypeException;
import com.ibm.workplace.wcm.api.exceptions.PropertyRetrievalException;
import com.ibm.workplace.wcm.api.extensions.authoring.*;
import com.prudential.authoring.AuthoringUtils;
import com.prudential.utils.*;
import javax.naming.NamingException;

public class DeactivateNewsletterProfile implements AuthoringAction {

	/** Logger for the class */
	private static final Logger s_log = Logger.getLogger(DeactivateNewsletterProfile.class.getName());
	private static final String DESC = "DeactivateNewsletterProfile used to enable/disable a Newsletter Profile";
	private static final String TITLE = "DeactivateNewsletterProfile";
	
	private static final String ATNAME = "AT - Newsletter Profile";
	
	private static final String ACTIVEPATH = "NewsletterProfiles/Active";
	private static final String INACTIVEPATH = "NewsletterProfiles/Inactive";
	
	private static Workspace wksp = null;

	/**
	 * @param p_arg0
	 * @return 
	 * @see com.ibm.portal.Localized#getDescription(java.util.Locale)
	 */
	@Override
	public String getDescription(Locale p_arg0) {
		boolean isDebug = s_log.isLoggable(Level.FINEST);
		if (isDebug) {
			s_log.exiting("DeactivateNewsletterProfile", "getDescription returning " + DESC);
		}

		return DESC;
	}

	/**
	 * @param p_arg0
	 * @return 
	* @see com.ibm.portal.Localized#getTitle(java.util.Locale)
	*/
	@Override
	public String getTitle(Locale p_arg0) {
		boolean isDebug = s_log.isLoggable(Level.FINEST);
		if (isDebug) {
			s_log.exiting("DeactivateNewsletterProfile", "getTitle returning " + TITLE);
		}
		return TITLE;
	}

	/**
	 * @param fc
	 * @return 
	 * @see com.ibm.workplace.wcm.api.extensions.authoring.AuthoringAction#execute(com.ibm.workplace.wcm.api.extensions.authoring.FormContext)
	 */
	@Override
	public ActionResult execute(FormContext fc) {
		boolean isDebug = s_log.isLoggable(Level.FINEST);
		if (isDebug) {
			s_log.entering("DeactivateNewsletterProfile", "execute " + fc);
		}
		Content cont = (Content)fc.document();
		return AuthoringUtils.setActivation(cont, AuthoringUtils.Action.DEACTIVATE);
	}

	/**
	 * @param fc
	 * @return 
	 * @see com.ibm.workplace.wcm.api.extensions.authoring.AuthoringAction#isValidForForm(com.ibm.workplace.wcm.api.extensions.authoring.FormContext)
	 */
	@Override
	public boolean isValidForForm(FormContext fc) {
		boolean isDebug = s_log.isLoggable(Level.FINEST);
		boolean isValid = false;

		if (isDebug) {
			s_log.entering("DeactivateNewsletterProfile", "isValidForForm " + fc);
		}
		if (fc.isFormReadOnly()) {
			Document doc = fc.document();
			if (doc instanceof Content) {
				try {
					Content cont = (Content)doc;
					if (ATNAME.equals(cont.getAuthoringTemplateID().getName())) {
						if ("Active".equals(cont.getParentId().getName())) {
							if (isDebug) {
								s_log.log(Level.FINEST, "Is in the Active site area - SHOW");
							}
							isValid = true;
						}
					}
				} catch (AuthorizationException ex) {
					Logger.getLogger(DeactivateNewsletterProfile.class.getName()).log(Level.SEVERE, null, ex);
				} catch (PropertyRetrievalException ex) {
					Logger.getLogger(DeactivateNewsletterProfile.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
		if (isDebug) {
			s_log.entering("DeactivateNewsletterProfile", "isValidForForm returning " + isValid);
		}
		return isValid;
	}

	/**
	 * @return 
	 * @see com.ibm.workplace.wcm.api.extensions.authoring.AuthoringAction#ordinal()
	 */
	@Override
	public int ordinal() {
		return 0;
	}

	public ListModel<Locale> getLocales() {
		return null;
	}
}