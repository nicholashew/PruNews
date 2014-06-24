/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.objects;

import com.ibm.workplace.wcm.api.Content;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;
import org.json.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pete Raleigh
 */
public class JSON {
   private String localhostname;

   private JSONObject header;

   private JSONObject body;

   private JSONObject status;

   public JSON() {
      header = new JSONObject();
      header.put("appCode", "WCMPolsAndPrins");
      body = new JSONObject();
      status = new JSONObject();

      // Check the current HOST
      try {
         localhostname = java.net.InetAddress.getLocalHost().getHostName();
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

      LinkedHashMap userList = new LinkedHashMap();
      userList.put("UserType", "IONS_ID");
      userList.put("User", user);
      header.put("userList", userList);
      header.put("language", "en");
      header
         .put("link", "https://" + localhostname + "/wps/myportal/wcmAuthoring?wcmAuthoringAction=read&docid=" + cont.getId().toString());

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

   private URL getMyActionsServiceURL(String service) {
      try {
         URL url;

         // 1. URL
         if (localhostname.startsWith("inside-dev")) {
            // url = new URL("https://myactions-dev.prudential.com/MyActionsRestService/service/V1/" + service);
            // Unrestricted server... temporary fix
            url = new URL("http://njros1up6004.prudential.com:9088/MyActionsRestService/service/V1/" + service);
         }
         else if (localhostname.startsWith("inside-qa")) {
            url = new URL("https://myactions-qa.prudential.com/MyActionsRestService/service/V1/" + service);
         }
         else if (localhostname.startsWith("inside-stage")) {
            url = new URL("https://myactions-stage.prudential.com/MyActionsRestService/service/V1/" + service);
         }
         else {
            url = new URL("https://myactions.prudential.com/MyActionsRestService/service/V1/" + service);
         }
      }
      catch (MalformedURLException e) {
         Logger.getLogger(JSON.class.getName()).log(Level.SEVERE, null, e);
      }
      return null;
   }

   private String getJSONResponse(HttpURLConnection conn) {
      StringBuilder sb;
      try {
         BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
         sb = new StringBuilder();
         String str = br.readLine();
         while (str != null) {
            sb.append(str);
            str = br.readLine();
         }
         JSONObject json = new JSONObject(sb.toString());
         String key = "groupTransId";
         if (json.has(key)) {
            return (String) json.get(key);
         }
         else {
            Logger.getLogger(JSON.class.getName()).log(Level.WARNING, null, "Key: " + key + ", not found.");
         }
      }
      catch (IOException e) {
         Logger.getLogger(JSON.class.getName()).log(Level.SEVERE, null, e);
      }
      return null;
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

   public String createMyActionRequest(String service, JSONObject json) {
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
         conn.setDoOutput(true);
         conn.setDoInput(true);
         conn.setRequestProperty("Cache-Control", "no-cache");

         try {
            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(json.toString());
         }
         catch (Exception e) {
            e.printStackTrace();
         }

         return getJSONResponse(conn);
      }
      catch (IOException ex) {
         Logger.getLogger(JSON.class.getName()).log(Level.SEVERE, null, ex);
      }
      return null;
   }
}
