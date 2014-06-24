/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.vp;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
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
import com.ibm.workplace.wcm.api.exceptions.OperationFailedException;
import com.ibm.workplace.wcm.api.exceptions.ServiceNotAvailableException;
import com.ibm.workplace.wcm.api.exceptions.WCMException;
import com.prudential.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

public class HandleReorderJSON implements VirtualPortalScopedAction {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(HandleReorderJSON.class.getName());

   private String json_string = "";

   private static boolean returnedValue = true;

   private static Workspace m_theWorkspace;

   public HandleReorderJSON(String uuidString) {
      json_string = uuidString;
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
         s_log.entering("HandleReorderJSON", "run " + json_string);
      }

      boolean success = true;

      Workspace thisWorkspace = Utils.getSystemWorkspace();

      /*
       * String will look like
       * [
      {
        "uuid": "95205165-143c-4419-8e31-b424a8439398",
        "children": [
            {
                "uuid": "25f77195-6a92-4417-9e83-920a8c8d5682",
                "children": []
            },
            {
                "uuid": "3f808300-649c-457d-9557-0e96320b95fb",
                "children": [
                    {
                        "uuid": "ddfb9e47-4de0-44cb-9674-9797f5b931c6",
                        "children": []
                    }
                ]
            },
            {
                "uuid": "1a4e7a46-5e99-4c41-a9a9-150214722d4d"
            }
        ]
      }
      ]
       */

      // have to read the json string into gson 
      if (this.json_string == null) {
         json_string = "[]";
      }
      Gson gson = new Gson();
      Type type = new TypeToken<List<JSONMoveItemWrapper>>() {
      }.getType();
      List<JSONMoveItemWrapper> l = gson.fromJson(json_string, type);
      if (isDebug) {
         s_log.log(Level.FINEST, "l is " + l.toString());
      }

      iterateOverItems(l);

      if (isDebug) {
         s_log.exiting("HandleReorderJSON", "run");
      }

   }

   private static Workspace getWorkspace() {
      if (m_theWorkspace == null) {
         try {
            m_theWorkspace = Utils.getSystemWorkspace();
         }
         catch (Exception e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST)) {
               s_log.log(Level.FINEST, "", e);
            }
         }
      }
      return m_theWorkspace;
   }

   public static boolean processMove(String p_parentuuid, String p_childuuid) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);

      boolean success = true;

      if (isDebug) {
         s_log.entering("HandleReorderJSON", "processMove p_parentuuid = " + p_parentuuid + " , p_childuuid = " + p_childuuid);
      }
      Workspace thisWorkspace = getWorkspace();
      try {
         thisWorkspace.login();
         // now, since they are passed first, second, third, etc
         // just place them last under the top level site area. That way, the
         // first will be last, then second be last which moved first up
         // and so on
         DocumentId parentId = null;
         DocumentId childId = null;

         parentId = thisWorkspace.createDocumentId(p_parentuuid);
         childId = thisWorkspace.createDocumentId(p_childuuid);
         if (parentId != null && childId != null) {
            thisWorkspace.move(childId, parentId);
         }
         else {
            success = false;
            if (isDebug) {
               s_log.log(Level.FINEST, "parent or child could not be retrieved, returning false");
            }
         }
      }
      catch (Exception e) {
         success = false;
         if (isDebug) {
            s_log.log(Level.FINEST, "Error occurred " + e.getMessage());
            e.printStackTrace();
         }
      }
      finally {
         thisWorkspace.logout();
      }

      if (isDebug) {
         s_log.exiting("HandleReorderJSON", "processMove " + success);
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

   public static void setReturnedValue(boolean p_returnedValue) {
      returnedValue = p_returnedValue;
   }

   private static void iterateOverItems(List<JSONMoveItemWrapper> itemList) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);

      for (JSONMoveItemWrapper itemWrapper : itemList) {
         if (isDebug) {
            s_log.log(Level.FINEST, "parent uuid = " + itemWrapper.getUuid());
         }
         String parentuuid = itemWrapper.getUuid();
         if (itemWrapper.getChildren() != null && itemWrapper.getChildren().size() > 0) {

            for (int i = 0; i < itemWrapper.getChildren().size(); i++) {
               if (isDebug) {
                  s_log.log(Level.FINEST, "children contains = " + itemWrapper.getChildren().get(i).getUuid());
               }
               String childuuid = itemWrapper.getChildren().get(i).getUuid();
               if (childuuid != null && parentuuid != null) {
                  // process the move
                  boolean successful = processMove(parentuuid, childuuid);
                  if (!successful) {
                     setReturnedValue(successful);
                  }
               }
            }

            iterateOverItems(itemWrapper.getChildren());
         }
      }
   }
}
