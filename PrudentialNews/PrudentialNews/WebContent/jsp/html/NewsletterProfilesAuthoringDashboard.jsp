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
                 java.util.*,
                 com.prudential.utils.Utils,
                 java.text.*,
                 com.prudential.authoring.launchpage.*"%>
<%@ taglib uri="/WEB-INF/tld/wcm.tld" prefix="wcm"%>
<%@ taglib uri="/WEB-INF/tld/std-portlet.tld" prefix="portlet" %>
<portlet:defineObjects/>
<wcm:initworkspace
	user="<%= (java.security.Principal)request.getUserPrincipal() %>" />


<wcm:libraryComponent name="Custom Authoring Assets/HTML - InitjQuery" library="PrudentialNewsDesign" />	
<wcm:libraryComponent
	name="Custom Authoring Assets/HTML - NewButtonPanel"
	library="PrudentialNewsDesign" />
<wcm:libraryComponent name="Custom Authoring Assets/HTML - New Profile Button" library="PrudentialNewsDesign"/>
<br>

	<%
	RenderingContext rc = (RenderingContext) pageContext.getRequest().getAttribute(Workspace.WCM_RENDERINGCONTEXT_KEY);
	Content incoming = rc.getContent();
	DocumentId parentId = incoming.getDirectParent();
	DocumentId[] parentIds = {parentId};	
	SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");	
	// set the sort to show last mod first	
	// set the library selectors
	ArrayList <Library>libraryList = new ArrayList<Library>();
	// get the workspace
	Workspace ws = Utils.getWorkspace();
%>
<script>

<%
	// build results for newsletters
	CustomAuthoringLaunchPageQueryParams queryParmsNewsletter = new CustomAuthoringLaunchPageQueryParams();
	// set the sort to show last mod first
	queryParmsNewsletter.setModifiedSortActive(true);
	// set the library selectors
	ArrayList <Library>libraryListNewsletter = new ArrayList<Library>();
	
	// get the libraries I care about
    Library tempLibNewsletter = ws.getDocumentLibrary("PrudentialNewsletterContent");
	libraryListNewsletter.add(tempLibNewsletter);
	
	/*
	*	get the authoring templates
	*/
	String ATNewsletterIDString = "c35294c9-1a81-4516-b4fe-f419445fbb50";
	String ATDistListIDString = "eceebdc6-dc90-4951-8e04-e87ad30b4441";
	
	DocumentId atNewsletterId = ws.createDocumentId(ATNewsletterIDString);
	DocumentId atDistlistId = ws.createDocumentId(ATDistListIDString);
	
	AuthoringTemplate limitedNewsletterTemplate = null;
	if(atNewsletterId != null) {
		limitedNewsletterTemplate = (AuthoringTemplate)ws.getById(atNewsletterId);
	}
	
	AuthoringTemplate limitedDistlistTemplate = null;
	if(atDistlistId != null) {
		limitedDistlistTemplate = (AuthoringTemplate)ws.getById(atDistlistId);
	}
	Selector authTemplateSelector = null;
	ArrayList additionalSelectorsNewsletter = null;
	ArrayList additionalSelectorsDistList = null;
	
	authTemplateSelector = null;	
	
	if(limitedNewsletterTemplate != null) {
		authTemplateSelector = Selectors.authoringTemplateEquals(limitedNewsletterTemplate);
		additionalSelectorsNewsletter = new ArrayList();
		additionalSelectorsNewsletter.add(authTemplateSelector);
	}
	if(limitedDistlistTemplate != null) {
		authTemplateSelector = Selectors.authoringTemplateEquals(limitedDistlistTemplate);
		additionalSelectorsDistList = new ArrayList();
		additionalSelectorsDistList.add(authTemplateSelector);
	}
	Selector classesSelector = Selectors.typeIn(Content.class);
	queryParmsNewsletter.setClassesSelector(classesSelector);	
	queryParmsNewsletter.setLibraries(libraryListNewsletter);	
	
	Query theQueryNewsletter = CustomAuthoringLaunchPageQueries.buildQuery(additionalSelectorsNewsletter,queryParmsNewsletter);
	Query theQueryDistLists = CustomAuthoringLaunchPageQueries.buildQuery(additionalSelectorsDistList,queryParmsNewsletter);
	ResultIterator resultsNewsletter = CustomAuthoringLaunchPageQueries.runQuery(ws,theQueryNewsletter,queryParmsNewsletter);
	ResultIterator resultsDistLists = CustomAuthoringLaunchPageQueries.runQuery(ws,theQueryDistLists,queryParmsNewsletter);
	
	String[] additionalAttributesNewsletter = {};
	List<CustomAuthoringItemWrapper> wrapperResultsNewsletter = CustomAuthoringLaunchPageQueries.wrapResults(resultsNewsletter,renderRequest,renderResponse,additionalAttributesNewsletter,true);
	List<CustomAuthoringItemWrapper> wrapperResultsDistLists = CustomAuthoringLaunchPageQueries.wrapResults(resultsDistLists,renderRequest,renderResponse,additionalAttributesNewsletter,true);

	// get the change status content
