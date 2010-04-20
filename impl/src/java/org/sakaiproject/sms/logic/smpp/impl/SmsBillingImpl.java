/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.sms.logic.smpp.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.logic.HibernateLogicLocator;
import org.sakaiproject.sms.logic.exception.SmsAccountNotFoundException;
import org.sakaiproject.sms.logic.exception.SmsInsufficientCreditsException;
import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.smpp.SmsBilling;
import org.sakaiproject.sms.model.SmsAccount;
import org.sakaiproject.sms.model.SmsConfig;
import org.sakaiproject.sms.model.SmsTask;
import org.sakaiproject.sms.model.SmsTransaction;
import org.sakaiproject.sms.model.constants.SmsConstants;

/**
 * The billing service will handle all financial functions for the sms tool in
 * Sakai.
 * 
 * @author Julian Wyngaard
 * @version 1.0
 * @created 12-Dec-2008
 */
public class SmsBillingImpl implements SmsBilling {

	// Transaction Type properties
	private final static Properties PROPERTIES = new Properties();

	private final static Log LOG = LogFactory.getLog(SmsBillingImpl.class);

	private HibernateLogicLocator hibernateLogicLocator;

	public HibernateLogicLocator getHibernateLogicLocator() {
		return hibernateLogicLocator;
	}

	private ExternalLogic externalLogic;

	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	public void setHibernateLogicLocator(
			HibernateLogicLocator hibernateLogicLocator) {
		this.hibernateLogicLocator = hibernateLogicLocator;
	}

