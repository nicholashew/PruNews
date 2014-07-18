/********************************************************************/ 
/* Asponte 
/* cmknight 
/********************************************************************/ 

package com.prudential.objects; 

import com.ibm.workplace.wcm.api.Content; 
import com.prudential.utils.Utils;

import java.io.BufferedReader; 
import java.io.InputStreamReader; 
import java.io.OutputStreamWriter; 
import java.io.IOException; 

import java.net.HttpURLConnection; 
import java.net.MalformedURLException; 
import java.net.URL; 
import java.net.UnknownHostException; 
import java.util.*; 
import org.json.*; 
import java.util.logging.Level; 
import java.util.logging.Logger; 
import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory; 
import javax.xml.parsers.ParserConfigurationException; 

import org.w3c.dom.Document; 
import org.w3c.dom.Element; 
import org.w3c.dom.Node; 
import org.w3c.dom.NodeList; 
import org.xml.sax.SAXException; 

/** 
 * 
 * @author Pete Raleigh 
 */ 
public class JSON { 
   private static final Logger s_log = Logger.getLogger(JSON.class.getName()); 

   private static String WCM_DEV = "paehowuw20137.prudential.com"; 
   private static String WCM_QA = "paehowuw21337.prudential.com"; 
   private static String WCM_STAGE = "njros1uw21442.prudential.com"; 

   private static String HTTP_DEV = "inside-dev.prudential.com"; 
   private static String HTTP_QA = "inside-qa.prudential.com"; 
   private static String HTTP_STAGE = "inside-stage.prudential.com"; 
   private static String HTTP_PROD = "inside.prudential.com"; 

   private String localhostname; 
   private String authoringhostname; 

   private JSONObject header; 

   private JSONObject body; 

   private JSONObject status; 

   public JSON() { 
      boolean isDebug = s_log.isLoggable(Level.FINEST); 

      header = new JSONObject(); 
      header.put("appCode", "WCMPolsAndPrins"); 
      body = new JSONObject(); 
      status = new JSONObject(); 

      // Check the current HOST 
      try { 
                 // Get the Application Server that the Workflow Action is running on 
         localhostname = java.net.InetAddress.getLocalHost().getHostName(); 
         if (isDebug) s_log.log(Level.FINEST, "Local Hostname: " + localhostname); 
         this.getPortalAuthoringServer(); 
         if (isDebug) s_log.log(Level.FINEST, "Authoring Server: " + authoringhostname); 
      } 
      catch (UnknownHostException e) { 
      } 
   } 

   public JSONObject createMyAction(Content cont, String[] approvers) { 
      Date now = new Date(); 
      long day = 1000 * 60 * 60 * 24; 
      long week = 7 * day; 
      long month = 31 * day; 

      JSONObject result = new JSONObject(); 

      header.put("businessKey", "PolsApprWFA-" + now.getTime()); 

      LinkedList user = new LinkedList(); 
      user.addAll(Arrays.asList(approvers)); 

      String linkString = Utils.getPreviewURL(cont);
      //String linkString = "https://" + authoringhostname + "/wps/myportal/wcmAuthoring?wcmAuthoringAction=read&docid=" + cont.getId().toString();
      
      LinkedHashMap userList = new LinkedHashMap(); 
      userList.put("UserType", "IONS_ID"); 
      userList.put("User", user); 
      header.put("userList", userList); 
      header.put("language", "en"); 
      header.put("link", linkString); 

      body.put("notificationType", "Note"); 
      body.put("priority", 2); 
      body.put("messageStatus", "Active"); 
      body.put("progress", 0); 
      body.put("startDate", now.getTime()); 
      body.put("dueDate", (now.getTime() + week)); 
      body.put("expirationDate", (now.getTime() + month)); 
      body.put("title", "Approval Required"); 
      body.put("shortDescription", "Approval required for " + cont.getTitle()); 
      body.put("longDescription", "Approval required for the Policy: " + cont.getTitle()); 

      status.put("serviceVersion", "1.0"); 
      status.put("uiRemoveFlag", "false"); 

      result.put("messageHeader", header); 
      result.put("messageBody", body); 
      result.put("messageStatus", status); 

      return result; 
   } 

   public JSONObject deleteMyAction(String id) { 

      JSONObject result = new JSONObject(); 

      result.put("messageHeader", header); 
      result.put("messageBody", body); 
      result.put("messageStatus", status); 

      return result; 
   } 

   private void getPortalAuthoringServer() { 
      boolean isDebug = s_log.isLoggable(Level.FINEST); 

      // Match the APP Server Hostname 
      if (localhostname.startsWith(WCM_DEV)) { 
          authoringhostname = HTTP_DEV; 
          if (isDebug) s_log.log(Level.FINEST, "AppServer Hostname matches DEV - " + WCM_DEV); 
      } 
      else if (localhostname.startsWith(WCM_QA)) { 
          authoringhostname = HTTP_QA; 
          if (isDebug) s_log.log(Level.FINEST, "AppServer Hostname matches QA - " + WCM_QA); 
      } 
      else if (localhostname.startsWith(WCM_STAGE)) { 
          authoringhostname = HTTP_STAGE; 
          if (isDebug) s_log.log(Level.FINEST, "AppServer Hostname matches STAGE - " + WCM_STAGE); 
      } 
      else { 
          authoringhostname = HTTP_PROD; 
          if (isDebug) { 
               s_log.log(Level.FINEST, "No match of AppServer Hostname for: " + WCM_DEV + ", " + WCM_QA + ", " + WCM_STAGE); 
               s_log.log(Level.FINEST, "Assuming Production AppServer for: " + localhostname); 
          } 
      } 
   } 

