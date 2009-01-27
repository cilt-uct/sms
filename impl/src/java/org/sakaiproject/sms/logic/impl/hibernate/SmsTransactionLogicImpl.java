/***********************************************************************************
 * SmsTransactionLogicImpl.java
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

package org.sakaiproject.sms.logic.impl.hibernate;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.sms.bean.SearchFilterBean;
import org.sakaiproject.sms.bean.SearchResultContainer;
import org.sakaiproject.sms.dao.SmsDao;
import org.sakaiproject.sms.logic.hibernate.SmsTransactionLogic;
import org.sakaiproject.sms.logic.impl.hibernate.exception.SmsAccountNotFoundException;
import org.sakaiproject.sms.logic.impl.hibernate.exception.SmsSearchException;
import org.sakaiproject.sms.model.hibernate.SmsAccount;
import org.sakaiproject.sms.model.hibernate.SmsConfig;
import org.sakaiproject.sms.model.hibernate.SmsTransaction;
import org.sakaiproject.sms.model.hibernate.constants.SmsConst_Billing;
import org.sakaiproject.sms.model.hibernate.constants.SmsHibernateConstants;
import org.sakaiproject.sms.model.hibernate.factory.SmsTransactionFactory;
import org.sakaiproject.sms.util.HibernateUtil;

/**
 * The data service will handle all sms Transaction database transactions for
 * the sms tool in Sakai.
 * 
 * @author julian@psybergate.com
 * @version 1.0
 * @created 25-Nov-2008 08:12:41 AM
 */
