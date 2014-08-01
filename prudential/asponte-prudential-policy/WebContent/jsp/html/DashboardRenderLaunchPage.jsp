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
<%! 
public List<CustomAuthoringItemWrapper> queryByAT(String authTempId, Object portletRequest, Object portletResponse) throws Exception { 

  // get the items and iterate them 
  // set the libraries and limit to drafts 
  CustomAuthoringLaunchPageQueryParams queryParms = new CustomAuthoringLaunchPageQueryParams(); 
  // set the sort to show last mod first 
  queryParms.setModifiedSortActive(true); 
  // set the library selectors 
  ArrayList <Library>libraryList = new ArrayList<Library>(); 
  // get the workspace 
  Workspace ws = Utils.getWorkspace(); 
  ws.useUserAccess(true); 
  // get the libraries I care about 
  Library tempLib = ws.getDocumentLibrary("PruPolicyContent"); 
  libraryList.add(tempLib); 
  
  queryParms.setLibraries(libraryList); 
  // now add the Content selector 
  Selector classesSelector = Selectors.typeIn(Content.class); 
  ArrayList additionalSelectors = new ArrayList(); 
  String[] authTempIds = {authTempId}; 
  for(int i = 0; i < authTempIds.length; ++i) { 
    DocumentId atId = ws.createDocumentId(authTempIds[i]); 
    AuthoringTemplate limitedTemplate = null; 
    if(atId != null) { 
      limitedTemplate = (AuthoringTemplate)ws.getById(atId); 
    } 
    queryParms.setClassesSelector(classesSelector); 
    Selector authTemplateSelector = null; 
    if(limitedTemplate != null) { 
      authTemplateSelector = Selectors.authoringTemplateEquals(limitedTemplate); 
      additionalSelectors.add(authTemplateSelector); 
    } 
    else 
    { 
      //out.println("not able to retrieve authoring template " + authTempIds[i]); 
      // TODO: log error 
    } 
  } 
  
  Query theQuery = CustomAuthoringLaunchPageQueries.buildQuery(additionalSelectors,queryParms); 
  ResultIterator results = CustomAuthoringLaunchPageQueries.runQuery(ws,theQuery,queryParms); 
  String[] additionalAttributes = {""}; 
  // now build the items from the results 
  return CustomAuthoringLaunchPageQueries.wrapResults(results,portletRequest,portletResponse,additionalAttributes,false); 
} 

public Document getDocumentById(Workspace ws, String contentId) throws Exception { 
  DocumentId docId = ws.createDocumentId(contentId); 
  return ws.getById(docId); 
} 

public String escapeText(String text) { 
  
  if(text != null) { 
    text = text.replaceAll("\"", "\\\\\"").replaceAll("'", "&apos;").replaceAll("\n", " "); 
    // text = text.replaceAll("\"", "&quot;").replaceAll("'", "&apos;").replaceAll("\n", " "); 
  } 
  
  return text; 
} 

public String getStandards(Document doc) throws Exception { 
  String text = ""; 
  boolean shouldEscape = true; 
  ContentComponent cmpnt = WCMUtils.getContentComponent(doc, "Standards"); 
  if (cmpnt instanceof RichTextComponent) { 
    text = ((RichTextComponent) cmpnt).getRichText(); 
  } else if (cmpnt instanceof HTMLComponent) { 
          text = ((HTMLComponent)cmpnt).getHTML(); 
  } else if (cmpnt instanceof LinkComponent) { 
    shouldEscape = false; 
    LinkComponent link = (LinkComponent)cmpnt; 
    text = "<a href=\""+link.getURL()+"\">"+link.getLinkText()+"</a>"; 
  } 
  if(shouldEscape) { 
          text = escapeText(text); 
  } 
  return text; 
} 

public String getModelPolicyLinkValue(Document doc) throws Exception { 
  String modelPolicyId = null; 
  ContentComponent cmpnt = WCMUtils.getContentComponent(doc, "ModelPolicy"); 
  if(cmpnt == null) { 
    cmpnt = WCMUtils.getContentComponent(doc, "ModelPolicyLink"); 
  } 
  
  if (cmpnt instanceof LinkComponent) { 
    DocumentId id = ((LinkComponent) cmpnt).getDocumentReference(); 
    if(id != null) { 
      modelPolicyId = id.getId(); 
    } 
  } 
  return modelPolicyId; 
} 

public String getUsers(Document doc, String fieldName) { 
  String text = ""; 
  ContentComponent cmpnt = WCMUtils.getContentComponent(doc, fieldName); 
  
  if (cmpnt instanceof UserSelectionComponent) { 
    java.security.Principal[] selection = ((UserSelectionComponent) cmpnt).getSelections(); 
    
    if(selection != null) { 
      StringBuilder strb = new StringBuilder(); 
      for(int i = 0; i < selection.length; ++i) { 
        strb.append(selection[i].getName()); 
        if(i < selection.length-1) { 
          strb.append(", "); 
        } 
      } 
      text = strb.toString(); 
    } 
  } 
  return escapeText(text); 
} 


public DocumentId getSiteAreaByPath(Workspace ws, String path) { 
        System.out.println("getSiteAreaByPath for "+path);   
        System.out.println("getSiteAreaByPath for ws "+ws); 
        DocumentId returnId = null; 
        DocumentIdIterator itor = ws.findByPath(path, Workspace.WORKFLOWSTATUS_ALL); 
        if(itor.hasNext()) { 
                returnId = itor.nextId(); 
        } 
        System.out.println("getSiteAreaByPath returning =  "+returnId); 
        return returnId; 
} 

