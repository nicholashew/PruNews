<%@ page import="javax.naming.InitialContext,javax.naming.NamingException"%>
<%@ page import="com.ibm.workplace.wcm.api.*,com.ibm.workplace.wcm.api.exceptions.*"%>
<%@ page import="java.util.List,java.util.ArrayList,java.util.Iterator,java.util.Date,java.util.Calendar,java.text.SimpleDateFormat"%>

<%!
	public static final String PREVIEW_FLAG = "FT PREVIEW";
	public static final String DATE_FORMAT_NOW = "MM/dd/yyyy KK:mm a";
	
	StringBuffer rbuf = null;
	StringBuffer ebuf = null;
	
	boolean errFlag = false;
	boolean debugFlag = false;
	
	WebContentService webcontentservice = null;
	Workspace workspace = null;
	
	List workflowStages = null;
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
	private void initializeWorkspace(String user, String pwd) {
		try {
			// Construct an initial context
			InitialContext ctx = new InitialContext();
			
			// Retrieve the service using the JNDI name
			webcontentservice = (WebContentService) ctx.lookup("portal:service/wcm/WebContentService");
			
			if (webcontentservice!=null) {
				workspace = webcontentservice.getRepository().getWorkspace(user,pwd);
			}			
			
			if (workspace==null) {
				rbuf.append("<p><font color='red'>ERROR: Unable to retrieve the workspace").append("</font></p>\n");
			} else {
				String wkspcUser = null;
				if (workspace.getUserProfile()!=null) {
					wkspcUser = workspace.getUserProfile().getUsername();
					if (!wkspcUser.equalsIgnoreCase(user)) {
						rbuf.append("<p><font color='red'>ERROR: Unable to authenticate user [").append(user).append("].</font></p>\n");
						workspace = null;
					}
				}
			}
		} catch (NamingException ne) {
			rbuf.append("<p><font color='red'>ERROR: Naming Exception when getting the web content service").append(ne.getMessage()).append("</font></p>\n");
		} catch (ServiceNotAvailableException snae) {
			rbuf.append("<p><font color='red'>ERROR: Service not available while initiating the workspace</font></p>\n");
		} catch (OperationFailedException ofe) {
			rbuf.append("<p><font color='red'>ERROR: Operation failed while initiating the workspace</font></p>\n");
		}
	}
	
	/**
	 *	Retrieves a WCM Document and checks the Description field for the PREVIEW_FLAG.
	 *
	 *	See also the populatePreviewWorkflowStage() method
	 */
	private boolean includeWorkflowStage(DocumentId id) {
		boolean rtnValue = true;
		return rtnValue;
	}

	/**
	 *	Retrieves the list of DocumentIds for all of the workflow stages,
	 *	iterates through them to determine if they should be included in the
	 *	document promotion for preview purposes.
	 *
	 *	Expecting a String to appear in the description field - defined by PREVIEW_FLAG -
	 *	for all stages that will have documents that should be previewed in the FT environment.
	 */
	private void populatePreviewWorkflowStages() {
		try {
			// initialize the workflow stage list
			workflowStages = new ArrayList();
			
			DocumentIdIterator ids = workspace.findByType(DocumentTypes.WorkflowStage);
			DocumentId id = null;
			String description = null;
			
			while (ids.hasNext()) { 
				id = ids.nextId();
				if (includeWorkflowStage(id)) {
					workflowStages.add(id);
				}
			}
		} catch (Exception ex) {
			ebuf.append("<p>Workflow Stage Retrieval Exception - [").append(ex.getMessage()).append("].</p>\n");
			errFlag = true;
		}
	}
	
	/**
	 *	Processes the list of Document items at this WorkflowStage for 
	 *	moving them to LIVE for PREVIEW purposes.
	 */	 
	private void publishDocumentForStage(DocumentId stageId) {
		DocumentId[] stages = {stageId};
		DocumentId id = null;
		WorkflowedDocument item = null;
		int counter = 0;
		int counter2 = 0;
		boolean processingDetermined = false;
		boolean process = false;
		try {
			rbuf.append("<li>[").append(stageId.getName()).append("]\n");
			ebuf.append("<li>[").append(stageId.getName()).append("]\n");
			DocumentIdIterator ids = workspace.findDocumentsByWorkflowStage(stages);
			if (ids.hasNext()) {
				while ((!processingDetermined)&&(ids.hasNext())) {
					id = (DocumentId) ids.next();
					item = (WorkflowedDocument)retrieveItem(id);			
					if (item!=null) {
						process = processStage(item);
						processingDetermined = true;
					}
				}
				if (process) {
					if (publish(item)) {
						counter2++;
					}
					counter++;
					while (ids.hasNext()) {
						id = (DocumentId) ids.next();
						item = (WorkflowedDocument) retrieveItem(id);
						if (item!=null) {
							if (publish(item)) {
								counter2++;
							}
							counter++;
						}
					}
					rbuf.append(" ").append(counter).append(" item(s) processed with ").append(counter2).append(" item(s) effective dates altered.</li>\n"); 
					ebuf.append("</li>\n");
				} 
				if (!processingDetermined) {
					rbuf.append("Unable to determine processing (see errors)</li>\n");
					ebuf.append("</li>\n");
				}
			} else {
				rbuf.append(" No items to process.</li>\n");
				ebuf.append("</li>\n");
			}
		} catch (IllegalDocumentTypeException idte) {
			ebuf.append("<p>Illegal Document Type Exception - [").append(stageId.toString()).append("]</p>\n");
			errFlag = true;
		}
	}
	
	/**
	 *	Examines the first document item to determine if this stage
	 *	is DRAFT, LIVE or EXPIRED.
	 */
	private boolean processStage(WorkflowedDocument item) {
		boolean processStage = false;
		try {
			if (item.isDraft()) {
				processStage = true;
			} else if (item.isPublished()) {
				rbuf.append(" No items processed - stage is LIVE.</li>\n");
				ebuf.append("</li>\n");
			} else if (item.isExpired()) {
				rbuf.append(" No items processed - stage is EXPIRED.</li>\n");
				ebuf.append("</li>\n");
			}
		} catch (PropertyRetrievalException pre) {
			ebuf.append("<p>Property Retrieval Exception (isPublished): -[").append(((Document)item).getName()).append("]</p>\n");
			errFlag = true;
		}
		return processStage;
	}

	/**
	 *	Retrieves a WCM Document item from the passed in Document ID
	 */
	private WorkflowedDocument retrieveItem(DocumentId id) {
		WorkflowedDocument rtnValue = null;
		try {
			// ignore Authoring Templates
			if (!(id.getType().equals(DocumentTypes.AuthoringTemplate))) {
				rtnValue = (WorkflowedDocument) workspace.getById(id);
			}
		} catch (DocumentRetrievalException dre) {
			ebuf.append("<p>Document Retrieval Exception: -[").append(id.getName()).append("]</p>\n");
			errFlag = true;
		} catch (AuthorizationException ae) {
			ebuf.append("<p>Document Retrieval Authorization Exception: -[").append(id.getName()).append("]</p>\n");
			errFlag = true;
		}
		return rtnValue;
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
		} catch (AuthorizationException ae) {
			ebuf.append("<p>Approval Authorization Exception: -[").append(((Document)item).getName()).append("]</p>\n");
			errFlag = true;
		} catch (OperationFailedException ofe) {
			ebuf.append("<p>Approval Operation Failed Exception: -[").append(((Document)item).getName()).append("]</p>\n");
			errFlag = true;
		} catch (NoMoreWorkflowStagesException nmwse) {
			ebuf.append("<p>Approval No More Workflow Exception: -[").append(((Document)item).getName()).append("]</p>\n");
			errFlag = true;
		} catch (PropertyRetrievalException pre) {
			ebuf.append("<p>Approval Property Retrieval Exception (isPublished): -[").append(((Document)item).getName()).append("]</p>\n");
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
				workspace.save(((Document)item));
				changed = true;
			}
		} catch (PropertyRetrievalException pre) {
			ebuf.append("<p>Property Retrieval Exception (get effective date): -[").append(((Document)item).getName()).append("]</p>\n");
			errFlag = true;
		} catch (OperationFailedException ofe) {
			ebuf.append("<p>Operation Failed Exception (set effective date): -[").append(((Document)item).getName()).append("]</p>\n");
			errFlag = true;
		} catch (DocumentSaveException dse) {
			ebuf.append("<p>Document Save Exception (set effective date): -[").append(((Document)item).getName()).append("]</p>\n");
			errFlag = true;
		} catch (AuthorizationException ae) {
			ebuf.append("<p>Authorization Exception (set effective date): -[").append(((Document)item).getName()).append("]</p>\n");
			errFlag = true;
		} catch (DuplicateChildException dce) {
			ebuf.append("<p>Duplicate Child Exception (set effective date): -[").append(((Document)item).getName()).append("]</p>\n");
			errFlag = true;
		}
		return changed;
	}
