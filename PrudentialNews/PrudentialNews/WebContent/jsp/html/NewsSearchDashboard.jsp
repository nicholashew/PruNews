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
<!--  new div -->
<div style="position: relative; height: 350px;overflow: hidden; ">
<div id="grid1" style="position: absolute; left: 0px; width: 39.9%; height: 350px; border: 1px solid silver;">
<div class="searchTitle">Search
	<div class="searchTitleHelp">?</div>
</div>
<form name="searchWCM" action="" style="padding: 4px">
<table>
<tr>
   	<td class="tdSearchFields">Start Date: </td>
   	<td class="tdSearchFields">
   		<input class="disableEnable searchInputFields" type="text" id="startDatePicker" name="startDatePicker">
   	</td>
   	<td class="tdSearchFields">
   		<input type='image' src='/webradar/public/images/icon-cross.png' name="clearStartDate" id="clearStartDate" alt="Delete Date">
   	</td>
</tr>
<tr>
   <td class="tdSearchFields">End Date: </td>
   <td class="tdSearchFields">
   <input class="disableEnable searchInputFields" type="text" id="endDatePicker" name="endDatePicker">
   </td>
   <td class="tdSearchFields">
   <input type='image' src='/webradar/public/images/icon-cross.png' name="clearEndDate" id="clearEndDate" alt="Delete Date">
   </td>
</tr>
<tr>
	<td class="tdSearchFields">
		<input class="disableEnable btn" type='button' name='basic' id="openCatModal" value='Select Categories' class='basic-modal'/>
	</td>
	<td class="tdSearchFields">
		<input type="hidden" id="SelectedCats" name="SelectedCats" style="width:500px; height:500px;">
		<input type="text" id="tokenfield" />
	</td>
	<td class="tdSearchFields">	
		<input type='image' src='/webradar/public/images/icon-cross.png' name="clearSelectedCatsField" id="clearSelectedCatsField" alt="Delete Categories">
	</td>
</tr>
<tr>
	<td class="tdSearchFields">
		<input class="disableEnable btn" type='button' name='basic' id="openProfileModal" value='Select Profile' class='basic-modal'/>
	</td>
	<td class="tdSearchFields">
		<input class="disableEnable searchInputFields" type="text" id="profiletokenfield" readonly />
		<input type="hidden" type="text" id="profiletokenfieldid"/>
	</td>
	<td class="tdSearchFields">
		<input type='image' src='/webradar/public/images/icon-cross.png' name="clearProfileTokenField" id="clearProfileTokenField" alt="Delete Profile">
	</td>
</tr>
</table>


	<div class="w2ui-buttons">
		<button class="btn" name="clearFields" id="clearFields">Reset</button>
		<button class="btn" name="search">Search</button>
	</div>
</form>

</div>
<div id="grid2" style="position: absolute; right: 0px; width: 59.9%; height: 350px; ">
	<div id="gridNewsResults" style="width: auto; height: 100%;" name="gridNewsResults" class="w2ui-reset w2ui-grid"></div>
</div>
<div id="loader" style="display: none">
	<div class='loading spinner'><img src='/webradar/public/images/spinner.gif'></div>		
</div>
<div id="categoryModal">	
	<div class='content' id="category-list-ajax"></div>	
</div>
<div id="profileModal">	
	<div class='content' id="profile-list-ajax"></div>	
</div>

</div> <!-- close new div -->
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

	var profileListHtml = null;	
	var categoryListHtml = null;	

