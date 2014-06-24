/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.vp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.workplace.wcm.api.ChildPosition;
import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.ContentLink;
import com.ibm.workplace.wcm.api.DocumentId;
import com.ibm.workplace.wcm.api.DocumentIdIterator;
import com.ibm.workplace.wcm.api.DocumentLibrary;
import com.ibm.workplace.wcm.api.DocumentTypes;
import com.ibm.workplace.wcm.api.SiteArea;
import com.ibm.workplace.wcm.api.TextComponent;
import com.ibm.workplace.wcm.api.VirtualPortalScopedAction;
import com.ibm.workplace.wcm.api.WCM_API;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.exceptions.WCMException;
import com.prudential.utils.PrudentialMLUtils;
import com.prudential.utils.Utils;
public class HandleReorder implements VirtualPortalScopedAction {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(HandleReorder.class.getName());

   private String p_uuids = "";
   private boolean returnedValue = false;
   
   boolean p_processML = false;
   public boolean isP_processML() {
      return p_processML;
   }
   public void setP_processML(boolean p_p_processML) {
      p_processML = p_p_processML;
   }
   public String getP_configContentName() {
      return p_configContentName;
   }
   public void setP_configContentName(String p_p_configContentName) {
      p_configContentName = p_p_configContentName;
   }
   public boolean isP_processAsJSON() {
      return p_processAsJSON;
   }
   public void setP_processAsJSON(boolean p_p_processAsJSON) {
      p_processAsJSON = p_p_processAsJSON;
   }
   public String getP_effectString() {
      return p_effectString;
   }
   public void setP_effectString(String p_p_effectString) {
      p_effectString = p_p_effectString;
   }
   public String getP_siteAreaString() {
      return p_siteAreaString;
   }
   public void setP_siteAreaString(String p_p_siteAreaString) {
      p_siteAreaString = p_p_siteAreaString;
   }

   String p_configContentName = "";
   boolean p_processAsJSON = false;
   String p_effectString = "";
   String p_siteAreaString = "";
   
   public HandleReorder(String uuidString)
   {
      p_uuids = uuidString;
   }
   /**
    * 
    * @see com.ibm.workplace.wcm.api.VirtualPortalScopedAction#run()
    */
   @Override
   public void run() throws WCMException {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      
      if (isDebug) {
         s_log.entering("HandleReorder", "run");
      }

      boolean success = true;

      String[] uuids = p_uuids.split(",");
      ArrayList docIdList = new ArrayList();
      Workspace thisWorkspace = Utils.getSystemWorkspace();
      if (isDebug) {
          s_log.log(Level.FINEST, "UUIDs received:" +p_uuids);
      }

      DocumentId tempDocId = null;

      for (int x = 0; x < uuids.length; x++) {
          tempDocId = thisWorkspace.createDocumentId(uuids[x]);
          if (isDebug) {
              s_log.log(Level.FINEST, "Value: " + uuids[x]);
              s_log.log(Level.FINEST, "Retrieved docId = "
                      + tempDocId);
          }
          docIdList.add(tempDocId);
      }

      // now process for the library that was passed in
      success = processMove(docIdList, thisWorkspace);
      // now process in the rest of the libraries if required
      if (p_processML) {
          success = processMoveInMLLibraries(docIdList, thisWorkspace,
                  p_configContentName);
      }
      
      if(p_effectString != null && !p_effectString.equals("")) {
          success = this.handleEffect(thisWorkspace, p_effectString, p_siteAreaString);
      }
      
      if (isDebug) {
         s_log.exiting("HandleReorder", "run");
      }
      
      
   }
   
