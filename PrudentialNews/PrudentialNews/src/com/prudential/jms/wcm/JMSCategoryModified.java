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
public class JMSCategoryModified {
	private static final Logger s_log = Logger.getLogger(JMSNewsletterProfileModified.class.getName());
	private static Category cont;
	
	public JMSCategoryModified(Category content) {
		cont = content;
	}
	
	public void processMove() {
		boolean isDebug = s_log.isLoggable(Level.FINEST);
		
		// Nothing to do		
		if (isDebug) {
			s_log.log(Level.FINEST, "*** NEED TO PROCESS CATEGORY: MOVE DETECTED for: " + cont.getName() + " ***");
		}
	}
}
