/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.referencecount;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.aptrix.pluto.event.AddedEvent;
import com.ibm.portal.ListModel;
import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.DocumentId;
import com.ibm.workplace.wcm.api.DocumentType;
import com.ibm.workplace.wcm.api.DocumentTypes;
import com.ibm.workplace.wcm.api.Reference;
import com.ibm.workplace.wcm.api.RenderingContext;
import com.ibm.workplace.wcm.api.TemplatedDocument;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.exceptions.AuthorizationException;
import com.ibm.workplace.wcm.api.exceptions.DocumentIdCreationException;
import com.ibm.workplace.wcm.api.exceptions.DocumentRetrievalException;
import com.ibm.workplace.wcm.api.exceptions.OperationFailedException;
import com.ibm.workplace.wcm.api.plugin.rendering.RenderingPlugin;
import com.ibm.workplace.wcm.api.plugin.rendering.RenderingPluginException;
import com.ibm.workplace.wcm.api.plugin.rendering.RenderingPluginModel;
import com.prudential.*;
import com.prudential.utils.Utils;

public class RenderReferenceCount implements RenderingPlugin {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(RenderReferenceCount.class.getName());

   private static String m_title = "RenderReferenceCount";

   public RenderReferenceCount() {
      // TODO Auto-generated constructor stub

   }

   @Override
   public boolean isShownInAuthoringUI() {
      // TODO Auto-generated method stub
      return true;

   }

   @Override
   public String getDescription(Locale p_arg0) {
      // TODO Auto-generated method stub
      return m_title;

   }

   @Override
   public ListModel<Locale> getLocales() {
      // TODO Auto-generated method stub
      return null;

   }

   @Override
   public String getTitle(Locale p_arg0) {
      // TODO Auto-generated method stub
      return m_title;

   }

   @Override
   public String getName() {
      // TODO Auto-generated method stub
      return m_title;
   }

   @Override
   public boolean render(RenderingPluginModel rpm) throws RenderingPluginException {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      boolean successful = false;
      String uuid = null;
      int count = 0;
      if (isDebug) {
         s_log.entering("RenderHitCount", "render");
      }
      StringBuilder jsonString = new StringBuilder();
      List<String> list;
      Map<String, List<String>> params = rpm.getPluginParameters();
      RenderingContext renderingContext = rpm.getRenderingContext();
      boolean displayCount = false;
      list = params.get("displayCount");
      if (list != null && list.size() > 0) {
         String displayCountString = list.get(0);
         displayCount = Boolean.parseBoolean(displayCountString);
         if (isDebug) {
            s_log.log(Level.FINEST, "displayCount passed = "+displayCountString);
         }
      }
      
      list = params.get("uuid");
      if (list != null && list.size() > 0) {
          uuid = list.get(0);
          if (isDebug) {
              s_log.log(Level.FINEST, "render" + " uuid = " + uuid);
          }
          
      }


      String outputString = "";
      Writer out = rpm.getWriter();
      Workspace ws = renderingContext.getContent().getSourceWorkspace();
      RenderingContext rc = rpm.getRenderingContext();

      TemplatedDocument theResult = rc.getRenderedItem();
      
      if(uuid != null && uuid.length()>0) {
         DocumentId tempId = null;
         
         try {
            tempId = ws.createDocumentId(uuid);
            theResult = (TemplatedDocument)ws.getById(tempId);
         }
         catch (DocumentRetrievalException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST))
            {
               s_log.log(Level.FINEST, "", e);
            }
         }
         catch (AuthorizationException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST))
            {
               s_log.log(Level.FINEST, "", e);
            }
         }
         catch (DocumentIdCreationException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST))
            {
               s_log.log(Level.FINEST, "", e);
            }
         }
      }

      if (theResult != null) {
         // get the count or the json
         if (displayCount) {
            outputString = getCountString(theResult);
         }
         else {
            outputString = getJSONString(theResult);
         }
         try {
            out.write(outputString);
         }
         catch (IOException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST)) {
               s_log.log(Level.FINEST, "", e);
            }
         }
      }
      else {
         if (isDebug) {
            s_log.log(Level.FINEST, "no result, cannot generate string");
         }
      }

      if (isDebug) {
         s_log.exiting("RenderHitCount", "render " + outputString);
      }
      return successful;

   }

   /** 
    * 
    * getJSONString build the references
    * @param theResult
    * @return
    */
   private String getJSONString(TemplatedDocument theResult) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      StringBuilder returnString = new StringBuilder();
      int count = 0;
      if (isDebug) {
         s_log.entering("RenderReferenceCount", "getJSONString");
      }

      Workspace ws = Utils.getSystemWorkspace();
      try {
         Reference[] refs = ws.getReferences(theResult.getId());
         for (int x = 0; x < refs.length; x++) {
            Reference tempRef = refs[x];
            // only count content
            DocumentType dt = tempRef.getRefererDocumentId().getType();
            if (dt.isOfType(DocumentTypes.Content)) {
               count++;
               returnString.append("{\"uuid\": \"" + tempRef.getRefererDocumentId() + "\"}");
               if (isDebug) {
                  s_log.log(Level.FINEST, "Reference: refereeDocId " + tempRef.getRefereeDocumentId());
                  s_log.log(Level.FINEST, "Reference: refererDocId " + tempRef.getRefererDocumentId());
               }
               if (x < refs.length - 1) {
                  returnString.append(",");
               }
            }
         }
         returnString.insert(0, "[{\"uuid\": \"" + theResult.getId().getId() + "\",\"count\":\"" + count + "\",\"referrers\": [");
         // if it ends in , take it off
         if (returnString.toString().endsWith(",")) {
            returnString.deleteCharAt(returnString.lastIndexOf(","));
         }

         returnString.append("]}]");

      }
      catch (OperationFailedException e) {
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
      if (isDebug) {
         s_log.exiting("RenderReferenceCount", "getJSONString returning " + returnString.toString());
      }

      return returnString.toString();

   }

   private String getCountString(TemplatedDocument theResult) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      String returnString = "";
      int count = 0;
      if (isDebug) {
         s_log.entering("RenderReferenceCount", "getCountString");
      }

      Workspace ws = Utils.getSystemWorkspace();
      try {
         Reference[] refs = ws.getReferences(theResult.getId());
         for (int x = 0; x < refs.length; x++) {
            Reference tempRef = refs[x];
            // only count content
            DocumentType dt = tempRef.getRefererDocumentId().getType();
            if (dt.isOfType(DocumentTypes.Content)) {
               count++;
               if (isDebug) {
                  s_log.log(Level.FINEST, "Reference: refereeDocId " + tempRef.getRefereeDocumentId());
                  s_log.log(Level.FINEST, "Reference: refererDocId " + tempRef.getRefererDocumentId());
               }

            }
         }
         returnString = "" + count;
      }
      catch (OperationFailedException e) {
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

      if (isDebug) {
         s_log.exiting("RenderReferenceCount", "getCountString returning " + returnString.toString());
      }

      return returnString.toString();

   }
}
