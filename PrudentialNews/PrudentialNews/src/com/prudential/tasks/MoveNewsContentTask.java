/********************************************************************/
/* Asponte
 /* cmknight
 /********************************************************************/

package com.prudential.tasks;

import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.DocumentId;
import com.ibm.workplace.wcm.api.DocumentLibrary;
import com.ibm.workplace.wcm.api.MoveOptions;
import com.ibm.workplace.wcm.api.Placement;
import com.ibm.workplace.wcm.api.PlacementLocation;
import com.ibm.workplace.wcm.api.Repository;
import com.ibm.workplace.wcm.api.VirtualPortalContext;
import com.ibm.workplace.wcm.api.WCM_API;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.exceptions.VirtualPortalNotFoundException;
import com.ibm.workplace.wcm.api.exceptions.WCMException;
import com.prudential.utils.Utils;
import com.prudential.vpactions.CreateOrRetrieveSiteAreaScopedAction;
import com.prudential.vpactions.MoveOrLinkToSiteAreaScopedAction;

/**
 * task to invoke the create site area if necessary, and then move the content to it
 * @author cmknight
 *
 */
public class MoveNewsContentTask extends TimerTask
{

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(MoveNewsContentTask.class.getName());

   /** the content that needs to be moved **/
   private String contentuuid = "";

   /** the parent of the content that it's being moved to. **/
   private String parentName = "";

   /** the parent of the content that it's being moved to. **/
   private String libraryName = "";
   
   private static String NEWSBYDATEPARENT = "NewsByDate";

   public MoveNewsContentTask(String p_contentuuid, String p_parentName, String p_libraryName) {
      contentuuid = p_contentuuid;
      parentName = p_parentName;
      libraryName = p_libraryName;
   }
   @Override
   public void run()
   {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug)
      {
         s_log.entering("MoveNewsContentTask", "run");
      }

      // CreateOrRetrieveSiteAreaScopedAction
      // MoveOrLinkToSiteAreaScopedAction
      
      Repository repo = WCM_API.getRepository();
      try {
          VirtualPortalContext vctx = repo.generateVPContextFromContextPath(Utils.getVPName());
          CreateOrRetrieveSiteAreaScopedAction vpA = new CreateOrRetrieveSiteAreaScopedAction(NEWSBYDATEPARENT, parentName, libraryName);
          repo.executeInVP(vctx, vpA);
          //content = vpA.getReturnedValue();
          DocumentId destinationId = vpA.getReturnedValue();
          if(destinationId != null) {
             if (isDebug) {
               s_log.log(Level.FINEST, "destinationId != null");
            }
             MoveOrLinkToSiteAreaScopedAction moveScopedAction = new MoveOrLinkToSiteAreaScopedAction(contentuuid, destinationId.getId(), libraryName, false);
             repo.executeInVP(vctx, moveScopedAction);
             if (isDebug) {
               s_log.log(Level.FINEST, "move was successful? "+moveScopedAction.getReturnedValue());
            }
          }
          else {
             if (isDebug) {
               s_log.log(Level.FINEST, "destinationId was null, nothing to do");
            }
          }
      } catch (VirtualPortalNotFoundException e) {
         e.printStackTrace();
      } catch (WCMException e) {
         e.printStackTrace();
      }
      
      
      if (isDebug)
      {
         s_log.exiting("MoveNewsContentTask", "run");
      }
   }

}
