/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.vp;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
public class JSONMoveItemWrapper {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(JSONMoveItemWrapper.class.getName());
   
   private String uuid;
   private List<JSONMoveItemWrapper> children;


   public String getUuid()
   {
       return uuid;
   }

   public String toString() {
       return "name: " + uuid + ", children = " + children;
   }
   public List<JSONMoveItemWrapper> getChildren() {
       return children;
   }
   public void setChildren(List<JSONMoveItemWrapper> children) {
       this.children = children;
   }
}