$(function() {

	function clearField (dateField) {
		$.removeCookie(dateField);
		$("#" + dateField).val("");
		return false;
	}

	function clearTokenField() {
		$("#tokenfield").tokenfield('destroy');	
		$("#tokenfield").val('');
		$("#tokenfield").tokenfield();		
		// Now clear all the checkboxes
		$(":checkbox:checked").each(function() {
			$(this).prop('checked', false); 
		});
	}
	
	$( "#clearStartDate" ).click(function() {
		clearField("startDatePicker");
		return false;
	});
	$( "#clearEndDate" ).click(function() {
		clearField("endDatePicker");
		return false;
	});
	$( "#clearProfileTokenField" ).click(function() {
		clearField("profiletokenfield");
		clearField("profiletokenfieldid");
		return false;
	});
	$( "#clearSelectedCatsField" ).click(function() {
		clearField("SelectedCats");
		clearTokenField();		
		return false;
	});

	$( "#clearFields" ).button().click(function() {	
		clearField("startDatePicker");
		clearField("endDatePicker");
		clearField("profiletokenfield");
		clearField("profiletokenfieldid");
		clearField("SelectedCats");
		clearTokenField();		
		return false;		
	});
	
	//Check for cookies and reset fields if they exist

	var profiletokenfield = $.cookie("profiletokenfield");
	if (profiletokenfield) {
		$("#profiletokenfield").val(profiletokenfield);
	}
	var profiletokenfieldid = $.cookie("profiletokenfieldid");
	if (profiletokenfieldid) {
		$("#profiletokenfieldid").val(profiletokenfieldid);
	}

	if ($.cookie("startDatePicker")) {
		$("#startDatePicker").val($.cookie("startDatePicker"));
	}

	if ($.cookie("endDatePicker")) {
		$("#endDatePicker").val($.cookie("endDatePicker"));
	}

	if ($.cookie("SelectedCats")) {
		var jsonData = $.cookie("SelectedCats");
		var jsonObj = JSON.parse(jsonData);
		$("#tokenfield").tokenfield('destroy');	
		$("#tokenfield").val('');
		$("#tokenfield").tokenfield();		
		$.each(jsonObj, function(i, obj) {
			$("#tokenfield").tokenfield('createToken', { value: obj.id, label: obj.label });			
		});		
		$("#SelectedCats").val(jsonData);
	}

	$( "#startDatePicker" ).datepicker({ dateFormat: "mm/dd/yy" });
	$( "#endDatePicker" ).datepicker({ dateFormat: "mm/dd/yy" });

	$( "#startDatePicker" ).change(function() {
		$.cookie("startDatePicker", $( this ).val());
	});
	$( "#endDatePicker" ).change(function() {
		$.cookie("endDatePicker", $( this ).val());
	});
	
	$(".searchTitleHelp").click(function() {
		helpPopup();
	});
	$(".searchTitleHelp").hover(function() {
		$(this).css('cursor','pointer');
		console.log ("done");
	});
});


function helpPopup() {
    w2popup.open({
        title: 'Search Help',
        body: '<div class="w2ui-centered">' +
               '<p>All fields are optional and may be combined.</p>'+
               '<p>Enter a start date to show all content since that date.</p> ' +
               '<p>Enter an end date to show all content before that date.</p> ' +
               '<p>Enter start and end to show content between those two dates.</p>' +
               '<p>Use Select Categories to select one or more categories or use Select Profile to select categories from a single profile. '+
               'Note that using Select Profile will overwrite categories previously selected.</p>' +
               '<p>Data is retained between searches. Use Reset to clear.</p>' +
               '</div>'
    });
}


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
function loopThroughCats () {
	var profileRadioButtonValue = $('input:radio[name=profile]:checked').val();
	var profileRadioButtonId = $('input:radio[name=profile]:checked').attr('id');
	var label = $('label[for="'+profileRadioButtonId+'"]').text();
	$("#profiletokenfield").val(label);
	$.cookie("profiletokenfield", label);
	$("#profiletokenfieldid").val(profileRadioButtonId);
	$.cookie("profiletokenfieldid", profileRadioButtonId);
	
	var arrayIds = profileRadioButtonValue.split(',');
	// Clear out the checkboxes first
	$(":checkbox:checked").each(function() {
		$(this).prop('checked', false); 
	});
	for (var i = 0; i < arrayIds.length; i++) {
	    var jquerySelector = arrayIds[i];
		//var checkProp = $("#" + jquerySelector).prop("checked");
		$("#" + jquerySelector).prop("checked", true);
		//var checkProp = $("#" + jquerySelector).prop("checked");
	}
	updateCategory();
}

// Update the categories in the checkbox modal after selecting a newsletter profile
function updateCategoryCheckboxes () {
	// The value of the selected radio button is a list of IDs. Loop through all IDs and check the 
	// checkboxes with the matching id.
	if(categoryListHtml == null) {
		$.ajax({
			url:"/wps/wcm/myconnect/prudential/PrudentialNewsDesign/JSPAssets/CategoryList",
			traditional: true,
			beforeSend: function() {
    			$('#loader').show();
    			$(".disableEnable").prop('disabled',true);
				//$(".disableEnable").attr('disabled','disabled');
  			},
  			complete: function(){
     			$('#loader').hide();
    			$(".disableEnable").prop('disabled',false);
    			//$(".disableEnable").attr('disabled','enabled');
  			},
			success: function(html){
				$("#category-list-ajax").html(html);
				categoryListHtml = html;
				loopThroughCats();
			}
		});		
	} else {
		loopThroughCats();
	}	
	//var selectedNewsletterProfile = $()
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
	$.cookie("SelectedCats", JSON.stringify(categories));
	//console.log ("Setting tokenfield");
	//$.cookie("tokenfield", 	$("#tokenfield").val());
	// update with the tokenfield
	
}

