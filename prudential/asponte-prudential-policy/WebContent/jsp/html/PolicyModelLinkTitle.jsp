<%@ taglib uri="/WEB-INF/tld/wcm.tld" prefix="wcm" %> 
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" %> 
<%@ page import="com.ibm.workplace.wcm.api.*" %> 
<wcm:initworkspace user="<%= (java.security.Principal)request.getUserPrincipal()%>">Cannot get Workspace</wcm:initworkspace> 

<% 
Workspace usersWorkspace = (Workspace) pageContext.getAttribute(Workspace.WCM_WORKSPACE_KEY);
RenderingContext currentRenderingContext = (RenderingContext) pageContext.getRequest().getAttribute(Workspace.WCM_RENDERINGCONTEXT_KEY);
// now get the content from the rc
DocumentId currentResultId = currentRenderingContext.getCurrentResultId();

Content myContent = (Content)usersWorkspace.getById(currentResultId);


// check for the component
if(myContent.hasComponent("ModelPolicyLink")) {
  LinkComponent theLink = (LinkComponent)myContent.getComponentByReference("ModelPolicyLink");
  DocumentId referenceId = theLink.getDocumentReference();
  out.print(referenceId.getName().trim());
}

%>