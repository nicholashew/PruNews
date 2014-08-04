/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.vp;

import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
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
import com.ibm.workplace.wcm.api.LibraryDateComponent;
import com.ibm.workplace.wcm.api.ShortTextComponent;
import com.ibm.workplace.wcm.api.SiteArea;
import com.ibm.workplace.wcm.api.TextComponent;
import com.ibm.workplace.wcm.api.VirtualPortalScopedAction;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.exceptions.AuthorizationException;
import com.ibm.workplace.wcm.api.exceptions.DocumentCreationException;
import com.ibm.workplace.wcm.api.exceptions.DocumentRetrievalException;
import com.ibm.workplace.wcm.api.exceptions.DocumentSaveException;
import com.ibm.workplace.wcm.api.exceptions.DuplicateChildException;
import com.ibm.workplace.wcm.api.exceptions.IllegalDocumentTypeException;
import com.ibm.workplace.wcm.api.exceptions.OperationFailedException;
import com.ibm.workplace.wcm.api.exceptions.WCMException;
import com.prudential.tasks.EmailReminderTask;
import com.prudential.utils.Utils;

public class RetrieveReminderTaskAction implements VirtualPortalScopedAction {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(RetrieveReminderTaskAction.class.getName());

   public static String p_lastRunComponent = "ReminderLastRun";

   public void run_old() {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);

