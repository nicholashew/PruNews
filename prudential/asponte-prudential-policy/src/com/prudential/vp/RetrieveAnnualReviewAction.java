/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.vp;

import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.portal.um.exceptions.PumaAttributeException;
import com.ibm.portal.um.exceptions.PumaMissingAccessRightsException;
import com.ibm.portal.um.exceptions.PumaModelException;
import com.ibm.portal.um.exceptions.PumaSystemException;
import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.DateComponent;
import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.DocumentId;
import com.ibm.workplace.wcm.api.DocumentIdIterator;
import com.ibm.workplace.wcm.api.DocumentLibrary;
import com.ibm.workplace.wcm.api.DocumentType;
import com.ibm.workplace.wcm.api.DocumentTypes;
import com.ibm.workplace.wcm.api.Library;
import com.ibm.workplace.wcm.api.LibraryDateComponent;
import com.ibm.workplace.wcm.api.Repository;
import com.ibm.workplace.wcm.api.ShortTextComponent;
import com.ibm.workplace.wcm.api.SiteArea;
import com.ibm.workplace.wcm.api.TextComponent;
import com.ibm.workplace.wcm.api.VirtualPortalContext;
import com.ibm.workplace.wcm.api.VirtualPortalScopedAction;
import com.ibm.workplace.wcm.api.WCM_API;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.exceptions.AuthorizationException;
import com.ibm.workplace.wcm.api.exceptions.DocumentCreationException;
import com.ibm.workplace.wcm.api.exceptions.DocumentRetrievalException;
import com.ibm.workplace.wcm.api.exceptions.DocumentSaveException;
import com.ibm.workplace.wcm.api.exceptions.DuplicateChildException;
import com.ibm.workplace.wcm.api.exceptions.IllegalDocumentTypeException;
import com.ibm.workplace.wcm.api.exceptions.OperationFailedException;
import com.ibm.workplace.wcm.api.exceptions.WCMException;
import com.ibm.workplace.wcm.api.query.Query;
import com.ibm.workplace.wcm.api.query.QueryService;
import com.ibm.workplace.wcm.api.query.ResultIterator;
import com.ibm.workplace.wcm.api.query.Selector;
import com.ibm.workplace.wcm.api.query.Selectors;
import com.ibm.workplace.wcm.api.query.WorkflowSelectors;
import com.ibm.workplace.wcm.api.query.WorkflowSelectors.Status;
import com.prudential.tasks.EmailReminderTask;
import com.prudential.utils.Utils;

public class RetrieveAnnualReviewAction implements VirtualPortalScopedAction {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(RetrieveAnnualReviewAction.class.getName());

   public static String p_lastRunComponent = "ReminderLastRun";
   private static String s_dayField = "LastRevisedDate";
   private static String s_reviewDelay = "ReviewDateDelay";

