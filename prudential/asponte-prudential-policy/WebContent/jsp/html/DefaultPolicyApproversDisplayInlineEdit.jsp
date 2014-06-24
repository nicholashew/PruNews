<%@ page import="com.ibm.workplace.wcm.api.UserSelectionComponent,com.ibm.workplace.wcm.api.Workspace,com.ibm.workplace.wcm.api.DocumentId,com.ibm.workplace.wcm.api.RenderingContext,com.ibm.workplace.wcm.api.*,java.security.Principal"%>
<%@ taglib uri="/WEB-INF/tld/wcm.tld" prefix="wcm"%>
<wcm:initworkspace user="<%= (java.security.Principal)request.getUserPrincipal() %>" />
<%
String renderString = "%><wcm:libraryComponent name="CMKTestHTML" library="PrudentialNewsDesign" /><%";
out.println(renderString);
%>