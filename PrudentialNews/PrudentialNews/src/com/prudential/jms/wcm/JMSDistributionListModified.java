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
public class JMSDistributionListModified {
	private static final Logger s_log = Logger.getLogger(JMSNewsletterProfileModified.class.getName());
	private static Content cont;
	
	public JMSDistributionListModified(Content content) {
		cont = content;
	}
	
	public void processChange() {
		boolean isDebug = s_log.isLoggable(Level.FINEST);
		
		// Get the Content item - the Distribution List
		// Compare the "Old" and the "Current" users / groups on the Elements
		// Record the differences to a "Changes" log
		// Update the "Old" values with the "Current"...
		if (isDebug) {
			s_log.log(Level.FINEST, "*** NEED TO PROCESS DISTRIBUTION LIST: ADD / REMOVE USERS AND GROUPS for: " + cont.getName() + " ***");
		}
	}
	
	public void processDelete() {
		boolean isDebug = s_log.isLoggable(Level.FINEST);
		
		// See TaskList: 4.2.2R3
		if (isDebug) {
			s_log.log(Level.FINEST, "*** NEED TO PROCESS DISTRIBUTION LIST DELETE for: " + cont.getName() + " ***");
		}
	}
}