   // try wrapping in priv exception action
   public void run() {
      boolean isDebug = s_log.isLoggable(Level.FINEST);

      PrivilegedExceptionAction runAction = new PrivilegedExceptionAction() {

         @Override
         public Object run() throws Exception {
            // TODO Auto-generated method stub
            boolean isDebug = s_log.isLoggable(Level.FINEST);
            // pull all content that's in the reminder workflow stages
            Workspace ws = Utils.getSystemWorkspace();
            DocumentLibrary oldLib = null;
            try {
               ws.login();

               // get the documentid of the folder
               DocumentId folderId = null;
               oldLib = ws.getCurrentDocumentLibrary();
               DocumentLibrary designLib = ws.getDocumentLibrary("PruPolicyDesign");
               ws.setCurrentDocumentLibrary(designLib);

               DocumentLibrary[] libs = {designLib};
               // use WCM API queries to find content in specific workflow stages

               try {
                  String wfStageIds = "dbaa9d23-fd56-4015-b121-c0fce5bd3b0a";
                  DocumentId stageId = ws.createDocumentId(wfStageIds);
                  // now set the lib to the content lib
                  DocumentLibrary theLib = ws.getDocumentLibrary("PruPolicyContent");
                  ArrayList<Library> libraryList = new ArrayList<Library>();
                  libraryList.add(theLib);
                  ws.setCurrentDocumentLibrary(theLib);
                  QueryService qs = ws.getQueryService();
                  Query query = qs.createQuery();
                  Selector librarySelector = Selectors.libraryIn(libraryList);
                  // get in specific stage
                  //Selector stageSelector = Selectors.
                  Selector genDateOneSelector = WorkflowSelectors.generalDateOneBefore(new Date(), false);
                  Selector wfStageSelector = WorkflowSelectors.stageEquals(stageId);
                  Selector publishedSelector = WorkflowSelectors.statusEquals(Status.PUBLISHED);
                  query.addSelector(librarySelector);
                  query.addSelector(genDateOneSelector);
                  query.addSelector(wfStageSelector);
                  query.addSelector(publishedSelector);

                  if (isDebug) {
                     s_log.log(Level.FINEST, "query is " + query);
                  }
                  Repository repo = WCM_API.getRepository();
                  ResultIterator results = qs.execute(query);
                  if (results != null) {
                     if (isDebug) {
                        s_log.log(Level.FINEST, "results " + results.getSize());
                     }
                     while (results.hasNext()) {
                        Document theContent = (Document) results.next();

                        try {
                           VirtualPortalContext vctx = repo.generateVPContextFromContextPath(Utils.getVPName());
                           CreateAnnualReviewDraft theTask = new CreateAnnualReviewDraft(theContent.getId().getId());
                           repo.executeInVP(vctx, theTask);
                        }
                        catch (Exception e) {
                           if (isDebug) {
                              s_log.log(Level.FINEST, "Exception " + e.getMessage());
                              e.printStackTrace();
                           }
                        }

                     }
                  }

               }
               catch (Exception e) {
                  if (isDebug) {
                     s_log.log(Level.FINEST, " " + e.getMessage());
                     e.printStackTrace();
                  }
               }
            }
            catch (Exception e) {
               if (isDebug) {
                  s_log.log(Level.FINEST, " " + e.getMessage());
                  e.printStackTrace();
               }
            }

            finally {
               if (ws != null) {
                  ws.logout();
               }
            }

            return null;

         }

      };

      // now run it
      try {
         Utils.getPumaHome().getEnvironment().runUnrestricted(runAction);
      }
      catch (PrivilegedActionException e) {
         // TODO Auto-generated catch block
         if (s_log.isLoggable(Level.FINEST)) {
            s_log.log(Level.FINEST, "", e);
         }
      }
   }
   
   public static Content createAnnualReviewDraft(Content theContent, Workspace ws) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      
      if(theContent.hasComponent(s_reviewDelay)) {
         ShortTextComponent days;
         try {
            days = (ShortTextComponent)theContent.getComponentByReference(s_dayField);
            String value = days.getText();
            try {
               int offset = Integer.parseInt(value);
               Date tempDate = new Date();//theContent.getEffectiveDate();
               Calendar tempCal = Calendar.getInstance();
               tempCal.setTime(tempDate);
               tempCal.add(Calendar.DATE, offset);
               tempDate = tempCal.getTime();
               theContent = Utils.setGeneralDateOne(theContent, tempDate);                  
               if (isDebug) {
                  s_log.log(Level.FINEST, "General Date One set to "+tempDate);
               }
            }
            catch (Exception e) {
               if (isDebug) {
                  s_log.log(Level.FINEST, "exception setting the date field");
                  e.printStackTrace();
               }               
            }
         }
         catch (Exception e1) {
            // TODO Auto-generated catch block
            if (isDebug) {
               s_log.log(Level.FINEST, "exception setting the date field");
               e1.printStackTrace();
            }  
         }
         
      }
      
      // now try to save
      String[] errors = ws.save(theContent);
      if(errors.length > 0) {
         if (isDebug) {
            for(int x=0;x<errors.length;x++) {
               s_log.log(Level.FINEST, "Error saving "+errors[x]);  
            }               
         }
         
         throw new Exception("Content failed save");
      }
      Content draft = (Content) theContent.createDraftDocument();
      draft.setEffectiveDate(new Date());
      // check for gendateone
      Date genOne = theContent.getGeneralDateOne();
      if (genOne != null) {
         draft.setGeneralDateTwo(genOne);
      }
      // now clear the genDateOne 
      draft.setGeneralDateOne(null);
      // Run both the Exit Actions and the Entry Actions of the next stage
      if (isDebug) {
         s_log.log(Level.FINEST, "draft documents stage is "+draft.getWorkflowStageId());
      }
      draft.nextWorkflowStage(true, true, "Moved automatically - via " + CreateAnnualReviewDraft.class.getName());
      if (isDebug) {
         s_log.log(Level.FINEST, "after next stage, draft documents stage is "+draft.getWorkflowStageId());
      }
      
      return theContent;
   }
}
