/******************************************************************************
 * SmsBean.java - created by Sakai App Builder -AZ
 * 
 * Copyright (c) 2008 Sakai Project/Sakai Foundation
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 *****************************************************************************/

package org.sakaiproject.sms.tool;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.sms.logic.ExternalLogic;
import org.sakaiproject.sms.logic.SmsLogic;
import org.sakaiproject.sms.model.SmsItem;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

/**
 * This is the backing bean for actions related to SmsItems
 * @author Sakai App Builder -AZ
 */
public class ItemsBean {
	
	private static Log log = LogFactory.getLog(ItemsBean.class);

	private final Boolean DEFAULT_HIDDEN = Boolean.TRUE;

	public SmsItem newItem = new SmsItem();
	public Map<String, Boolean> selectedIds = new HashMap<String, Boolean>();

	private SmsLogic logic;
	public void setLogic(SmsLogic logic) {
		this.logic = logic;
	}

	private ExternalLogic externalLogic;
	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	private TargettedMessageList messages;
	public void setMessages(TargettedMessageList messages) {
		this.messages = messages;
	}

	public void init() {
		log.debug("init");
	}

	public ItemsBean() {
		log.debug("constructor");
	}

	public String processActionAdd() {
		log.debug("in process action add, title=" + newItem.getTitle());
		if (newItem.getId() == null) {
			// Test for empty items and don't add them
			if (newItem.getTitle() == null || newItem.getTitle().equals("")) {
				String message = "Cannot create item, item title is required";
		        messages.addMessage(new TargettedMessage("title_required",
		                new Object[] { newItem.getTitle() }, 
		                TargettedMessage.SEVERITY_ERROR));
				log.error(message);
				throw new IllegalArgumentException("sms_add_error");
			} else {
				// create a new item (will be set to the current user and site by default)
				if (newItem.getHidden() == null) {
					// null here means that the box was not checked
					newItem.setHidden( DEFAULT_HIDDEN );
				}
				logic.saveItem(newItem);
				String message = "Added new item: " + newItem.getTitle();
		        messages.addMessage(new TargettedMessage("item_added",
		                new Object[] { newItem.getTitle() }, 
		                TargettedMessage.SEVERITY_INFO));
				log.debug(message);
			}
		} else {
			// load in the item from the DB, update the changed fields, and save it
			SmsItem updateItem = logic.getItemById( newItem.getId() );

			// the item values will be null if the items have not changed
			boolean updateThis = false;
			if (newItem.getTitle() != null) {
				updateItem.setTitle( newItem.getTitle() );
				updateThis = true;
			}
			if (newItem.getHidden() != null) {
				updateItem.setHidden( newItem.getHidden() );
				updateThis = true;
			}
			if (updateThis) {
				// save the item if any fields actually changed
				logic.saveItem(updateItem);
				String message = "Updated item: " + updateItem.getTitle();
		        messages.addMessage(new TargettedMessage("item_updated",
		                new Object[] { updateItem.getTitle() }, 
		                TargettedMessage.SEVERITY_INFO));
				log.debug(message);
			}
		}
		return "added";
	}

	public String processActionDelete() {
		log.debug("in process action delete...");
		List<SmsItem> items = logic.getAllVisibleItems(externalLogic.getCurrentLocationId(), externalLogic.getCurrentUserId());
		int itemsRemoved = 0;
		for (Iterator<SmsItem> iter = items.iterator(); iter.hasNext(); ) {
			SmsItem item = iter.next();
			log.debug("Checking to remove item:" + item.getId());
			if (selectedIds.get(item.getId().toString()) == Boolean.TRUE) {
				logic.removeItem(item);
				itemsRemoved++;
				log.debug("Removing item:" + item.getId());
			}
		}
		String message = "Removed " + itemsRemoved + " items";
	   messages.addMessage(new TargettedMessage("items_removed",
	            new Object[] { new Integer(itemsRemoved) },
	            TargettedMessage.SEVERITY_INFO));
		log.info(message);
		return "deleteItems";
	}

}
