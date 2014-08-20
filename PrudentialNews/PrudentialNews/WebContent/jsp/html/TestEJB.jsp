<%@ page import="java.util.*"%>
<%@page import="java.io.File"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="java.net.URL"%>
<%@page import="java.io.FileReader"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Properties"%>
<%@page import="javax.ejb.EJB"%>
<%@page import="javax.ejb.embeddable.EJBContainer"%>
<%@page import="javax.naming.Context"%>
<%@page import="javax.naming.InitialContext"%>
<%@page import="javax.naming.NamingException"%>
<%@ page
	import="com.ibm.workplace.wcm.api.*,com.ibm.workplace.wcm.api.exceptions.*"%>
<%@ page import="com.prudential.utils.Utils"%>
<%@ page import="com.prudential.wcm.WCMUtils"%>

<%
   Properties props = new Properties();
	props.put(Context.INITIAL_CONTEXT_FACTORY,
            "com.ibm.websphere.naming.WsnInitialContextFactory");
   ITestEJBRemoteInterface loEJB =null; 
   Object lobj; 
   try { 
   	InitialContext ctx = new InitialContext(props); 
   	lobj = ctx.lookup("checkName"); 
   	if (lobj instanceof ITestEJBRemoteInterface) { 
   		loEJB = (ITestEJBRemoteInterface) lobj; 
   	} 
   	String lsName = "Imran"; 
   	// Invoke the Method using bean object ; 
   	System.out.println("Is "+ lsName + " present in the list:: "+loEJB.checkNames(lsName)); 
   	System.out.println("EJB run successful"); 
   	} catch (NamingException e) { 
   	// TODO Auto-generated catch block 
   	e.printStackTrace(); 
   	} 
   	
%>