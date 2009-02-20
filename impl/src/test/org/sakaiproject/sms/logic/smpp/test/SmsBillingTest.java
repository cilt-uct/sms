/***********************************************************************************
 * SmsBillingTest.java
 * Copyright (c) 2008 Sakai Project/Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.s
 *
 **********************************************************************************/
package org.sakaiproject.sms.logic.smpp.test;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.sakaiproject.sms.dao.StandaloneSmsDaoImpl;
import org.sakaiproject.sms.logic.smpp.impl.SmsBillingImpl;
import org.sakaiproject.sms.logic.smpp.impl.SmsSmppImpl;
import org.sakaiproject.sms.model.hibernate.SmsAccount;
import org.sakaiproject.sms.model.hibernate.SmsMessage;
import org.sakaiproject.sms.model.hibernate.SmsTask;
import org.sakaiproject.sms.model.hibernate.SmsTransaction;
import org.sakaiproject.sms.model.hibernate.constants.SmsConst_DeliveryStatus;
import org.sakaiproject.sms.util.AbstractBaseTestCase;

// TODO: Auto-generated Javadoc
/**
 * Tests the billing mthods.
 */
public class SmsBillingTest extends AbstractBaseTestCase {

	/** The sms smpp impl. */
	private static SmsSmppImpl smsSmppImpl = null;

	/** The sms billing impl. */
	private static SmsBillingImpl smsBillingImpl = null;

	/** The account. */
	public static SmsAccount account;

	static {
		smsBillingImpl = new SmsBillingImpl();
		smsBillingImpl.setHibernateLogicLocator(hibernateLogicLocator);
		smsSmppImpl = new SmsSmppImpl();
		smsSmppImpl.setHibernateLogicLocator(hibernateLogicLocator);
		smsSmppImpl.init();

		account = new SmsAccount();
		account.setSakaiSiteId("121112322");
		account.setMessageTypeCode("");
		account.setCredits(10L);
		account.setAccountName("account name");
		account.setStartdate(new Date());
		account.setAccountEnabled(true);

	}

	/** The test amount. */
	public final Float testAmount = 100F;

	/** The test credits. */
	public final int testCredits = 100;

	/**
	 * Instantiates a new sms billing test.
	 */
	public SmsBillingTest() {
	}

