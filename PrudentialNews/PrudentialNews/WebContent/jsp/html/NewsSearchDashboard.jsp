<%--
/********************************************************************/
/* Asponte
/* cmknight/bpoole
/********************************************************************/

--%>
<%@ page
	import="org.apache.jetspeed.portlet.PortletURI,
                 com.ibm.workplace.wcm.api.*,
                 com.ibm.workplace.wcm.api.query.*,
                 java.util.*,
                 com.prudential.utils.Utils,
                 java.text.*,
                 org.json.*,
                 com.ibm.workplace.wcm.api.query.WorkflowSelectors.Status,
                 com.prudential.authoring.launchpage.*"%>
<%@ taglib uri="/WEB-INF/tld/wcm.tld" prefix="wcm"%>
<%@ taglib uri="/WEB-INF/tld/std-portlet.tld" prefix="portlet" %>
<portlet:defineObjects/>
<wcm:initworkspace
	user="<%= (java.security.Principal)request.getUserPrincipal() %>" />


<wcm:libraryComponent name="Custom Authoring Assets/HTML - InitjQuery" library="PrudentialNewsDesign" />	
<br/>
<form name="searchWCM" action="">
<table>
<tr>
<td colspan="2">Choose start and/or end modification dates for search. Leave blank to select all</td>
</tr>
<tr>
   <td>Modified Since Date: </td><td><input type="text" id="startDatePicker" name="startDatePicker"></td>
</tr>
<tr>
   <td>Modified Before Date: </td><td><input type="text" id="endDatePicker" name="endDatePicker"></td>
</tr>
<tr>
<td><input type='button' name='basic' id="openCatModal" value='Select Categories' class='basic-modal'/></td><td><input type="hidden" id="SelectedCats" name="SelectedCats" style="width:500px; height:500px;">
<input type="text" id="tokenfield" />
</td>
</tr>
</table>

<div id="categoryModal">	
	<div class='content' id="category-list-ajax"></div>	
</div>
<input type="submit" value="Search"><br/>
</form>
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

// Date picker
 $(function() {
$( "#startDatePicker" ).datepicker({ dateFormat: "mm/dd/yy" });
$( "#endDatePicker" ).datepicker({ dateFormat: "mm/dd/yy" });
});

// Category picker

function getCategoryIds() {
	var catIds = [];
	try {
		var catsJson = jQuery.parseJSON(unescape($("#SelectedCats").val()));
		$.each(catsJson, function(i, obj) {
			catIds.push(obj.id);
		});
	} catch(ex) {
	}
	return catIds;
}

function updateCategory() {
    
	var categories = [];
	$("#SelectedCats").val("");
	$("#tokenfield").tokenfield('destroy');	
	$("#tokenfield").val('');
	$("#tokenfield").tokenfield();
	$(":checkbox:checked").each(function() {
		var obj = {id:"", label:""};
		obj.id = $(this).val();
		obj.label = $('label[for="'+obj.id+'"]').text();
		//obj.label = $(this).find("label").text();
		categories.push(obj);
		$("#tokenfield").tokenfield('createToken', { value: obj.id, label: obj.label });
	});
	$("#SelectedCats").val(JSON.stringify(categories));
	
	// update with the tokenfield
	
}

function updateCategoryListSelection(catIds) {
	for(var i = 0; i < catIds.length; ++i) {
		$("#"+catIds[i]+"").prop("checked", true);
	}
}

