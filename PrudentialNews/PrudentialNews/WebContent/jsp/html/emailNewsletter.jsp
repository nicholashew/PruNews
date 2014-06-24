<%@ page import="javax.naming.InitialContext,javax.naming.NamingException"%>
<%@ page import="java.util.*"%>
<%@ page import="java.util.logging.Level"%>
<%@ page import="java.util.logging.Logger"%>
<%@ page import="com.ibm.workplace.wcm.api.*,com.ibm.workplace.wcm.api.exceptions.*"%>
<%@ page import="com.prudential.utils.Utils"%>
<%@ page import="com.prudential.wcm.WCMUtils"%>
<%@ page import="java.security.Principal"%>
<%@ page import="com.ibm.portal.um.*"%>

<%@ taglib uri="/WEB-INF/tld/wcm.tld" prefix="wcm"%>

<%! 
   private static final Logger s_log = Logger.getLogger("com.prudential");
 /**
    * 
    * getEmailAddresses get all the recipients.  Have to retrieve the distribution list content
    * @param theContent
    * @param ws
    * @return
    */
   public ArrayList getEmailAddresses(Content theContent, Workspace ws) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      ArrayList returnList = new ArrayList();
      StringBuffer returnAddresses = new StringBuffer();
      try {
         // have to get the distribution list contents included in the list
         if (theContent.hasComponent("Distribution Lists")) {
            HTMLComponent distList = (HTMLComponent) theContent.getComponent("Distribution Lists");
            // this will contain ; delimited list of dist lists for the newsletter
            String list = distList.getHTML();
            String[] contents = list.split(";");
            if (isDebug) {
               s_log.log(Level.FINEST, "Retrieved Distribution Lists from content " + list);
            }
            for (int x = 0; x < contents.length; x++) {
               if (isDebug) {
                  s_log.log(Level.FINEST, "Checking for content " + contents[x]);
               }
               DocumentId distListContentId = Utils.getContentIdByName(ws, contents[x], "PrudentialNewsContent");
               if (distListContentId != null) {
                  // now, get the emails from the content
                  Content distContent = (Content) ws.getById(distListContentId);
                  if (distContent != null) {
                     // get the user
                     if (distContent.hasComponent("Users")) {
                        if (isDebug) {
                           s_log.log(Level.FINEST, "Checking Users");
                        }
                        UserSelectionComponent users = (UserSelectionComponent) distContent.getComponentByReference("Users");
                        Principal[] principals = users.getSelections();
                        boolean oldValue = ws.isDistinguishedNamesUsed();
                        if (principals != null) {
                           for (int p = 0; p < principals.length; p++) {
                              // get the principal, and get the unique email addresses from it.  May be multiples
                              // check for authors/owners group
                              Principal currentPrincipal = principals[p];
                              if (currentPrincipal.getName().equalsIgnoreCase("[authors]")) {
                                 // set workspace to return full dn values so we can
                                 // retrieve the user by dn                              
                                 ws.useDistinguishedNames(true);
                                 String authors[] = theContent.getAuthors();
                                 for (int a = 0; a < authors.length; a++) {
                                    User theUser = Utils.getUserByDN(authors[a]);
                                    if (theUser != null) {
                                       returnList.addAll(Utils.getEmailsUser(theUser));
                                    }
                                    // else, try group
                                    else {
                                       // try to get a group
                                       Group theGroup = Utils.getGroupByDistinguishedName(authors[a]);
                                       returnList.addAll(Utils.getEmailsGroup(theGroup));
                                    }
                                 }
                                 ws.useDistinguishedNames(oldValue);
                              }
                              else if (currentPrincipal.getName().equalsIgnoreCase("[owners]")) {
                                 ws.useDistinguishedNames(true);
                                 String owners[] = theContent.getOwners();
                                 for (int a = 0; a < owners.length; a++) {
                                    User theUser = Utils.getUserByDN(owners[a]);
                                    returnList.add(Utils.getEmailAddressFromUser(theUser));
                                 }
                                 ws.useDistinguishedNames(oldValue);
                              }
                              else {
                                 User theUser = Utils.getUserByDN(currentPrincipal.getName());
                                 returnList.addAll(Utils.getEmails(currentPrincipal));
                              }

                           }
                        }

                     }
                     if (distContent.hasComponent("Additional Email Addresses")) {
                        if (isDebug) {
                           s_log.log(Level.FINEST, "Checking for Additional Email Addresses");
                        }

                        ShortTextComponent distListEmailAddys = (ShortTextComponent) distContent.getComponent("Additional Email Addresses");
                        String addresses[] = distListEmailAddys.getText().split(";");
                        for (int y = 0; y < addresses.length; y++) {
                           returnList.add(addresses[y]);
                           if (isDebug) {
                              s_log.log(Level.FINEST, "Adding email address " + addresses[y]);
                           }
                        }
                     }

                  }
               }
            }
         }
      }
      catch (Exception e) {
         if (isDebug) {
            s_log.log(Level.FINEST, "Exception occured " + e);
            e.printStackTrace();
         }
      }

      if (returnList.isEmpty()) {
         returnList.add("chris.knight@asponte.com");
      }
      return returnList;
   }

%>
<%
	RenderingContext rc = (RenderingContext) pageContext.getRequest().getAttribute(Workspace.WCM_RENDERINGCONTEXT_KEY);
	if(rc != null) {
		System.out.println("rc != null");
	} 
	Content incoming = rc.getContent();	
	System.out.println(incoming.getName());
	// get a system workspace
	Workspace ws = null;
	WebContentService webcontentservice = null;
	try {
			// Construct an initial context
			InitialContext ctx = new InitialContext();
			
			// Retrieve the service using the JNDI name
			webcontentservice = (WebContentService) ctx.lookup("portal:service/wcm/WebContentService");
			
			if (webcontentservice!=null) {
				ws = webcontentservice.getRepository().getSystemWorkspace();
			}			
    }
    finally {
    
    }			
    if(ws != null) {
    	// get the newsletter email references
    	rc.setPresentationTemplateOverride("");
    	String renderedContent = ws.render(rc);
    	System.out.println("Actual content = "+renderedContent);
    	// now, get the distribution emails
    	//ArrayList emailArray = getEmailAddresses(incoming, ws);
    	//Iterator emails = emailArray.iterator();
    	//while(emails.hasNext()) {
    	//	System.out.println("emails contain "+emails.next());
    	//}
    }
%>
