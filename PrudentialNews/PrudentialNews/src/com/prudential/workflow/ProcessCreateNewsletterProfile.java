/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/


package com.prudential.workflow;

import java.net.URL;
import java.security.Principal;
import java.util.Date;

import com.ibm.workplace.wcm.api.*;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowAction;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowActionResult;
import com.ibm.workplace.wcm.api.custom.Directive;
import com.ibm.workplace.wcm.api.custom.Directives;
import com.ibm.workplace.wcm.api.query.Query;
import com.ibm.workplace.wcm.api.query.QueryService;
import com.ibm.workplace.wcm.api.query.ResultIterator;
import com.ibm.workplace.wcm.api.query.Selector;
import com.ibm.workplace.wcm.api.query.Selectors;
import com.prudential.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.util.*;

import javax.activation.DataHandler;
import javax.activation.URLDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.x500.X500Principal;

public class ProcessCreateNewsletterProfile implements CustomWorkflowAction
{
   private static final Logger s_log = Logger.getLogger(ProcessCreateNewsletterProfile.class.getName());

   @Override
   /**
    * Method to create the distribution list if necessary and
    * populate the link in the content to it.
    * High level:
    * Retrieve the content from the workflow check the link field
    * if it's empty, populate it with a link to the distribution list with the same name
    * if That item doesn't exist, create it and then populate the link to it
    */
   public CustomWorkflowActionResult execute(Document document)
   {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if(isDebug)
      {
         s_log.entering("ProcessCreateNewsletterProfile", "execute called for document "+document);
      }
      WebContentCustomWorkflowService webContentCustomWorkflowService = null;
      Workspace ws = null;
      CustomWorkflowActionResult result = null;
      Directive directive = Directives.CONTINUE;
      String message = "Successfully processed link to distribution list";
      boolean successful = true;
      
      try
      {
         InitialContext ctx = new InitialContext();
         WebContentService webContentService = (WebContentService) ctx.lookup("portal:service/wcm/WebContentService");
         ws = webContentService.getRepository().getSystemWorkspace();
         // Retrieve Custom Workflow Service
         webContentCustomWorkflowService = (WebContentCustomWorkflowService) ctx
            .lookup("portal:service/wcm/WebContentCustomWorkflowService");
         result = webContentCustomWorkflowService.createResult(directive, message);
        
         
         // get the ID of the distribution list
         DocumentId distListId = this.getDistributionListId(ws, document.getName());
         Content theContent = (Content)document;
         if(distListId != null)
         {
            // get the contentlink object and populate with the distribution list
            LinkComponent clc = null;
            
            if(theContent.hasComponent("Distribution List"))
            {
               clc = (LinkComponent)theContent.getComponent("Distribution List");               
               clc.setDocumentReference(distListId);
               theContent.setComponent("Distribution List", clc);
               if(isDebug)
               {
                  s_log.log(Level.FINEST, "Successfully set link for "+theContent.getName());
               }
            }
            
            // set the owners of this newsletter profile into the distribution list
            Content distListContent = (Content)ws.getById(distListId);
            boolean usingDn = ws.isDistinguishedNamesUsed();
            ws.useDistinguishedNames(true);
            String[] theOwners = theContent.getOwners();
            if(distListContent.hasComponent("Users")) {
               UserSelectionComponent usc = (UserSelectionComponent)distListContent.getComponent("Users");
               // get Principal array based on the array of owners
               //Utils.getPrincipalById(p_id)
               ArrayList principals = new ArrayList();
               for(int x=0;x<theOwners.length;x++) {
                  if (isDebug) {
                     s_log.log(Level.FINEST, "adding user "+theOwners[x]);
                  }
                  X500Principal tempPrincipal = new X500Principal(theOwners[x]);
                  principals.add(tempPrincipal);
               }
               Principal[] principalArray = (Principal[])principals.toArray(new Principal[0]);
               usc.setSelections(principalArray);
               distListContent.setComponent("Users",usc);
               String[] errors = ws.save(distListContent);
               if(errors.length>0 && isDebug) {
                  for(int y=0;y<errors.length;y++) {
                     if (isDebug) {
                        s_log.log(Level.FINEST, errors[y]);
                     }
                  }                 
               }
            }
            ws.useDistinguishedNames(usingDn);
         }
         else
         {
            // don't hold back creating the content just log
            if(isDebug)
            {
               s_log.log(Level.FINEST, "Could not retrieve distribution list will not process link");
            }
         }
         
         // now, create a site area in /PrudentialNewsletterDrafts/Newsletters/NewsletterProfiles to hold draft profiles if its not there         
         DocumentId profileSiteAreaId = Utils.getSiteAreaIdByName(ws, theContent.getName(), "PrudentialNewsletterDrafts");
         if(profileSiteAreaId == null)
         {
            if(isDebug)
            {
               s_log.log(Level.FINEST, "about to create site area /PrudentialNewsletterDrafts/Newsletters/NewsletterProfilesDraftNewsletters/"+theContent.getName());
            }
            // get the NewsletterProfiles site area
            DocumentId profileParentSiteAreaId = Utils.getSiteAreaIdByName(ws, "NewsletterProfilesDraftNewsletters", "PrudentialNewsletterDrafts");
            if(profileParentSiteAreaId == null)
            {
               // don't fail state but the site area should have been retrieved
               throw new Exception("Could not retrieve NewsletterProfilesDraftNewsletters site area");
            }            
            profileSiteAreaId = Utils.createSiteArea(ws,profileParentSiteAreaId,theContent.getName(),"PrudentialNewsletterDrafts");
            if(isDebug)
            {
               s_log.log(Level.FINEST, "after create "+profileSiteAreaId);
            }
         }
      }
      catch (Exception e)
      {
         if(isDebug)
         {
            s_log.log(Level.FINEST, "exception occured "+e);
            e.printStackTrace();
         }
           
      }
         
      if(isDebug)
      {
         s_log.exiting("ProcessCreateNewsletterProfile", "execute returning "+result);
      }
      return result;
   }

