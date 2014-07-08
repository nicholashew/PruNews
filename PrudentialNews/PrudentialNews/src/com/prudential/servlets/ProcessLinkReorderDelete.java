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
		String processMLString = param(request, "processML");
		String processAsJSONString = param(request, "processJSON");
		boolean processML = Boolean.valueOf(processMLString);
		boolean processAsJSON = Boolean.valueOf(processAsJSONString);
		String configContentName = param(request, "configContentName");

		boolean success = true;

		if (isDebug) {
			s_log.log(Level.FINEST, "uuid of link is " + uuidString);
			s_log.log(Level.FINEST, "processML = " + processML);
			s_log.log(Level.FINEST, "processAsJSON = " + processAsJSON);
			s_log.log(Level.FINEST, "configContentName = " + configContentName);
		}

		Repository repo = WCM_API.getRepository();
		try {
			VirtualPortalContext vctx = repo
					.generateVPContextFromContextPath(Utils.getVPName());
			HandleReorder vpA = new HandleReorder(uuidString);
			// check if we need to call the setters
			if (processML) {
				vpA.setP_processML(processML);
			}
			if (processAsJSON) {
				// vpA.setP_processAsJSON(processAsJSON);
				HandleReorderJSON vpAJSON = new HandleReorderJSON(
						uuidString);
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

	public static String param(HttpServletRequest request, String name) {
		String s = request.getParameter(name);
		if (s != null) {
			s = s.trim();
		}
		return s;
	}

}