%> 
<%@ taglib uri="/WEB-INF/tld/wcm.tld" prefix="wcm" %> 
<%@ taglib uri="/WEB-INF/tld/std-portlet.tld" prefix="portlet" %> 
<portlet:defineObjects/> 
<wcm:initworkspace user="<%= (java.security.Principal)request.getUserPrincipal() %>" /> 

<script>
	if(!window.jQuery) { 
		document.write(unescape('%3Cscript src="//ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"%3E%3C/script%3E'));
		document.write(unescape('%3Clink rel="stylesheet" href="//ajax.googleapis.com/ajax/libs/jqueryui/1.10.4/themes/smoothness/jquery-ui.css"%3E%3C/link%3E'));
		document.write(unescape('%3Cscript src="//ajax.googleapis.com/ajax/libs/jqueryui/1.10.4/jquery-ui.min.js"%3E%3C/script%3E'));
	}
</script>

<% 
  Workspace ws = Utils.getWorkspace(); 
  // set to use read 
  ws.useUserAccess(true); 
  ws.setCurrentDocumentLibrary(ws.getDocumentLibrary("PruPolicyContent")); 
  String bgPolicySubPath = "prupolicycontent/Business+Group+Policies".toLowerCase(); 
  String hrPolicyPath = "prupolicycontent/Corporate+Policies/Content/Human+Resources+Policies".toLowerCase(); 
  String fpaPolicyPath = "prupolicycontent/Corporate+Policies/Content/US+Expense".toLowerCase(); 
  String modelPolicyPath = "prupolicycontent/Corporate+Policies/Content/Model+Policies".toLowerCase(); 
  
  RenderingContext rc = (RenderingContext) pageContext.getRequest().getAttribute(Workspace.WCM_RENDERINGCONTEXT_KEY); 
  Content incoming = rc.getContent(); 
  DocumentId parentId = incoming.getDirectParent(); 
  String wcmContextPath = ws.getPathById(parentId, false, false); 
  boolean isBPA = wcmContextPath.startsWith(bgPolicySubPath); 
  boolean isMPA = wcmContextPath.startsWith(modelPolicyPath); 
  
  DocumentId[] parentIds = null;   
  if(isBPA) { 
        parentIds = new DocumentId[] {parentId, getSiteAreaByPath(ws, modelPolicyPath.replaceAll("\\+", " "))}; 
  } else { 
        parentIds = new DocumentId[] {parentId}; 
  } 
  
  System.out.println("wcmContextPath: " + wcmContextPath); 
  System.out.println("isBPA: " + isBPA); 
  System.out.println("isMPA: " + isMPA); 
  
  // out.println("wcmContextPath: " + wcmContextPath); 
  // out.println("isBPA: " + isBPA); 
  // out.println("isMPA: " + isMPA); 
%> 

<style> 
table{ 
border-collapse:collapse; 
border:1px solid #000; 
} 
table td{ 
border:1px solid #000; 
font-size:11px; 
} 
.gid-actions { 
  font-size:9px; 
  display:none; 
  margin-top:2px; 
} 
.gid-actions.MODEL { 
  display:block; 
} 

.gid-actions label { 
  vertical-align:top; 
  padding:1px; 
  margin-left:2px; 
} 

.gid-actions label:first-of-type { 
        border-right:1px solid black;padding-right:4px;margin-right:8px; 
} 

#grid-accept-all-place-holder label { 
  padding:1px; 
  margin-left:2px; 
} 

.dialog-no-title-bar .ui-dialog-titlebar { 
        display:none; 
} 
</style> 

<style> 
@import "/wps/portal_dojo/v1.7/dojo/resources/dojo.css"; 
@import "/wps/portal_dojo/v1.7/dijit/themes/claro/claro.css"; 
@import "/wps/portal_dojo/v1.7/dojox/grid/enhanced/resources/claro/EnhancedGrid.css"; 
@import "/wps/portal_dojo/v1.7/dojox/grid/enhanced/resources/EnhancedGrid_rtl.css"; 
</style> 
<style> 
#grid { 
    width: 100%; 
    height: 30em; 
    margin:5px 0; 
} 
.hidded { 
  display:none; 
} 
.dojoxGridArrowButtonChar { 
    display: inline; 
    float: right; 
} 
</style> 
<% 
  
  String[] additionalAttributes = {"PolicyOwner"}; 
  // now build the items from the results 
  // get the documentiditerator from the authoring template and site area 
  String[] authTempIds = {"4143aff0-6901-4207-852b-9b2c3d098679"}; 
  String linkATTempId = "9889d022-56d9-4e27-a2be-ded469428e15"; 
  DocumentId atId = null; 
  DocumentId atId2 = ws.createDocumentId(linkATTempId);; 
  for(int i = 0; i < authTempIds.length; ++i) { 
    atId = ws.createDocumentId(authTempIds[i]); 
  } 
  DocumentIdIterator theWrapperResults = ws.contentSearch(atId, parentIds, null, null, Workspace.WORKFLOWSTATUS_DRAFT | Workspace.WORKFLOWSTATUS_PUBLISHED | Workspace.WORKFLOWSTATUS_EXPIRED ); 
  DocumentIdIterator thelinkResults = ws.contentSearch(atId2, parentIds, null, null, Workspace.WORKFLOWSTATUS_DRAFT | Workspace.WORKFLOWSTATUS_PUBLISHED | Workspace.WORKFLOWSTATUS_EXPIRED ); 
  DocumentId[] theResultArray = null; 
  
  List<CustomAuthoringItemWrapper> linkResults = CustomAuthoringLaunchPageQueries.wrapResults(thelinkResults,renderRequest,renderResponse,additionalAttributes,false); 
  List<CustomAuthoringItemWrapper> wrapperResults = CustomAuthoringLaunchPageQueries.wrapResults(theWrapperResults,renderRequest,renderResponse,additionalAttributes,false); 
  SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy"); 
