/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.prudential.jms.wcm;

import com.ibm.workplace.wcm.api.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pete Raleigh
 */
public class JMSNewsletterProfileModified {
	private static final Logger s_log = Logger.getLogger(JMSNewsletterProfileModified.class.getName());
	private static Content cont;
	
	public JMSNewsletterProfileModified(Content content) {
		cont = content;
	}
	
	public void processCategories() {
		boolean isDebug = s_log.isLoggable(Level.FINEST);
		
		// Get the Content item - the Profile
		// Check if a Newsletter exists
		// Perform a Content search to see if any News items exist
		// For each Newsletter, add the above News item links.
		// Need to determine how the Categories are stored... and process accordingly...
		if (isDebug) {
			s_log.log(Level.FINEST, "*** NEED TO PROCESS ADD / REMOVE CATEGORIES for: " + cont.getName() + " ***");
		}
	}
	
	public void processDelete() {
		boolean isDebug = s_log.isLoggable(Level.FINEST);
		
		// See TaskList: 4.1.2R3:1
		if (isDebug) {
			s_log.log(Level.FINEST, "*** NEED TO PROCESS PROFILE DELETE for: " + cont.getName() + " ***");
		}
	}
}
