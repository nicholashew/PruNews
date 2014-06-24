package com.prudential.hitcount;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class RetrieveHitCount
 */
@WebServlet("/RetrieveHitCount")
public class RetrieveHitCount extends HttpServlet {
   private static final long serialVersionUID = 1L;

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(RetrieveHitCount.class.getName());

   /**
    * @see HttpServlet#HttpServlet()
    */
   public RetrieveHitCount() {
      super();
      // TODO Auto-generated constructor stub
   }

   /**
    * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
    */
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      // TODO Auto-generated method stub
      handleRequest(request, response);
   }

   /**
    * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
    */
   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      // TODO Auto-generated method stub
      handleRequest(request, response);
   }

   protected void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      // TODO Auto-generated method stub
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      
      if (isDebug) {
         s_log.entering("RetrieveHitCount", "handleRequest");
      }
      // get the uuid
      String uuidValue = "";
      uuidValue = (String)request.getParameter("uuid");
      if(uuidValue!=null && !uuidValue.equals("")) {
         if (isDebug) {
            s_log.log(Level.FINEST, "update count for "+uuidValue);
         }
         PrintWriter out = response.getWriter();
         out.print(HitCountDBUtils.getCount(uuidValue));
      }
            
      if (isDebug) {
         s_log.exiting("UpdateHitCount", "handleResponse");
      }
      
      
      if (isDebug) {
         s_log.exiting("RetrieveHitCount", "handleRequest");
      }
      

   }

}