%> 
<link rel="stylesheet" href="//ajax.googleapis.com/ajax/libs/jqueryui/1.10.4/themes/smoothness/jquery-ui.css"></link> 
<script src="//ajax.googleapis.com/ajax/libs/jqueryui/1.10.4/jquery-ui.min.js"></script> 
<script> 
var fullDataset = { 
      identifier: 'itemId', 
    items: [ 
<% 
  int id = 0; 
  for (int i = 0; i < linkResults.size(); ++i, ++id) { 
    CustomAuthoringItemWrapper theWrapper = linkResults.get(i); 
        // get the action URLs 
        CustomAuthoringItemAction previewAction = (CustomAuthoringItemAction)theWrapper.getAction("Preview"); 
        String editURL = previewAction.getActionURL(); 
		
        String curUuid = theWrapper.getItemId(); 
        DocumentId curDocId = ws.createDocumentId(curUuid); 
        Content theContent = (Content) ws.getById(curDocId); 
        editURL="<a href='"+Utils.getPreviewURL(theContent)+"'>"+theContent.getName()+"</a>";
        LinkComponent modelPolicyLinkCmpt = (LinkComponent) theContent.getComponent("ModelPolicyLink"); 
        DocumentId modelPolicyDocId = modelPolicyLinkCmpt.getDocumentReference(); 
        if(modelPolicyDocId != null){ 
          String curModelPolicyPath = ws.getPathById(modelPolicyDocId, true, false); 
          String pathToContent = "wcm%3apath%3a%2F" + curModelPolicyPath; 
          //editURL = "?1dmy&page=com.prudential.page.PP.PolicyDetail&urile=" + pathToContent; 
        } 
		
        // ensure live date isn't null 
        String liveDateFormatted = ""; 
        if(theWrapper.getLiveDate() != null) { 
          liveDateFormatted = formatter.format(theWrapper.getLiveDate()); 
        } // end-if 
        String expireDateFormatted = ""; 
        if(theWrapper.getExpireDate() != null) { 
          expireDateFormatted = formatter.format(theWrapper.getExpireDate()); 
        } // end-if 
    
        String lastModFormatted = ""; 
    if(theWrapper.getLastModDate() != null) { 
      lastModFormatted = formatter.format(theWrapper.getLastModDate()); 
    } // end-if 
    
    String reviewDate = ""; 
    if(theWrapper.getReviewDate() != null) { 
      reviewDate = formatter.format(theWrapper.getReviewDate()); 
    } // end-if 
    else { 
    
    } 
    
    String stage = theWrapper.getWfStage().toLowerCase(); 
    //Document doc = getDocumentById(ws, theWrapper.getItemId()); 
    String retireRationale = ""; 
    if(stage.contains("draft")) { 
      stage = "Draft"; 
    } else if(stage.contains("review")) { 
      stage = "Review"; 
      // if we're in review, get the review date 
      //Content theContent = (Content)doc; 
      Date enteredStage = theContent.getDateEnteredStage(); 
      reviewDate = formatter.format(enteredStage); 
    } else if(stage.contains("approval")) { 
      stage = "Approve"; 
      // if we're in review, get the review date 
      //Content theContent = (Content)doc; 
      Date enteredStage = theContent.getDateEnteredStage(); 
      reviewDate = formatter.format(enteredStage); 
    } else if(stage.contains("publish")) { 
      stage = "Published"; 
    } else if(stage.contains("approveretire")) { 
      stage = "Pending Retire"; 
    } else if(stage.contains("retire content")) { 
      stage = "Retired";     
      //Content theContent = (Content)doc; 
      HistoryLogIterator hli = theContent.getHistoryLog(); 
      ArrayList comments = new ArrayList();
      while(hli.hasNext()) { 
        HistoryLogEntry hle = hli.nextLogEntry(); 
        int code = hle.getCode();
        if(code >= 10000 && code <= 19999)
		{
			comments.add(hle);
		}	             
      } 
      if(!comments.isEmpty()) {
        HistoryLogEntry hle = (HistoryLogEntry)comments.get(comments.size()-1);
      	retireRationale = hle.getMessage(); 
      }
    } else if(stage.contains("expire")) { 
      stage = "Retired"; 
      //Content theContent = (Content)doc; 
      HistoryLogIterator hli = theContent.getHistoryLog(); 
      ArrayList comments = new ArrayList();
      while(hli.hasNext()) { 
        HistoryLogEntry hle = hli.nextLogEntry(); 
        int code = hle.getCode();
        if(code >= 10000 && code <= 19999)
		{
			comments.add(hle);
		}	
      } 
      if(!comments.isEmpty()) {
        HistoryLogEntry hle = (HistoryLogEntry)comments.get(comments.size()-1);
      	retireRationale = hle.getMessage(); 
      }
    } 
    
    
    String modelPolicyId = getModelPolicyLinkValue(theContent); 
    Document parent = getDocumentById(ws, modelPolicyId); 
    String contentPath = theWrapper.getPath().toLowerCase(); 
    String policyType = "LINK"; 
    if(modelPolicyId == null) { 
      modelPolicyId = ""; 
    } 
  
    /* SDD 10863*/
    String _policyOwner=theWrapper.getAdditionalAttribute("PolicyOwner");
	if(_policyOwner==null){_policyOwner="";}
	else{_policyOwner=_policyOwner.replace("\"","\\\"").replaceAll("[\\r\\n]+"," ").split("[|]")[0].trim();}
    %>{ 
    itemId:"<%=id%>", 
    contentId:"<%=theWrapper.getItemId()%>", 
    viewCount:"<wcm:plugin name="RenderReferenceCount" displayCount="true" uuid="<%=theWrapper.getItemId()%>" ></wcm:plugin>", 
    //source:"/wps/wcm/myconnect/dd251369-8346-4d70-a834-21112c85f1a9/link.jpg?MOD=AJPERES&CACHEID=dd251369-8346-4d70-a834-21112c85f1a9&cache=none", 
    source:"Link", 
    type:"<%=policyType%>", 
    authTemp:"<%=theWrapper.getAuthTemplateName()%>", 
    //editURL:"<%=editURL%>", 
    editURL:"<wcm:plugin name="RemoteAction" action="preview" docid="<%=theWrapper.getItemId()%>" ></wcm:plugin>", 
    title:"<%=theWrapper.getTitle()%>", 
    modelPolicyId:"<%= modelPolicyId %>", 
    status:"<%=stage%>", 
    liveDateFormatted:"<%=liveDateFormatted%>", 
    lastModFormatted:"<%=lastModFormatted%>", 
    reviewDate:"<%=reviewDate%>", 
    retiredDate:"<%=expireDateFormatted%>", 
    retireRationale:"<%=retireRationale%>", 
    author:"<%=_policyOwner%>", 
    stage:"<%=stage %>", 
    path:"<%=theWrapper.getPath() %>", 
    standards:"<%= getStandards(parent) %>", 
    reviewers:"", 
    approvers:"", 
    contacts:"" 

    }, 
<% 
    } // end for-loop 
  
  for (int i = 0; i < wrapperResults.size(); ++i, ++id) { 
    CustomAuthoringItemWrapper theWrapper = wrapperResults.get(i); 
        // get the action URLs 
        CustomAuthoringItemAction previewAction = (CustomAuthoringItemAction)theWrapper.getAction("Preview"); 
        //String editURL = previewAction.getActionURL(); 
        // ensure live date isn't null 
        String liveDateFormatted = ""; 
        if(theWrapper.getLiveDate() != null) { 
          liveDateFormatted = formatter.format(theWrapper.getLiveDate()); 
        } // end-if 
        String expireDateFormatted = ""; 
        if(theWrapper.getExpireDate() != null) { 
          expireDateFormatted = formatter.format(theWrapper.getExpireDate()); 
        } // end-if 
    
        String lastModFormatted = ""; 
    if(theWrapper.getLastModDate() != null) { 
      lastModFormatted = formatter.format(theWrapper.getLastModDate()); 
    } // end-if 
    
        String reviewDate = ""; 
    if(theWrapper.getReviewDate() != null) { 
      reviewDate = formatter.format(theWrapper.getReviewDate()); 
    } // end-if 
    
    String stage = theWrapper.getWfStage().toLowerCase(); 
    Document doc = getDocumentById(ws, theWrapper.getItemId()); 
    String editURL="<a href='"+Utils.getPreviewURL(doc)+"'>"+doc.getName()+"</a>";
    Content theContent = (Content)doc; 
    String retireRationale = ""; 
    if(stage.contains("draft")) { 
      stage = "Draft"; 
    } else if(stage.contains("review")) {       
      stage = "Review"; 
      // if we're in review, get the review date 
      Date enteredStage = theContent.getDateEnteredStage(); 
      reviewDate = formatter.format(enteredStage); 
    } else if(stage.contains("approval")) { 
      stage = "Approve"; 
      // if we're in review, get the review date 
      Date enteredStage = theContent.getDateEnteredStage(); 
      reviewDate = formatter.format(enteredStage); 
    } else if(stage.contains("publish")) { 
      stage = "Published"; 
    } else if(stage.contains("approveretire")) { 
      stage = "Pending Retire"; 
    } else if(stage.contains("retire content")) { 
      stage = "Retired";           
      HistoryLogIterator hli = theContent.getHistoryLog(); 
      ArrayList comments = new ArrayList();
      while(hli.hasNext()) { 
        HistoryLogEntry hle = hli.nextLogEntry(); 
        int code = hle.getCode();
        if(code >= 10000 && code <= 19999)
		{
			comments.add(hle);
		}	
      } 
      if(!comments.isEmpty()) {
        HistoryLogEntry hle = (HistoryLogEntry)comments.get(comments.size()-1);
      	retireRationale = hle.getMessage(); 
      }
    } else if(stage.contains("expire")) { 
      stage = "Retired"; 
      HistoryLogIterator hli = theContent.getHistoryLog(); 
      ArrayList comments = new ArrayList();
      while(hli.hasNext()) { 
        HistoryLogEntry hle = hli.nextLogEntry(); 
        int code = hle.getCode();
        if(code >= 10000 && code <= 19999)
		{
			comments.add(hle);
		}	
      } 
      if(!comments.isEmpty()) {
        HistoryLogEntry hle = (HistoryLogEntry)comments.get(comments.size()-1);
      	retireRationale = hle.getMessage(); 
      }
    }   
    
    doc = getDocumentById(ws, theWrapper.getItemId()); 
    String modelPolicyId = getModelPolicyLinkValue(doc); 
    Document parent = null; 
    if(modelPolicyId != null) { 
      parent = getDocumentById(ws, modelPolicyId); 
    } 
    String modelPolicyTitle = ""; 
    if(parent != null) { 
      modelPolicyTitle = parent.getTitle(); 
    } 
    
    String contentPath = theWrapper.getPath().toLowerCase(); 
    String policyType = ""; 
    String sourceIcon = ""; 
    if(contentPath.startsWith(modelPolicyPath)) { 
      policyType = "MODEL"; 
      sourceIcon = "/wps/wcm/myconnect/cf25496c-287d-4812-ac91-2e21d45dd58f/model-policy.jpg?MOD=AJPERES&CACHEID=cf25496c-287d-4812-ac91-2e21d45dd58f&cache=none"; 
    } else if(modelPolicyId == null) { 
      policyType = "NEW"; 
      sourceIcon = "/wps/wcm/myconnect/b7ed5424-1ad4-43da-a717-94c5dea84fea/policy.jpg?MOD=AJPERES&CACHEID=b7ed5424-1ad4-43da-a717-94c5dea84fea&cache=none"; 
    } else { 
      policyType = "COPY"; 
      sourceIcon = "/wps/wcm/myconnect/a1730a69-0971-4f86-9d20-8c8ee494ecdb/copy.jpg?MOD=AJPERES&CACHEID=a1730a69-0971-4f86-9d20-8c8ee494ecdb&cache=none"; 
    } 
    
    if(modelPolicyId == null) { 
      modelPolicyId = ""; 
    } 
  
    /* SDD 10863*/
    String _policyOwner=theWrapper.getAdditionalAttribute("PolicyOwner");
	if(_policyOwner==null){_policyOwner="";}
	else{_policyOwner=_policyOwner.replace("\"","\\\"").replaceAll("[\\r\\n]+"," ").split("[|]")[0].trim();}
    %>{ 
    itemId: <%=id%>, 
    viewCount:"<wcm:plugin name="RenderReferenceCount" displayCount="true" uuid="<%=theWrapper.getItemId()%>" ></wcm:plugin>", 
    contentId:"<%=theWrapper.getItemId()%>", 
    source:"<%=sourceIcon%>", 
    type:"<%=policyType%>", 
    authTemp:"<%=theWrapper.getAuthTemplateName()%>", 
    editURL:"<wcm:plugin name="RemoteAction" action="preview" docid="<%=theWrapper.getItemId()%>" ></wcm:plugin>", 
    title:"<%=theWrapper.getTitle()%>", 
    modelPolicyId:"<%= modelPolicyId %>", 
    status:"<%=stage%>", 
    liveDateFormatted:"<%=liveDateFormatted%>", 
    lastModFormatted:"<%=lastModFormatted%>", 
    reviewDate:"<%=reviewDate%>", 
    retiredDate:"<%=expireDateFormatted%>", 
    retireRationale:"<%=retireRationale%>", 
    author:"<%=_policyOwner%>", 
    stage:"<%=stage %>", 
    path:"<%=theWrapper.getPath() %>", 
    standards:"<%= getStandards(doc) %>", 
    reviewers:"<%= getUsers(doc, "PolicyReviewers") %>", 
    approvers:"<%= getUsers(doc, "PolicyApprovers") %>", 
    contacts:"<%= getUsers(doc, "Contacts") %>", 
    replacement:"<%= modelPolicyTitle %>" 

    }<% 
    if(i < wrapperResults.size()-1) { 
    %>,<% 
    } // end-if 
      } // end for-loop 
%> 
]}; 

