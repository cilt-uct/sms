/******************************************************************************
 * SmsLogicImpl.java - created by Sakai App Builder -AZ
 * 
 * Copyright (c) 2008 Sakai Project/Sakai Foundation
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 *****************************************************************************/

package org.sakaiproject.sms.logic;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.genericdao.api.search.Search;

import org.sakaiproject.sms.logic.ExternalLogic;
import org.sakaiproject.sms.dao.SmsDao;
import org.sakaiproject.sms.logic.SmsLogic;
import org.sakaiproject.sms.model.SmsItem;

/**
 * This is the implementation of the business logic interface
 * @author Sakai App Builder -AZ
 */
public class SmsLogicImpl implements SmsLogic {

   private static Log log = LogFactory.getLog(SmsLogicImpl.class);

   private SmsDao dao;
   public void setDao(SmsDao dao) {
      this.dao = dao;
   }

   private ExternalLogic externalLogic;
   public void setExternalLogic(ExternalLogic externalLogic) {
      this.externalLogic = externalLogic;
   }

   /**
    * Place any code that should run when this class is initialized by spring here
    */
   public void init() {
      log.debug("init");
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.sms.logic.SmsLogic#getItemById(java.lang.Long)
    */
   public SmsItem getItemById(Long id) {
      log.debug("Getting item by id: " + id);
      return dao.findById(SmsItem.class, id);
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.sms.logic.SmsLogic#canWriteItem(org.sakaiproject.sms.model.SmsItem, java.lang.String, java.lang.String)
    */
   public boolean canWriteItem(SmsItem item, String locationId, String userId) {
      log.debug("checking if can write for: " + userId + ", " + locationId + ": and item=" + item.getTitle() );
      if (item.getOwnerId().equals( userId ) ) {
         // owner can always modify an item
         return true;
      } else if ( externalLogic.isUserAdmin(userId) ) {
         // the system super user can modify any item
         return true;
      } else if ( locationId.equals(item.getLocationId()) &&
            externalLogic.isUserAllowedInLocation(userId, ExternalLogic.ITEM_WRITE_ANY, locationId) ) {
         // users with permission in the specified site can modify items from that site
         return true;
      }
      return false;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.sms.logic.SmsLogic#getAllVisibleItems(java.lang.String, java.lang.String)
    */
   public List<SmsItem> getAllVisibleItems(String locationId, String userId) {
      log.debug("Fetching visible items for " + userId + " in site: " + locationId);
      List<SmsItem> l = null;
      if (locationId == null) {
         // get all items
         l = dao.findAll(SmsItem.class);
      } else {
         l = dao.findBySearch(SmsItem.class, 
               new Search("locationId", locationId) );
      }
      // check if the current user can see all items (or is super user)
      if ( externalLogic.isUserAdmin(userId) || 
            externalLogic.isUserAllowedInLocation(userId, ExternalLogic.ITEM_READ_HIDDEN, locationId) ) {
         log.debug("Security override: " + userId + " able to view all items");
      } else {
         // go backwards through the loop to avoid hitting the "end" early
         for (int i=l.size()-1; i >= 0; i--) {
            SmsItem item = (SmsItem) l.get(i);
            if ( item.getHidden().booleanValue() &&
                  !item.getOwnerId().equals(userId) ) {
               l.remove(item);
            }
         }
      }
      return l;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.sms.logic.SmsLogic#removeItem(org.sakaiproject.sms.model.SmsItem)
    */
   public void removeItem(SmsItem item) {
      log.debug("In removeItem with item:" + item.getId() + ":" + item.getTitle());
      // check if current user can remove this item
      if ( canWriteItem(item, externalLogic.getCurrentLocationId(), externalLogic.getCurrentUserId() ) ) {
         dao.delete(item);
         log.info("Removing item: " + item.getId() + ":" + item.getTitle());
      } else {
         throw new SecurityException("Current user cannot remove item " + 
               item.getId() + " because they do not have permission");
      }
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.sms.logic.SmsLogic#saveItem(org.sakaiproject.sms.model.SmsItem)
    */
   public void saveItem(SmsItem item) {
      log.debug("In saveItem with item:" + item.getTitle());
      // set the owner and site to current if they are not set
      if (item.getOwnerId() == null) {
         item.setOwnerId( externalLogic.getCurrentUserId() );
      }
      if (item.getLocationId() == null) {
         item.setLocationId( externalLogic.getCurrentLocationId() );
      }
      if (item.getDateCreated() == null) {
         item.setDateCreated( new Date() );
      }
      // save item if new OR check if the current user can update the existing item
      if ( (item.getId() == null) || 
            canWriteItem(item, externalLogic.getCurrentLocationId(), externalLogic.getCurrentUserId()) ) {
         dao.save(item);
         log.info("Saving item: " + item.getId() + ":" + item.getTitle());
      } else {
         throw new SecurityException("Current user cannot update item " + 
               item.getId() + " because they do not have permission");
      }
   }

}