$(function(){
	var theData = $("#tokenfield").val(); 
	if (theData.indexOf("[") == -1) {
	    theData = "[" + theData + "]";
	}
	var jsonData = $.parseJSON(theData);
	$("#tokenfield")
		.on('tokenfield:removedtoken', function (e) {
	        var checkId = e.attrs.value;
	        console.log ("got a change in " + checkId);
	        $("#" + checkId + "").prop('checked', false); 
	        var catsJson = jQuery.parseJSON(unescape($("#SelectedCats").val()));
			console.log ("JSON Is " + JSON.stringify(catsJson));
		   	var indexToDelete = null;
		   	$.each(catsJson, function(i, obj) {
				console.log ("Got : " + obj.id);
				if (obj.id == checkId) {
				   indexToDelete = i;
				}
			});
			catsJson.splice(indexToDelete, 1);
			console.log ("Now JSON Is " + JSON.stringify(catsJson));
    		$("#SelectedCats").val(JSON.stringify(catsJson));
	        
   	        //$("#SelectedCats").val(JSON.stringify(categories));
	   })	
	.tokenfield();	
	//var $select = $('#select-test');
	var categories = [];
	$(jsonData).each(function (index, o) {  
		var obj = {id:"", label:""};
		obj.id = o.id;
		obj.label = o.label;
		//obj.label = $(this).find("label").text();
		categories.push(obj);
		$("#tokenfield").tokenfield('createToken', { value: obj.id, label: obj.label });
	});
	$("#SelectedCats").val(JSON.stringify(categories));

	function showCategoryList(html) {
		$("#category-list-ajax").html(html);
		// get the catIds first
		//updateCategoryListSelection(catIds);
		updateCategoryListSelection(getCategoryIds());
	}
	var categoryListHtml = null;	
	$( "#openCatModal" ).button().click(function() {
		$( "#categoryModal" ).dialog( "open" );
	});
	
	$("#categoryModal").dialog({
		autoOpen: false,
		height: 600,
		width: 1000,
		modal: true,   
		buttons: {
		 Accept : function() {
			 $("#categoryModal").dialog( "close" );
			 updateCategory();
		 },
		 Cancel: function() {
			 $("#categoryModal").dialog( "close" );
		 }
        	},
		open: function( event, ui ) {
			$("#category-list-ajax").html("<div class='loading'><br><br><img src='/webradar/public/images/spinner.gif'></div>");
			//var catIds = getCategoryIds();			
			if(categoryListHtml == null) {
			$.ajax({
				url:"/wps/wcm/myconnect/prudential/PrudentialNewsDesign/JSPAssets/CategoryList",
				traditional: true,
				success: function(html){
					showCategoryList(html);
					categoryListHtml = html;
				}
			});
			} else {
				showCategoryList(categoryListHtml);
			}
		},
		close: function() {
		      	 $("#categoryModal").dialog( "close" );
      		} // end close
	
	}); // end dialog	
});

