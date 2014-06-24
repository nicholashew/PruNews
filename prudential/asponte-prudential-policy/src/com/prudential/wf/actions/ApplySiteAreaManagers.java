/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.wf.actions;

import com.ibm.workplace.wcm.api.Content;
import java.util.logging.Logger;

import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.DocumentId;
import com.ibm.workplace.wcm.api.Hierarchical;
import com.ibm.workplace.wcm.api.WCM_API;
import com.ibm.workplace.wcm.api.WebContentCustomWorkflowService;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowAction;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowActionResult;
import com.ibm.workplace.wcm.api.custom.Directive;
import com.ibm.workplace.wcm.api.custom.Directives;
import com.ibm.workplace.wcm.api.custom.RollbackDirectiveParams;
import com.ibm.workplace.wcm.api.exceptions.AuthorizationException;
import com.ibm.workplace.wcm.api.exceptions.DocumentRetrievalException;
import com.ibm.workplace.wcm.api.exceptions.OperationFailedException;
import com.ibm.workplace.wcm.api.exceptions.ServiceNotAvailableException;
import com.ibm.workplace.wcm.api.security.Access;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author Pete Raleigh
 */
public class ApplySiteAreaManagers implements CustomWorkflowAction {

	private static final Logger s_log = Logger.getLogger(ApplySiteAreaManagers.class.getName());
	private static Workspace wksp;
	private String message = "";

	// This specifies when the custom action will be executed
	@Override
	public Date getExecuteDate(Document document) {
		return DATE_EXECUTE_NOW;
	}
	
	// This method contains the code that will run when the custom action is executed.
	@Override
	public CustomWorkflowActionResult execute(Document doc) {
	   boolean isDebug = s_log.isLoggable(Level.FINEST);
		Directive directive = Directives.CONTINUE;
		RollbackDirectiveParams params = (RollbackDirectiveParams)Directives.ROLLBACK_DOCUMENT.createDirectiveParams();

		if (doc instanceof Content) {
			Content cont = (Content)doc;
			if (isDebug) {
               s_log.log(Level.FINEST, "Content: {0} ({1})", new Object[]{cont.getTitle(), cont.getName()});
            }
			
			try {
				if (wksp == null) {
					wksp = WCM_API.getRepository().getSystemWorkspace();
					wksp.useDistinguishedNames(false);
				}
				DocumentId parId = ((Hierarchical)cont).getParentId();
				boolean foundManagers = false;
				while (parId != null && !foundManagers) {
					Document parent = wksp.getById(parId);
					String[] managers = parent.getMembersForAccess(Access.MANAGER);
					if (managers != null) {
						foundManagers = true;
						cont.addOwners(managers);
						if (isDebug) {
                           s_log.log(Level.FINEST, "Owners field set to: {0}", Arrays.toString(managers));
                        }
					} else {
						parId = ((Hierarchical)parent).getParentId();
					}
				}
			} catch (OperationFailedException e) {
			   if (isDebug) {
                  s_log.log(Level.FINEST, "exception occurred "+e.getMessage());
                  e.printStackTrace();
               }				
			} catch (ServiceNotAvailableException e) {
			   if (isDebug) {
                  s_log.log(Level.FINEST, "exception occurred "+e.getMessage());
                  e.printStackTrace();
               }
			} catch (DocumentRetrievalException e) {
			   if (isDebug) {
                  s_log.log(Level.FINEST, "exception occurred "+e.getMessage());
                  e.printStackTrace();
               }
			} catch (AuthorizationException e) {
			   if (isDebug) {
                  s_log.log(Level.FINEST, "exception occurred "+e.getMessage());
                  e.printStackTrace();
               }
			}
		}
		WebContentCustomWorkflowService webContentCustomWorkflowService;
		try {
			// Construct and inital Context
			InitialContext ctx = new InitialContext();
			// Retrieve WebContentCustomWorkflowService using JNDI name
			webContentCustomWorkflowService = (WebContentCustomWorkflowService)ctx.lookup("portal:service/wcm/WebContentCustomWorkflowService");
		} catch(NamingException ex) {
			return null;
		}
		if (directive == Directives.CONTINUE) {
			message = "OK - Owners fields set";
			if (isDebug) {
               s_log.log(Level.FINEST, message);               
            }			
			return webContentCustomWorkflowService.createResult(directive, message);
		}
		message = "ERROR: An error has occurred - contact your System Administrator";
		if (isDebug) {
           s_log.log(Level.FINEST, message);               
        }
		params.setCustomErrorMsg(message);
		return webContentCustomWorkflowService.createResult(directive, "Rolling back document.", params);
	}
}