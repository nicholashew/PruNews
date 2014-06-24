package com.prudential.servlets;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.workplace.wcm.api.Repository;
import com.ibm.workplace.wcm.api.VirtualPortalContext;
import com.ibm.workplace.wcm.api.WCM_API;
import com.ibm.workplace.wcm.api.exceptions.VirtualPortalNotFoundException;
import com.ibm.workplace.wcm.api.exceptions.WCMException;
import com.prudential.utils.Utils;
import com.prudential.vp.HandleDelete;
/**
 * Servlet implementation class ProcessLinkDelete
 */
@WebServlet("/ProcessLinkDelete")
public class ProcessLinkDelete extends HttpServlet {
   private static final long serialVersionUID = 1L;

   // the logger for the class    
   static Logger s_log = Logger.getLogger(ProcessLinkDelete.class.getName());

   /**
    * @see HttpServlet#HttpServlet()
    */
   public ProcessLinkDelete() {
      super();
      // TODO Auto-generated constructor stub
   }

   /**
    * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
    *      response)
    */
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      // TODO Auto-generated method stub
      this.handleRequest(request, response);
   }

   /**
    * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
    *      response)
    */
   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      // TODO Auto-generated method stub
      this.handleRequest(request, response);
   }

   protected void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      String uuidString = param(request, "uuid");
      if (isDebug) {
         s_log.entering("ProcessLinkDelete", "handleRequest");
      }
      boolean success = true;

      Repository repo = WCM_API.getRepository();
      try {
         VirtualPortalContext vctx = repo.generateVPContextFromContextPath(Utils.getVPName());
         HandleDelete vpA = new HandleDelete(uuidString);
         // check if we need to call the setters
         repo.executeInVP(vctx, vpA);
         success = vpA.getReturnedValue();

      }
      catch (VirtualPortalNotFoundException e) {
         e.printStackTrace();
      }
      catch (WCMException e) {
         e.printStackTrace();
      }

      if (isDebug) {
         s_log.exiting("ProcessLinkDelete", "handleRequest");
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
