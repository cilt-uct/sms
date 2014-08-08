package org.sakaiproject.sms.logic.hibernate.test;

import java.util.Date;
import java.util.List;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.sakaiproject.sms.logic.exception.DuplicateUniqueFieldException;
import org.sakaiproject.sms.logic.smpp.impl.SmsBillingImpl;
import org.sakaiproject.sms.model.SmsAccount;
import org.sakaiproject.sms.model.SmsTransaction;
import org.sakaiproject.sms.model.constants.SmsConstants;
import org.sakaiproject.sms.util.AbstractBaseTestCase;

/**
 * The Class SmsAccountTest. Do some basic crud functions on the account table.
 */
public class SmsAccountTest extends AbstractBaseTestCase {

	/** The insert sms account. */
	private static SmsAccount insertSmsAccount;

	/** The insert sms transaction1. */
	private static SmsTransaction insertSmsTransaction1;

	/** The insert sms transaction2. */
	private static SmsTransaction insertSmsTransaction2;

	private static SmsBillingImpl smsBillingImpl = new SmsBillingImpl();

    @BeforeClass()
    public static void setupBeforeClass() {
		if (!SmsConstants.isDbSchemaCreated) {
			smsDao.createSchema();
			SmsConstants.isDbSchemaCreated = true;
		}
		smsBillingImpl.setHibernateLogicLocator(hibernateLogicLocator);
		insertSmsAccount = new SmsAccount();
		insertSmsAccount
				.setSakaiUserId(SmsConstants.SMS_DEV_DEFAULT_SAKAI_USER_ID);
		insertSmsAccount
				.setSakaiSiteId(SmsConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID);
		insertSmsAccount.setMessageTypeCode("12345");
		insertSmsAccount.setOverdraftLimit(1000);
		insertSmsAccount.setCredits(5000);
		insertSmsAccount.setAccountName("accountName");
		insertSmsAccount.setOwnerId("admin");
		insertSmsAccount.setAccountEnabled(true);
		insertSmsAccount.setMessageTypeCode("SO");

		insertSmsTransaction1 = new SmsTransaction();
		insertSmsTransaction1.setCreditBalance(100);
		insertSmsTransaction1.setSakaiUserId("5");
		insertSmsTransaction1.setTransactionCredits(100);
		insertSmsTransaction1.setTransactionDate(new Date(System
				.currentTimeMillis()));
		insertSmsTransaction1.setTransactionTypeCode("TTC");
		insertSmsTransaction1.setSmsTaskId(1L);

		insertSmsTransaction2 = new SmsTransaction();
		insertSmsTransaction2.setCreditBalance(100);
		insertSmsTransaction2.setSakaiUserId("SakaiUserId2");
		insertSmsTransaction2.setTransactionCredits(100);
		insertSmsTransaction2.setTransactionDate(new Date(System
				.currentTimeMillis()));
		insertSmsTransaction2.setTransactionTypeCode("TTC");
		insertSmsTransaction2.setSmsTaskId(1L);
	}
    
    /**
     * Make sure the insertSmsAccount is persisted before every test.
     */
    @Before
    public void setup(){
        //reset the id
        insertSmsAccount.setId(null);
        hibernateLogicLocator.getSmsAccountLogic().persistSmsAccount(insertSmsAccount);
    }
    
    /**
     * Make sure the insertSmsAccount is deleted after every test.
     */
    @After
    public void teardown(){
        hibernateLogicLocator.getSmsAccountLogic().deleteSmsAccount(insertSmsAccount);
    }

	/**
	 * Test insert sms account.
	 */
    @Test
	public void testInsertSmsAccount() {
		// Check the record was created on the DB... an id will be assigned.
		assertTrue("Object not persisted", insertSmsAccount.exists());
	}

	/**
	 * Test insertion of account with duplicate site id
	 */
    @Test
	public void testInsertSmsAccountDuplicateSiteId() {
		// Check the record was created on the DB... an id will be assigned.
		assertTrue("Object not persisted", insertSmsAccount.exists());

		SmsAccount newSmsAccount = new SmsAccount();
		newSmsAccount.setSakaiSiteId(insertSmsAccount.getSakaiSiteId());
		newSmsAccount.setMessageTypeCode("SO");
		newSmsAccount.setAccountName(insertSmsAccount.getAccountName());
		newSmsAccount.setCredits(insertSmsAccount.getCredits());
		newSmsAccount.setStartdate(insertSmsAccount.getStartdate());
		newSmsAccount.setAccountEnabled(true);

		try {
			hibernateLogicLocator.getSmsAccountLogic().persistSmsAccount(
					newSmsAccount);
			fail("DuplicateUniqueFieldException should be caught");
		} catch (DuplicateUniqueFieldException due) {
			assertEquals("sakaiSiteId", due
					.getField());
		}
	}

	/**
	 * Test insertion of account with duplicate user id
	 */
    @Test
	public void testInsertSmsAccountDuplicateUserId() {
		// Check the record was created on the DB... an id will be assigned.
		assertTrue("Object not persisted", insertSmsAccount.exists());

		SmsAccount newSmsAccount = new SmsAccount();
		newSmsAccount.setSakaiUserId(insertSmsAccount.getSakaiUserId());
		newSmsAccount.setAccountName(insertSmsAccount.getAccountName());
		newSmsAccount.setMessageTypeCode("SO");
		newSmsAccount.setCredits(insertSmsAccount.getCredits());
		newSmsAccount.setStartdate(insertSmsAccount.getStartdate());
		newSmsAccount.setAccountEnabled(true);

		try {
			hibernateLogicLocator.getSmsAccountLogic().persistSmsAccount(
					newSmsAccount);
			fail("DuplicateUniqueFieldException should be caught");
		} catch (DuplicateUniqueFieldException due) {
			assertEquals("sakaiUserId", due
					.getField());
		}
	}

