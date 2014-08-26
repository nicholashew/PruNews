<%@ page import="java.util.*,com.prudential.portal.utils.ree.*" %>
<html>
<body>
<%
String JNDI_LOOKUPNAME = "ree/Prudential/ThemeConfig";
String resourceKey = "wcm.authoring.active";
String theValue = null;
      try {
         theValue = REEConfigMap.getProperty(JNDI_LOOKUPNAME, resourceKey);
         out.println(theValue+"<br>");
         theValue = REEConfigMap.getProperty(JNDI_LOOKUPNAME, "footer.wcmlibs");
		 out.println(theValue+"<br>");
		 Config theConfig = REEConfigMap.getProperties(JNDI_LOOKUPNAME);
		 HashMap hm = (HashMap)theConfig.getAttributes();
		 Set keys = hm.keySet();
		 Iterator keyIterator = keys.iterator();
		 while(keyIterator.hasNext()) {
		 	out.println("keys contains "+keyIterator.next()+"<br>");
		 }
		 
      }
      catch (Exception e) {
         out.println(e);
      }
      
 %>
</body>
</html>


