/********************************************************************/
/* Asponte
 /* cmknight
 /********************************************************************/

package com.prudential.authoring.buttons;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;

import com.ibm.portal.ListModel;
import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.DocumentId;
import com.ibm.workplace.wcm.api.DocumentTypes;
import com.ibm.workplace.wcm.api.HTMLComponent;
import com.ibm.workplace.wcm.api.Item;
import com.ibm.workplace.wcm.api.JSPComponent;
import com.ibm.workplace.wcm.api.LibraryShortTextComponent;
import com.ibm.workplace.wcm.api.ShortTextComponent;
import com.ibm.workplace.wcm.api.WebContentService;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.exceptions.AuthorizationException;
import com.ibm.workplace.wcm.api.exceptions.DocumentIdCreationException;
import com.ibm.workplace.wcm.api.exceptions.PropertyRetrievalException;
import com.ibm.workplace.wcm.api.extensions.authoring.ActionResult;
import com.ibm.workplace.wcm.api.extensions.authoring.AuthoringAction;
import com.ibm.workplace.wcm.api.extensions.authoring.FormContext;
import com.ibm.workplace.wcm.api.extensions.authoring.directive.AuthoringDirective;
import com.ibm.workplace.wcm.api.extensions.authoring.directive.AuthoringDirectiveType;
import com.ibm.workplace.wcm.api.extensions.authoring.directive.CloseForm;
import com.ibm.workplace.wcm.api.query.Query;
import com.ibm.workplace.wcm.api.query.QueryService;
import com.ibm.workplace.wcm.api.query.ResultIterator;
import com.ibm.workplace.wcm.api.query.Selector;
import com.ibm.workplace.wcm.api.query.Selectors;
import com.prudential.utils.Utils;

public class PrudentialPushToPendingRetire implements AuthoringAction {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(PrudentialPushToPendingRetire.class.getName());

   private static String DESC = "PrudentialPushToPendingRetire used to allow managers to approve using System workspace";

   private static String TITLE = "Move to Pending Retire";

   /**
    * @see com.ibm.portal.Localized#getDescription(java.util.Locale)
    */
   @Override
   public String getDescription(Locale p_arg0) {

      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.exiting("PrudentialPushToPendingRetire", "getDescription returning " + DESC);
      }

      return DESC;
   }

   /**
    * @see com.ibm.portal.Localized#getTitle(java.util.Locale)
    */
   @Override
   public String getTitle(Locale p_arg0) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.exiting("PrudentialPushToPendingRetire", "getTitle returning " + TITLE);
      }
      return TITLE;
   }

   /**
    * @see com.ibm.workplace.wcm.api.extensions.authoring.AuthoringAction#execute(com.ibm.workplace.wcm.api.extensions.authoring.FormContext)
    */
   @Override
   public ActionResult execute(FormContext fc) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("PrudentialPushToPendingRetire", "execute " + fc);
      }

      Content theContent = (Content) fc.document();
      // now, get the contents of the html
      try {
         // Construct an initial Context
         InitialContext ctx = new InitialContext();

         // Retrieve WebContentService using JNDI name
         WebContentService webContentService = (WebContentService) ctx.lookup("portal:service/wcm/WebContentService");

         /**
          * 1) Update the history on the content
          * 2) Use system workspace to next stage
          */

         Workspace systemWs = Utils.getSystemWorkspace();
         Workspace wsUser = Utils.getWorkspace();
         wsUser.useDistinguishedNames(true);
         String userName = wsUser.getUserProfile().getUsername();
         //addHistoryLogEntry("Content copied by user " + userName);
         theContent.addHistoryLogEntry("Content pushed to Pending Retire by " + userName);
         //theContent.next
         //this.
         try {
            Content systemContent = (Content)systemWs.getById(theContent.getId());
            systemContent.nextWorkflowStage();
         }
         catch (Exception e) {
            if (isDebug) {
               e.printStackTrace();
            }
         }

      }
      catch (Exception e) {
         if (isDebug) {
            e.printStackTrace();
         }
      }
      AuthoringDirective theDirective = new CloseForm();
      CustomAuthoringActionResult theResult = new CustomAuthoringActionResult(theDirective);
      if (isDebug) {
         s_log.exiting("PrudentialPushToPendingRetire", "execute " + theResult);
      }
      return theResult;
   }

   /**
    * @see com.ibm.workplace.wcm.api.extensions.authoring.AuthoringAction#isValidForForm(com.ibm.workplace.wcm.api.extensions.authoring.FormContext)
    */
   @Override
   public boolean isValidForForm(FormContext fc) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      boolean isValid = false;
      if (isDebug) {
         s_log.entering("PrudentialPushToPendingRetire", "isValidForForm " + fc);
      }

      Workspace ws = Utils.getSystemWorkspace();
      Document theDoc = fc.document();
      if (theDoc instanceof Content) {
         Content theContent = (Content) theDoc;
         // check to see if the content is in the published stage
         // and if the current user has manager access
         // and if there's a pending action
         try {
            String publishedStageId = "dbaa9d23-fd56-4015-b121-c0fce5bd3b0a";
            DocumentId pubStageDocId = ws.createDocumentId(publishedStageId);
            DocumentId currentStageId = theContent.getWorkflowStageId();
            if (currentStageId.equals(pubStageDocId)) {
               if (isDebug) {
                  s_log.log(Level.FINEST, "Content in the published stage");
               }
               // if theres a gen date one and its > than today, or expire date, will be pending
               boolean isPending = false;
               Date genOne = theContent.getGeneralDateOne();
               Date expiry = theContent.getExpiryDate();
               Date now = new Date();
               if (genOne != null && genOne.after(now)) {
                  isPending = true;
               }
               if (!isPending && expiry != null && expiry.after(now)) {
                  isPending = true;
               }
               if (isDebug) {
                  s_log.log(Level.FINEST, "isPending = " + isPending);
               }
               if (isPending) {
                  isValid = true;
               }
            }

         }
         catch (AuthorizationException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST)) {
               s_log.log(Level.FINEST, "", e);
            }
         }
         catch (PropertyRetrievalException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST)) {
               s_log.log(Level.FINEST, "", e);
            }
         }
         catch (DocumentIdCreationException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST)) {
               s_log.log(Level.FINEST, "", e);
            }
         }

      }

      if (isDebug) {
         s_log.entering("PrudentialPushToPendingRetire", "isValidForForm returning " + isValid);
      }
      return isValid;
   }

   /**
    * @see com.ibm.workplace.wcm.api.extensions.authoring.AuthoringAction#ordinal()
    */
   @Override
   public int ordinal() {
      return 0;
   }

   public ListModel<Locale> getLocales() {
      return null;
   }
}
