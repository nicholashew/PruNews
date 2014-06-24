package com.prudential.wcm.tasks;

import java.util.logging.Logger;

import com.prudential.wcm.DocumentHandle;

/**
 * Interface for a Runnable task. This interface adds setters for a DocumentHandle 
 * and a flag for controlling the closing of that handle after task execution
 * 
 * @author Luke Carpenter
 */
public interface WCMDocumentTask extends Runnable {

	public abstract void setHandle(DocumentHandle handle);

	public abstract void setClose(boolean close);
}