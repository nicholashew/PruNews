package com.prudential.wcm.tasks;

import java.util.logging.Logger;

import com.ibm.workplace.wcm.api.Document;
import com.prudential.wcm.DocumentHandle;

public abstract class WCMDocumentTaskImpl implements WCMDocumentTask {
	private static Logger log = Logger.getLogger(WCMDocumentTaskImpl.class.getName());
	private DocumentHandle handle; 
	private boolean close;

	public void setHandle(DocumentHandle handle) {
		this.handle = handle;
	}


	public void setClose(boolean close) {
		this.close = close;
	}
	
	public void run() {
		log.entering("WCMDocumentTaskImpl", "run");
		try {
			handle.init();
			this.process(handle.getDocument());
		} catch(Exception ex) {
			// TODO: error handling :)
		} finally {
			if(close) {
				handle.close();
			}
		}
	}
	
	public abstract void process(Document doc);
}
