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
package org.sakaiproject.sms.logic.smpp.simulatorrequired.test;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

import org.sakaiproject.sms.logic.smpp.impl.SmsBillingImpl;
import org.sakaiproject.sms.logic.smpp.impl.SmsSmppImpl;
import org.sakaiproject.sms.model.SmsAccount;
import org.sakaiproject.sms.model.SmsMessage;
import org.sakaiproject.sms.model.SmsTask;
import org.sakaiproject.sms.model.SmsTransaction;
import org.sakaiproject.sms.model.constants.SmsConst_DeliveryStatus;
import org.sakaiproject.sms.model.constants.SmsConstants;
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

	/** The test amount. */
	public final Float testAmount = 100F;

	/** The test credits. */
	public final int testCredits = 100;

	@BeforeClass
	public static void beforeClass(){
		if (!SmsConstants.isDbSchemaCreated) {
			smsDao.createSchema();
			SmsConstants.isDbSchemaCreated = true;
		}

		smsBillingImpl = new SmsBillingImpl();
		smsBillingImpl.setHibernateLogicLocator(hibernateLogicLocator);
		smsSmppImpl = new SmsSmppImpl();
		smsSmppImpl.setHibernateLogicLocator(hibernateLogicLocator);
		smsSmppImpl.init();

		account = new SmsAccount();
		account.setSakaiSiteId("121112322");
		account.setMessageTypeCode("");
		account.setCredits(10L);
		account.setAccountName("SmsBillingTest account name2");
		account.setStartdate(new Date());
		account.setAccountEnabled(true);
		hibernateLogicLocator.getSmsAccountLogic().persistSmsAccount(account);
	}

	/**
	 * Test check sufficient credits are not available.
	 */
    @Test
	public void testCheckSufficientCredits_False() {

		int creditsRequired = 11;

		boolean sufficientCredits = smsBillingImpl.checkSufficientCredits(
				account.getId(), creditsRequired);
		assertFalse("Expected insufficient credit", sufficientCredits);
	}

	/**
	 * Test check sufficient credits are available from overdraft.
	 */
    @Test
	public void testCheckSufficientCredits_OverdraftFacility() {

		int creditsRequired = 11;

		SmsMessage msg = new SmsMessage();
		SmsTask smsTask = new SmsTask();
		smsTask.setSakaiSiteId(SmsConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID);
		smsTask.setSmsAccountId(account.getId());
		smsTask.setDateCreated(new Timestamp(System.currentTimeMillis()));
		smsTask.setDateToSend(new Timestamp(System.currentTimeMillis()));
		smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		smsTask.setAttemptCount(2);
		smsTask.setMessageBody(SmsConstants.SMS_DEV_DEFAULT_SMS_MESSAGE_BODY);
		smsTask.setSenderUserName("senderUserName");
		smsTask.setMaxTimeToLive(1);
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
    @Test
	public void testCheckSufficientCredits_True() {

		int creditsRequired = 1;
		account.setCredits(10l);
		hibernateLogicLocator.getSmsAccountLogic().persistSmsAccount(account);
		SmsMessage msg = new SmsMessage();
		SmsTask smsTask = new SmsTask();
		smsTask.setSakaiSiteId(SmsConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID);
		smsTask.setSmsAccountId(account.getId());
		smsTask.setDateCreated(new Timestamp(System.currentTimeMillis()));
		smsTask.setDateToSend(new Timestamp(System.currentTimeMillis()));
		smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		smsTask.setAttemptCount(2);
		smsTask.setMessageBody(SmsConstants.SMS_DEV_DEFAULT_SMS_MESSAGE_BODY);
		smsTask.setSenderUserName("senderUserName");
		smsTask.setMaxTimeToLive(1);
		smsTask.getSmsMessages().add(msg);
		smsTask.setCreditEstimate(creditsRequired);
		msg.setSmsTask(smsTask);
		boolean sufficientCredits = smsBillingImpl.checkSufficientCredits(
				account.getId(), creditsRequired);
		assertTrue("Expected sufficient credit", sufficientCredits);

		account.setCredits(0l);
		hibernateLogicLocator.getSmsAccountLogic().persistSmsAccount(account);
		boolean insufficientCredits = !smsBillingImpl.checkSufficientCredits(
				account.getId(), creditsRequired);
		assertTrue("Expected insufficient credit", insufficientCredits);

	}

	/**
	 * Test convert amount to credits.
	 */
    @Test
	public void testConvertAmountToCredits() {
		double amount = smsBillingImpl.convertCreditsToAmount(testCredits);
		double credits = smsBillingImpl.convertAmountToCredits(amount);
		assertTrue(credits == testCredits);
	}

	/**
	 * Test convert credits to amount.
	 */
    @Test
	public void testConvertCreditsToAmount() {
		smsBillingImpl.convertAmountToCredits(testAmount);

		// Not sure how to test this properly.
	}

	/**
	 * Test recalculate account balance.
	 */
    @Test
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
    @Test
	public void testReserveCredits() {

		int creditEstimate = 50;
		long origionalAccBalance = 100;

		SmsAccount smsAccount = new SmsAccount();
		smsAccount.setSakaiUserId("2");
		smsAccount.setSakaiSiteId("2");
		smsAccount.setMessageTypeCode("2");
		smsAccount.setOverdraftLimit(1000L);
		smsAccount.setCredits(origionalAccBalance);
		smsAccount.setAccountName("accountname");
		smsAccount.setAccountEnabled(true);
		hibernateLogicLocator.getSmsAccountLogic()
				.persistSmsAccount(smsAccount);

		SmsTask smsTask = new SmsTask();
		smsTask.setSakaiSiteId(SmsConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID);
		smsTask.setSenderUserName("sakaiUserId");
		smsTask.setSmsAccountId(smsAccount.getId());
		smsTask.setDateCreated(new Timestamp(System.currentTimeMillis()));
		smsTask.setDateToSend(new Timestamp(System.currentTimeMillis()));
		smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		smsTask.setAttemptCount(2);
		smsTask.setMessageBody(SmsConstants.SMS_DEV_DEFAULT_SMS_MESSAGE_BODY);
		smsTask.setSenderUserName("senderUserName");
		smsTask.setMaxTimeToLive(1);
		smsTask.setCreditEstimate(creditEstimate);
		smsTask.setMessageTypeId(SmsConstants.MESSAGE_TYPE_SYSTEM_ORIGINATING);
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
    @Test
	public void testSettleCreditDifference() {
		int creditEstimate = 50;
		Long originalAccBalance = 150l;

		SmsAccount smsAccount = new SmsAccount();
		smsAccount.setSakaiUserId("3");
		smsAccount.setSakaiSiteId("3");
		smsAccount.setMessageTypeCode("3");
		smsAccount.setOverdraftLimit(1000L);
		smsAccount.setCredits(originalAccBalance);
		smsAccount.setAccountName("accountname");
		smsAccount.setAccountEnabled(true);
		hibernateLogicLocator.getSmsAccountLogic()
				.persistSmsAccount(smsAccount);

		SmsTask smsTask = new SmsTask();
		smsTask.setSakaiSiteId(SmsConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID);
		smsTask.setSenderUserName("sakaiUserId");
		smsTask.setSmsAccountId(smsAccount.getId());
		smsTask.setDateCreated(new Timestamp(System.currentTimeMillis()));
		smsTask.setDateToSend(new Timestamp(System.currentTimeMillis()));
		smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		smsTask.setAttemptCount(2);
		smsTask.setMessageBody(SmsConstants.SMS_DEV_DEFAULT_SMS_MESSAGE_BODY);
		smsTask.setSenderUserName("senderUserName");
		smsTask.setMaxTimeToLive(1);
		smsTask.setCreditEstimate(creditEstimate);
		smsTask.setGroupSizeActual(0);
		smsTask.setMessageTypeId(SmsConstants.MESSAGE_TYPE_SYSTEM_ORIGINATING);
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
		assertTrue(smsAccount.getCredits() < originalAccBalance);

		smsBillingImpl.settleCreditDifference(smsTask, smsTask.getCreditEstimate(), smsTask.getCreditsActual());

		smsAccount = hibernateLogicLocator.getSmsAccountLogic().getSmsAccount(
				smsAccount.getId());

		// Account balance was returnd to origional state since the actual
		// groups size on the task was zero
		assertTrue(smsAccount.getCredits() == originalAccBalance);

	}

	/**
	 * Test cancel pending request.
	 */
    @Test
	public void testCancelPendingRequest() {
		Long originalCreditBalance = 100L;
		int creditEstimate = 50;

		SmsAccount smsAccount = new SmsAccount();
		smsAccount.setSakaiUserId("4");
		smsAccount.setSakaiSiteId("4");
		smsAccount.setMessageTypeCode("12345");
		smsAccount.setOverdraftLimit(1000L);
		smsAccount.setCredits(originalCreditBalance);
		smsAccount.setAccountName("accountName");
		smsAccount.setAccountEnabled(true);
		hibernateLogicLocator.getSmsAccountLogic()
				.persistSmsAccount(smsAccount);

		SmsTask smsTask = new SmsTask();
		smsTask.setSakaiSiteId(SmsConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID);
		smsTask.setSenderUserName("sakaiUserId");
		smsTask.setSmsAccountId(smsAccount.getId());
		smsTask.setDateCreated(new Timestamp(System.currentTimeMillis()));
		smsTask.setDateToSend(new Timestamp(System.currentTimeMillis()));
		smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		smsTask.setAttemptCount(2);
		smsTask.setMessageBody(SmsConstants.SMS_DEV_DEFAULT_SMS_MESSAGE_BODY);
		smsTask.setSenderUserName("senderUserName");
		smsTask.setMaxTimeToLive(1);
		smsTask.setMessageTypeId(SmsConstants.MESSAGE_TYPE_SYSTEM_ORIGINATING);
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
		assertTrue(retAccount.getCredits() < originalCreditBalance);

		smsBillingImpl.cancelPendingRequest(smsTask.getId());
		// Check the credits have been reserved.
		retAccount = hibernateLogicLocator.getSmsAccountLogic().getSmsAccount(
				smsAccount.getId());
		assertNotNull(retAccount);
		assertTrue(retAccount.getCredits() == originalCreditBalance);
	}

	/**
	 * Test credit late message.
	 */
    @Test
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
		smsTask.setSakaiSiteId(SmsConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID);
		smsTask.setSenderUserName("sakaiUserId");
		smsTask.setSmsAccountId(smsAccount.getId());
		smsTask.setDateCreated(new Timestamp(System.currentTimeMillis()));
		smsTask.setDateToSend(new Timestamp(System.currentTimeMillis()));
		smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		smsTask.setAttemptCount(2);
		smsTask.setMessageBody(SmsConstants.SMS_DEV_DEFAULT_SMS_MESSAGE_BODY);
		smsTask.setSenderUserName("senderUserName");
		smsTask.setMaxTimeToLive(1);
		smsTask.setCreditEstimate(creditEstimate);
		Calendar cal = Calendar.getInstance();
		cal.setTime(smsTask.getDateToSend());
		cal.add(Calendar.SECOND, smsTask.getMaxTimeToLive());
		smsTask.setDateToExpire(cal.getTime());
		hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);

		smsBillingImpl.debitLateMessages(smsTask, 1);
		// Check the account balance was deducted from
		SmsAccount retAccount = hibernateLogicLocator.getSmsAccountLogic()
				.getSmsAccount(smsAccount.getId());
		assertNotNull(retAccount);
		assertTrue(retAccount.getCredits() < origionalAccountBalance);
	}

	/**
	 * Test debit account.
	 */
    @Test
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

		smsBillingImpl.creditAccount(smsAccount.getId(), 100L, "something");
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

	/**
	 * Test check sufficient credits when account is enddated
	 */
    @Test
	public void testCheckSufficientCredits_Enddated() {

		int creditsRequired = 11;
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.HOUR, -2);
		account.setEnddate(cal.getTime());
		hibernateLogicLocator.getSmsAccountLogic().persistSmsAccount(account);
		boolean sufficientCredits = smsBillingImpl.checkSufficientCredits(
				account.getId(), creditsRequired);
		assertFalse("Expected insufficient credit", sufficientCredits);
		account.setEnddate(null); // restore
		hibernateLogicLocator.getSmsAccountLogic().persistSmsAccount(account);
	}
}
