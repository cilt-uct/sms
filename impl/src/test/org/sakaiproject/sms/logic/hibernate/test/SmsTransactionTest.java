package org.sakaiproject.sms.logic.hibernate.test;

import java.util.Date;
import java.util.List;

import org.sakaiproject.sms.bean.SearchFilterBean;
import org.sakaiproject.sms.bean.SearchResultContainer;
import org.sakaiproject.sms.dao.StandaloneSmsDaoImpl;
import org.sakaiproject.sms.logic.smpp.impl.SmsBillingImpl;
import org.sakaiproject.sms.model.hibernate.SmsAccount;
import org.sakaiproject.sms.model.hibernate.SmsTransaction;
import org.sakaiproject.sms.model.hibernate.constants.SmsHibernateConstants;
import org.sakaiproject.sms.util.AbstractBaseTestCase;

/**
 * The Class SmsTransactionTest.
 */
public class SmsTransactionTest extends AbstractBaseTestCase {

	private final static SmsBillingImpl smsBillingImpl = new SmsBillingImpl();

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
	@Override
	public void testOnetimeSetup() {
		StandaloneSmsDaoImpl.createSchema();
		smsBillingImpl.setHibernateLogicLocator(hibernateLogicLocator);
	}

	/**
	 * Test get sms transaction by id.
	 */
	public void testGetSmsTransactionById() {

		SmsAccount smsAccount = new SmsAccount();
		smsAccount.setSakaiUserId("0");
		smsAccount.setSakaiSiteId("0");
		smsAccount.setMessageTypeCode("12345");
		smsAccount.setOverdraftLimit(100L);
		smsAccount.setCredits(0l);
		smsAccount.setAccountName("accountname");
		smsAccount.setAccountEnabled(true);
		hibernateLogicLocator.getSmsAccountLogic()
				.persistSmsAccount(smsAccount);

		SmsTransaction smsTransaction = new SmsTransaction();
		smsTransaction.setCreditBalance(10L);
		smsTransaction.setSakaiUserId("sakaiUserId");
		smsTransaction.setTransactionDate(new Date(System.currentTimeMillis()));
		smsTransaction.setTransactionTypeCode("TC");
		smsTransaction.setTransactionCredits(666);

		smsTransaction.setSmsAccount(smsAccount);
		smsTransaction.setSmsTaskId(1L);

		hibernateLogicLocator.getSmsTransactionLogic()
				.insertReserveTransaction(smsTransaction);

		SmsTransaction getSmsTransaction = hibernateLogicLocator
				.getSmsTransactionLogic().getSmsTransaction(
						smsTransaction.getId());
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
		smsAccount.setOverdraftLimit(100L);
		smsAccount.setCredits(0l);
		smsAccount.setAccountName("accountname");
		smsAccount.setAccountEnabled(true);

		SmsTransaction smsTransaction = new SmsTransaction();
		smsTransaction.setCreditBalance(10L);
		smsTransaction.setSakaiUserId("sakaiUserId");
		smsTransaction.setTransactionDate(new Date(System.currentTimeMillis()));
		smsTransaction.setTransactionTypeCode("TC");
		smsTransaction.setTransactionCredits(666);

		smsTransaction.setSmsAccount(smsAccount);
		smsTransaction.setSmsTaskId(1L);

		hibernateLogicLocator.getSmsAccountLogic()
				.persistSmsAccount(smsAccount);

		hibernateLogicLocator.getSmsTransactionLogic()
				.insertCreditAccountTransaction(smsTransaction);
		assertTrue("Object not persisted", smsTransaction.exists());

		// Check the record was created on the DB... an id will be assigned.
		SmsAccount theNewAccount = hibernateLogicLocator.getSmsAccountLogic()
				.getSmsAccount(smsAccount.getId());
		assertNotNull(theNewAccount);

		assertTrue(theNewAccount.getCredits() == 666L);
	}

	/**
	 * Test insert credit transaction.
	 */

