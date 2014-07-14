/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.workflow;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.DocumentId;
import com.ibm.workplace.wcm.api.TextComponent;
import com.ibm.workplace.wcm.api.WebContentCustomWorkflowService;
import com.ibm.workplace.wcm.api.WebContentService;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowAction;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowActionResult;
import com.ibm.workplace.wcm.api.custom.Directive;
import com.ibm.workplace.wcm.api.custom.Directives;
import com.ibm.workplace.wcm.api.exceptions.OperationFailedException;
import com.ibm.workplace.wcm.api.exceptions.ServiceNotAvailableException;
import com.prudential.commons.wcm.authoring.ObjectWrapper;
import com.prudential.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

public class PopulateCategories implements CustomWorkflowAction {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(PopulateCategories.class.getName());

   private static String s_selectedField = "SelectedCats";

   private static String s_suggestedField = "SuggestedCats";

   @Override
   public CustomWorkflowActionResult execute(Document p_theDoc) {
      // TODO Auto-generated method stub

      boolean isDebug = s_log.isLoggable(Level.FINEST);

      WebContentCustomWorkflowService webContentCustomWorkflowService = null;
      Workspace ws = null;
      CustomWorkflowActionResult result = null;
      Directive directive = Directives.CONTINUE;
      String message = "Successfully added categories";
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
         
         // get the suggested cats
         Content theContent = (Content)p_theDoc;
         Gson gson = new Gson();
         Type type = new TypeToken<List<ObjectWrapper>>() { }.getType();
         ArrayList catIds = new ArrayList<DocumentId>();
         
         if(theContent.hasComponent(s_selectedField)) {
            if (isDebug) {
               s_log.log(Level.FINEST, "content has "+s_selectedField);
            }
            TextComponent selectedCmpnt = (TextComponent)theContent.getComponentByReference(s_selectedField);
            String selectedJSON = (String)selectedCmpnt.getText();
            if(selectedJSON == null || selectedJSON.length() < 1) {
               selectedJSON = "[]";
            }
            if (isDebug) {
               s_log.log(Level.FINEST, "selectedJSON = "+selectedJSON);
            }
            List<ObjectWrapper> itemList = gson.fromJson(selectedJSON, type);
            for (ObjectWrapper itemWrapper : itemList) {               
               DocumentId docId = ws.createDocumentId(itemWrapper.getId());
               if (isDebug) {
                  s_log.log(Level.FINEST, "adding "+docId);
               }
               catIds.add(docId);
            }
         }
         
         if(theContent.hasComponent(s_suggestedField)) {
            if (isDebug) {
               s_log.log(Level.FINEST, "content has "+s_suggestedField);
            }
            TextComponent suggestedCmpnt = (TextComponent)theContent.getComponentByReference(s_suggestedField);
            String suggestedJSON = (String)suggestedCmpnt.getText();
            if(suggestedJSON == null || suggestedJSON.length() < 1) {
               suggestedJSON = "[]";
            }
            if (isDebug) {
               s_log.log(Level.FINEST, "selectedJSON = "+suggestedJSON);
            }
            List<ObjectWrapper> itemList = gson.fromJson(suggestedJSON, type);
            for (ObjectWrapper itemWrapper : itemList) {               
               DocumentId docId = ws.createDocumentId(itemWrapper.getId());
               if (isDebug) {
                  s_log.log(Level.FINEST, "adding "+docId);
               }
               catIds.add(docId);
            }
         }
         if (isDebug) {
            s_log.log(Level.FINEST, "adding categories to the content");
         }
         DocumentId[] catIdArray = (DocumentId[]) catIds.toArray(new DocumentId[0]);
         theContent.addCategoryIds(catIdArray);
         
      }
      catch (Exception e) {
         // TODO Auto-generated catch block
         if (s_log.isLoggable(Level.FINEST)) {
            s_log.log(Level.FINEST, "", e);
            e.printStackTrace();
         }
         message = "Exception in PopulateCategories";
         directive = Directives.ROLLBACK_DOCUMENT;
         // Create a result object
         result = webContentCustomWorkflowService.createResult(directive, message);
         successful = false;
      }      
      finally {
         if(ws != null) {
            ws.logout();  
         }         
      }
      
      if (isDebug) {
         s_log.exiting("PopulateCategories", "execute returning "+result+" successful "+successful);
      }
      
      return result;
   }

   @Override
   public Date getExecuteDate(Document p_arg0) {
      // TODO Auto-generated method stub
      return new Date();
   }

}
