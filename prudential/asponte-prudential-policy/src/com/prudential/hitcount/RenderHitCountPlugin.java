/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.hitcount;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.ibm.portal.ListModel;
import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.RenderingContext;
import com.ibm.workplace.wcm.api.TemplatedDocument;
import com.ibm.workplace.wcm.api.plugin.rendering.RenderingPlugin;
import com.ibm.workplace.wcm.api.plugin.rendering.RenderingPluginException;
import com.ibm.workplace.wcm.api.plugin.rendering.RenderingPluginModel;

public class RenderHitCountPlugin implements RenderingPlugin {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(RenderHitCountPlugin.class.getName());

   private static String m_title = "RenderHitCountPlugin";

   public RenderHitCountPlugin() {
      // TODO Auto-generated constructor stub

   }

   @Override
   public boolean isShownInAuthoringUI() {
      // TODO Auto-generated method stub
      return true;

   }

   @Override
   public String getDescription(Locale p_arg0) {
      // TODO Auto-generated method stub
      return m_title;

   }

   @Override
   public ListModel<Locale> getLocales() {
      // TODO Auto-generated method stub
      return null;

   }

   @Override
   public String getTitle(Locale p_arg0) {
      // TODO Auto-generated method stub
      return m_title;

   }

   @Override
   public String getName() {
      // TODO Auto-generated method stub
      return m_title;
   }

   @Override
   public boolean render(RenderingPluginModel rpm) throws RenderingPluginException {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      boolean successful = false;
      String outputString = "";
      Writer out = rpm.getWriter();

      RenderingContext rc = rpm.getRenderingContext();

      String uuid = "";
      Map<String, List<String>> params = rpm.getPluginParameters();
      List<String> list;      
      if (isDebug) {
         s_log.entering("RenderHitCountPlugin", "render", new Object[] {rpm, params});
      }

      list = params.get("uuid");
      if (list != null && list.size() > 0) {
         uuid = list.get(0);
         if (isDebug) {
            s_log.log(Level.FINEST, "render" + " uuid = " + uuid);
         }

      }

      //String uuid = theResult.getId().getId();
      if (isDebug) {
         s_log.log(Level.FINEST, "trying to retrieve count for " + uuid);
      }
      if (uuid != null && uuid.length() > 0) {
         int count = HitCountDBUtils.getCount(uuid);
         successful = true;
         outputString = "" + count;
      }

      if (isDebug) {
         s_log.exiting("RenderHitCountPlugin", "render " + successful + " writing " + outputString);
      }

      try {
         out.write(outputString);
      }
      catch (IOException e) {
         // TODO Auto-generated catch block
         if (s_log.isLoggable(Level.FINEST)) {
            s_log.log(Level.FINEST, "", e);
         }
      }
      return successful;

   }
}
