/******************************************************************************
 * SmsLogicImplTest.java - created by Sakai App Builder -AZ
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

import junit.framework.Assert;

import org.sakaiproject.sms.dao.SmsDao;
import org.sakaiproject.sms.logic.SmsLogicImpl;
import org.sakaiproject.sms.logic.stubs.ExternalLogicStub;
import org.sakaiproject.sms.model.SmsItem;
import org.springframework.test.AbstractTransactionalSpringContextTests;


/**
 * Testing the Logic implementation methods
 * @author Sakai App Builder -AZ
 */
public class SmsLogicImplTest extends AbstractTransactionalSpringContextTests {

   protected SmsLogicImpl logicImpl;

   private FakeDataPreload tdp;

   protected String[] getConfigLocations() {
      // point to the needed spring config files, must be on the classpath
      // (add component/src/webapp/WEB-INF to the build path in Eclipse),
      // they also need to be referenced in the project.xml file
      return new String[] {"hibernate-test.xml", "spring-hibernate.xml"};
   }

   // run this before each test starts
   protected void onSetUpBeforeTransaction() throws Exception {
   }

   // run this before each test starts and as part of the transaction
   protected void onSetUpInTransaction() {
      // load the spring created dao class bean from the Spring Application Context
      SmsDao dao = (SmsDao) applicationContext.
         getBean("org.sakaiproject.sms.dao.SmsDao");
      if (dao == null) {
         throw new NullPointerException("DAO could not be retrieved from spring context");
      }

      // load up the test data preloader from spring
      tdp = (FakeDataPreload) applicationContext.
         getBean("org.sakaiproject.sms.logic.test.FakeDataPreload");
      if (tdp == null) {
         throw new NullPointerException("FakeDataPreload could not be retrieved from spring context");
      }

      // reload the test objects in this session
      tdp.reloadTestData();
      
      // init the class if needed

      // setup the mock objects

      // create and setup the object to be tested
      logicImpl = new SmsLogicImpl();
      logicImpl.setDao(dao);
      logicImpl.setExternalLogic( new ExternalLogicStub() );

      // can set up the default mock object returns here if desired
      // Note: Still need to activate them in the test methods though

      // run the init
      logicImpl.init();
   }

