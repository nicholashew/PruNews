package com.prudential.policy.scheduler;

import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.ws.webcontainer.servlet.ServletConfig;
import com.prudential.tasks.RetrieveAnnualReviewContentTask;
import com.prudential.tasks.RetrieveReminderContentTask;

/**
 * Servlet implementation class CheckNotificationEmail
 */
@WebServlet("/CheckNotificationEmail")
public class CheckNotificationEmail extends HttpServlet {
   private static final long serialVersionUID = 1L;

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(CheckNotificationEmail.class.getName());

   private static boolean isInit = false;

   // daily
   // every 10 minutes for testing
   //private static long interval = 86400000;
   private static long interval = 600000;

   /**
    * @see HttpServlet#HttpServlet()
    */
   public CheckNotificationEmail() {
      super();
      // TODO Auto-generated constructor stub
   }

   public void init() throws ServletException {
      if (!isInit) {
         isInit = true;
         System.out.println("Initializing CheckNotificationEmail");
         /* create and schedule the updatedcontenttask */
         RetrieveReminderContentTask thisTask = new RetrieveReminderContentTask();
         RetrieveAnnualReviewContentTask annualReviewTask = new RetrieveAnnualReviewContentTask();
         Timer timer = new Timer(true);
         timer.scheduleAtFixedRate(thisTask, new Date(), interval);
         timer.scheduleAtFixedRate(annualReviewTask, new Date(), interval);
      }
      //timer.schedule(thisTask, new Date(), interval);
      super.init();

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
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("CheckNotificationEmail", "handleRequest");
      }

      // run the task once
      RetrieveReminderContentTask thisTask = new RetrieveReminderContentTask();
      RetrieveAnnualReviewContentTask annualReviewTask = new RetrieveAnnualReviewContentTask();

      Timer timer = new Timer("EMAILREMINDERS");
      timer.schedule(thisTask, new Date());
      timer.schedule(annualReviewTask, new Date());
      if (isDebug) {
         s_log.exiting("CheckNotificationEmail", "handleRequest");
      }

   }

}
