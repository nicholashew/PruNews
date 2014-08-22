/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.vp;

import java.security.Principal;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.portal.um.Group;
import com.ibm.portal.um.User;
import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.ContentComponent;
import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.DocumentId;
import com.ibm.workplace.wcm.api.Hierarchical;
import com.ibm.workplace.wcm.api.UserSelectionComponent;
import com.ibm.workplace.wcm.api.VirtualPortalScopedAction;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.exceptions.AuthorizationException;
import com.ibm.workplace.wcm.api.exceptions.PropertyRetrievalException;
import com.ibm.workplace.wcm.api.exceptions.WCMException;
import com.ibm.workplace.wcm.api.security.Access;
import com.prudential.utils.Utils;
import com.prudential.wcm.WCMUtils;

public class ApplyManagersAsOwnersScopedAction implements VirtualPortalScopedAction {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(ApplyManagersAsOwnersScopedAction.class.getName());

   private static Document result = null;

   private static String s_uuid;
   

   public static String getUuid() {
      return s_uuid;
   }

   public static void setUuid(String p_uuid) {
      s_uuid = p_uuid;
   }

   public static Document getResult() {
      return result;
   }

   public static void setResult(Document p_result) {
      result = p_result;
   }

   public ApplyManagersAsOwnersScopedAction(String p_uuid) {
      s_uuid = p_uuid;
   }

   @Override
   public void run() throws WCMException {
      // TODO Auto-generated method stub

      PrivilegedExceptionAction runAction = new PrivilegedExceptionAction() {
         @Override
         public Object run() throws Exception {
            boolean isDebug = s_log.isLoggable(Level.FINEST);
           
            try {
               // get system workspace, login, then process the move
               // try different workspace
               Workspace ws = Utils.getSystemWorkspace();
               if (ws != null && s_uuid != null) {
                  DocumentId contentId = ws.createDocumentId(s_uuid);
                  if (contentId == null) {
                     throw new Exception("Could not create document id");
                  }
                  else {
                     if (isDebug) {
                        s_log.log(Level.FINEST, "contentId " + contentId);
                     }
                  }

                  Content theResult = (Content) ws.getById(contentId);
                  // drop the managers in
                  String[] managersContent = theResult.getMembersForInheritedAccess(Access.MANAGER);
                  if(managersContent != null && managersContent.length > 0) {
                     if (isDebug) {
                       s_log.log(Level.FINEST, "found content managers, using to populate the authors");
                       for(int y=0;y<managersContent.length;y++) {
                          s_log.log(Level.FINEST, "managers included "+managersContent[y]);
                       }
                       
                    }
                     theResult.addOwners(managersContent);
                     if (isDebug) {
                        s_log.log(Level.FINEST, "Owners field set to: {0}", Arrays.toString(managersContent));
                     }
                     try {
                        if(ws.isLocked(theResult.getId())) {
                           ws.unlock(theResult.getId());
                        }
                        String[] errors = ws.save(theResult);
                        if(errors.length > 0) {
                           if (isDebug) {
                              s_log.log(Level.FINEST, "errors "+errors.length);
                           }
                           for(int x=0;x<errors.length;x++) {
                              s_log.log(Level.FINEST, "errors contain "+errors[x]);
                           }
                        }
                        else {
                           if (isDebug) {
                              s_log.log(Level.FINEST, "Content saved");
                           }
                        }
                     }
                     catch (Exception e) {
                        if (isDebug) {
                           s_log.log(Level.FINEST, "Exception "+e.getMessage());
                           e.printStackTrace();
                        }
                     }
                  }
                  else {
                     DocumentId parId = ((Hierarchical)theResult).getParentId();
                      boolean foundManagers = false;
                      while (parId != null && !foundManagers) {
                          Document parent = ws.getById(parId);
                          String[] managers = parent.getMembersForAccess(Access.MANAGER);
                          String[] inheritManagers = parent.getInheritedManagerAccessMembers();
                          if (managers != null && managers.length > 0) {
                             for(int y=0;y<managers.length;y++) {
                                s_log.log(Level.FINEST, "managers included "+managers[y]);
                             }
                              foundManagers = true;
                              theResult.addOwners(managers);
                              if (isDebug) {
                                 s_log.log(Level.FINEST, "Owners field set to: {0}", Arrays.toString(managers));
                              }
                              try {
                                 if(ws.isLocked(theResult.getId())) {
                                    ws.unlock(theResult.getId());
                                 }
                                 String[] errors = ws.save(theResult);
                                 if(errors.length > 0) {
                                    if (isDebug) {
                                       s_log.log(Level.FINEST, "errors "+errors.length);
                                    }
                                    for(int x=0;x<errors.length;x++) {
                                       s_log.log(Level.FINEST, "errors contain "+errors[x]);
                                    }
                                 }
                                 else {
                                    if (isDebug) {
                                       s_log.log(Level.FINEST, "Content saved");
                                    }
                                 }
                              }
                              catch (Exception e) {
                                 if (isDebug) {
                                    s_log.log(Level.FINEST, "Exception "+e.getMessage());
                                    e.printStackTrace();
                                 }
                              }
                          }
                          else if (inheritManagers != null && inheritManagers.length > 0) {
                             for(int y=0;y<inheritManagers.length;y++) {
                                s_log.log(Level.FINEST, "managers included "+managers[y]);
                             }
                              foundManagers = true;
                              theResult.addOwners(inheritManagers);
                              if (isDebug) {
                                 s_log.log(Level.FINEST, "Owners field set to: {0}", Arrays.toString(inheritManagers));
                              }
                              try {
                                 if(ws.isLocked(theResult.getId())) {
                                    ws.unlock(theResult.getId());
                                 }
                                 String[] errors = ws.save(theResult);
                                 if(errors.length > 0) {
                                    if (isDebug) {
                                       s_log.log(Level.FINEST, "errors "+errors.length);
                                    }
                                    for(int x=0;x<errors.length;x++) {
                                       s_log.log(Level.FINEST, "errors contain "+errors[x]);
                                    }
                                 }
                                 else {
                                    if (isDebug) {
                                       s_log.log(Level.FINEST, "Content saved");
                                    }
                                 }
                              }
                              catch (Exception e) {
                                 if (isDebug) {
                                    s_log.log(Level.FINEST, "Exception "+e.getMessage());
                                    e.printStackTrace();
                                 }
                              }
                          }
                          else {
                              parId = ((Hierarchical)parent).getParentId();
                          }
                      }
                  }
               }
            }
            catch (Exception e) {
               if (isDebug) {
                  s_log.log(Level.FINEST, "Exception " + e.getMessage());
                  e.printStackTrace();
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
