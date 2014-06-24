/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.authoring.launchpage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.ibm.workplace.wcm.*;
import com.ibm.workplace.wcm.api.*;
import com.ibm.ws.webcontainer.util.ExtDocRootFile;

public class CustomAuthoringItemWrapper {

   /** Logger for the class */
   
   /** The actions available to launch for the item */
   private List<CustomAuthoringItemAction> m_actions;
   
   /** a map of any passed additional attributes.  If query builder has passed fields to pull it will be in here */
   private Map<String, Object> m_additionalAttributes = new HashMap<String, Object>();
   
   /** an arraylist of documentid values for the categories */
   private ArrayList<DocumentId> m_categories = new ArrayList<DocumentId>();
   
   public ArrayList<DocumentId> getCategories() {
      return m_categories;
   }

   public void setCategories(ArrayList<DocumentId> p_categories) {
      m_categories = p_categories;
   }

   private static Logger s_log = Logger.getLogger(CustomAuthoringItemWrapper.class.getName());

   /** Action name for the Create Content action */
   public static String CREATE_CONTENT_ACTION_ID = "CreateContent";

   /** Action name for the Open View action */
   public static String OPEN_VIEW_ACTION_ID = "OpenView";

   /** Action name for the Open Item action */
   public static String OPEN_ACTION_ID = "Open";

   /** Action name for the Preview Item action */
   public static String PREVIEW_ACTION_ID = "Preview";

   /** Action name for the Approve Item action */
   public static String APPROVE_ACTION_ID = "Approve";
   
   /** Action name for the Approve Item action */
   public static String EDIT_ACTION_ID = "Edit";

   /** Action name for the Reject Item action */
   public static String REJECT_ACTION_ID = "Reject";
   
   /** Action name for the Reject Item action */
   public static String DELETE_ACTION_ID = "Delete";

   /** Display name (ie. the alt text) for the icon for the item */
   private String m_iconDisplayName;

   /** Path of the icon for the item */
   private String m_iconPath;

   /** Title of the item */
   private String m_title;

   /** Path to the item as a list of titles (e.g. Library/site area/content) */
   private String m_path;

   /** Formatted last log event date */
   private String m_lastLogEntryDateString;

   /** Details for the item */
   private String m_details;

   /** Internal JCR id for the item */
   private String m_itemId;

   /** Workflow status for the item */
   private String m_status;

   /** The item's library */
   private String m_library;
   
   /** item's workflow stage*/
   private String m_wfStage;
   
   public String getWfStage() {
      return m_wfStage;
   }

   public void setWfStage(String p_wfStage) {
      m_wfStage = p_wfStage;
   }

   private Date m_expireDate;
   
   public Date getExpireDate() {
      return m_expireDate;
   }

   public void setExpireDate(Date p_expireDate) {
      m_expireDate = p_expireDate;
   }

   private Date m_liveDate;
   
   private String m_authTemplateName = "";

   public String getAuthTemplateName() {
      return m_authTemplateName;
   }

   public void setAuthTemplateName(String p_authTemplateName) {
      m_authTemplateName = p_authTemplateName;
   }

   public Date getLiveDate() {
      return m_liveDate;
   }

   public void setLiveDate(Date p_liveDate) {
      m_liveDate = p_liveDate;
   }

   public Date getLastModDate() {
      return m_lastModDate;
   }

   public void setLastModDate(Date p_lastModDate) {
      m_lastModDate = p_lastModDate;
   }

   public Date getReviewDate() {
      return m_reviewDate;
   }

   public void setReviewDate(Date p_reviewDate) {
      m_reviewDate = p_reviewDate;
   }

   private Date m_lastModDate;
   
   private Date m_reviewDate;
   
   private Date m_createdDate;
   
   
   public Date getCreatedDate() {
      return m_createdDate;
   }

   public void setCreatedDate(Date p_createdDate) {
      m_createdDate = p_createdDate;
   }

   /** Author for the item */
   private String m_author;

   public String getIconDisplayName() {
      return m_iconDisplayName;
   }

   public void setIconDisplayName(String p_iconDisplayName) {
      m_iconDisplayName = p_iconDisplayName;
   }

   public String getIconPath() {
      return m_iconPath;
   }

   public void setIconPath(String p_iconPath) {
      m_iconPath = p_iconPath;
   }