%>

var dataNewsletter = [
<%for (int j = 0; j < wrapperResultsNewsletter.size(); ++j) {
		CustomAuthoringItemWrapper theWrapperNewsletter = wrapperResultsNewsletter.get(j);
        // get the action URLs        
        String editURL = "";
        // ensure live date isn't null
        String liveDateFormatted = "";
        if(theWrapperNewsletter.getLiveDate() != null) {
        	liveDateFormatted = formatter.format(theWrapperNewsletter.getLiveDate());
        } // end-if
		
        String lastModFormatted = "";
        // get a comma delim list of the cat names
        StringBuilder catStringBuilder = new StringBuilder();
        ArrayList catIds = theWrapperNewsletter.getCategories();
        Iterator catIts = catIds.iterator();
        while(catIts.hasNext()) {
        	DocumentId tempId = (DocumentId)catIts.next();
        	catStringBuilder.append(tempId.getName());
        	if(catIts.hasNext()) {
        	catStringBuilder.append(",");
        	}
        }
        String catString = catStringBuilder.toString();
        String contentPath = theWrapperNewsletter.getPath();
        String activeStatus = "Active";
        String changeStatus = "Inactive";
        if(contentPath.toLowerCase().contains("inactive")) {
        	activeStatus = "Inactive";
        	changeStatus = "Active";
        }
        // generate a URL to the change status content
        String changeStatusURL = "/"
	if(theWrapperNewsletter.getLastModDate() != null) {
		lastModFormatted = formatter.format(theWrapperNewsletter.getLastModDate());
    } // end-if%>{
		recid:"<%=j%>",
		authTemplate:"<%=theWrapperNewsletter.getAuthTemplateName()%>",
		contentId:"<%=theWrapperNewsletter.getItemId()%>",
		source:"<%=theWrapperNewsletter.getIconPath()%>",
		editURL:"<wcm:plugin name="RemoteAction" dialog="true" action="edit" docid="<%=theWrapperNewsletter.getItemId()%>" dialog="true"></wcm:plugin>",
		deleteURL:"<wcm:plugin name="RemoteAction" dialog="true" action="delete" docid="<%=theWrapperNewsletter.getItemId()%>" dialog="true"></wcm:plugin>",
		title:"<%=theWrapperNewsletter.getTitle()%>",
		categories:"<%=catString%>",
		status:"<%=theWrapperNewsletter.getStatus()%>",
		activeStatus:"<%=activeStatus%>",		
		changeStatusURL:"<%=changeStatus%>",
		liveDateFormatted:"<%=liveDateFormatted%>",
		lastModFormatted:"<%=lastModFormatted%>",
		reviewDate:"<%=theWrapperNewsletter.getReviewDate()%>",
		wfStage:"<%=theWrapperNewsletter.getWfStage()%>",
		author:"<%=theWrapperNewsletter.getAuthor()%>"

		}<%if(j < wrapperResultsNewsletter.size()-1) {%>,<%} // end-if
      } // end for-loop%>
];

