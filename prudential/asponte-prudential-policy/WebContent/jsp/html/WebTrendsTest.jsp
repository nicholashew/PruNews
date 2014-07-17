<%@ page
	import="com.ibm.workplace.wcm.api.*,com.ibm.portal.extension.*,
                 com.ibm.workplace.wcm.api.*,
                 com.ibm.workplace.wcm.api.query.*,
                 java.util.*,com.prudential.utils.*,
                 java.text.*,com.prudential.authoring.launchpage.*"%>
<%@ taglib uri="/WEB-INF/tld/wcm.tld" prefix="wcm" %>
<%@ taglib uri="/WEB-INF/tld/std-portlet.tld" prefix="portlet" %>
<portlet:defineObjects/>
<wcm:initworkspace user="<%= (java.security.Principal)request.getUserPrincipal() %>" />

<!-- START OF SmartSource Data Collector TAG -->
<!-- Copyright (c) 1996-2011 WebTrends Inc.  All rights reserved. -->
<!-- Version: 9.3.0 -->
<!-- Tag Builder Version: 3.1  -->
<!-- Created: 3/23/2011 2:50:06 PM -->
<script src="js/webtrends.js" type="text/javascript"></script>
<!-- ----------------------------------------------------------------------------------- -->
<!-- Warning: The two script blocks below must remain inline. Moving them to an external -->
<!-- JavaScript include file can cause serious problems with cross-domain tracking.      -->
<!-- ----------------------------------------------------------------------------------- -->
<script type="text/javascript"> 
//<![CDATA[
<!--dcs1666a79hw9jzwa999cmh1q_4e3w	for all lower tiers-->
<!--dcsqsvyd89hw9jzwa999cmh1q_3e4m      for Production-->
var pixelTrackingDcsId = "dcs1666a79hw9jzwa999cmh1q_4e3w"
var _tag=new WebTrends(pixelTrackingDcsId);
_tag.dcsGetId();
//]]>
</script>
<%
	PortletWCMContextHelper contextHelper = new PortletWCMContextHelper();
	String contextString = contextHelper.getCurrentWCMContext(renderRequest, renderResponse);
	out.println(contextString);
%>
<script type="text/javascript"> 
//<![CDATA[
_tag.dcsCustom=function(){
// Add custom parameters here.
//_tag.DCSext.param_name=param_value;
_tag.DCS.dcsuri=pageDesc_VAR;
//_tag.DCSext.pruURI=_tag.DCS.dcsuri;
_tag.DCSext.pruURI=<%=contextString%>;
_tag.DCSext.userID=userID_VAR;
}
_tag.dcsCollect();
//]]>
</script>
<noscript>
<div><img alt="DCSIMG" id="DCSIMG" width="1" height="1" src="//sdc.prudential.com/dcs1666a79hw9jzwa999cmh1q_4e3w/njs.gif?dcsuri=/nojavascript&amp;WT.js=No&amp;WT.tv=9.4.0&amp;dcssip=www.prudential.com"/></div>
</noscript>
<!-- END OF SmartSource Data Collector TAG -->
