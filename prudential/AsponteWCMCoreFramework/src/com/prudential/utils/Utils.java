/********************************************************************/
/* Asponte
 /* cmknight
 /********************************************************************/

package com.prudential.utils;

//import java.security.Principal;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.CompositeName;
import javax.naming.InitialContext;
import javax.naming.Name;

import com.ibm.portal.um.*;
import com.ibm.portal.um.exceptions.PumaAttributeException;
import com.ibm.portal.um.exceptions.PumaMissingAccessRightsException;
import com.ibm.portal.um.exceptions.PumaModelException;
import com.ibm.portal.um.exceptions.PumaSystemException;
import com.ibm.workplace.wcm.api.AuthoringTemplate;
import com.ibm.workplace.wcm.api.Category;
import com.ibm.workplace.wcm.api.ChildPosition;
import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.ContentLink;
import com.ibm.workplace.wcm.api.DateComponent;
import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.DocumentId;
import com.ibm.workplace.wcm.api.DocumentIdIterator;
import com.ibm.workplace.wcm.api.DocumentLibrary;
import com.ibm.workplace.wcm.api.DocumentTypes;
import com.ibm.workplace.wcm.api.Folder;
import com.ibm.workplace.wcm.api.LibraryComponent;
import com.ibm.workplace.wcm.api.LibraryShortTextComponent;
import com.ibm.workplace.wcm.api.MoveOptions;
import com.ibm.workplace.wcm.api.Placement;
import com.ibm.workplace.wcm.api.PlacementLocation;
import com.ibm.workplace.wcm.api.PresetFolderType;
import com.ibm.workplace.wcm.api.Repository;
import com.ibm.workplace.wcm.api.ShortTextComponent;
import com.ibm.workplace.wcm.api.SiteArea;
import com.ibm.workplace.wcm.api.WCM_API;
import com.ibm.workplace.wcm.api.WebContentService;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.exceptions.AuthorizationException;
import com.ibm.workplace.wcm.api.exceptions.ComponentNotFoundException;
import com.ibm.workplace.wcm.api.exceptions.DocumentCreationException;
import com.ibm.workplace.wcm.api.exceptions.DocumentRetrievalException;
import com.ibm.workplace.wcm.api.exceptions.DocumentSaveException;
import com.ibm.workplace.wcm.api.exceptions.DuplicateChildException;
import com.ibm.workplace.wcm.api.exceptions.IllegalDocumentTypeException;
import com.ibm.workplace.wcm.api.exceptions.IllegalTypeChangeException;
import com.ibm.workplace.wcm.api.exceptions.OperationFailedException;
import com.ibm.workplace.wcm.api.exceptions.ServiceNotAvailableException;
import com.ibm.workplace.wcm.api.query.Query;
import com.ibm.workplace.wcm.api.query.QueryDepth;
import com.ibm.workplace.wcm.api.query.QueryService;
import com.ibm.workplace.wcm.api.query.ResultIterator;
import com.ibm.workplace.wcm.api.query.Selector;
import com.ibm.workplace.wcm.api.query.Selectors;
import com.ibm.workplace.wcm.services.config.WCMConfig;
import com.prudential.commons.cache.*;
import com.prudential.wcm.WCMUtils;

public class Utils {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(Utils.class.getName());

   private static String default_vp = "PRUDENTIAL_PLACEHOLDER";

   private static String default_vp_name = "prudential";

   private static String default_libraryname = "PrudentialNewsletterConfiguration";

   private static String default_shorttextcmpntname = "VirtualPortalName";

   /** hard coded for now, but can pass setter */
   public static String email_username = "prudentialalert@gmail.com";

   /** */
   public static String email_password = "jk78uijk";

   /**
    * From Email Address
    */
   public static String fromEmailAddress = "prudentialalert@gmail.com";

   /** The Puma Home Reference */
   private static PumaHome s_pumaHome = null;

   /** The primary email attribute key */
   public static final String PRIMARY_EMAIL_KEY = "ibm-primaryEmail";

   public static String p_prevStageCmpnt = "PreviousStage";

   public static LibraryComponent getLibraryComponentByName(Workspace ws, String name, String libraryName) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      LibraryComponent returnComponent = null;
      DocumentLibrary originalLib = null;
      if (isDebug) {
         s_log.entering("Utils", "getLibraryComponentByName called for " + name + " in library " + libraryName);
      }
      try {
         if (ws != null) {

            ws.login();
            originalLib = ws.getCurrentDocumentLibrary();
            ws.setCurrentDocumentLibrary(ws.getDocumentLibrary(libraryName));

            DocumentIdIterator results = ws.findComponentByName(name);
            if (results.hasNext()) {
               DocumentId theResult = (DocumentId) results.next();
               returnComponent = (LibraryComponent) ws.getById(theResult);
            }
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
         if (ws != null) {
            if (originalLib != null) {
               ws.setCurrentDocumentLibrary(originalLib);
            }
            ws.logout();
         }
      }

      if (isDebug) {
         String componentName = "null";
         if (returnComponent != null) {
            componentName = returnComponent.getName();
         }
         s_log.exiting("Utils", "getLibraryComponentByName returning " + componentName);
      }

      return returnComponent;
   }

   /**
    * 
    * getDistributionListId helper method to get the site area
    * 
    * @param ws
    *            the workspace
    * @param name
    *            the name of the site area to get
    * @param libraryName
    *            the library to retrieve in
    * @return
    */
   public static DocumentId getSiteAreaIdByName(Workspace ws, String name, String libraryName) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      DocumentId returnId = null;
      if (isDebug) {
         s_log.entering("Utils", "getSiteAreaIdByName called for " + name + " in library " + libraryName);
      }
      DocumentLibrary currentDocLib = ws.getCurrentDocumentLibrary();
      ws.setCurrentDocumentLibrary(ws.getDocumentLibrary(libraryName));
      try {
         // find the parent site area, if it doesn't exist create it
         // DocumentIdIterator
         QueryService queryService = ws.getQueryService();
         Query query = queryService.createQuery();

         Selector titleSelector = Selectors.titleEquals(name);
         Selector typeSelector = Selectors.typeEquals(DocumentTypes.SiteArea.getApiType());
         query.addSelector(titleSelector);
         query.addSelector(typeSelector);

         ResultIterator results = queryService.execute(query);
         if (results.hasNext()) {
            SiteArea theSiteArea = (SiteArea) results.next();
            returnId = theSiteArea.getId();
            if (isDebug) {
               s_log.log(Level.FINEST, "returnId found " + returnId);
            }
         }
         else {
            if (isDebug) {
               s_log.log(Level.FINEST, "Site area NOT found");
            }
            returnId = null;
         }
      }
      catch (Exception e) {
         if (isDebug) {
            s_log.log(Level.FINEST, "Exception " + e);
            e.printStackTrace();
         }
         returnId = null;
      }
      finally {
         ws.setCurrentDocumentLibrary(currentDocLib);
      }

