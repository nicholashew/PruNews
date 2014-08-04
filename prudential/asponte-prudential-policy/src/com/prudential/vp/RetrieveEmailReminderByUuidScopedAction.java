/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.vp;
import java.security.Principal;
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
import com.ibm.workplace.wcm.api.UserSelectionComponent;
import com.ibm.workplace.wcm.api.VirtualPortalScopedAction;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.exceptions.AuthorizationException;
import com.ibm.workplace.wcm.api.exceptions.PropertyRetrievalException;
import com.ibm.workplace.wcm.api.exceptions.WCMException;
import com.prudential.utils.Utils;
import com.prudential.wcm.WCMUtils;
public class RetrieveEmailReminderByUuidScopedAction implements VirtualPortalScopedAction {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(RetrieveEmailReminderByUuidScopedAction.class.getName());
   
   private static Document result = null;
   private static String s_uuid;  
   private ArrayList s_recipients = null;
   public ArrayList getRecipients() {
      return s_recipients;
   }

   public void setRecipients(ArrayList p_recipients) {
      s_recipients = p_recipients;
   }

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
   
   public RetrieveEmailReminderByUuidScopedAction(String p_uuid) {
      s_uuid = p_uuid;
   }

   @Override
   public void run() throws WCMException {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);

      if (isDebug) {
         s_log.entering("RetrieveDocumentByUuidScopedAction", "enclosing_method");
      }
      try {
         // get system workspace, login, then process the move
         // try different workspace
         Workspace ws = Utils.getSystemWorkspace();      
         if (ws != null && s_uuid != null) {
            DocumentId contentId = ws.createDocumentId(s_uuid);
            if(contentId == null) {
               throw new Exception("Could not create document id");
            } else {
               if (isDebug) {
                  s_log.log(Level.FINEST, "contentId "+contentId);
               }
            }
            
            Document theResult = (Document)ws.getById(contentId);
            setResult(theResult);
            setRecipients(getRecipients(theResult));
         }
      } catch (Exception e) {
         if (isDebug) {
            s_log.log(Level.FINEST, "Exception "+e.getMessage());
            e.printStackTrace();
         }
      }
      
