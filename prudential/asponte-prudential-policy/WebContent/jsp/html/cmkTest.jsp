<%--
/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

--%>
<%@ page import="org.apache.jetspeed.portlet.PortletURI,
                 com.ibm.workplace.wcm.api.*,
                 com.ibm.workplace.wcm.api.query.*,
                 java.util.*,
                 com.prudential.utils.*,
         com.prudential.wcm.*,
                 java.text.*,
                 com.prudential.authoring.launchpage.*,
                 com.ibm.workplace.wcm.api.LinkComponent"%>

<%@ taglib uri="/WEB-INF/tld/wcm.tld" prefix="wcm" %>
<wcm:initworkspace user="<%= (java.security.Principal)request.getUserPrincipal() %>" />

<%
  Workspace ws = Utils.getWorkspace();
  ws.setCurrentDocumentLibrary(ws.getDocumentLibrary("PrudentialNewsDesign"));
  DocumentId theFolderId = Utils.getFolderId(ws, "Components");
  out.println(theFolderId);
%>