   /**
    * Sms method for {@link org.sakaiproject.sms.logic.impl.SmsLogicImpl#getItemById(java.lang.Long)}.
    */
   public void testGetItemById() {
      SmsItem item = logicImpl.getItemById(tdp.item1.getId());
      Assert.assertNotNull(item);
      Assert.assertEquals(item, tdp.item1);

      SmsItem baditem = logicImpl.getItemById( new Long(-1) );
      Assert.assertNull(baditem);

      try {
         logicImpl.getItemById(null);
         Assert.fail("Should have thrown IllegalArgumentException");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e.getMessage());
      }
   }

   /**
    * Sms method for {@link org.sakaiproject.sms.logic.impl.SmsLogicImpl#canWriteItem(org.sakaiproject.sms.model.SmsItem, java.lang.String, java.lang.String)}.
    */
   public void testCanWriteItemSmsItemStringString() {
      // testing perms as a normal user
      Assert.assertFalse( logicImpl.canWriteItem(tdp.adminitem, FakeDataPreload.LOCATION1_ID, FakeDataPreload.USER_ID) );
      Assert.assertFalse( logicImpl.canWriteItem(tdp.maintitem, FakeDataPreload.LOCATION1_ID, FakeDataPreload.USER_ID) );
      Assert.assertTrue( logicImpl.canWriteItem(tdp.item1, FakeDataPreload.LOCATION1_ID, FakeDataPreload.USER_ID) );

      // testing perms as user with special perms
      Assert.assertFalse( logicImpl.canWriteItem(tdp.adminitem, FakeDataPreload.LOCATION1_ID, FakeDataPreload.MAINT_USER_ID) );
      Assert.assertTrue( logicImpl.canWriteItem(tdp.maintitem, FakeDataPreload.LOCATION1_ID, FakeDataPreload.MAINT_USER_ID) );
      Assert.assertTrue( logicImpl.canWriteItem(tdp.item1, FakeDataPreload.LOCATION1_ID, FakeDataPreload.MAINT_USER_ID) );

      // testing perms as admin user
      Assert.assertTrue( logicImpl.canWriteItem(tdp.adminitem, FakeDataPreload.LOCATION1_ID, FakeDataPreload.ADMIN_USER_ID) );
      Assert.assertTrue( logicImpl.canWriteItem(tdp.maintitem, FakeDataPreload.LOCATION1_ID, FakeDataPreload.ADMIN_USER_ID) );
      Assert.assertTrue( logicImpl.canWriteItem(tdp.item1, FakeDataPreload.LOCATION1_ID, FakeDataPreload.ADMIN_USER_ID) );
   }

   /**
    * Sms method for {@link org.sakaiproject.sms.logic.impl.SmsLogicImpl#getAllVisibleItems(java.lang.String, java.lang.String)}.
    */
   public void testGetAllVisibleItemsStringString() {

      // add 2 items to test if we can see the visible one and not the hidden one
      SmsItem itemHidden = new SmsItem("New item title", 
            FakeDataPreload.MAINT_USER_ID, FakeDataPreload.LOCATION1_ID, Boolean.TRUE, new Date());
      logicImpl.saveItem(itemHidden);
      SmsItem itemVisible = new SmsItem("New item title", 
            FakeDataPreload.MAINT_USER_ID, FakeDataPreload.LOCATION1_ID, Boolean.FALSE, new Date());
      logicImpl.saveItem(itemVisible);

      List<SmsItem> l = logicImpl.getAllVisibleItems(FakeDataPreload.LOCATION1_ID, FakeDataPreload.USER_ID); // test normal user
      Assert.assertNotNull(l);
      Assert.assertEquals(4, l.size());
      Assert.assertTrue(l.contains(tdp.item1));
      Assert.assertTrue(! l.contains(tdp.item2));
      Assert.assertTrue(l.contains(itemVisible));
      Assert.assertTrue(! l.contains(itemHidden));

      List<SmsItem> lmaintain = logicImpl.getAllVisibleItems(FakeDataPreload.LOCATION1_ID, FakeDataPreload.MAINT_USER_ID); // test maintainer
      Assert.assertNotNull(lmaintain);
      Assert.assertEquals(5, lmaintain.size());
      Assert.assertTrue(lmaintain.contains(tdp.item1));
      Assert.assertTrue(! lmaintain.contains(tdp.item2));
      Assert.assertTrue(lmaintain.contains(itemVisible));
      Assert.assertTrue(lmaintain.contains(itemHidden));

      List<SmsItem> ladmin = logicImpl.getAllVisibleItems(FakeDataPreload.LOCATION1_ID, FakeDataPreload.ADMIN_USER_ID); // test admin
      Assert.assertNotNull(ladmin);
      Assert.assertEquals(5, ladmin.size());
      Assert.assertTrue(ladmin.contains(tdp.item1));
      Assert.assertTrue(! ladmin.contains(tdp.item2));
      Assert.assertTrue(ladmin.contains(itemVisible));
      Assert.assertTrue(ladmin.contains(itemHidden));
   }

   /**
    * Sms method for {@link org.sakaiproject.sms.logic.impl.SmsLogicImpl#removeItem(org.sakaiproject.sms.model.SmsItem)}.
    */
   public void testRemoveItem() {
      try {
         logicImpl.removeItem(tdp.adminitem); // user cannot delete this
         Assert.fail("Should have thrown SecurityException");
      } catch (SecurityException e) {
         Assert.assertNotNull(e.getMessage());
      }

      try {
         logicImpl.removeItem(tdp.adminitem); // permed user cannot delete this
         Assert.fail("Should have thrown SecurityException");
      } catch (SecurityException e) {
         Assert.assertNotNull(e.getMessage());
      }

      logicImpl.removeItem(tdp.item1); // user can delete this
      SmsItem item = logicImpl.getItemById(tdp.item1.getId());
      Assert.assertNull(item);
   }

   /**
    * Sms method for {@link org.sakaiproject.sms.logic.impl.SmsLogicImpl#saveItem(org.sakaiproject.sms.model.SmsItem)}.
    */
   public void testSaveItem() {

      SmsItem item = new SmsItem("New item title", FakeDataPreload.USER_ID, FakeDataPreload.LOCATION1_ID, Boolean.FALSE, new Date());
      logicImpl.saveItem(item);
      Long itemId = item.getId();
      Assert.assertNotNull(itemId);

      // test saving an incomplete item
      SmsItem incompleteItem = new SmsItem();
      incompleteItem.setTitle("New incomplete item");
      incompleteItem.setHidden(Boolean.FALSE);

      // mock object is needed here
      logicImpl.saveItem(incompleteItem);

      Long incItemId = item.getId();
      Assert.assertNotNull(incItemId);

      item = logicImpl.getItemById(incItemId);
      Assert.assertNotNull(item);     
      Assert.assertEquals(item.getOwnerId(), FakeDataPreload.USER_ID);
      Assert.assertEquals(item.getLocationId(), FakeDataPreload.LOCATION1_ID);

      // test saving a null value for failure
      try {
         logicImpl.saveItem(null);
         Assert.fail("Should have thrown NullPointerException");
      } catch (NullPointerException e) {
         Assert.assertNotNull(e.getStackTrace());
      }
   }

}
