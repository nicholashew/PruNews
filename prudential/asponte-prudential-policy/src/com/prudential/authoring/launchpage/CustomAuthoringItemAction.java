/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.authoring.launchpage;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * An action available to launch for a <code>LaunchPageItemListBean</code>, e.g. 
 * Open the item.
 */
public class CustomAuthoringItemAction implements Serializable
{

   /** The id of the action */
   private String m_id;

   /** The display name of the action */
   private String m_name;

   /** The URL of the action to launch for the item */
   private String m_actionURL;
   
   /** Additional params for the HTML link. This can be used to set the target, name, etc. */
   private String m_additionalLinkParams = "";

   /**
    * Constructor
    * @param p_id The id of the action
    * @param p_name The display name of the action
    * @param p_actionURL The URL of the action to launch for the item
    */
   public CustomAuthoringItemAction(String p_id, String p_name, String p_actionURL)
   {
      m_id = p_id;
      m_name = p_name;
      m_actionURL = p_actionURL;
   }
   
   /**
    * Get the id of the action
    * @return the id of the action
    */
   public String getId()
   {
      return m_id;
   }
   
   /**
    * Get the display name of the action
    * @return the display name of the action
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Get the URL of the action to launch for the item
    * @return the URL of the action to launch for the item
    */
   public String getActionURL()
   {
      return m_actionURL;
   }
   
   /**
    * Get additional params for the HTML link. This can be used to set the target, name, etc.
    * @return additional params
    */
   public String getAdditionalLinkParams()
   {
      return m_additionalLinkParams;
   }
   
   /**
    * Set additional params for the HTML link. This can be used to set the target, name, etc.
    * @param p_additionalLinkParams additional params
    */
   public void setAdditionalLinkParams(String p_additionalLinkParams)
   {
      m_additionalLinkParams = p_additionalLinkParams;
   }

   @Override
   public String toString()
   {
      return "LaunchPageItemAction [m_actionURL=" + m_actionURL + ", m_additionalLinkParams=" + m_additionalLinkParams + ", m_id="
         + m_id + ", m_name=" + m_name + "]";
   }
}