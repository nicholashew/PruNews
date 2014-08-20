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
<%@ taglib uri="/WEB-INF/tld/std-portlet.tld" prefix="portlet"%>
<portlet:defineObjects />
<wcm:initworkspace
	user="<%= (java.security.Principal)request.getUserPrincipal() %>" />


<wcm:libraryComponent name="Custom Authoring Assets/HTML - InitjQuery"
	library="PrudentialNewsDesign" />
<wcm:libraryComponent
	name="Custom Authoring Assets/HTML - NewButtonPanel"
	library="PrudentialNewsDesign" />
<wcm:libraryComponent
	name="Custom Authoring Assets/HTML - New News Button"
	library="PrudentialNewsDesign" />

<%
	RenderingContext rc = (RenderingContext) pageContext.getRequest().getAttribute(Workspace.WCM_RENDERINGCONTEXT_KEY);
	Content incoming = rc.getContent();
	DocumentId parentId = incoming.getDirectParent();
	DocumentId[] parentIds = {parentId};	
	//out.println(parentId);
%>
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
	//String[] additionalAttributes = {"Issuing Orgainization"};
	String[] additionalAttributes = {""};
	// now build the items from the results
	
	List<CustomAuthoringItemWrapper> wrapperResults = CustomAuthoringLaunchPageQueries.wrapResults(results,renderRequest,renderResponse,additionalAttributes,true);
	
	SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
%>
<script>
var dataNews = [
<%for (int i = 0; i < wrapperResults.size(); ++i) {
		CustomAuthoringItemWrapper theWrapper = wrapperResults.get(i);
        // get the action URLs
        //CustomAuthoringItemAction previewAction = (CustomAuthoringItemAction)theWrapper.getAction("Edit");
        //String editURL = "";//previewAction.getActionURL();
        // ensure live date isn't null
        String liveDateFormatted = "";
        if(theWrapper.getLiveDate() != null) {
        	liveDateFormatted = formatter.format(theWrapper.getLiveDate());
        } // end-if
		
		
		StringBuilder catStringBuilder = new StringBuilder();
        ArrayList catIds = theWrapper.getCategories();
        Iterator catIts = catIds.iterator();
        while(catIts.hasNext()) {
        	DocumentId tempId = (DocumentId)catIts.next();
        	catStringBuilder.append(tempId.getName());
        	if(catIts.hasNext()) {
        	catStringBuilder.append(",");
        	}
        }
        String catString = catStringBuilder.toString();
        String lastModFormatted = "";
	if(theWrapper.getLastModDate() != null) {
		lastModFormatted = formatter.format(theWrapper.getLastModDate());
    } // end-if%>{
		recid:"<%=i%>",
		authTemplate:"<%=theWrapper.getAuthTemplateName()%>",
		contentId:"<%=theWrapper.getItemId()%>",		
		editURL:"<wcm:plugin name="RemoteAction" action="edit" docid="<%=theWrapper.getItemId()%>" dialog="true"></wcm:plugin>",
		readURL:"<wcm:plugin name="RemoteAction" action="read" docid="<%=theWrapper.getItemId()%>" dialog="true"></wcm:plugin>",
		deleteURL:"<wcm:plugin name="RemoteAction" action="delete" docid="<%=theWrapper.getItemId()%>" dialog="true"></wcm:plugin>",
		title:"<%=theWrapper.getTitle()%>",
		status:"<%=theWrapper.getStatus()%>",
		liveDateFormatted:"<%=liveDateFormatted%>",
		lastModFormatted:"<%=lastModFormatted%>",
		categories:"<%=catString%>",
		reviewDate:"<%=theWrapper.getReviewDate()%>",
		author:"<%=theWrapper.getAuthor()%>"

		}<%if(i < wrapperResults.size()-1) {%>,<%} // end-if
	} // end for-loop%>
];


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
	List<CustomAuthoringItemWrapper> wrapperResultsNewsletter = CustomAuthoringLaunchPageQueries.wrapResults(resultsNewsletter,renderRequest,renderResponse,additionalAttributesNewsletter,true);
%>
var dataNewsletter = [
<%for (int j = 0; j < wrapperResultsNewsletter.size(); ++j) {
		CustomAuthoringItemWrapper theWrapperNewsletter = wrapperResultsNewsletter.get(j);
        // get the action URLs
        //CustomAuthoringItemAction previewAction = (CustomAuthoringItemAction)theWrapperNewsletter.getAction("Edit");
        //String editURL = previewAction.getActionURL();
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
		editURL:"<wcm:plugin name="RemoteAction" dialog="false" action="preview" docid="<%=theWrapperNewsletter.getItemId()%>" ></wcm:plugin>",
		readURL:"<wcm:plugin name="RemoteAction" dialog="true" action="read" docid="<%=theWrapperNewsletter.getItemId()%>" ></wcm:plugin>",
		deleteURL:"<wcm:plugin name="RemoteAction" action="delete" docid="<%=theWrapperNewsletter.getItemId()%>" dialog="true"></wcm:plugin>",
		approveURL:"<wcm:plugin name="RemoteAction" action="approve" docid="<%=theWrapperNewsletter.getItemId()%>" dialog="true"></wcm:plugin>",
		title:"<%=theWrapperNewsletter.getTitle()%>",
		status:"<%=theWrapperNewsletter.getStatus()%>",
		liveDateFormatted:"<%=liveDateFormatted%>",
		lastModFormatted:"<%=lastModFormatted%>",
		categories:"<%=catString%>",
		reviewDate:"<%=theWrapperNewsletter.getReviewDate()%>",
		wfStage:"<%=theWrapperNewsletter.getWfStage()%>",
		author:"<%=theWrapperNewsletter.getAuthor()%>"

		}<%if(j < wrapperResultsNewsletter.size()-1) {%>,<%} // end-if
      } // end for-loop%>
];

