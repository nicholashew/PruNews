<%@ page import="java.util.*"%>
<%@page import="java.io.File"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="java.net.URL"%>
<%@page import="java.io.FileReader"%>
<%@page import="java.io.BufferedReader"%>
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
<wcm:libraryComponent name="Custom Authoring Assets/HTML - InitjQuery" library="PrudentialNewsDesign" />

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
    String fvalue = sb.toString();
%>


<script>
function getCategoryIds() {
	var catIds = [];
	try {
		var catsJson = jQuery.parseJSON(unescape($j("#SuggestedCats").val()));
		$j.each(catsJson, function(i, obj) {
			catIds.push(obj.id);
		});
	} catch(ex) {
	}
	return catIds;
}
function updateCategory() {
	var categories = [];
	$j(":checkbox:checked").each(function() {
		var obj = {id:"", label:""};
		obj.id = $j(this).val();
		obj.label = $j('label[for="'+obj.id+'"]').text();
		//obj.label = $j(this).find("label").text();
		categories.push(obj);
	});
	$j("#SuggestedCats").val(JSON.stringify(categories));
}

function updateCategory_old() {
	var categories = [];
	$j("#category-modal-container .category-list li:has( input[type=checkbox]:checked )").each(function() {
		var obj = {id:"", label:""};
		obj.id = $j(this).find("input[type=checkbox]").val();
		obj.label = $j(this).find("label").text();
		categories.push(obj);
	});
	$j("#SuggestedCats").val(JSON.stringify(categories));
}
function updateCategoryListSelection(catIds) {
	$j("#category-modal-container ul.category-list > li > input[type='checkbox']").prop("checked", false);
	for(var i = 0; i < catIds.length; ++i) {
		$j("#category-modal-container ul.category-list > li > input#"+catIds[i]).prop("checked", true);
	}
}
$j(function(){
	function showCategoryList(html) {
		$j("#category-list-ajax").html(html);
		// get the catIds first
		//updateCategoryListSelection(catIds);
		updateCategoryListSelection(getCategoryIds());
	}
	var categoryListHtml = null;	
	$j( "#openCatModal" ).button().click(function() {
		$j( "#categoryModal" ).dialog( "open" );
	});
	
	$j("#categoryModal").dialog({
		autoOpen: false,
		height: 600,
		width: 1000,
		modal: true,   
		buttons: {
		 Accept : function() {
			 updateCategory();
			 $j("#categoryModal").dialog( "close" );
		 },
		 Cancel: function() {
			 $j("#categoryModal").dialog( "close" );
		 }
        	},
		open: function( event, ui ) {
			$j("#category-list-ajax").html("<div class='loading'><br><br><img src='http://www.jlg.com/images/layout/loadingGif.gif'></div>");
			var catIds = getCategoryIds();			
			if(categoryListHtml == null) {
			$j.ajax({
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
		      	 $j("#categoryModal").dialog( "close" );
      		} // end close
	
	}); // end dialog

});
</script>

<script type="text/javascript">
// really get the items from the input
function myoptionsubmit()
{
	$j("#<%=customItem.getFieldName()%>").val($j("#category-list-ajax").val());
	//$j("#<%=customItem.getFieldName()%>").val(stringedCats);
}

</script>

json:<%=fvalue%><br>

<textarea id="SuggestedCats" style="width:500px; height:500px;"></textarea>

<input type='button' name='basic' id="openCatModal" value='Demo' class='basic-modal'/>

<div id="categoryModal">	
	<div class='content' id="category-list-ajax"></div>	
</div>