      if (isDebug) {
         s_log.entering("RetrieveReminderTaskAction", "run");
      }

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
         String[] wfStageIds = {"4aeb32b4-4565-44e3-bdef-94293fc162bb", "55e26c4d-9bef-466d-994f-8695aa173c9f"};
         DocumentId[] stageIds = new DocumentId[wfStageIds.length];
         for (int x = 0; x < wfStageIds.length; x++) {
            try {
               DocumentId tempId = ws.createDocumentId(wfStageIds[x]);
               stageIds[x] = tempId;
            }
            catch (Exception e) {
               if (isDebug) {
                  s_log.log(Level.FINEST, "Exception " + e.getMessage());
                  e.printStackTrace();
               }
            }
         }
         //Selector wfSelector = Selectors.authoringTemplateIn(arg0);
         try {
            // now set the lib to the content lib
            ws.setCurrentDocumentLibrary(ws.getDocumentLibrary("PruPolicyContent"));
            DocumentIdIterator contentIterator = ws.findContentByWorkflowStage(stageIds);
            if (contentIterator == null) {
               throw new Exception("Content by wf stage is empty");
            }
            while (contentIterator.hasNext()) {
               DocumentId tempContentId = (DocumentId) contentIterator.next();
               if (isDebug) {
                  s_log.log(Level.FINEST, "tempContentId=" + tempContentId);
               }
               Content theContent = (Content) ws.getById(tempContentId);
               DateComponent dc = null;
               boolean contentHadComponent = false;
               if (theContent.hasComponent(p_lastRunComponent)) {
                  if (isDebug) {
                     s_log.log(Level.FINEST, "content had component " + p_lastRunComponent);
                  }
                  dc = (DateComponent) theContent.getComponent(p_lastRunComponent);
                  contentHadComponent = true;
               }

               else {
                  try {
                     dc = theContent.createComponent(p_lastRunComponent, DocumentTypes.DateComponent);
                     dc.setDate(new Date());
                     if (isDebug) {
                        s_log.log(Level.FINEST, "content did Not have component, created");
                     }
                  }
                  catch (DocumentCreationException e) {
                     // TODO Auto-generated catch block
                     if (s_log.isLoggable(Level.FINEST)) {
                        s_log.log(Level.FINEST, "", e);
                     }
                  }
               }

               // now we have the ldc, get the date from it.  This will be the last time it was run
               Date theDate = dc.getDate();
               Date now = new Date();
               boolean shouldSend = false;
               // if its null, send and update the date. 
               if (theDate == null) {
                  shouldSend = true;
               }
               else if (now.after(theDate)) {
                  shouldSend = true;
               }
               else {
                  if (isDebug) {
                     s_log.log(Level.FINEST, "not sending email for doc " + tempContentId);
                  }
               }
               if (shouldSend) {
                  if (isDebug) {
                     s_log.log(Level.FINEST, "sending email for doc " + tempContentId);
                  }
                  try {
                     // have to get the delay.  How?

                     int delay = 3;
                     String wfStageName = theContent.getWorkflowStageId().getName();
                     // ApproveReadyReminderDelay is for approve, ReviewReadyReminderDelay is for review
                     String componentName = "ApproveReadyReminderDelay";
                     String messageComponentName = "ApproverMessage";
                     if (wfStageName.equalsIgnoreCase("pp-review-wfs")) {
                        componentName = "ReviewReadyReminderDelay";
                        messageComponentName = "ReviewerMessage";
                     }
                     if (theContent.hasComponent(componentName)) {
                        try {
                           ShortTextComponent stc = (ShortTextComponent) theContent.getComponentByReference(componentName);
                           delay = Integer.parseInt(stc.getText());
                        }

                        catch (Exception e) {
                           if (isDebug) {
                              s_log.log(Level.FINEST, "Exception " + e.getMessage());
                              e.printStackTrace();
                           }
                        }
                     }
                     else {
                        if (isDebug) {
                           s_log.log(Level.FINEST, "Content did not have component " + componentName);
                        }
                     }
                     Date updatedDate = new Date();
                     updatedDate = Utils.addDays(updatedDate, delay);
                     // now get the content to get the field
                     dc.setDate(updatedDate);
                     if (theContent.hasComponent(p_lastRunComponent)) {
                        theContent.setComponent(p_lastRunComponent, dc);
                     }
                     else {
                        theContent.addComponent(p_lastRunComponent, dc);
                     }
                     try {
                        ws.save(theContent);
                     }
                     catch (DocumentSaveException e) {
                        // TODO Auto-generated catch block
                        if (s_log.isLoggable(Level.FINEST)) {
                           s_log.log(Level.FINEST, "", e);
                        }
                        // try one more
                        ws.login();
                        try {
                           ws.save(theContent);
                        }
                        catch (DocumentSaveException e2) {
                           // TODO Auto-generated catch block
                           if (s_log.isLoggable(Level.FINEST)) {
                              s_log.log(Level.FINEST, "", e2.getMessage());
                           }
                        }
                     }
                     // now send the reminder email;
                     /* create and schedule the EmailReminderTask */
                     String defaultMessage = "";
                     DocumentId parentID = theContent.getDirectParent();
                     SiteArea parent = (SiteArea) ws.getById(parentID);
                     if (parent.hasComponent(messageComponentName)) {
                        TextComponent tc = (TextComponent) parent.getComponentByReference(messageComponentName);
                        defaultMessage = tc.getText();
                     }
                     // get the component from the parent site area
                     EmailReminderTask thisTask = new EmailReminderTask();
                     thisTask.setUuid(tempContentId.getId());
                     thisTask.setDefaultMessage(defaultMessage);
                     Timer timer = new Timer("EMAILREMINDERS");
                     timer.schedule(thisTask, new Date());
                  }
                  catch (OperationFailedException e) {
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

            }
         }
         catch (IllegalDocumentTypeException e) {
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
         catch (AuthorizationException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST)) {
               s_log.log(Level.FINEST, "", e);
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
      if (isDebug) {
         s_log.exiting("RetrieveReminderTaskAction", "run");
      }

   }

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
               String[] wfStageIds = {"4aeb32b4-4565-44e3-bdef-94293fc162bb", "55e26c4d-9bef-466d-994f-8695aa173c9f"};
               DocumentId[] stageIds = new DocumentId[wfStageIds.length];
               for (int x = 0; x < wfStageIds.length; x++) {
                  try {
                     DocumentId tempId = ws.createDocumentId(wfStageIds[x]);
                     stageIds[x] = tempId;
                  }
                  catch (Exception e) {
                     if (isDebug) {
                        s_log.log(Level.FINEST, "Exception " + e.getMessage());
                        e.printStackTrace();
                     }
                  }
               }
               //Selector wfSelector = Selectors.authoringTemplateIn(arg0);
               try {
                  // now set the lib to the content lib
                  ws.setCurrentDocumentLibrary(ws.getDocumentLibrary("PruPolicyContent"));
                  DocumentIdIterator contentIterator = ws.findContentByWorkflowStage(stageIds);
                  if (contentIterator == null) {
                     throw new Exception("Content by wf stage is empty");
                  }
                  while (contentIterator.hasNext()) {
                     DocumentId tempContentId = (DocumentId) contentIterator.next();
                     if (isDebug) {
                        s_log.log(Level.FINEST, "tempContentId=" + tempContentId);
                     }
                     Content theContent = (Content) ws.getById(tempContentId);
                     DateComponent dc = null;
                     boolean contentHadComponent = false;
                     if (theContent.hasComponent(p_lastRunComponent)) {
                        if (isDebug) {
                           s_log.log(Level.FINEST, "content had component " + p_lastRunComponent);
                        }
                        dc = (DateComponent) theContent.getComponent(p_lastRunComponent);
                        contentHadComponent = true;
                     }

                     else {
                        try {
                           dc = theContent.createComponent(p_lastRunComponent, DocumentTypes.DateComponent);
                           dc.setDate(new Date());
                           if (isDebug) {
                              s_log.log(Level.FINEST, "content did Not have component, created");
                           }
                        }
                        catch (DocumentCreationException e) {
                           // TODO Auto-generated catch block
                           if (s_log.isLoggable(Level.FINEST)) {
                              s_log.log(Level.FINEST, "", e);
                           }
                        }
                     }

                     // now we have the ldc, get the date from it.  This will be the last time it was run
                     Date theDate = dc.getDate();
                     Date now = new Date();
                     boolean shouldSend = false;
                     // if its null, send and update the date. 
                     if (theDate == null) {
                        shouldSend = true;
                     }
                     else if (now.after(theDate)) {
                        shouldSend = true;
                     }
                     else {
                        if (isDebug) {
                           s_log.log(Level.FINEST, "not sending email for doc " + tempContentId);
                        }
                     }
                     if (shouldSend) {
                        if (isDebug) {
                           s_log.log(Level.FINEST, "sending email for doc " + tempContentId);
                        }
                        try {
                           // have to get the delay.  How?

                           int delay = 3;
                           String wfStageName = theContent.getWorkflowStageId().getName();
                           // ApproveReadyReminderDelay is for approve, ReviewReadyReminderDelay is for review
                           String componentName = "ApproveReadyReminderDelay";
                           String messageComponentName = "ApproverMessage";
                           if (wfStageName.equalsIgnoreCase("pp-review-wfs")) {
                              componentName = "ReviewReadyReminderDelay";
                              messageComponentName = "ReviewerMessage";
                           }
                           if (theContent.hasComponent(componentName)) {
                              try {
                                 ShortTextComponent stc = (ShortTextComponent) theContent.getComponentByReference(componentName);
                                 delay = Integer.parseInt(stc.getText());
                              }

                              catch (Exception e) {
                                 if (isDebug) {
                                    s_log.log(Level.FINEST, "Exception " + e.getMessage());
                                    e.printStackTrace();
                                 }
                              }
                           }
                           else {
                              if (isDebug) {
                                 s_log.log(Level.FINEST, "Content did not have component " + componentName);
                              }
                           }
                           Date updatedDate = new Date();
                           updatedDate = Utils.addDays(updatedDate, delay);
                           // now get the content to get the field
                           dc.setDate(updatedDate);
                           if (theContent.hasComponent(p_lastRunComponent)) {
                              theContent.setComponent(p_lastRunComponent, dc);
                           }
                           else {
                              theContent.addComponent(p_lastRunComponent, dc);
                           }
                           try {
                              ws.save(theContent);
                           }
                           catch (DocumentSaveException e) {
                              // TODO Auto-generated catch block
                              if (s_log.isLoggable(Level.FINEST)) {
                                 s_log.log(Level.FINEST, "", e);
                              }
                              // try one more
                              ws.login();
                              try {
                                 ws.save(theContent);
                              }
                              catch (DocumentSaveException e2) {
                                 // TODO Auto-generated catch block
                                 if (s_log.isLoggable(Level.FINEST)) {
                                    s_log.log(Level.FINEST, "", e2.getMessage());
                                 }
                              }
                           }
                           // now send the reminder email;
                           /* create and schedule the EmailReminderTask */
                           String defaultMessage = "";
                           DocumentId parentID = theContent.getDirectParent();
                           SiteArea parent = (SiteArea) ws.getById(parentID);
                           if (parent.hasComponent(messageComponentName)) {
                              TextComponent tc = (TextComponent) parent.getComponentByReference(messageComponentName);
                              defaultMessage = tc.getText();
                           }
                           // get the component from the parent site area
                           EmailReminderTask thisTask = new EmailReminderTask();
                           thisTask.setUuid(tempContentId.getId());
                           thisTask.setDefaultMessage(defaultMessage);
                           Timer timer = new Timer("EMAILREMINDERS");
                           timer.schedule(thisTask, new Date());
                        }
                        catch (OperationFailedException e) {
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

                  }
               }
               catch (IllegalDocumentTypeException e) {
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
               catch (AuthorizationException e) {
                  // TODO Auto-generated catch block
                  if (s_log.isLoggable(Level.FINEST)) {
                     s_log.log(Level.FINEST, "", e);
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
}
