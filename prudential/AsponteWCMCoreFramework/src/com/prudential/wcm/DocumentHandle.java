package com.prudential.wcm;

import java.util.logging.Logger;

import javax.naming.NamingException;

import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.DocumentId;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.exceptions.WCMException;
import com.prudential.utils.Utils;

/**
 * An abstraction of handling a WCM Document
 * 
 * @author Luke Carpenter
 */
public class DocumentHandle {
	private static Logger log = Logger.getLogger(DocumentHandle.class.getName());
	private String docLibId = null;
	private String contentId = null;
	private Workspace ws = null;
	private Document doc = null;
	
	public DocumentHandle(String contentId, String docLibId) {
		super();
		this.contentId = contentId;
		this.docLibId = docLibId;
	}

	public DocumentHandle(Document doc) {
		super();
		this.doc = doc;
	}
	
	public Document getDocument() {
		return doc;
	}
	
	public void init() throws WCMException, NamingException {
		if(docLibId != null && contentId != null && doc == null) {
			ws = Utils.getSystemWorkspace();
			ws.login();
			ws.setCurrentDocumentLibrary(ws.getDocumentLibrary(docLibId));
			DocumentId docId = ws.createDocumentId(contentId);
			doc = ws.getById(docId);
		}
	}
	
	public void close() {
		if(ws != null) {
			ws.logout();
			doc = null;
			ws = null;
		}
	}
}
