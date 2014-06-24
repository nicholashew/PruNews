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
	
	String ATNewsletterIDString = "c35294c9-1a81-4516-b4fe-f419445fbb50";
	DocumentId atNewsletterId = ws.createDocumentId(ATNewsletterIDString);
	
	AuthoringTemplate limitedNewsletterTemplate = null;
	if(atNewsletterId != null) {
		limitedNewsletterTemplate = (AuthoringTemplate)ws.getById(atNewsletterId);
	}
	Selector authTemplateSelector = null;
	ArrayList additionalSelectorsNewsletter = null;
	Selector classesSelector = Selectors.typeIn(Content.class);
	queryParmsNewsletter.setClassesSelector(classesSelector);
	authTemplateSelector = null;	
	if(limitedNewsletterTemplate != null) {
		authTemplateSelector = Selectors.authoringTemplateEquals(limitedNewsletterTemplate);
		additionalSelectorsNewsletter = new ArrayList();
		additionalSelectorsNewsletter.add(authTemplateSelector);
	}
	else 
	{
		out.println("not able to retrieve authoring template");
	}
	
	queryParmsNewsletter.setLibraries(libraryListNewsletter);
	queryParmsNewsletter.setClassesSelector(classesSelector);	
	
	Query theQueryNewsletter = CustomAuthoringLaunchPageQueries.buildQuery(additionalSelectorsNewsletter,queryParmsNewsletter);
	ResultIterator resultsNewsletter = CustomAuthoringLaunchPageQueries.runQuery(ws,theQueryNewsletter,queryParmsNewsletter);
	
	String[] additionalAttributesNewsletter = {};
	List<CustomAuthoringItemWrapper> wrapperResultsNewsletter = CustomAuthoringLaunchPageQueries.wrapResults(resultsNewsletter,renderRequest,renderResponse,additionalAttributesNewsletter,true);
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
	if(theWrapperNewsletter.getLastModDate() != null) {
		lastModFormatted = formatter.format(theWrapperNewsletter.getLastModDate());
    } // end-if%>{
		recid:"<%=j%>",
		authTemplate:"<%=theWrapperNewsletter.getAuthTemplateName()%>",
		contentId:"<%=theWrapperNewsletter.getItemId()%>",
		source:"<%=theWrapperNewsletter.getIconPath()%>",
		editURL:"<wcm:plugin name="RemoteAction" dialog="true" action="edit" docid="<%=theWrapperNewsletter.getItemId()%>" dialog="true"></wcm:plugin>",
		title:"<%=theWrapperNewsletter.getTitle()%>",
		categories:"<%=catString%>",
		status:"<%=theWrapperNewsletter.getStatus()%>",
		liveDateFormatted:"<%=liveDateFormatted%>",
		lastModFormatted:"<%=lastModFormatted%>",
		reviewDate:"<%=theWrapperNewsletter.getReviewDate()%>",
		wfStage:"<%=theWrapperNewsletter.getWfStage()%>",
		author:"<%=theWrapperNewsletter.getAuthor()%>"

		}<%if(j < wrapperResultsNewsletter.size()-1) {%>,<%} // end-if
      } // end for-loop%>
];

</script>
<div id="gridNewsletter" style="height: 200px;" name="gridNewsletter" class="w2ui-reset w2ui-grid"></div>
<script>
/*set up layout*/
        var layoutNewsletters = [
        { field: 'actions', caption: 'actions', size: '30%', sortable: true, resizable: true,render: function (record, index, column_index) {
					var html = '<div style="display: inline-block"><span class="ui-icon ui-icon-circle-check" style="display: inline-block"></span><span class="ui-icon ui-icon-circlesmall-plus" style="display: inline-block"></span></div>';
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
					var html = '<div style="display: inline-block"><span class="ui-icon ui-icon-circle-check" style="display: inline-block"></span><span class="ui-icon ui-icon-circlesmall-plus" style="display: inline-block"></span></div>';
					return html;
				}  },
        { field: 'title', caption: 'News', size: '30%', sortable: true, resizable: true,render: function (record, index, column_index) {
					var html = '<div><a href="'+ record.editURL + '"> ' + record.title + '</a></div>';
					return html;
				}  },
        { field: 'lastModFormatted', caption: 'Last Modified', size: '30%', sortable: true, resizable: true },
        { field: 'author', caption: 'Author', size: '40%', resizable: true }
    ];

$j('#gridNewsletter').w2grid({
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
</script>
