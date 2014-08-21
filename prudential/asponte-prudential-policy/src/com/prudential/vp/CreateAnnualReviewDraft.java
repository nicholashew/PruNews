/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.vp;
import java.util.Calendar;
import java.util.Date;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.DocumentId;
import com.ibm.workplace.wcm.api.ShortTextComponent;
import com.ibm.workplace.wcm.api.VirtualPortalScopedAction;
import com.ibm.workplace.wcm.api.Workspace;
import com.prudential.utils.Utils;
import com.prudential.wf.actions.CreateDraftPolicy;
public class CreateAnnualReviewDraft implements VirtualPortalScopedAction {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(CreateAnnualReviewDraft.class.getName());

   private String p_uuid = null;
   
   private static String s_dayField = "LastRevisedDate";
   private static String s_reviewDelay = "ReviewDateDelay";
   
   
   public CreateAnnualReviewDraft(String uuid) {
      p_uuid = uuid;
   }
   @Override
   public void run() {      
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      Workspace ws = Utils.getSystemWorkspace();
      try {
         if(p_uuid == null) {
            throw new Exception("uuid is null, cannot create draft");
         }
         DocumentId tempId = ws.createDocumentId(p_uuid);
         if(tempId == null) {
            throw new Exception("tempId is null, cannot create draft");
         }
         Content theContent = (Content)ws.getById(tempId);
         if(theContent == null) {
            throw new Exception("theContent is null, cannot create draft");
         }
         
         if (isDebug) {
            s_log.log(Level.FINEST, "theContent "+theContent.getName()+" is having a draft created via the annual review process");
         }
         
         // before creating draft, try updating the content
         // if it fails, we won't create draft and it will get picked up next run
         theContent = Utils.setContentDateField(theContent, s_dayField, new Date());
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
         
         // now, reset gen date one on the Published content
      } catch (Exception e) {
         
      }
      finally {
         if(ws != null) {
            ws.logout();
         }
      }            
      
   }
}