      if (isDebug) {
         s_log.exiting("Utils", "getSiteAreaIdByName returing " + returnId);
      }
      return returnId;
   }

   /**
    * 
    * getSiteAreaIdByNameAndParent helper method to get the site area by name and parent
    * 
    * @param ws
    *            the workspace
    * @param name
    *            the name of the site area to get
    * @param libraryName
    *            the library to retrieve in
    * @return DocumentId of the sitearea if it exists
    */
   public static DocumentId getSiteAreaIdByNameAndParent(Workspace ws, String name, DocumentId parentId, String libraryName) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      DocumentId returnId = null;
      if (isDebug) {
         s_log.entering("Utils", "getSiteAreaIdByName called for " + name + " in library " + libraryName);
      }
      DocumentLibrary currentDocLib = ws.getCurrentDocumentLibrary();
      ws.setCurrentDocumentLibrary(ws.getDocumentLibrary(libraryName));
      try {
         // find the parent site area, if it doesn't exist create it
         // DocumentIdIterator
         QueryService queryService = ws.getQueryService();
         Query query = queryService.createQuery();

         Selector titleSelector = Selectors.titleEquals(name);
         Selector typeSelector = Selectors.typeEquals(DocumentTypes.SiteArea.getApiType());
         query.addSelector(titleSelector);
         query.addSelector(typeSelector);
         query.addParentId(parentId, QueryDepth.CHILDREN);

         ResultIterator results = queryService.execute(query);
         if (results.hasNext()) {
            SiteArea theSiteArea = (SiteArea) results.next();
            returnId = theSiteArea.getId();
            if (isDebug) {
               s_log.log(Level.FINEST, "returnId found " + returnId);
            }
         }
         else {
            if (isDebug) {
               s_log.log(Level.FINEST, "Site area NOT found");
            }
            returnId = null;
         }
      }
      catch (Exception e) {
         if (isDebug) {
            s_log.log(Level.FINEST, "Exception " + e);
            e.printStackTrace();
         }
         returnId = null;
      }
      finally {
         ws.setCurrentDocumentLibrary(currentDocLib);
      }

      if (isDebug) {
         s_log.exiting("Utils", "getSiteAreaIdByName returing " + returnId);
      }
      return returnId;
   }

   /**
    * 
    * getDistributionListId helper method to get the site area
    * 
    * @param ws
    *            the workspace
    * @param name
    *            the name of the AuthoringTemplate to get
    * @param libraryName
    *            the library to retrieve in
    * 
    * @return
    */
   public static DocumentId getAuthoringTemplateIdByName(Workspace ws, String name, String libraryName) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      DocumentId returnId = null;
      if (isDebug) {
         s_log.entering("Utils", "getAuthoringTemplateIdByName called for " + name + " in library " + libraryName);
      }
      DocumentLibrary currentDocLib = ws.getCurrentDocumentLibrary();
      ws.setCurrentDocumentLibrary(ws.getDocumentLibrary(libraryName));
      try {
         // find the parent site area, if it doesn't exist create it
         // DocumentIdIterator
         QueryService queryService = ws.getQueryService();
         Query query = queryService.createQuery();

         Selector titleSelector = Selectors.titleEquals(name);
         Selector typeSelector = Selectors.typeEquals(DocumentTypes.AuthoringTemplate.getApiType());
         query.addSelector(titleSelector);
         query.addSelector(typeSelector);

         ResultIterator results = queryService.execute(query);
         if (results.hasNext()) {
            AuthoringTemplate theAuthTemplate = (AuthoringTemplate) results.next();
            returnId = theAuthTemplate.getId();
            if (isDebug) {
               s_log.log(Level.FINEST, "returnId found " + returnId);
            }
         }
         else {
            if (isDebug) {
               s_log.log(Level.FINEST, "AuthoringTemplate NOT found");
            }
            returnId = null;
         }
      }
      catch (Exception e) {
         if (isDebug) {
            s_log.log(Level.FINEST, "Exception " + e);
            e.printStackTrace();
         }
         returnId = null;
      }
      finally {
         ws.setCurrentDocumentLibrary(currentDocLib);
      }

      if (isDebug) {
         s_log.exiting("Utils", "getAuthoringTemplateIdByName returing " + returnId);
      }
      return returnId;
   }

   /**
    * 
    * getDistributionListId helper method to get the site area
    * 
    * @param ws
    *            the workspace
    * @param name
    *            the name of the Content to get
    * @param libraryName
    *            the library to retrieve in
    * 
    * @return
    */
   public static DocumentId getContentIdByName(Workspace ws, String name, String libraryName) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      DocumentId returnId = null;
      if (isDebug) {
         s_log.entering("Utils", "getContentIdByName called for " + name + " in library " + libraryName);
      }
      DocumentLibrary currentDocLib = ws.getCurrentDocumentLibrary();
      ws.setCurrentDocumentLibrary(ws.getDocumentLibrary(libraryName));
      try {
         // find the parent site area, if it doesn't exist create it
         // DocumentIdIterator
         QueryService queryService = ws.getQueryService();
         Query query = queryService.createQuery();

         Selector titleSelector = Selectors.titleEquals(name);
         Selector typeSelector = Selectors.typeEquals(DocumentTypes.Content.getApiType());
         query.addSelector(titleSelector);
         query.addSelector(typeSelector);

         ResultIterator results = queryService.execute(query);
         if (results.hasNext()) {
            Content theContent = (Content) results.next();
            returnId = theContent.getId();
            if (isDebug) {
               s_log.log(Level.FINEST, "returnId found " + returnId);
            }
         }
         else {
            if (isDebug) {
               s_log.log(Level.FINEST, "Content NOT found");
            }
            returnId = null;
         }
      }
      catch (Exception e) {
         if (isDebug) {
            s_log.log(Level.FINEST, "Exception " + e);
            e.printStackTrace();
         }
         returnId = null;
      }
      finally {
         ws.setCurrentDocumentLibrary(currentDocLib);
      }

      if (isDebug) {
         s_log.exiting("Utils", "getContentIdByName returing " + returnId);
      }
      return returnId;
   }

   public static DocumentId createSiteArea(Workspace ws, DocumentId newsParentId, String siteAreaName, String libraryName)
      throws Exception, DocumentCreationException, AuthorizationException, IllegalDocumentTypeException, DocumentSaveException,
      DuplicateChildException {
      boolean isDebug = s_log.isLoggable(Level.FINEST);

      if (isDebug) {
         s_log.entering("Utils", "createSiteArea called for " + siteAreaName + " under " + newsParentId + " in library " + libraryName);
      }
      DocumentId resultId;
      DocumentLibrary currentDocLib = ws.getCurrentDocumentLibrary();
      ws.setCurrentDocumentLibrary(ws.getDocumentLibrary(libraryName));
      try {
         if (newsParentId == null) {
            throw new Exception("Could not retrieve newsParentId site area return error");
         }
         SiteArea newSA = ws.createSiteArea(newsParentId, null, ChildPosition.START);
         newSA.setName(siteAreaName);

         String errors[] = ws.save(newSA);
         if (isDebug) {
            for (int x = 0; x < errors.length; x++) {
               s_log.log(Level.FINEST, "error saving site area " + newSA.getName() + " " + errors[x]);
            }
         }
         if (errors.length > 0) {
            throw new Exception("Error saving content");
         }
         resultId = newSA.getId();
      }
      catch (Exception e) {
         resultId = null;
         if (isDebug) {
            s_log.log(Level.FINEST, "Exception " + e);
            e.printStackTrace();
         }
      }

      finally {
         ws.setCurrentDocumentLibrary(currentDocLib);
      }
      return resultId;
   }

   public static DocumentId createContent(Workspace ws, DocumentId authTemplateId, DocumentId newsParentId, String contentName,
      String libraryName) throws DocumentCreationException, AuthorizationException, IllegalDocumentTypeException, DocumentSaveException,
      DuplicateChildException, Exception {

      return createContent(ws, authTemplateId, newsParentId, contentName, libraryName, null);
   }

   /**
    * 
    * createContent description
    * @param ws
    * @param authTemplateId
    * @param newsParentId
    * @param contentName
    * @param libraryName
    * @param categoryIds - the 
    * @return
    * @throws Exception
    * @throws DocumentCreationException
    * @throws AuthorizationException
    * @throws IllegalDocumentTypeException
    * @throws DocumentSaveException
    * @throws DuplicateChildException
    */
   public static DocumentId createContent(Workspace ws, DocumentId authTemplateId, DocumentId newsParentId, String contentName,
      String libraryName, DocumentId[] categoryIds) throws Exception, DocumentCreationException, AuthorizationException,
      IllegalDocumentTypeException, DocumentSaveException, DuplicateChildException {
      boolean isDebug = s_log.isLoggable(Level.FINEST);

      if (isDebug) {
         s_log.entering("Utils", "createContent called for " + contentName + " under " + newsParentId + " using auth template "
            + authTemplateId + " in library " + libraryName);
      }

      DocumentId resultId;
      DocumentLibrary currentDocLib = ws.getCurrentDocumentLibrary();
      ws.setCurrentDocumentLibrary(ws.getDocumentLibrary(libraryName));
      try {
         if (newsParentId == null) {
            throw new Exception("Could not retrieve newsParentId site area return error");
         }
         Content newContent = ws.createContent(authTemplateId, newsParentId, null, ChildPosition.START);
         newContent.setName(contentName);
         if (categoryIds != null) {
            newContent.addCategoryIds(categoryIds);
         }
         String errors[] = ws.save(newContent);
         if (isDebug) {
            for (int x = 0; x < errors.length; x++) {
               s_log.log(Level.FINEST, "error saving content " + newContent.getName() + " " + errors[x]);
            }
         }
         if (errors.length > 0) {
            throw new Exception("Error saving content");
         }
         resultId = newContent.getId();
      }
      catch (Exception e) {
         resultId = null;
         if (isDebug) {
            s_log.log(Level.FINEST, "Exception " + e);
            e.printStackTrace();
         }
      }
      finally {
         ws.setCurrentDocumentLibrary(currentDocLib);
      }

      return resultId;
   }

   /**
    * helper method to check for the VPName that we're supposed to be running
    * in if any this assumes a library in the Base portal named
    * PrudentialNewsletterConfiguration and a shorttext named VirtualPortalName
    * We will use a cache, so if the value is changed in the config content a
    * restart or a cache refresh will be necessary
    * 
    * @return
    */
   public static String getVPName() {

      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("Utils", "getVPName");
      }
      String theVPName = null;

      String cacheKey = default_vp;
      TheCache theCache = TheCache.getInstance();
      TheCacheEntry cacheEntry = null;

      /*
       * retrieve from the cache. If the cache is empty retrieve from WCM. If
       * WCM doesn't have the value store default_vp in as the value
       */
      cacheEntry = theCache.get(cacheKey);
      if (cacheEntry != null) {
         String cacheContent = (String) cacheEntry.getCacheEntry();
         if (isDebug) {
            s_log.log(Level.FINEST, "item for cacheKey was retrieved");
            s_log.log(Level.FINEST, "cacheContent was" + cacheContent);
         }
         if (!cacheContent.equals(default_vp)) {
            theVPName = cacheContent;
         }
      }
      // else if null, then
      else {
         /*
          * get a system workspace retrieve the component if it doesn't
          * exist, just store default_vp
          */
         Workspace ws = null;

         try {
            // Construct an initial Context
            InitialContext ctx = new InitialContext();

            // Retrieve WebContentService using JNDI name
            WebContentService webContentService = (WebContentService) ctx.lookup("portal:service/wcm/WebContentService");

            // get the system workspace
            ws = webContentService.getRepository().getSystemWorkspace();
            ws.login();
            // set the doc lib
            DocumentLibrary theLib = null;
            theLib = ws.getDocumentLibrary(default_libraryname);
            if (theLib == null) {
               if (isDebug) {
                  s_log.log(Level.FINEST, "could not retrieve library " + default_libraryname);
                  // dump out all libs
                  Iterator libs = ws.getDocumentLibraries();
                  if (libs.hasNext()) {
                     DocumentLibrary tempLib = (DocumentLibrary) libs.next();
                     s_log.log(Level.FINEST, "tempLib is " + tempLib.getName());
                     s_log.log(Level.FINEST, "tempLib id " + tempLib.getId());
                  }
                  else {
                     s_log.log(Level.FINEST, "no libraries found.");
                  }
               }

               // set up the cache with an initial value
               theCache.put(default_vp, default_vp);
            }
            else {
               if (isDebug) {
                  s_log.log(Level.FINEST, "retrieved library " + default_libraryname);
               }
               ws.setCurrentDocumentLibrary(theLib);
               // now retrieve the shorttext cmpnt by name
               DocumentIdIterator theIt = ws.findByName(DocumentTypes.ShortTextComponent, default_shorttextcmpntname);
               if (theIt.hasNext()) {
                  if (isDebug) {
                     s_log.log(Level.FINEST, "found component " + default_shorttextcmpntname);
                  }
                  LibraryShortTextComponent stc = (LibraryShortTextComponent) ws.getById(theIt.nextId());
                  if (stc != null) {
                     String vpValue = stc.getText();
                     if (vpValue != null) {
                        theVPName = vpValue;
                        theCache.put(default_vp, theVPName);
                        if (isDebug) {
                           s_log.log(Level.FINEST, "retrieved value from WCM, caching the value as well");
                        }
                     }
                  }
               }
               else {
                  if (isDebug) {
                     s_log.log(Level.FINEST, "unable to retrieve " + default_shorttextcmpntname);
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
            if (ws != null) {
               ws.logout();
            }
         }
      }

      if (isDebug) {
         s_log.exiting("Utils", "getVPName returning " + theVPName);
      }

      return theVPName;
   }

   public static Workspace getSystemWorkspace() {
      Repository repo = WCM_API.getRepository();
      Workspace ws = null;
      try {
         if (WCMUtils.isWCMWidgets()) {
            ws = repo.getWorkspace("cknight", "ses03pwd");
         }
         else {
            ws = repo.getSystemWorkspace();
         }

      }
      catch (ServiceNotAvailableException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (OperationFailedException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

      return ws;
   }

   public static boolean moveOrLinkContentToSiteArea(String p_contentuuid, String p_parentuuid, String p_libraryName, boolean p_createLink) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("Utils", "moveOrLinkContentToSiteArea");
      }

      boolean returnedValue = false;
      // get system workspace, login, then process the move
      Workspace ws = Utils.getSystemWorkspace();
      boolean libraryChanged = false;
      DocumentLibrary oldLib = ws.getCurrentDocumentLibrary();
      if (ws != null) {
         try {
            // get the library, content, and parent

            DocumentLibrary theLib = ws.getDocumentLibrary(p_libraryName);

            if (!oldLib.getName().equals(theLib.getName())) {
               libraryChanged = true;
            }

            // get the contentid 
            DocumentId theContentId = null;
            theContentId = ws.createDocumentId(p_contentuuid);
            if (theContentId == null) {
               throw new Exception("the contentuuid value could't be retrieved");
            }

            // get the site area
            DocumentId theSiteAreaId = null;
            theSiteAreaId = ws.createDocumentId(p_parentuuid);

            if (theSiteAreaId == null) {
               throw new Exception("the parentuuid value could't be retrieved");
            }

            Content theContent = (Content) ws.getById(theContentId);

            // now, move the content to this site area
            // or link
            if (p_createLink) {
               try {
                  ContentLink createdLink = ws.createContentLink(theContentId, theSiteAreaId, null, ChildPosition.END);
                  if (createdLink == null) {
                     throw new Exception("Could not create link to content " + theContentId + " in site area " + theSiteAreaId);
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
                  if (isDebug) {
                     s_log.log(Level.FINEST, "about to attempt to move " + theContent.getName() + " under " + loc.getTargetDocId());
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
         s_log.exiting("Utils", "moveOrLinkContentToSiteArea set returnedValue = " + returnedValue);
      }

      return returnedValue;
   }

   public static String getEmailAddressFromUser(final Principal p_user) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      String emailAddress = "";
      if (isDebug) {
         s_log.entering("Utils", "getEmailAddressFromUser called for " + p_user);
      }
      try {
         if (getPumaHome() != null) {
            PrivilegedExceptionAction<String> getEmailAddress = new PrivilegedExceptionAction<String>() {

               public String run() throws PumaAttributeException, PumaSystemException, PumaModelException, PumaMissingAccessRightsException {
                  String returnAttributeString = null;
                  List<String> attributeNames = new ArrayList<String>(1);
                  attributeNames.add(PRIMARY_EMAIL_KEY);

                  // Get the attribute value
                  Map attributes = getPumaHome().getProfile().getAttributes(p_user, attributeNames);
                  returnAttributeString = (String) attributes.get(PRIMARY_EMAIL_KEY);
                  return returnAttributeString;
               }

            };

            emailAddress = getPumaHome().getEnvironment().runUnrestricted(getEmailAddress);
         }
      }
      catch (Exception e) {
         if (isDebug) {
            s_log.log(Level.FINEST, "getEmailAddressFromUser exception " + e.getMessage());
            e.printStackTrace();
         }
      }

      if (isDebug) {
         s_log.exiting("Utils", "getEmailAddressFromUser returning " + emailAddress);
      }
      return emailAddress;
   }

   public static String getAttributeFromUser(final Principal p_user, final String attributeToRetrieve) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      String returnAttribute = "";
      if (isDebug) {
         s_log.entering("Utils", "getAttributeFromUser called for " + p_user + " for attribute " + attributeToRetrieve);
      }
      try {
         if (getPumaHome() != null) {

            PrivilegedExceptionAction<String> getReturnAttribute = new PrivilegedExceptionAction<String>() {

               public String run() throws PumaAttributeException, PumaSystemException, PumaModelException, PumaMissingAccessRightsException {
                  String returnAttributeString = null;
                  List<String> attributeNames = new ArrayList<String>(1);
                  attributeNames.add(attributeToRetrieve);

                  // Get the attribute value
                  Map attributes = getPumaHome().getProfile().getAttributes(p_user, attributeNames);
                  returnAttributeString = (String) attributes.get(returnAttributeString);

                  return returnAttributeString;
               }

            };

            returnAttribute = getPumaHome().getEnvironment().runUnrestricted(getReturnAttribute);
         }
      }
      catch (Exception e) {
         if (isDebug) {
            s_log.log(Level.FINEST, "getAttributeFromUser exception " + e.getMessage());
            e.printStackTrace();
         }
      }

      if (isDebug) {
         s_log.exiting("Utils", "getAttributeFromUser returning " + returnAttribute);
      }
      return returnAttribute;
   }

   /**
    * getPumaHome helper method to get puma home
    * 
    * @return PumaHome
    */
   public static PumaHome getPumaHome() {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (s_pumaHome == null) {
         try {
            InitialContext ctx = new InitialContext();
            Name myjndiname = new CompositeName(PumaHome.JNDI_NAME);
            s_pumaHome = (PumaHome) ctx.lookup(myjndiname);
         }
         catch (Exception e) {
            if (isDebug) {
               s_log.log(Level.FINEST, "Exception getting puma home " + e.getMessage());
               e.printStackTrace();
            }
         }
      }
      return s_pumaHome;
   }

   /**
    * helper method to get user by dn
    */
   public static User getUserById(final String p_dn) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);

      if (isDebug) {
         s_log.entering("Utils", "getUserById called with " + p_dn);
      }
      User user = null;
      try {
         List<User> users = Collections.emptyList();
         boolean retrieved = false;
         try {
            if (isDebug) {
               s_log.log(Level.FINEST, "retrieving by default attribute");
            }
            PrivilegedExceptionAction<User> getUser = new PrivilegedExceptionAction<User>() {

               public User run() throws PumaAttributeException, PumaSystemException, PumaModelException, PumaMissingAccessRightsException {
                  boolean isDebug = s_log.isLoggable(Level.FINEST);
                  User returnUser = null;
                  List<User> users = Collections.emptyList();
                  if (isDebug) {
                     s_log.log(Level.FINEST, "trying findUsersByDefaultAttribute "+p_dn);
                  }
                  users = getPumaHome().getLocator().findUsersByDefaultAttribute(p_dn);
                  if (!users.isEmpty()) {
                     returnUser = users.get(0);                     
                  }
                  if(returnUser == null) {
                     if (isDebug) {
                        s_log.log(Level.FINEST, "trying findUsersByAttribute cn "+p_dn);
                     }
                     users = getPumaHome().getLocator().findUsersByAttribute("cn", p_dn);
                     if (!users.isEmpty()) {
                        returnUser = users.get(0);                     
                     }
                  }
                  if(returnUser == null) {
                     if (isDebug) {
                        s_log.log(Level.FINEST, "trying findUsersByAttribute uid "+p_dn);
                     }
                     users = getPumaHome().getLocator().findUsersByAttribute("uid", p_dn);
                     if (!users.isEmpty()) {
                        returnUser = users.get(0);                     
                     }
                  }
                  return returnUser;
               }

            };
            user = getPumaHome().getEnvironment().runUnrestricted(getUser);
         }
         catch (Exception e) {
            if (isDebug) {
               s_log.log(Level.FINEST, "Exception getting user " + p_dn + " because " + e.getMessage());
               // e.printStackTrace();
            }
         }         
      }
      catch (Exception e) {
         if (isDebug) {
            s_log.log(Level.FINEST, "Exception getting user " + p_dn + " because " + e.getMessage());
            e.printStackTrace();
         }
      }

      if (isDebug) {
         if (user != null) {
            s_log.exiting("Utils", "getUserByDistinguishedName result " + user);
         }
         else {
            s_log.exiting("Utils", "getUserByDistinguishedName result null!");
         }
      }

      return user;
   }

   /**
    * helper method to get user by dn
    */
   public static User getUserByDN(final String p_dn) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);

      if (isDebug) {
         s_log.entering("Utils", "getUserByDN called with " + p_dn);
      }
      User user = null;
      try {
         PrivilegedExceptionAction<User> getUser = new PrivilegedExceptionAction<User>() {

            public User run() throws PumaAttributeException, PumaSystemException, PumaModelException, PumaMissingAccessRightsException {
               User returnUser = null;
               returnUser = getPumaHome().getLocator().findUserByIdentifier(p_dn);
               return returnUser;
            }

         };
         
         user = getPumaHome().getEnvironment().runUnrestricted(getUser);
      }
      catch (Exception e) {
         if (isDebug) {
            s_log.log(Level.FINEST, "Exception getting user " + p_dn + " because " + e.getMessage());
            e.printStackTrace();
         }
      }

      if (isDebug) {
         if (user != null) {
            s_log.exiting("Utils", "getUserByDN result " + user);
         }
         else {
            s_log.exiting("Utils", "getUserByDN result null!");
         }
      }

      return user;
   }

   /**
    * getGroupMembers helper method to get all users
    * 
    * @param p_group
    * @param p_findNestedMembers
    * @return
    */
   public static List<Principal> getGroupMembers(final Group p_group, final boolean p_findNestedMembers) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("Utils", "getGroupMembers called for " + p_group);
      }
      List<Principal> members = null;
      try {
         PrivilegedExceptionAction<List> getMembers = new PrivilegedExceptionAction<List>() {

            public List run() throws PumaAttributeException, PumaSystemException, PumaModelException, PumaMissingAccessRightsException {
               List returnMembers = null;
               returnMembers = getPumaHome().getLocator().findMembersByGroup(p_group, p_findNestedMembers);
               return returnMembers;
            }

         };
         
         members = getPumaHome().getEnvironment().runUnrestricted(getMembers);
      }
      catch (Exception e) {
         if (isDebug) {
            s_log.log(Level.FINEST, "getGroupMembers exception " + e.getMessage());
            e.printStackTrace();
         }
      }
      return members;
   }

   /**
    * getGroupByDistinguishedName helper method to get a group by dn
    * 
    * @param p_dn
    * @return
    */
   public static Group getGroupByDistinguishedName(final String p_dn) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      Group theGroup = null;
      if (isDebug) {
         s_log.entering("Utils", "getGroupByDistinguishedName called for " + p_dn);
      }
      try {
         if (getPumaHome() != null) {
            PrivilegedExceptionAction<Group> getGroup = new PrivilegedExceptionAction<Group>() {

               public Group run() throws PumaAttributeException, PumaSystemException, PumaModelException, PumaMissingAccessRightsException {
                  Group returnGroup = null;
                  getPumaHome().getLocator().findGroupByIdentifier(p_dn);
                  return returnGroup;
               }

            };
            theGroup = getPumaHome().getEnvironment().runUnrestricted(getGroup);
         }
      }
      catch (Exception e) {
         if (isDebug) {
            s_log.log(Level.FINEST, "getGroupByDistinguishedName exception " + e.getMessage());
            e.printStackTrace();
         }
      }

      if (isDebug) {
         s_log.exiting("Utils", "getGroupByDistinguishedName returning " + theGroup);
      }
      return theGroup;
   }

   /**
    * getGroupByDistinguishedName helper method to get a group by dn
    * 
    * @param p_dn
    * @return
    */
   public static Group getGroupById(final String p_id) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      Group theGroup = null;
      if (isDebug) {
         s_log.entering("Utils", "getGroupById called for " + p_id);
      }

      try {
         if (getPumaHome() != null) {
            PrivilegedExceptionAction<Group> getGroup = new PrivilegedExceptionAction<Group>() {

               public Group run() throws PumaAttributeException, PumaSystemException, PumaModelException, PumaMissingAccessRightsException {
                  Group returnGroup = null;
                  List<Group> groups = Collections.emptyList();
                  groups = getPumaHome().getLocator().findGroupsByDefaultAttribute(p_id);
                  if (!groups.isEmpty()) {
                     returnGroup = groups.get(0);
                  }

                  if (returnGroup == null) {
                     groups = getPumaHome().getLocator().findGroupsByAttribute("cn", p_id);
                     if (!groups.isEmpty()) {
                        returnGroup = groups.get(0);
                     }
                  }
                  if (returnGroup == null) {
                     groups = getPumaHome().getLocator().findGroupsByAttribute("uid", p_id);
                     if (!groups.isEmpty()) {
                        returnGroup = groups.get(0);
                     }
                  }
                  return returnGroup;
               }

            };
            theGroup = getPumaHome().getEnvironment().runUnrestricted(getGroup);
         }
      }
      catch (Exception e) {
         if (isDebug) {
            s_log.log(Level.FINEST, "getGroupById exception " + e.getMessage());
            e.printStackTrace();
         }
      }

      if (isDebug) {
         s_log.exiting("Utils", "getGroupById returning " + theGroup);
      }
      return theGroup;
   }

   /**
    * Get an API workspace for the current user
    * @return the workspace
    */
   public static Workspace getWorkspace() {
      boolean isDebug = s_log.isLoggable(Level.FINEST);

      Workspace theWorkspace = null;
      try {
         theWorkspace = WCM_API.getRepository().getWorkspace();
      }

      catch (Exception e) {
         if (isDebug) {
            s_log.log(Level.FINEST, "Exception getting workspace");
            e.printStackTrace();
         }
      }
      return theWorkspace;
   }

   /**
    * 
    * getEmailAddresses return email address(es) for the principal
    * could be a group
    * @param p_principal - the user or group to get email addresses for
    * @return list of email addresses
    */
   public static ArrayList getEmails(java.security.Principal p_principal) {
      ArrayList returnList = new ArrayList();
      boolean isDebug = s_log.isLoggable(Level.FINEST);

      if (isDebug) {
         s_log.entering("Utils", "getEmails " + p_principal);
      }

      String theDn = getDnForJavaPrincipal(p_principal);
      if (isDebug) {
         s_log.log(Level.FINEST, "theDn = " + theDn);
      }
      // check if it's a group
      //p_principal.
      //User theUser = Utils.getUserByDN(p_principal.getName());
      User theUser = Utils.getUserByDN(theDn);
      if (theUser != null) {
         if (isDebug) {
            s_log.log(Level.FINEST, "theUser = " + theUser);
         }
         returnList.addAll(getEmailsUser(theUser));
      }
      // else, try group
      else {
         if (isDebug) {
            s_log.log(Level.FINEST, "theUser was null, check group");
         }
         // try to get a group
         Group theGroup = Utils.getGroupByDistinguishedName(theDn);
         if (theGroup != null) {
            if (isDebug) {
               s_log.log(Level.FINEST, "theGroup = " + theGroup);
            }
            returnList.addAll(getEmailsGroup(theGroup));
         }
         else {
            if (isDebug) {
               s_log.log(Level.FINEST, "theGroup was null");
            }
         }
      }

      if (isDebug) {
         s_log.exiting("Utils", "getEmails returning " + returnList);
      }

      return returnList;
   }

   /**
    * 
    * getEmailsUser description
    * @param theUser
    * @return
    */
   public static ArrayList getEmailsUser(User theUser) {
      ArrayList returnList = new ArrayList();
      boolean isDebug = s_log.isLoggable(Level.FINEST);

      if (theUser != null) {
         if (isDebug) {
            s_log.log(Level.FINEST, "theUser = " + theUser);
         }
         String emailAddress = Utils.getEmailAddressFromUser(theUser);
         if (emailAddress != null) {
            if (isDebug) {
               s_log.log(Level.FINEST, "emailAddress for theUser = " + emailAddress);
            }
            returnList.add(emailAddress);
         }
      }

      return returnList;
   }

   /**
    * 
    * getEmailsUser description
    * @param theUser
    * @return
    */
   public static ArrayList getEmailsGroup(Group theGroup) {
      ArrayList returnList = new ArrayList();
      boolean isDebug = s_log.isLoggable(Level.FINEST);

      // now get the membership
      if (theGroup != null) {
         if (isDebug) {
            s_log.log(Level.FINEST, "theGroup = " + theGroup);
         }
         List<com.ibm.portal.um.Principal> members = Utils.getGroupMembers(theGroup, true);
         if (members != null) {
            Iterator<com.ibm.portal.um.Principal> groupMembers = members.iterator();
            while (groupMembers.hasNext()) {

               com.ibm.portal.um.Principal tempUser = (com.ibm.portal.um.Principal) groupMembers.next();
               if (isDebug) {
                  s_log.log(Level.FINEST, "group contains member " + tempUser);
               }
               String emailAddress = Utils.getEmailAddressFromUser(tempUser);
               if (emailAddress != null && !emailAddress.equals("")) {
                  if (isDebug) {
                     s_log.log(Level.FINEST, "adding email address " + emailAddress);
                  }
                  returnList.add(emailAddress);
               }
            }
         }
      }

      return returnList;
   }

   /**
    * 
    * getDnForPrincipal helper method to get the dn for a principal object
    * @param p_principal
    * @return
    */
   public static String getDnForPrincipal(final Principal p_principal) {
      String returnString = "";
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("Utils", "getDnForPrincipal " + p_principal.toString());
      }

      try {
         PrivilegedExceptionAction<String> getDn = new PrivilegedExceptionAction<String>() {

            public String run() throws PumaAttributeException, PumaSystemException, PumaModelException, PumaMissingAccessRightsException {
               String returnAttributeString = null;
               returnAttributeString = getPumaHome().getProfile().getIdentifier(p_principal);
               return returnAttributeString;
            }

         };

         returnString = getPumaHome().getEnvironment().runUnrestricted(getDn);
      }
      catch (PrivilegedActionException e) {
         // TODO Auto-generated catch block
         if (s_log.isLoggable(Level.FINEST)) {
            s_log.log(Level.FINEST, "", e);
         }
      }
      if (isDebug) {
         s_log.exiting("Utils", "getDnForPrincipal " + returnString);
      }

      return returnString;
   }

   /**
    * 
    * getDnForPrincipal helper method to get the dn for a principal object
    * @param p_principal
    * @return
    */
   public static String getDnForJavaPrincipal(final java.security.Principal p_principal) {
      String returnString = "";
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("Utils", "getDnForJavaPrincipal " + p_principal.toString());
      }

      try {
         PrivilegedExceptionAction<String> getDn = new PrivilegedExceptionAction<String>() {

            public String run() throws PumaAttributeException, PumaSystemException, PumaModelException, PumaMissingAccessRightsException {
               String theReturnString = "";
               Principal thePumaPrincipal = getUserById(p_principal.getName());
               if (thePumaPrincipal == null) {
                  thePumaPrincipal = getGroupById(p_principal.getName());
               }
               theReturnString = getPumaHome().getProfile().getIdentifier(thePumaPrincipal);
               return theReturnString;
            }

         };

         returnString = getPumaHome().getEnvironment().runUnrestricted(getDn);

      }

      catch (PrivilegedActionException e) {
         // TODO Auto-generated catch block
         if (s_log.isLoggable(Level.FINEST)) {
            s_log.log(Level.FINEST, "", e);
         }
      }
      if (isDebug) {
         s_log.exiting("Utils", "getDnForJavaPrincipal " + returnString);
      }

      return returnString;
   }

   /**
    * 
    * getPrincipalById helper method to get a Principal object by id
    * @param p_id
    * @return
    */
   public static Principal getPrincipalById(String p_id) {
      Principal returnPrincipal = null;
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("Utils", "getPrincipalById " + p_id);
      }
      returnPrincipal = Utils.getUserById(p_id);
      if (returnPrincipal == null) {
         returnPrincipal = Utils.getGroupByDistinguishedName(p_id);
      }

      if (isDebug) {
         s_log.exiting("Utils", "getPrincipalById returning " + returnPrincipal.toString());
      }

      return returnPrincipal;
   }

   /**
    * 
    * getFolderId helper method to get folder by name in the library thats been passed in 
    * @param ws
    * @param libraryName
    * @param folderName
    * @return
    */
   public static DocumentId getFolderId(Workspace ws, String libraryName, String folderName) {
      DocumentId returnId = null;
      boolean isDebug = s_log.isLoggable(Level.FINEST);

      if (isDebug) {
         s_log.entering("Utils", "getFolderId " + folderName);
      }
      DocumentIdIterator folders = ws.findByName(DocumentTypes.Folder, folderName);
      DocumentLibrary currentLib = ws.getCurrentDocumentLibrary();
      DocumentLibrary lib = ws.getDocumentLibrary(libraryName);
      ws.setCurrentDocumentLibrary(lib);

      if (folders.hasNext()) {
         returnId = (DocumentId) folders.next();
      }
      // if it doesnt, create the folder
      else {
         DocumentId tempId = null;
         Folder theFolder;
         try {
            Folder componentFolder = lib.getPresetFolder(PresetFolderType.COMPONENT);
            theFolder = ws.createFolder(componentFolder);
            theFolder.setName(folderName);
            ws.save(theFolder);
            returnId = theFolder.getId();
         }
         catch (DocumentCreationException e) {
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
         catch (DocumentRetrievalException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST)) {
               s_log.log(Level.FINEST, "", e);
            }
         }
         catch (DocumentSaveException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST)) {
               s_log.log(Level.FINEST, "", e);
            }
         }
         catch (DuplicateChildException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST)) {
               s_log.log(Level.FINEST, "", e);
            }
         }
      }

      if (isDebug) {
         s_log.exiting("Utils", "getFolderId returning " + returnId);
      }

      ws.setCurrentDocumentLibrary(currentLib);
      return returnId;
   }

   public static Date addDays(Date date, int days) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);
      cal.add(Calendar.DATE, days); //minus number would decrement the days
      return cal.getTime();
   }

   /**
    * 
    * getPreviewURL helper method to generate preview URL
    * @param theDoc
    * @return
    */
   public static String getPreviewURL(Document theDoc) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      String previewURL = "";
      if (isDebug) {
         s_log.entering("Utils", "getPreviewURL " + theDoc.getName());
      }
      StringBuilder sb = new StringBuilder();

      String host = "";
      String componentName = "EmailActionHost_";
      String contextRoot = "/wps/myportal";
      String path = "";
      Workspace ws = getSystemWorkspace();
      DocumentLibrary currentLib = ws.getCurrentDocumentLibrary();
      try {
         // get the system properties
         ws.setCurrentDocumentLibrary(ws.getDocumentLibrary("PruPolicyDesign"));
         Properties p = System.getProperties();
         String key = "com.pru.AppServerEnv";
         // will be DEV/QA/STAGE/PROD
         String value = (String) p.get(key);
         componentName = componentName + value;
         if (isDebug) {
            s_log.log(Level.FINEST, "componentName = " + componentName);
         }
         LibraryShortTextComponent stlc = null;
         DocumentIdIterator hostValueIterator = ws.findByName(DocumentTypes.LibraryShortTextComponent, componentName);
         if (hostValueIterator.hasNext()) {

            DocumentId tempId = (DocumentId) hostValueIterator.next();
            stlc = (LibraryShortTextComponent) ws.getById(tempId);
            if (stlc != null) {
               host = stlc.getText();
               if (isDebug) {
                  s_log.log(Level.FINEST, "host from component " + host);
               }
               else {
                  if (isDebug) {
                     s_log.log(Level.FINEST, "Component not found, leave blank");
                  }
               }
            }

         }
         else {
            if (isDebug) {
               s_log.log(Level.FINEST, "Component not found, leave blank");
            }
         }

         path = ws.getPathById(theDoc.getId(), true, true);
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
      catch (AuthorizationException e) {
         // TODO Auto-generated catch block
         if (s_log.isLoggable(Level.FINEST)) {
            s_log.log(Level.FINEST, "", e);
         }
      }
      finally {
         if (currentLib != null) {
            ws.setCurrentDocumentLibrary(currentLib);
         }
      }
      sb.append(host);
      sb.append(contextRoot);
      sb.append("?page=com.prudential.page.PP.PolicyDetail&urile=wcm:path:" + path + "&previewopt=id&previewopt=" + theDoc.getId().getId());
      previewURL = sb.toString();
      if (isDebug) {
         s_log.exiting("Utils", "getPreviewURL " + previewURL);
      }

      return previewURL;

   }
   
   /**
    * 
    * getAuthoringURL helper method to generate URL to authoring portlet
    * @param theDoc
    * @return
    */
   public static String getAuthoringURL(Document theDoc) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      String previewURL = "";
      if (isDebug) {
         s_log.entering("Utils", "getPreviewURL " + theDoc.getName());
      }
      StringBuilder sb = new StringBuilder();

      String host = "";
      String componentName = "EmailActionHost_";
      String contextRoot = "/wps/myportal";
      String path = "";
      Workspace ws = getSystemWorkspace();
      DocumentLibrary currentLib = ws.getCurrentDocumentLibrary();
      try {
         // get the system properties
         ws.setCurrentDocumentLibrary(ws.getDocumentLibrary("PruPolicyDesign"));
         Properties p = System.getProperties();
         String key = "com.pru.AppServerEnv";
         // will be DEV/QA/STAGE/PROD
         String value = (String) p.get(key);
         componentName = componentName + value;
         if (isDebug) {
            s_log.log(Level.FINEST, "componentName = " + componentName);
         }
         LibraryShortTextComponent stlc = null;
         DocumentIdIterator hostValueIterator = ws.findByName(DocumentTypes.LibraryShortTextComponent, componentName);
         if (hostValueIterator.hasNext()) {

            DocumentId tempId = (DocumentId) hostValueIterator.next();
            stlc = (LibraryShortTextComponent) ws.getById(tempId);
            if (stlc != null) {
               host = stlc.getText();
               if (isDebug) {
                  s_log.log(Level.FINEST, "host from component " + host);
               }
               else {
                  if (isDebug) {
                     s_log.log(Level.FINEST, "Component not found, leave blank");
                  }
               }
            }

         }
         else {
            if (isDebug) {
               s_log.log(Level.FINEST, "Component not found, leave blank");
            }
         }

         path = ws.getPathById(theDoc.getId(), true, true);
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
      catch (AuthorizationException e) {
         // TODO Auto-generated catch block
         if (s_log.isLoggable(Level.FINEST)) {
            s_log.log(Level.FINEST, "", e);
         }
      }
      finally {
         if (currentLib != null) {
            ws.setCurrentDocumentLibrary(currentLib);
         }
      }
      sb.append(host);
      sb.append(contextRoot);
      sb.append("/wcmAuthoring?wcmAuthoringAction=read&docid="+theDoc.getId());
      //sb.append("?page=com.prudential.page.PP.PolicyDetail&urile=wcm:path:" + path + "&previewopt=id&previewopt=" + theDoc.getId().getId());
      previewURL = sb.toString();
      if (isDebug) {
         s_log.exiting("Utils", "getPreviewURL " + previewURL);
      }

      return previewURL;

   }
   
   public static Content setGeneralDateOne(Content theContent, Date theDate) {
      
      if(theContent.isWorkflowed()) {
         try {
            theContent.setGeneralDateOne(theDate);
         }
         catch (OperationFailedException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST))
            {
               s_log.log(Level.FINEST, "", e);
               e.printStackTrace();
            }
         }
      }
      return theContent;
   }
   
   public static Content setContentDateField(Content theContent, String componentName, Date theDate ) {
      if(theContent.hasComponent(componentName)) {
         try {
            DateComponent dc = (DateComponent) theContent.getComponent(componentName);
            dc.setDate(theDate);
            theContent.setComponent(componentName, dc);
         }
         catch (ComponentNotFoundException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST))
            {
               s_log.log(Level.FINEST, "", e);
               e.printStackTrace();
            }
         }
         catch (OperationFailedException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST))
            {
               s_log.log(Level.FINEST, "", e);
               e.printStackTrace();
            }
         }
         catch (IllegalTypeChangeException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST))
            {
               s_log.log(Level.FINEST, "", e);
               e.printStackTrace();
            }
         }
         
      }
      return theContent;
   }

}