fullDataset.items.sort(function(a, b) { 
        return a.title < b.title ? -1 : a.title > b.title; 
}); 

dojo.require("dojox.grid.EnhancedGrid"); 
dojo.require("dojox.grid.enhanced.plugins.Pagination"); 
dojo.require("dojo.data.ItemFileWriteStore"); 
/* SDD */
dojo.require("dojo.date.locale");
/* /SDD */

var gridVisibility = { 
  "all":[true,true,true,true,true,true,false,true], 
  "Draft":[true,true,true,false,false,false,false,true], 
  "Review":[true,true,true,true,true,true,false,true], 
  "Approve":[true,true,true,true,true,true,false,true], 
  "Published":[true,true,true,true,true,true,false,true], 
  "Pending Retire":[true,true,true,false,false,false,true,true], 
  "Retired":[true,true,true,false,false,false,true,true], 
  "mdlplcs":[true,true,true,true,true,true,false,true] 
}; 

var updateGridColumns = function() { 
  var filterValue = getFilterValue(); 
  columnVsblty = gridVisibility[filterValue]; 
  for(var i = 0; i < columnVsblty.length; ++i) { 
    grid.layout.setColumnVisibility(i, columnVsblty[i]); 
  } 
} 

var getFilterValue = function() { 
  var ddList = dojo.byId("status-filter-value"); 
  return ddList.options[ddList.selectedIndex].value; 
}; 

