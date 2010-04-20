/***********************************************************************************
 * SmsTransactionLogic.java
 * Copyright (c) 2008, 2009 Sakai Project/Sakai Foundation
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

package org.sakaiproject.sms.logic;

import java.util.List;

import org.sakaiproject.sms.bean.SearchFilterBean;
import org.sakaiproject.sms.bean.SearchResultContainer;
import org.sakaiproject.sms.logic.exception.SmsSearchException;
import org.sakaiproject.sms.model.SmsTransaction;

/**
 * The data service will handle all sms Account database transactions for the
 * sms tool in Sakai.
 * 
 * @author julian@psybergate.com
 * @version 1.0
 * @created 25-Nov-2008 08:12:41 AM
 */
public interface SmsTransactionLogic {

	// /**
	// * Persist a transaction to reserve credits for a sms sending
	// *
	// * @param smsTaskId
	// * @param smsAccountId
	// * @param credits
	// * @throws SmsAccountNotFoundException
	// */
	// public void reserveCredits(Long smsTaskId, Long smsAccountId,
	// Integer credits) throws SmsAccountNotFoundException;

	/**
	 * Deletes and the given entity from the DB
	 */
	public void deleteSmsTransaction(SmsTransaction smsTransaction);

	/**
	 * Gets a SmsTransaction entity for the given id
	 * 
	 * @param Long
	 *            sms transaction id
	 * @return sms congiguration
	 */
	public SmsTransaction getSmsTransaction(Long smsTransactionId);

	/**
	 * Gets all the sms transaction records
	 * 
	 * @return List of SmsTransaction objects
	 */
	public List<SmsTransaction> getAllSmsTransactions();

	/**
	 * Gets a search results container housing the result set for a particular
	 * displayed page
	 * 
	 * @param searchBean
	 * @return Search result container
	 * @throws SmsSearchException
	 */
	public SearchResultContainer<SmsTransaction> getPagedSmsTransactionsForCriteria(
			SearchFilterBean searchBean) throws SmsSearchException;

	/**
	 * Gets a list of all SmsTransaction objects for the specified search
	 * criteria
	 * 
	 * @param search
	 *            Bean containing the search criteria
	 * @return List of SmsTransactions
	 * @throws SmsSearchException
	 *             when an invalid search criteria is specified
	 */
	public List<SmsTransaction> getAllSmsTransactionsForCriteria(
			SearchFilterBean searchBean) throws SmsSearchException;

	/**
	 * Insert reserve transaction.
	 * <p>
	 * This will also update the related account balance.
	 * 
	 * @param smsTransaction
	 *            the sms transaction
	 */
	public void insertReserveTransaction(SmsTransaction smsTransaction);

	/**
	 * Insert settle transaction.
	 * <p>
	 * This will also update the related account balance.
	 * 
	 * @param smsTransaction
	 *            the sms transaction
	 */
	public void insertSettleTransaction(SmsTransaction smsTransaction);

	/**
	 * Insert cancel pending request transaction.
	 * <p>
	 * This will also update the related account balance.
	 * 
	 * @param smsTransaction
	 *            the sms transaction
	 */
	public void insertCancelPendingRequestTransaction(
			SmsTransaction smsTransaction);

	/**
	 * Insert transaction for a late message.
	 * <p>
	 * This will also update the related account balance.
	 * 
	 * @param smsTransaction
	 *            the sms transaction
	 */
	public void insertLateMessageTransaction(SmsTransaction smsTransaction);

	/**
	 * Insert transaction for a late message.
	 * <p>
	 * This will also update the related account balance.
	 * 
	 * @param smsTransaction
	 *            the sms transaction
	 */
	public void insertIncomingMessageTransaction(SmsTransaction smsTransaction);


	/**
	 * Insert transaction to credit an account
	 * <p>
	 * This will also update the related account balance.
	 * 
	 * @param smsTransaction
	 *            the sms transaction
	 */
	public void insertCreditAccountTransaction(SmsTransaction smsTransaction);

	/**
	 * Gets all the related transaction for the specified account id.
	 * 
	 * @param accountId
	 *            the account id
	 * 
	 * @return the sms transactions for account id
	 */
	public List<SmsTransaction> getSmsTransactionsForAccountId(Long accountId);

	/**
	 * Gets all the related transaction for the specified task id.
	 * 
	 * @param taskId
	 *            the task id
	 * 
	 * @return the sms transactions for account id
	 */
	public List<SmsTransaction> getSmsTransactionsForTaskId(Long taskId);

	/**
	 * Gets transaction that will be used to create to populate a new
	 * transaction to cancel this one.
	 * 
	 * @param taskId
	 *            the task id
	 * 
	 * @return the cancel sms transaction for task
	 */
	public SmsTransaction getCancelSmsTransactionForTask(Long taskId);
}
