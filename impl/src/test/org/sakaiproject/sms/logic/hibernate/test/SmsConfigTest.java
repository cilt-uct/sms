package org.sakaiproject.sms.logic.hibernate.test;

import java.util.List;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.stubs.ExternalLogicStub;
import org.sakaiproject.sms.model.SmsConfig;
import org.sakaiproject.sms.model.constants.SmsConstants;
import org.sakaiproject.sms.util.AbstractBaseTestCase;
import static org.sakaiproject.sms.util.AbstractBaseTestCase.hibernateLogicLocator;

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
		assertTrue("Object not persisted", insertSmsConfig.exists());
	}

	/**
	 * Test get sms config by id.
	 */
    @Test
	public void testGetSmsConfigById() {
		SmsConfig getSmsConfig = hibernateLogicLocator.getSmsConfigLogic()
				.getSmsConfig(insertSmsConfig.getId());
		assertTrue("Object not persisted", insertSmsConfig.exists());
		assertNotNull(getSmsConfig);
		assertEquals(insertSmsConfig, getSmsConfig);
		assertTrue("Boolean property problem",
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
		assertEquals("newSakaiSiteId", smsConfig.getSakaiSiteId());
	}

	/**
	 * Test get sms configs.
	 */
    @Test
	public void testGetSmsConfigs() {
		List<SmsConfig> confs = hibernateLogicLocator.getSmsConfigLogic()
				.getAllSmsConfig();
		assertNotNull("Returned collection is null", confs);
		assertTrue("No records returned", confs.size() > 0);
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
		assertTrue("Object not created correctly", smsConfig.exists());

		try {
			SmsConfig conf = hibernateLogicLocator.getSmsConfigLogic()
					.getOrCreateSmsConfigBySakaiSiteId(testId);
			assertNotNull("Object not found", conf);
			assertEquals("Incorrect object returned", conf, smsConfig);
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
		assertTrue("Object not created correctly", smsConfig.exists());

		try {
			SmsConfig conf = hibernateLogicLocator.getSmsConfigLogic()
					.getSmsConfigBySakaiToolId(testId);
			assertNotNull("Object not found", conf);
			assertEquals("Incorrect object returned", conf, smsConfig);

			conf = hibernateLogicLocator.getSmsConfigLogic()
					.getSmsConfigBySakaiToolId("SomeOtherId");
			assertNull("No object should be found", conf);

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
		assertNotNull(getSmsConfig);

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
		assertNull(getSmsConfig);
		assertNull("Object not removed", getSmsConfig);
	}

}
