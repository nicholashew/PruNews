<%@ page import="java.util.*"%>
<%@page import="java.io.File"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="java.net.URL"%>
<%@page import="java.io.FileReader"%>
<%@page import="java.io.BufferedReader"%>
<%@ page
	import="com.ibm.workplace.wcm.api.*,com.ibm.workplace.wcm.api.exceptions.*"%>
<%@ page import="com.prudential.utils.Utils"%>
<%@ page import="com.prudential.wcm.WCMUtils"%>

<%@ taglib uri="/WEB-INF/tld/wcm.tld" prefix="wcm"%>
<wcm:initworkspace
	user="<%= (java.security.Principal)request.getUserPrincipal() %>" />

<%
   String libName = "PrudentialNewsDesign";
   String taxName = "PrudentialCategories";

   // get a system workspace
   Workspace ws = Utils.getSystemWorkspace();
   ws.login();

   ws.setCurrentDocumentLibrary(ws.getDocumentLibrary(libName));
   String taxIdString = "871d653e-5ad8-40bf-a83f-47f1bcbebd98";
   DocumentId taxId = ws.createDocumentId(taxIdString);
   Taxonomy activeTax = (Taxonomy) ws.getById(taxId);
   DocumentIdIterator<Document> itor = activeTax.getAllChildren();
   //DocumentIterator<Document> itor = ws.getByIds(ws.findByType(DocumentTypes.Category), true, false);

   while (itor.hasNext()) {
   	DocumentId catId = (DocumentId)itor.next();
     
      try {
         ws.delete(catId);
         out.println("Deleted category " + catId);
      }
      catch (Exception e) {
         out.println(e.getMessage() + "<br>");
      }
   }
%>
</ul>