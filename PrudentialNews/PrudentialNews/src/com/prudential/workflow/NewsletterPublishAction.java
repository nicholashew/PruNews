/********************************************************************/
/* Asponte
 /* cmknight
 /********************************************************************/

package com.prudential.workflow;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ibm.workplace.wcm.api.*;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowAction;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowActionResult;
import com.ibm.workplace.wcm.api.custom.Directive;
import com.ibm.workplace.wcm.api.custom.Directives;
import com.ibm.workplace.wcm.api.exceptions.AuthorizationException;
import com.ibm.workplace.wcm.api.exceptions.DocumentCreationException;
import com.ibm.workplace.wcm.api.exceptions.DocumentSaveException;
import com.ibm.workplace.wcm.api.exceptions.DuplicateChildException;
import com.ibm.workplace.wcm.api.exceptions.IllegalDocumentTypeException;
import com.ibm.workplace.wcm.api.exceptions.VirtualPortalNotFoundException;
import com.ibm.workplace.wcm.api.exceptions.WCMException;
import com.ibm.workplace.wcm.api.query.Query;
import com.ibm.workplace.wcm.api.query.QueryService;
import com.ibm.workplace.wcm.api.query.ResultIterator;
import com.ibm.workplace.wcm.api.query.Selector;
import com.ibm.workplace.wcm.api.query.Selectors;
import com.prudential.tasks.LinkNewsToNewsletters;
import com.prudential.tasks.MoveNewsContentTask;
import com.prudential.tasks.MoveNewsletterTask;
import com.prudential.utils.Utils;
import com.prudential.vpactions.CreateOrRetrieveSiteAreaScopedAction;
import com.prudential.vpactions.MoveOrLinkToSiteAreaScopedAction;
import com.prudential.vpactions.ProcessNewNewsContentScopedAction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
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

/**
 * The NewsletterPublishAction will be invoked when a newsletter is published to be
 * sent out.  When it is, it will be pushed over the the "live" library
 */
public class NewsletterPublishAction implements CustomWorkflowAction {
   private static final Logger s_log = Logger.getLogger(NewsletterPublishAction.class.getName());

   @Override
   /**
    * method to update any newsletters to alert that new content has been created
    * may have to create the draft newsletter
    * also have to find/create the site area to place the content under
    */
   public CustomWorkflowActionResult execute(Document document) {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("NewsletterPublishAction", "execute called for document " + document);
      }
      
      WebContentCustomWorkflowService webContentCustomWorkflowService = null;
      Workspace ws = null;
      CustomWorkflowActionResult result = null;
      Directive directive = Directives.CONTINUE;
      String message = "Successfully processed newsletter";
      boolean successful = true;

      try {
         InitialContext ctx = new InitialContext();
         WebContentService webContentService = (WebContentService) ctx.lookup("portal:service/wcm/WebContentService");
         //ws = webContentService.getRepository().getSystemWorkspace();
         ws = Utils.getSystemWorkspace();
         // Retrieve Custom Workflow Service
         webContentCustomWorkflowService = (WebContentCustomWorkflowService) ctx
            .lookup("portal:service/wcm/WebContentCustomWorkflowService");
         result = webContentCustomWorkflowService.createResult(directive, message);

         Content theContent = (Content) document;

         Timer timer = new Timer();
         // create a date object
         LinkComponent clc = null;
         String distListHolder = "No Profile";
         if(theContent.hasComponent("Newsletter Profile"))
         {
            clc = (LinkComponent)theContent.getComponent("Newsletter Profile");               
            DocumentId newsletterProfileId = clc.getDocumentReference();
            if(newsletterProfileId != null) {
               distListHolder = newsletterProfileId.getName();
            }
            if(isDebug)
            {
               s_log.log(Level.FINEST, "Successfully set link for "+theContent.getName());
            }
         }
         MoveNewsletterTask moveNewsletterTask = new MoveNewsletterTask(theContent.getId().getId(), distListHolder, "PrudentialNewsContent");
         Calendar calendar = Calendar.getInstance();
         calendar.add(Calendar.SECOND, 15);

         Date theDate = calendar.getTime();
         // run immediately
         timer.schedule(moveNewsletterTask, theDate);

      }
      catch (Exception e) {
         if (isDebug) {
            e.printStackTrace();
         }
         message = "Exception in NewsletterPublishAction";
         directive = Directives.ROLLBACK_DOCUMENT;
         // Create a result object
         result = webContentCustomWorkflowService.createResult(directive, message);
         successful = false;
      }

      if (isDebug) {
         s_log.exiting("NewsletterPublishAction", "execute successful = " + successful, directive + ": " + message);
      }
      return result;
   }

   @Override
   public Date getExecuteDate(Document p_arg0) {
      // TODO Auto-generated method stub
      return new Date();
   }

}