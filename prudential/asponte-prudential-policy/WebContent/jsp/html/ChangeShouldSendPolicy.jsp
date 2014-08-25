<%@ page import="java.util.*,com.prudential.shouldact.DynamicShouldEmail" %>
<html>
<body>
Set Server Active or Inactive <br>
<%
// handle the setter first
DynamicShouldEmail dse = DynamicShouldEmail.getInstance();
Enumeration atts = request.getParameterNames();
         while(atts.hasMoreElements()) {
         	String key = (String)atts.nextElement().toString();
         	String value = (String)request.getParameter(key).toString();
         	out.println("Key: "+key+", value: "+value+"<br>");
         }
String incomingValue = (String)request.getParameter("activateServer");
if(incomingValue != null && incomingValue.length()>0) {
	System.out.println("Process activate server "+incomingValue);
	out.println("Process activate server "+incomingValue+"<br>");
}
dse.setShouldSendMail(Boolean.parseBoolean(incomingValue));

boolean currentValue = dse.isShouldSendMail();
out.println("currentValue = "+currentValue+"<br>");
%>
<br>
<form action="">
<select name="activateServer">
<option value="false">Inactive</option>
<option value="true">Active</option>
</select>
<input type="submit" value="Submit Changes">
</form>
</body>
</html>


