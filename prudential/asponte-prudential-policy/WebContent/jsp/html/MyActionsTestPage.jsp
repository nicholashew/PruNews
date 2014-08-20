<%-- 
/********************************************************************/ 
/* Asponte 
/* cmknight 
/********************************************************************/ 

--%> 
<%@ page import="java.util.*,org.json.*,com.ibm.workplace.wcm.api.*,
                 com.prudential.utils.*,com.prudential.objects.*, 
         		 com.prudential.wcm.*" %>
<%@ page import="com.ibm.workplace.wcm.api.Content" %>
<%@ page import="com.prudential.utils.Utils" %>
<%@ page import=" java.io.BufferedReader" %>
<%@ page import="java.io.InputStreamReader" %> 
<%@ page import="java.io.OutputStreamWriter" %> 
<%@ page import="java.io.IOException" %>

<%@ page import=" java.net.HttpURLConnection" %>
<%@ page import="java.net.MalformedURLException" %> 
<%@ page import="java.net.URL" %>
<%@ page import="java.net.UnknownHostException" %>
<%@ page import="java.util.*" %>
<%@ page import="org.json.*" %>
<%@ page import="java.util.logging.Level" %>
<%@ page import="java.util.logging.Logger" %>
<%@ page import="javax.xml.parsers.DocumentBuilder" %>
<%@ page import="javax.xml.parsers.DocumentBuilderFactory" %>
<%@ page import="javax.xml.parsers.ParserConfigurationException" %>

<%@ page import=" org.w3c.dom.Document" %>
<%@ page import="org.w3c.dom.Element" %>
<%@ page import="org.w3c.dom.Node" %>
<%@ page import="org.w3c.dom.NodeList" %>
<%@ page import="org.xml.sax.SAXException" %>         		 
<%@ taglib uri="/WEB-INF/tld/wcm.tld" prefix="wcm"%>
<%@ taglib uri="/WEB-INF/tld/portlet.tld" prefix="portletAPI"%>