	/**
	 * Test get sms account by id.
	 */
    @Test
	public void testGetSmsAccountById() {
		SmsAccount getSmsAccount = hibernateLogicLocator.getSmsAccountLogic()
				.getSmsAccount(insertSmsAccount.getId());
		assertTrue("Object not persisted", insertSmsAccount.exists());
		assertNotNull(getSmsAccount);
		assertEquals(insertSmsAccount, getSmsAccount);
	}

	/**
	 * Test update sms account.
	 */
    @Test
	public void testUpdateSmsAccount() {
		SmsAccount smsAccount = hibernateLogicLocator.getSmsAccountLogic()
				.getSmsAccount(insertSmsAccount.getId());
		smsAccount.setSakaiSiteId(SmsConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID
				+ "new");
		hibernateLogicLocator.getSmsAccountLogic()
				.persistSmsAccount(smsAccount);
		smsAccount = hibernateLogicLocator.getSmsAccountLogic().getSmsAccount(
				insertSmsAccount.getId());
		assertEquals(SmsConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID + "new",
				smsAccount.getSakaiSiteId());
	}

	/**
	 * Test get sms accounts.
	 */
    @Test
	public void testGetSmsAccounts() {
		List<SmsAccount> accounts = hibernateLogicLocator.getSmsAccountLogic()
				.getAllSmsAccounts();
		assertNotNull("Returnend collection is null", accounts);
		assertTrue("No records returned", accounts.size() > 0);
	}

	/**
	 * Test delete sms account.
	 */
    @Test
	public void testDeleteSmsAccount() {
		hibernateLogicLocator.getSmsAccountLogic().deleteSmsAccount(
				hibernateLogicLocator.getSmsAccountLogic().getSmsAccount(
						insertSmsAccount.getId()));
		SmsAccount getSmsAccount = hibernateLogicLocator.getSmsAccountLogic()
				.getSmsAccount(insertSmsAccount.getId());
		assertNull(getSmsAccount);
		assertNull("Object not removed", getSmsAccount);

	}

	/**
	 * Test get sms account by sakai site id.
	 */
    @Test
	public void testGetSmsAccount_sakaiSiteId() {
		SmsAccount account = new SmsAccount();
		account.setSakaiSiteId(SmsConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID+2);
		account.setMessageTypeCode("12345");
		account.setOverdraftLimit(1000);
		account.setCredits(5000);
		account.setAccountName("accountName");
		account.setAccountEnabled(true);
		hibernateLogicLocator.getSmsAccountLogic().persistSmsAccount(account);
		SmsAccount retAccount = hibernateLogicLocator.getSmsAccountLogic()
				.getSmsAccount(account.getSakaiSiteId(), "");
		assertNotNull(retAccount);
		assertEquals(retAccount, account);
	}

	/**
	 * Test get sms account by sakai user id.
	 */
    @Test
	public void testGetSmsAccount_sakaiUserId() {
		SmsAccount account = new SmsAccount();
		account.setSakaiUserId(SmsConstants.SMS_DEV_DEFAULT_SAKAI_USER_ID + 1);
		account.setSakaiSiteId(SmsConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID + 1);
		account.setMessageTypeCode("12345");
		account.setOverdraftLimit(1000);
		account.setCredits(5000);
		account.setAccountName(SmsConstants.SMS_DEV_DEFAULT_SAKAI_ACCOUNT_NAME);
		account.setAccountEnabled(true);
		hibernateLogicLocator.getSmsAccountLogic().persistSmsAccount(account);
		SmsAccount retAccount = hibernateLogicLocator.getSmsAccountLogic()
				.getSmsAccount(SmsConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID + 1,
						account.getSakaiUserId());
		assertNotNull(retAccount);
		assertEquals(retAccount, account);
	}
	
    @Test
	public void testGestSMSAccountsForOwner() {
		SmsAccount account = new SmsAccount();
		account.setSakaiSiteId(SmsConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID+3);
		account.setMessageTypeCode("12345");
		account.setOverdraftLimit(1000);
		account.setCredits(5000);
		account.setAccountName(SmsConstants.SMS_DEV_DEFAULT_SAKAI_ACCOUNT_NAME);
		account.setOwnerId("admin");
		account.setAccountEnabled(true);
		hibernateLogicLocator.getSmsAccountLogic().persistSmsAccount(account);
		
		
		
		List<SmsAccount> one = hibernateLogicLocator.getSmsAccountLogic().getSmsAccountsForOwner("admin");
		assertNotNull(one);
        //the insertSmsAccount and the account in this method
		assertEquals(2, one.size());
		
		List<SmsAccount> two = hibernateLogicLocator.getSmsAccountLogic().getSmsAccountsForOwner("wegwerg");
		assertNotNull(two);
		assertEquals(0, two.size());
		
	}
	
}
