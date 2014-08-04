/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.commons.wcm.authoring;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.DocumentId;
import com.ibm.workplace.wcm.api.DocumentIdIterator;
import com.ibm.workplace.wcm.api.DocumentLibrary;
import com.ibm.workplace.wcm.api.DocumentTypes;
import com.ibm.workplace.wcm.api.Taxonomy;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.exceptions.AuthorizationException;
import com.ibm.workplace.wcm.api.exceptions.DocumentIdCreationException;
import com.ibm.workplace.wcm.api.exceptions.DocumentRetrievalException;
import com.ibm.workplace.wcm.api.exceptions.IllegalDocumentTypeException;
import com.prudential.commons.cache.TheCache;
import com.prudential.commons.cache.TheCacheEntry;
import com.prudential.utils.Utils;

public class CategoryJSONWrapper {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(CategoryJSONWrapper.class.getName());

   public static String getCategoryJSON(String libraryName) {
      return getCategoryJSON(libraryName, null);
   }

   public static String getCategoryJSON(String libraryName, String taxId) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      String returnString = "";
      Workspace ws = Utils.getSystemWorkspace();
      List<ObjectWrapper> returnList = null;
      try {
         // first check the cache
         ws.login();
         returnList = getCategoryWrapperList(ws, libraryName, taxId);

      }
      finally {
         if (ws != null) {
            ws.logout();
         }
      }
      StringBuffer sb = new StringBuffer();
      sb.append("[");
      for (Iterator<ObjectWrapper> itor = returnList.iterator(); itor.hasNext();) {
         sb.append(itor.next());
         if (itor.hasNext()) {
            sb.append(",");
         }
      }
      sb.append("]");
      returnString = sb.toString();
      if (isDebug) {
         s_log.exiting("CategoryJSONWrapper", "getCategoryJSON return " + returnString);
      }

      return returnString;
   }

   public static ArrayList<ObjectWrapper> getCategoryWrapperList(Workspace ws, String libraryName) {
      return getCategoryWrapperList(ws, libraryName, null);
   }

   public static ArrayList<ObjectWrapper> getCategoryWrapperList(Workspace ws, String libraryName, String taxIdString) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);

      if (isDebug) {
         s_log.entering("CategoryJSONWrapper", "getCategoryWrapperList");
      }
      TheCache theCache = TheCache.getInstance();
      TheCacheEntry tce = theCache.get("catWrapper_" + libraryName);
      ArrayList<ObjectWrapper> categories = null;
      if (tce != null) {
         categories = (ArrayList<ObjectWrapper>) tce.getCacheEntry();
         if (isDebug) {
            s_log.log(Level.FINEST, "retrieved cats from the cache, returm");
         }
         return categories;
      }
      DocumentLibrary currentDocLib = ws.getCurrentDocumentLibrary();
      ws.setCurrentDocumentLibrary(ws.getDocumentLibrary(libraryName));
      // get the Active taxonomy and it's children
      if (taxIdString == null || taxIdString.equals("")) {
         taxIdString = "537dafe3-657c-4117-9e4f-ebdab27b57cc";
      }
      try {
         DocumentId taxId = ws.createDocumentId(taxIdString);
         Taxonomy activeTax = (Taxonomy) ws.getById(taxId);

         //DocumentIdIterator<Document> itor = ws.findByType(DocumentTypes.Category);
         DocumentIdIterator<Document> itor = activeTax.getAllChildren();
         categories = new ArrayList<ObjectWrapper>(itor.getCount());
         try {
            while (itor.hasNext()) {
               DocumentId tempId = itor.next();
               String path;
               try {
                  path = ws.getPathById(tempId, false, false);
                  String title = tempId.getName();
                  String id = tempId.getID();
                  ObjectWrapper ow = new ObjectWrapper(id, title, path);
                  if (isDebug) {
                     s_log.log(Level.FINEST, "Adding object " + id + " to the arraylist");
                  }
                  categories.add(ow);
               }
               catch (DocumentRetrievalException e) {
                  // TODO Auto-generated catch block
                  if (s_log.isLoggable(Level.FINEST)) {
                     s_log.log(Level.FINEST, "", e);
                  }
               }
               catch (IllegalDocumentTypeException e) {
                  // TODO Auto-generated catch block
                  if (s_log.isLoggable(Level.FINEST)) {
                     s_log.log(Level.FINEST, "", e);
                  }
               }
            }

         }
         catch (Exception e) {
            if (isDebug) {
               s_log.log(Level.FINEST, "Exception " + e);
               e.printStackTrace();
            }
         }
      }
      catch (DocumentIdCreationException e) {
         // TODO Auto-generated catch block
         if (s_log.isLoggable(Level.FINEST)) {
            s_log.log(Level.FINEST, "", e);
         }
      }
      catch (DocumentRetrievalException e) {
         // TODO Auto-generated catch block
         if (s_log.isLoggable(Level.FINEST)) {
            s_log.log(Level.FINEST, "", e);
         }
      }
      catch (AuthorizationException e) {
         // TODO Auto-generated catch block
         if (s_log.isLoggable(Level.FINEST)) {
            s_log.log(Level.FINEST, "", e);
         }
      }
      finally {
         ws.setCurrentDocumentLibrary(currentDocLib);
      }

      if (s_log.isLoggable(Level.FINEST)) {
         StringBuilder strb = new StringBuilder();
         for (ObjectWrapper t : categories) {
            strb.append(t.getLabel()).append(",");
         }
         s_log.finest("All Categories: " + strb);
      }

      if (categories != null && categories.size() > 0) {
         theCache.put("catWrapper_" + libraryName, categories);
      }
      return categories;
   }
}