   /**
    * 
    * @param docIdList
    * @param thisWorkspace
    * @param configContentName
    *            the name of the content holding the configuration we care
    *            about
    */
   public static boolean processMoveInMLLibraries(ArrayList docIdList,
           Workspace thisWorkspace, String configContentName) {
       
       boolean success = true;
       boolean isTracing = s_log.isLoggable(Level.FINEST);

       if (isTracing) {
           s_log.entering("ProcessLinkReorder", "processMoveInMLLibraries"
                   + docIdList);
       }
       /**
        * high level: retrieve the libraries that the ML asset is handling.
        * Then, process the reorder in those libraries as well. the docIds
        * ArrayList has them in the order they need to be processed so get the
        * doc id to get the path, then change the path to match the ML library
        * to retrieve the item in that path, Then pull that items docid
        */
       try {
           Iterator docIdIterator = docIdList.iterator();
           DocumentId tempId = null;
           ArrayList itemsToMove = new ArrayList();
           String tempPath = "";
           ArrayList pathArray = new ArrayList();
           String initialLibraryName = "";

           while (docIdIterator.hasNext()) {
               tempId = (DocumentId) docIdIterator.next();
               tempPath = thisWorkspace.getPathById(tempId, true, true);
               if (initialLibraryName == "") {
                   initialLibraryName = tempId.getContainingLibrary()
                           .getName();
               }
               if (isTracing) {
                   s_log.log(Level.FINEST, "adding path " + tempPath);
               }
               pathArray.add(tempPath);
           }

           // now, we have the paths to the docId's that were passed in
           // we iterate the other libraries, switch out the path
           // and process the reorder
           // retrieve the list of libraries
           // if the configContentName has been passed in and its not "", then
           // grab the string based on that
           // else grab All the configured ML libraries.
           String libraryListString = "";
           if (configContentName != null && !configContentName.equals("")) {
               libraryListString = PrudentialMLUtils.retrieveLibraryListString(
                       thisWorkspace, configContentName);
               processMoveInSingleLibrary(initialLibraryName,
                       libraryListString, thisWorkspace, pathArray);
           } else {
               ArrayList mlContentList = PrudentialMLUtils
                       .retrieveLibraryListStrings(thisWorkspace);
               Iterator contentListIterator = mlContentList.iterator();
               while (contentListIterator.hasNext()) {
                   libraryListString = (String) contentListIterator.next();
                   processMoveInSingleLibrary(initialLibraryName,
                           libraryListString, thisWorkspace, pathArray);
               }
           }

       } catch (Exception e) {
           if (isTracing) {
               s_log.log(Level.FINEST,
                       "exception occurred " + e.getMessage());
               e.printStackTrace();
           }
       }
       if (isTracing) {
           s_log.exiting("ProcessLinkReorder", "processMoveInMLLibraries returning "+success);
       }
       
       return success;
   }

   /**
    * helper method to process the move
    * 
    * @param docIdList
    *            the document id's in order
    */
   public static boolean processMove(ArrayList docIdList, Workspace thisWorkspace) {
       boolean isTracing = s_log.isLoggable(Level.FINEST);

       boolean success = true;
       
       if (isTracing) {
           s_log.entering("ProcessLinkReorder", "processMove" + docIdList);
       }
       try {
           // now, since they are passed first, second, third, etc
           // just place them last under the top level site area. That way, the
           // first will be last, then second be last which moved first up
           // and so on
           DocumentId parentId = null;
           Content tempContent = null;
           ContentLink tempContentLink = null;
           Iterator idIterator = docIdList.iterator();
           DocumentId tempDocId = null;

           while (idIterator.hasNext()) {
               tempDocId = (DocumentId) idIterator.next();
               if (parentId == null) {
                   if (tempDocId.getType() == DocumentTypes.Content) {
                       tempContent = (Content) thisWorkspace
                               .getById(tempDocId);
                       parentId = tempContent.getDirectParent();
                   } else if (tempDocId.getType() == DocumentTypes.ContentLink) {
                       tempContentLink = (ContentLink) thisWorkspace
                               .getById(tempDocId);
                       parentId = tempContentLink.getParentId();
                   }
               }
               // now add the SA to the end
               // catch exceptions so we can process the reorder on the rest
               try {
                   if (isTracing) {
                       s_log.log(Level.FINEST, "About to move = "
                               + tempDocId + " to the last position under "
                               + parentId);
                   }
                   thisWorkspace.moveSiteFrameworkDocument(tempDocId,
                           parentId, null, ChildPosition.END);
               } catch (Exception e) {
                   if (isTracing) {
                       s_log.log(Level.FINEST,
                               "Error occurred " + e.getMessage());
                       e.printStackTrace();
                   }
               }

           }
       } catch (Exception e) {
           if (isTracing) {
               s_log.log(Level.FINEST, "Error occurred " + e.getMessage());
               e.printStackTrace();
           }
       }

       if (isTracing) {
           s_log.exiting("ProcessLinkReorder", "processMove "+success);            
       }
       return success;
   }

