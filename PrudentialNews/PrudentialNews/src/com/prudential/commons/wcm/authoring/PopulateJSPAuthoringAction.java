/********************************************************************/
/* Asponte
 /* cmknight
 /********************************************************************/

package com.prudential.commons.wcm.authoring;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;

import com.ibm.portal.ListModel;
import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.DocumentTypes;
import com.ibm.workplace.wcm.api.HTMLComponent;
import com.ibm.workplace.wcm.api.Item;
import com.ibm.workplace.wcm.api.JSPComponent;
import com.ibm.workplace.wcm.api.LibraryShortTextComponent;
import com.ibm.workplace.wcm.api.ShortTextComponent;
import com.ibm.workplace.wcm.api.WebContentService;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.extensions.authoring.ActionResult;
import com.ibm.workplace.wcm.api.extensions.authoring.AuthoringAction;
import com.ibm.workplace.wcm.api.extensions.authoring.FormContext;
import com.ibm.workplace.wcm.api.query.Query;
import com.ibm.workplace.wcm.api.query.QueryService;
import com.ibm.workplace.wcm.api.query.ResultIterator;
import com.ibm.workplace.wcm.api.query.Selector;
import com.ibm.workplace.wcm.api.query.Selectors;

public class PopulateJSPAuthoringAction implements AuthoringAction {

	/** Logger for the class */
	private static Logger s_log = Logger
			.getLogger(PopulateJSPAuthoringAction.class.getName());

	private static String DESC = "PopulateJSPAuthoringAction used to copy html to jsp";
	private static String TITLE = "PopulateJSPAuthoringAction";

	/**
	 * @see com.ibm.portal.Localized#getDescription(java.util.Locale)
	 */
	@Override
	public String getDescription(Locale p_arg0) {

		boolean isDebug = s_log.isLoggable(Level.FINEST);
		if (isDebug) {
			s_log.exiting("PopulateJSPAuthoringAction",
					"getDescription returning " + DESC);
		}

		return DESC;
	}

	/**
	 * @see com.ibm.portal.Localized#getTitle(java.util.Locale)
	 */
	@Override
	public String getTitle(Locale p_arg0) {
		boolean isDebug = s_log.isLoggable(Level.FINEST);
		if (isDebug) {
			s_log.exiting("PopulateJSPAuthoringAction", "getTitle returning "
					+ TITLE);
		}
		return TITLE;
	}

	/**
	 * @see com.ibm.workplace.wcm.api.extensions.authoring.AuthoringAction#execute(com.ibm.workplace.wcm.api.extensions.authoring.FormContext)
	 */
	@Override
	public ActionResult execute(FormContext fc) {
		boolean isDebug = s_log.isLoggable(Level.FINEST);
		if (isDebug) {
			s_log.entering("PopulateJSPAuthoringAction", "execute " + fc);
		}

		Content theContent = (Content) fc.document();
		// now, get the contents of the html
		try {
			// Construct an initial Context
			InitialContext ctx = new InitialContext();

			// Retrieve WebContentService using JNDI name
			WebContentService webContentService = (WebContentService) ctx
					.lookup("portal:service/wcm/WebContentService");


			if (theContent.hasComponent("JSP Text")) {
				HTMLComponent theHTML = (HTMLComponent) theContent
						.getComponentByReference("JSP Text");
				String theText = theHTML.getHTML();
				// now get the file
				// if it doesn't exist, create it. First, have to retrieve the
				// path from the content which will be store as text in fileSystemPath
				String fsPath = "";
				ShortTextComponent stc = (ShortTextComponent)theContent.getComponentByReference("fileSystemPath");
				if(stc != null)
				{
					// now get the jsp path
					JSPComponent theJsp = (JSPComponent)theContent.getComponentByReference("JSP");
					if(theJsp != null)
					{
						String[] jspSplit = theJsp.getJspPath().split(";");
						// split it on ;
						fsPath = stc.getText()+jspSplit[1];
						
						FileOutputStream jspStream = null;
						try
						{
							if (isDebug) {
								s_log.log(Level.FINEST," about to write file to "+fsPath);
								s_log.log(Level.FINEST," writing "+theText);
							}
							StringBuffer fileOutput = new StringBuffer();
							File myJspFile = new File(fsPath);
							jspStream = new FileOutputStream(myJspFile, false); // true to append
							                                                                 // false to overwrite.
							byte[] myBytes = theText.getBytes();
							jspStream.write(myBytes);
							jspStream.close();
							
						}
						catch(Exception e)
						{
							if(isDebug)
							{
								e.printStackTrace();
							}
						}
						finally
						{
							// close the file output stream
							if(jspStream != null)
							{
								jspStream.close();
							}
						}
					}
				}				
			}
		} catch (Exception e) {
			if (isDebug) {
				e.printStackTrace();
			}
		}

		if (isDebug) {
			s_log.exiting("PopulateJSPAuthoringAction", "execute " + fc);
		}
		return null;
	}

	/**
	 * @see com.ibm.workplace.wcm.api.extensions.authoring.AuthoringAction#isValidForForm(com.ibm.workplace.wcm.api.extensions.authoring.FormContext)
	 */
	@Override
	public boolean isValidForForm(FormContext fc) {
		boolean isDebug = s_log.isLoggable(Level.FINEST);
		boolean isValid = false;

		if (isDebug) {
			s_log.entering("PopulateJSPAuthoringAction", "isValidForForm " + fc);
		}
		Document theDoc = fc.document();
		if (theDoc instanceof Content) {
			Content theContent = (Content) theDoc;
			if (theContent.hasComponent("JSP")
					&& theContent.hasComponent("JSP Text")) {
				isValid = true;
			}
		}
		if (isDebug) {
			s_log.entering("PopulateJSPAuthoringAction",
					"isValidForForm returning " + isValid);
		}
		return isValid;
	}

	/**
	 * @see com.ibm.workplace.wcm.api.extensions.authoring.AuthoringAction#ordinal()
	 */
	@Override
	public int ordinal() {
		return 0;
	}

	public ListModel<Locale> getLocales() {
		return null;
	}
}
