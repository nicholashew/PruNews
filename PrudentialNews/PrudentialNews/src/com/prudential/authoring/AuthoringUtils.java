/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.prudential.authoring;

import com.ibm.workplace.wcm.api.*;
import com.ibm.workplace.wcm.api.exceptions.*;
import com.ibm.workplace.wcm.api.extensions.authoring.ActionResult;
import com.prudential.authoring.buttons.*;
import com.prudential.authoring.Result.ResultStatus;
import com.prudential.utils.Utils;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pete Raleigh
 */
public class AuthoringUtils {
	private static final Logger s_log = Logger.getLogger(ActivateNewsletterProfile.class.getName());
	private static Workspace wksp = null;
	// cmk change

	public static enum Action {
		ACTIVATE, DEACTIVATE;
	}

	private static final String ACTIVEPATH = "NewsletterProfiles/Active";
	private static final String INACTIVEPATH = "NewsletterProfiles/Inactive";
	
	public static ActionResult setActivation(Content cont, Action action) {
		boolean isDebug = s_log.isLoggable(Level.FINEST);
		try {
			if (wksp == null) {
				wksp = Utils.getSystemWorkspace();
			}
			if (action == Action.ACTIVATE) {
				String path = cont.getOwnerLibrary().getName() + "/" + ACTIVEPATH;
				if (isDebug) {
					s_log.log(Level.FINEST, "Path: " + path);
				}
				DocumentIdIterator dii = wksp.findByPath(path, Workspace.WORKFLOWSTATUS_PUBLISHED);
				if (dii.hasNext()) {
					DocumentId di = dii.next();
					// Found the required SiteArea
					PlacementLocation pl = new PlacementLocation(di, Placement.END);
					wksp.move(cont, pl, null);
				}
			} else if (action == Action.DEACTIVATE) {
				String path = cont.getOwnerLibrary().getName() + "/" + INACTIVEPATH;
				DocumentIdIterator dii = wksp.findByPath(path, Workspace.WORKFLOWSTATUS_PUBLISHED);
				if (dii.hasNext()) {
					DocumentId di = dii.next();
					// Found the required SiteArea
					PlacementLocation pl = new PlacementLocation(di, Placement.END);
					wksp.move(cont, pl, null);
				}				
			}
		} catch (DocumentRetrievalException ex) {
			Logger.getLogger(ActivateNewsletterProfile.class.getName()).log(Level.SEVERE, null, ex);
		} catch (AuthorizationException ex) {
			Logger.getLogger(ActivateNewsletterProfile.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IllegalDocumentTypeException ex) {
			Logger.getLogger(ActivateNewsletterProfile.class.getName()).log(Level.SEVERE, null, ex);
		} catch (DuplicateChildException ex) {
			Logger.getLogger(ActivateNewsletterProfile.class.getName()).log(Level.SEVERE, null, ex);
		}

		if (isDebug) {
			s_log.exiting("ActivateNewsletterProfile", "execute " + cont.getName());
		}

		Result result = new Result(ResultStatus.SUCCESS);
		if (action == Action.ACTIVATE) {
			result.setTitle("Newsletter Profile moved to 'Active'");
			result.setDescription("Newsletter Profile moved to the 'Active' - " + ACTIVEPATH);
		} else if (action == Action.DEACTIVATE) {
			result.setTitle("Newsletter Profile moved to 'Inactive'");
			result.setDescription("Newsletter Profile moved to the 'Inactive' - " + INACTIVEPATH);
		}
		return (ActionResult)result;
	}
}
