package com.prudential.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.ibm.workplace.wcm.api.*;
import com.ibm.workplace.wcm.api.exceptions.AuthorizationException;
import com.ibm.workplace.wcm.api.exceptions.DocumentIdCreationException;
import com.ibm.workplace.wcm.api.exceptions.DocumentRetrievalException;
import com.ibm.workplace.wcm.api.exceptions.VirtualPortalNotFoundException;
import com.ibm.workplace.wcm.api.exceptions.WCMException;
import com.prudential.utils.Utils;
import com.prudential.vp.HandleDelete;
import com.prudential.vp.HandleReorder;
import com.prudential.vp.HandleReorderJSON;

/**
 * Servlet implementation class ProcessLinkReorder
 */
@WebServlet("/ProcessLinkReorderDelete")
public class ProcessLinkReorderDelete extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// the logger for the class
	static Logger s_log = Logger.getLogger(ProcessLinkReorderDelete.class
			.getName());

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ProcessLinkReorderDelete() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		this.handleRequest(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		this.handleRequest(request, response);
	}

	protected void handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		boolean isDebug = s_log.isLoggable(Level.FINEST);
		if (isDebug) {
			s_log.entering("ProcessLinkReorderDelete()", "handleRequest");
		}
		String uuidString = param(request, "uuid");
		String uuidDeleteString = param(request, "uuidDelete");
		String siteAreaId = param(request, "siteAreaID");
		String processMLString = param(request, "processML");
		String processAsJSONString = param(request, "processJSON");
		boolean processML = Boolean.valueOf(processMLString);
		boolean processAsJSON = Boolean.valueOf(processAsJSONString);
		String configContentName = param(request, "configContentName");

		boolean success = true;

		String uuidLinkString = null;
		try {
			uuidLinkString = getLinkIds(uuidString, siteAreaId, isDebug);
		} catch (DocumentIdCreationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (DocumentRetrievalException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (AuthorizationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (isDebug) {
			s_log.log(Level.FINEST, "uuid of content is " + uuidLinkString);
			s_log.log(Level.FINEST, "uuid of link is " + uuidString);
			s_log.log(Level.FINEST, "processML = " + processML);
			s_log.log(Level.FINEST, "processAsJSON = " + processAsJSON);
			s_log.log(Level.FINEST, "configContentName = " + configContentName);
		}

		Repository repo = WCM_API.getRepository();
		try {
			VirtualPortalContext vctx = repo
					.generateVPContextFromContextPath(Utils.getVPName());
			HandleReorder vpA = new HandleReorder(uuidLinkString);
			// check if we need to call the setters
			if (processML) {
				vpA.setP_processML(processML);
			}
			if (processAsJSON) {
				// vpA.setP_processAsJSON(processAsJSON);
				HandleReorderJSON vpAJSON = new HandleReorderJSON(
						uuidLinkString);
				repo.executeInVP(vctx, vpAJSON);
				success = vpAJSON.getReturnedValue();
			} else {
				if (configContentName != null && !configContentName.equals("")) {
					vpA.setP_configContentName(configContentName);
				}

				repo.executeInVP(vctx, vpA);
				success = vpA.getReturnedValue();
			}

			// content = vpA.getReturnedValue();
			if (uuidDeleteString != null && uuidDeleteString.length() > 0) {
				try {
					HandleDelete vpDelete = new HandleDelete(uuidDeleteString);
					// check if we need to call the setters
					repo.executeInVP(vctx, vpDelete);
					success = vpDelete.getReturnedValue();

				} catch (VirtualPortalNotFoundException e) {
					e.printStackTrace();
				} catch (WCMException e) {
					e.printStackTrace();
				}

			}

		} catch (VirtualPortalNotFoundException e) {
			e.printStackTrace();
		} catch (WCMException e) {
			e.printStackTrace();
		}

		if (isDebug) {
			s_log.exiting("ProcessLinkReorderDelete()",
					"handleRequest was success = " + success);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private String getLinkIds(String uuidString, String siteAreaId, boolean isDebug) throws DocumentIdCreationException, DocumentRetrievalException, AuthorizationException {
		if (isDebug) {
			s_log.entering("ProcessLinkReorderDelete()", "getLinkIds()");
		}
		String uuidLinkString = null;
		String[] uuids = uuidString.split(",");
		ArrayList docIdList = new ArrayList();
		Workspace thisWorkspace = Utils.getSystemWorkspace();

		DocumentId tempDocId = null;

		for (int x = 0; x < uuids.length; x++) {
			tempDocId = thisWorkspace.createDocumentId(uuids[x]);
			if (isDebug) {
				s_log.log(Level.FINEST, "Value: " + uuids[x]);
				s_log.log(Level.FINEST, "Retrieved docId = " + tempDocId);
			}
			docIdList.add(tempDocId);
		}

		DocumentId siteAreaDocId = thisWorkspace.createDocumentId(siteAreaId);
		if (isDebug) {
			s_log.log(Level.FINEST, "Site area id: " + siteAreaDocId.toString());
			s_log.log(Level.FINEST, "Site area name: " + siteAreaDocId.getName());
		}
		
		// Need to sort the linkedDoc ids in the same order that docIdList is in.
		// docIdList is the correct order but is the actual documents IDs, not the content link doc ids
		String orderedContentLinkIds[] = new String[docIdList.size()];
		SiteArea siteArea = (SiteArea) thisWorkspace.getById(siteAreaDocId, true);
		DocumentIdIterator linkedDocIdsIter = siteArea.getLinkedChildren();
		if (isDebug) {
			s_log.log(Level.FINEST, "Getting linked doc IDs for site area");
		}

		while (linkedDocIdsIter.hasNext()) {
			DocumentId linkedDocId = linkedDocIdsIter.next();
			ContentLink contentLink = thisWorkspace.getById(linkedDocId);
			DocumentId realDocId = contentLink.getContentId();
			if (isDebug) {
				s_log.log(Level.FINEST, "Linked doc id: " + linkedDocId.toString() + " name: " + linkedDocId.getName());
				s_log.log(Level.FINEST, "Real doc id: " + realDocId.toString() + " name: " + realDocId.getName());
			}
			if (docIdList.contains(realDocId)) {
				orderedContentLinkIds[docIdList.indexOf(realDocId)] = contentLink.toString();
			}
		}
		uuidLinkString = StringUtils.join(orderedContentLinkIds, ",");		
		if (isDebug) {
			s_log.exiting("ProcessLinkReorderDelete()", "getLinkIds() returning: " + uuidLinkString);
		}
		return uuidLinkString;
	}

	public static String param(HttpServletRequest request, String name) {
		String s = request.getParameter(name);
		if (s != null) {
			s = s.trim();
		}
		return s;
	}

}
