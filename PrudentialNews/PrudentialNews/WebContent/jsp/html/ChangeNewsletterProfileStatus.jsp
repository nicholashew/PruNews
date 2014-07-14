<%@ page import="java.util.*"%>
<%@	page import="java.io.File"%>
<%@ page import="java.io.InputStreamReader"%>
<%@ page import="java.net.URL"%>
<%@ page import="java.io.FileReader"%>
<%@ page import="java.io.BufferedReader"%>
<%@ page import="com.prudential.authoring.AuthoringUtils"%>
<%@ page
	import="com.ibm.workplace.wcm.api.*,com.ibm.workplace.wcm.api.exceptions.*"%>
<%@ page import="com.prudential.utils.Utils"%>
<%@ page import="com.prudential.wcm.WCMUtils"%>

<%@ taglib uri="/WEB-INF/tld/wcm.tld" prefix="wcm"%>
<wcm:initworkspace
	user="<%= (java.security.Principal)request.getUserPrincipal() %>" />

<%
   // get a system workspace
   Workspace ws = Utils.getSystemWorkspace();
   ws.login();
   String statusString = request.getParameter("status");
   String uuid = request.getParameter("uuid");
   
   DocumentId contentId = ws.createDocumentId(uuid);
   Content theContent = (Content) ws.getById(contentId);

   if (statusString.equalsIgnoreCase("active")) {
      AuthoringUtils.setActivation(theContent, AuthoringUtils.Action.ACTIVATE);
   }
   else if (statusString.equalsIgnoreCase("inactive")) {
      AuthoringUtils.setActivation(theContent, AuthoringUtils.Action.DEACTIVATE);
   }

   // get the content then change the status
%>
