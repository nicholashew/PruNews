<%@ page import="javax.naming.InitialContext,javax.naming.NamingException"%>
<%@ page import="java.util.*"%>
<%@ page import="java.util.logging.Level"%>
<%@ page import="java.util.logging.Logger"%>
<%@ page import="com.ibm.workplace.wcm.api.*,com.ibm.workplace.wcm.api.exceptions.*"%>
<%@ page import="com.prudential.utils.Utils"%>
<%@ page import="com.prudential.wcm.WCMUtils"%>
<%@ page import="java.security.Principal"%>
<%@ page import="com.ibm.portal.um.*"%>
<%@ taglib uri="/WEB-INF/tld/wcm.tld" prefix="wcm"%>
<wcm:initworkspace
	user="<%= (java.security.Principal)request.getUserPrincipal() %>" /><%
	RenderingContext rc = (RenderingContext) pageContext.getRequest().getAttribute(Workspace.WCM_RENDERINGCONTEXT_KEY);
	if(rc != null) {
		System.out.println("rc != null");
	} 
	DocumentId incomingId = rc.getCurrentResultId();	
        String stringID = incomingId.getId();
        System.out.println ("Got ID --" + stringID + "--");
	out.print(stringID);
%>