<%--
/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

--%>
<%@ page import="org.apache.jetspeed.portlet.PortletURI,
                 com.ibm.workplace.wcm.api.*,
                 com.ibm.workplace.wcm.api.query.*,
                 java.util.*,com.prudential.utils.*,
                 java.text.*,com.prudential.authoring.launchpage.*"%>
<%@ taglib uri="/WEB-INF/tld/wcm.tld" prefix="wcm" %>
<%@ taglib uri="/WEB-INF/tld/portlet.tld" prefix="portletAPI" %>
<portletAPI:init/>

<wcm:initworkspace user="<%= (java.security.Principal)request.getUserPrincipal() %>" />

<style>
table{
border-collapse:collapse;
border:1px solid #000;
}
table td{
border:1px solid #000;
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
    height: 20em;
}
.dojoxGridArrowButtonChar {
    display: inline;
    float: right;
}
</style>
<wcm:setExplicitContext path="/PrudentialNewsDesign/JSPAssets/CMKTestJSP"/>
<wcm:libraryComponent name="Custom Authoring Assets/HTML - New News Button" library="PrudentialNewsDesign"/>
<br>
<wcm:libraryComponent name="Custom Authoring Assets/HTML - New Profile Button" library="PrudentialNewsDesign"/>
<%
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
        Library tempLib = ws.getDocumentLibrary("PrudentialNewsContent");
	libraryList.add(tempLib);
	
	queryParms.setLibraries(libraryList);
	// now add the Content selector
	Selector classesSelector = Selectors.typeIn(Content.class);
	// get the AT's for the selector	
	//String ATIDString = "4143aff0-6901-4207-852b-9b2c3d098679";
	//6a is the news authoring template
	String ATNewsIDString = "6aa27fc2-dbd9-4f7c-808d-2a98ba0f8611";
	DocumentId atId = ws.createDocumentId(ATNewsIDString);
	AuthoringTemplate limitedTemplate = null;
	if(atId != null) {
		limitedTemplate = (AuthoringTemplate)ws.getById(atId);
	}
	queryParms.setClassesSelector(classesSelector);
	Selector authTemplateSelector = null;
	ArrayList additionalSelectors = null;
	if(limitedTemplate != null) {
		authTemplateSelector = Selectors.authoringTemplateEquals(limitedTemplate);
		additionalSelectors = new ArrayList();
		additionalSelectors.add(authTemplateSelector);
	}
	else 
	{
		out.println("not able to retrieve authoring template");
	}
	
	Query theQuery = CustomAuthoringLaunchPageQueries.buildQuery(additionalSelectors,queryParms);
	ResultIterator results = CustomAuthoringLaunchPageQueries.runQuery(ws,theQuery,queryParms);
	String[] additionalAttributes = {"Issuing Orgainization"};
	// now build the items from the results
	List<CustomAuthoringItemWrapper> wrapperResults = CustomAuthoringLaunchPageQueries.wrapResults(results,portletRequest,portletResponse,additionalAttributes,false);
	SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
%>
<script>
var data = {
      identifier: 'itemId',
	  items: [
<%
	for (int i = 0; i < wrapperResults.size(); ++i) {
		CustomAuthoringItemWrapper theWrapper = wrapperResults.get(i);
        // get the action URLs
        CustomAuthoringItemAction previewAction = (CustomAuthoringItemAction)theWrapper.getAction("Edit");
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
	
		%>{
		itemId:"<%=i%>",
		contentId:"<%=theWrapper.getItemId()%>",
		source:"<%=theWrapper.getIconPath()%>",
		editURL:"<%=editURL%>",
		title:"<%=theWrapper.getTitle()%>",
		status:"<%=theWrapper.getStatus()%>",
		editURL:"<%=editURL%>",
		liveDateFormatted:"<%=liveDateFormatted%>",
		lastModFormatted:"<%=lastModFormatted%>",
		reviewDate:"<%=theWrapper.getReviewDate()%>",
		author:"<%=theWrapper.getAuthor()%>"

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

dojo.ready(function(){
    /*set up data store*/
    var store = new dojo.data.ItemFileWriteStore({data: data});
	
    /*set up layout*/
    var layout = [
      {name: 'Source', field: 'source', width: "60px", formatter: function(value) { 
		return "<img src='" + value + "'>"; 
		}},
      {name: 'Policy', field: 'title', width: "200px", formatter: function(value, index) { 
		return "<a href='"+ data.items[index].editURL + "'>" + value + "</a>"; 
		}},
      {name: 'Status', field: 'status', width: "70px"},
      {name: 'Publish Date', field: 'liveDateFormatted', width: "70px"},
      {name: 'Last Review Date', field: 'lastModFormatted', width: "70px"},
      {name: 'Review Date', field: 'reviewDate', width: "70px"},
      {name: 'Owner', field: 'author', width: "180px"}
    ];

    /*create a new grid:*/
    var grid = new dojox.grid.EnhancedGrid({
        id: 'grid',
        store: store,
        structure: layout,
        rowSelector: '20px',
        plugins: {
          pagination: {
              pageSizes: ["10", "50", "100", "All"],
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
    }, document.createElement('div'));

    /*append the new grid to the div*/
    dojo.byId("gridDiv").appendChild(grid.domNode);

    /*Call startup() to render the grid*/
    grid.startup();
});
	</script>
<br>
<b>Recent News</b>
<script>
var submitFilterForm = function() {
    alert('changed again');
    alert(document.getElementById('filter-form'));
    document.getElementById('filter-form').submit();
}
</script>
<form method="get" id="filter-form">
<select name="status-filter-value" onChange="submitFilterForm();" style="float:right">
	<option value="all" selected="selected">All</option>
	<option value="draft">Draft</option>
	<option value="review">Review</option>
	<option value="approve">Approve</option>
	<option value="published">Published</option>
	<option value="expired">Retired</option>
	<option value="mdlplcs">Available Model Policies</option>
</select>
</form>
<div id="gridDiv" style="padding:15px 0 0"></div>