</script>
<div id="gridNews" style="height: 200px;" name="gridNews"
	class="w2ui-reset w2ui-grid"></div>
<br>
<br>
<div id="gridNewsletter" style="height: 200px;" name="gridNewsletter"
	class="w2ui-reset w2ui-grid"></div>
<script>
/*set up layout*/
        var layoutNewsletters = [
        { field: 'actions', caption: 'actions', size: '30%', sortable: true, resizable: true,render: function (record, index, column_index) {
					var html = '<div style="display: inline-block"><a href="'+ record.readURL + '"><span class="ui-icon ui-icon-circle-check" style="display: inline-block"></span></a><span class="ui-icon ui-icon-circlesmall-plus" style="display: inline-block"></span><span onclick="ajaxDeleteNewsletter(\'' + record.contentId + '\',\'' + record.title + '\')" class="ui-icon ui-icon-trash" style="display: inline-block"></span></div>';
					return html;
				}  },
        { field: 'title', caption: 'Newsletter', size: '30%', sortable: true, resizable: true,render: function (record, index, column_index) {					
					var html = '<div><a href="'+ record.editURL + '" target="_blank"> ' + record.title + '</a></div>';
					return html;
				}  },
				{ field: 'categories', caption: 'Categories', size: '30%', sortable: true, resizable: true },
        { field: 'lastModFormatted', caption: 'Last Modified', size: '30%', sortable: true, resizable: true },
        { field: 'status', caption: 'Status', size: '30%', sortable: true, resizable: true },
        { field: 'wfStage', caption: 'wfStage', size: '30%', sortable: true, resizable: true },
        { field: 'author', caption: 'Author', size: '40%', resizable: true }
    ];
    var layoutNews = [
        { field: 'actions', caption: 'actions', size: '30%', sortable: true, resizable: true,render: function (record, index, column_index) {
					var html = '<div style="display: inline-block"><a href="'+ record.readURL + '"><span class="ui-icon ui-icon-circle-check" style="display: inline-block"></span></a><span class="ui-icon ui-icon-circlesmall-plus" style="display: inline-block"></span><a href="'+ record.deleteURL + '"><span class="ui-icon ui-icon-trash" style="display: inline-block"></span></a></div>';
					return html;
				}  },				
        { field: 'title', caption: 'News', size: '30%', sortable: true, resizable: true,render: function (record, index, column_index) {
					var html = '<div><a href="'+ record.editURL + '"> ' + record.title + '</a></div>';
					return html;
				}  },
				{ field: 'categories', caption: 'Categories', size: '30%', sortable: true, resizable: true },
        { field: 'lastModFormatted', caption: 'Last Modified', size: '30%', sortable: true, resizable: true },
        { field: 'author', caption: 'Author', size: '40%', resizable: true }
    ];

$('#gridNews').w2grid({
    name: 'gridNews',
    header: 'Recent News',
    show: {
        toolbar: true,
        footer: true
    },
    columns: layoutNews,
    sortData: [{ field: 'title', direction: 'ASC' }],
    records: dataNews
});

$('#gridNewsletter').w2grid({
    name: 'gridNewsletter',
    header: 'Recent News',
    show: {
        toolbar: true,
        footer: true
    },
    columns: layoutNewsletters,
    sortData: [{ field: 'title', direction: 'ASC' }],
    records: dataNewsletter
});


function ajaxCallback(result) {
	console.log ("Inside ajaxCallback with result = " + result);
	if (result.match(/.*success.*/ig)) {
	   console.log ("It succeeded");
	} else {
	   console.log ("It failed");
	   alert ("Failure in deleting newsletter, contact your System Administrator");
	}
    window.location.reload(true);
}

function ajaxDeleteNewsletter (contentId, title) {
    var encodedContentId = encodeURIComponent(contentId);
    var deleteNewsLetter = confirm("Delete newsletter " + title + "?");
    if (deleteNewsLetter == true) {
     // make an http request to get the content
      return $.ajax({
         url: "/wps/wcm/connect/prudential/prudentialnewsdesign/jspassets/deletenewsletter.jsp",
         type: "POST",
         data: {'newsletter_uuid' : encodedContentId},
         success: ajaxCallback,
//         complete: function(data, returnMsg) { 
//             alert ("Got success path and data: " + $(data).text() + " and msg: " + returnMsg);
//             window.location.reload(true);
//         }
//       error: errorFunction
      });
    } 
}
</script>
