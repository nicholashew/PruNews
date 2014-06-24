/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.utils;

import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.DocumentIdIterator;
import com.ibm.workplace.wcm.api.DocumentLibrary;
import com.ibm.workplace.wcm.api.SiteArea;
import com.ibm.workplace.wcm.api.TextComponent;
import com.ibm.workplace.wcm.api.Workspace;

public class PrudentialMLUtils {

   static Logger s_log = Logger.getLogger(PrudentialMLUtils.class.getName());

   // cheap cache to save some reretrievals
   private static Map PrudentialMLUtilsMap = new WeakHashMap();

   /**
    * helper method to retrieve list of libraries being managed by multilingual asset
    * 
    * @param thisWorkspace the workspace to use
    * @param contentName the name of the content to retrieve from in the path 
    * /MLConfiguration_v7/ConfigurationHome/ConfigurationData/
    */
   public static String retrieveLibraryListString(Workspace thisWorkspace, String contentName) {
      boolean isTracing = s_log.isLoggable(Level.FINEST);

      String returnString = "";
      if (isTracing) {
      }
      try {
         // have to retrieve the content, then the field value of the holding
         // use the m_ProcessLinkReorderMap as a weak cache
         String tempString = "";
         tempString = (String) PrudentialMLUtilsMap.get("LibraryListString" + contentName);
         if (tempString != null && tempString.length() > 0) {
            returnString = tempString;
            if (isTracing) {
               s_log.log(Level.FINEST, "Library list retrieved from cache " + returnString);
            }
         }
         // if wasn't in cache, have to retrieve the content in the ML library
         else {
            // save the library off so we can return to it
            DocumentLibrary originalLibrary = thisWorkspace.getCurrentDocumentLibrary();
            thisWorkspace.setCurrentDocumentLibrary(thisWorkspace.getDocumentLibrary("MLConfiguration_v7"));
            // retrieve the content that's configuring the list of libraries
            // TODO: iterate all content under this site area instead of this to find all 
            // content being managed by ML asset
            DocumentIdIterator contentIterator = thisWorkspace.findByPath("/MLConfiguration_v7/ConfigurationHome/ConfigurationData/"
               + contentName, Workspace.WORKFLOWSTATUS_PUBLISHED);
            Content tempContent = null;
            while (contentIterator.hasNext()) {
               tempContent = (Content) thisWorkspace.getById(contentIterator.nextId());
               break;
            }
            if (tempContent != null) {
               if (isTracing) {
                  s_log.log(Level.FINEST, "successfully retrieved " + tempContent);
               }
               // retrieve the component value for ContentLibraries
               TextComponent libraryValues = (TextComponent) tempContent.getComponent("ContentLibraries");
               returnString = libraryValues.getText();
               if (isTracing) {
                  s_log.log(Level.FINEST, "value from content = " + returnString);
               }
               // add to the cache
               PrudentialMLUtilsMap.put("LibraryListString" + contentName, returnString);
            }
            else {
               if (isTracing) {
                  s_log.log(Level.FINEST,
                     "unsuccessfully retrieved content at /MLConfiguration_v7/ConfigurationHome/ConfigurationData/InitialMLConfiguration");
                  s_log.log(Level.FINEST, "Cannot process reorder in other libraries");
               }
            }
            // set the library back
            thisWorkspace.setCurrentDocumentLibrary(originalLibrary);
         }

      }
      catch (Exception e) {
         if (isTracing) {
            s_log.log(Level.FINEST, "Error occurred " + e.getMessage());
            e.printStackTrace();
         }
      }

      if (isTracing) {
         s_log.exiting("PrudentialMLUtils", "retrieveLibraryListString " + returnString);
      }

      return returnString;
   }

   public static ArrayList retrieveLibraryListStrings(Workspace thisWorkspace) {
      boolean isTracing = s_log.isLoggable(Level.FINEST);

      ArrayList returnList = new ArrayList();
      String returnString = "";
      if (isTracing) {
         s_log.entering("ProcessLinkReorder", "retrieveLibraryList");
      }
      try {
         // have to retrieve the content, then the field value of the holding
         // use the m_ProcessLinkReorderMap as a weak cache
         String tempString = "";
         ArrayList tempList = (ArrayList) PrudentialMLUtilsMap.get("LibraryList");
         if (tempList != null && tempList.size() > 0) {
            returnList = tempList;
            if (isTracing) {
               s_log.log(Level.FINEST, "Library list retrieved from cache " + returnList);
            }
         }
         // if wasn't in cache, have to retrieve the content in the ML library
         else {
            // save the library off so we can return to it
            DocumentLibrary originalLibrary = thisWorkspace.getCurrentDocumentLibrary();
            thisWorkspace.setCurrentDocumentLibrary(thisWorkspace.getDocumentLibrary("MLConfiguration_v7"));
            // retrieve the content that's configuring the list of libraries
            // TODO: iterate all content under this site area instead of this to find all 
            // content being managed by ML asset
            DocumentIdIterator siteAreaIterator = thisWorkspace.findByPath("/MLConfiguration_v7/ConfigurationHome/ConfigurationData",
               Workspace.WORKFLOWSTATUS_PUBLISHED);
            SiteArea theSiteArea = null;
            while (siteAreaIterator.hasNext()) {
               theSiteArea = (SiteArea) thisWorkspace.getById(siteAreaIterator.nextId());
               break;
            }
            if (theSiteArea != null) {
               if (isTracing) {
                  s_log.log(Level.FINEST, "successfully retrieved " + theSiteArea);
               }
               // iterate the children of the site area
               DocumentIdIterator theSiteAreaChildren = theSiteArea.getAllDirectChildren();
               while (theSiteAreaChildren.hasNext()) {
                  // retrieve the component value for ContentLibraries
                  Content tempContent = (Content) thisWorkspace.getById(theSiteAreaChildren.nextId());
                  if (tempContent == null) {
                     continue;
                  }
                  TextComponent libraryValues = (TextComponent) tempContent.getComponent("ContentLibraries");
                  returnString = libraryValues.getText();
                  if (isTracing) {
                     s_log.log(Level.FINEST, "value from content = " + returnString);
                  }
                  // add to the arraylist
                  returnList.add(returnString);
               }

               // add to the cache
               PrudentialMLUtilsMap.put("LibraryList", returnList);
            }
            else {
               if (isTracing) {
                  s_log.log(Level.FINEST,
                     "unsuccessfully retrieved content at /MLConfiguration_v7/ConfigurationHome/ConfigurationData/InitialMLConfiguration");
                  s_log.log(Level.FINEST, "Cannot process reorder in other libraries");
               }
            }
            // set the library back
            thisWorkspace.setCurrentDocumentLibrary(originalLibrary);
         }

      }
      catch (Exception e) {
         if (isTracing) {
            s_log.log(Level.FINEST, "Error occurred " + e.getMessage());
            e.printStackTrace();
         }
      }

      if (isTracing) {
         s_log.exiting("ProcessLinkReorder", "retrieveLibraryList " + returnString);
      }

      return returnList;
   }
}