<%!
 public MyActionsResponse createMyActionRequest(String service, JSONObject json, JspWriter out) { 
       
      MyActionsResponse myResponse = new MyActionsResponse(); 

      try { 
         // Check the current HOST 
         URL url = getMyActionsServiceURL(service, out); 
         if (url == null) 
            return null; 

         // 2. Open connection 
         HttpURLConnection conn = (HttpURLConnection) url.openConnection(); 

         // 3. Specify POST method 
         conn.setRequestMethod("POST"); 

         // 4. Set the headers 
         conn.setRequestProperty("Content-Type", "application/json"); 
         conn.setRequestProperty("Accept", "application/json"); 
         conn.setDoOutput(true); 
         conn.setDoInput(true); 
         conn.setRequestProperty("Cache-Control", "no-cache"); 

         OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream()); 
        
         osw.write(json.toString());
         out.write(json.toString());  
         osw.flush(); 

         return processResponse(conn, myResponse, out); 

      } 
      catch (IOException ex) { 
         myResponse.setResponse("An exception occurred processing the request."); 
         myResponse.setResponseCode(500);          
      } 
      return myResponse; 
   } 
   
   private URL getMyActionsServiceURL(String service, JspWriter out) throws IOException { 
     String WCM_DEV = "paehowuw20137.prudential.com"; 
   String WCM_QA = "paehowuw21337.prudential.com"; 
   String WCM_STAGE = "njros1uw21442.prudential.com"; 

   String HTTP_DEV = "inside-dev.prudential.com"; 
   String HTTP_QA = "inside-qa.prudential.com"; 
   String HTTP_STAGE = "inside-stage.prudential.com"; 
   String HTTP_PROD = "inside.prudential.com"; 
      try { 
         URL url; 
		String localhostname = java.net.InetAddress.getLocalHost().getHostName();
         // URL 
         if (localhostname.startsWith(WCM_DEV)) { 
            url = new URL("https://myactions-dev.prudential.com/MyActionsRestService/service/V1/" + service); 
         } 
         else if (localhostname.startsWith(WCM_QA)) { 
            url = new URL("https://myactions-qa.prudential.com/MyActionsRestService/service/V1/" + service); 
         } 
         else if (localhostname.startsWith(WCM_STAGE)) { 
            url = new URL("https://myactions-stage.prudential.com/MyActionsRestService/service/V1/" + service); 
         } 
         else { 
            url = new URL("https://myactions.prudential.com/MyActionsRestService/service/V1/" + service); 
         } 
         out.println("Using "+url+"<br>");
         return url; 
      } 
      catch (Exception e) { 
         
      } 
      return null; 
   } 
   
   MyActionsResponse processResponse(HttpURLConnection conn_in, MyActionsResponse response, JspWriter out) throws IOException { 
      
      try { 
         int responseCode = conn_in.getResponseCode(); 
         
            out.println("Response code - " + responseCode); 
       
         // If the responseCode is a 200+ response... success! 
         if (responseCode >= 200 && responseCode <= 206) { 

            String data = readResponse(conn_in, out); 
            if (data != null) { 
               String responseType = conn_in.getContentType(); 
               if ("application/json".equals(responseType)) { 
                  out.println("JSON response detected... processing JSON"); 
                   
                  response = processJSONResponse(data, out); 
               } else if ("application/xml".equals(responseType)) { 
                  out.println("XML response detected... processing XML"); 
                  
                  response = processXMLResponse(data, out); 
               } 
               response.setResponseCode(responseCode); 
               return response; 
            } 
           out.println("Unknown format response detected... Not supported"); 
            
            response.setResponse("Unsupported format response. Unable to process."); 
         } else { 
            response.setResponse("A processing error occurred on MyActions server."); 
            response.setResponseCode(responseCode); 
         } 
      } catch (IOException e) { 
         response.setResponseCode(500); 
         response.setResponse("IOException thrown retrieving ResponseCode"); 
      } 
      return response; 
   } 
   
    MyActionsResponse processJSONResponse(String response, JspWriter out) throws IOException { 
    Logger s_log = Logger.getLogger(JSON.class.getName()); 
      boolean isDebug = true; 
      MyActionsResponse result = new MyActionsResponse(); 

      try { 
         JSONObject json = new JSONObject(response); 
         if (isDebug) { 
            out.println("JSON Response: " + json.toString()); 
         } 
         if (json.has("serviceResult")) { 
            JSONObject sResult = json.getJSONObject("serviceResult"); 
            if (sResult.has("completed")) { 
               if (sResult.getBoolean("completed")) { 
                  if (sResult.has("messageCode")) { 
                     if (sResult.getInt("messageCode") == -1) { 
                        if (sResult.has("groupTransId")) { 
                           String gId = "" + sResult.getLong("groupTransId"); 
                           if (isDebug) { 
                              out.println("Transaction completed successfully. GroupTransId: " + gId); 
                           } 
                           result.setSuccess(true); 
                           result.setGroupTransId(gId); 
                           result.setResponse("MyActions message (" + gId + ") created successfully."); 
                           return result; 
                        } 
                     } 
                  } 
               } 
            } 
            if (json.has("serviceMessage")) { 
               result.setResponse("FAIL - " + (String)json.get("serviceMessage")); 
            } else { 
               result.setResponse("FAIL - an error occurred processing the MyActions request - No 'serviceMessage' object"); 
            } 
         } else { 
            result.setResponse("FAIL - an error occurred processing the MyActions request - No 'serviceResult' object"); 
         } 
      } catch (Exception e) { 
         out.println("Exception thrown processing JSON response."); 
         result.setResponse("Exception thrown processing JSON response."); 
      } 
      if (isDebug) { 
         out.println("JSON response did not meet 'success' criteria."); 
      } 
      return result; 
   } 
   
   MyActionsResponse processXMLResponse(String response, JspWriter out) throws IOException { 
   	 Logger s_log = Logger.getLogger(JSON.class.getName()); 
      boolean isDebug = true;
      MyActionsResponse result = new MyActionsResponse(); 

      try { 
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance(); 
         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder(); 
         Document doc = dBuilder.parse(response); 

         doc.getDocumentElement().normalize(); 
         if (isDebug) { 
            out.println("Root element :" + doc.getDocumentElement().getNodeName()); 
         } 

         String error = doc.getElementsByTagName("ns1:errorCode").item(0).getTextContent(); 
         if (isDebug) { 
            out.println("ErrorCode: " + error); 
         } 
         long errorCode = Long.valueOf(error).longValue(); 
         if (errorCode == 0) { 
            if (isDebug) { 
               out.println("No error detected"); 
            } 
            NodeList nList = doc.getElementsByTagName("serviceResult"); 
            if (nList.getLength() == 1) { 
               Node nNode = nList.item(0); 
               if (nNode.getNodeType() == Node.ELEMENT_NODE) { 
                  // Check the response 
                  Element eElement = (Element)nNode; 
                  if (Boolean.getBoolean(eElement.getElementsByTagName("ns1:completed").item(0).getTextContent())) { 
                     String gId = eElement.getElementsByTagName("ns1:groupTransId").item(0).getTextContent(); 
                     if (isDebug) { 
                        out.println("Transaction completed successfully. GroupTransId: " + gId); 
                     } 
                     result.setSuccess(true); 
                     result.setGroupTransId(gId); 
                     result.setResponse("MyActions message (" + gId + ") created successfully."); 
                     return result; 
                  } 
               } 
            } 
         } 
         result.setResponse("FAIL - MyActions server unable to process the request. Error code: " + errorCode); 
      } catch (ParserConfigurationException e) { 
         result.setResponse("Exception thrown processing XML response."); 
      } catch (SAXException e) { 
         result.setResponse("Exception thrown processing XML response."); 
      } catch (IOException e) { 
         result.setResponse("Exception thrown processing XML response."); 
      } 
      if (isDebug) { 
         out.println("Something went wrong - Exception or processing error!"); 
      } 
      return result; 
   } 
   
   String readResponse(HttpURLConnection conn, JspWriter out) { 
      boolean isDebug = true;

      StringBuilder sb; 
      try { 
         BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream())); 
         sb = new StringBuilder(); 
         String str = br.readLine(); 
         while (str != null) { 
            sb.append(str); 
            str = br.readLine(); 
         } 
         if (isDebug) { 
            out.println("Response: " + sb.toString()); 
         } 
         return sb.toString(); 
      } 
      catch (IOException e) { 
         Logger.getLogger(JSON.class.getName()).log(Level.SEVERE, null, e); 
      } 
      return null; 
   } 
    %>
<portletAPI:init />

<wcm:initworkspace
	user="<%= (java.security.Principal)request.getUserPrincipal() %>" /> 
<%
	Workspace ws = Utils.getSystemWorkspace();
	out.println("testing myactions");
	String[] approvers = {"x181054"};
	String contentIdString = "737eb4da-d2dd-4b42-bb39-a977740c8ee6";
	DocumentId contentId = ws.createDocumentId(contentIdString);
	Content theContent = (Content)ws.getById(contentId);
	JSON j = new JSON(); 
            JSONObject jsonMsg = j.createMyAction(theContent, approvers); 

            // Send the message - get a response 
            MyActionsResponse myResponse = createMyActionRequest("messages", jsonMsg, out);    
            String message = "";         
            message = myResponse.getResponse(); 
            //message = message.substring(0, Math.min(message.length(), 200)); 
	
	out.println("Message is "+message);	
%>