<%@ page import="java.util.*"%>
<%@ page import="java.io.File"%>
<%@ page import="java.io.InputStreamReader"%>
<%@ page import="java.net.URL"%>
<%@ page import="java.io.FileReader"%>
<%@ page import="java.io.BufferedReader"%>
<%@ page import="com.ibm.workplace.wcm.api.*,com.ibm.workplace.wcm.api.exceptions.*"%>
<%@ page import="com.prudential.utils.Utils"%>
<%@ page import="com.prudential.commons.wcm.authoring.*"%>
<%@ page import="com.prudential.wcm.WCMUtils"%>
<%!
public String[] getParameterValues(javax.servlet.ServletRequest request, String name) {
	System.out.println("getParameterValues for "+name);  
	String[] rawValues = request.getParameterValues(name);
	java.util.LinkedList<String> values = new java.util.LinkedList<String>();
	if(rawValues != null) {
		for(int i = 0; i < rawValues.length; ++i) {
			System.out.println("getParameterValues rawValue "+rawValues[i]);  
			values.addAll(java.util.Arrays.asList(String.valueOf(rawValues[i]).split(",")));
		}
	}
	
	System.out.println("getParameterValues values "+values); 
	return (String[])values.toArray(new String[values.size()]);
}


%>

<%@ taglib uri="/WEB-INF/tld/wcm.tld" prefix="wcm"%>
<wcm:initworkspace
	user="<%= (java.security.Principal)request.getUserPrincipal() %>" />
<wcm:setExplicitContext path="/PrudentialNewsDesign/JSPAssets/CMKTestJSP" />
<script>
var selectedCategories = [<%
	String[] catIds = getParameterValues(request, "catIds");
	for(int i = 0; i < catIds.length; ++i) {
		out.print("'" + catIds[i] + "'");
		if(i < catIds.length-1) {
			out.print(",");
		}
	}
%>];

function filterCategories(filterText) {
	filterText = filterText.toLowerCase();
	$j(".category-list li").each(function(){
		var text = $j(this).text().toLowerCase();
		var display = true;
		if(filterText != "" && text.indexOf(filterText) < 0) {
			display = false;
		}
		if(display) {
			$j(this).show();
		} else {
			$j(this).hide();
		}
	});
}

$j(function(){
	$j(".category-filter .clear-icon").click(function(){
		$j(".category-filter input").val("");
		filterCategories("");
	});
	$j(".category-filter .search-icon").click(function(){
		filterCategories($j(".category-filter input").val());
	});
	
	for(var i = 0; i < selectedCategories.length; ++i) {
		$j("ul.category-list > li > input#"+selectedCategories[i]).prop("checked", true);
	}
	
	$j("[placeholder]").focus(function() {
	  var input = $j(this);
	  if (input.val() == input.attr("'placeholder'")) {
		input.val("''");
		input.removeClass("'placeholder'");
	  }
	}).blur(function() {
	  var input = $j(this);
	  if (input.val() == "''" || input.val() == input.attr("'placeholder'")) {
		input.addClass("'placeholder'");
		input.val(input.attr("'placeholder'"));
	  }
	}).blur();
});
</script>
<style>
/* <![CDATA[ */
ul.category-list {
    width:500px;
    list-style:none;
    padding:0;
    border:1px solid black;
	-webkit-border-radius: 5px;
	-moz-border-radius: 5px;
	border-radius: 5px;
}
ul.category-list > li {
    width: 100%;
    height:30px;
    border:1px solid #ddd;
    border-width: 0 0 1px;
    padding: 4px 0 0;
}
ul.category-list > li > label {
	margin-left:10px;
}
ul.category-list > li > input[type='checkbox'] {
    float:right;
	margin-right:10px;
}
.category-filter {
	position: relative;
}
.category-filter > input {
	width:300px;
	height:30px;
	-webkit-border-radius: 5px;
	-moz-border-radius: 5px;
	border-radius: 5px;
}
.category-filter > .clear-icon {
	display:inline-block;
	width:20px;
	height:20px;
	left: -27px;
	top: 5px;
	position: relative;
	z-index:999999;
	background-position:center; 
}
.category-filter > .search-icon {
	display:inline-block;
	background-position:10px center; 
	background-color:#cdcdcd;
	width:100px;
	height:30px;
	-webkit-border-radius: 5px;
	-moz-border-radius: 5px;
	border-radius: 5px;
	left: -20px;
	position: relative;
}