<%

	// build results for newsletters
	CustomAuthoringLaunchPageQueryParams queryParmsNews = new CustomAuthoringLaunchPageQueryParams();
	// set the sort to show last mod first
	queryParmsNews.setModifiedSortActive(true);
	// set the library selectors
	ArrayList <Library>libraryListNews = new ArrayList<Library>();
	
	// get the libraries I care about
    Library libNews = ws.getDocumentLibrary("PrudentialNewsletterContent");
	libraryListNews.add(libNews);
	
	/* Get the search parameters, if they exist. Default to empty string */
	String startDateToSearch = request.getParameter("startDatePicker") != null ? request.getParameter("startDatePicker") : "";
	String endDateToSearch   = request.getParameter("endDatePicker") != null ? request.getParameter("endDatePicker") : "";
	String selectedCats = request.getParameter("SelectedCats") != null ? request.getParameter("SelectedCats") : "";
	
	System.out.println ("Got startDateToSearch: --" + startDateToSearch + "--");
	System.out.println ("Got endDateToSearch: --" + endDateToSearch + "--");
	System.out.println ("Got selectedCats: --" + selectedCats + "--");
	
	/*
	*	get the authoring templates PrudentialNewsDesign/AT - News
	*/
	ws.setCurrentDocumentLibrary(ws.getDocumentLibrary("PrudentialNewsDesign"));
	DocumentIdIterator docIdIter = ws.findByName(DocumentTypes.AuthoringTemplate, "AT - News");
	DocumentId atNewsId = null;
	if (docIdIter.hasNext()) {
	   atNewsId = docIdIter.next();
	}
	
	AuthoringTemplate newsTemplate = null;
	if(atNewsId != null) {
		newsTemplate = (AuthoringTemplate)ws.getById(atNewsId);
	}
	
	Selector authTemplateSelector = null;
	ArrayList additionalSelectorsNews = null;
	
	authTemplateSelector = null;	
	
	if(newsTemplate != null) {
		authTemplateSelector = Selectors.authoringTemplateEquals(newsTemplate);
	    System.out.println ("Got AT selector = " + authTemplateSelector.toString());
		additionalSelectorsNews = new ArrayList();
		additionalSelectorsNews.add(authTemplateSelector);
	}

	/* Get the start times for searching */
	if (startDateToSearch.length() > 0) {
	    Date startDate = (Date) formatter.parse(startDateToSearch);
	   // System.out.println ("Got start date types = " + startDate.toString());
	    Selector modifiedAfterSelector = HistorySelectors.modifiedSince(startDate); 
	   // System.out.println ("Got start date selector = " + modifiedAfterSelector.toString());
		additionalSelectorsNews.add(modifiedAfterSelector);
	}

	/* Get the end times for searching */
	if (endDateToSearch.length() > 0) {
	    Date endDate = (Date) formatter.parse(endDateToSearch);
	   // System.out.println ("Got end date types = " + endDate.toString());
	    Selector modifiedBeforeSelector = HistorySelectors.modifiedBefore(endDate); 
	   // System.out.println ("Got end date selector = " + modifiedBeforeSelector.toString());
		additionalSelectorsNews.add(modifiedBeforeSelector);
	}

	/* Check for categories. Comes in as an array of objects */
	if (selectedCats.length() > 0) {
		List<DocumentId> listOfIds = new ArrayList<DocumentId>();
		JSONArray jsonArray = new JSONArray(selectedCats);		
		for (int i = 0; i < jsonArray.length(); i++) {
  			JSONObject item = jsonArray.getJSONObject(i);
  			String id = item.getString("id");
  			System.out.println ("Got id: " + id);
  			DocumentId catId = ws.createDocumentId(id);
  			listOfIds.add(catId);
		}
		Selector catSelector = ProfileSelectors.categoriesContains(listOfIds);
		additionalSelectorsNews.add(catSelector);
	}
    
	Selector classesSelector = Selectors.typeIn(Content.class);
	queryParmsNews.setClassesSelector(classesSelector);	
	queryParmsNews.setLibraries(libraryListNews);	
	
	Query theQueryNewsletter = CustomAuthoringLaunchPageQueries.buildQuery(additionalSelectorsNews,queryParmsNews);
	ResultIterator resultsNewsletter = CustomAuthoringLaunchPageQueries.runQuery(ws,theQueryNewsletter,queryParmsNews);
	
	String[] additionalAttributesNews = {};
	List<CustomAuthoringItemWrapper> wrapperResultsNews = CustomAuthoringLaunchPageQueries.wrapResults(resultsNewsletter,renderRequest,renderResponse,additionalAttributesNews,true);

	// get the change status content
%>

