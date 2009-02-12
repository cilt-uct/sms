/***********************************************************************************
 * SmsBillingImpl.java
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
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.sms.logic.smpp.impl;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.sakaiproject.sms.logic.hibernate.exception.SmsAccountNotFoundException;
import org.sakaiproject.sms.logic.impl.hibernate.HibernateLogicFactory;
import org.sakaiproject.sms.logic.smpp.SmsBilling;
import org.sakaiproject.sms.model.hibernate.SmsAccount;
import org.sakaiproject.sms.model.hibernate.SmsConfig;
import org.sakaiproject.sms.model.hibernate.SmsMessage;
import org.sakaiproject.sms.model.hibernate.SmsTask;
import org.sakaiproject.sms.model.hibernate.SmsTransaction;

// TODO: Auto-generated Javadoc
/**
 * The billing service will handle all financial functions for the sms tool in
 * Sakai.
 *
 * @author Julian Wyngaard
 * @version 1.0
 * @created 12-Dec-2008
 */
public class SmsBillingImpl implements SmsBilling {

	/**
	 * Debit an account by the supplied amount of credits.
	 *
	 * @param accountId
	 * @param creditsToDebit
	 */
	public void debitAccount(Long accountId, long creditsToDebit) {

		if (creditsToDebit < 0) {
			throw new RuntimeException(
					"The amount of credits supplied to debit an account must be positive");
		}

		SmsAccount account = HibernateLogicFactory.getAccountLogic()
				.getSmsAccount(accountId);

		SmsTransaction smsTransaction = new SmsTransaction();
		smsTransaction.setTransactionCredits(0);
		smsTransaction.setCreditBalance(((creditsToDebit)));
		smsTransaction.setSakaiUserId(account.getSakaiUserId());
		smsTransaction.setSmsAccount(account);
		smsTransaction.setSmsTaskId(0L);

		HibernateLogicFactory.getTransactionLogic()
				.insertDebitAccountTransaction(smsTransaction);
	}

	/**
	 * Add extra credits to the specific account by making an entry into
	 * SMS_TRANSACTION Also update the available credits on the account.
	 *
	 * @param accountID
	 *            the account id
	 * @param creditCount
	 *            the credit count
	 */
	public void allocateCredits(Long accountID, int creditCount) {
		// TODO Auto-generated method stub

	}

	/**
	 *
	 * Return true of the account has the required credits available. Take into
	 * account overdraft limits, if applicable.
	 *
	 * @param smsTask
	 * @return
	 */
	public boolean checkSufficientCredits(SmsTask smsTask) {
		return this.checkSufficientCredits(smsTask.getSmsAccountId(), smsTask
				.getCreditEstimate());
	}

	/**
	 * Return true of the account has the required credits available. Take into
	 * account overdraft limits, if applicable.
	 *
	 * @param accountID
	 *            the account id
	 * @param creditsRequired
	 *            the credits required
	 *
	 * @return true, if sufficient credits
	 */
	public boolean checkSufficientCredits(Long accountID,
			Integer creditsRequired) {
		SmsAccount account = HibernateLogicFactory.getAccountLogic()
				.getSmsAccount(accountID);

		// Account is null or disabled
		if (account == null
				|| !account.getAccountEnabled()
				|| creditsRequired == null
				|| creditsRequired < 0
				|| (account.getEnddate() != null && account.getEnddate()
						.before(new Date()))) {
			return false;
		}

		boolean sufficientCredit = false;
		if (account.getOverdraftLimit() != null) {
			if ((account.getCredits() + account
					.getOverdraftLimit()) >= creditsRequired) {
				sufficientCredit = true;
			}
		} else if (account.getCredits() >= creditsRequired) {
			sufficientCredit = true;
		}

		return sufficientCredit;
	}

	/**
	 * Convert amount to credits.
	 *
	 * @param amount
	 *            the amount
	 *
	 * @return the double
	 */
	public Long convertAmountToCredits(Float amount) {
		SmsConfig config = HibernateLogicFactory.getConfigLogic()
				.getOrCreateSystemSmsConfig();
		Float result = (amount / config.getCreditCost());
		return result.longValue();
	}

