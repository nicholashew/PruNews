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
import com.ibm.workplace.wcm.api.DocumentIdIterator;
import com.ibm.workplace.wcm.api.DocumentLibrary;
import com.ibm.workplace.wcm.api.HTMLComponent;
import com.ibm.workplace.wcm.api.LinkComponent;
import com.ibm.workplace.wcm.api.SiteArea;
import com.ibm.workplace.wcm.api.VirtualPortalScopedAction;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.exceptions.WCMException;
import com.prudential.utils.Utils;

public class ProcessNewNewsContentScopedAction implements VirtualPortalScopedAction {

   private static Logger s_log = Logger.getLogger(ProcessNewNewsContentScopedAction.class.getName());

   private String theContentuuid = null;

   public ProcessNewNewsContentScopedAction(String s_theContentuuid) {
      theContentuuid = s_theContentuuid;
   }

   @Override
   public void run() throws WCMException {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("ProcessNewNewsContentScopedAction", "run theContentuuid = " + theContentuuid);
      }

      // get system workspace, login, then process the move
      // try different workspace
      Workspace ws = Utils.getSystemWorkspace();      
      if (ws != null && theContentuuid != null) {
         try {
            // login
            ws.login();
            // have to wrap this all in a scoped action
            // retrieve the content based on the uuid
            DocumentId contentId = ws.createDocumentId(theContentuuid);
            Content theContent = (Content)ws.getById(contentId);
            DocumentId[] categoryIds = theContent.getCombinedCategoryIds();
            DocumentLibrary currentDocLib = ws.getCurrentDocumentLibrary();
            ws.setCurrentDocumentLibrary(ws.getDocumentLibrary("PrudentialNewsletterContent"));
            DocumentId authTemplateId = Utils.getAuthoringTemplateIdByName(ws, "AT - Newsletter Profile", "PrudentialNewsDesign");
            if (authTemplateId == null) {
               throw new Exception("Could not retrieve AT - Newsletter Profile");
            }
            // get the site area for Active newsletter profiles
            String activeUUID = "cb0552c0-d981-4e52-b495-36d9295f182a";
            DocumentId activeDocId = ws.createDocumentId(activeUUID);
            if (activeDocId == null) {
               if (isDebug) {
                  s_log.log(Level.FINEST, "unable to get "+activeUUID+" which should be the active site area for newsletter profiles");
               }
               throw new Exception("Could not retrieve Active newsletter profile site area");
            }
            DocumentId[] siteUUIDs = {activeDocId};
            for (int x = 0; x < categoryIds.length; x++) {

               DocumentId currentCatId = categoryIds[x];
               DocumentId[] catIds = {currentCatId};
               if (isDebug) {
                  s_log.log(Level.FINEST, "checking for newsletter profiles matching category " + currentCatId.getName());
               }
               // find any content in the PrudentialNewsletterDrafts that match the cat
               DocumentIdIterator draftNewsletterProfiles = ws.contentSearch(authTemplateId, siteUUIDs, catIds, null);

               while (draftNewsletterProfiles.hasNext()) {
                  DocumentId currentDraftID = (DocumentId) draftNewsletterProfiles.next();
                  Content currentDraftNewsletterProfile = (Content) ws.getById(currentDraftID);
                  // get the cats to push to the newsletter
                  DocumentId[] newsletterProfileCategories  = currentDraftNewsletterProfile.getCombinedCategoryIds();

                  if (isDebug) {
                     s_log.log(Level.FINEST, "currentDraftContent found " + currentDraftNewsletterProfile.getName());
                     s_log.log(Level.FINEST, "currentDraftContent categories include:");
                     for(int y=0;y<newsletterProfileCategories.length;y++) {
                        s_log.log(Level.FINEST, newsletterProfileCategories[y].toString());
                     }
                  }

                  // now check for all the content under this profiles site area in the draft 
                  // we do this by getting the site area that matches the content's name in the draft 
                  // area
                  DocumentId draftsSiteAreaId = Utils.getSiteAreaIdByName(ws, currentDraftNewsletterProfile.getName(),
                     "PrudentialNewsletterDrafts");
                  // if this doesn't exist, skip for now but the custom workflow action should have created it.
                  if (draftsSiteAreaId != null) {
                     // now check for child.  If one doesn't exist, means that there's no draft newsletter content yet have to create it.  If it does 
                     // exist, then grab it and update the html
                     SiteArea newsletterProfileSiteArea = (SiteArea) ws.getById(draftsSiteAreaId);
                     // get the children, should only have one
                     DocumentId contentToUpdateId = null;
                     DocumentIdIterator children = newsletterProfileSiteArea.getChildren();
                     boolean contentCreated = false;
                     if (!children.hasNext()) {
                        // get the authoring template for Newsletters
                        authTemplateId = Utils.getAuthoringTemplateIdByName(ws, "AT - Newsletter", "PrudentialNewsDesign");
                        // create the content
                        contentToUpdateId = Utils.createContent(ws, authTemplateId, draftsSiteAreaId,currentDraftNewsletterProfile.getName() + " - newsletter", "PrudentialNewsletterDrafts",newsletterProfileCategories);
                        contentCreated = true;
                        
                        // have to link the profile to the newsletter
                        Content newsletterContent = (Content)ws.getById(contentToUpdateId);
                        if(newsletterContent.hasComponent("Newsletter Profile")) {
                           if (isDebug) {
                              s_log.log(Level.FINEST, "Setting the newsletter link to the profile to documentid "+currentDraftID);
                           }
                           LinkComponent lc = (LinkComponent)newsletterContent.getComponent("Newsletter Profile");
                           lc.setDocumentReference(currentDraftID);
                           newsletterContent.setComponent("Newsletter Profile", lc);
                           String[] errors = ws.save(newsletterContent);
                           if(errors.length<0 && isDebug) {
                              for(int y=0;y<errors.length;y++) {
                                 if (isDebug) {
                                    s_log.log(Level.FINEST, "error during save "+errors[y]);
                                 }
                              }
                           }
                        }
                     }
                     else {
                        // have to ensure the child is a content object
                        while (children.hasNext()) {
                           DocumentId tempId = (DocumentId) children.next();
                           if (tempId.getName().equals(currentDraftNewsletterProfile.getName() + " - newsletter")) {
                              contentToUpdateId = tempId;
                              break;
                           }
                        }
                     }

                     DocumentId siteAreaId = Utils.getSiteAreaIdByNameAndParent(ws, "NewsLinks", draftsSiteAreaId,
                        "PrudentialNewsletterDrafts");
                     if (siteAreaId == null) {
                        // create it
                        siteAreaId = Utils.createSiteArea(ws, draftsSiteAreaId, "NewsLinks", "PrudentialNewsletterDrafts");
                     }
                     SiteArea theNewsSiteArea = (SiteArea)ws.getById(siteAreaId);
                     //ContentLink theLink = ws.createContentLink(arg0, arg1, arg2, arg3)
                     // check if ws is logged in
                     ws.login();
                     ContentLink createdLink = ws.createContentLink(theContent.getId(), siteAreaId, null, ChildPosition.END);
                     // now, get the content and update it.
                     /*
                     if (contentToUpdateId != null) {
                        Content theContentToUpdate = (Content) ws.getById(contentToUpdateId);
                        // retrieve the IncludedNews which is HTML component that holds uuid; values
                        HTMLComponent includeNews = (HTMLComponent) theContentToUpdate.getComponent("IncludedNews");
                        String currentHTML = includeNews.getHTML();
                        StringBuffer sb = new StringBuffer();
                        sb.append(currentHTML);
                        sb.append(theContent.getId().getId() + ";");
                        includeNews.setHTML(sb.toString());
                        theContentToUpdate.setComponent("IncludedNews", includeNews);

                        if (isDebug) {
                           s_log.log(Level.FINEST, "currentDraftContent setting html for IncludedNews to " + sb.toString());
                        }

                        // have to set the distribution lists as well
                        // get the content from the currentDraftNewsletterProfile link
                        // only do this if it was new content
                        if (currentDraftNewsletterProfile.hasComponent("Distribution List") && contentCreated) {
                           LinkComponent clc = (LinkComponent) currentDraftNewsletterProfile.getComponent("Distribution List");
                           DocumentId linkedContent = clc.getDocumentReference();
                           if (linkedContent != null) {
                              if (theContentToUpdate.hasComponent("Distribution Lists")) {
                                 HTMLComponent distList = (HTMLComponent) theContentToUpdate.getComponent("Distribution Lists");
                                 StringBuffer distListSB = new StringBuffer();
                                 distListSB.append(distList.getHTML());
                                 distListSB.append(linkedContent.getName() + ";");
                                 distList.setHTML(distListSB.toString());
                                 theContentToUpdate.setComponent("Distribution Lists", distList);
                                 if (isDebug) {
                                    s_log.log(Level.FINEST,
                                       "currentDraftContent setting html for Distribution Lists to " + distListSB.toString());
                                 }
                              }
                           }
                        }

                        String errors[] = ws.save(theContentToUpdate);
                        if (isDebug) {
                           for (int index = 0; index < errors.length; index++) {
                              s_log.log(Level.FINEST, "error saving content " + theContentToUpdate.getName() + " " + errors[index]);
                           }
                        }
                        
                     }
                     */
                  }
                  else {
                     if (isDebug) {
                        s_log.log(Level.FINEST, "draftsSiteAreaId was null for " + currentDraftNewsletterProfile.getName());
                     }
                  }

               }
            }

         }
         catch (Exception e) {
            if (isDebug) {
               s_log.log(Level.FINEST, "Exception e = "+e.getMessage());
               e.printStackTrace();
            }
              
         }
         finally {
            if(ws != null) {
               ws.logout();
            }
         }
      }

      if (isDebug) {
         s_log.exiting("ProcessNewNewsContentScopedAction", "run theContentuuid = " + theContentuuid);
      }

   }

}
