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
                 com.prudential.authoring.launchpage.*"%>
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
	String[] additionalAttributes = {"Issuing Orgainization"};
	// now build the items from the results
	return CustomAuthoringLaunchPageQueries.wrapResults(results,portletRequest,portletResponse,additionalAttributes,false);
}

public Document getDocumentById(Workspace ws, String contentId) throws Exception {
	DocumentId docId = ws.createDocumentId(contentId);
	return ws.getById(docId);
}

public String escapeText(String text) {
	if(text != null) {
		text = text.replaceAll("\"", "&quot;").replaceAll("'", "&apos;").replaceAll("\n", " ");
	}
	return text;
}

public String getStandards(Document doc) throws Exception {
	String text = "";
	ContentComponent cmpnt = WCMUtils.getContentComponent(doc, "Standards");
	System.out.println("Standards ContentComponent: " + cmpnt);
	if (cmpnt instanceof RichTextComponent) {
		text = ((RichTextComponent) cmpnt).getRichText();
	}
	return escapeText(text);
}

public String getModelPolicyLinkValue(Document doc) throws Exception {
	String modelPolicyId = null;
	ContentComponent cmpnt = WCMUtils.getContentComponent(doc, "ModelPolicy");
	if(cmpnt == null) {
		cmpnt = WCMUtils.getContentComponent(doc, "ModelPolicyLink");
	}
	System.out.println("ModelPolicy ContentComponent: " + cmpnt);
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
	System.out.println(fieldName+" ContentComponent: " + cmpnt);
	if (cmpnt instanceof UserSelectionComponent) {
		java.security.Principal[] selection = ((UserSelectionComponent) cmpnt).getSelections();
		System.out.println(fieldName+" Principal Array: " + selection);
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

%>
<%@ taglib uri="/WEB-INF/tld/wcm.tld" prefix="wcm" %>
<%@ taglib uri="/WEB-INF/tld/portlet.tld" prefix="portletAPI" %>
<portletAPI:init/>

<wcm:initworkspace user="<%= (java.security.Principal)request.getUserPrincipal() %>" />

<script>window.jQuery || document.write(unescape('%3Cscript src="http://jqueryui.com/jquery-wp-content/themes/jquery/js/jquery-1.9.1.min.js"%3E%3C/script%3E'))</script>

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
    margin-top:50px;
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
	Workspace ws = Utils.getWorkspace();
	ws.setCurrentDocumentLibrary(ws.getDocumentLibrary("PruPolicyContent"));
	
	String modelPolicyPath = "prupolicycontent/Corporate+Policies/Content/Model+Policies".toLowerCase();
	List<CustomAuthoringItemWrapper> linkResults = queryByAT("9889d022-56d9-4e27-a2be-ded469428e15", portletRequest, portletResponse);
	List<CustomAuthoringItemWrapper> wrapperResults = queryByAT("4143aff0-6901-4207-852b-9b2c3d098679", portletRequest, portletResponse);
	SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
%>
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
        // ensure live date isn't null
        String liveDateFormatted = "";
        if(theWrapper.getLiveDate() != null) {
        	liveDateFormatted = formatter.format(theWrapper.getLiveDate());
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
		
		if(stage.contains("draft")) {
			stage = "Draft";
		} else if(stage.contains("review")) {
			stage = "Review";
		} else if(stage.contains("approval")) {
			stage = "Approve";
		} else if(stage.contains("publish")) {
			stage = "Published";
		} else if(stage.contains("retire")) {
			stage = "Retired";
		}
		
		Document doc = getDocumentById(ws, theWrapper.getItemId());
		String modelPolicyId = getModelPolicyLinkValue(doc);
		Document parent = getDocumentById(ws, modelPolicyId);
		String contentPath = theWrapper.getPath().toLowerCase();
		String policyType = "LINK";
		if(modelPolicyId == null) {
			modelPolicyId = "";
		}
	
		%>{
		itemId:"<%=id%>",
		contentId:"<%=theWrapper.getItemId()%>",
		source:"/wps/wcm/myconnect/prudential/dd251369-8346-4d70-a834-21112c85f1a9/link.jpg?MOD=AJPERES&CACHEID=dd251369-8346-4d70-a834-21112c85f1a9&cache=none",
		type:"<%=policyType%>",
		authTemp:"<%=theWrapper.getAuthTemplateName()%>",
		editURL:"<%=editURL%>",
		title:"<%=theWrapper.getTitle()%>",
		modelPolicyId:"<%= modelPolicyId %>",
		status:"<%=stage%>",
		editURL:"<%=editURL%>",
		liveDateFormatted:"<%=liveDateFormatted%>",
		lastModFormatted:"<%=lastModFormatted%>",
		reviewDate:"<%=reviewDate%>",
		retiredDate:"",
		author:"<%=theWrapper.getAuthor()%>",
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
        String editURL = previewAction.getActionURL();
        // ensure live date isn't null
        String liveDateFormatted = "";
        if(theWrapper.getLiveDate() != null) {
        	liveDateFormatted = formatter.format(theWrapper.getLiveDate());
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
		
		if(stage.contains("draft")) {
			stage = "Draft";
		} else if(stage.contains("review")) {
			stage = "Review";
		} else if(stage.contains("approval")) {
			stage = "Approve";
		} else if(stage.contains("publish")) {
			stage = "Published";
		} else if(stage.contains("retire")) {
			stage = "Retired";
		}
		
		Document doc = getDocumentById(ws, theWrapper.getItemId());
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
			sourceIcon = "/wps/wcm/myconnect/prudential/cf25496c-287d-4812-ac91-2e21d45dd58f/model-policy.jpg?MOD=AJPERES&CACHEID=cf25496c-287d-4812-ac91-2e21d45dd58f&cache=none";
		} else if(modelPolicyId == null) {
			policyType = "NEW";
			sourceIcon = "/wps/wcm/myconnect/prudential/b7ed5424-1ad4-43da-a717-94c5dea84fea/policy.jpg?MOD=AJPERES&CACHEID=b7ed5424-1ad4-43da-a717-94c5dea84fea&cache=none";
		} else {
			policyType = "COPY";
			sourceIcon = "/wps/wcm/myconnect/prudential/a1730a69-0971-4f86-9d20-8c8ee494ecdb/copy.jpg?MOD=AJPERES&CACHEID=a1730a69-0971-4f86-9d20-8c8ee494ecdb&cache=none";
		}
		
		if(modelPolicyId == null) {
			modelPolicyId = "";
		}
	
		%>{
		itemId: <%=id%>,
		contentId:"<%=theWrapper.getItemId()%>",
		source:"<%=sourceIcon%>",
		type:"<%=policyType%>",
		authTemp:"<%=theWrapper.getAuthTemplateName()%>",
		editURL:"<%=editURL%>",
		title:"<%=theWrapper.getTitle()%>",
		modelPolicyId:"<%= modelPolicyId %>",
		status:"<%=stage%>",
		editURL:"<%=editURL%>",
		liveDateFormatted:"<%=liveDateFormatted%>",
		lastModFormatted:"<%=lastModFormatted%>",
		reviewDate:"<%=reviewDate%>",
		retiredDate:"",
		author:"<%=theWrapper.getAuthor()%>",
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

dojo.require("dojox.grid.EnhancedGrid");
dojo.require("dojox.grid.enhanced.plugins.Pagination");
dojo.require("dojo.data.ItemFileWriteStore");

var gridVisibility = {
	"all":[true,true,true,true,true,true,false,true],
	"Draft":[true,true,true,false,false,false,false,true],
	"Review":[true,true,true,true,true,true,false,true],
	"Approve":[true,true,true,true,true,true,false,true],
	"Published":[true,true,true,true,true,true,false,true],
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
	
	if(filterValue === "all") {
		data.items = fullDataset.items.slice(0);
	} else if(filterValue === "mdlplcs") {
		for(var i = 0; i < fullDataset.items.length; ++i) {
			if(fullDataset.items[i].type == "MODEL") {
				data.items.push(fullDataset.items[i]);
			}
		}
	} else {
		for(var i = 0; i < fullDataset.items.length; ++i) {
			if(fullDataset.items[i].status == filterValue) {
				data.items.push(fullDataset.items[i]);
			}
		}
	}
	
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

var store = null;
var activeDataSet = null;
var grid = null;



var gridVisibility = {
	"all":[true,true,true,false,true,true,false,false,false,false,false,false,false,false,true,true,true,false],
	"Draft":[true,true,false,false,false,true,true,false,false,false,false,false,false,false,true,true,true,false],
	"Review":[true,true,true,true,false,false,false,false,false,false,true,false,true,false,true,true,true,false],
	"Approve":[true,true,true,true,false,false,false,false,false,true,false,true,true,true,true,true,true,false],
	"Published":[true,true,true,false,true,false,false,false,false,false,false,false,false,false,true,true,true,false],
	"Retired":[true,true,true,false,true,false,false,true,true,false,false,false,false,false,true,true,false,true],
	"mdlplcs":[true,true,true,false,true,true,false,false,false,false,false,false,false,false,true,true,true,false]
};

var renderTable = function() {
	var data = filterData(fullDataset);
    /*set up data store*/
    store = new dojo.data.ItemFileWriteStore({data: data});
	activeDataSet = data;
    /*set up layout*/
    var layout = [
      {name: 'Source', noresize: true, field: 'source', width: "100px", formatter: function(value, index) { 
		return "<img alt='" + grid.getItem(index).type + "' height='20px' src='" + value + "'><div class='gid-actions "+ grid.getItem(index).type +"'><input id='accept-"+grid.getItem(index).itemId+"' name='accept-copy-"+grid.getItem(index).itemId+"' type='checkbox' value='accept' onClick=\"getElementById('copy-"+grid.getItem(index).itemId+"').checked=false;\" /><label for='accept-"+grid.getItem(index).itemId+"'>Accept</label> | <input id='copy-"+grid.getItem(index).itemId+"' name='accept-copy-"+grid.getItem(index).itemId+"' type='checkbox' value='copy' onClick=\"getElementById('accept-"+grid.getItem(index).itemId+"').checked=false;\" /><label for='copy-"+grid.getItem(index).itemId+"'>Copy</label></div>";
		}},
      {name: 'Policy', noresize: true, field: 'title', width: "200px", formatter: function(value, index) { 
		return "<a href='"+ grid.getItem(index).editURL + "' target='_new' >" + value + "</a>"; 
		}},
      {name: 'Status', noresize: true, field: 'status', width: "60px"},
      {name: 'Date Submitted for Review', noresize: true, field: 'reviewDate', width: "70px"},
      {name: 'Publish Date', noresize: true, field: 'liveDateFormatted', width: "70px"},
      {name: 'Last Review Date', noresize: true, field: 'lastModFormatted', width: "70px"},
      {name: 'Scheduled Review Date', noresize: true, field: 'reviewDate', width: "70px"},
      {name: 'Retired Date', noresize: true, field: 'retiredDate', width: "70px"},
      {name: 'Rationale For Retirement', noresize: true, field: 'retiredDate', width: "120px"},
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
      {name: 'Policy Replacement', noresize: true, field: 'replacement', width: "120px"}
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
			  defaultPageSize: 25,
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

var process = function() {
	var targetPath = "prupolicycontent/business group policies/Lukes Test";
	var accept = [];
	var copy = [];
	var inputs = document.getElementsByTagName("INPUT");
	
	for(var i = 0; i < inputs.length; ++i) {
		var name = inputs[i].getAttribute("name");
		if(name && name.startsWith("accept-copy-") && inputs[i].checked) {
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
		$.get("/wps/wcm/myconnect/prudential/prupolicydesign/jspassets/processpoliciesjsp", {"pp-copy":copy.join(), "pp-link":accept.join(), "targetPath":targetPath}, function(){location.reload();});
	}	
};

	</script>
<br>
<div style="float:left; margin-left:110px">
	<input type="text" id="title-filter" style="width:160px; padding:2px"/> <button type="button" onClick="refreshData()" style="padding:0">Filter</button>
</div>
<select name="status-filter-value" id="status-filter-value" onChange="refreshData();" style="float:right">
	<option value="all" selected="selected">All</option>
	<option value="Draft">Draft</option>
	<option value="Review">Review</option>
	<option value="Approve">Approve</option>
	<option value="Published">Published</option>
	<option value="Retired">Retired</option>
	<option value="mdlplcs">Available Model Policies</option>
</select>

<div id="gridDiv" style="padding:15px 0 0"></div>

<button type="button" onClick="process()">Process</button>
