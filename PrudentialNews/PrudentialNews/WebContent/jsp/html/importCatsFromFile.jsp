<%@ page import="java.util.*"%>
<%@page import="java.io.File"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="java.net.URL"%>
<%@page import="java.io.FileReader"%>
<%@page import="java.io.BufferedReader"%>
<%@ page import="com.ibm.workplace.wcm.api.*,com.ibm.workplace.wcm.api.exceptions.*"%>
<%@ page import="com.prudential.utils.Utils"%>
<%@ page import="com.prudential.wcm.WCMUtils"%>


<%@ taglib uri="/WEB-INF/tld/wcm.tld" prefix="wcm"%>

<% 
	String libName = "PrudentialNewsDesign";
	String taxName = "PrudentialCategories";
	
	// get a system workspace
	Workspace ws = Utils.getSystemWorkspace();
	ws.login();
	
	// now pick up from the filestem the name of the cats
	// 871d653e-5ad8-40bf-a83f-47f1bcbebd98 is the tax
	// 537dafe3-657c-4117-9e4f-ebdab27b57cc is the active one
	DocumentId taxId = ws.createDocumentId("537dafe3-657c-4117-9e4f-ebdab27b57cc");
	if(taxId != null) {
		// now open the file
		String fsPath = "/home/cknight/cats.txt";
		File myJspFile = new File(fsPath);
		BufferedReader reader = new BufferedReader(new FileReader(myJspFile));
            StringBuilder sb = new StringBuilder();
            String line;

            while((line = reader.readLine())!= null){
                String catName = line;
                // try to create the cat
                Category tempCat = ws.createCategory(taxId);
                tempCat.setName(catName);
                try {
                	String[] errors = ws.save(tempCat);
                	if(errors.length > 0) {
                		for(int x = 0;x<errors.length;x++){
							System.out.println("CMK:: error "+errors[x]);
						}
                	} else {
                		System.out.println("CMK:: saved "+catName);
                		sb.append(catName+"<br>");
                	}
                }
                catch (Exception e) {
                	e.printStackTrace();
                }
            }
            out.println("created:<br>"+sb.toString());
           		
	}
	//ws.createCategory();
	
%>