	public void init() {
		try {
			final InputStream inputStream = this.getClass()
					.getResourceAsStream("/transaction_codes.properties");
			if (inputStream == null) {
				final FileInputStream fileInputStream = new FileInputStream(
						"transaction_codes.properties");

				PROPERTIES.load(fileInputStream);
				if (fileInputStream != null) {
					fileInputStream.close();
				}

			} else {
				PROPERTIES.load(inputStream);
			}
			if (inputStream != null) {
				inputStream.close();
			}
		} catch (FileNotFoundException e) {
			LOG.error(e.getMessage(), e);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	/**
	 * Credits an account by the supplied amount of credits.
	 * 
	 * @param accountId
	 * @param creditsToDebit
	 */
	public void creditAccount(final Long accountId,
			final double creditsToDebit, String description) {

		final SmsAccount account = hibernateLogicLocator.getSmsAccountLogic()
				.getSmsAccount(accountId);

		SmsTransaction smsTransaction = new SmsTransaction();
		smsTransaction.setTransactionCredits(creditsToDebit);
		smsTransaction.setCreditBalance(creditsToDebit);
		smsTransaction.setSakaiUserId(account.getSakaiUserId());
		smsTransaction.setSmsAccount(account);
		smsTransaction.setSmsTaskId(0L);
		smsTransaction.setSakaiUserId(hibernateLogicLocator.getExternalLogic()
				.getCurrentUserId());
		smsTransaction.setDescription(description);
		
		hibernateLogicLocator.getSmsTransactionLogic()
				.insertCreditAccountTransaction(smsTransaction);

		String txRef = "/sms-account/" + account.getId() + "/transaction/"
				+ smsTransaction.getId();
		externalLogic.postEvent(ExternalLogic.SMS_EVENT_ACCOUNT_CREDIT, txRef,
				null);
	}

	/**
	 * 
	 * Return true of the account has the required credits available. Take into
	 * account overdraft limits, if applicable.
	 * 
	 * @param smsTask
	 * @parm overDraftCheck
	 * @return
	 */
	public boolean checkSufficientCredits(SmsTask smsTask,
			boolean overDraftCheck) {
		return this.checkSufficientCredits(smsTask.getSmsAccountId(), smsTask
				.getCreditEstimate(), overDraftCheck);
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
	 * Return true of the account has the required credits available.
	 * 
	 * @param accountID
	 *            the account id
	 * @param creditsRequired
	 *            the credits required
	 * 
	 * @return true, if sufficient credits
	 */
	public boolean checkSufficientCredits(Long accountID,
			double creditsRequired) {

		return this.checkSufficientCredits(accountID, creditsRequired, false);
	}

	/**
	 * Return true of the account has the required credits available.
	 * 
	 * @param accountID
	 *            the account id
	 * @param creditsRequired
	 *            the credits required
	 * @param overDraftCheck
	 *            the overDraftCheck
	 * 
	 * @return true, if sufficient credits
	 */
	public boolean checkSufficientCredits(Long accountID,
			double creditsRequired, boolean overDraftCheck) {
		final SmsAccount account = hibernateLogicLocator.getSmsAccountLogic()
				.getSmsAccount(accountID);

		// Account is null or disabled
		if (account == null
				|| !account.getAccountEnabled()
				|| creditsRequired < 0
				|| (account.getEnddate() != null && account.getEnddate()
						.before(new Date()))) {
			return false;
		}

		boolean sufficientCredit = false;

		double availableCredits = account.getCredits();

		if (overDraftCheck) {
			availableCredits += account.getOverdraftLimit();
		}

		if (availableCredits >= creditsRequired) {
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
	public double convertAmountToCredits(double amount) {
		final SmsConfig config = hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSystemSmsConfig();
		return (amount / config.getCreditCost());
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
	public double convertCreditsToAmount(double creditCount) {
		SmsConfig config = hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSystemSmsConfig();
		return config.getCreditCost() * creditCount;
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
		SmsAccount account = hibernateLogicLocator.getSmsAccountLogic()
				.getSmsAccount(sakaiSiteID, sakaiUserID);
		if (account == null) {
			throw new SmsAccountNotFoundException();

		} else {
			return account.getId();

		}

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

		SmsAccount account = hibernateLogicLocator.getSmsAccountLogic()
				.getSmsAccount(smsTask.getSmsAccountId());

		if (account == null) {
			// Account does not existaccount.getCredits() -
			return false;
		}

		if (smsTask.getMessageTypeId().equals(
				SmsConstants.MESSAGE_TYPE_MOBILE_ORIGINATING)) {
			if (!checkSufficientCredits(smsTask, true)) {
				return false;
			}
		} else {
			if (!checkSufficientCredits(smsTask, false)) {
				return false;
			}
		}

		SmsTransaction smsTransaction = new SmsTransaction();

		// Set transaction credit and Credits to negative number because we are
		// reserving.
		double credits = smsTask.getCreditEstimate() * -1;
		smsTransaction.setCreditBalance(credits);
		smsTransaction.setTransactionCredits(credits);
		smsTransaction.setSakaiUserId(smsTask.getSenderUserId());
		smsTransaction.setSmsAccount(account);
		smsTransaction.setSmsTaskId(smsTask.getId());

		// Insert credit transaction
		hibernateLogicLocator.getSmsTransactionLogic()
				.insertReserveTransaction(smsTransaction);
		return true;

	}

	/**
	 * Credits account for a message that came in late.
	 * 
	 * @param smsTask
	 * @return true, if successful
	 */
	public boolean debitLateMessages(SmsTask smsTask, double credits) {
		SmsAccount account = hibernateLogicLocator.getSmsAccountLogic()
				.getSmsAccount(smsTask.getSmsAccountId());
		if (account == null) {
			// Account does not exist
			return false;
		}

		SmsTransaction smsTransaction = new SmsTransaction();

		// The juicy bits

		smsTransaction.setCreditBalance((-1L));
		smsTransaction.setTransactionCredits(-1 * credits);
		smsTransaction.setSakaiUserId(smsTask.getSenderUserId());
		smsTransaction.setSmsAccount(account);
		smsTransaction.setSmsTaskId(smsTask.getId());

		hibernateLogicLocator.getSmsTransactionLogic()
				.insertLateMessageTransaction(smsTransaction);

		return true;

	}

	/**
	 * Credits account for a message that came in late.
	 * 
	 * @param smsTask
	 * @return true, if successful
	 */
	public boolean debitIncomingMessage(SmsAccount account, double credits, Long replyTaskId) {
		
		if (account == null) {
			// Account does not exist
			return false;
		}

		SmsTransaction smsTransaction = new SmsTransaction();

		// The juicy bits

		smsTransaction.setCreditBalance((-1L));
		smsTransaction.setTransactionCredits(-1 * credits);
		smsTransaction.setSakaiUserId(SmsConstants.DEFAULT_MO_SENDER_USERNAME);
		smsTransaction.setSmsAccount(account);
		smsTransaction.setSmsTaskId(replyTaskId);

		hibernateLogicLocator.getSmsTransactionLogic()
				.insertIncomingMessageTransaction(smsTransaction);

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
	private void recalculateAccountBalance(Long accountId,
			SmsAccount account) {
		hibernateLogicLocator.getSmsAccountLogic().recalculateAccountBalance(
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
		List<SmsAccount> accounts = hibernateLogicLocator.getSmsAccountLogic()
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

		SmsTask smsTask = hibernateLogicLocator.getSmsTaskLogic().getSmsTask(
				smsTaskId);
		SmsAccount smsAccount = hibernateLogicLocator.getSmsAccountLogic()
				.getSmsAccount(smsTask.getSmsAccountId());
		SmsTransaction origionalTransaction = hibernateLogicLocator
				.getSmsTransactionLogic().getCancelSmsTransactionForTask(
						smsTaskId);

		SmsTransaction smsTransaction = new SmsTransaction();

		if (origionalTransaction == null) {
			return false;
		}

		// The juicy bits
		double transactionCredits = origionalTransaction.getTransactionCredits()
				* -1;// Reverse the sign cause we are deducting from the account
		smsTransaction.setTransactionCredits(transactionCredits);
		smsTransaction.setCreditBalance(transactionCredits);

		smsTransaction.setSakaiUserId(smsTask.getSenderUserId());
		smsTransaction.setSmsAccount(smsAccount);
		smsTransaction.setSmsTaskId(smsTask.getId());

		hibernateLogicLocator.getSmsTransactionLogic()
				.insertCancelPendingRequestTransaction(smsTransaction);

		return true;
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
	public boolean settleCreditDifference(SmsTask smsTask, double creditEstimate, double actualCreditsUsed) {
		// we might want to use a separate account to pay when the overdraft is
		// exceeded.
		SmsAccount account = hibernateLogicLocator.getSmsAccountLogic()
				.getSmsAccount(smsTask.getSmsAccountId());
		if (account == null) {
			// Account does not exist
			return false;
		}

		SmsTransaction smsTransaction = new SmsTransaction();

		// The juicy bits
		double transactionCredits = creditEstimate - actualCreditsUsed;
		smsTransaction.setCreditBalance(transactionCredits);
		smsTransaction.setTransactionCredits(transactionCredits);

		smsTransaction.setSakaiUserId(smsTask.getSenderUserId());
		smsTransaction.setSmsAccount(account);
		smsTransaction.setSmsTaskId(smsTask.getId());

		hibernateLogicLocator.getSmsTransactionLogic().insertSettleTransaction(
				smsTransaction);

		return true;
	}

	public String getCancelCode() {
		return StringUtils.left(PROPERTIES.getProperty("TRANS_CANCEL", "TCAN"),
				5);
	}

	public String getCancelReserveCode() {
		return StringUtils.left(PROPERTIES.getProperty("TRANS_CANCEL_RESERVE",
				"RCAN"), 5);
	}

	public String getCreditAccountCode() {
		return StringUtils.left(PROPERTIES.getProperty("TRANS_CREDIT_ACCOUNT",
				"CRED"), 5);
	}

	public String getDebitLateMessageCode() {
		return StringUtils.left(PROPERTIES.getProperty(
				"TRANS_DEBIT_LATE_MESSAGE", "LATE"), 5);
	}

	public String getReserveCreditsCode() {
		return StringUtils.left(PROPERTIES.getProperty("TRANS_RESERVE_CREDITS",
				"RES"), 5);
	}

	public String getSettleDifferenceCode() {
		return StringUtils.left(PROPERTIES.getProperty(
				"TRANS_SETTLE_DIFFERENCE", "RSET"), 5);
	}

	public String getIncomingMessageCode() {
		return StringUtils.left(PROPERTIES.getProperty(
				"TRANS_INCOMING", "IN"), 5);
	}

	public void transferAccountCredits(Long fromAccountId,
			Long toAccountId, double credits) throws SmsInsufficientCreditsException {
		
		//consider checking if accounts are enabled?
		//cannot transfer from an account in overdraft
		if( checkSufficientCredits(fromAccountId, credits) ){
			creditAccount(fromAccountId, -credits, "Transfer credits to account " + toAccountId);
			creditAccount(toAccountId, credits, "Transfer credits from account " + fromAccountId);
		}else{
			//not enough credits in from account
			throw new SmsInsufficientCreditsException("Cannot transfer " + credits + "credits from account: " + fromAccountId);
		}
		
	}

}