   private URL getMyActionsServiceURL(String service) { 
      boolean isDebug = s_log.isLoggable(Level.FINEST); 

      try { 
         URL url; 

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
         if (isDebug) { 
            s_log.log(Level.FINEST, "Connecting to - " + url.toString()); 
         } 
         return url; 
      } 
      catch (MalformedURLException e) { 
         Logger.getLogger(JSON.class.getName()).log(Level.SEVERE, null, e); 
      } 
      return null; 
   } 

   private String readResponse(HttpURLConnection conn) { 
      boolean isDebug = s_log.isLoggable(Level.FINEST); 

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
            s_log.log(Level.FINEST, "Response: " + sb.toString()); 
         } 
         return sb.toString(); 
      } 
      catch (IOException e) { 
         Logger.getLogger(JSON.class.getName()).log(Level.SEVERE, null, e); 
      } 
      return null; 
   } 

   private MyActionsResponse processResponse(HttpURLConnection conn, MyActionsResponse response) { 
      boolean isDebug = s_log.isLoggable(Level.FINEST); 

      try { 
         int responseCode = conn.getResponseCode(); 
         if (isDebug) { 
            s_log.log(Level.FINEST, "Response code - " + responseCode); 
         } 
         // If the responseCode is a 200+ response... success! 
         if (responseCode >= 200 && responseCode <= 206) { 

            String data = readResponse(conn); 
            if (data != null) { 
               String responseType = conn.getContentType(); 
               if ("application/json".equals(responseType)) { 
                  if (isDebug) { 
                     s_log.log(Level.FINEST, "JSON response detected... processing JSON"); 
                  } 
                  response = processJSONResponse(data); 
               } else if ("application/xml".equals(responseType)) { 
                  if (isDebug) { 
                     s_log.log(Level.FINEST, "XML response detected... processing XML"); 
                  } 
                  response = processXMLResponse(data); 
               } 
               response.setResponseCode(responseCode); 
               return response; 
            } 
            if (isDebug) { 
               s_log.log(Level.FINEST, "Unknown format response detected... Not supported"); 
            } 
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

   public MyActionsResponse processJSONResponse(String response) { 
      boolean isDebug = s_log.isLoggable(Level.FINEST); 
      MyActionsResponse result = new MyActionsResponse(); 

      try { 
         JSONObject json = new JSONObject(response); 
         if (isDebug) { 
            s_log.log(Level.FINEST, "JSON Response: " + json.toString()); 
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
                              s_log.log(Level.FINEST, "Transaction completed successfully. GroupTransId: " + gId); 
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
      } catch (JSONException e) { 
         s_log.log(Level.FINEST, "Exception thrown processing JSON response."); 
         result.setResponse("Exception thrown processing JSON response."); 
      } 
      if (isDebug) { 
         s_log.log(Level.FINEST, "JSON response did not meet 'success' criteria."); 
      } 
      return result; 
   } 

   private MyActionsResponse processXMLResponse(String response) { 
      boolean isDebug = s_log.isLoggable(Level.FINEST); 
      MyActionsResponse result = new MyActionsResponse(); 

      try { 
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance(); 
         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder(); 
         Document doc = dBuilder.parse(response); 

         doc.getDocumentElement().normalize(); 
         if (isDebug) { 
            s_log.log(Level.FINEST, "Root element :" + doc.getDocumentElement().getNodeName()); 
         } 

         String error = doc.getElementsByTagName("ns1:errorCode").item(0).getTextContent(); 
         if (isDebug) { 
            s_log.log(Level.FINEST, "ErrorCode: " + error); 
         } 
         long errorCode = Long.valueOf(error).longValue(); 
         if (errorCode == 0) { 
            if (isDebug) { 
               s_log.log(Level.FINEST, "No error detected"); 
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
                        s_log.log(Level.FINEST, "Transaction completed successfully. GroupTransId: " + gId); 
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
         s_log.log(Level.FINEST, "Something went wrong - Exception or processing error!"); 
      } 
      return result; 
   } 

   public int deleteMyActionRequest(String service, String id) { 
      try { 
         URL url = getMyActionsServiceURL(service + "/" + id); 
         if (url == null) 
            return -1; 

         HttpURLConnection conn = (HttpURLConnection) url.openConnection(); 
         conn.setDoOutput(true); 
         conn.setRequestProperty("Content-Type", "application/json"); 
         conn.setRequestMethod("DELETE"); 
         conn.connect(); 
         return conn.getResponseCode(); 
      } 
      catch (IOException e) { 
         Logger.getLogger(JSON.class.getName()).log(Level.SEVERE, null, e); 
      } 
      return -1; 
   } 

   public MyActionsResponse createMyActionRequest(String service, JSONObject json) { 
      boolean isDebug = s_log.isLoggable(Level.FINEST); 
      MyActionsResponse myResponse = new MyActionsResponse(); 

      try { 
         // Check the current HOST 
         URL url = getMyActionsServiceURL(service); 
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
         if (isDebug) { 
            s_log.log(Level.FINEST, "JSON Request: " + json.toString()); 
         } 
         osw.write(json.toString()); 
         osw.flush(); 

         return processResponse(conn, myResponse); 

      } 
      catch (IOException ex) { 
         myResponse.setResponse("An exception occurred processing the request."); 
         myResponse.setResponseCode(500); 
         Logger.getLogger(JSON.class.getName()).log(Level.SEVERE, null, ex); 
      } 
      return myResponse; 
   } 
}