	/**
	 * Test get sms transactions.
	 */
	public void testGetSmsTransactions() {
		List<SmsTransaction> transactions = hibernateLogicLocator
				.getSmsTransactionLogic().getAllSmsTransactions();
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
		smsAccount.setOverdraftLimit(100L);
		smsAccount.setCredits(500L);
		smsAccount.setAccountName("accountName");
		smsAccount.setAccountEnabled(true);
		hibernateLogicLocator.getSmsAccountLogic()
				.persistSmsAccount(smsAccount);

		SmsTransaction smsTransaction = new SmsTransaction();
		smsTransaction.setCreditBalance(10L);
		smsTransaction.setSakaiUserId("sakaiUserId");
		smsTransaction.setTransactionDate(new Date(System.currentTimeMillis()));
		smsTransaction.setTransactionTypeCode("TTC");
		smsTransaction.setTransactionCredits(666);

		smsTransaction.setSmsTaskId(1L);

		smsTransaction.setSmsAccount(smsAccount);

		hibernateLogicLocator.getSmsTransactionLogic()
				.insertReserveTransaction(smsTransaction);

		try {

			assertTrue("Object not created successfully", smsTransaction
					.exists());

			SearchFilterBean bean = new SearchFilterBean();
			bean.setTransactionType(smsTransaction.getTransactionTypeCode());
			bean.setNumber(smsTransaction.getSmsAccount().getId().toString());
			bean.setDateFrom(new Date());
			bean.setDateTo(new Date());
			bean.setSender(smsTransaction.getSakaiUserId());

			List<SmsTransaction> transactions = hibernateLogicLocator
					.getSmsTransactionLogic()
					.getPagedSmsTransactionsForCriteria(bean).getPageResults();
			assertTrue("Collection returned has no objects", transactions
					.size() > 0);

			for (SmsTransaction transaction : transactions) {
				// We know that only one transaction should be returned
				assertEquals(transaction, smsTransaction);
			}
		} catch (Exception se) {
			fail(se.getMessage());
		}
		SmsAccount account = hibernateLogicLocator.getSmsAccountLogic()
				.getSmsAccount(smsAccount.getId());
		hibernateLogicLocator.getSmsAccountLogic().deleteSmsAccount(account);
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
		smsAccount.setOverdraftLimit(1000L);
		smsAccount.setCredits(500L);
		smsAccount.setAccountName("accountname");
		smsAccount.setAccountEnabled(true);
		hibernateLogicLocator.getSmsAccountLogic()
				.persistSmsAccount(smsAccount);

		for (int i = 0; i < recordsToInsert; i++) {

			SmsTransaction smsTransaction = new SmsTransaction();
			smsTransaction.setCreditBalance(100L);
			smsTransaction.setSakaiUserId("sakaiUserId");
			smsTransaction.setTransactionDate(new Date(System
					.currentTimeMillis()));
			smsTransaction
					.setTransactionTypeCode(smsBilling.getReserveCreditsCode());
			smsTransaction.setTransactionCredits(i);

			smsTransaction.setSmsAccount(smsAccount);
			smsTransaction.setSmsTaskId(1L);

			hibernateLogicLocator.getSmsTransactionLogic()
					.insertReserveTransaction(smsTransaction);
		}
		try {

			SearchFilterBean bean = new SearchFilterBean();
			bean.setNumber(smsAccount.getId().toString());
			bean.setDateFrom(new Date());
			bean.setDateTo(new Date());
			bean.setTransactionType(smsBilling.getReserveCreditsCode());
			bean.setSender("sakaiUserId");

			bean.setCurrentPage(2);

			SearchResultContainer<SmsTransaction> con = hibernateLogicLocator
					.getSmsTransactionLogic()
					.getPagedSmsTransactionsForCriteria(bean);
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

			con = hibernateLogicLocator.getSmsTransactionLogic()
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

	/**
	 * Test delete sms transaction.
	 */
	public void testDeleteSmsTransaction() {

		SmsAccount smsAccount = new SmsAccount();
		smsAccount.setSakaiUserId("6");
		smsAccount.setSakaiSiteId("6");
		smsAccount.setMessageTypeCode("12345");
		smsAccount.setOverdraftLimit(1000l);
		smsAccount
				.setCredits(500L);
		smsAccount.setAccountName("accountName");
		smsAccount.setAccountEnabled(true);
		hibernateLogicLocator.getSmsAccountLogic()
				.persistSmsAccount(smsAccount);

		SmsTransaction smsTransaction = new SmsTransaction();
		smsTransaction.setCreditBalance(10l);
		smsTransaction.setSakaiUserId("sakaiUserId");
		smsTransaction.setTransactionDate(new Date(System.currentTimeMillis()));
		smsTransaction.setTransactionTypeCode("TTC");
		smsTransaction.setTransactionCredits(666);
		smsTransaction.setSmsTaskId(1L);

		smsTransaction.setSmsAccount(smsAccount);

		hibernateLogicLocator.getSmsTransactionLogic()
				.insertReserveTransaction(smsTransaction);

		hibernateLogicLocator.getSmsTransactionLogic().deleteSmsTransaction(
				smsTransaction);
		SmsTransaction getSmsTransaction = hibernateLogicLocator
				.getSmsTransactionLogic().getSmsTransaction(
						smsTransaction.getId());
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
		smsAccount.setOverdraftLimit(1000L);
		smsAccount
				.setCredits(500L);
		smsAccount.setAccountName("accountName");
		smsAccount.setAccountEnabled(true);
		hibernateLogicLocator.getSmsAccountLogic()
				.persistSmsAccount(smsAccount);

		int noOfTrans = 4;
		for (int i = 0; i < noOfTrans; i++) {
			SmsTransaction smsTransaction = new SmsTransaction();
			smsTransaction.setCreditBalance(10l);
			smsTransaction.setSakaiUserId("1" + i);
			smsTransaction.setTransactionDate(new Date(System
					.currentTimeMillis()));
			smsTransaction.setTransactionTypeCode("TC");
			smsTransaction.setTransactionCredits(i);

			smsTransaction.setSmsAccount(smsAccount);
			smsTransaction.setSmsTaskId(1L);

			hibernateLogicLocator.getSmsTransactionLogic()
					.insertReserveTransaction(smsTransaction);
		}

		// Check it was crested
		assertTrue(smsAccount.exists());
		List<SmsTransaction> transactions = hibernateLogicLocator
				.getSmsTransactionLogic().getSmsTransactionsForAccountId(
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
		smsAccount.setOverdraftLimit(10000L);
		smsAccount
				.setCredits(5000L);
		smsAccount.setAccountName("accountName");
		smsAccount.setAccountEnabled(true);
		hibernateLogicLocator.getSmsAccountLogic()
				.persistSmsAccount(smsAccount);
		Long smsTaskId = 112345L;
		int noOfTrans = 4;
		for (int i = 0; i < noOfTrans; i++) {
			SmsTransaction smsTransaction = new SmsTransaction();
			smsTransaction.setCreditBalance(100L);
			smsTransaction.setSakaiUserId("1" + i);
			smsTransaction.setTransactionDate(new Date(System
					.currentTimeMillis()));
			smsTransaction.setTransactionTypeCode("TC");
			smsTransaction.setTransactionCredits(i);
			smsTransaction.setSmsAccount(smsAccount);
			smsTransaction.setSmsTaskId(smsTaskId);

			hibernateLogicLocator.getSmsTransactionLogic()
					.insertReserveTransaction(smsTransaction);
		}

		// Check it was created
		assertTrue(smsAccount.exists());
		List<SmsTransaction> transactions = hibernateLogicLocator
				.getSmsTransactionLogic().getSmsTransactionsForAccountId(
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
		smsAccount.setOverdraftLimit(10000L);
		smsAccount
				.setCredits(500L);
		smsAccount.setAccountName("accountName");
		smsAccount.setAccountEnabled(true);
		hibernateLogicLocator.getSmsAccountLogic()
				.persistSmsAccount(smsAccount);

		SmsTransaction smsTransaction = new SmsTransaction();
		smsTransaction.setCreditBalance(5000L);
		smsTransaction.setSakaiUserId("sakaiUserId");
		smsTransaction.setTransactionDate(new Date(System.currentTimeMillis()));
		smsTransaction
				.setTransactionTypeCode(smsBilling.getReserveCreditsCode());
		smsTransaction.setTransactionCredits(100);
		smsTransaction.setSmsTaskId(123L);
		smsTransaction.setSmsAccount(smsAccount);

		hibernateLogicLocator.getSmsTransactionLogic()
				.insertReserveTransaction(smsTransaction);

		SmsTransaction cancelTransaction = hibernateLogicLocator
				.getSmsTransactionLogic().getCancelSmsTransactionForTask(123L);
		assertNotNull(cancelTransaction);
		assertEquals(cancelTransaction, smsTransaction);
	}
}