var dataDistributionLists = [
<%for (int j = 0; j < wrapperResultsDistLists.size(); ++j) {
		CustomAuthoringItemWrapper theWrapperNewsletter = wrapperResultsDistLists.get(j);
        // get the action URLs        
        String editURL = "";
        // ensure live date isn't null
        String liveDateFormatted = "";
        if(theWrapperNewsletter.getLiveDate() != null) {
        	liveDateFormatted = formatter.format(theWrapperNewsletter.getLiveDate());
        } // end-if
		
        String lastModFormatted = "";
        // get a comma delim list of the cat names
        StringBuilder catStringBuilder = new StringBuilder();
        ArrayList catIds = theWrapperNewsletter.getCategories();
        Iterator catIts = catIds.iterator();
        while(catIts.hasNext()) {
        	DocumentId tempId = (DocumentId)catIts.next();
        	catStringBuilder.append(tempId.getName());
        	if(catIts.hasNext()) {
        	catStringBuilder.append(",");
        	}
        }
        String catString = catStringBuilder.toString();
	if(theWrapperNewsletter.getLastModDate() != null) {
		lastModFormatted = formatter.format(theWrapperNewsletter.getLastModDate());
    } // end-if%>{
		recid:"<%=j%>",
		authTemplate:"<%=theWrapperNewsletter.getAuthTemplateName()%>",
		contentId:"<%=theWrapperNewsletter.getItemId()%>",
		source:"<%=theWrapperNewsletter.getIconPath()%>",
		editURL:"<wcm:plugin name="RemoteAction" dialog="true" action="edit" docid="<%=theWrapperNewsletter.getItemId()%>" dialog="true"></wcm:plugin>",
		readURL:"<wcm:plugin name="RemoteAction" dialog="true" action="read" docid="<%=theWrapperNewsletter.getItemId()%>" dialog="true"></wcm:plugin>",
		deleteURL:"<wcm:plugin name="RemoteAction" dialog="true" action="delete" docid="<%=theWrapperNewsletter.getItemId()%>" dialog="true"></wcm:plugin>",
		title:"<%=theWrapperNewsletter.getTitle()%>",
		categories:"<%=catString%>",
		status:"<%=theWrapperNewsletter.getStatus()%>",
		liveDateFormatted:"<%=liveDateFormatted%>",
		lastModFormatted:"<%=lastModFormatted%>",
		reviewDate:"<%=theWrapperNewsletter.getReviewDate()%>",
		wfStage:"<%=theWrapperNewsletter.getWfStage()%>",
		author:"<%=theWrapperNewsletter.getAuthor()%>"

		}<%if(j < wrapperResultsDistLists.size()-1) {%>,<%} // end-if
      } // end for-loop%>
];
</script>

<div id="gridNewsletterProfiles" style="height: 200px;" name="gridNewsletterProfiles" class="w2ui-reset w2ui-grid"></div><br>
<div id="gridDistLists" style="height: 200px;" name="gridDistLists" class="w2ui-reset w2ui-grid"></div>
<script>
/*set up layout*/
        var layoutNewsletterProfiles = [
        { field: 'actions', caption: 'actions', size: '30%', sortable: true, resizable: true,render: function (record, index, column_index) {
					var html = '<div style="display: inline-block"><span class="ui-icon ui-icon-circle-check" style="display: inline-block"></span><span class="ui-icon ui-icon-circlesmall-plus" style="display: inline-block"></span><a href="'+ record.deleteURL + '"><span class="ui-icon ui-icon-trash" style="display: inline-block"></span></a></div>';
					return html;
				}  },
        { field: 'title', caption: 'Newsletter Profile', size: '30%', sortable: true, resizable: true,render: function (record, index, column_index) {					
					var html = '<div><a href="'+ record.editURL + '" target="_blank"> ' + record.title + '</a></div>';
					return html;
				}  },
        { field: 'categories', caption: 'Categories', size: '30%', sortable: true, resizable: true },
        { field: 'activeStatus', caption: 'Active Status', size: '30%', sortable: true, resizable: true },
        { field: 'lastModFormatted', caption: 'Last Modified', size: '30%', sortable: true, resizable: true },
        { field: 'author', caption: 'Author', size: '40%', resizable: true }
    ];
    var layoutDistList = [
        { field: 'actions', caption: 'actions', size: '30%', sortable: true, resizable: true,render: function (record, index, column_index) {
					var html = '<div style="display: inline-block"><span class="ui-icon ui-icon-circle-check" style="display: inline-block"></span><span class="ui-icon ui-icon-circlesmall-plus" style="display: inline-block"></span><a href="'+ record.deleteURL + '"><span class="ui-icon ui-icon-trash" style="display: inline-block"></span></a></div>';
					return html;
				}  },
        { field: 'title', caption: 'Distribution List', size: '30%', sortable: true, resizable: true,render: function (record, index, column_index) {
					var html = '<div><a href="'+ record.readURL + '"> ' + record.title + '</a></div>';
					return html;
				}  },
        { field: 'lastModFormatted', caption: 'Last Modified', size: '30%', sortable: true, resizable: true },
        { field: 'author', caption: 'Author', size: '40%', resizable: true }
    ];

$('#gridNewsletterProfiles').w2grid({
    name: 'gridNewsletterProfiles',
    header: 'Newsletter Profiles',
    show: {
        toolbar: true,
        footer: true
    },
    columns: layoutNewsletterProfiles,
    sortData: [{ field: 'title', direction: 'ASC' }],
    records: dataNewsletter
});
$('#gridDistLists').w2grid({
    name: 'gridDistLists',
    header: 'Distribution Lists',
    show: {
        toolbar: true,
        footer: true
    },
    columns: layoutDistList,
    sortData: [{ field: 'title', direction: 'ASC' }],
    records: dataDistributionLists
});
</script>