public class SmsTransactionLogicImpl extends SmsDao implements
		SmsTransactionLogic {

	/**
	 * Persist a transaction to when a task fails
	 * 
	 * @param smsTask
	 * @throws SmsAccountNotFoundException
	 */
	public void cancelTransaction(Long smsTaskId, Long smsAccountId)
			throws SmsAccountNotFoundException {
		SmsTransaction smsTransaction = SmsTransactionFactory.createCancelTask(
				smsTaskId, smsAccountId);
		persist(smsTransaction);
	}

	// /**
	// * Persist a transaction to reserve credits for a sms sending
	// *
	// * @param smsTaskId
	// * @param smsAccountId
	// * @param credits
	// * @throws SmsAccountNotFoundException
	// */
	// public void reserveCredits(Long smsTaskId, Long smsAccountId,
	// Integer credits) throws SmsAccountNotFoundException {
	// SmsTransaction smsTransaction = SmsTransactionFactory
	// .createReserveCreditsTask(smsTaskId, smsAccountId, credits);
	// persist(smsTransaction);
	// }

	/**
	 * Leave this as protected to try and prevent the random instantiation of
	 * this class.
	 * <p>
	 * Use LogicFactory.java to get instances of logic classes.
	 */
	protected SmsTransactionLogicImpl() {

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
		Session s = HibernateUtil.getSession();
		Query query = s.createQuery("from SmsTransaction");
		return query.list();
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

		List<SmsTransaction> transactions = getSmsTransactionsForCriteria(searchBean);

		SearchResultContainer<SmsTransaction> con = new SearchResultContainer<SmsTransaction>(
				getPageSize());
		con.setTotalResultSetSize(new Long(transactions.size()));
		con.calculateAndSetPageResults(transactions, searchBean
				.getCurrentPage());

		return con;
	}

	private List<SmsTransaction> getSmsTransactionsForCriteria(
			SearchFilterBean searchBean) throws SmsSearchException {
		Criteria crit = HibernateUtil.getSession().createCriteria(
				SmsTransaction.class).createAlias("smsAccount", "smsAccount");

		List<SmsTransaction> transactions = new ArrayList<SmsTransaction>();

		try {
			// Transaction type
			if (searchBean.getTransactionType() != null
					&& !searchBean.getTransactionType().trim().equals("")) {
				crit.add(Restrictions.ilike("transactionTypeCode", searchBean
						.getTransactionType()));
			}

			// Account number
			if (searchBean.getNumber() != null) {
				crit.add(Restrictions.like("smsAccount.id", new Long(searchBean
						.getNumber())));
			}

			// Transaction date start
			if (searchBean.getDateFrom() != null) {
				Date date = DateUtil.getDateFromStartDateString(searchBean
						.getDateFrom());
				crit.add(Restrictions.ge("transactionDate", date));
			}

			// Transaction date end
			if (searchBean.getDateTo() != null) {
				Date date = DateUtil.getDateFromEndDateString(searchBean
						.getDateTo());
				crit.add(Restrictions.le("transactionDate", date));
			}

			// Sender name
			if (searchBean.getSender() != null
					&& !searchBean.getSender().trim().equals("")) {
				crit.add(Restrictions.ilike("sakaiUserId", searchBean
						.getSender(), MatchMode.ANYWHERE));
			}

			// Ordering
			if (searchBean.getOrderBy() != null
					&& !searchBean.getOrderBy().trim().equals("")) {
				crit.addOrder((searchBean.sortAsc() ? Order.asc(searchBean
						.getOrderBy()) : Order.desc(searchBean.getOrderBy())));
			}

			crit.setMaxResults(SmsHibernateConstants.READ_LIMIT);

		} catch (ParseException e) {
			throw new SmsSearchException(e);
		} catch (Exception e) {
			throw new SmsSearchException(e);
		}
		transactions = crit.list();
		HibernateUtil.closeSession();
		return transactions;
	}

	private int getPageSize() {
		SmsConfig smsConfig = HibernateLogicFactory.getConfigLogic()
				.getOrCreateSmsConfigBySakaiSiteId(
						SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID);
		if (smsConfig == null)
			return SmsHibernateConstants.DEFAULT_PAGE_SIZE;
		else
			return smsConfig.getPagingSize();
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
		Session s = HibernateUtil.getSession();
		List<SmsTransaction> transactions = new ArrayList<SmsTransaction>();
		Query query = s
				.createQuery("from SmsTransaction transaction where transaction.smsAccount.id = :accountId");
		query.setParameter("accountId", accountId);
		transactions = query.list();
		HibernateUtil.closeSession();
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
		Session s = HibernateUtil.getSession();
		List<SmsTransaction> transactions = new ArrayList<SmsTransaction>();
		Query query = s
				.createQuery("from SmsTransaction transaction where transaction.smsTaskId = :taskId");
		query.setParameter("smsTaskId", taskId);
		transactions = query.list();
		HibernateUtil.closeSession();
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
		Session s = HibernateUtil.getSession();

		StringBuilder hql = new StringBuilder();
		hql
				.append(" from SmsTransaction transaction where transaction.smsTaskId = :taskId ");
		hql
				.append(" and transaction.transactionTypeCode = :transactionTypeCode ");
		hql.append(" order by transaction.transactionDate desc ");

		Query query = s.createQuery(hql.toString());
		query.setParameter("taskId", taskId);
		query.setParameter("transactionTypeCode",
				SmsConst_Billing.TRANS_RESERVE_CREDITS);
		List<SmsTransaction> transactions = query.list();
		HibernateUtil.closeSession();

		if (transactions != null && transactions.size() > 0) {
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
		insertTransaction(smsTransaction,
				SmsConst_Billing.TRANS_RESERVE_CREDITS);
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
		insertTransaction(smsTransaction,
				SmsConst_Billing.TRANS_SETTLE_DIFFERENCE);
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
		insertTransaction(smsTransaction, SmsConst_Billing.TRANS_CANCEL_RESERVE);
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
		insertTransaction(smsTransaction,
				SmsConst_Billing.TRANS_CREDIT_LATE_MESSAGE);
	}

	/**
	 * Insert transaction to debit an account
	 * <p>
	 * This will also update the related account balance.
	 * 
	 * @param smsTransaction
	 *            the sms transaction
	 */
	public void insertDebitAccountTransaction(SmsTransaction smsTransaction) {
		insertTransaction(smsTransaction, SmsConst_Billing.TRANS_DEBIT_ACCOUNT);
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
		SmsAccount account = HibernateLogicFactory.getAccountLogic()
				.getSmsAccount(smsTransaction.getSmsAccount().getId());
		smsTransaction.setTransactionTypeCode(transactionType);
		smsTransaction.setTransactionDate(new Date(System.currentTimeMillis()));
		// Update the account balance
		account.setBalance(account.getBalance()
				+ smsTransaction.getTransactionAmount());
		smsTransaction.setBalance(account.getBalance());
		HibernateLogicFactory.getAccountLogic().persistSmsAccount(account);

		persist(smsTransaction);
	}

}