   /**
    * 
    * @param initialLibraryName
    *            the name of the library to skip since the reorder was done
    *            there
    * @param libraryListString
    *            comma delim list of libraries being managed by the ML asset
    * @param thisWorkspace
    *            the workspace to use
    * @param pathArray
    *            the path list of items we need to re-retrieve
    */
   public static boolean processMoveInSingleLibrary(String initialLibraryName,
           String libraryListString, Workspace thisWorkspace,
           ArrayList pathArray) {

       boolean success = true;
       boolean isTracing = s_log.isLoggable(Level.FINEST);
       if (isTracing) {
           s_log.entering("ProcessLinkReorder",
                   "processMoveInSingleLibrary", "initialLibraryName = "
                           + initialLibraryName + ", libraryListString = "
                           + libraryListString);
       }
       String[] libraries = libraryListString.split(",");
       for (int x = 0; x < libraries.length; x++) {
           // now, set the library on the workspace
           thisWorkspace.logout();
           thisWorkspace.login();
           String currentLibraryName = libraries[x];
           if (isTracing) {
               s_log.log(Level.FINEST, "processing library "
                       + currentLibraryName);
           }

           // if we have already processed this library (based on the incoming
           // uuids)
           // then skip
           if (currentLibraryName.equalsIgnoreCase(initialLibraryName)) {
               if (isTracing) {
                   s_log
                           .log(Level.FINEST, "skipping library "
                                   + currentLibraryName
                                   + " because already processed");
               }
               continue;
           }
           DocumentLibrary tempLibrary = thisWorkspace
                   .getDocumentLibrary(currentLibraryName);
           if (tempLibrary == null) {
               if (isTracing) {
                   s_log.log(Level.FINEST, "skipping library "
                           + currentLibraryName
                           + " because it could not be retrieved");
               }
               continue;
           }
           thisWorkspace.setCurrentDocumentLibrary(tempLibrary);

           // now, work through the array list of paths, and change the
           // library in the path, retrieve by the path, and put the docid in
           // an array list
           // to be passed to processMove method if it exists
           ArrayList otherLibraryDocIds = new ArrayList();
           Iterator paths = pathArray.iterator();
           DocumentId tempIdFromPath = null;
           while (paths.hasNext()) {
               String currentPath = (String) paths.next();
               if (isTracing) {
                   s_log.log(Level.FINEST, "initial path " + currentPath);
               }
               // to find the library piece, remove the "/ if it starts with
               // one
               if (currentPath.startsWith("/")) {
                   currentPath = currentPath.substring(1);
               }
               // now, find to the next /
               int postLibrarySlash = currentPath.indexOf("/");
               currentPath = currentLibraryName + currentPath.substring(postLibrarySlash);

               if (isTracing) {
                   s_log.log(Level.FINEST,
                           "libraryReplaced initialPath = " + currentPath);
               }

               // now, pull the docId if we can find it
               DocumentIdIterator potentialMatch = thisWorkspace.findByPath(
                       currentPath, Workspace.WORKFLOWSTATUS_PUBLISHED);
               if (potentialMatch.hasNext()) {
                   if (isTracing) {
                       s_log.log(Level.FINEST, "found match by the path");
                   }
                   otherLibraryDocIds.add(potentialMatch.nextId());
               }

           }
           // now, we have otherLibraryDocIds which will
           // contain the site areas in the order in which they should appear.
           // This is
           // because the incoming list of UUIDs was already ordered
           processMove(otherLibraryDocIds, thisWorkspace);
       }
       if (isTracing) {
           s_log.exiting("ProcessLinkReorder",
                   "processMoveInSingleLibrary", "initialLibraryName = "
                           + initialLibraryName + ", libraryListString = "
                           + libraryListString+" returning "+success);
       }
       
       return success;
   }
   
   /**
    * helper method to set transition effects if necessary
    */
   public static boolean handleEffect(Workspace theWorkspace, String effect,
           String siteArea) {
       boolean isTracing = s_log.isLoggable(Level.FINEST);

       if (isTracing) {
           s_log.entering("ProcessLinkReorder", "handleEffect");
       }
       boolean success = false;
       SiteArea tempSA = null;
       TextComponent tc = null;

       try {
           DocumentId tempDocId = theWorkspace.createDocumentId(siteArea);
           tempSA = (SiteArea) theWorkspace.getById(tempDocId);
           if (tempSA.hasComponent("sliderEffect")) {
               tc = (TextComponent) tempSA.getComponent("sliderEffect");
           } else {
               tc = (TextComponent) tempSA.createComponent("sliderEffect",
                       DocumentTypes.ShortTextComponent);
           }
           if (tc != null) {
               tc.setText(effect);
               tempSA.setComponent("sliderEffect", tc);
               String[] errors = theWorkspace.save(tempSA);
               if (errors.length == 0) {
                   success = true;
               } else {
                   if (isTracing) {
                       for (int ii = 0; ii < errors.length; ii++) {
                           s_log.log(Level.FINEST, errors[ii]);
                       }
                   }
               }
           }
       } catch (Exception e) {
           if (isTracing) {
               s_log.log(Level.FINEST,
                       "exception occured check SystemError");
               e.printStackTrace();
           }
       }
       if (isTracing) {
           s_log.exiting("ProcessLinkReorder", "handleEffect returning "
                   + success);
       }

       return success;
   }
   
   /**
    * getter method to return the created site area document id
    * @return
    */
   public boolean getReturnedValue() {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.log(Level.FINEST, "returning "+returnedValue);
      }
      return returnedValue;
   }

   public void setReturnedValue(boolean p_returnedValue) {
      returnedValue = p_returnedValue;
   }
   
}

