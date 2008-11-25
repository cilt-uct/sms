/******************************************************************************
 * SmsLogic.java - created by Sakai App Builder -AZ
 * 
 * Copyright (c) 2008 Sakai Project/Sakai Foundation
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 *****************************************************************************/

package org.sakaiproject.sms.logic;

import java.util.List;

import org.sakaiproject.sms.model.SmsItem;

/**
 * This is the interface for the app Logic, 
 * @author Sakai App Builder -AZ
 */
public interface SmsLogic {

   /**
    * This returns an item based on an id
    * @param id the id of the item to fetch
    * @return a SmsItem or null if none found
    */
   public SmsItem getItemById(Long id);

   /**
    * Check if a specified user can write this item in a specified site
    * @param item to be modified or removed
    * @param locationId a unique id which represents the current location of the user (entity reference)
    * @param userId the internal user id (not username)
    * @return true if item can be modified, false otherwise
    */
   public boolean canWriteItem(SmsItem item, String locationId, String userId);

   /**
    * This returns a List of items for a specified site that are
    * visible to the specified user
    * @param locationId a unique id which represents the current location of the user (entity reference)
    * @param userId the internal user id (not username)
    * @return a List of SmsItem objects
    */
   public List<SmsItem> getAllVisibleItems(String locationId, String userId);

   /**
    * Save (Create or Update) an item (uses the current site)
    * @param item the SmsItem to create or update
    */
   public void saveItem(SmsItem item);

   /**
    * Remove an item
    * @param item the SmsItem to remove
    */
   public void removeItem(SmsItem item);

}
