<%@ page
	import="javax.naming.InitialContext,javax.naming.NamingException"%>
<%@ page
	import="com.ibm.workplace.wcm.api.*,com.ibm.workplace.wcm.api.exceptions.*"%>
<%@ page import="com.ibm.workplace.wcm.api.query.*"%>
<%@ page
	import="com.ibm.workplace.wcm.api.query.WorkflowSelectors.Status"%>
<%@ page
	import="java.util.List,java.util.ArrayList,java.util.Iterator,java.util.Date,java.util.Calendar,java.text.SimpleDateFormat"%>

<%!public static final String PREVIEW_FLAG = "FT PREVIEW";

   public static final String DATE_FORMAT_NOW = "MM/dd/yyyy KK:mm a";

   StringBuffer rbuf = null;

   StringBuffer ebuf = null;

   boolean errFlag = false;

   WebContentService webcontentservice = null;

   Workspace workspace = null;

   DocumentIdIterator documentIds = null;

   Date curDateTime = new Date(System.currentTimeMillis());

   /**
    *	Format the current date and time for report display
    */
   private String now() {
      Calendar cal = Calendar.getInstance();
      SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
      return sdf.format(cal.getTime());
   }

   /**
    * Gets the web content service and establishes the WCM workspace
    */
   private void initializeWorkspace() {
      try {
         // Construct an initial context
         InitialContext ctx = new InitialContext();

         // Retrieve the service using the JNDI name
         webcontentservice = (WebContentService) ctx.lookup("portal:service/wcm/WebContentService");

         if (webcontentservice != null) {
            workspace = webcontentservice.getRepository().getSystemWorkspace();
         }

         if (workspace == null) {
            rbuf.append("<p><font color='red'>ERROR: Unable to retrieve the workspace").append("</font></p>\n");
         }
      }
      catch (NamingException ne) {
         rbuf.append("<p><font color='red'>ERROR: Naming Exception when getting the web content service").append(ne.getMessage()).append

         ("</font></p>\n");
      }
      catch (ServiceNotAvailableException snae) {
         rbuf.append("<p><font color='red'>ERROR: Service not available while initiating the workspace</font></p>\n");
      }
      catch (OperationFailedException ofe) {
         rbuf.append("<p><font color='red'>ERROR: Operation failed while initiating the workspace</font></p>\n");
      }
   }

   /**
    *	Publishes a WCM Document item by moving it through its' workflow 
    *	stages until it shows as PUBLISHED; returns true if the Effective
    *	Date is changed back to the current time (a.k.a. content has a future
    *	effective date).
    */
   private boolean publish(WorkflowedDocument item) {
      boolean dateChanged = false;
      try {
         boolean published = item.isPublished();
         if (!published) {
            dateChanged = pushPublishDate(item);
         }
         while (!published) {
            item.nextWorkflowStage();
            published = item.isPublished();
         }
      }
      catch (AuthorizationException ae) {
         ebuf.append("<p>Approval Authorization Exception: -[").append(((Document) item).getName() + " id: " + ((Document) item).getId())
            .append("]</p>\n");
            ebuf.append("<p>"+ae.getMessage()).append("</p>\n");
         errFlag = true;
      }
      catch (OperationFailedException ofe) {
         ebuf.append("<p>Approval Operation Failed Exception: -[")
            .append(((Document) item).getName() + " id: " + ((Document) item).getId()).append("]</p>\n");
            ebuf.append("<p>"+ofe.getMessage()).append("</p>\n");
         errFlag = true;
      }
      catch (NoMoreWorkflowStagesException nmwse) {
         ebuf.append("<p>Approval No More Workflow Exception: -[")
            .append(((Document) item).getName() + " id: " + ((Document) item).getId()).append("]</p>\n");
            ebuf.append("<p>"+nmwse.getMessage()).append("</p>\n");
         errFlag = true;
      }
      catch (PropertyRetrievalException pre) {
         ebuf.append("<p>Approval Property Retrieval Exception (isPublished): -[")
            .append(((Document) item).getName() + " id: " + ((Document) item).getId()).append("]</p>\n");
            ebuf.append("<p>"+pre.getMessage()).append("</p>\n");
         errFlag = true;
      }
      return dateChanged;
   }

   /**
    *	Check & change if neccesary to make sure the published date is not in the future.
    */
   private boolean pushPublishDate(WorkflowedDocument item) {
      boolean changed = false;
      try {
         if (curDateTime.before(item.getEffectiveDate())) {
            item.setEffectiveDate(curDateTime);
            workspace.save(((Document) item));
            changed = true;
         }
      }
      catch (PropertyRetrievalException pre) {
         ebuf.append("<p>Property Retrieval Exception (get effective date): -[")
            .append(((Document) item).getName() + " id: " + ((Document) item).getId()).append("]</p>\n");
         errFlag = true;
      }
      catch (OperationFailedException ofe) {
         ebuf.append("<p>Operation Failed Exception (set effective date): -[")
            .append(((Document) item).getName() + " id: " + ((Document) item).getId()).append("]</p>\n");
         errFlag = true;
      }
      catch (DocumentSaveException dse) {
         ebuf.append("<p>Document Save Exception (set effective date): -[")
            .append(((Document) item).getName() + " id: " + ((Document) item).getId()).append("]</p>\n");
         errFlag = true;
      }
      catch (AuthorizationException ae) {
         ebuf.append("<p>Authorization Exception (set effective date): -[")
            .append(((Document) item).getName() + " id: " + ((Document) item).getId()).append("]</p>\n");
         errFlag = true;
      }
      catch (DuplicateChildException dce) {
         ebuf.append("<p>Duplicate Child Exception (set effective date): -[")
            .append(((Document) item).getName() + " id: " + ((Document) item).getId()).append("]</p>\n");
         errFlag = true;
      }
      return changed;
   }%>
