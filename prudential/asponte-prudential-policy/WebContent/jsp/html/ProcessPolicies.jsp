<%@ page
	import="org.apache.jetspeed.portlet.PortletURI, 
                 com.ibm.workplace.wcm.api.*, 
                 com.ibm.workplace.wcm.api.query.*, 
                 java.util.*, 
                 com.prudential.utils.*, 
                 com.prudential.wcm.*, 
                 java.text.*, 
                 java.security.Principal,
                 java.util.logging.*, 
                 com.prudential.authoring.launchpage.*"%>



<%!public String[] getParameterValues(javax.servlet.ServletRequest request, String name) {
      Logger s_log = Logger.getLogger("com.prudential.JSP");
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.log(Level.FINEST, "getParameterValues for " + name);
      }

      String[] rawValues = request.getParameterValues(name);
      java.util.LinkedList<String> values = new java.util.LinkedList<String>();
      if (rawValues != null) {
         for (int i = 0; i < rawValues.length; ++i) {
            if (isDebug) {
               s_log.log(Level.FINEST, "getParameterValues rawValue " + rawValues[i]);
            }

            values.addAll(java.util.Arrays.asList(String.valueOf(rawValues[i]).split(",")));
         }
      }

      if (isDebug) {
         s_log.log(Level.FINEST, "getParameterValues values " + values);
      }

      return (String[]) values.toArray(new String[values.size()]);
   }

   public DocumentId getSiteAreaByPath(Workspace ws, String path) {
      Logger s_log = Logger.getLogger("com.prudential.JSP");
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.log(Level.FINEST, "getSiteAreaByPath for " + path);
         s_log.log(Level.FINEST, "getSiteAreaByPath for ws " + ws);
      }

      DocumentId returnId = null;
      DocumentIdIterator itor = ws.findByPath(path, Workspace.WORKFLOWSTATUS_ALL);
      if (itor.hasNext()) {
         returnId = itor.nextId();
      }
      if (isDebug) {
         s_log.log(Level.FINEST, "getSiteAreaByPath returning =  " + returnId);
      }
      return returnId;
   }

   public void setModelPolicyLinkValue(Document doc, DocumentId modelPolicyId) throws Exception {
      Logger s_log = Logger.getLogger("com.prudential.JSP");
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      String cmpntName = "ModelPolicy";
      ContentComponent cmpnt = WCMUtils.getContentComponent(doc, cmpntName);
      if (cmpnt == null) {
         cmpntName = "ModelPolicyLink";
         cmpnt = WCMUtils.getContentComponent(doc, cmpntName);
      }
      if (isDebug) {
         s_log.log(Level.FINEST, "ModelPolicy ContentComponent: " + cmpnt);
      }
      if (cmpnt instanceof LinkComponent) {
         if (isDebug) {
            s_log.log(Level.FINEST, "Setting ModelPolicy Link to: " + modelPolicyId);
         }
         ((LinkComponent) cmpnt).setDocumentReference(modelPolicyId);
         ((ContentComponentContainer) doc).setComponent(cmpntName, cmpnt);
      }
      else {
         if (isDebug) {
            s_log.log(Level.FINEST, "Setting ModelPolicy failed");
         }
      }
   }%>

