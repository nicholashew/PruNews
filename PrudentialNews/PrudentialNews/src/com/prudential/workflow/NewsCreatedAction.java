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
 * The NewsCreatedAction will be invoked whenever a news article is created
 * there are some actions that need to be done.  First of all, the content will be moved to the
 * correct underlying site area based currently on the date.  Secondly, all of the newsletters that need to will be updated
 * to reflect the new news item will get a link to the news created under it.
 * Since content has to be moved, have to do this as a task instead of as a workflow action
 */
public class NewsCreatedAction implements CustomWorkflowAction {
   private static final Logger s_log = Logger.getLogger(NewsCreatedAction.class.getName());

   private static String NEWSBYDATEPARENT = "NewsByDate";
   
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
         s_log.entering("NewsCreatedAction", "execute called for document " + document);
      }
      
      WebContentCustomWorkflowService webContentCustomWorkflowService = null;
      Workspace ws = null;
      CustomWorkflowActionResult result = null;
      Directive directive = Directives.CONTINUE;
      String message = "Successfully processed news";
      boolean successful = true;

      try {
         InitialContext ctx = new InitialContext();
         WebContentService webContentService = (WebContentService) ctx.lookup("portal:service/wcm/WebContentService");
         ws = webContentService.getRepository().getSystemWorkspace();
         // Retrieve Custom Workflow Service
         webContentCustomWorkflowService = (WebContentCustomWorkflowService) ctx
            .lookup("portal:service/wcm/WebContentCustomWorkflowService");
         result = webContentCustomWorkflowService.createResult(directive, message);

         // we want to get a site area that corresponds to today's date
         // then move the content from the placeholder site area where it's created and move it to 
         // the updated site area, creating if necessary
         SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
         String siteAreaName = formatter.format(new Date());
         Content theContent = (Content) document;

         // CreateOrRetrieveSiteAreaScopedAction
         Timer timer = new Timer();
         // create a date object
         MoveNewsContentTask moveContentTask = new MoveNewsContentTask(theContent.getId().getId(), siteAreaName, "PrudentialNewsContent");
         Calendar calendar = Calendar.getInstance();
         calendar.add(Calendar.SECOND, 15);

         Date theDate = calendar.getTime();
         // run immediately
         timer.schedule(moveContentTask, theDate);
         // if it was null, create it         
         
         // after the move, have to retrieve any newsletter profiles to see which newsletters need to be updated.
         // this will be done by retrieving all newsletter profiles that match the current content's categories (one at a time)
         // then check to see if that newsletter profile has a draft newsletter.  If it doesn't create one
         
         // create and execute the scoped action
         timer = new Timer();
         // create a date object
         LinkNewsToNewsletters linkContentTask = new LinkNewsToNewsletters(theContent.getId().getId());
         calendar = Calendar.getInstance();
         calendar.add(Calendar.SECOND, 10);

         theDate = calendar.getTime();
         // run immediately
         timer.schedule(linkContentTask, theDate);

      }
      catch (Exception e) {
         if (isDebug) {
            e.printStackTrace();
         }
         message = "Exception in NewsCreatedAction";
         directive = Directives.ROLLBACK_DOCUMENT;
         // Create a result object
         result = webContentCustomWorkflowService.createResult(directive, message);
         successful = false;
      }

      if (isDebug) {
         s_log.exiting("NewsCreatedAction", "execute successful = " + successful, directive + ": " + message);
      }
      return result;
   }

   @Override
   public Date getExecuteDate(Document p_arg0) {
      // TODO Auto-generated method stub
      return new Date();
   }

}