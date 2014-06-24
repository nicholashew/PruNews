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
import org.apache.commons.lang3.StringEscapeUtils;

import java.security.Principal;

/**
 * Author: John Ewers <john.ewers@asponte.com>
 * A class to print out the Approvers and Reviewers for a site area.
 */
public class ApproversReviewersPlugin implements RenderingPlugin {
   private static Logger s_log = Logger.getLogger(ApproversReviewersPlugin.class.getName());

   @Override
   public String getDescription(final Locale p_locale) {
      return "Rendering plugin for getting the approvers and reviewers from a site area.";
   }

   @Override
   public ListModel<Locale> getLocales() {
      return null;
   }

   @Override
   public String getName() {
      return "ApproversReviewersPlugin";
   }

   @Override
   public String getTitle(final Locale p_locale) {
      return "ApproversReviewersPlugin";
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

      Workspace usersWorkspace = Utils.getWorkspace();
      boolean originalValueDN = usersWorkspace.isDistinguishedNamesUsed();
      usersWorkspace.useDistinguishedNames(true);
      String curApprovers = "";
      String curReviewers = "";
      boolean forJavascript = false;
      List<String> list;
      
      list = params.get("forJavascript");
      if (list != null && list.size() > 0) {
         String displayJavascriptString = list.get(0);
         forJavascript = Boolean.parseBoolean(displayJavascriptString);
         if (isDebug) {
            s_log.log(Level.FINEST, "forJavascript passed = "+displayJavascriptString);
         }
      }
      RenderingContext rc = (RenderingContext) p_model.getRenderingContext();
      Content incoming = rc.getContent();
      if (incoming != null) {
         DocumentId parentId = incoming.getDirectParent();
         SiteArea curSA;
         try {
            curSA = (SiteArea) usersWorkspace.getById(parentId);
            UserSelectionComponent approversCmpt = (UserSelectionComponent) curSA.getComponent("DefaultPolicyApprover");
            if (approversCmpt != null) {
               Principal[] approversAry = approversCmpt.getSelections();
               for (int i = 0; i < approversAry.length; i++) {
                  if(forJavascript) {
                     curApprovers += "&wcmfield.element.PolicyApprovers='" + StringEscapeUtils.escapeJava(StringEscapeUtils.escapeXml(approversAry[i].getName())) + "' ";
                  }
                  else {
                     curApprovers += "wcmfield.element.PolicyApprovers=\"" + approversAry[i].getName() + "\" ";
                  }
                  
               }
               successful = true;
            }
            UserSelectionComponent reviewerCmpt = (UserSelectionComponent) curSA.getComponent("DefaultPolicyReviewer");
            if (reviewerCmpt != null) {
               Principal[] reviewersAry = reviewerCmpt.getSelections();
               for (int i = 0; i < reviewersAry.length; i++) {
                  if(forJavascript) {
                     curReviewers += "&wcmfield.element.PolicyReviewers='" + StringEscapeUtils.escapeJava(StringEscapeUtils.escapeXml(reviewersAry[i].getName())) + "' ";
                  } else {
                     curReviewers += "wcmfield.element.PolicyReviewers=\"" + reviewersAry[i].getName() + "' ";
                  }
                  
               }
               successful = true;
            }
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
         catch (ComponentNotFoundException e) {
            // TODO Auto-generated catch block
            if (s_log.isLoggable(Level.FINEST))
            {
               s_log.log(Level.FINEST, "", e);
            }
         }

      }

      try {
         writer.write(curApprovers + " ");
         writer.write(curReviewers + " ");
      }
      catch (IOException e) {
         if (isDebug) {
            s_log.log(Level.FINEST, "", e);
         }
      }
      usersWorkspace.useDistinguishedNames(originalValueDN);
      return successful;
   }
}