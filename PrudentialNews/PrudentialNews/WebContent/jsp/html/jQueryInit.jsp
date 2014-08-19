<%@ taglib uri="/WEB-INF/tld/portlet.tld" prefix="portletAPI" %>
<portletAPI:init/>
<%@ taglib uri="/WEB-INF/tld/wcm.tld" prefix="wcm" %>
<wcm:initworkspace
	user="<%= (java.security.Principal)request.getUserPrincipal() %>" />
<wcm:setExplicitContext path="/PrudentialNewsDesign/JSPAssets/CMKTestJSP" />
<wcm:libraryComponent name="Custom Authoring Assets/HTML - InitjQuery" library="PrudentialNewsDesign" />