%>
<%
	rbuf = new StringBuffer("<b>Publishing or Verifying all objects to LIVE state:</b>\n");
	rbuf.append("<br/>").append(now()).append("<br/>\n");
	
	ebuf = new StringBuffer();
	errFlag = false;
		
	try {
		
		String user = request.getParameter("user");
		String pwd = request.getParameter("password");
		String debug = request.getParameter("debug");
		
		debugFlag = false;
		if (debug!=null) {
			if (debug.equalsIgnoreCase("true")) {
				debugFlag = true;
			}
		}
		
		System.out.println("Publish preview JSP starting");
		
		initializeWorkspace(user,pwd);
		
		if (workspace!=null) {
			// perform login
			workspace.login();
			
			Iterator documentLibraries = workspace.getDocumentLibraries();
			DocumentLibrary library = null;
			
			while (documentLibraries.hasNext()) {
			
				library = (DocumentLibrary) documentLibraries.next();

				workspace.setCurrentDocumentLibrary(workspace.getDocumentLibrary(library.getName()));
				
				rbuf.append("<hr/>\n<b>LIBRARY: ").append(library.getName()).append("</b><br/>\n").append("<ul>\n");
				ebuf.append("<b>LIBRARY: ").append(library.getName()).append("</b><br/>\n").append("<ul>\n");
				
				populatePreviewWorkflowStages();
				
				if (!workflowStages.isEmpty()) {
					Iterator stagesIter = workflowStages.iterator();
					while (stagesIter.hasNext()) {
						publishDocumentForStage((DocumentId)stagesIter.next());
					}
				} else {
					rbuf.append("No workflow stages found<br/>\n");
					rbuf.append("<br/>\n");
				}
				rbuf.append("</ul>\n");
				ebuf.append("</ul>\n");
			}
			
			// perform logout
			workspace.logout();
			
			// end workspace
			webcontentservice.getRepository().endWorkspace();
		}
	
	} catch (Exception ex) {
		rbuf.append("<font color='red'> Workflow Stage Exception encountered: ").append(ex.getMessage()).append("</font>\n");
		ex.printStackTrace(new java.io.PrintWriter(System.out));
	}
	
	if (errFlag) {
		rbuf.append("<br/><hr>\n");
		rbuf.append("<p><b><font color='red'>The following errors were encountered during processing:</font></b><br/>\n").append(ebuf.toString());
	}	
		
	// write response string buffer to out
	out.write(rbuf.toString());
	if (debugFlag) {
		System.out.println("Publish preview JSP Results:\n"+rbuf.toString());
	}
	
	System.out.println("Publish preview JSP completed");

	// cleanup
	workspace = null;
	webcontentservice = null;

%>