	/**
	 * Instantiates a new sms billing test.
	 *
	 * @param name
	 *            the name
	 */
	public SmsBillingTest(String name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.sms.util.AbstractBaseTestCase#testOnetimeSetup()
	 */
	@Override
	public void testOnetimeSetup() {
		StandaloneSmsDaoImpl.createSchema();
		hibernateLogicLocator.getSmsAccountLogic().persistSmsAccount(account);

	}

	/**
	 * Test check sufficient credits are not available.
	 */
	public void testCheckSufficientCredits_False() {

		int creditsRequired = 11;

		boolean sufficientCredits = smsBillingImpl.checkSufficientCredits(
				account.getId(), creditsRequired);
		assertFalse("Expected insufficient credit", sufficientCredits);
	}

	/**
	 * Test check sufficient credits are available from overdraft.
	 */
	public void testCheckSufficientCredits_OverdraftFacility() {

		int creditsRequired = 11;

		SmsMessage msg = new SmsMessage();
		SmsTask smsTask = new SmsTask();
		smsTask.setSakaiSiteId("sakaiSiteId");
		smsTask.setSmsAccountId(account.getId());
		smsTask.setDateCreated(new Timestamp(System.currentTimeMillis()));
		smsTask.setDateToSend(new Timestamp(System.currentTimeMillis()));
		smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		smsTask.setAttemptCount(2);
		smsTask.setMessageBody("messageBody");
		smsTask.setSenderUserName("senderUserName");
		smsTask.setMaxTimeToLive(1);
		smsTask.setDelReportTimeoutDuration(1);
		smsTask.getSmsMessages().add(msg);
		smsTask.setCreditEstimate(creditsRequired);
		msg.setSmsTask(smsTask);

		boolean sufficientCredits = smsBillingImpl.checkSufficientCredits(
				account.getId(), creditsRequired);
		assertFalse("Expected insufficient credit", sufficientCredits);
		hibernateLogicLocator.getSmsAccountLogic().persistSmsAccount(account);
		sufficientCredits = smsBillingImpl.checkSufficientCredits(account
				.getId(), creditsRequired);
		assertTrue("Expected insufficient credit", !sufficientCredits);
	}

	/**
	 * Test check sufficient credits are available.
	 */
	public void testCheckSufficientCredits_True() {

		int creditsRequired = 1;
		account.setCredits(smsBillingImpl.convertAmountToCredits((10f)));
		hibernateLogicLocator.getSmsAccountLogic().persistSmsAccount(account);
		SmsMessage msg = new SmsMessage();
		SmsTask smsTask = new SmsTask();
		smsTask.setSakaiSiteId("sakaiSiteId");
		smsTask.setSmsAccountId(account.getId());
		smsTask.setDateCreated(new Timestamp(System.currentTimeMillis()));
		smsTask.setDateToSend(new Timestamp(System.currentTimeMillis()));
		smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		smsTask.setAttemptCount(2);
		smsTask.setMessageBody("messageBody");
		smsTask.setSenderUserName("senderUserName");
		smsTask.setMaxTimeToLive(1);
		smsTask.setDelReportTimeoutDuration(1);
		smsTask.getSmsMessages().add(msg);
		smsTask.setCreditEstimate(creditsRequired);
		msg.setSmsTask(smsTask);

		boolean sufficientCredits = smsBillingImpl.checkSufficientCredits(
				account.getId(), creditsRequired);
		assertTrue("Expected sufficient credit", sufficientCredits);

		account.setCredits((smsBillingImpl.convertAmountToCredits(-10f)));
		hibernateLogicLocator.getSmsAccountLogic().persistSmsAccount(account);
		boolean insufficientCredits = !smsBillingImpl.checkSufficientCredits(
				account.getId(), creditsRequired);
		assertTrue("Expected insufficient credit", insufficientCredits);

	}

	/**
	 * Test convert amount to credits.
	 */
	public void testConvertAmountToCredits() {
		float amount = smsBillingImpl.convertCreditsToAmount(testCredits);
		Long credits = smsBillingImpl.convertAmountToCredits(amount);
		assertTrue(credits == testCredits);
	}

	/**
	 * Test convert credits to amount.
	 */
	public void testConvertCreditsToAmount() {
		Long credits = smsBillingImpl.convertAmountToCredits(testAmount);
		// Not sure how to test this properly.
	}

	/**
	 * Test recalculate account balance.
	 */
	public void testRecalculateAccountBalance() {
		SmsAccount smsAccount = new SmsAccount();
		smsAccount.setSakaiUserId("1");
		smsAccount.setSakaiSiteId("1");
		smsAccount.setMessageTypeCode("1");
		smsAccount.setOverdraftLimit(1000L);
		smsAccount.setCredits(10L);
		smsAccount.setAccountName("accountname");
		smsAccount.setAccountEnabled(true);
		hibernateLogicLocator.getSmsAccountLogic()
				.persistSmsAccount(smsAccount);
		insertTestTransactionsForAccount(smsAccount);

		assertTrue(smsAccount.exists());

		List<SmsTransaction> transactions = hibernateLogicLocator
				.getSmsTransactionLogic().getSmsTransactionsForAccountId(
						smsAccount.getId());

		assertNotNull(transactions);
		assertTrue(transactions.size() > 0);

		smsBillingImpl.recalculateAccountBalance(smsAccount.getId());

		SmsAccount recalculatedAccount = hibernateLogicLocator
				.getSmsAccountLogic().getSmsAccount(smsAccount.getId());
		assertNotNull(recalculatedAccount);
		assertTrue(recalculatedAccount.getCredits() == 6660);
	}

	/**
	 * Test reserve credits.
	 */
	public void testReserveCredits() {

		int creditEstimate = 50;
		float origionalAccBalance = 100;

		SmsAccount smsAccount = new SmsAccount();
		smsAccount.setSakaiUserId("2");
		smsAccount.setSakaiSiteId("2");
		smsAccount.setMessageTypeCode("2");
		smsAccount.setOverdraftLimit(1000L);
		smsAccount.setCredits(smsBillingImpl
				.convertAmountToCredits(origionalAccBalance));
		smsAccount.setAccountName("accountname");
		smsAccount.setAccountEnabled(true);
		hibernateLogicLocator.getSmsAccountLogic()
				.persistSmsAccount(smsAccount);

		SmsTask smsTask = new SmsTask();
		smsTask.setSakaiSiteId("sakaiSiteId");
		smsTask.setSenderUserName("sakaiUserId");
		smsTask.setSmsAccountId(smsAccount.getId());
		smsTask.setDateCreated(new Timestamp(System.currentTimeMillis()));
		smsTask.setDateToSend(new Timestamp(System.currentTimeMillis()));
		smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		smsTask.setAttemptCount(2);
		smsTask.setMessageBody("messageBody");
		smsTask.setSenderUserName("senderUserName");
		smsTask.setMaxTimeToLive(1);
		smsTask.setDelReportTimeoutDuration(1);
		smsTask.setCreditEstimate(creditEstimate);
		Calendar cal = Calendar.getInstance();
		cal.setTime(smsTask.getDateToSend());
		cal.add(Calendar.SECOND, smsTask.getMaxTimeToLive());
		smsTask.setDateToExpire(cal.getTime());
		hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);

		smsBillingImpl.reserveCredits(smsTask);

		smsAccount = hibernateLogicLocator.getSmsAccountLogic().getSmsAccount(
				smsAccount.getId());
		assertNotNull(smsAccount);

		// Transaction did reduce the account balance
		assertTrue(smsAccount.getCredits() < origionalAccBalance);
	}

