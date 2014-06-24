/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.vp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.workplace.wcm.api.ChildPosition;
import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.ContentLink;
import com.ibm.workplace.wcm.api.DocumentId;
import com.ibm.workplace.wcm.api.DocumentIdIterator;
import com.ibm.workplace.wcm.api.DocumentLibrary;
import com.ibm.workplace.wcm.api.DocumentTypes;
import com.ibm.workplace.wcm.api.SiteArea;
import com.ibm.workplace.wcm.api.TextComponent;
import com.ibm.workplace.wcm.api.VirtualPortalScopedAction;
import com.ibm.workplace.wcm.api.WCM_API;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.exceptions.WCMException;
import com.prudential.utils.PrudentialMLUtils;
import com.prudential.utils.Utils;

public class HandleDelete implements VirtualPortalScopedAction {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(HandleDelete.class.getName());

   private String p_uuids = "";

   private boolean returnedValue = false;

   public HandleDelete(String uuidString) {
      p_uuids = uuidString;
   }

   /**
    * 
    * @see com.ibm.workplace.wcm.api.VirtualPortalScopedAction#run()
    */
   @Override
   public void run() throws WCMException {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);

      if (isDebug) {
         s_log.entering("HandleDelete", "run");
      }

      boolean success = true;

      String[] uuids = p_uuids.split(",");
      ArrayList<DocumentId<?>> docIdList = new ArrayList<DocumentId<?>>();
      Workspace thisWorkspace = Utils.getSystemWorkspace();
      if (isDebug) {
         s_log.log(Level.FINEST, "UUIDs received:" + p_uuids);
      }

      DocumentId<?> tempDocId = null;

      for (int x = 0; x < uuids.length; x++) {
         tempDocId = thisWorkspace.createDocumentId(uuids[x]);
         if (isDebug) {
            s_log.log(Level.FINEST, "Value: " + uuids[x]);
            s_log.log(Level.FINEST, "Retrieved docId = " + tempDocId);
         }
         docIdList.add(tempDocId);
      }

      // now process for the library that was passed in
      success = processDelete(docIdList, thisWorkspace);
      setReturnedValue(success);

      if (isDebug) {
         s_log.exiting("HandleDelete", "run success = "+success);
      }

   }

   /**
    * helper method to process the move
    * 
    * @param docIdList
    *            the document id's in order
    */
   public static boolean processDelete(ArrayList<DocumentId<?>> docIdList, Workspace thisWorkspace) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);

      boolean success = false;

      if (isDebug) {
         s_log.entering("ProcessLinkReorder", "processMove" + docIdList);
      }
      try {
         // now, since they are passed first, second, third, etc
         // just place them last under the top level site area. That way, the
         // first will be last, then second be last which moved first up
         // and so on
         DocumentId<?> deleteId = null;
         Iterator<DocumentId<?>> idIterator = docIdList.iterator();
         while (idIterator.hasNext()) {
            deleteId = (DocumentId<?>) idIterator.next();

            // now add the SA to the end
            // catch exceptions so we can process the reorder on the rest
            try {
               if (isDebug) {
                  s_log.log(Level.FINEST, "About to delete = " + deleteId);
               }
               String[] errors = thisWorkspace.delete(deleteId);
               if (errors.length > 0 && isDebug) {
                  if (isDebug) {
                     s_log.log(Level.FINEST, "delete errors not empty");
                     for(int x=0;x<errors.length;x++) {
                        s_log.log(Level.FINEST, "delete errors contain "+errors[x]);
                     }
                  }
               } else {
                  success = true;
               }
            }
            catch (Exception e) {
               if (isDebug) {
                  s_log.log(Level.FINEST, "Error occurred " + e.getMessage());
                  e.printStackTrace();
               }
            }

         }
      }
      catch (Exception e) {
         if (isDebug) {
            s_log.log(Level.FINEST, "Error occurred " + e.getMessage());
            e.printStackTrace();
         }
      }

      if (isDebug) {
         s_log.exiting("HandleDelete", "processDelete " + success);
      }
      return success;
   }

   /**
    * getter method to return the created site area document id
    * @return
    */
   public boolean getReturnedValue() {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.log(Level.FINEST, "returning " + returnedValue);
      }
      return returnedValue;
   }

   public void setReturnedValue(boolean p_returnedValue) {
      returnedValue = p_returnedValue;
   }

}
