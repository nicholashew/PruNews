<%--
/********************************************************************/
/* Asponte
/* jewers
/********************************************************************/
--%>
<%@ page import="org.apache.jetspeed.portlet.PortletURI,
                 com.ibm.workplace.wcm.api.*,
                 com.ibm.workplace.wcm.api.query.*,
                 java.util.*,
                 java.text.*,
                 com.prudential.authoring.launchpage.*,
                 com.ibm.workplace.wcm.api.authoring.CustomItemBean"%>
<%@ taglib uri="/WEB-INF/tld/std-portlet.tld" prefix="portlet" %>
<%@ taglib uri="/WEB-INF/tld/wcm.tld" prefix="wcm" %>
<portlet:defineObjects/>

<wcm:initworkspace user="<%= (java.security.Principal)request.getUserPrincipal() %>" />

<%
  Workspace usersWorkspace = (Workspace) pageContext.getAttribute(Workspace.WCM_WORKSPACE_KEY);
  CustomItemBean customItem = (CustomItemBean) request.getAttribute("CustomItemBean");
  String currentDocId = "";
  String id = customItem.getId();
  DocumentId docid = null;
  DocumentId pid = null;
  Content curContent = null;
  SiteArea pSiteArea = null;

  if(id!=null && id!="") { 
    docid=usersWorkspace.createDocumentId(id);
    if(docid.getType().equals(DocumentTypes.Content)){
      curContent=(Content)usersWorkspace.getById(docid,true);
      pid=curContent.getDirectParent();
      if(pid!=null){
        pSiteArea = (SiteArea)usersWorkspace.getById(pid,true);
      }
    }
  }

  if(curContent!=null && pSiteArea!=null){
    //Content and parent site area have been found. Next step is to see if the content has approvers/reviewers already.
    if(curContent.hasComponent("DefaultPolicyReviewer") && pSiteArea.hasComponent("DefaultPolicyReviewer")){
      UserSelectionComponent contentPolicyReviewer = (UserSelectionComponent) curContent.getComponent("DefaultPolicyReviewer");
      if(contentPolicyReviewer==null || contentPolicyReviewer.getSelections()==null || contentPolicyReviewer.getSelections().length > 1) { 
        UserSelectionComponent saPolicyReviewer = (UserSelectionComponent)pSiteArea.getComponent("DefaultPolicyReviewer");
        curContent.setComponent("DefaultPolicyReviewer", saPolicyReviewer);
      }
    }
  }
%>