	/**
	 * Test settle credit difference.
	 */
	public void testSettleCreditDifference() {
		int creditEstimate = 50;
		float origionalAccBalance = 0;

		SmsAccount smsAccount = new SmsAccount();
		smsAccount.setSakaiUserId("3");
		smsAccount.setSakaiSiteId("3");
		smsAccount.setMessageTypeCode("3");
		smsAccount.setOverdraftLimit(1000L);
		smsAccount.setCredits(smsBillingImpl
				.convertAmountToCredits(origionalAccBalance));
		smsAccount.setAccountName("accountname");
		smsAccount.setAccountEnabled(true);
		hibernateLogicLocator.getSmsAccountLogic()
				.persistSmsAccount(smsAccount);

		SmsTask smsTask = new SmsTask();
		smsTask.setSakaiSiteId("sakaiSiteId");
		smsTask.setSenderUserName("sakaiUserId");
		smsTask.setSmsAccountId(smsAccount.getId());
		smsTask.setDateCreated(new Timestamp(System.currentTimeMillis()));
		smsTask.setDateToSend(new Timestamp(System.currentTimeMillis()));
		smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		smsTask.setAttemptCount(2);
		smsTask.setMessageBody("messageBody");
		smsTask.setSenderUserName("senderUserName");
		smsTask.setMaxTimeToLive(1);
		smsTask.setDelReportTimeoutDuration(1);
		smsTask.setCreditEstimate(creditEstimate);
		smsTask.setGroupSizeActual(0);
		Calendar cal = Calendar.getInstance();
		cal.setTime(smsTask.getDateToSend());
		cal.add(Calendar.SECOND, smsTask.getMaxTimeToLive());
		smsTask.setDateToExpire(cal.getTime());
		hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);

