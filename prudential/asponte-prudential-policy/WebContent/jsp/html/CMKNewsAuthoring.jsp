<%--
/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

--%>
<%@ page
	import="org.apache.jetspeed.portlet.PortletURI,
                 com.ibm.workplace.wcm.api.*,
                 com.ibm.workplace.wcm.api.query.*,
                 java.util.*,com.prudential.utils.*,
                 java.text.*,com.prudential.authoring.launchpage.*"%>
<%@ taglib uri="/WEB-INF/tld/wcm.tld" prefix="wcm"%>
<%@ taglib uri="/WEB-INF/tld/portlet.tld" prefix="portletAPI"%>
<portletAPI:init />

<wcm:initworkspace
	user="<%= (java.security.Principal)request.getUserPrincipal() %>" />


<wcm:setExplicitContext
	path="/PrudentialNewsDesign/JSPAssets/CMKTestJSP" />
<wcm:libraryComponent name="Custom Authoring Assets/HTML - InitjQuery" library="PrudentialNewsDesign" />	
<wcm:libraryComponent
	name="Custom Authoring Assets/HTML - NewButtonPanel"
	library="PrudentialNewsDesign" />
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
<%for (int i = 0; i < wrapperResults.size(); ++i) {
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
    } // end-if%>{
		itemId:"<%=i%>",
		authTemplate:"<%=theWrapper.getAuthTemplateName()%>",
		contentId:"<%=theWrapper.getItemId()%>",
		source:"<%=theWrapper.getIconPath()%>",
		editURL:"<%=editURL%>",
		title:"<%=theWrapper.getTitle()%>",
		status:"<%=theWrapper.getStatus()%>",
		liveDateFormatted:"<%=liveDateFormatted%>",
		lastModFormatted:"<%=lastModFormatted%>",
		reviewDate:"<%=theWrapper.getReviewDate()%>",
		author:"<%=theWrapper.getAuthor()%>"

		}<%if(i < wrapperResults.size()-1) {%>,<%} // end-if
      } // end for-loop%>
]};


<%
	// build results for newsletters
	CustomAuthoringLaunchPageQueryParams queryParmsNewsletter = new CustomAuthoringLaunchPageQueryParams();
	// set the sort to show last mod first
	queryParmsNewsletter.setModifiedSortActive(true);
	// set the library selectors
	ArrayList <Library>libraryListNewsletter = new ArrayList<Library>();
	
	// get the libraries I care about
    	Library tempLibNewsletter = ws.getDocumentLibrary("PrudentialNewsletterDrafts");
	libraryListNewsletter.add(tempLibNewsletter);
	String ATNewsletterIDString = "94bcefed-fe75-4c3f-864e-e28da660c328";
	DocumentId atNewsletterId = ws.createDocumentId(ATNewsIDString);
	
	AuthoringTemplate limitedNewsletterTemplate = null;
	if(atNewsletterId != null) {
		limitedNewsletterTemplate = (AuthoringTemplate)ws.getById(atNewsletterId);
	}
	queryParms.setClassesSelector(classesSelector);
	authTemplateSelector = null;
	additionalSelectors = null;
	if(limitedTemplate != null) {
		authTemplateSelector = Selectors.authoringTemplateEquals(limitedNewsletterTemplate);
		additionalSelectors = new ArrayList();
		additionalSelectors.add(authTemplateSelector);
	}
	else 
	{
		out.println("not able to retrieve authoring template");
	}
	
	queryParmsNewsletter.setLibraries(libraryListNewsletter);
	queryParmsNewsletter.setClassesSelector(classesSelector);
	
	ArrayList additionalSelectorsNewsletter = null;
	
	Query theQueryNewsletter = CustomAuthoringLaunchPageQueries.buildQuery(additionalSelectorsNewsletter,queryParmsNewsletter);
	ResultIterator resultsNewsletter = CustomAuthoringLaunchPageQueries.runQuery(ws,theQueryNewsletter,queryParmsNewsletter);
	
	String[] additionalAttributesNewsletter = {};
	List<CustomAuthoringItemWrapper> wrapperResultsNewsletter = CustomAuthoringLaunchPageQueries.wrapResults(resultsNewsletter,portletRequest,portletResponse,additionalAttributesNewsletter,false);
%>
var dataNewsletter = [
<%for (int j = 0; j < wrapperResultsNewsletter.size(); ++j) {
		CustomAuthoringItemWrapper theWrapperNewsletter = wrapperResultsNewsletter.get(j);
        // get the action URLs
        CustomAuthoringItemAction previewAction = (CustomAuthoringItemAction)theWrapperNewsletter.getAction("Edit");
        String editURL = previewAction.getActionURL();
        // ensure live date isn't null
        String liveDateFormatted = "";
        if(theWrapperNewsletter.getLiveDate() != null) {
        	liveDateFormatted = formatter.format(theWrapperNewsletter.getLiveDate());
        } // end-if
		
        String lastModFormatted = "";
	if(theWrapperNewsletter.getLastModDate() != null) {
		lastModFormatted = formatter.format(theWrapperNewsletter.getLastModDate());
    } // end-if%>{
		recid:"<%=j%>",
		authTemplate:"<%=theWrapperNewsletter.getAuthTemplateName()%>",
		contentId:"<%=theWrapperNewsletter.getItemId()%>",
		source:"<%=theWrapperNewsletter.getIconPath()%>",
		editURL:"<%=editURL%>",
		title:"<%=theWrapperNewsletter.getTitle()%>",
		status:"<%=theWrapperNewsletter.getStatus()%>",
		liveDateFormatted:"<%=liveDateFormatted%>",
		lastModFormatted:"<%=lastModFormatted%>",
		reviewDate:"<%=theWrapperNewsletter.getReviewDate()%>",
		author:"<%=theWrapperNewsletter.getAuthor()%>"

		}<%if(j < wrapperResultsNewsletter.size()-1) {%>,<%} // end-if
      } // end for-loop%>
];

$j(function() {
  
    /*set up layout*/
    var layout = [     
      {name: 'AuthTemplate', field: 'authTemplate', width: "70px"},
      {name: 'News', field: 'title', width: "200px", formatter: function(value, index) { 
		return "<a href='"+ data.items[index].editURL + "'>" + value + "</a>"; 
		}},
      {name: 'Status', field: 'status', width: "70px"},
      {name: 'Publish Date', field: 'liveDateFormatted', width: "70px"},
      {name: 'Last Updated', field: 'lastModFormatted', width: "70px"},
      {name: 'Owner', field: 'author', width: "180px"}
    ];
    
     
});
</script>

<div id="w2grid" style="height: 400px;" name="grid" class="w2ui-reset w2ui-grid"></div>
<script>
/*set up layout*/
        var layoutNewsletters = [
        { field: 'title', caption: 'Newsletter', size: '30%', sortable: true, resizable: true,render: function (record, index, column_index) {
					var html = '<div><a href="'+ record.editURL + '"> ' + record.title + '</a></div>';
					return html;
				}  },
        { field: 'lastModFormatted', caption: 'Last Modified', size: '30%', sortable: true, resizable: true },
        { field: 'author', caption: 'Author', size: '40%', resizable: true }
    ];
$j('#w2grid').w2grid({
    name: 'grid2',
    header: 'List of Names',
    show: {
        toolbar: true,
        footer: true
    },
    columns: layoutNewsletters,
    sortData: [{ field: 'title', direction: 'ASC' }],
    records: dataNewsletter
});
</script>