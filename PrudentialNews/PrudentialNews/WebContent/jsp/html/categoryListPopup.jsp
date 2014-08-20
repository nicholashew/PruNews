<%@ page import="java.util.*"%>
<%@ page import="java.io.File"%>
<%@ page import="java.io.InputStreamReader"%>
<%@ page import="java.net.URL"%>
<%@ page import="java.io.FileReader"%>
<%@ page import="java.io.BufferedReader"%>
<%@ page import="com.ibm.workplace.wcm.api.*,com.ibm.workplace.wcm.api.exceptions.*"%>
<%@ page import="com.prudential.utils.Utils"%>
<%@ page import="com.prudential.wcm.WCMUtils"%>
<%@ taglib uri="/WEB-INF/tld/portlet.tld" prefix="portletAPI" %>
<portletAPI:init/>
<%@ taglib uri="/WEB-INF/tld/wcm.tld" prefix="wcm" %>
<%@ page import="com.ibm.workplace.wcm.api.authoring.CustomItemBean" %>

<wcm:initworkspace
	user="<%= (java.security.Principal)request.getUserPrincipal() %>" />
<wcm:setExplicitContext path="/PrudentialNewsDesign/JSPAssets/CMKTestJSP" />

<% 
    CustomItemBean customItem = 
    (CustomItemBean) request.getAttribute("CustomItemBean"); 
    customItem.setSubmitFunctionName("myoptionsubmit");
    StringBuilder sb = new StringBuilder();
    sb.append((String)customItem.getFieldValue());
    // check if the first char is [.  If not, add at beginning and ] at the end
    if(sb.indexOf("[") < 0) {
    	sb.insert(0,"[");
    	sb.append("]");
    }
    //fvalue = fvalue.replaceAll("\"", "&quot;").replaceAll("\"","&#39;");
    String catListPopupvalue = sb.toString();
%>

<script>
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
	$("#category-list-ajax :checkbox:checked").each(function() {
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
	var theData = '<%=catListPopupvalue%>';
	var jsonData = $.parseJSON(theData);
	$("#tokenfield")
		.on('tokenfield:removedtoken', function (e) {
	        var checkId = e.attrs.value;
	        //console.log ("got a change in " + checkId);
	        $("#" + checkId + "").prop('checked', false); 
	        var catsJson = jQuery.parseJSON(unescape($("#SelectedCats").val()));
			//console.log ("JSON Is " + JSON.stringify(catsJson));
		   	var indexToDelete = null;
		   	$.each(catsJson, function(i, obj) {
				//console.log ("Got : " + obj.id);
				if (obj.id == checkId) {
				   indexToDelete = i;
				}
			});
			catsJson.splice(indexToDelete, 1);
			//console.log ("Now JSON Is " + JSON.stringify(catsJson));
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
			 updateCategory();
			 $("#categoryModal").dialog( "close" );
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
</script>

<script type="text/javascript">
// really get the items from the input
function myoptionsubmit()
{
	$("#<%=customItem.getFieldName()%>").val($("#SelectedCats").val());
	//$("#<%=customItem.getFieldName()%>").val(stringedCats);
}

</script>

<input type="hidden" id="SelectedCats" style="width:500px; height:500px;"></textarea>
<input type="text" id="tokenfield" />

<input type='button' name='basic' id="openCatModal" value='Select Categories' class='basic-modal'/>

<div id="categoryModal">	
	<div class='content' id="category-list-ajax"></div>	
</div>
