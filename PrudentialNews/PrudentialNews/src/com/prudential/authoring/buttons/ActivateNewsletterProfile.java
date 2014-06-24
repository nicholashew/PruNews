package com.prudential.authoring.buttons;

import com.ibm.portal.ListModel;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.workplace.wcm.api.*;
import com.ibm.workplace.wcm.api.exceptions.AuthorizationException;
import com.ibm.workplace.wcm.api.exceptions.PropertyRetrievalException;
import com.ibm.workplace.wcm.api.extensions.authoring.*;
import com.prudential.authoring.AuthoringUtils;

public class ActivateNewsletterProfile implements AuthoringAction {

	/** Logger for the class */
	private static final Logger s_log = Logger.getLogger(ActivateNewsletterProfile.class.getName());
	private static final String DESC = "ActivateNewsletterProfile used to enable/disable a Newsletter Profile";
	private static final String TITLE = "ActivateNewsletterProfile";
	private static final String ATNAME = "AT - Newsletter Profile";
	
	/**
	 * @param p_arg0
	 * @return 
	 * @see com.ibm.portal.Localized#getDescription(java.util.Locale)
	 */
	@Override
	public String getDescription(Locale p_arg0) {
		boolean isDebug = s_log.isLoggable(Level.FINEST);
		if (isDebug) {
			s_log.exiting("ActivateNewsletterProfile", "getDescription returning " + DESC);
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
			s_log.exiting("ActivateNewsletterProfile", "getTitle returning " + TITLE);
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
			s_log.entering("ActivateNewsletterProfile", "execute " + fc);
		}

		Content cont = (Content)fc.document();
		return AuthoringUtils.setActivation(cont, AuthoringUtils.Action.ACTIVATE);
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
			s_log.entering("ActivateNewsletterProfile", "isValidForForm " + fc);
		}
		if (fc.isFormReadOnly()) {
			if (isDebug) {
				s_log.log(Level.FINEST, "Document is in read only mode");
			}
			Document doc = fc.document();
			if (doc instanceof Content) {
				Content cont = (Content)doc;
				try {
					if (isDebug) {
						s_log.log(Level.FINEST, "Is content...");
					}
					if (ATNAME.equals(cont.getAuthoringTemplateID().getName())) {
						if ("Inactive".equals(cont.getParentId().getName())) {
							if (isDebug) {
								s_log.log(Level.FINEST, "Is in the Inactive site area - SHOW");
							}
							isValid = true;
						}
					}
				} catch (AuthorizationException ex) {
					Logger.getLogger(ActivateNewsletterProfile.class.getName()).log(Level.SEVERE, null, ex);
				} catch (PropertyRetrievalException ex) {
					Logger.getLogger(ActivateNewsletterProfile.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
		if (isDebug) {
			s_log.entering("ActivateNewsletterProfile", "isValidForForm returning " + isValid);
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