var filterData = function(fullDataset) { 
  var filterValue = getFilterValue(); 
  var data = { 
    identifier: 'itemId', 
    items: [] 
  }; 

  // if(filterValue === "all") { 
    // data.items = fullDataset.items.slice(0); 
  // } else if(filterValue === "mdlplcs") { 
    // for(var i = 0; i < fullDataset.items.length; ++i) { 
      // if(fullDataset.items[i].type == "MODEL") { 
        // data.items.push(fullDataset.items[i]); 
      // } 
    // } 
  // } else { 
    // for(var i = 0; i < fullDataset.items.length; ++i) { 
      // if(fullDataset.items[i].status == filterValue) { 
        // data.items.push(fullDataset.items[i]); 
      // } 
    // } 
  // } 
<% if(isBPA) { %> 
  if(filterValue === "mdlplcs") { 
    for(var i = 0; i < fullDataset.items.length; ++i) {     
      if(fullDataset.items[i].type == "MODEL" && (fullDataset.items[i].status == "Published" || fullDataset.items[i].status == "Pending Retire" )) { 
        data.items.push(fullDataset.items[i]); 
      } 
      // only include model policies
      /**
      else if(fullDataset.items[i].type != "MODEL") { 
        data.items.push(fullDataset.items[i]); 
          } 
          */
    } 
  } else if(filterValue === "all") { 
    for(var i = 0; i < fullDataset.items.length; ++i) { 
      if(fullDataset.items[i].type != "MODEL") { 
        data.items.push(fullDataset.items[i]); 
      } 
    } 
  } else { 
    for(var i = 0; i < fullDataset.items.length; ++i) { 
      if(fullDataset.items[i].type != "MODEL" && fullDataset.items[i].status == filterValue) { 
        data.items.push(fullDataset.items[i]); 
      } 
    } 
  } 
<% } else { %> 
  if(filterValue === "all") { 
    data.items = fullDataset.items.slice(0); 
  } else { 
    for(var i = 0; i < fullDataset.items.length; ++i) { 
      if(fullDataset.items[i].status == filterValue) { 
        data.items.push(fullDataset.items[i]); 
      } 
    } 
  } 
<% } %> 
  
  var titleFilter = getTitleFilterValue().toLowerCase(); 
  if(titleFilter.length > 0) { 
    for(var i = data.items.length -1; i >= 0; --i) { 
      var title = (""+data.items[i].title).toLowerCase(); 
      if(title.indexOf(titleFilter) < 0) { 
        data.items.splice(i, 1); 
      } 
    } 
  } 
  
  return data; 
}; 
/* SDD */
var _policyDateFormat={formatLength:'short', selector:'date', locale:'en-us'};
var _policyDateCompare=function(a, b){
	var ret = 0;
	var dateA = dojo.date.locale.parse(a,_policyDateFormat);
	var dateB=dojo.date.locale.parse(b,_policyDateFormat);
	if(dateA>dateB){ret=1;}
	else if(dateA<dateB){ret=-1;}
	return ret;
};
/* /SDD */
var store = null; 
var activeDataSet = null; 
var grid = null; 



