/********************************************************************/
/* Asponte
 /* cmknight
 /********************************************************************/

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
import com.ibm.workplace.wcm.api.VirtualPortalScopedAction;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.exceptions.WCMException;
import com.prudential.tasks.MoveNewsContentTask;
import com.prudential.utils.Utils;

public class MoveOrLinkToSiteAreaScopedAction implements VirtualPortalScopedAction {

   private static Logger s_log = Logger.getLogger(MoveOrLinkToSiteAreaScopedAction.class.getName());

   /** the content that needs to be moved **/
   private String contentuuid = "";

   /** the parent of the content that it's being moved to. **/
   private String parentuuid = "";

   /** the parent of the content that it's being moved to. **/
   private String libraryname = "";
   
   /** whether or not to create a link **/
   private boolean createLink = false;
   
   /**
    * 
    * @return boolean 
    */
   public boolean isCreateLink() {
      return createLink;
   }

   public void setCreateLink(boolean p_createLink) {
      createLink = p_createLink;
   }

   private boolean returnedValue = false;
   
   public boolean getReturnedValue() {
      return returnedValue;
   }

   public void setReturnedValue(boolean p_returnedValue) {
      returnedValue = p_returnedValue;
   }

   public String getParentuuid() {
      return parentuuid;
   }
   
   public String getContentuuid() {
      return contentuuid;
   }

   public void setContentuuid(String p_contentuuid) {
      contentuuid = p_contentuuid;
   }

   public void setParentuuid(String p_parentuuid) {
      parentuuid = p_parentuuid;
   }

   public String getLibraryname() {
      return libraryname;
   }

   public void setLibraryname(String p_libraryname) {
      libraryname = p_libraryname;
   }

   public MoveOrLinkToSiteAreaScopedAction(String p_contentuuid, String p_parentuuid, String p_libraryName) {
      this(p_contentuuid, p_parentuuid, p_libraryName, false);
   }

   public MoveOrLinkToSiteAreaScopedAction(String p_contentuuid, String p_parentuuid, String p_libraryName, boolean p_createLink) {
      contentuuid = p_contentuuid;
      parentuuid = p_parentuuid;
      libraryname = p_libraryName;
      createLink = p_createLink;
   }
   

   @Override
   public void run() throws WCMException {
      // TODO Auto-generated method stub

      // just create the WCM API workspace, set the VP, and run the code in the 
      // scoped action

      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("MoveOrLinkToSiteAreaScopedAction", "run");
      }

      // get system workspace, login, then process the move
      Workspace ws = Utils.getSystemWorkspace();
      boolean libraryChanged = false;
      DocumentLibrary oldLib = ws.getCurrentDocumentLibrary();
      if (ws != null) {
         try {
            // login
            ws.login();
            // get the library, content, and parent

            DocumentLibrary theLib = ws.getDocumentLibrary(libraryname);

            // check for null libs
            if(oldLib != null && theLib != null) {
               if (!oldLib.getName().equals(theLib.getName())) {
                  libraryChanged = true;
               }
            }            

            // get the contentid 
            DocumentId theContentId = null;
            theContentId = ws.createDocumentId(contentuuid);
            if (theContentId == null) {
               throw new Exception("the contentuuid value could't be retrieved");
            }

            // get the site area
            DocumentId theSiteAreaId = null;
            theSiteAreaId = ws.createDocumentId(parentuuid);

            if (theSiteAreaId == null) {
               throw new Exception("the parentuuid value could't be retrieved");
            }

            Content theContent = (Content) ws.getById(theContentId);

            // now, move the content to this site area
            // or link
            if(isCreateLink()) {
               try {
                  ContentLink createdLink = ws.createContentLink(theContentId, theSiteAreaId, null, ChildPosition.END);
                  if(createdLink == null)
                  {
                     throw new Exception("Could not create link to content "+theContentId+" in site area "+theSiteAreaId);
                  }
                  else {
                     returnedValue = true;
                  }
               }
               catch (Exception e) {
                  if (isDebug) {
                     e.printStackTrace();
                  }
               }
            }
            else {
               PlacementLocation loc = new PlacementLocation(theSiteAreaId, Placement.END);
               MoveOptions opts = new MoveOptions();
               try {
                  if(isDebug) {
                     s_log.log(Level.FINEST, "about to attempt to move "+theContent.getName()+" under "+loc.getTargetDocId());
                  }
                  ws.move(theContent, loc, opts);
                  returnedValue = true;
               }
               catch (Exception e) {
                  if (isDebug) {
                     e.printStackTrace();
                  }
               }
            }
            
         }
         catch (Exception e) {
            if (isDebug) {
               e.printStackTrace();
            }
         }
         finally {
            if (libraryChanged) {
               ws.setCurrentDocumentLibrary(oldLib);
            }
            if (ws != null) {
               ws.logout();
            }

         }
      }
      if (isDebug) {
         s_log.exiting("MoveOrLinkToSiteAreaScopedAction", "run set returnedValue = "+getReturnedValue());
      }

   }

}
