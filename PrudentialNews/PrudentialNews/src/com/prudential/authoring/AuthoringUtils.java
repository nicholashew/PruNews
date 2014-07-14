/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.prudential.authoring;

import com.ibm.workplace.wcm.api.*;
import com.ibm.workplace.wcm.api.exceptions.*;
import com.ibm.workplace.wcm.api.extensions.authoring.ActionResult;
import com.prudential.authoring.buttons.*;
import com.prudential.authoring.Result.ResultStatus;
import com.prudential.utils.Utils;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pete Raleigh
 */
public class AuthoringUtils {
   private static final Logger s_log = Logger.getLogger(ActivateNewsletterProfile.class.getName());

   private static Workspace wksp = null;

   // cmk change

   public static enum Action {
      ACTIVATE, DEACTIVATE;
   }

   private static final String ACTIVEPATH = "NewsletterProfiles/Active";

   private static final String INACTIVEPATH = "NewsletterProfiles/Inactive";

   public static ActionResult setActivation(Content cont, Action action) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      // use the docid instead of findbypath because we know the uuid of the site areas

      try {
         if (wksp == null) {
            wksp = Utils.getSystemWorkspace();
         }
         if (action == Action.ACTIVATE) {
            String activeUUID = "cb0552c0-d981-4e52-b495-36d9295f182a";

            DocumentId di;
            try {
               di = wksp.createDocumentId(activeUUID);
               // Found the required SiteArea
               PlacementLocation pl = new PlacementLocation(di, Placement.END);
               wksp.move(cont, pl, null);
            }
            catch (DocumentIdCreationException e) {
               // TODO Auto-generated catch block
               if (s_log.isLoggable(Level.FINEST)) {
                  s_log.log(Level.FINEST, "", e);
               }
            }

         }
         else if (action == Action.DEACTIVATE) {
            String inactiveUUID = "428ea38f-d977-40ee-b9e1-524f6c55d104";

            DocumentId di;
            try {
               di = wksp.createDocumentId(inactiveUUID);
               // Found the required SiteArea
               PlacementLocation pl = new PlacementLocation(di, Placement.END);
               wksp.move(cont, pl, null);
            }
            catch (DocumentIdCreationException e) {
               // TODO Auto-generated catch block
               if (s_log.isLoggable(Level.FINEST)) {
                  s_log.log(Level.FINEST, "", e);
               }
            }

         }
      }
      catch (DocumentRetrievalException ex) {
         Logger.getLogger(ActivateNewsletterProfile.class.getName()).log(Level.SEVERE, null, ex);
      }
      catch (AuthorizationException ex) {
         Logger.getLogger(ActivateNewsletterProfile.class.getName()).log(Level.SEVERE, null, ex);
      }
      catch (IllegalDocumentTypeException ex) {
         Logger.getLogger(ActivateNewsletterProfile.class.getName()).log(Level.SEVERE, null, ex);
      }
      catch (DuplicateChildException ex) {
         Logger.getLogger(ActivateNewsletterProfile.class.getName()).log(Level.SEVERE, null, ex);
      }

      if (isDebug) {
         s_log.exiting("ActivateNewsletterProfile", "execute " + cont.getName());
      }

      Result result = new Result(ResultStatus.SUCCESS);
      if (action == Action.ACTIVATE) {
         result.setTitle("Newsletter Profile moved to 'Active'");
         result.setDescription("Newsletter Profile moved to the 'Active' - " + ACTIVEPATH);
      }
      else if (action == Action.DEACTIVATE) {
         result.setTitle("Newsletter Profile moved to 'Inactive'");
         result.setDescription("Newsletter Profile moved to the 'Inactive' - " + INACTIVEPATH);
      }
      return (ActionResult) result;
   }
}
