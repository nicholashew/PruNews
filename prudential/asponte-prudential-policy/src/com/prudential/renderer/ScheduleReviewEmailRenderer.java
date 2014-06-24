package com.prudential.renderer;

import java.util.logging.Logger;

import java.util.HashMap;
import java.util.Map;

import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.Document;
import com.prudential.wcm.WCMUtils;
import com.prudential.wcm.renderer.MapRenderResults;
import com.prudential.wcm.renderer.RenderException;
import com.prudential.wcm.renderer.WCMEmailRenderer;
import com.prudential.wcm.renderer.WCMRenderResults;

public class ScheduleReviewEmailRenderer implements WCMEmailRenderer {
	private static Logger log = Logger.getLogger(ScheduleReviewEmailRenderer.class.getName());
	private String wcmContentLink = null;
	
	public void setWcmContentLink(String wcmContentLink) {
		this.wcmContentLink = wcmContentLink;
	}

	public WCMRenderResults render(Document doc) throws RenderException {
		log.entering(this.getClass().getSimpleName(), "render");
		try {
			Content theContent = (Content) doc;
			//String currentStage = theContent.getWorkflowStageId().getName();
			Map<String, Object> segments = new HashMap<String, Object>();
			segments.put(SUBJECT_KEY, "Scheduled Review: " + theContent.getName());			

			StringBuffer theText = new StringBuffer();
			
			theText.append("<p>The content is due for a scheduled review.");
			theText.append("<p>Please click on the following links to review the content:</p>");
			String contentId = doc.getId().toString();
			String linkString = wcmContentLink + contentId;
			String label1 = "English";
			theText.append(label1 + " version:  <a href=\""
					+ linkString + "\">" + theContent.getTitle() + "</a>");
							
			//HISTORY
			theText.append("<br /><br /><b>Most Recent History ("+label1+" version):</b><br />");
			theText.append(WCMUtils.getDocHistoryEntries(theContent, 5));
			segments.put(BODY_KEY, theText.toString());
			return new MapRenderResults(segments);
		} catch (Exception ex) {
			throw new RenderException(ex);
		}
	}
}