	/**
	 * Convert the given credits to currency base on the defined conversion
	 * value at the given time.
	 *
	 * @param creditCount
	 *            the credit count
	 *
	 * @return the credit amount
	 */
	public Float convertCreditsToAmount(long creditCount) {
		SmsConfig config = HibernateLogicFactory.getConfigLogic()
				.getOrCreateSystemSmsConfig();
		return config.getCreditCost() * creditCount;
	}

	/**
	 * Return the currency amount available in the account.
	 *
	 * @param accountID
	 *            the account id
	 *
	 * @return the account balance
	 */
	public double getAccountBalance(Long accountID) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * Return credits available in the account.
	 *
	 * @param accountID
	 *            the account id
	 *
	 * @return the account credits
	 */
	public int getAccountCredits(Long accountID) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * Use Sakai siteID, Sakai userID and account type to get a valid account
	 * id. AccountType is only outgoing masses for now.
	 *
	 * @param sakaiSiteID
	 *            (e.g. !admin)
	 * @param sakaiUserID
	 *            the sakai user id
	 *
	 * @return the account id
	 *
	 * @throws SmsAccountNotFoundException
	 *             the sms account not found exception
	 */
	public Long getAccountID(String sakaiSiteID, String sakaiUserID)
			throws SmsAccountNotFoundException {
		SmsAccount account = HibernateLogicFactory.getAccountLogic()
				.getSmsAccount(sakaiSiteID, sakaiUserID);
		if (account != null) {
			return account.getId();
		} else {
			throw new SmsAccountNotFoundException();

		}

	}

	/**
	 * Return a list of all transactions between startDate and endDate for the
	 * specific account.
	 *
	 * @param accountID
	 *            the account id
	 * @param startDate
	 *            the start date
	 * @param endDate
	 *            the end date
	 *
	 * @return the acc transactions
	 */
	public Set getAccTransactions(Long accountID, Date startDate, Date endDate) {
		// TODO Auto-generated method stub
		return null;

	}

	/**
	 * Return all accounts linked to the given Sakai site.
	 *
	 * @param sakaiSiteID
	 *            the sakai site id
	 *
	 * @return the all site accounts
	 */
	public Set getAllSiteAccounts(String sakaiSiteID) {
		// TODO Auto-generated method stub
		return null;

	}

	/**
	 * Insert a new account and return the new account id.
	 *
	 * @param sakaiSiteID
	 *            the sakai site id
	 *
	 * @return true, if insert account
	 */
	public boolean insertAccount(String sakaiSiteID) {
		return false;
	}

	/**
	 * Insert a new transaction for the given account id.
	 *
	 * @param accountID
	 *            the account id
	 * @param transCodeID
	 *            the trans code id
	 * @param creditAmount
	 * @return true, if insert transaction the credit amount
	 *
	 */
	public Boolean insertTransaction(Long accountID, int transCodeID,
			int creditAmount) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Insert a new transaction and indicate that the credits are reserved. If
	 * the request is pending and the administrator delete the request, the
	 * reservation must be rolled back with another transaction.
	 *
	 * @param smsTask
	 *            the sms task
	 *
	 * @return true, if reserve credits
	 */
	public boolean reserveCredits(SmsTask smsTask) {

		SmsAccount account = HibernateLogicFactory.getAccountLogic()
				.getSmsAccount(smsTask.getSmsAccountId());
		if (account == null) {
			// Account does not existaccount.getCredits() -
			return false;
		}

		SmsTransaction smsTransaction = new SmsTransaction();

		// Set transaction credit and Credits to negative number because we are
		// reserving.
		int credits = smsTask.getCreditEstimate() * -1;
		smsTransaction.setCreditBalance(new Long(credits));
		smsTransaction.setTransactionCredits(credits);
		smsTransaction.setSakaiUserId(smsTask.getSenderUserName());
		smsTransaction.setSmsAccount(account);
		smsTransaction.setSmsTaskId(smsTask.getId());

		// Insert credit transaction
		HibernateLogicFactory.getTransactionLogic().insertReserveTransaction(
				smsTransaction);
		return true;

	}

