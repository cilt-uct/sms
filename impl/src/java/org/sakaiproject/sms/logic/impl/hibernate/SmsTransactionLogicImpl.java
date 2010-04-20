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
package org.sakaiproject.sms.logic.impl.hibernate;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Hibernate;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;
import org.sakaiproject.sms.bean.SearchFilterBean;
import org.sakaiproject.sms.bean.SearchResultContainer;
import org.sakaiproject.sms.logic.HibernateLogicLocator;
import org.sakaiproject.sms.logic.QueryParameter;
import org.sakaiproject.sms.logic.SmsLogic;
import org.sakaiproject.sms.logic.SmsTransactionLogic;
import org.sakaiproject.sms.logic.exception.SmsSearchException;
import org.sakaiproject.sms.logic.smpp.SmsBilling;
import org.sakaiproject.sms.model.SmsAccount;
import org.sakaiproject.sms.model.SmsConfig;
import org.sakaiproject.sms.model.SmsTransaction;
import org.sakaiproject.sms.model.constants.SmsConstants;
import org.sakaiproject.sms.util.DateUtil;

/**
 * The data service will handle all sms Transaction database transactions for
 * the sms tool in Sakai.
 * 
 * @author julian@psybergate.com
 * @version 1.0
 * @created 25-Nov-2008 08:12:41 AM
 */
