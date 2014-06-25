/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.prudential.objects;

import com.ibm.workplace.wcm.api.*;
import com.ibm.workplace.wcm.api.messaging.*;
import com.ibm.workplace.wcm.api.messaging.MessagingConstants.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.*;

/**
 *
 * @author Pete Raleigh
 */
public class WCMMessage {
	private static final Logger s_log = Logger.getLogger(WCMMessage.class.getName());
	
	private String docId;
	private String docName;
	private Date docModDate;
	private String libName;
	private String libTitle;
	private String libId;
	private String vpId;
	private String itemLoc;
	private String sourceNodeName;
	private boolean itemRenamed;
	private ItemState itemState;
	private ItemStatus itemStatus;
	private boolean IWKContentUpdate = false;
	private String atdocid = null;
	
	public WCMMessage(Message message) {
		boolean isDebug = s_log.isLoggable(Level.FINEST);
		try {
			if (message.getJMSType().equals(MessagingConstants.MessageUpdateType.IWKContentUpdate.toString())) {
				IWKContentUpdate = true;
				if (message instanceof MapMessage) {
					MapMessage mm = (MapMessage)message;
					//System.out.println("Message: " + message.toString());
					
					if (isDebug) {
						Enumeration en = mm.getMapNames();
						while (en.hasMoreElements()) {
							String key = (String)en.nextElement();
							String val = (String)mm.getString(key);
							s_log.log(Level.FINEST, "MapName:" + key + " - Val:" + val);
						}
						en = mm.getPropertyNames();
						while (en.hasMoreElements()) {
							String key = (String)en.nextElement();
							String val = (String)mm.getStringProperty(key);
							s_log.log(Level.FINEST, "Property:" + key + " - Val:" + val);
						}
					}
					
					// Construct the Document / Item ID
					String id = mm.getString(MessagingConstants.MESSAGE_PROPERTY_DOCID);
					String type = mm.getString(MessagingConstants.MESSAGE_PROPERTY_DOCTYPE);
					String name = mm.getString(MessagingConstants.MESSAGE_PROPERTY_DOCNAME);
					String status = mm.getString(MessagingConstants.MESSAGE_PROPERTY_ITEM_STATUS);
					
					if (isDebug) {
						s_log.log(Level.FINEST, "Doc Id: " + docId);
						s_log.log(Level.FINEST, "Type: " + type);
						s_log.log(Level.FINEST, "Name: " + name);
						s_log.log(Level.FINEST, "Status: " + status);
					}
					
					if (id != null && type != null && name != null && status != null) {
						// Reconstruct the DocumentId String...
						docId = type + "/" + name + "/" + id + "/" + status;
						if (isDebug) {
							s_log.log(Level.FINEST, "DocId: " + docId);
							s_log.log(Level.FINEST, "DocumentType:Content - " + DocumentTypes.Content.toString());
						}

						if (type.equals(DocumentTypes.Content.getApiType().getName())) {
							String atname = mm.getString(MessagingConstants.MESSAGE_PROPERTY_AUTHORING_TEMPLATE_NAME);
							String atid = mm.getString(MessagingConstants.MESSAGE_PROPERTY_AUTHORING_TEMPLATE_DOCID);
							atdocid = DocumentTypes.AuthoringTemplate.getApiType().getName() + "/" + atname + "/" + atid + "/PUBLISHED";
							if (isDebug) {
								s_log.log(Level.FINEST, "AT DOC ID: " + atdocid);
							}
						} else {
							if (isDebug) {
								s_log.log(Level.FINEST, "Type not a content item: " + type);
							}
						}
					}
					itemState = MessagingConstants.ItemState.valueOf(mm.getString(MessagingConstants.MESSAGE_PROPERTY_ITEM_STATE_STRING));
					docName = mm.getString(MessagingConstants.MESSAGE_PROPERTY_DOCNAME);
					itemLoc = mm.getString(MessagingConstants.MESSAGE_PROPERTY_ITEM_LOCATION);
					itemStatus = ItemStatus.valueOf(status);
					String val = mm.getString(MessagingConstants.MESSAGE_PROPERTY_DOCMODDATE);
					if (val != null) {
						docModDate = new Date(Long.valueOf(val));
					}
					
					libName = mm.getString(MessagingConstants.MESSAGE_PROPERTY_LIBRARY_NAME);
					libTitle = mm.getString(MessagingConstants.MESSAGE_PROPERTY_LIBRARY_DISPLAY_NAME);
					sourceNodeName = mm.getString(MessagingConstants.MESSAGE_PROPERTY_SOURCE_NODE_NAME);
					itemRenamed = mm.getBoolean(MessagingConstants.MESSAGE_PROPERTY_DOCUMENT_RENAMED_BOOL);
					
					vpId = mm.getString(MessagingConstants.MESSAGE_PROPERTY_VPID);
					libId = mm.getString(MessagingConstants.MESSAGE_PROPERTY_LIBRARY_ID);
				}
			}
		} catch (JMSException ex) {
			Logger.getLogger(WCMMessage.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public String getAuthoringTemplateId() {
		return atdocid;
	}
	public boolean isIWKContentUpdate() {
		return IWKContentUpdate;
	}
	public Date getDocModDate() {
		return docModDate;
	}

	public String getLibName() {
		return libName;
	}

	public String getLibTitle() {
		return libTitle;
	}

	public String getLibId() {
		return libId;
	}

	public String getItemLoc() {
		return itemLoc;
	}
	public String getDocName() {
		return docName;
	}
	public ItemStatus getItemStatus() {
		return itemStatus;
	}
	public String getLibraryName() {
		return libName;
	}
	public String getItemLocation() {
		return itemLoc;
	}
	public String getSourceNodeName() {
		return sourceNodeName;
	}
	public boolean isItemRenamed() {
		return itemRenamed;
	}
	public ItemState getItemState() {
		return itemState;
	}
	public String getDocId() {
		return docId;
	}
	public String getVpId() {
		return vpId;
	}
	public String getLibraryId() {
		return libId;
	}
}
