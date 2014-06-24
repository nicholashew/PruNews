<%--
/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

--%>
<%@ page import="com.ibm.portal.model.VirtualPortalListHome"%>
<%@ page import="com.ibm.portal.model.VirtualPortalListProvider"%>
<%@ page import="javax.naming.CompositeName"%>
<%@ page import="javax.naming.Context"%>
<%@ page import="javax.naming.InitialContext"%>
<%@ page import="javax.naming.Name"%>
<%@ page import="javax.naming.NamingException"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="com.ibm.portal.ObjectID"%>
<%@ page import="com.ibm.portal.admin.VirtualPortal"%>
<%@ page import="com.ibm.portal.admin.VirtualPortalList"%>

<%
try {
	VirtualPortalListProvider vplp = null;
	Context ctx = new InitialContext();
    Name myjndiname = new CompositeName(VirtualPortalListHome.VIRTUAL_PORTAL_LIST_JNDI_NAME);
    VirtualPortalListHome home =  (VirtualPortalListHome) ctx.lookup(myjndiname);
    vplp = home.getVirtualPortalListProvider();
    
    VirtualPortalList<VirtualPortal> vp = vplp.getVirtualPortalList();
    Iterator<VirtualPortal> vpIterator = vp.iterator();
    while(vpIterator.hasNext()) {
        VirtualPortal currentVp = vpIterator.next();        
    	ObjectID id = currentVp.getObjectID();
    	String vpTitle = currentVp.getTitle(request.getLocale());
    	out.println("id = "+id+"<br>");
    	out.println("vpTitle = "+vpTitle+"<br>");
    }
 }
 catch (Exception e) {
    e.printStackTrace();
 }    
%>