   @Override
   public Date getExecuteDate(Document p_arg0)
   {
      // TODO Auto-generated method stub
       return new Date();
   }
   
   /**
    * 
    * getDistributionListId helper method to get the distribution list 
    * @param ws
    * @param name
    * @return
    */
   private DocumentId getDistributionListId(Workspace ws, String name)
   {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      DocumentId returnId = null;
      String distListName = name+" Distribution List";
      if (isDebug)
      {
         s_log.entering("ProcessCreateNewsletterProfile", "getDistributionListId for name "+name);
      }
      
      try
      {

         DocumentId contentId = Utils.getContentIdByName(ws, distListName, "PrudentialNewsContent");
         if (contentId != null)
         {           
            returnId = contentId;
            if(isDebug)
            {
               s_log.log(Level.FINEST,"content found "+returnId);
            }
         }
         else
         {
            if(isDebug)
            {
               s_log.log(Level.FINEST,"Content not found, create it");
            }
            
            // have to retrieve the authoring template id
            //DocumentId authTemplateId = this.
            DocumentId authTemplateId = Utils.getAuthoringTemplateIdByName(ws, "AT - Distribution List","PrudentialNewsDesign");
            // retrieve the parent site area id
            DocumentId parentId = Utils.getSiteAreaIdByName(ws, "DistributionLists","PrudentialNewsContent");
            if(authTemplateId == null || parentId == null)
            {
               throw new Exception ("AuthoringTemplate or SiteArea could not be found to get distribution list");
            }
            returnId = Utils.createContent(ws, authTemplateId, parentId, distListName, "PrudentialNewsContent");            
         }
      }
      catch (Exception e)
      {
         if(isDebug)
         {
            s_log.log(Level.FINEST, "exception "+e);
            e.printStackTrace();
         }
      }     
      if (isDebug)
      {
         s_log.exiting("ProcessCreateNewsletterProfile", "getDistributionListId returing " + returnId);
      }
      return returnId;
   }

}