var gridVisibility = { 
  "all":[<%= isBPA %>,true,true,false,true,true,false,false,false,false,false,false,false,false,true,true,true,false,<%= isMPA %>], 
  "Draft":[<%= isBPA %>,true,true,false,false,true,true,false,false,false,false,false,false,false,true,true,true,false,<%= isMPA %>], 
  "Review":[<%= isBPA %>,true,true,true,false,false,false,false,false,false,true,false,true,false,true,true,true,false,<%= isMPA %>], 
  "Approve":[<%= isBPA %>,true,true,true,false,false,false,false,false,true,false,true,true,true,true,true,true,false,<%= isMPA %>], 
  "Published":[<%= isBPA %>,true,true,false,true,false,false,false,false,false,false,false,false,false,true,true,true,false,<%= isMPA %>], 
  "Pending Retire":[<%= isBPA %>,true,true,false,true,false,false,true,true,false,false,false,false,false,true,true,false,true,<%= isMPA %>], 
  "Retired":[<%= isBPA %>,true,true,false,true,false,false,true,true,false,false,false,false,false,true,true,false,true,<%= isMPA %>], 
  "mdlplcs":[<%= isBPA %>,true,true,false,true,true,false,false,false,false,false,false,false,false,true,true,true,false,<%= isMPA %>] 
}; 

