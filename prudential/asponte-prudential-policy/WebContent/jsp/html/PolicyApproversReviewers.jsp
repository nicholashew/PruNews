<%@ page
	import="com.prudential.utils.*,com.ibm.workplace.wcm.api.UserSelectionComponent,com.ibm.workplace.wcm.api.Workspace,com.ibm.workplace.wcm.api.DocumentId,com.ibm.workplace.wcm.api.RenderingContext,com.ibm.workplace.wcm.api.*,java.security.Principal"%>
<%@ taglib uri="/WEB-INF/tld/wcm.tld" prefix="wcm"%>
<wcm:initworkspace
	user="<%= (java.security.Principal)request.getUserPrincipal() %>" />
<%
   Workspace usersWorkspace = (Workspace) pageContext.getAttribute(Workspace.WCM_WORKSPACE_KEY);
   String curAR = "";
   RenderingContext rc = (RenderingContext) pageContext.getRequest().getAttribute(Workspace.WCM_RENDERINGCONTEXT_KEY);
   Content incoming = rc.getContent();
   if (incoming != null) {
      DocumentId parentId = incoming.getDirectParent();
      SiteArea curSA = (SiteArea) usersWorkspace.getById(parentId);
      if (curSA.hasComponent("DefaultPolicyApprover")) {
         UserSelectionComponent approversCmpt = (UserSelectionComponent) curSA.getComponent("DefaultPolicyApprover");
         if (approversCmpt != null) {
            Principal[] approversAry = approversCmpt.getSelections();
            for (int i = 0; i < approversAry.length; i++) {
               String curText = Utils.getDnForJavaPrincipal(approversAry[i]);
               //String curText = approversAry[i].toString();
               //curText = curText.split("dn: ")[1];
               //curText = curText.split(", ")[0];
               curAR += "&wcmfield.element.PolicyApprovers=" + curText;
            }
         }
      }
      if (curSA.hasComponent("DefaultPolicyReviewer")) {
         UserSelectionComponent reviewerCmpt = (UserSelectionComponent) curSA.getComponent("DefaultPolicyReviewer");
         if (reviewerCmpt != null) {
            Principal[] reviewersAry = reviewerCmpt.getSelections();
            for (int i = 0; i < reviewersAry.length; i++) {
               //String curText = reviewersAry[i].toString();
               String curText = Utils.getDnForJavaPrincipal(reviewersAry[i]);
               //curText = curText.split("dn: ")[1];
               //curText = curText.split(", ")[0];
               curAR += "&wcmfield.element.PolicyReviewers=" + curText;
            }
         }
      }
      if (curAR != "") {
         out.print("+'" + curAR + "'");
      }
   }
%>
