/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.authoring.launchpage;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.exceptions.QueryServiceException;
import com.ibm.workplace.wcm.api.query.Query;
import com.ibm.workplace.wcm.api.query.QueryService;
import com.ibm.workplace.wcm.api.query.ResultIterator;
import com.prudential.utils.Utils;

public class CustomAuthoringItemRetriever {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(CustomAuthoringItemRetriever.class.getName());
   
   public static List<CustomAuthoringItemWrapper> getAuthoringItemsList(Query theQuery) {
      ArrayList<CustomAuthoringItemWrapper> returnList = new ArrayList();
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      
      if (isDebug) {
         s_log.entering("CustomAuthoringItemRetriever", "getAuthoringItemsList");
      }
      
      // get a workspace for the current user
      Workspace ws = Utils.getWorkspace();
      
      // fire the query
      QueryService qs = ws.getQueryService();
      try {
         ResultIterator results = qs.execute(theQuery);
         while(results.hasNext()) {
            
         }
      }
      catch (QueryServiceException e) {
         // TODO Auto-generated catch block
         if (s_log.isLoggable(Level.FINEST))
         {
            s_log.log(Level.FINEST, "", e);
         }
      }
      
      // iterate the results and build the wrappers around
      
      
      
      if (isDebug) {
         s_log.exiting("CustomAuthoringItemRetriever", "getAuthoringItemsList return "+returnList);
      }
      
     
      
      return returnList;
   }  
}