var renderTable = function() { 
  var data = filterData(fullDataset); 
    /*set up data store*/ 
    store = new dojo.data.ItemFileWriteStore({data: data}); 
    	/* SDD */
	// Define the comparator function for dates.
    store.comparatorMap = {};
    store.comparatorMap["liveDateFormatted"] = _policyDateCompare;
    store.comparatorMap["lastModFormatted"] = _policyDateCompare;
	/* /SDD */
  activeDataSet = data; 
    /*set up layout*/ 
    var layout = [ 
      {name: 'Source', noresize: true, field: 'source', width: "100px", formatter: function(value, index) { 
    //return "<img alt='" + grid.getItem(index).type + "' height='20px' src='" + value + "'><div class='gid-actions "+ grid.getItem(index).type +"'><input id='accept-"+grid.getItem(index).itemId+"' name='accept-copy-"+grid.getItem(index).itemId+"' type='checkbox' value='accept' 0nClick=\"getElementById('copy-"+grid.getItem(index).itemId+"').checked=false;\" /><label for='accept-"+grid.getItem(index).itemId+"'>Adopt</label><input id='copy-"+grid.getItem(index).itemId+"' name='accept-copy-"+grid.getItem(index).itemId+"' type='checkbox' value='copy' 0nClick=\"getElementById('accept-"+grid.getItem(index).itemId+"').checked=false;\" /><label for='copy-"+grid.getItem(index).itemId+"'>Copy</label></div>"; 
    return "<span height='20px'>"+grid.getItem(index).type+"</span><div class='gid-actions "+ grid.getItem(index).type +"'><input id='accept-"+grid.getItem(index).itemId+"' name='accept-copy-"+grid.getItem(index).itemId+"' type='checkbox' value='accept' 0nClick=\"getElementById('copy-"+grid.getItem(index).itemId+"').checked=false;\" /><label for='accept-"+grid.getItem(index).itemId+"'>Adopt</label><input id='copy-"+grid.getItem(index).itemId+"' name='accept-copy-"+grid.getItem(index).itemId+"' type='checkbox' value='copy' 0nClick=\"getElementById('accept-"+grid.getItem(index).itemId+"').checked=false;\" /><label for='copy-"+grid.getItem(index).itemId+"'>Copy</label></div>"; 
    }}, 
      {name: 'Policy', noresize: true, field: 'title', width: "200px", formatter: function(value, index) { 
    return "<a href='"+ grid.getItem(index).editURL + "' target='_blank' >" + value + "</a>"; 
    }}, 
      {name: 'Status', noresize: true, field: 'status', width: "60px"}, 
      {name: 'Date Submitted for Review', noresize: true, field: 'reviewDate', width: "70px"}, 
      {name: 'Publish Date', noresize: true, field: 'liveDateFormatted', width: "70px"}, 
      {name: 'Last Review Date', noresize: true, field: 'lastModFormatted', width: "70px"}, 
      {name: 'Scheduled Review Date', noresize: true, field: 'reviewDate', width: "70px"}, 
      {name: 'Retired Date', noresize: true, field: 'retiredDate', width: "70px"}, 
      {name: 'Rationale For Retirement', noresize: true, field: 'retireRationale', width: "120px"}, 
      {name: 'Submitter', noresize: true, field: 'reviewers', width: "120px"}, 
      {name: 'Reviewers', noresize: true, field: 'reviewers', width: "120px"}, 
      {name: 'Approvers', noresize: true, field: 'approvers', width: "120px"}, 
      {name: 'Reviewer Completion Date', noresize: true, field: 'reviewDate', width: "120px"}, 
      {name: 'Date Approved', noresize: true, field: 'reviewDate', width: "120px"}, 
      {name: 'Owner', noresize: true, field: 'author', width: "120px"}, 
      {name: 'Policy Contacts', noresize: true, field: 'contacts', width: "120px"}, 
    {name: 'Standards', noresize: true, field: 'itemId', width: "200px", formatter: function(value, index) { 
    return grid.getItem(index).standards; 
    }}, 
      {name: 'Policy Replacement', noresize: true, field: 'replacement', width: "120px"}, 
      {name: 'Adopt/Copy', noresize: true, field: 'viewCount', width: "120px"}, 
    ]; 

    /*create a new grid:*/ 
    grid = new dojox.grid.EnhancedGrid({ 
        id: 'grid', 
        store: store, 
        structure: layout, 
        rowSelector: '20px', 
        plugins: { 
          pagination: { 
              pageSizes: ["25", "50", "100", "All"], 
        defaultPageSize: 100, 
              description: true, 
              sizeSwitch: true, 
              pageStepper: true, 
              gotoButton: true, 
                      /*page step to be displayed*/ 
              maxPageStep: 4, 
                      /*position of the pagination bar*/ 
              position: "bottom" 
          } 
        } 
    }, "gridDiv"); 

    /*append the new grid to the div*/ 
    //dojo.byId("gridDiv").appendChild(grid.domNode); 

    /*Call startup() to render the grid*/ 
    grid.startup(); 
  updateGridColumns(); 
}; 

var refreshData = function() { 
  // for(var i = 0; i < fullDataset.items.length; ++i) { 
    // store.deleteItem(fullDataset.items[i]); 
  // } 
  grid.setStore(); 
  
  var data = filterData(fullDataset); 
  activeDataSet = data; 
  store = new dojo.data.ItemFileWriteStore({data: data}); 
  grid.setStore(store); 
  updateGridColumns(); 
}; 

var getItemById = function(itemId) { 
  for(var i = 0; i < activeDataSet.items.length; ++i) { 
    if(activeDataSet.items[i].itemId == itemId) { 
      return activeDataSet.items[i]; 
    } 
  } 
  return null; 
}; 


var contains = function(a, obj) { 
    for (var i = 0; i < a.length; i++) { 
        if (a[i] === obj) { 
            return true; 
        } 
    } 
    return false; 
} 


dojo.ready(function(){ 
  var modelPolicyIds = []; 
  for(var i = 0; i < fullDataset.items.length; ++i) { 
    if(fullDataset.items[i].modelPolicyId.length > 0) { 
      modelPolicyIds.push(fullDataset.items[i].modelPolicyId); 
    } 
  } 
  
  var purgedDataList = []; 
  for(var i = 0; i < fullDataset.items.length; ++i) { 
    if(!contains(modelPolicyIds, fullDataset.items[i].contentId)) { 
      purgedDataList.push(fullDataset.items[i]); 
    } 
  } 
  
  fullDataset.items = purgedDataList; 
  
    renderTable(); 
}); 

