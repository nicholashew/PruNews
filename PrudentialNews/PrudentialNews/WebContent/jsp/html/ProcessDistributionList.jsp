<%@page import="javax.naming.CompositeName"%>
<%@page import="javax.naming.InitialContext"%>
<%@page import="javax.naming.Name"%>
<%@page import="java.security.Principal"%>

<%@page import="java.util.Date"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Arrays"%>
<%@page import="java.util.HashMap"%>
<%@page import="com.ibm.workplace.wcm.api.*"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%!
/* 
 * HOW TO USE:
 * -----------
 * 1. Create a form in WCM / Dashboard which displays a list of Newsletters (checkboxes)
 *    with the ID of each Newsletter stored as the checkbox value.
 *
 *    <form method="POST" action=".../ProcessDistributionList.jsp">
 *       <input type="hidden" name="action" value="subscribe" />
 *       <input type="checkbox" name="newsletter" value="[Newsletter ID]" />Newsletter A<br />
 *       ...
 *       <input type="submit" value="Subscribe" />
 *    </form>
 *
 *    OR
 *
 *    <form method="POST" action=".../ProcessDistributionList.jsp">
 *       <input type="hidden" name="action" value="unsubscribe" />
 *       <input type="checkbox" name="newsletter" value="[Newsletter ID]" />Newsletter A<br />
 *       ...
 *       <input type="submit" value="Unsubscribe" />
 *    </form>
 * 2. Set the "METHOD" to "POST" and the "ACTION" to this JSP (as above).
 * 3. When it executes, it will iterate through each of the IDs passed and re-construct
 *    the Newsletter ID.
 * 4. Based on the Newsletter ID (stored in the newsletter[] array parameter), it will 
 *    retrieve the Distribution List (which must be linked) and process each user by Email
 *    address each of the Newsletters.
 *
 * NOTE: It currently takes the Principal (current User) that is logged into Portal, however,
 *       it can easily be modified to retrieve a user based on their Email address - if they
 *       exist by adding:
 *       Email: <input type="input" name="user" type="email" /><br />
 */

Date start = new Date();
static Repository repos = WCM_API.getRepository();
static Workspace wksp;
static Logger s_log = Logger.getLogger("Subscribe-Unsubscribe");
%>

<%
boolean DEBUG = s_log.isLoggable(Level.FINEST);

String result = "";
if (DEBUG) {
	s_log.log(Level.FINEST, "METHOD: " + request.getMethod());
}
if ("POST".equals(request.getMethod().toUpperCase())) {
	// Process the request
	String action = request.getParameter("action");
	if (action != null) {
		if (DEBUG) {
			s_log.log(Level.FINEST, "ACTION: " + action);
		}
		String[] newsletters = request.getParameterValues("newsletter");
		// Get the SystemWorkspace
		try {
			if (wksp == null) {
				wksp = repos.getSystemWorkspace();
			}

			java.security.Principal user_principal = request.getUserPrincipal();

			if (DEBUG) {
				s_log.log(Level.FINEST, "USER: " + user_principal.toString());
			}
			for (int x = 0; x < newsletters.length; x++) {
				String newsletter = newsletters[x];
				DocumentId id = wksp.createDocumentId(newsletter);

				// Found the Newsletter document - doc
				Content cont = (Content)wksp.getById(id);

				if (DEBUG) {
					s_log.log(Level.FINEST, "Newsletter: " + cont.getTitle());
				}

				String elName = "Distribution List";
				if (cont.hasComponent(elName)) {
					LinkComponent dListLink = (LinkComponent)cont.getComponent(elName);
					DocumentId listId = dListLink.getDocumentReference();
					cont = (Content)wksp.getById(listId);
					
					if (DEBUG) {
						s_log.log(Level.FINEST, "Distribution List: " + cont.getTitle());
					}
					elName = "Users";
					if (cont.hasComponent(elName)) {
						UserSelectionComponent usc = (UserSelectionComponent)cont.getComponent(elName);
						
						java.security.Principal[] pArray = usc.getSelections();
						Set<Principal> qSet = new HashSet<Principal>();
						boolean dirty = false;
						if ("UNSUBSCRIBE".equals(action.toUpperCase())) {
							// Unsubscribe
							for (int c = 0; c < pArray.length; c++) {
								java.security.Principal p = pArray[x];
								if (p.equals(user_principal)) {
									s_log.log(Level.FINEST, "Removing: " + user_principal);
								} else {
									qSet.add(p);
								}
							}
							java.security.Principal[] qArray = qSet.toArray(new Principal[qSet.size()]);
							if (pArray.length != qArray.length) {
								dirty = true;
								usc.setSelections(qArray);
							}
						} else if ("SUBSCRIBE".equals(action.toUpperCase())) {
							// Subscribe
							for (int c = 0; c < pArray.length; c++) {
								java.security.Principal p = pArray[x];
								qSet.add(p);
							}
							qSet.add(user_principal);
							java.security.Principal[] qArray = qSet.toArray(new Principal[qSet.size()]);
							if (pArray.length != qArray.length) {
								dirty = true;
								usc.setSelections(qArray);
							}
						} else {
							// Not implemented.
						}
						if (dirty) {
							s_log.log(Level.FINEST, "Saving: " + cont.getTitle());
							cont.setComponent("Users", usc);
							String[] errors = wksp.save(cont);
							for (int y = 0; y < errors.length; y++) {
								result += "<div class=\"error\">" + errors[x];
							}

						}
					}
				}
			}
			result = "<div class=\"success\">Prferences updated</div>";
		} catch (Exception e) {
			result = "<div class=\"error\">An error occurred: " + e.toString() + "</div>";
		}
	}
	out.println(result);
} else {
	// Not implemented.
}
Date end = new Date();
%>
<!-- Completed: <%= (end.getTime() - start.getTime()) %>ms -->
