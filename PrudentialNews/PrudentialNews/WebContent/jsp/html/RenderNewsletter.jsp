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
<wcm:initworkspace user="<%= (java.security.Principal)request.getUserPrincipal() %>" />
<%
RenderingContext rc = (RenderingContext) request.getAttribute(Workspace.WCM_RENDERINGCONTEXT_KEY);
Workspace ws = Utils.getSystemWorkspace();
	   try {
	      ws.login();
	      String uuid = (String)request.getParameter("newsletter_uuid");
	      
	      DocumentId contentId = ws.createDocumentId(uuid);
	      if(contentId == null) {
	         throw new Exception("couldn't retrieve contentId");
	      }
	      Content newsContent = (Content)ws.getById(contentId);
	      if(newsContent == null) {
             throw new Exception("couldn't retrieve newsContent");
          }
	      DocumentId parentSAID = newsContent.getParentId();
	      if(parentSAID == null) {
             throw new Exception("couldn't retrieve parentSAID");
          }
	      SiteArea parentSA = (SiteArea)ws.getById(parentSAID);
	      if(parentSA == null) {
             throw new Exception("couldn't retrieve parentSA");
          }
	      
	      
	      Map params = new HashMap();	     
	      
	      rc.setRenderedContent(newsContent, parentSA);
	      String renderedContent = ws.render(rc);
	      out.print(renderedContent);	      
	   }
	   catch (Exception e) {	 
	     e.printStackTrace();
	   }
	   finally {
	      if(ws != null) {
	         ws.logout();
	      }
	   }
	   %>