<%
   rbuf = new StringBuffer("<b>Publishing or Verifying all objects to LIVE state:</b>\n");
   rbuf.append("<br/>").append(now()).append("<br/>\n");

   ebuf = new StringBuffer();
   errFlag = false;

   try {

      System.out.println("Publish preview JSP starting");

      // get the jvm property
      String isTest = System.getProperty("com.oafp.aul.env.server");
      out.print("isTest = " + isTest+"<br>");

      boolean isTestBool = isTest.equalsIgnoreCase("systest");
      // don't run if it's not test
      if (!isTestBool) {
		out.print("don't run, not in test"+"<br>");
      }
      else {
         initializeWorkspace();
         QueryService queryService = null;
         Query query = null;
         if (workspace != null) {
            // perform login
            workspace.login();
            // now do a query to get all draft items
            queryService = workspace.getQueryService();
            query = queryService.createQuery();
            Iterator allLibs = workspace.getDocumentLibraries();
            ArrayList libMap = new ArrayList();
            while (allLibs.hasNext()) {
               Library tempLib = (Library) allLibs.next();
               if (!tempLib.getName().equalsIgnoreCase("oacontent") || !tempLib.getName().equalsIgnoreCase("oadesign")
                  || !tempLib.getName().contains("Pru")) {
                  libMap.add(tempLib);
               }
            }
            //Selector libSelector = Selectors.libraryEquals(docLib);
            Selector libSelector = Selectors.libraryIn(libMap);
            Selector draft = WorkflowSelectors.statusEquals(Status.DRAFT);
            Selector contentType = Selectors.typeEquals(Content.class);
            query.addSelector(draft);
            //query.addSelector(contentType);

            // now iterate and publish
            System.out.println("Before Query Execute");
            ResultIterator queryResults = queryService.execute(query);
            System.out.println("After Query Execute");
            while (queryResults.hasNext()) {
               Document tempItem = (Document) queryResults.next();
               Library tempLibrary = tempItem.getId().getContainingLibrary();
               String docName = tempItem.getName();
               System.out.println("Before publish Execute");
               boolean wasPublished = publish((WorkflowedDocument) tempItem);
               System.out.println("After publish Execute");
               rbuf.append("<br><b>Item: ").append(docName)
                  .append("</b> in library " + tempLibrary.getName() + " published " + wasPublished);
            }

            //}

            // perform logout
            workspace.logout();

            // end workspace
            webcontentservice.getRepository().endWorkspace();
         }
      }

   }
   catch (Exception ex) {
      rbuf.append("<font color='red'> Workflow Stage Exception encountered: ").append(ex.getMessage()).append("</font>\n");
      ex.printStackTrace(new java.io.PrintWriter(System.out));
   }

   if (errFlag) {
      rbuf.append("<br/><hr>\n");
      rbuf.append("<p><b><font color='red'>The following errors were encountered during processing:</font></b><br/>\n").append(
         ebuf.toString());
   }

   // write response string buffer to out
   out.write(rbuf.toString());

   System.out.println("Publish preview JSP completed");
   // cleanup
   workspace = null;
   webcontentservice = null;
%>
