<%--
/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

--%>
<%@ page
	import="org.apache.jetspeed.portlet.PortletURI,
                 com.ibm.workplace.wcm.api.*,
                 com.ibm.workplace.wcm.api.query.*,
                 java.util.*,
                 com.prudential.utils.*,
         com.prudential.wcm.*,
                 java.text.*,
                 com.prudential.authoring.launchpage.*,
                 com.ibm.workplace.wcm.api.LinkComponent"%>
<%@ taglib uri="/WEB-INF/tld/wcm.tld" prefix="wcm"%>
<%@ taglib uri="/WEB-INF/tld/std-portlet.tld" prefix="portlet"%>
<wcm:initworkspace
	user="<%= (java.security.Principal)request.getUserPrincipal() %>" />

<%
   Workspace ws = Utils.getWorkspace();
   ws.setCurrentDocumentLibrary(ws.getDocumentLibrary("PruPolicyContent"));
   /*
   String said = "ac60f231-bfaa-4e6b-9002-c4acd8a43824";
   DocumentId saDocId = ws.createDocumentId(said);
   SiteArea sa = (SiteArea) ws.getById(saDocId);
   String name = sa.getName();

   DocumentIdIterator children = sa.getAllChildren();
   if (children == null || children.getCount() < 1) {
      out.println("children null or empty");
   }
   else {
      out.println("children not null or empty " + children.getCount());
      while (children.hasNext()) {
         out.println("children contains " + children.nextId().toString() + "<br>");
      }
   }
   */
    // now get by name
   String name = "CMKChild2";
   QueryService queryService = ws.getQueryService();
   Query query = queryService.createQuery();
   Selector titleSelector = Selectors.nameEquals(name);
   Selector typeSelector = Selectors.typeEquals(DocumentTypes.SiteArea.getApiType());
   query.addSelector(titleSelector);
   query.addSelector(typeSelector);
   ResultIterator results = queryService.execute(query);
   if(results == null) {
   	  out.println("results null for "+name);
   }
   else if (results.hasNext()) {
      SiteArea sa = (SiteArea) results.next();
      DocumentIdIterator children = sa.getAllChildren();
      if (children == null || children.getCount() < 1) {
         out.println("children null or empty<br>");
      }
      else {
         out.println("children not null or empty " + children.getCount() + "<br>");
         while (children.hasNext()) {
            out.println("children contains " + children.nextId().toString() + "<br>");
         }
      }
   }
%>