function updateCategoryListSelection(catIds) {
	for(var i = 0; i < catIds.length; ++i) {
		$("#"+catIds[i]+"").prop("checked", true);
	}
}

$(function(){

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
			console.log ("Index to delete: " + indexToDelete);
			catsJson.splice(indexToDelete, 1);
			console.log ("Now JSON Is " + JSON.stringify(catsJson));
    		$("#SelectedCats").val(JSON.stringify(catsJson));
	        // Update SelectedCats cookie
	        $.cookie("SelectedCats", JSON.stringify(catsJson));
	   })	
	.tokenfield();	
	
	// Select Profiles
	function showProfileList(html) {
		$("#profile-list-ajax").html(html);
	}
	
	
	
	$( "#openProfileModal" ).button().click(function() {
		$( "#profileModal" ).dialog( "open" );
	});
	
	$("#profileModal").dialog({
		autoOpen: false,
		height: 600,
		width: 1000,
		modal: true,   
		buttons: {
			Accept : function() {
				 $("#profileModal").dialog( "close" );
				 updateCategoryCheckboxes();
		 	},
		 	Cancel: function() {
				 $("#profileModal").dialog( "close" );
		 	}
        },
		open: function( event, ui ) {
			$("#profile-list-ajax").html("<div class='loading'><br><br><img src='/webradar/public/images/spinner.gif'></div>");

			if(profileListHtml == null) {
			$.ajax({
				url:"/wps/wcm/myconnect/prudential/PrudentialNewsDesign/JSPAssets/ProfileList",
				traditional: true,
				success: function(html){
					showProfileList(html);
					profileListHtml = html;
				}
			});
			} else {
				showProfileList(profileListHtml);
			}
		},
		close: function() {
		      	 $("#profileModal").dialog( "close" );
      	} // end close
	
	}); // end dialog	
	
	
	function showCategoryList(html) {
		$("#category-list-ajax").html(html);
		// get the catIds first
		//updateCategoryListSelection(catIds);
		updateCategoryListSelection(getCategoryIds());
	}
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
	
	//System.out.println ("Got startDateToSearch: --" + startDateToSearch + "--");
	//System.out.println ("Got endDateToSearch: --" + endDateToSearch + "--");
	//System.out.println ("Got selectedCats: --" + selectedCats + "--");
	
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
	ArrayList additionalSelectorsNews = new ArrayList();
	
	if(newsTemplate != null) {
		authTemplateSelector = Selectors.authoringTemplateEquals(newsTemplate);
	    //System.out.println ("Got AT selector = " + authTemplateSelector.toString());
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
  			//System.out.println ("Got id: " + id);
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


/*set up layout*/
        var layoutNews = [
 /*       { field: 'actions', caption: 'actions', size: '30%', sortable: true, resizable: true,render: function (record, index, column_index) {
					var html = '<div style="display: inline-block"><span class="ui-icon ui-icon-circle-check" style="display: inline-block"></span><span class="ui-icon ui-icon-circlesmall-plus" style="display: inline-block"></span><a href="'+ record.deleteURL + '"><span class="ui-icon ui-icon-trash" style="display: inline-block"></span></a></div>';
					return html;
				}  }, */
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
<style>
.spinner {
    position: fixed;
    top: 50%;
    left: 50%;
    margin-left: -50px; /* half width of the spinner gif */
    margin-top: -50px; /* half height of the spinner gif */
    text-align:center;
    z-index:1234;
    overflow: auto;
    width: 100px; /* width of the spinner gif */
    height: 102px; /*hight of the spinner gif +2px to fix IE8 issue */
}
.w2ui-buttons {
    background-color: #fafafa;
    border-bottom: 0 solid #d5d8d8;
    border-bottom-left-radius: 3px;
    border-bottom-right-radius: 3px;
    border-top: 1px solid #d5d8d8;
    bottom: 0;
    left: 0;
    padding: 15px 0 !important;
    position: absolute;
    right: 0;
    text-align: center;
}
.form-control {
  width: auto;
}
.tokenfield {
	overflow-x: hidden;
    overflow-y: scroll;
	max-height: 125px;
	max-width: 200px;
}
.searchTitle {
	text-align: center;
	background-image: linear-gradient(#dae6f3, #c2d5ed);
	height: 28px;
	font-size: 13px;
	padding: 7px;
}
.searchTitleHelp {
	float: right;
	
}
.searchInputFields {
	width: 200px;
}
.btn {
	width: 125px;
}
.tdSearchFields {
	padding-right: 10px;
	padding-bottom: 5px;
 }
</style>