<%
   Logger s_log = Logger.getLogger("com.prudential.JSP");
   boolean isDebug = s_log.isLoggable(Level.FINEST);
   Workspace wsUser = Utils.getWorkspace();
   wsUser.useDistinguishedNames(true);
   String userName = wsUser.getUserProfile().getUsername();
   Workspace ws = Utils.getSystemWorkspace();
   ws.useDistinguishedNames(true);
   String[] authors = {userName};
   if (isDebug) {
      s_log.log(Level.FINEST, "adding author/owner " + authors[0]);
   }
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
   for (int x = 0; x < copyContent.length; x++) {
      //for(String item: copyContent) { 
      String item = copyContent[x];

      if (item != null && item.trim().length() > 0) {
         try {

            DocumentId docId = ws.createDocumentId(item);
            if (docId != null) {
               if (isDebug) {
                  s_log.log(Level.FINEST, "processing copy " + item);
                  s_log.log(Level.FINEST, "Copy Policy ID: " + docId + ", to path ID: " + pathId);
               }

               Content doc = (Content) ws.copy(docId, pathId);
               doc.setTitle(doc.getTitle() + " (copied)");
               setModelPolicyLinkValue(doc, docId);
               // set content to draft by restarting workflow 
               doc.restartWorkflow();
               doc.addHistoryLogEntry("Content copied by user " + userName);
               doc.setEffectiveDate(new Date());
               if (isDebug) {
                  s_log.log(Level.FINEST, "before add authors");
                  String[] previousAuthors = doc.getAuthors();
                  String[] previousOwners = doc.getOwners();
                  for (int a = 0; a < previousAuthors.length; a++) {
                     s_log.log(Level.FINEST, "authors contains " + previousAuthors[a]);
                  }
                  for (int b = 0; b < previousOwners.length; b++) {
                     s_log.log(Level.FINEST, "owners contains " + previousOwners[b]);
                  }
               }
               doc.addAuthors(authors);
               doc.addOwners(authors);
               if (isDebug) {
                  s_log.log(Level.FINEST, "after add authors");
                  String[] previousAuthors = doc.getAuthors();
                  String[] previousOwners = doc.getOwners();
                  for (int a = 0; a < previousAuthors.length; a++) {
                     s_log.log(Level.FINEST, "authors contains " + previousAuthors[a]);
                  }
                  for (int b = 0; b < previousOwners.length; b++) {
                     s_log.log(Level.FINEST, "owners contains " + previousOwners[b]);
                  }
               }

               System.out.println("Saving: " + doc);
               String[] errors = ws.save(doc);
               if (errors != null && errors.length > 0) {
                  if (isDebug) {
                     s_log.log(Level.FINEST, "Error Count: " + errors.length);
                     s_log.log(Level.FINEST, "Copy Policy ID: " + docId + ", to path ID: " + pathId);
                  }

                  for (String errMsg : errors) {
                     s_log.log(Level.FINEST, "Error: " + errMsg);
                  }
               }
               else {
                  if (isDebug) {
                     s_log.log(Level.FINEST, "Save returned no errors");
                  }
               }
               doc.nextWorkflowStage();
            }

         }
         catch (Exception e) {
            e.printStackTrace();
         }
      }
   }

   DocumentId atId = ws.createDocumentId("9889d022-56d9-4e27-a2be-ded469428e15");
   for (int y = 0; y < linkContent.length; y++) {
      //for(String item: linkContent) { 
      String item = linkContent[y];
      if (item != null && item.trim().length() > 0) {
         try {
            DocumentId docId = ws.createDocumentId(item);
            if (docId != null) {
               if (isDebug) {
                  s_log.log(Level.FINEST, "processing link " + item);
                  s_log.log(Level.FINEST, "Creating Policy Link of: " + docId + ", in path ID: " + pathId);
               }
               Document parent = ws.getById(docId);
               Content doc = (Content) ws.createContent(atId, pathId, null, ChildPosition.END);
               doc.setName(parent.getName());
               doc.setTitle(parent.getTitle() + " (adopted)");
               doc.setDescription(parent.getDescription());
               doc.addAuthors(authors);
               setModelPolicyLinkValue(doc, docId);
               // do not restart wf or issue will occur. 
               //doc.restartWorkflow(); 

               doc.setEffectiveDate(new Date());

               doc.addHistoryLogEntry("Content Adopted by user " + userName);
               // we need to set the PolicyApprover
               // get from the parent site area 
               String[] errors = ws.save(doc);
               if (errors != null && errors.length > 0) {
                  if (isDebug) {
                     s_log.log(Level.FINEST, "Error Count: " + errors.length);
                     s_log.log(Level.FINEST, "Copy Policy ID: " + docId + ", to path ID: " + pathId);

                     for (String errMsg : errors) {
                        s_log.log(Level.FINEST, "Error: " + errMsg);
                     }
                  }
               }
               else {
                  if (isDebug) {
                     s_log.log(Level.FINEST, "Save returned no errors");
                  }
               }

               // after save, nextstage it 
               // we need to set the PolicyApprover
               // get from the parent site area 
               Content theContent = (Content) ws.getById(doc.getId());
               String componentName = "PolicyApprovers";
               String componentNameSiteArea = "DefaultPolicyApprover";
               SiteArea parentSite = (SiteArea) ws.getById(pathId);
               if (parentSite.hasComponent(componentNameSiteArea)) {
                  if (isDebug) {
                     s_log.log(Level.FINEST, "Pull value for " + componentNameSiteArea + " from sitearea " + parentSite.getName());
                  }
                  UserSelectionComponent usc = (UserSelectionComponent) parentSite.getComponent(componentNameSiteArea);
                  Principal[] values = usc.getSelections();
                  if (theContent.hasComponent(componentName)) {
                     if (isDebug) {
                        s_log.log(Level.FINEST, "content has component, use the principals to set the value");
                        for (int x = 0; x < values.length; x++) {
                           s_log.log(Level.FINEST, "values contains " + values[x]);
                        }

                     }
                     UserSelectionComponent contentusc = (UserSelectionComponent) theContent.getComponent(componentName);
                     contentusc.setSelections(values);
                     theContent.setComponent(componentName, contentusc);
                     // save the content
                     String[] errors2 = ws.save(theContent);
                     if (errors2 != null && errors2.length > 0) {
                        if (isDebug) {
                           s_log.log(Level.FINEST, "Error Count: " + errors2.length);
                           for (String errMsg : errors2) {
                              s_log.log(Level.FINEST, "Error: " + errMsg);
                           }
                        }
                     }
                     else {
                        if (isDebug) {
                           s_log.log(Level.FINEST, "Save returned no errors");
                        }
                     }
                  }
                  else {
                     if (isDebug) {
                        s_log.log(Level.FINEST, "content does not have " + componentName);
                     }
                  }

               }
               else {
                  if (isDebug) {
                     s_log.log(Level.FINEST, "sitearea does not have " + componentNameSiteArea);
                  }
               }

               // check to see if the parent site area has 
               //doc.nextWorkflowStage(); 
            }
         }
         catch (Exception e) {
            e.printStackTrace();
         }
      }
   }
   ws.logout();
%>