      if (isDebug) {
         s_log.exiting("RetrieveDocumentByUuidScopedAction", "run");
      }
      
   }
   
   /**
   *
   * in both cases it's the content approvers. Any subclass should override if necessary.
   */
  ArrayList getRecipients_old(Document doc) {
     // TODO Auto-generated method stub
     boolean isDebug = s_log.isLoggable(Level.FINEST);
     if (isDebug) {
        s_log.entering("RetrieveEmailReminderByUuidScopedAction", "getRecipients for doc "+doc.getName());
     }
     
     ArrayList recipientList = new ArrayList();
     // get the approvers from the doc
     if(doc instanceof Content) {
        Content theContent = (Content)doc;
        // set the workspace to use dn
        Workspace ws = doc.getSourceWorkspace();
        boolean dnUsed = ws.isDistinguishedNamesUsed();
        ws.useDistinguishedNames(true);
        try {
           String[] approvers = theContent.getCurrentApprovers();
           if(approvers != null) {
              for(int x=0;x<approvers.length;x++) {
                 String dn = approvers[x];
                 if (isDebug) {
                    s_log.log(Level.FINEST, "dn = "+dn+", retrieve email");
                 }
                 User theUser = Utils.getUserByDN(dn);
                 if(theUser != null) {
                    recipientList.addAll(Utils.getEmailsUser(theUser));
                 } 
                 else {
                    if (isDebug) {
                       s_log.log(Level.FINEST, "theUser was null, try group");
                    }
                    Group theGroup = Utils.getGroupByDistinguishedName(dn);
                    if(theGroup != null) {
                       recipientList.addAll(Utils.getEmailsGroup(theGroup));
                    }
                    else {
                       if (isDebug) {
                          s_log.log(Level.FINEST, "theGroup was null");
                       }
                       
                    }
                 }
                 
              }
           }
        }
        catch (PropertyRetrievalException e) {
           // TODO Auto-generated catch block
           if (s_log.isLoggable(Level.FINEST))
           {
              s_log.log(Level.FINEST, "", e);
           }
        }
        finally {
           if(ws != null) {
              ws.useDistinguishedNames(dnUsed);
           }
        }
        
     }
     
     if (isDebug) {
        s_log.exiting("RetrieveEmailReminderByUuidScopedAction", "getRecipients");
     }
     
     return recipientList;

  }
  
  /**
   * instead of sending to wcm approvers, send to the PolicyApprovers
   * @see com.prudential.wf.actions.BaseEmailAction#getRecipients(com.ibm.workplace.wcm.api.Document)
   */
  ArrayList getRecipients(Document doc) {
     // TODO Auto-generated method stub
     boolean isDebug = s_log.isLoggable(Level.FINEST);
     if (isDebug) {
        s_log.entering("RetrieveEmailReminderByUuidScopedAction", "getRecipients for doc "+doc.getName());
     }
     Set recipientSet = new HashSet();
     ArrayList recipientList = null;
     // get the approvers from the doc
     if(doc instanceof Content) {
        Content theContent = (Content)doc;
        // set the workspace to use dn
        Workspace ws = doc.getSourceWorkspace();
        boolean dnUsed = ws.isDistinguishedNamesUsed();
        ws.useDistinguishedNames(true);
        try {
           //String[] approvers = theContent.getCurrentApprovers();
           // get the wf stage.  if its review, get reviewers, if its approve get approvers
           String m_usercmpntname = "PolicyReviewers";
           DocumentId wfstage = theContent.getWorkflowStageId();
           String stageName = wfstage.getName().toLowerCase();
           if(stageName.contains("approve")) {
              m_usercmpntname = "PolicyApprovers";
           }
           Set<String> approverSet = new HashSet<String>();
           extractApprovers(doc, approverSet, m_usercmpntname);
           String[] approvers = approverSet.toArray(new String[approverSet.size()]);
           // need string[] of dn values
           if(approvers != null) {
              for(int x=0;x<approvers.length;x++) {
                 String dn = approvers[x];
                 if (isDebug) {
                    s_log.log(Level.FINEST, "dn = "+dn+", retrieve email");
                 }
                 User theUser = Utils.getUserByDN(dn);
                 if(theUser != null) {
                    recipientSet.addAll(Utils.getEmailsUser(theUser));
                 } 
                 else {
                    if (isDebug) {
                       s_log.log(Level.FINEST, "theUser was null, try group");
                    }
                    Group theGroup = Utils.getGroupByDistinguishedName(dn);
                    if(theGroup != null) {
                       recipientSet.addAll(Utils.getEmailsGroup(theGroup));
                    }
                    else {
                       if (isDebug) {
                          s_log.log(Level.FINEST, "theGroup was null");
                       }
                       
                    }
                 }
                 
              }
           }
        }
      catch (AuthorizationException e) {
         // TODO Auto-generated catch block
         if (s_log.isLoggable(Level.FINEST))
         {
            s_log.log(Level.FINEST, "", e);
         }
      }
      catch (PropertyRetrievalException e) {
         // TODO Auto-generated catch block
         if (s_log.isLoggable(Level.FINEST))
         {
            s_log.log(Level.FINEST, "", e);
         }
      }
        finally {
           if(ws != null) {
              ws.useDistinguishedNames(dnUsed);
           }
        }
        
     }
     
     if (isDebug) {
        s_log.exiting("RetrieveEmailReminderByUuidScopedAction", "getRecipients");
     }
     
     recipientList = new ArrayList(Arrays.asList(recipientSet.toArray()));
     return recipientList;

  }
  
  /**
   * 
   * extractApprovers helper method to retrieve the approvers from the userselectioncmpnt
   * @param doc the content to retrieve from
   * @param approvers the set to add the approvers to
   */
  protected void extractApprovers(Document doc, Set<String> approvers, String p_componentName) {
     boolean isDebug = s_log.isLoggable(Level.FINEST);
     ContentComponent cmpnt = WCMUtils.getContentComponent(doc, p_componentName);
     if (cmpnt instanceof UserSelectionComponent) {
        for (Principal p : ((UserSelectionComponent) cmpnt).getSelections()) {
           if (isDebug) {
              s_log.log(Level.FINEST, "adding principal "+p.getName());
           }
           com.ibm.portal.um.Principal thePrincipal = null;
           thePrincipal = Utils.getPrincipalById(p.getName());
           approvers.add(Utils.getDnForPrincipal(thePrincipal));
        }
     }
  }
   
}