   public String getTitle() {
      return m_title;
   }

   public void setTitle(String p_title) {
      m_title = p_title;
   }

   public String getPath() {
      return m_path;
   }

   public void setPath(String p_path) {
      m_path = p_path;
   }

   public String getLastLogEntryDateString() {
      return m_lastLogEntryDateString;
   }

   public void setLastLogEntryDateString(String p_lastLogEntryDateString) {
      m_lastLogEntryDateString = p_lastLogEntryDateString;
   }

   public String getDetails() {
      return m_details;
   }

   public void setDetails(String p_details) {
      m_details = p_details;
   }

   public String getItemId() {
      return m_itemId;
   }

   public void setItemId(String p_itemId) {
      m_itemId = p_itemId;
   }

   public String getStatus() {
      return m_status;
   }

   public void setStatus(String p_status) {
      m_status = p_status;
   }

   public String getLibrary() {
      return m_library;
   }

   public void setLibrary(String p_library) {
      m_library = p_library;
   }

   public String getAuthor() {
      return m_author;
   }

   public void setAuthor(String p_author) {
      m_author = p_author;
   }
   
   /**
    * Get all the actions for the item
    * @return the actions
    */
   public List<CustomAuthoringItemAction> getActions()
   {
      return m_actions;
   }
   
   /**
    * Find an item action by id
    * @param p_actionId The action id
    * @return The action with the given id
    */
   public CustomAuthoringItemAction getAction(String p_actionId)
   {
      CustomAuthoringItemAction actionWithGivenId = null;
      for (CustomAuthoringItemAction action : m_actions)
      {
         if (action.getId().equals(p_actionId))
         {
            actionWithGivenId = action;
            break;
         }
      }
      return actionWithGivenId;
   }
   
   /**
    * Get the open action for this item bean
    * @return the action
    */
   public CustomAuthoringItemAction getOpenAction()
   {
      return getAction(OPEN_ACTION_ID);
   }

   public CustomAuthoringItemWrapper(String p_iconDisplayName, String p_iconPath, String p_title, String p_itemId, String p_status, List <CustomAuthoringItemAction>p_actions,
      String p_library, String p_author, Date p_createdDate, Date p_modifiedDate, Date p_pubDate, Map<String,Object> additionalAttributeMap, String p_atName, String p_wfStageName) {

      m_iconDisplayName = p_iconDisplayName;
      m_iconPath = p_iconPath;
      m_title = p_title;
      m_itemId = p_itemId;
      m_status = p_status;
      m_actions = p_actions;
      m_library = p_library;
      m_author = p_author;
      
      m_lastModDate = p_modifiedDate;
      m_createdDate = p_createdDate;
      m_liveDate = p_pubDate;
      
      m_additionalAttributes = new HashMap<String, Object>(additionalAttributeMap);
      
      m_authTemplateName = p_atName;
      
      m_wfStage = p_wfStageName;

      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.log(Level.FINEST, " constructor "+this.toString()+" m_additionalAttributes = "+m_additionalAttributes);
      }
   }

   @Override
   public String toString() {
      return "CustomAuthoringItemWrapper [ m_author=" + m_author + ", m_details=" + m_details + ", m_iconDisplayName=" + m_iconDisplayName
         + ", m_iconPath=" + m_iconPath + ", m_itemId=" + m_itemId + ", m_lastLogEntryDateString=" + m_lastLogEntryDateString
         + ", m_library=" + m_library + ", m_path=" + m_path + ", m_status=" + m_status + ", m_title=" + m_title + ", m_authTemplateName="+m_authTemplateName+"]";
   }
   
   public String getAdditionalAttribute(String p_key) {
      String returnValue = "";
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      
      if (isDebug) {
         s_log.entering("CustomAuthoringItemWrapper", "getAdditionalAttribute for attribute "+p_key+" map contains "+m_additionalAttributes+" for item "+m_itemId);         
      }
      // check from the map
      if(m_additionalAttributes.get(p_key)!= null) {
         returnValue = (String)m_additionalAttributes.get(p_key);
      }
      
      if (isDebug) {
         s_log.exiting("CustomAuthoringItemWrapper", "getAdditionalAttribute returning "+returnValue);
      }
      
      
      return returnValue;
   }

}