.clear-icon {
	background: url("data:image/svg+xml;charset=US-ASCII,%3C%3Fxml%20version%3D%221.0%22%20encoding%3D%22iso-8859-1%22%3F%3E%3C!DOCTYPE%20svg%20PUBLIC%20%22-%2F%2FW3C%2F%2FDTD%20SVG%201.1%2F%2FEN%22%20%22http%3A%2F%2Fwww.w3.org%2FGraphics%2FSVG%2F1.1%2FDTD%2Fsvg11.dtd%22%3E%3Csvg%20version%3D%221.1%22%20id%3D%22Layer_1%22%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20xmlns%3Axlink%3D%22http%3A%2F%2Fwww.w3.org%2F1999%2Fxlink%22%20x%3D%220px%22%20y%3D%220px%22%20%20width%3D%2214px%22%20height%3D%2214px%22%20viewBox%3D%220%200%2014%2014%22%20style%3D%22enable-background%3Anew%200%200%2014%2014%3B%22%20xml%3Aspace%3D%22preserve%22%3E%3Cpolygon%20fill%3D%22%23999%22%20points%3D%2214%2C3%2011%2C0%207%2C4%203%2C0%200%2C3%204%2C7%200%2C11%203%2C14%207%2C10%2011%2C14%2014%2C11%2010%2C7%20%22%2F%3E%3Cg%3E%3C%2Fg%3E%3Cg%3E%3C%2Fg%3E%3Cg%3E%3C%2Fg%3E%3Cg%3E%3C%2Fg%3E%3Cg%3E%3C%2Fg%3E%3Cg%3E%3C%2Fg%3E%3Cg%3E%3C%2Fg%3E%3Cg%3E%3C%2Fg%3E%3Cg%3E%3C%2Fg%3E%3Cg%3E%3C%2Fg%3E%3Cg%3E%3C%2Fg%3E%3Cg%3E%3C%2Fg%3E%3Cg%3E%3C%2Fg%3E%3Cg%3E%3C%2Fg%3E%3Cg%3E%3C%2Fg%3E%3C%2Fsvg%3E");
	background-repeat:no-repeat;
}
.search-icon {
	background:url("data:image/svg+xml;charset=US-ASCII,%3C%3Fxml%20version%3D%221.0%22%20encoding%3D%22iso-8859-1%22%3F%3E%3C!DOCTYPE%20svg%20PUBLIC%20%22-%2F%2FW3C%2F%2FDTD%20SVG%201.1%2F%2FEN%22%20%22http%3A%2F%2Fwww.w3.org%2FGraphics%2FSVG%2F1.1%2FDTD%2Fsvg11.dtd%22%3E%3Csvg%20version%3D%221.1%22%20id%3D%22Layer_1%22%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20xmlns%3Axlink%3D%22http%3A%2F%2Fwww.w3.org%2F1999%2Fxlink%22%20x%3D%220px%22%20y%3D%220px%22%20%20width%3D%2214px%22%20height%3D%2214px%22%20viewBox%3D%220%200%2014%2014%22%20style%3D%22enable-background%3Anew%200%200%2014%2014%3B%22%20xml%3Aspace%3D%22preserve%22%3E%3Cpath%20style%3D%22fill%3A%23000000%3B%22%20d%3D%22M10.171%2C8.766c0.617-0.888%2C0.979-1.964%2C0.979-3.126c0-3.037-2.463-5.5-5.5-5.5s-5.5%2C2.463-5.5%2C5.5%20s2.463%2C5.5%2C5.5%2C5.5c1.152%2C0%2C2.223-0.355%2C3.104-0.962l3.684%2C3.683l1.414-1.414L10.171%2C8.766z%20M5.649%2C9.14c-1.933%2C0-3.5-1.567-3.5-3.5%20c0-1.933%2C1.567-3.5%2C3.5-3.5c1.933%2C0%2C3.5%2C1.567%2C3.5%2C3.5C9.149%2C7.572%2C7.582%2C9.14%2C5.649%2C9.14z%22%2F%3E%3C%2Fsvg%3E");
	background-repeat:no-repeat;
}
/* ]]> */
</style>
<div class="category-filter">
<input type="text" value="" placeholder="Category Filter"/>
<div class="clear-icon"></div>
<button class="search-icon">Search</button>
</div>
<ul class="category-list">
<% 
	String libName = "PrudentialNewsDesign";
	String taxName = "PrudentialCategories";
	
	// get a system workspace
	Workspace ws = Utils.getSystemWorkspace();
	ws.login();

	ws.setCurrentDocumentLibrary(ws.getDocumentLibrary(libName));
	//DocumentIterator<Document> itor = ws.getByIds(ws.findByType(DocumentTypes.Category), true, false); 
    ArrayList<ObjectWrapper> categories = null;
    categories = CategoryJSONWrapper.getCategoryWrapperList(ws, libName);
    Iterator itor = categories.iterator();
	while(itor.hasNext()) {
		ObjectWrapper category = (ObjectWrapper)itor.next();
		String title = category.getLabel();
                String id = category.getId();
%>
<li><label for="<%= id %>"><%= title %></label> <input type="checkbox" value="<%= id %>" id="<%= id %>" /> </li>
<%
	}	
%>
</ul>