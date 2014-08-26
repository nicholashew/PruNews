<%@ page import="java.util.*,com.prudential.portal.utils.ree.REEConfigMap" %>
<html>
<body>
<%
String JNDI_LOOKUPNAME = "ree/Prudential/ThemeConfig";
String resourceKey = "wcm.authoring.active";
String theValue = null;
      try {
         theValue = REEConfigMap.getProperty(JNDI_LOOKUPNAME, resourceKey);
         out.println(theValue);
         theValue = REEConfigMap.getProperty(JNDI_LOOKUPNAME, "footer.wcmlibs");
		 out.println(theValue);
      }
      catch (Exception e) {
         out.println(e);
      }
      
 %>
</body>
</html>


