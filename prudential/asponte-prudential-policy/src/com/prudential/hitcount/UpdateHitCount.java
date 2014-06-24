package com.prudential.hitcount;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.bsf.debug.util.DebugLog;

/**
 * Servlet implementation class UpdateHitCount
 */
@WebServlet("/UpdateHitCount")
public class UpdateHitCount extends HttpServlet {
   private static final long serialVersionUID = 1L;
   private static Logger s_log = Logger.getLogger(UpdateHitCount.class.getName());
   

   /**
    * @see HttpServlet#HttpServlet()
    */
   public UpdateHitCount() {
      super();
      // TODO Auto-generated constructor stub
   }

   /**
    * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
    */
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      // TODO Auto-generated method stub
      handleResponse(request, response);
   }

   /**
    * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
    */
   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      // TODO Auto-generated method stub
      handleResponse(request, response);
   }

   protected void handleResponse(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      
      if (isDebug) {
         s_log.entering("UpdateHitCount", "enclosing_method");
      }
      
      // get the uuid
      String uuidValue = "";
      uuidValue = (String)request.getParameter("uuid");
      if(uuidValue!=null && !uuidValue.equals("")) {
         if (isDebug) {
            s_log.log(Level.FINEST, "update count for "+uuidValue);
         }
         HitCountDBUtils.updateCount(uuidValue);
      }
            
      if (isDebug) {
         s_log.exiting("UpdateHitCount", "handleResponse");
      }
      
      
   }

}
