/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.sms.logic.smpp;

import org.sakaiproject.sms.logic.exception.SmsAccountNotFoundException;
import org.sakaiproject.sms.logic.exception.SmsInsufficientCreditsException;
import org.sakaiproject.sms.model.SmsAccount;
import org.sakaiproject.sms.model.SmsTask;

/**
 * The billing service will handle all financial functions for the sms tool in
 * Sakai.
 * 
 * @author louis@psybergate.com
 * @version 1.0
 * @created 12-Nov-2008
 */
public interface SmsBilling {

	/**
	 * Credit an account by the supplied amount of credits
	 * 
	 * @param accountId
	 * @param creditsToCredit
	 * @param description 
	 */
	public void creditAccount(Long accountId, double creditsToCredit, String Description);

	/**
	 * Return true of the account has the required credits available. Take into
	 * account overdraft limits, if applicable.
	 * 
	 * @param accountID
	 *            the account id
	 * @param creditsRequired
	 *            the credits required
	 * @param overDraftCheck
	 *            the overDraftCheck
	 * 
	 * @return true, if check sufficient credits
	 */
	public boolean checkSufficientCredits(Long accountID,
			double creditsRequired, boolean overDraftCheck);

	/**
	 * 
	 * Return true of the account has the required credits available. Take into
	 * account overdraft limits, if applicable.
	 * 
	 * @param smsTask
	 * @return
	 */
	public boolean checkSufficientCredits(SmsTask smsTask);

	/**
	 * 
	 * Return true of the account has the required credits available. Take into
	 * account overdraft limits, if applicable.
	 * 
	 * @param smsTask
	 * @param overDraftCheck
	 * @return
	 */
	public boolean checkSufficientCredits(SmsTask smsTask,
			boolean overDraftCheck);

	/**
	 * Convert amount to credits.
	 * 
	 * @param amount
	 *            the amount
	 * 
	 * @return the double
	 */
	public double convertAmountToCredits(double amount);

	/**
	 * Convert the given credits to currency base on the defined conversion
	 * value at the given time.
	 * 
	 * @param creditCount
	 *            the credit count
	 * 
	 * @return the credit amount
	 */
	public double convertCreditsToAmount(double creditCount);

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
			throws SmsAccountNotFoundException;

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
	public boolean reserveCredits(SmsTask smsTask);

	/**
	 * Recalculate balance for a specific account.
	 * 
	 * @param accountId
	 *            the account id
	 */
	public void recalculateAccountBalance(Long accountId);

	/**
	 * Cancel pending request.
	 * 
	 * @param smsTaskId
	 *            the sms task id
	 * 
	 * @return true, if successful
	 */
	public boolean cancelPendingRequest(Long smsTaskId);

	/**
	 * Recalculate balances for all existing accounts.
	 * 
	 * @param accountId
	 *            the account id
	 */
	public void recalculateAccountBalances();

	/**
	 * Settle credit difference.
	 * 
	 * @param smsTask
	 *            the sms task
	 * 
	 * @return true, if successful
	 */
	public boolean settleCreditDifference(SmsTask smsTask, double creditEstimate, double actualCreditsUsed);

	/**
	 * Debit account for messages that came in late.
	 * 
	 * @param smsTask The task for which to debit the credits
	 * @param credits The number of credits to debit
	 * @return
	 */
	public boolean debitLateMessages(SmsTask smsTask, double credits);

	/**
	 * Retrieve code for reserving credits (default if none specified)
	 * 
	 * @return code as {@link String}
	 */
	public String getReserveCreditsCode();

	/**
	 * Retrieve code for settling difference (default if none specified)
	 * 
	 * @return code as {@link String}
	 */
	public String getSettleDifferenceCode();

	/**
	 * Retrieve code for cancel reserve (default if none specified)
	 * 
	 * @return code as {@link String}
	 */
	public String getCancelReserveCode();

	/**
	 * Retrieve code for cancel (default if none specified)
	 * 
	 * @return code as {@link String}
	 */
	public String getCancelCode();

	/**
	 * Retrieve code for credit account (default if none specified)
	 * 
	 * @return code as {@link String}
	 */
	public String getCreditAccountCode();

	/**
	 * Retrieve code for debit late message (default if none specified)
	 * 
	 * @return code as {@link String}
	 */
	public String getDebitLateMessageCode();

	/**
	 * Retrieve code for incoming message (default if none specified)
	 * 
	 * @return code as {@link String}
	 */
	public String getIncomingMessageCode();
	
	public boolean debitIncomingMessage(SmsAccount account, double credits, Long replyTaskId);
	

	/**
	 * Move credits from one account to the other.
	 * @param fromAccount Account to debit
	 * @param toAccount Account to credit
	 * @param credits Amount to transfer
	 * @param description 
	 * @throws SmsInsufficientCreditsException 
	 */
	public void transferAccountCredits(Long fromAccount, Long toAccount, double credits) throws SmsInsufficientCreditsException;

}