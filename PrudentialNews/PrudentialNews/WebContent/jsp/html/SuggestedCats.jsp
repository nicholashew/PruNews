<%@ taglib uri="/WEB-INF/tld/portlet.tld" prefix="portletAPI" %>
<portletAPI:init/>
<%@ taglib uri="/WEB-INF/tld/wcm.tld" prefix="wcm" %>
<%@ page import="java.lang.StringBuilder"%>
<%@ page import="com.ibm.workplace.wcm.api.authoring.CustomItemBean" %>
<wcm:initworkspace
	user="<%= (java.security.Principal)request.getUserPrincipal() %>" />
<wcm:setExplicitContext path="/PrudentialNewsDesign/JSPAssets/CMKTestJSP" />
<wcm:libraryComponent name="Custom Authoring Assets/HTML - InitjQuery" library="PrudentialNewsDesign" />
<style>
.klinikos-ehr .ui-autobox {
	display:block;
	position:relative;
	padding: 10px;
}
.my-widget {
	position:relative;
	background:none rgb(219,219,219);
	padding: 10px;
}
.autobox-text { 
	font-size:14px;
	background:none #fff;
	padding:2px;
	-webkit-border-radius: 25px;
	-moz-border-radius: 25px;
	border-radius: 25px;
	white-space:normal;
	line-height:0px;
}
.autobox-text .autobox-bubble {
	font-size:14px;
	line-height:14px;
	float:left;
	padding:1px 2px 3px 10px;
	margin: 4px 6px;
	color:rgb(81, 99, 119);
	background: none rgb(215, 220, 227);
	border: solid 0px #777;
	-webkit-border-radius: 20px;
	-moz-border-radius: 20px;
	border-radius: 20px;
	white-space:nowrap;
}	
.autobox-text input {
	font-size:14px;
	font-weight:bold;
	padding:1px 2px 3px 10px;
	margin: 4px 6px;
	color:rgb(81, 99, 119);
	background: none transparent;
	border: solid 0px #777;
	width:20px;
}	
.autobox-text .autobox-bubble .autobox-bubble-remove {
	font-size:18px;
	font-weight:800;
	margin:0 6px 0 10px;
	color:rgb(81, 99, 119);
	background: none transparent;
}
.autobox-option-list {
	border-bottom: solid 2px #EAEAEA;
}
.autobox-option-list .list-option {
}
#doc-lib-search-btn {
    border: 0 solid;
    float: left;
    margin: 4px;
}
#doc-lib-help-btn {
        position:absolute;
        top: 16px;
        right: 10px;
}
</style> 

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
 
<script type="text/javascript">
// really get the items from the input
function myoptionsubmit()
{
	var categories = [];
	// iterate the autobox items
	$(".autobox-bubble").each(function() {
		var obj = {id:"", label:""};
		obj.id = $(this).data("id");
		obj.label = $(this).data("label");
		categories.push(obj);
	});
	var stringedCats = JSON.stringify(categories);
	alert(stringedCats);
	$("#<%=customItem.getFieldName()%>").val($("#SuggestedCats").val());
	//$("#<%=customItem.getFieldName()%>").val($("#<%=customItem.getFieldName()%>_mycustomoption").val());
	$("#<%=customItem.getFieldName()%>").val(stringedCats);
}
$(function() {
	var theData = '<%=fvalue%>';
	var jsonData = $.parseJSON(theData);
	$("#suggestedCatTokenfield").tokenfield();	
	//var $select = $('#select-test');
	var categories = [];
	$(jsonData).each(function (index, o) {  
		var obj = {id:"", label:""};
		obj.id = o.id;
		obj.label = o.label;
		//obj.label = $(this).find("label").text();
		categories.push(obj);
		$("#suggestedCatTokenfield").tokenfield('createToken', { value: obj.id, label: obj.label });
	});
	$("#SuggestedCats").val(JSON.stringify(categories));
		
	//$("#select-test").autobox({styleClasses:{"autobox-bg":"my-widget"}});
}); // end function	

</script>

json:<%=fvalue%><br>
<input type="hidden" id="SuggestedCats" />
<input type="text" id="suggestedCatTokenfield" />
<div class="autobox" id="catField" name="docids"></div>
