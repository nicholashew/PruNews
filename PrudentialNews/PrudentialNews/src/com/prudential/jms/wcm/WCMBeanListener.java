/*
 * asponte
 * praleigh
 */

package com.prudential.jms.wcm;

import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.Category;
import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.DocumentId;
import com.ibm.workplace.wcm.api.WCM_API;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.exceptions.AuthorizationException;
import com.ibm.workplace.wcm.api.exceptions.DocumentIdCreationException;
import com.ibm.workplace.wcm.api.exceptions.DocumentRetrievalException;
import com.ibm.workplace.wcm.api.messaging.MessagingConstants.ItemState;
import com.prudential.objects.WCMMessage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 *
 * @author Pete Raleigh
 */
@MessageDriven(mappedName = "jms/IWKTopics/Items", activationConfig = {
	@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
	@ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "NonDurable"),
	@ActivationConfigProperty(propertyName = "clientId", propertyValue = "jms/IWKTopics/Items"),
	@ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "jms/IWKTopics/Items")
})

public class WCMBeanListener implements MessageListener {
	private static final Logger s_log = Logger.getLogger(WCMBeanListener.class.getName());
	private static Workspace wksp;
	private static final String NEWSLETTERPROFILEAT = "AT - Newsletter Profile";
	private static final String DISTRIBUTIONLISTAT = "AT - Distribution List";
	
	/**
	 *
	 */
	public WCMBeanListener() {
		boolean isDebug = s_log.isLoggable(Level.FINEST);
		try {
			wksp = WCM_API.getRepository().getSystemWorkspace();
			if (isDebug) {
				s_log.log(Level.FINEST, "MDB - " + WCMBeanListener.class.getName() + " - created!");
			}
		} catch (Exception e) {
			System.err.println("MDB - " + WCMBeanListener.class.getName() + " - failed!");
		}
	}

	/**
	 * @param message
	 */
	@Override
	public void onMessage(Message message) {
		boolean isDebug = s_log.isLoggable(Level.FINEST);
		if (message == null) return;
		try {
			WCMMessage msg = new WCMMessage(message);
			if (msg.isIWKContentUpdate()) {
				String docIdS = msg.getDocId();
				if (wksp != null) {
					if (isDebug) {
						s_log.log(Level.FINEST, "Workspace is not null");
					}
					DocumentId docId = wksp.createDocumentId(docIdS);
					if (isDebug) {
						s_log.log(Level.FINEST, "Content Id: " + docIdS);
					}
					Document doc = wksp.getById(docId);
					if (isDebug) {
						s_log.log(Level.FINEST, "Workspace is not null");
						s_log.log(Level.FINEST, "Content Id: " + docIdS);
						s_log.log(Level.FINEST, "Content Name: " + docId.getName());
					}
					if (doc instanceof Content) {
						String atIdS = msg.getAuthoringTemplateId();
						if (atIdS != null) {
							if (isDebug) {
								s_log.log(Level.FINEST, "AuthTemplate Id: " + atIdS);
							}
							Content cont = (Content)doc;
							DocumentId atId = wksp.createDocumentId(atIdS);
							if (isDebug) {
								s_log.log(Level.FINEST, "Content Name: " + docId.getName());
								s_log.log(Level.FINEST, "Authoring Template Name: " + atId.getName());
							}
							if (NEWSLETTERPROFILEAT.equals(atId.getName())) {
								// Content based on the Newsletter Profile was modified ... start processing
								JMSNewsletterProfileModified profileModified = new JMSNewsletterProfileModified(cont);
								if (msg.getItemState() == ItemState.CHANGED) {
									profileModified.processCategories();
								} else if (msg.getItemState() == ItemState.REMOVED) {
									profileModified.processDelete();
								}
							} else if (DISTRIBUTIONLISTAT.equals(atId.getName())) {
								// Content based on the Distribution List was modified ... start processing
								JMSDistributionListModified dlistModified = new JMSDistributionListModified(cont);
								if (msg.getItemState() == ItemState.CHANGED) {
									dlistModified.processChange();
								} else if (msg.getItemState() == ItemState.REMOVED) {
									dlistModified.processDelete();
								}
							}
						} else {
							s_log.log(Level.WARNING, "AT ID was null.");
						}
					} else if (doc instanceof Category) {
						Category cat = (Category)doc;
						// Category was modified - check if it was MOVED
						JMSCategoryModified catModified = new JMSCategoryModified(cat);
						if (msg.getItemState() == ItemState.MOVED) {
							catModified.processMove();
						}
					}
				}
			}
		} catch (DocumentIdCreationException ex) {
			Logger.getLogger(WCMBeanListener.class.getName()).log(Level.SEVERE, null, ex);
		} catch (DocumentRetrievalException ex) {
			Logger.getLogger(WCMBeanListener.class.getName()).log(Level.SEVERE, null, ex);
		} catch (AuthorizationException ex) {
			Logger.getLogger(WCMBeanListener.class.getName()).log(Level.SEVERE, null, ex);
		}		
	}
}
