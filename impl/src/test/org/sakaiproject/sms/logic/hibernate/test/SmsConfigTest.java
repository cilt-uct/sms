package org.sakaiproject.sms.logic.hibernate.test;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.stubs.ExternalLogicStub;
import org.sakaiproject.sms.model.SmsConfig;
import org.sakaiproject.sms.model.constants.SmsConstants;
import org.sakaiproject.sms.util.AbstractBaseTestCase;

/**
 * Some basic crud tests on sms tool configuration.
 */
public class SmsConfigTest extends AbstractBaseTestCase {

	/** The insert sms config. */
	private static SmsConfig insertSmsConfig;

	private final ExternalLogic externalLogic = new ExternalLogicStub();

    @BeforeClass
	public static void beforeClass(){
		if (!SmsConstants.isDbSchemaCreated) {
			smsDao.createSchema();
			SmsConstants.isDbSchemaCreated = true;
		}
		insertSmsConfig = new SmsConfig();
		insertSmsConfig.setSakaiSiteId("sakaiSiteId2");
		insertSmsConfig.setSakaiToolId("sakaiToolId");
		insertSmsConfig.setNotificationEmail("notification@Email.Address");
		insertSmsConfig.setSendSmsEnabled(false);
	}
    
    /**
     * Make sure the insertSmsConfig is persisted before every test.
     */
    @Before
    public void setup(){
        //reset the id
        insertSmsConfig.setId(null);
        hibernateLogicLocator.getSmsConfigLogic().persistSmsConfig(insertSmsConfig);
    }
    
    /**
     * Make sure the insertSmsAccount is deleted after every test.
     */
    @After
    public void teardown(){
        hibernateLogicLocator.getSmsConfigLogic().deleteSmsConfig(insertSmsConfig);
    }

	/**
	 * Test insert sms config.
	 */
    @Test
	public void testInsertSmsConfig() {
		// Check the record was created on the DB... an id will be assigned.
    	Assert.assertTrue("Object not persisted", insertSmsConfig.exists());
	}

	/**
	 * Test get sms config by id.
	 */
    @Test
	public void testGetSmsConfigById() {
		SmsConfig getSmsConfig = hibernateLogicLocator.getSmsConfigLogic()
				.getSmsConfig(insertSmsConfig.getId());
		Assert.assertTrue("Object not persisted", insertSmsConfig.exists());
		Assert.assertNotNull(getSmsConfig);
		Assert.assertEquals(insertSmsConfig, getSmsConfig);
		Assert.assertTrue("Boolean property problem",
				getSmsConfig.isSendSmsEnabled() == false);
	}

	/**
	 * Test update sms config.
	 */
    @Test
	public void testUpdateSmsConfig() {
		SmsConfig smsConfig = hibernateLogicLocator.getSmsConfigLogic()
				.getSmsConfig(insertSmsConfig.getId());
		smsConfig.setSakaiSiteId("newSakaiSiteId");
		hibernateLogicLocator.getSmsConfigLogic().persistSmsConfig(smsConfig);
		smsConfig = hibernateLogicLocator.getSmsConfigLogic().getSmsConfig(
				insertSmsConfig.getId());
		Assert.assertEquals("newSakaiSiteId", smsConfig.getSakaiSiteId());
	}

	/**
	 * Test get sms configs.
	 */
    @Test
	public void testGetSmsConfigs() {
		List<SmsConfig> confs = hibernateLogicLocator.getSmsConfigLogic()
				.getAllSmsConfig();
		Assert.assertNotNull("Returned collection is null", confs);
		Assert.assertTrue("No records returned", confs.size() > 0);
	}

	/**
	 * Test get sms config by sakia site id.
	 */
    @Test
	public void testGetSmsConfigBySakiaSiteId() {
		String testId = "testGetSmsConfigBySakiaSiteId";

		SmsConfig smsConfig = new SmsConfig();
		smsConfig.setSakaiSiteId(testId);
		smsConfig.setSakaiToolId("testGetSmsConfigBySakiaSiteId");
		smsConfig.setNotificationEmail("notification@Email.Address");
		smsConfig.setSendSmsEnabled(false);
		hibernateLogicLocator.getSmsConfigLogic().persistSmsConfig(
				smsConfig);
		Assert.assertTrue("Object not created correctly", smsConfig.exists());

		try {
			SmsConfig conf = hibernateLogicLocator.getSmsConfigLogic()
					.getOrCreateSmsConfigBySakaiSiteId(testId);
			Assert.assertNotNull("Object not found", conf);
			Assert.assertEquals("Incorrect object returned", conf, smsConfig);
		} finally {
			hibernateLogicLocator.getSmsConfigLogic().deleteSmsConfig(
					smsConfig);
		}
	}

	/**
	 * Test get sms config by sakia tool id.
	 */
    @Test
	public void testGetSmsConfigBySakiaToolId() {
		String testId = "testGetSmsConfigBySakiaToolId";

		SmsConfig smsConfig = new SmsConfig();
		smsConfig.setSakaiSiteId("testGetSmsConfigBySakiaToolId");
		smsConfig.setSakaiToolId(testId);
		smsConfig.setNotificationEmail("notification@Email.Address");
		smsConfig.setSendSmsEnabled(false);
		hibernateLogicLocator.getSmsConfigLogic().persistSmsConfig(
				smsConfig);
		Assert.assertTrue("Object not created correctly", smsConfig.exists());

		try {
			SmsConfig conf = hibernateLogicLocator.getSmsConfigLogic()
					.getSmsConfigBySakaiToolId(testId);
			Assert.assertNotNull("Object not found", conf);
			Assert.assertEquals("Incorrect object returned", conf, smsConfig);

			conf = hibernateLogicLocator.getSmsConfigLogic()
					.getSmsConfigBySakaiToolId("SomeOtherId");
			Assert.assertNull("No object should be found", conf);

		} finally {
			hibernateLogicLocator.getSmsConfigLogic().deleteSmsConfig(
					smsConfig);
		}
	}

	/**
	 * Test delete sms config.
	 */
    @Test
	public void testFindByIdDevMode() {
		SmsConfig getSmsConfig = hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSmsConfigBySakaiSiteId(
						externalLogic.getCurrentSiteId());
		Assert.assertNotNull(getSmsConfig);

	}

	/**
	 * Test delete sms config.
	 */
    @Test
	public void testDeleteSmsConfig() {
		hibernateLogicLocator.getSmsConfigLogic().deleteSmsConfig(
				insertSmsConfig);
		SmsConfig getSmsConfig = hibernateLogicLocator.getSmsConfigLogic()
				.getSmsConfig(insertSmsConfig.getId());
		Assert.assertNull(getSmsConfig);
		Assert.assertNull("Object not removed", getSmsConfig);
	}

}
