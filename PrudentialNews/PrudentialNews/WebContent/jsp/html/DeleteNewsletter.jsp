<%@ page
	import="javax.naming.InitialContext,javax.naming.NamingException"%>
<%@ page import="java.util.*"%>
<%@ page import="java.util.logging.Level"%>
<%@ page import="java.util.logging.Logger"%>
<%@ page
	import="com.ibm.workplace.wcm.api.*,com.ibm.workplace.wcm.api.exceptions.*"%>
<%@ page import="com.prudential.utils.Utils"%>
<%@ page import="com.prudential.wcm.WCMUtils"%>
<%@ page import="java.security.Principal"%>
<%@ page import="com.ibm.portal.um.*"%>

<%@ taglib uri="/WEB-INF/tld/wcm.tld" prefix="wcm"%>
<%!private static final Logger s_log = Logger.getLogger("com.prudential");%>
<wcm:initworkspace
	user="<%=(java.security.Principal) request.getUserPrincipal()%>" />
<%
	RenderingContext rc = (RenderingContext) request
			.getAttribute(Workspace.WCM_RENDERINGCONTEXT_KEY);
	Workspace ws = Utils.getSystemWorkspace();
	String errors = "success";
	boolean isDebug = s_log.isLoggable(Level.FINEST);
	try {
		ws.login();
		String uuid = (String) request.getParameter("newsletter_uuid");

		DocumentId contentId = ws.createDocumentId(uuid);
		if (contentId == null) {
			throw new Exception("couldn't retrieve contentId");
		}
		Content newsLetterContent = (Content) ws.getById(contentId);
		if (newsLetterContent == null) {
			throw new Exception("couldn't retrieve newsLetterContent");
		}
		DocumentId parentSAID = newsLetterContent.getParentId();
		if (parentSAID == null) {
			throw new Exception("couldn't retrieve parentSAID");
		}
		SiteArea parentSA = (SiteArea) ws.getById(parentSAID);
		if (parentSA == null) {
			throw new Exception("couldn't retrieve parentSA");
		}
		if (isDebug) {
			s_log.log(Level.FINEST,
					"We have " + newsLetterContent.getTitle()
							+ " in site area " + parentSA.getTitle());
		}

		DocumentIdIterator docIdIter = parentSA.getAllChildren();
		while (docIdIter.hasNext()) {
			DocumentId docId = docIdIter.next();
			if (docId.isOfType(DocumentTypes.Content)) {
				Content contentItem = (Content) ws.getById(docId);
				if (isDebug) {
					s_log.log(Level.FINEST, "Will delete content "
							+ contentItem.getTitle());
				}
				try {
					if (ws.isLocked(docId)) {
						ws.unlock(docId);
					}
					ws.delete(docId);
				} catch (Exception e) {
					errors = "fail";
					if (isDebug) {
						s_log.log(Level.FINEST, e.getMessage());
					}
				}

			} else if (docId.isOfType(DocumentTypes.ContentLink)) {
				ContentLink contentItem = (ContentLink) ws
						.getById(docId);
				if (isDebug) {
					s_log.log(Level.FINEST, "Will delete link "
							+ contentItem.getTitle());
				}
				try {
					if (ws.isLocked(docId)) {
						ws.unlock(docId);
					}
					ws.delete(docId);
				} catch (Exception e) {
					errors = "fail";
					if (isDebug) {
						s_log.log(Level.FINEST, e.getMessage());
					}
				}
			}
		}
		// At this point all content should be deleted. Now delete what should be the one remaining site area
		docIdIter = parentSA.getAllChildren();
		while (docIdIter.hasNext()) {
			DocumentId docId = docIdIter.next();
			if (docId.isOfType(DocumentTypes.SiteArea)) {
				SiteArea siteAreaItem = (SiteArea) ws.getById(docId);
				if (isDebug) {
					s_log.log(Level.FINEST, "Will delete siteAreaItem "
							+ siteAreaItem.getTitle());
				}
				try {
					if (ws.isLocked(docId)) {
						ws.unlock(docId);
					}
					ws.delete(docId);
				} catch (Exception e) {
					errors = "fail";
					if (isDebug) {
						s_log.log(Level.FINEST, e.getMessage());
					}
				}
			}
		}
		if (isDebug) {
			s_log.log(Level.FINEST, "All done with DeleteNewsletter");
		}

	} catch (Exception e) {
		e.printStackTrace();
		if (isDebug) {
			s_log.log(Level.FINEST, e.getMessage());
		}
	} finally {
		out.println(errors);
		if (isDebug) {
			s_log.log(Level.FINEST,
					"All done with delete newsletter. Status = "
							+ errors);
		}
		if (ws != null) {
			ws.logout();
		}
	}
%>