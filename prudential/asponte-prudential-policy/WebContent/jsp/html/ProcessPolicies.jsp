<%@ page
	import="org.apache.jetspeed.portlet.PortletURI, 
                 com.ibm.workplace.wcm.api.*, 
                 com.ibm.workplace.wcm.api.query.*, 
                 java.util.*, 
                 com.prudential.utils.*, 
                 com.prudential.wcm.*, 
                 java.text.*, 
                 com.prudential.authoring.launchpage.*"%>
<%!public String[] getParameterValues(javax.servlet.ServletRequest request, String name) {
      System.out.println("getParameterValues for " + name);
      String[] rawValues = request.getParameterValues(name);
      java.util.LinkedList<String> values = new java.util.LinkedList<String>();
      if (rawValues != null) {
         for (int i = 0; i < rawValues.length; ++i) {
            System.out.println("getParameterValues rawValue " + rawValues[i]);
            values.addAll(java.util.Arrays.asList(String.valueOf(rawValues[i]).split(",")));
         }
      }

      System.out.println("getParameterValues values " + values);
      return (String[]) values.toArray(new String[values.size()]);
   }

   public DocumentId getSiteAreaByPath(Workspace ws, String path) {
      System.out.println("getSiteAreaByPath for " + path);
      System.out.println("getSiteAreaByPath for ws " + ws);
      DocumentId returnId = null;
      DocumentIdIterator itor = ws.findByPath(path, Workspace.WORKFLOWSTATUS_ALL);
      if (itor.hasNext()) {
         returnId = itor.nextId();
      }
      System.out.println("getSiteAreaByPath returning =  " + returnId);
      return returnId;
   }

   public void setModelPolicyLinkValue(Document doc, DocumentId modelPolicyId) throws Exception {
      String cmpntName = "ModelPolicy";
      ContentComponent cmpnt = WCMUtils.getContentComponent(doc, cmpntName);
      if (cmpnt == null) {
         cmpntName = "ModelPolicyLink";
         cmpnt = WCMUtils.getContentComponent(doc, cmpntName);
      }
      System.out.println("ModelPolicy ContentComponent: " + cmpnt);
      if (cmpnt instanceof LinkComponent) {
         System.out.println("Setting ModelPolicy Link to: " + modelPolicyId);
         ((LinkComponent) cmpnt).setDocumentReference(modelPolicyId);
         ((ContentComponentContainer) doc).setComponent(cmpntName, cmpnt);
      }
      else {
         System.out.println("Setting ModelPolicy failed");
      }
   }%>
<%
   Workspace wsUser = Utils.getWorkspace();
   String userName = wsUser.getUserProfile().getUsername();
   Workspace ws = Utils.getSystemWorkspace();
   ws.login();
   ws.useUserAccess(true);
   ws.setCurrentDocumentLibrary(ws.getDocumentLibrary("prupolicycontent"));

   String siateAreaPath = request.getParameter("targetPath");
   DocumentId pathId = getSiteAreaByPath(ws, siateAreaPath.replaceAll("\\+", " "));
   String[] copyContent = getParameterValues(request, "pp-copy");
   String[] linkContent = getParameterValues(request, "pp-link");
%>

<div>
	Target Path:
	<%=siateAreaPath%></div>
<div>
	Target ID:
	<%=pathId%></div>
<h1>This is the Policy Processor</h1>
<%
   System.out.println("Doing Copy");
   for (int x = 0; x < copyContent.length; x++) {
      //for(String item: copyContent) { 
      String item = copyContent[x];
      if (item != null && item.trim().length() > 0) {
         try {
            DocumentId docId = ws.createDocumentId(item);
            if (docId != null) {
               System.out.println("Copy Policy ID: " + docId + ", to path ID: " + pathId);
               Content doc = (Content) ws.copy(docId, pathId);
               doc.setTitle(doc.getTitle() + " (copied)");
               setModelPolicyLinkValue(doc, docId);
               // set content to draft by restarting workflow 
               doc.restartWorkflow();
               doc.addHistoryLogEntry("Content copied by user " + userName);
               doc.setEffectiveDate(new Date());
               System.out.println("Saving: " + doc);
               String[] errors = ws.save(doc);
               if (errors != null && errors.length > 0) {
                  System.out.println("Error Count: " + errors.length);
                  for (String errMsg : errors) {
                     System.out.println("Error: " + errMsg);
                     out.println("<br>Error: " + errMsg);
                  }
               }
               else {
                  System.out.println("Save returned no errors");
               }
               doc.nextWorkflowStage();
            }

         }
         catch (Exception e) {
            e.printStackTrace();
         }
      }
   }

   System.out.println("Doing Link");
   DocumentId atId = ws.createDocumentId("9889d022-56d9-4e27-a2be-ded469428e15");
   for (int y = 0; y < linkContent.length; y++) {
      //for(String item: linkContent) { 
      String item = linkContent[y];
      if (item != null && item.trim().length() > 0) {
         try {
            DocumentId docId = ws.createDocumentId(item);
            if (docId != null) {
               System.out.println("Creating Policy Link of: " + docId + ", in path ID: " + pathId);
               Document parent = ws.getById(docId);
               Content doc = (Content) ws.createContent(atId, pathId, null, ChildPosition.END);
               doc.setName(parent.getName());
               doc.setTitle(parent.getTitle() + " (adopted)");
               doc.setDescription(parent.getDescription());
               setModelPolicyLinkValue(doc, docId);
               // do not restart wf or issue will occur. 
               //doc.restartWorkflow(); 

               doc.setEffectiveDate(new Date());
               System.out.println("Saving: " + doc);
               doc.addHistoryLogEntry("Content Adopted by user " + userName);
               String[] errors = ws.save(doc);
               if (errors != null && errors.length > 0) {
                  System.out.println("Error Count: " + errors.length);
                  for (String errMsg : errors) {
                     System.out.println("Error: " + errMsg);
                     out.println("<br>Error: " + errMsg);
                  }
               }
               else {
                  System.out.println("Save returned no errors");
               }
               // after save, nextstage it 
               doc.nextWorkflowStage();
            }
         }
         catch (Exception e) {
            e.printStackTrace();
         }
      }
   }
   ws.logout();
%>