var dataNews = [
<%for (int j = 0; j < wrapperResultsNews.size(); ++j) {
		CustomAuthoringItemWrapper theWrapperNews = wrapperResultsNews.get(j);
        // get the action URLs        
        String editURL = "";
        // ensure live date isn't null
        String liveDateFormatted = "";
        if(theWrapperNews.getLiveDate() != null) {
        	//liveDateFormatted = formatter.format(theWrapperNews.getLiveDate());
        	// Bring in the data formatted as ms since 1970.
		    liveDateFormatted = String.valueOf(theWrapperNews.getLiveDate().getTime());
        } // end-if
		
        String lastModFormatted = "";
        // get a comma delim list of the cat names
        StringBuilder catStringBuilder = new StringBuilder();
        ArrayList catIds = theWrapperNews.getCategories();
        Iterator catIts = catIds.iterator();
        while(catIts.hasNext()) {
        	DocumentId tempId = (DocumentId)catIts.next();
        	catStringBuilder.append(tempId.getName());
        	if(catIts.hasNext()) {
        	catStringBuilder.append(",");
        	}
        }
        String catString = catStringBuilder.toString();
        String contentPath = theWrapperNews.getPath();
        String activeStatus = "Active";
        String changeStatus = "Inactive";
        if(contentPath.toLowerCase().contains("inactive")) {
        	activeStatus = "Inactive";
        	changeStatus = "Active";
        }
        // generate a URL to the change status content
        String changeStatusURL = "/";
	    if(theWrapperNews.getLastModDate() != null) {
		    //lastModFormatted = formatter.format(theWrapperNews.getLastModDate());
		    // Bring in the data formatted as ms since 1970.
		    lastModFormatted = String.valueOf(theWrapperNews.getLastModDate().getTime());
        } // end-if end for loop%>{
		recid:"<%=j%>",
		authTemplate:"<%=theWrapperNews.getAuthTemplateName()%>",
		contentId:"<%=theWrapperNews.getItemId()%>",
		source:"<%=theWrapperNews.getIconPath()%>",
		editURL:"<wcm:plugin name="RemoteAction" dialog="true" action="edit" docid="<%=theWrapperNews.getItemId()%>" dialog="true"></wcm:plugin>",
		deleteURL:"<wcm:plugin name="RemoteAction" dialog="true" action="delete" docid="<%=theWrapperNews.getItemId()%>" dialog="true"></wcm:plugin>",
		title:"<%=theWrapperNews.getTitle()%>",
		categories:"<%=catString%>",
		status:"<%=theWrapperNews.getStatus()%>",
		activeStatus:"<%=activeStatus%>",		
		changeStatusURL:"<%=changeStatus%>",
		liveDateFormatted:"<%=liveDateFormatted%>",
		lastModFormatted: "<%=lastModFormatted%>",
		reviewDate:"<%=theWrapperNews.getReviewDate()%>",
		wfStage:"<%=theWrapperNews.getWfStage()%>",
		author:"<%=theWrapperNews.getAuthor()%>"

		}<%if(j < wrapperResultsNews.size()-1) {%>,<%} // end-if
      } // end for-loop%>
];

</script>

<div id="gridNewsResults" style="height: 400px;" name="gridNewsResults" class="w2ui-reset w2ui-grid"></div><br>
<script>
/*set up layout*/
        var layoutNews = [
        { field: 'actions', caption: 'actions', size: '30%', sortable: true, resizable: true,render: function (record, index, column_index) {
					var html = '<div style="display: inline-block"><span class="ui-icon ui-icon-circle-check" style="display: inline-block"></span><span class="ui-icon ui-icon-circlesmall-plus" style="display: inline-block"></span><a href="'+ record.deleteURL + '"><span class="ui-icon ui-icon-trash" style="display: inline-block"></span></a></div>';
					return html;
				}  },
        { field: 'title', caption: 'News', size: '30%', sortable: true, resizable: true,render: function (record, index, column_index) {					
					var html = '<div><a href="'+ record.editURL + '" target="_blank"> ' + record.title + '</a></div>';
					return html;
				}  },
        { field: 'categories', caption: 'Categories', size: '30%', sortable: true, resizable: true },
        { field: 'activeStatus', caption: 'Active Status', size: '30%', sortable: true, resizable: true },
        { field: 'lastModFormatted', caption: 'Last Modified', size: '30%', sortable: true, resizable: true, render: 'date:mm/dd/yyyy'},
        { field: 'liveDateFormatted', caption: 'Publish Date', size: '30%', sortable: true, resizable: true, render: 'date:mm/dd/yyyy'},
        { field: 'author', caption: 'Author', size: '40%', resizable: true }
    ];

$('#gridNewsResults').w2grid({
    name: 'gridNewsResults',
    header: 'News Results',
    show: {
        header : true,
        toolbar: true,
        footer: true
    },
    multiSearch: true,
    
    columns: layoutNews,
    sortData: [{ field: 'title', direction: 'ASC' }],
    searches : [
       { type: 'date', field: 'lastModFormatted', caption: 'Modifed Date' },
       { type: 'date', field: 'liveDateFormatted', caption: 'Published Date' }
    ],
    records: dataNews
});
</script>
