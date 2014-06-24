<%@ page import="javax.naming.InitialContext, com.ibm.websphere.cache.DistributedMap" %>
<html>
<body>
Clearing caches... <br>
<%
String [] cacheNames = {"services/cache/iwk/strategy", "services/cache/iwk/global", "services/cache/iwk/module", "services/cache/iwk/processing", "services/cache/iwk/site", "services/cache/iwk/session", "services/cache/iwk/summary", "services/cache/iwk/abspathreverse", "services/cache/iwk/menu", "services/cache/iwk/nav", "services/cache/iwk/abspath"};

      InitialContext ctx = new InitialContext();
      for (int i = 0; i < cacheNames.length; i++)
      {
         DistributedMap dm = (DistributedMap) ctx.lookup(cacheNames[i]);
         dm.clear();
      }

%>
<br>
Caches cleared.
</body>
</html>


