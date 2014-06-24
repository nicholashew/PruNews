package com.prudential.vpactions;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.workplace.wcm.api.ChildPosition;
import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.ContentLink;
import com.ibm.workplace.wcm.api.DocumentId;
import com.ibm.workplace.wcm.api.DocumentLibrary;
import com.ibm.workplace.wcm.api.MoveOptions;
import com.ibm.workplace.wcm.api.Placement;
import com.ibm.workplace.wcm.api.PlacementLocation;
import com.ibm.workplace.wcm.api.SiteArea;
import com.ibm.workplace.wcm.api.VirtualPortalScopedAction;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.exceptions.WCMException;
import com.prudential.utils.Utils;

public class CreateOrRetrieveSiteAreaScopedActionById implements VirtualPortalScopedAction {

   private static Logger s_log = Logger.getLogger(CreateOrRetrieveSiteAreaScopedActionById.class.getName());

   /** the parent of the content that it's being moved to. **/
   private DocumentId parentId = null;

   public DocumentId getParentId() {
      return parentId;
   }

   public void setParentId(DocumentId p_parentId) {
      parentId = p_parentId;
   }

   /** the parent of the content that it's being moved to. **/
   private String siteareaname = "";

   /** the parent of the content that it's being moved to. **/
   private String libraryname = "";

   /** The documentId of the created site area */
   private DocumentId returnedValue = null;

   /**
    * getter method to return the created site area document id
    * @return
    */
   public DocumentId getReturnedValue() {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.log(Level.FINEST, "returning " + returnedValue);
      }
      return returnedValue;
   }

   public void setReturnedValue(DocumentId p_returnedValue) {
      returnedValue = p_returnedValue;
   }

   public String getSiteareaname() {
      return siteareaname;
   }

   public void setSiteareaname(String p_siteareaname) {
      siteareaname = p_siteareaname;
   }

   public String getLibraryname() {
      return libraryname;
   }

   public void setLibraryname(String p_libraryname) {
      libraryname = p_libraryname;
   }

   /**
    * 
    * @param p_parentuuid the uuid of the parent site area
    * @param p_siteareaname the name of the new site area
    * @param p_libraryname the name of the library
    */
   public CreateOrRetrieveSiteAreaScopedActionById(DocumentId p_parentId, String p_siteareaname, String p_libraryname) {
      parentId = p_parentId;
      siteareaname = p_siteareaname;
      libraryname = p_libraryname;
   }

   @Override
   public void run() throws WCMException {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("CreateOrRetrieveSiteAreaScopedActionById", "run parentId=" + parentId + ", siteareaname=" + siteareaname
            + ", libraryname=" + libraryname);
      }

      // get system workspace, login, then process the move
      Workspace ws = Utils.getSystemWorkspace();
      if (ws != null) {
         try {
            // login
            ws.login();
            // get the parent site area
            DocumentId theSiteAreaId = parentId;

            if (theSiteAreaId == null) {
               throw new Exception("the parentuuid value could't be retrieved");
            }
            SiteArea createdSiteArea = null;

            // try to retrieve first by the parent/name
            DocumentId resultId = Utils.getSiteAreaIdByNameAndParent(ws, siteareaname, theSiteAreaId, libraryname);
            if (resultId == null) {
               resultId = Utils.createSiteArea(ws, theSiteAreaId, siteareaname, libraryname);
            }

            if (resultId != null) {
               if (isDebug) {
                  s_log.log(Level.FINEST, "resultId != null, so set returned value");
               }
               this.setReturnedValue(resultId);
            }
            else {
               if (isDebug) {
                  s_log.log(Level.FINEST, "resultId == null, so don't set returned value");
               }
            }

         }
         catch (Exception e) {
            if (isDebug) {
               e.printStackTrace();
            }
         }
         finally {

            if (ws != null) {
               ws.logout();
            }

         }
      }

      if (isDebug) {
         s_log.exiting("CreateOrRetrieveSiteAreaScopedActionById", "run value is " + this.getReturnedValue());
      }

   }

}
