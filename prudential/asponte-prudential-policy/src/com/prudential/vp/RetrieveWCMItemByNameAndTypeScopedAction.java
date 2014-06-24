/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.vp;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.bsf.debug.util.DebugLog;
import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.DocumentId;
import com.ibm.workplace.wcm.api.DocumentIdIterator;
import com.ibm.workplace.wcm.api.DocumentLibrary;
import com.ibm.workplace.wcm.api.DocumentType;
import com.ibm.workplace.wcm.api.VirtualPortalScopedAction;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.exceptions.WCMException;
import com.prudential.utils.*;

public class RetrieveWCMItemByNameAndTypeScopedAction implements VirtualPortalScopedAction {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(RetrieveWCMItemByNameAndTypeScopedAction.class.getName());

   private static DocumentType theType;
   
   private Document returnedValue = null;
   
   public Document getReturnedValue() {
      return returnedValue;
   }

   public void setReturnedValue(Document p_returnedValue) {
      returnedValue = p_returnedValue;
   }

   private String theName = "";

   
   /**
    * 
    * Constructor
    * @param theName
    * @param theType
    */
   public RetrieveWCMItemByNameAndTypeScopedAction(String p_theName, DocumentType p_theType) {
      theType = p_theType;
      theName = p_theName;
   }
   
   @Override
   public void run() throws WCMException {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("RetrieveWCMItemByNameAndTypeScopedAction", "run");
      }
      Workspace ws = Utils.getSystemWorkspace();
      try {
         ws.login();
         DocumentLibrary docLib = ws.getDocumentLibrary("");
         DocumentIdIterator<Document> resultIt = ws.findByName(theType, theName);
         Document theResult = null;
         if(resultIt.hasNext()) {
            DocumentId resultId = (DocumentId)resultIt.next();
            if (isDebug) {
               s_log.log(Level.FINEST, "resultId from findByName "+resultId);
            }
            theResult = (Document)ws.getById(resultId);            
         }
         
      }
      catch (Exception e) {
         if (isDebug) {
            s_log.log(Level.FINEST, "exception "+e.getMessage());
            e.printStackTrace();
         }
      }
      finally {
         if(ws != null) {
            ws.logout();
         }
      }
      
      if (isDebug) {
         s_log.exiting("RetrieveWCMItemByNameAndTypeScopedAction", "run");
      }
      

   }
  
}