var filterUpdateTimer = null; 
var titleFilterChanged = function() { 
  if(filterUpdateTimer != null) { 
    clearTimeout(filterUpdateTimer); 
    filterUpdateTimer = null; 
  } 
  filterUpdateTimer = setTimeout(refreshTitleFilter, 500); 
}; 
var getTitleFilterValue = function () { 
  return document.getElementById("title-filter").value; 
}; 
var refreshTitleFilter = function() { 
  filterUpdateTimer = null; 
  refreshData(); 
}; 

var gridProcess = function() { 
  var targetPath = "<%= wcmContextPath %>"; 
  var accept = []; 
  var copy = []; 
  //var inputs = document.getElementsByTagName("INPUT"); 
  var inputs = jQuery("INPUT"); 
  console.log(inputs); 
  
  for(var i = 0; i < inputs.length; ++i) { 
    var name = inputs[i].getAttribute("name"); 
    //if(name && name.startsWith("accept-copy-") && inputs[i].checked) { 
    if(name && name.indexOf("accept-copy-")==0 && inputs[i].checked) { 
      var itemId = parseInt(name.substring(name.lastIndexOf("-")+1, name.length)); 
      var item = getItemById(itemId); 
      if(item) { 
        if(inputs[i].value == "accept") { 
          accept.push(item.contentId); 
        } else { 
          copy.push(item.contentId); 
        } 
      } 
    } 
  } 
  
  if(accept.length > 0 || copy.length > 0) { 
        try { 
                //  jquery ui might not be loaded so enclose in try catch 
                jQuery( "#processing-dlg" ).dialog("open"); 
        } catch(ex) {} 
    jQuery.get("/wps/wcm/myconnect/prupolicydesign/jspassets/processpoliciesjsp", {"pp-copy":copy.join(), "pp-link":accept.join(), "targetPath":targetPath}, function(){ 
                jQuery("#processing-dlg .progress-label").html("Processing completed, reloading page."); 
                location.reload(); 
        }); 
  } 
}; 

var gridAdoptAll = function() { 
  var adoptAll = jQuery(this).prop("checked"); 
  jQuery("#grid input[value='accept']").prop("checked", adoptAll); 
  jQuery("#grid input[value='copy']").prop("checked", false); 
}; 

var updateButtons = function() { 
        jQuery("#grid-accept-all").prop("checked", false); 
        
        var modelPoliciesShown = jQuery("#status-filter-value").val() == "mdlplcs"; 
        if(modelPoliciesShown) { 
                jQuery("#grid-accept-all-place-holder > *, #grid-process-place-holder > *").show(); 
        } else { 
                jQuery("#grid-accept-all-place-holder > *, #grid-process-place-holder > *").hide(); 
        } 
}; 

jQuery(function(){ 
        jQuery("#title-filter-bttn").click(refreshData); 
        jQuery("#status-filter-value").change(function(){ 
                refreshData(); 
                updateButtons(); 
        }); 
        jQuery("#grid-accept-all").click(gridAdoptAll); 
        jQuery("#grid-process-accept-copy").click(gridProcess); 
}); 
  </script> 
<br> 
<div style="float:left; margin-left:<%= (isBPA? "130px": "20px") %>"> 
  <input type="text" id="title-filter" style="width:160px; padding:2px"/> <button id="title-filter-bttn" type="button" style="padding:0">Filter</button> 
</div> 
<select name="status-filter-value" id="status-filter-value" style="float:right"> 
  <option value="all">All</option> 
  <option value="Draft">Draft</option> 
  <option value="Review">Review</option> 
  <option value="Approve">Approve</option> 
  <option value="Published">Published</option> 
  <option value="Pending Retire">Pending Retire</option> 
  <option value="Retired">Retired</option> 
  <% if(isBPA) { %> 
  <option value="mdlplcs">Available Model Policies</option> 
  <% } %> 
</select> 


<div id='grid-accept-all-place-holder' style="height:20px; margin:30px 0 0 30px;"> 
<% if(isBPA) { %> 
<input id='grid-accept-all' type='checkbox' value='accept-all'/><label for='grid-accept-all'>Adopt All</label> 
<% } %> 
</div> 

<div id="gridDiv" style="padding:15px 0 0"></div> 

<div id='grid-process-place-holder' style="height:30px;"> 
<% if(isBPA) { %> 
<button id="grid-process-accept-copy" type="button">Process</button> 
<% } %> 
</div> 


<script> 
        jQuery(function() { 
                //  jquery ui might not be loaded so enclose in try catch 
                try { 
                        jQuery("#processing-dlg .progressbar").progressbar({value: false}); 
                        jQuery( "#processing-dlg" ).dialog({ 
                                dialogClass: 'dialog-no-title-bar', 
                                autoOpen: false, 
                                height: 70, 
                                modal: true 
                        }); 
                } catch(ex) {} 
        }); 
</script> 

<div id="processing-dlg" style="display:none"> 
        <div class="progress-label">Processing, please wait...</div> 
        <div class="progressbar"></div> 
</div> 

<script> 
        // this code is left outside of a document ready block because it needs to run before everything else that runs on document ready 
        // it is placed here at the bottom of the page so the needed elements are loaded into the DOM when it runs 
        jQuery("#status-filter-value").each(function(){ 
                var selectedFilterValue = "all"; 
                jQuery(this).val(selectedFilterValue); 
        }); 
        updateButtons(); 
</script>