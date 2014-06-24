<%@ page import="com.ibm.workplace.wcm.api.*"%>
<%@ page import="com.ibm.workplace.wcm.api.exceptions.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.*"%>
<%@ taglib uri="/WEB-INF/tld/wcm.tld" prefix="wcm"%>
<wcm:initworkspace
	user="<%= (java.security.Principal)request.getUserPrincipal() %>" />
<%
   try {
      String user1 = "cknight";
      String user1pwd = "ses03pwd";
      String contentToUpdate = "Content1";
      Content savedContent = null;
      Content savedContent2 = null;

	  Workspace ws = (Workspace) pageContext.getAttribute(Workspace.WCM_WORKSPACE_KEY);
      //Workspace ws = WCM_API.getRepository().getWorkspace(user1, user1pwd);
      //ws.login();
      out.println("workspace is retrieved <br>");

      Content content = null;
      DocumentId wfID = null;
      DocumentId wfStageID = null;
      DocumentId contentStageId = null;
      DocumentLibrary docLib = null;

      docLib = ws.getDocumentLibrary("JDMenuIssue");
      if (docLib == null) {
         out.println("docLib is null <br/><br/>");
      }
      else {
         ws.setCurrentDocumentLibrary(docLib);
         out.println("docLib set to " + docLib.getName() + " <br/><br/>");
      }

      String returnedValue = "";

      // cmk 
      // 9ee18335-bc02-4b0c-ae03-3f93027aaaa0 is the external link
      // b9f628b2-2f77-4834-a806-8fd9becb0f4a is the file linked content
      String uuid = "9ee18335-bc02-4b0c-ae03-3f93027aaaa0";
      String uuid2 = "b9f628b2-2f77-4834-a806-8fd9becb0f4a";
      //String uuid = "6075f5d7-1553-471b-af05-09e4070eac14";    
      //String uuid = "61cd6301-e03c-4493-be1d-dbee46fd60d8";

      DocumentId theId = ws.createDocumentId(uuid);
      DocumentId theId2 = ws.createDocumentId(uuid2);
      DocumentId[] docIdArray = {theId, theId2};
      DocumentIdIterator theIt = ws.createDocumentIdIterator(docIdArray);
      while (theIt.hasNext()) {
		 theId = (DocumentId)theIt.next();
         // get the authoring template 
         Content theContent = (Content) ws.getById(theId);
         out.println("Content name =  " + theContent.getName() + " <br/><br/>");
         out.println("Content id =  " + theContent.getId() + " <br/><br/>");
         DocumentId atId = theContent.getAuthoringTemplateID();

         out.println("auth template name =  " + atId.getName() + " <br/><br/>");
         out.println("auth template id =  " + atId.getId() + " <br/><br/>");

         AuthoringTemplate theAt = (AuthoringTemplate) ws.getById(atId);
         out.println("auth template name =  " + theAt.getName() + " <br/><br/>");

         TargetableContentComponent theTarget = theAt.getTargetComponent();

         if (theTarget != null) {
            out.println("AT using targetable </br>");
            if (theTarget.getDocumentType().equals(DocumentTypes.LinkComponent)) {
               out.println("Link set as targetable </br>");
               LinkComponent lc = (LinkComponent) theTarget;
               String linkName = lc.getName();
               out.println("link name =  " + linkName + " <br/><br/>");                              
               // have to get from the Content
               lc = (LinkComponent)theContent.getComponent(linkName);
               out.println("link url =  " + lc.getURL() + " <br/><br/>");  
               returnedValue = lc.getURL();
            }
            else {
               out.println("File set as targetable </br>");
               FileComponent fc = (FileComponent) theTarget;
               String fileName = fc.getName();
               fc = (FileComponent)theContent.getComponent(fileName);
			   out.println("File component name =  " + fc.getName() + " <br/><br/>");
               out.println("Actual File name =  " + fc.getFileName() + " <br/><br/>");
               returnedValue = fc.getResourceURL();
               out.println("File url =  " + returnedValue + " <br/><br/>");
            }
         }
         else {
            out.println("target component is null </br>");
         }
      }

      //ws.logout();
      //WCM_API.getRepository().endWorkspace();
   }
   catch (Exception e) {
%><%=e.toString()%>
<%
   }
%>
