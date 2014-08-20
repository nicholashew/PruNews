/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.commons.wcm.authoring;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * a wrapper that contains both id and title
 */
public class ObjectWrapper {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(ObjectWrapper.class.getName());
   private String id;
   private String label;
   private String path;
   private String description;
   public String getDescription() {
      return description;
   }

   public void setDescription(String p_description) {
      description = p_description;
   }

   public String getPath() {
      return path;
   }

   public void setPath(String p_path) {
      path = p_path;
   }
   private boolean selected;
   
   public boolean isSelected() {
      return selected;
   }

   public void setSelected(boolean p_selected) {
      selected = p_selected;
   }

   public String getId() {
      return id;
   }

   public void setId(String p_id) {
      id = p_id;
   }

   public String getLabel() {
      return label;
   }

   public void setLabel(String p_label) {
      label = p_label;
   }
   
   public ObjectWrapper(String p_id,String p_label) {
      this(p_id,p_label,false);
   }

   public ObjectWrapper(String p_id,String p_label, boolean p_selected) {
     this(p_id,p_label,"",p_selected);
   }
   public ObjectWrapper(String p_id,String p_label, String p_path) {
      this(p_id,p_label,p_path,true);
   }
   public ObjectWrapper(String p_id,String p_label, String p_path, boolean p_selected) {
      this(p_id,p_label,p_path,p_selected,"");
   }
   public ObjectWrapper(String p_id,String p_label, String p_path, boolean p_selected, String p_description) {
      id = p_id;
      label = p_label;
      path = p_path;
      selected = p_selected;
      description = p_description;
   }
   
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("{\"id\":\""+id+"\"");
      sb.append(",\"label\":\""+label+"\"");
      sb.append(",\"path\":\""+path+"\"");
      sb.append(",\"description\":\""+description+"\"");
      sb.append(",\"selected\":\""+selected+"\"}");
      return sb.toString();
   }
}