@SuppressWarnings("unchecked")
public class SmsTransactionLogicImpl extends SmsLogic implements
		SmsTransactionLogic {

	private HibernateLogicLocator hibernateLogicLocator = null;

	public HibernateLogicLocator getHibernateLogicLocator() {
		return hibernateLogicLocator;
	}

	public void setHibernateLogicLocator(
			HibernateLogicLocator hibernateLogicLocator) {
		this.hibernateLogicLocator = hibernateLogicLocator;
	}

	private SmsBilling smsBilling = null;

	public void setSmsBilling(SmsBilling smsBilling) {
		this.smsBilling = smsBilling;
	}

	/**
	 * Deletes and the given entity from the DB
	 */
	public void deleteSmsTransaction(SmsTransaction smsTransaction) {
		delete(smsTransaction);
	}

	/**
	 * Gets a SmsTransaction entity for the given id
	 * 
	 * @param Long
	 *            sms transaction id
	 * @return sms congiguration
	 */
	public SmsTransaction getSmsTransaction(Long smsTransactionId) {
		return (SmsTransaction) findById(SmsTransaction.class, smsTransactionId);
	}

	/**
	 * Gets all the sms transaction records
	 * 
	 * @return List of SmsTransaction objects
	 */
	public List<SmsTransaction> getAllSmsTransactions() {
		return smsDao.runQuery("from SmsTransaction");
	}

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
			SearchFilterBean searchBean) throws SmsSearchException {
		return getSmsTransactionsForCriteria(searchBean);
	}

	/**
	 * Gets a search results container housing the result set for a particular
	 * displayed page
	 * 
	 * @param searchBean
	 * @return Search result container
	 * @throws SmsSearchException
	 */
	public SearchResultContainer<SmsTransaction> getPagedSmsTransactionsForCriteria(
			SearchFilterBean searchBean) throws SmsSearchException {

		final List<SmsTransaction> transactions = getSmsTransactionsForCriteria(searchBean);

		final SearchResultContainer<SmsTransaction> con = new SearchResultContainer<SmsTransaction>(
				getPageSize());
		con.setTotalResultSetSize(Long.valueOf(transactions.size()));
		con.calculateAndSetPageResults(transactions, searchBean
				.getCurrentPage());

		return con;
	}

	private List<SmsTransaction> getSmsTransactionsForCriteria(
			SearchFilterBean searchBean) throws SmsSearchException {
		/*
		 * final Criteria crit = smsDao.createCriteria(SmsTransaction.class)
		 * .createAlias("smsAccount", "smsAccount");
		 */
		Search search = new Search();
		try {
			// Transaction type
			if (searchBean.getTransactionType() != null
					&& !searchBean.getTransactionType().trim().equals("")) {

				search.addRestriction(new Restriction("transactionTypeCode",
						searchBean.getTransactionType()));
			}

			// Account number
			if (searchBean.getNumber() != null) {
				search.addRestriction(new Restriction("smsAccount.id", Long
						.valueOf(searchBean.getNumber())));
			}

			// Transaction date start
			if (searchBean.getDateFrom() != null) {
				final Date date = DateUtil
						.getDateFromStartDateString(searchBean.getDateFrom());
				search.addRestriction(new Restriction("transactionDate", date,
						Restriction.GREATER));
			}

			// Transaction date end
			if (searchBean.getDateTo() != null) {
				final Date date = DateUtil.getDateFromEndDateString(searchBean
						.getDateTo());
				search.addRestriction(new Restriction("transactionDate", date,
						Restriction.LESS));
			}

			// Sender name
			if (searchBean.getSender() != null
					&& !searchBean.getSender().trim().equals("")) {
				search.addRestriction(new Restriction("sakaiUserId", searchBean
						.getSender()));
			}

			// Ordering
			if (searchBean.getOrderBy() != null
					&& !searchBean.getOrderBy().trim().equals("")) {

				search
						.addOrder(new org.sakaiproject.genericdao.api.search.Order(
								searchBean.getOrderBy(), searchBean.sortAsc()));
			}
			if (searchBean.getTaskId() != null
					&& !"".equals(searchBean.getTaskId().trim())) {
				search.addRestriction(new Restriction("smsTaskId", Long
						.valueOf(searchBean.getTaskId())));
			}

		} catch (ParseException e) {
			throw new SmsSearchException(e);
		} catch (Exception e) {
			throw new SmsSearchException(e);
		}

		return smsDao.findBySearch(SmsTransaction.class, search);
	}

	private int getPageSize() {
		SmsConfig smsConfig = hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSmsConfigBySakaiSiteId(
						hibernateLogicLocator.getExternalLogic()
								.getCurrentSiteId());
		if (smsConfig == null) {
			return SmsConstants.DEFAULT_PAGE_SIZE;
		} else {
			return smsConfig.getPagingSize();
		}
	}

	/**
	 * Gets all the related transaction for the specified account id.
	 * 
	 * @param accountId
	 *            the account id
	 * 
	 * @return the sms transactions for account id
	 */
	public List<SmsTransaction> getSmsTransactionsForAccountId(Long accountId) {
		String hql = "from SmsTransaction transaction where transaction.smsAccount.id = :accountId";
		List<SmsTransaction> transactions = smsDao.runQuery(hql,
				new QueryParameter("accountId", accountId, Hibernate.LONG));
		return transactions;
	}

	/**
	 * Gets all the related transaction for the specified task id.
	 * 
	 * @param accountId
	 *            the account id
	 * 
	 * @return the sms transactions for account id
	 */
	public List<SmsTransaction> getSmsTransactionsForTaskId(Long taskId) {
		String hql = "from SmsTransaction transaction where transaction.smsTaskId = :taskId";
		List<SmsTransaction> transactions = smsDao.runQuery(hql,
				new QueryParameter("smsTaskId", taskId, Hibernate.LONG));
		return transactions;
	}

	/**
	 * Gets transaction that will be used to create to populate a new
	 * transaction to cancel this one.
	 * 
	 * @param taskId
	 *            the task id
	 * 
	 * @return the cancel sms transaction for task
	 */
	public SmsTransaction getCancelSmsTransactionForTask(Long taskId) {
		StringBuilder hql = new StringBuilder();
		hql
				.append(" from SmsTransaction transaction where transaction.smsTaskId = :taskId ");
		hql
				.append(" and transaction.transactionTypeCode = :transactionTypeCode ");
		hql.append(" order by transaction.transactionDate desc ");

		List<SmsTransaction> transactions = smsDao.runQuery(hql.toString(),
				new QueryParameter("taskId", taskId, Hibernate.LONG),
				new QueryParameter("transactionTypeCode", smsBilling
						.getReserveCreditsCode(), Hibernate.STRING));

		if (transactions != null && !transactions.isEmpty()) {
			return transactions.get(0);
		}
		return null;
	}

	/**
	 * Insert reserve transaction.
	 * <p>
	 * This will also update the related account balance.
	 * 
	 * @param smsTransaction
	 *            the sms transaction
	 */
	public void insertReserveTransaction(SmsTransaction smsTransaction) {
		insertTransaction(smsTransaction, smsBilling.getReserveCreditsCode());
	}

	/**
	 * Insert settle transaction.
	 * <p>
	 * This will also update the related account balance.
	 * 
	 * @param smsTransaction
	 *            the sms transaction
	 */
	public void insertSettleTransaction(SmsTransaction smsTransaction) {
		insertTransaction(smsTransaction, smsBilling.getSettleDifferenceCode());
	}

	/**
	 * Insert cancel pending request transaction.
	 * <p>
	 * This will also update the related account balance.
	 * 
	 * @param smsTransaction
	 *            the sms transaction
	 */
	public void insertCancelPendingRequestTransaction(
			SmsTransaction smsTransaction) {
		insertTransaction(smsTransaction, smsBilling.getCancelReserveCode());
	}

	/**
	 * Insert transaction for a late message.
	 * <p>
	 * This will also update the related account balance.
	 * 
	 * @param smsTransaction
	 *            the sms transaction
	 */
	public void insertLateMessageTransaction(SmsTransaction smsTransaction) {
		insertTransaction(smsTransaction, smsBilling.getDebitLateMessageCode());
	}

	public void insertIncomingMessageTransaction(SmsTransaction smsTransaction) {
		insertTransaction(smsTransaction, smsBilling.getIncomingMessageCode());
	}
	
	/**
	 * Insert transaction to credit an account
	 * <p>
	 * This will also update the related account balance.
	 * 
	 * @param smsTransaction
	 *            the sms transaction
	 */
	public void insertCreditAccountTransaction(SmsTransaction smsTransaction) {
		insertTransaction(smsTransaction, smsBilling.getCreditAccountCode());
	}

	/**
	 * Insert transaction.
	 * 
	 * @param smsTransaction
	 *            the sms transaction
	 * @param transactionType
	 *            the transaction type
	 */
	private void insertTransaction(SmsTransaction smsTransaction,
			String transactionType) {

		SmsAccount account = hibernateLogicLocator.getSmsAccountLogic()
				.getSmsAccount(smsTransaction.getSmsAccount().getId());
		smsTransaction.setTransactionTypeCode(transactionType);
		smsTransaction.setTransactionDate(new Date(System.currentTimeMillis()));
		smsTransaction.setCreditBalance(account.getCredits()
				+ smsTransaction.getTransactionCredits());
		String hql = "update SmsAccount set CREDITS = (CREDITS+?)  where ACCOUNT_ID = ?";
		ArrayList<Object> parms = new ArrayList<Object>();
		parms.add((smsTransaction.getTransactionCredits()));
		parms.add(account.getId());
		smsDao.executeUpdate(hql, parms);
		persist(smsTransaction);
	}

}
