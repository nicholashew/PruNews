package com.prudential.renderer;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.portal.ListModel;
import com.ibm.workplace.wcm.api.exceptions.AuthorizationException;
import com.ibm.workplace.wcm.api.exceptions.ComponentNotFoundException;
import com.ibm.workplace.wcm.api.exceptions.DocumentRetrievalException;
import com.ibm.workplace.wcm.api.plugin.rendering.RenderingPlugin;
import com.ibm.workplace.wcm.api.plugin.rendering.RenderingPluginException;
import com.ibm.workplace.wcm.api.plugin.rendering.RenderingPluginModel;
import com.ibm.workplace.wcm.api.*;
import com.prudential.utils.Utils;
import com.prudential.wcm.WCMUtils;

import org.apache.commons.lang3.StringEscapeUtils;

import java.security.Principal;

/**
 * Author: Chris Knight
 * A class to print out the passed Element from somewhere in the path of the content.
 */
public class RetrieveElementInPath implements RenderingPlugin {
   private static Logger s_log = Logger.getLogger(RetrieveElementInPath.class.getName());

   @Override
   public String getDescription(final Locale p_locale) {
      return "Rendering plugin for rendering a component from up the tree.";
   }

   @Override
   public ListModel<Locale> getLocales() {
      return null;
   }

   @Override
   public String getName() {
      return "RetrieveElementInPath";
   }

   @Override
   public String getTitle(final Locale p_locale) {
      return "RetrieveElementInPath";
   }

   @Override
   public boolean isShownInAuthoringUI() {
      return true;
   }

   @Override
   public boolean render(final RenderingPluginModel p_model) throws RenderingPluginException {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      boolean successful = false;
      final Map<String, List<String>> params = p_model.getPluginParameters();
      final Writer writer = p_model.getWriter();
      String returnString = "";
      String elementName = "";
      Workspace usersWorkspace = Utils.getWorkspace();
      boolean originalValueDN = usersWorkspace.isDistinguishedNamesUsed();
      usersWorkspace.useDistinguishedNames(true);

      List<String> list;

      list = params.get("element");
      if (list != null && list.size() > 0) {
         elementName = list.get(0);
         if (isDebug) {
            s_log.log(Level.FINEST, "element passed = " + elementName);
         }
      }
      RenderingContext rc = (RenderingContext) p_model.getRenderingContext();
      Content incoming = rc.getContent();
      if (incoming != null && elementName != null && !elementName.isEmpty()) {
         DocumentId parentId = incoming.getDirectParent();
         SiteArea curSA;
         while (parentId != null && !successful) {
            try {
               curSA = (SiteArea) usersWorkspace.getById(parentId);
               if (isDebug) {
                  s_log.log(Level.FINEST, "checking "+curSA.getName()+" for component "+elementName);
               }
               if (curSA.hasComponent(elementName)) {
                  ContentComponent result = WCMUtils.getContentComponent(curSA, elementName);
                  if (result != null) {
                     // now, figure out what the component is and if it's not empty, render it
                     returnString = WCMUtils.getCmpntString(result);
                     if (returnString != null && !returnString.isEmpty()) {
                        successful = true;
                        if (isDebug) {
                           s_log.log(Level.FINEST, "value found "+returnString);
                        }
                     }
                  }
               }
               parentId = curSA.getParentId();
            }

            catch (DocumentRetrievalException e) {
               // TODO Auto-generated catch block
               if (s_log.isLoggable(Level.FINEST)) {
                  s_log.log(Level.FINEST, "", e);
               }
            }
            catch (AuthorizationException e) {
               // TODO Auto-generated catch block
               if (s_log.isLoggable(Level.FINEST)) {
                  s_log.log(Level.FINEST, "", e);
               }
            }
         }

      }

      if (successful) {
         try {
            if (isDebug) {
               s_log.log(Level.FINEST, "Was successful, returning "+returnString);
            }
            writer.write(returnString);
            //writer.write(curReviewers + " ");
         }
         catch (IOException e) {
            if (isDebug) {
               s_log.log(Level.FINEST, "", e);
            }
         }
      }
      usersWorkspace.useDistinguishedNames(originalValueDN);
      return successful;
   }
}