package org.sakaiproject.sms.logic.hibernate.test;

import java.util.Date;
import java.util.List;

import org.sakaiproject.sms.bean.SearchFilterBean;
import org.sakaiproject.sms.bean.SearchResultContainer;
import org.sakaiproject.sms.logic.impl.hibernate.HibernateLogicFactory;
import org.sakaiproject.sms.logic.smpp.impl.SmsBillingImpl;
import org.sakaiproject.sms.model.hibernate.SmsAccount;
import org.sakaiproject.sms.model.hibernate.SmsTransaction;
import org.sakaiproject.sms.model.hibernate.constants.SmsConst_Billing;
import org.sakaiproject.sms.model.hibernate.constants.SmsHibernateConstants;
import org.sakaiproject.sms.util.AbstractBaseTestCase;
import org.sakaiproject.sms.util.HibernateUtil;

/**
 * The Class SmsTransactionTest.
 */
public class SmsTransactionTest extends AbstractBaseTestCase {

	private static SmsBillingImpl smsBillingImpl = new SmsBillingImpl();

	/**
	 * Instantiates a new sms transaction test.
	 */
	public SmsTransactionTest() {
	}

	/**
	 * Instantiates a new sms transaction test.
	 *
	 * @param name
	 *            the name
	 */
	public SmsTransactionTest(String name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.sms.util.AbstractBaseTestCase#testOnetimeSetup()
	 */
	public void testOnetimeSetup() {
		HibernateUtil.setTestConfiguration(true);
		HibernateUtil.createSchema();
	}

	/**
	 * Test get sms transaction by id.
	 */
	public void testGetSmsTransactionById() {

		SmsAccount smsAccount = new SmsAccount();
		smsAccount.setSakaiUserId("0");
		smsAccount.setSakaiSiteId("0");
		smsAccount.setMessageTypeCode("12345");
		smsAccount.setOverdraftLimit(smsBillingImpl
				.convertAmountToCredits(10000.00f));
		smsAccount.setCredits(0l);
		smsAccount.setAccountName("accountname");
		smsAccount.setAccountEnabled(true);
		HibernateLogicFactory.getAccountLogic().persistSmsAccount(smsAccount);

		SmsTransaction smsTransaction = new SmsTransaction();
		smsTransaction.setCreditBalance(smsBillingImpl.convertAmountToCredits(1.32f));
		smsTransaction.setSakaiUserId("sakaiUserId");
		smsTransaction.setTransactionDate(new Date(System.currentTimeMillis()));
		smsTransaction.setTransactionTypeCode("TC");
		smsTransaction.setTransactionCredits(666);

		smsTransaction.setSmsAccount(smsAccount);
		smsTransaction.setSmsTaskId(1L);

		HibernateLogicFactory.getTransactionLogic().insertReserveTransaction(
				smsTransaction);

		SmsTransaction getSmsTransaction = HibernateLogicFactory
				.getTransactionLogic()
				.getSmsTransaction(smsTransaction.getId());
		assertTrue("Object not persisted", smsTransaction.exists());
		assertNotNull(getSmsTransaction);
		assertEquals(smsTransaction, getSmsTransaction);
	}

	/**
	 * Test insert transaction
	 */
	public void testInsertTransaction() {

		SmsAccount smsAccount = new SmsAccount();
		smsAccount.setSakaiUserId("1");
		smsAccount.setSakaiSiteId("1");
		smsAccount.setMessageTypeCode("12345");
		smsAccount.setOverdraftLimit(smsBillingImpl
				.convertAmountToCredits(10000.00f));
		smsAccount.setCredits(0l);
		smsAccount.setAccountName("accountname");
		smsAccount.setAccountEnabled(true);

		SmsTransaction smsTransaction = new SmsTransaction();
		smsTransaction.setCreditBalance(smsBillingImpl.convertAmountToCredits(1.32f));
		smsTransaction.setSakaiUserId("sakaiUserId");
		smsTransaction.setTransactionDate(new Date(System.currentTimeMillis()));
		smsTransaction.setTransactionTypeCode("TC");
		smsTransaction.setTransactionCredits(666);

		smsTransaction.setSmsAccount(smsAccount);
		smsTransaction.setSmsTaskId(1L);

		HibernateLogicFactory.getAccountLogic().persistSmsAccount(smsAccount);

		HibernateLogicFactory.getTransactionLogic()
				.insertDebitAccountTransaction(smsTransaction);
		assertTrue("Object not persisted", smsTransaction.exists());

		// Check the record was created on the DB... an id will be assigned.
		SmsAccount theNewAccount = HibernateLogicFactory.getAccountLogic()
				.getSmsAccount(smsAccount.getId());
		assertNotNull(theNewAccount);
		// Check updated account balance with smaller positive value
		assertTrue(theNewAccount.getCredits() == smsBillingImpl
				.convertAmountToCredits(1000f));
	}

	/**
	 * Test insert credit transaction.
	 */

	/**
	 * Test get sms transactions.
	 */
	public void testGetSmsTransactions() {
		List<SmsTransaction> transactions = HibernateLogicFactory
				.getTransactionLogic().getAllSmsTransactions();
		assertNotNull("Returnend collection is null", transactions);
		assertTrue("No records returned", transactions.size() > 0);
	}

	/**
	 * Tests the getMessagesForCriteria method
	 */
	public void testGetTransactionsForCriteria() {

		SmsAccount smsAccount = new SmsAccount();
		smsAccount.setSakaiUserId("3");
		smsAccount.setSakaiSiteId("3");
		smsAccount.setMessageTypeCode("12345");
		smsAccount.setOverdraftLimit(smsBillingImpl
				.convertAmountToCredits(10000.00f));
		smsAccount.setCredits(smsBillingImpl.convertAmountToCredits(5000.00f));
		smsAccount.setAccountName("accountName");
		smsAccount.setAccountEnabled(true);
		HibernateLogicFactory.getAccountLogic().persistSmsAccount(smsAccount);

		SmsTransaction smsTransaction = new SmsTransaction();
		smsTransaction.setCreditBalance(smsBillingImpl
				.convertAmountToCredits((1.32f)));
		smsTransaction.setSakaiUserId("sakaiUserId");
		smsTransaction.setTransactionDate(new Date(System.currentTimeMillis()));
		smsTransaction.setTransactionTypeCode("TTC");
		smsTransaction.setTransactionCredits(666);

		smsTransaction.setSmsTaskId(1L);

		smsTransaction.setSmsAccount(smsAccount);

		HibernateLogicFactory.getTransactionLogic().insertReserveTransaction(
				smsTransaction);

		try {

			assertTrue("Object not created successfully", smsTransaction
					.exists());

			SearchFilterBean bean = new SearchFilterBean();
			bean.setTransactionType(smsTransaction.getTransactionTypeCode());
			bean.setNumber(smsTransaction.getSmsAccount().getId().toString());
			bean.setDateFrom(new Date());
			bean.setDateTo(new Date());
			bean.setSender(smsTransaction.getSakaiUserId());

			List<SmsTransaction> transactions = HibernateLogicFactory
					.getTransactionLogic().getPagedSmsTransactionsForCriteria(
							bean).getPageResults();
			assertTrue("Collection returned has no objects", transactions
					.size() > 0);

			for (SmsTransaction transaction : transactions) {
				// We know that only one transaction should be returned
				assertEquals(transaction, smsTransaction);
			}
		} catch (Exception se) {
			fail(se.getMessage());
		}
		SmsAccount account = HibernateLogicFactory.getAccountLogic()
				.getSmsAccount(smsAccount.getId());
		HibernateLogicFactory.getAccountLogic().deleteSmsAccount(account);
	}

	/**
	 * Test get tasks for criteria_ paging.
	 */
	public void testGetTasksForCriteria_Paging() {

		int recordsToInsert = 93;

		SmsAccount smsAccount = new SmsAccount();
		smsAccount.setSakaiUserId("4");
		smsAccount.setSakaiSiteId("4");
		smsAccount.setMessageTypeCode("12345");
		smsAccount.setOverdraftLimit(smsBillingImpl
				.convertAmountToCredits(10000.00f));
		smsAccount.setCredits(smsBillingImpl.convertAmountToCredits(5000.00f));
		smsAccount.setAccountName("accountname");
		smsAccount.setAccountEnabled(true);
		HibernateLogicFactory.getAccountLogic().persistSmsAccount(smsAccount);

		for (int i = 0; i < recordsToInsert; i++) {

			SmsTransaction smsTransaction = new SmsTransaction();
			smsTransaction.setCreditBalance(smsBillingImpl
					.convertAmountToCredits((1.32f)));
			smsTransaction.setSakaiUserId("sakaiUserId");
			smsTransaction.setTransactionDate(new Date(System
					.currentTimeMillis()));
			smsTransaction
					.setTransactionTypeCode(SmsConst_Billing.TRANS_RESERVE_CREDITS);
			smsTransaction.setTransactionCredits(i);

			smsTransaction.setSmsAccount(smsAccount);
			smsTransaction.setSmsTaskId(1L);

			HibernateLogicFactory.getTransactionLogic()
					.insertReserveTransaction(smsTransaction);
		}
		try {

			SearchFilterBean bean = new SearchFilterBean();
			bean.setNumber(smsAccount.getId().toString());
			bean.setDateFrom(new Date());
			bean.setDateTo(new Date());
			bean.setTransactionType(SmsConst_Billing.TRANS_RESERVE_CREDITS);
			bean.setSender("sakaiUserId");

			bean.setCurrentPage(2);

			SearchResultContainer<SmsTransaction> con = HibernateLogicFactory
					.getTransactionLogic().getPagedSmsTransactionsForCriteria(
							bean);
			List<SmsTransaction> tasks = con.getPageResults();
			assertTrue("Incorrect collection size returned",
					tasks.size() == SmsHibernateConstants.DEFAULT_PAGE_SIZE);

			// Test last page. We know there are 124 records to this should
			// return a list of 4
			int pages = recordsToInsert
					/ SmsHibernateConstants.DEFAULT_PAGE_SIZE;
			// set to last page
			if (recordsToInsert % SmsHibernateConstants.DEFAULT_PAGE_SIZE == 0) {
				bean.setCurrentPage(pages);
			} else {
				bean.setCurrentPage(pages + 1);
			}

			con = HibernateLogicFactory.getTransactionLogic()
					.getPagedSmsTransactionsForCriteria(bean);
			tasks = con.getPageResults();
			// int lastPageRecordCount = recordsToInsert % pages;
			int lastPageRecordCount = recordsToInsert
					- (pages * SmsHibernateConstants.DEFAULT_PAGE_SIZE);
			assertTrue("Incorrect collection size returned",
					tasks.size() == lastPageRecordCount);

		} catch (Exception se) {
			fail(se.getMessage());
		}
	}

	public void testCreateCancelTransaction() throws Exception {

		SmsAccount testAccount = new SmsAccount();
		testAccount.setSakaiUserId("3");
		testAccount.setSakaiSiteId("3");
		testAccount.setMessageTypeCode("12345");
		testAccount.setOverdraftLimit(smsBillingImpl
				.convertAmountToCredits(10000.00f));
		testAccount.setCredits(smsBillingImpl
				.convertAmountToCredits((5000.00f)));
		testAccount.setAccountName("accountName");
		testAccount.setAccountEnabled(true);
		HibernateLogicFactory.getAccountLogic().persistSmsAccount(testAccount);

		try {
			HibernateLogicFactory.getTransactionLogic().cancelTransaction(101L,
					testAccount.getId());
		} catch (Exception notExpected) {
			fail("Transaction should save successfully" + notExpected);
		}

	}

	/**
	 * Test delete sms transaction.
	 */
	public void testDeleteSmsTransaction() {

		SmsAccount smsAccount = new SmsAccount();
		smsAccount.setSakaiUserId("6");
		smsAccount.setSakaiSiteId("6");
		smsAccount.setMessageTypeCode("12345");
		smsAccount.setOverdraftLimit(smsBillingImpl
				.convertAmountToCredits(10000.00f));
		smsAccount
				.setCredits(smsBillingImpl.convertAmountToCredits((5000.00f)));
		smsAccount.setAccountName("accountName");
		smsAccount.setAccountEnabled(true);
		HibernateLogicFactory.getAccountLogic().persistSmsAccount(smsAccount);

		SmsTransaction smsTransaction = new SmsTransaction();
		smsTransaction.setCreditBalance(smsBillingImpl.convertAmountToCredits(1.32f));
		smsTransaction.setSakaiUserId("sakaiUserId");
		smsTransaction.setTransactionDate(new Date(System.currentTimeMillis()));
		smsTransaction.setTransactionTypeCode("TTC");
		smsTransaction.setTransactionCredits(666);
		smsTransaction.setSmsTaskId(1L);

		smsTransaction.setSmsAccount(smsAccount);

		HibernateLogicFactory.getTransactionLogic().insertReserveTransaction(
				smsTransaction);

		HibernateLogicFactory.getTransactionLogic().deleteSmsTransaction(
				smsTransaction);
		SmsTransaction getSmsTransaction = HibernateLogicFactory
				.getTransactionLogic()
				.getSmsTransaction(smsTransaction.getId());
		assertNull(getSmsTransaction);
		assertNull("Object not removed", getSmsTransaction);
	}

	/**
	 * Test get sms transactions for account id.
	 */
	public void testGetSmsTransactionsForAccountId() {

		SmsAccount smsAccount = new SmsAccount();
		smsAccount.setSakaiUserId("7");
		smsAccount.setSakaiSiteId("7");
		smsAccount.setMessageTypeCode("12345");
		smsAccount.setOverdraftLimit(smsBillingImpl
				.convertAmountToCredits(10000.00f));
		smsAccount
				.setCredits(smsBillingImpl.convertAmountToCredits((5000.00f)));
		smsAccount.setAccountName("accountName");
		smsAccount.setAccountEnabled(true);
		HibernateLogicFactory.getAccountLogic().persistSmsAccount(smsAccount);

		int noOfTrans = 4;
		for (int i = 0; i < noOfTrans; i++) {
			SmsTransaction smsTransaction = new SmsTransaction();
			smsTransaction.setCreditBalance(smsBillingImpl
					.convertAmountToCredits(1.32f));
			smsTransaction.setSakaiUserId("1" + i);
			smsTransaction.setTransactionDate(new Date(System
					.currentTimeMillis()));
			smsTransaction.setTransactionTypeCode("TC");
			smsTransaction.setTransactionCredits(i);

			smsTransaction.setSmsAccount(smsAccount);
			smsTransaction.setSmsTaskId(1L);

			HibernateLogicFactory.getTransactionLogic()
					.insertReserveTransaction(smsTransaction);
		}

		// Check it was crested
		assertTrue(smsAccount.exists());
		List<SmsTransaction> transactions = HibernateLogicFactory
				.getTransactionLogic().getSmsTransactionsForAccountId(
						smsAccount.getId());
		assertNotNull(transactions);
		assertTrue(transactions.size() == noOfTrans);
		for (SmsTransaction smsTransaction : transactions) {
			assertNotNull(smsTransaction.getSmsAccount());
			assertTrue(smsTransaction.getSmsAccount().getId().equals(
					smsAccount.getId()));
		}

	}

	/**
	 * Test get sms transactions for task id.
	 */
	public void testGetSmsTransactionsForTaskId() {

		SmsAccount smsAccount = new SmsAccount();
		smsAccount.setSakaiUserId("8");
		smsAccount.setSakaiSiteId("8");
		smsAccount.setMessageTypeCode("12345");
		smsAccount.setOverdraftLimit(smsBillingImpl
				.convertAmountToCredits(10000.00f));
		smsAccount
				.setCredits(smsBillingImpl.convertAmountToCredits((5000.00f)));
		smsAccount.setAccountName("accountName");
		smsAccount.setAccountEnabled(true);
		HibernateLogicFactory.getAccountLogic().persistSmsAccount(smsAccount);
		Long smsTaskId = 112345L;
		int noOfTrans = 4;
		for (int i = 0; i < noOfTrans; i++) {
			SmsTransaction smsTransaction = new SmsTransaction();
			smsTransaction.setCreditBalance(smsBillingImpl
					.convertAmountToCredits((1.32f)));
			smsTransaction.setSakaiUserId("1" + i);
			smsTransaction.setTransactionDate(new Date(System
					.currentTimeMillis()));
			smsTransaction.setTransactionTypeCode("TC");
			smsTransaction.setTransactionCredits(i);
			smsTransaction.setSmsAccount(smsAccount);
			smsTransaction.setSmsTaskId(smsTaskId);

			HibernateLogicFactory.getTransactionLogic()
					.insertReserveTransaction(smsTransaction);
		}

		// Check it was created
		assertTrue(smsAccount.exists());
		List<SmsTransaction> transactions = HibernateLogicFactory
				.getTransactionLogic().getSmsTransactionsForAccountId(
						smsAccount.getId());
		assertNotNull(transactions);
		assertTrue(transactions.size() == noOfTrans);
		for (SmsTransaction smsTransaction : transactions) {
			assertNotNull(smsTransaction.getSmsAccount());
			assertTrue(smsTransaction.getSmsTaskId().equals(smsTaskId));
		}
	}

	public void testGetSmsCancelTransactionForTask() {
		SmsAccount smsAccount = new SmsAccount();
		smsAccount.setSakaiUserId("9");
		smsAccount.setSakaiSiteId("9");
		smsAccount.setMessageTypeCode("12345");
		smsAccount.setOverdraftLimit(smsBillingImpl
				.convertAmountToCredits(10000.00f));
		smsAccount
				.setCredits(smsBillingImpl.convertAmountToCredits((5000.00f)));
		smsAccount.setAccountName("accountName");
		smsAccount.setAccountEnabled(true);
		HibernateLogicFactory.getAccountLogic().persistSmsAccount(smsAccount);

		SmsTransaction smsTransaction = new SmsTransaction();
		smsTransaction.setCreditBalance(smsBillingImpl
				.convertAmountToCredits((5000.00f)));
		smsTransaction.setSakaiUserId("sakaiUserId");
		smsTransaction.setTransactionDate(new Date(System.currentTimeMillis()));
		smsTransaction
				.setTransactionTypeCode(SmsConst_Billing.TRANS_RESERVE_CREDITS);
		smsTransaction.setTransactionCredits(100);
		smsTransaction.setSmsTaskId(123L);
		smsTransaction.setSmsAccount(smsAccount);

		HibernateLogicFactory.getTransactionLogic().insertReserveTransaction(
				smsTransaction);

		SmsTransaction cancelTransaction = HibernateLogicFactory
				.getTransactionLogic().getCancelSmsTransactionForTask(123L);
		assertNotNull(cancelTransaction);
		assertEquals(cancelTransaction, smsTransaction);
	}
}