	/**
	 * Credits account for a message that came in late.
	 *
	 * @param smsTask
	 * @return true, if successful
	 */
	public boolean creditLateMessage(SmsMessage smsMessage) {
		SmsTask smsTask = smsMessage.getSmsTask();
		SmsAccount account = HibernateLogicFactory.getAccountLogic()
				.getSmsAccount(smsTask.getSmsAccountId());
		if (account == null) {
			// Account does not exist
			return false;
		}

		SmsTransaction smsTransaction = new SmsTransaction();

		// The juicy bits

		smsTransaction.setCreditBalance((- 1L));
		smsTransaction.setTransactionCredits(1);
		smsTransaction.setSakaiUserId(smsTask.getSenderUserName());
		smsTransaction.setSmsAccount(account);
		smsTransaction.setSmsTaskId(smsTask.getId());

		HibernateLogicFactory.getTransactionLogic()
				.insertLateMessageTransaction(smsTransaction);

		return true;

	}

	/**
	 * Recalculate balance for a specific account.
	 *
	 * @param accountId
	 *            the account id
	 * @param account
	 *            the account
	 */
	private void recalculateAccountBalance(Long accountId, SmsAccount account) {
		HibernateLogicFactory.getAccountLogic().recalculateAccountBalance(
				accountId, account);
	}

	/**
	 * Recalculate balance for a specific account.
	 *
	 * @param account
	 *            the account
	 */
	private void recalculateAccountBalance(SmsAccount account) {
		recalculateAccountBalance(null, account);
	}

	/**
	 * Recalculate balance for a specific account.
	 *
	 * @param accountId
	 *            the account id
	 */
	public void recalculateAccountBalance(Long accountId) {
		recalculateAccountBalance(accountId, null);
	}

	/**
	 * Recalculate balances for all existing accounts.
	 */
	public void recalculateAccountBalances() {
		List<SmsAccount> accounts = HibernateLogicFactory.getAccountLogic()
				.getAllSmsAccounts();
		for (SmsAccount account : accounts) {
			recalculateAccountBalance(account);
		}
	}

	/**
	 * Cancel pending request.
	 *
	 * @param smsTaskId
	 *            the sms task id
	 *
	 * @return true, if successful
	 */
	public boolean cancelPendingRequest(Long smsTaskId) {

		SmsTask smsTask = HibernateLogicFactory.getTaskLogic().getSmsTask(
				smsTaskId);
		SmsAccount smsAccount = HibernateLogicFactory.getAccountLogic()
				.getSmsAccount(smsTask.getSmsAccountId());
		SmsTransaction origionalTransaction = HibernateLogicFactory
				.getTransactionLogic()
				.getCancelSmsTransactionForTask(smsTaskId);

		SmsTransaction smsTransaction = new SmsTransaction();

		// The juicy bits
		int transactionCredits = origionalTransaction.getTransactionCredits()
				* -1;// Reverse the sign cause we are deducting from the account
		smsTransaction.setTransactionCredits(transactionCredits);
		smsTransaction.setCreditBalance(new Long(transactionCredits));

		smsTransaction.setSakaiUserId(smsTask.getSenderUserName());
		smsTransaction.setSmsAccount(smsAccount);
		smsTransaction.setSmsTaskId(smsTask.getId());

		HibernateLogicFactory.getTransactionLogic()
				.insertCancelPendingRequestTransaction(smsTransaction);

		return false;
	}

	/**
	 * Settle credit difference. The group size might have change since the time
	 * that the task was requested. So we need to calculate the difference and
	 * settle the account.
	 *
	 * @param smsTask
	 *            the sms task
	 *
	 * @return true, if successful
	 */
	public boolean settleCreditDifference(SmsTask smsTask) {

		SmsAccount account = HibernateLogicFactory.getAccountLogic()
				.getSmsAccount(smsTask.getSmsAccountId());
		if (account == null) {
			// Account does not exist
			return false;
		}

		SmsTransaction smsTransaction = new SmsTransaction();

		// The juicy bits
		int creditEstimate = smsTask.getCreditEstimateInt();
		int actualCreditsUsed = smsTask.getMessagesDelivered();
		int transactionCredits = creditEstimate - actualCreditsUsed;
		smsTransaction.setCreditBalance(new Long(transactionCredits));
		smsTransaction.setTransactionCredits(transactionCredits);

		smsTransaction.setSakaiUserId(smsTask.getSenderUserName());
		smsTransaction.setSmsAccount(account);
		smsTransaction.setSmsTaskId(smsTask.getId());

		HibernateLogicFactory.getTransactionLogic().insertSettleTransaction(
				smsTransaction);

		return true;
	}

}