		smsBillingImpl.reserveCredits(smsTask);

		smsAccount = hibernateLogicLocator.getSmsAccountLogic().getSmsAccount(
				smsAccount.getId());
		assertNotNull(smsAccount);

		// Account was credited
		assertTrue(smsBillingImpl.convertCreditsToAmount(smsAccount
				.getCredits()) < origionalAccBalance);

		smsBillingImpl.settleCreditDifference(smsTask);

		smsAccount = hibernateLogicLocator.getSmsAccountLogic().getSmsAccount(
				smsAccount.getId());

		// Account balance was returnd to origional state since the actual
		// groups size on the task was zero
		assertTrue(smsBillingImpl.convertCreditsToAmount(
				smsAccount.getCredits()).equals(origionalAccBalance));

	}

	/**
	 * Test cancel pending request.
	 */
	public void testCancelPendingRequest() {
		Long origionalCreditBalance = 100L;
		int creditEstimate = 50;

		SmsAccount smsAccount = new SmsAccount();
		smsAccount.setSakaiUserId("4");
		smsAccount.setSakaiSiteId("4");
		smsAccount.setMessageTypeCode("12345");
		smsAccount.setOverdraftLimit(1000L);
		smsAccount.setCredits(origionalCreditBalance);
		smsAccount.setAccountName("accountName");
		smsAccount.setAccountEnabled(true);
		hibernateLogicLocator.getSmsAccountLogic()
				.persistSmsAccount(smsAccount);

		SmsTask smsTask = new SmsTask();
		smsTask.setSakaiSiteId("sakaiSiteId");
		smsTask.setSenderUserName("sakaiUserId");
		smsTask.setSmsAccountId(smsAccount.getId());
		smsTask.setDateCreated(new Timestamp(System.currentTimeMillis()));
		smsTask.setDateToSend(new Timestamp(System.currentTimeMillis()));
		smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		smsTask.setAttemptCount(2);
		smsTask.setMessageBody("messageBody");
		smsTask.setSenderUserName("senderUserName");
		smsTask.setMaxTimeToLive(1);
		smsTask.setDelReportTimeoutDuration(1);
		smsTask.setCreditEstimate(creditEstimate);
		Calendar cal = Calendar.getInstance();
		cal.setTime(smsTask.getDateToSend());
		cal.add(Calendar.SECOND, smsTask.getMaxTimeToLive());
		smsTask.setDateToExpire(cal.getTime());
		hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);

		smsBillingImpl.reserveCredits(smsTask);

		// Check the credits have been reserved.
		SmsAccount retAccount = hibernateLogicLocator.getSmsAccountLogic()
				.getSmsAccount(smsAccount.getId());
		assertNotNull(retAccount);
		assertTrue(retAccount.getCredits() < origionalCreditBalance);

		smsBillingImpl.cancelPendingRequest(smsTask.getId());
		// Check the credits have been reserved.
		retAccount = hibernateLogicLocator.getSmsAccountLogic().getSmsAccount(
				smsAccount.getId());
		assertNotNull(retAccount);
		assertTrue(retAccount.getCredits().equals(origionalCreditBalance));
	}

	/**
	 * Test credit late message.
	 */
	public void testDebitLateMessage() {
		Long origionalAccountBalance = 100L;
		int creditEstimate = 50;

		SmsAccount smsAccount = new SmsAccount();
		smsAccount.setSakaiUserId("5");
		smsAccount.setSakaiSiteId("5");
		smsAccount.setMessageTypeCode("12345");
		smsAccount.setOverdraftLimit(1000L);
		smsAccount.setCredits(origionalAccountBalance);
		smsAccount.setAccountName("accountName");
		smsAccount.setAccountEnabled(true);
		hibernateLogicLocator.getSmsAccountLogic()
				.persistSmsAccount(smsAccount);

		SmsTask smsTask = new SmsTask();
		smsTask.setSakaiSiteId("sakaiSiteId");
		smsTask.setSenderUserName("sakaiUserId");
		smsTask.setSmsAccountId(smsAccount.getId());
		smsTask.setDateCreated(new Timestamp(System.currentTimeMillis()));
		smsTask.setDateToSend(new Timestamp(System.currentTimeMillis()));
		smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		smsTask.setAttemptCount(2);
		smsTask.setMessageBody("messageBody");
		smsTask.setSenderUserName("senderUserName");
		smsTask.setMaxTimeToLive(1);
		smsTask.setDelReportTimeoutDuration(1);
		smsTask.setCreditEstimate(creditEstimate);
		Calendar cal = Calendar.getInstance();
		cal.setTime(smsTask.getDateToSend());
		cal.add(Calendar.SECOND, smsTask.getMaxTimeToLive());
		smsTask.setDateToExpire(cal.getTime());
		hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);

		SmsMessage smsMessage = new SmsMessage();
		smsMessage.setMobileNumber("0721998919");
		smsMessage.setSmscMessageId("criterai");
		smsMessage.setSakaiUserId("criterai");
		smsMessage.setDateDelivered(new Date(System.currentTimeMillis()));
		smsMessage.setStatusCode(SmsConst_DeliveryStatus.STATUS_SENT);
		smsMessage.setSmsTask(smsTask);

		smsBillingImpl.debitLateMessage(smsMessage);
		// Check the account balance was deducted from
		SmsAccount retAccount = hibernateLogicLocator.getSmsAccountLogic()
				.getSmsAccount(smsAccount.getId());
		assertNotNull(retAccount);
		assertTrue(retAccount.getCredits() < origionalAccountBalance);
	}

	/**
	 * Test debit account.
	 */
	public void testCreditAccount() {
		Long origionalCreditBalance = 0L;

		SmsAccount smsAccount = new SmsAccount();
		smsAccount.setSakaiUserId("6");
		smsAccount.setSakaiSiteId("6");
		smsAccount.setMessageTypeCode("12345");
		smsAccount.setOverdraftLimit(1000L);
		smsAccount.setCredits(origionalCreditBalance);
		smsAccount.setAccountName("accountName");
		smsAccount.setAccountEnabled(true);
		hibernateLogicLocator.getSmsAccountLogic()
				.persistSmsAccount(smsAccount);

		smsBillingImpl.creditAccount(smsAccount.getId(), 100L);
		SmsAccount retAccount = hibernateLogicLocator.getSmsAccountLogic()
				.getSmsAccount(smsAccount.getId());
		assertNotNull(retAccount);
		assertTrue(retAccount.getCredits() > origionalCreditBalance);
		assertTrue(retAccount.getCredits() == 100L);
	}

	// ///////////////////////////////////////
	// HELPER METHODS
	// ///////////////////////////////////////

	/**
	 * Gets the sms transactions.
	 *
	 * @param smsAccount
	 *            the sms account
	 *
	 * @return the sms transactions
	 */
	private void insertTestTransactionsForAccount(SmsAccount smsAccount) {

		int g = 10000;
		for (int i = 0; i < 10; i++) {
			SmsTransaction smsTransaction = new SmsTransaction();
			smsTransaction.setCreditBalance(10L);
			smsTransaction.setSakaiUserId("sakaiUserId" + i);
			smsTransaction.setTransactionDate(new Date(System
					.currentTimeMillis()
					+ g));
			smsTransaction.setTransactionTypeCode("TC");
			smsTransaction.setTransactionCredits(666);
			smsTransaction.setSmsAccount(smsAccount);
			smsTransaction.setSmsTaskId(1L);
			g += 1000;
			hibernateLogicLocator.getSmsTransactionLogic()
					.insertReserveTransaction(smsTransaction);
		}
	}

}
