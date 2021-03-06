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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.sms.logic.impl.hibernate;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.hibernate.type.DateType;
import org.hibernate.type.StringType;
import org.quartz.impl.jdbcjobstore.InvalidConfigurationException;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;
import org.sakaiproject.sms.logic.HibernateLogicLocator;
import org.sakaiproject.sms.logic.QueryParameter;
import org.sakaiproject.sms.logic.SmsAccountLogic;
import org.sakaiproject.sms.logic.SmsLogic;
import org.sakaiproject.sms.logic.exception.DuplicateUniqueFieldException;
import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.smpp.SmsBilling;
import org.sakaiproject.sms.model.SmsAccount;
import org.sakaiproject.sms.model.SmsConfig;
import org.sakaiproject.sms.model.SmsTransaction;
import org.sakaiproject.sms.model.constants.SmsConstants;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * The data service will handle all sms Account database transactions for the
 * sms tool in Sakai.
 * 
 * @author julian@psybergate.com
 * @version 1.0
 * @created 25-Nov-2008 08:12:41 AM
 */
@SuppressWarnings("unchecked")
@Slf4j
public class SmsAccountLogicImpl extends SmsLogic implements SmsAccountLogic {

	@Setter @Getter private HibernateLogicLocator hibernateLogicLocator = null;
	@Setter private ExternalLogic externalLogic = null;
	@Setter private SmsBilling smsBilling = null;

	/**
	 * Deletes and the given entity from the DB
	 */
	public void deleteSmsAccount(SmsAccount smsAccount) {
		delete(smsAccount);
	
		if (externalLogic != null) {
			externalLogic.postEvent(ExternalLogic.SMS_EVENT_ACCOUNT_DELETE, "/sms-account/" + smsAccount.getId(), null);
		}
	}		

	/**
	 * Gets a SmsAccount entity for the given id
	 * 
	 * @param Long
	 *            sms account id
	 * @return sms configuration
	 */
	public SmsAccount getSmsAccount(Long smsAccountId) {
		return (SmsAccount) findById(SmsAccount.class, smsAccountId);
	}

	/**
	 * Gets all the sms account records
	 * 
	 * @return List of SmsAccount objects
	 */
	public List<SmsAccount> getAllSmsAccounts() {
		return smsDao.runQuery("from SmsAccount");
	}

	
	public List<SmsAccount> getSmsAccountsForOwner(String sakaiUserId) {
		List<SmsAccount> ret = null;
		Search search = new Search();
		search.addRestriction(new Restriction("ownerId", sakaiUserId));
		ret = smsDao.findBySearch(SmsAccount.class, search);
		return ret;
	}
	
	/**
	 * This method will persists the given object.
	 * 
	 * If the object is a new entity then it will be created on the DB. If it is
	 * an existing entity then the record will be updates on the DB.
	 * 
	 * @param sms
	 *            account to be persisted
	 * @throws InvalidConfigurationException 
	 */
	public void persistSmsAccount(SmsAccount smsAccount) {
		if (!hasUniqueSakaiSiteId(smsAccount)) {
			throw new DuplicateUniqueFieldException("sakaiSiteId");
		}
		if (!hasUniqueSakaiUserId(smsAccount)) {
			throw new DuplicateUniqueFieldException("sakaiUserId");
		}
		
		persist(smsAccount);
		
		// TODO - Distinguish between creation and update events
		
		if (externalLogic != null) {
			externalLogic.postEvent(ExternalLogic.SMS_EVENT_ACCOUNT_REVISE, "/sms-account/" + smsAccount.getId(), null);
		}
	}

	/**
	 * Checks if account has unique Sakai site id
	 * 
	 * @param smsAccount
	 * @return
	 */
	private boolean hasUniqueSakaiSiteId(SmsAccount smsAccount) {
		final SmsAccount accBySite = getAccountBySakaiSiteId(smsAccount
				.getSakaiSiteId());

		if (accBySite != null && !accBySite.getId().equals(smsAccount.getId())) {
			return false;
		}

		return true;
	}

	/**
	 * Checks if account has unique Sakai user id
	 * 
	 * @param smsAccount
	 * @return
	 */
	private boolean hasUniqueSakaiUserId(SmsAccount smsAccount) {
		final SmsAccount accByUser = getAccountBySakaiUserId(smsAccount
				.getSakaiUserId());

		if (accByUser != null && !accByUser.getId().equals(smsAccount.getId())) {
			return false;
		}

		return true;
	}

	/**
	 * This is a test method to insert a sms account.It is only used during
	 * development.
	 * 
	 * @param sakaiSiteID
	 * @param sakaiUserID
	 * @return
	 */
	private SmsAccount insertTestSmsAccount(String sakaiSiteID,
			String sakaiUserID) {

		final SmsAccount smsAccount = new SmsAccount();
		smsAccount.setSakaiUserId(sakaiUserID);
		smsAccount.setSakaiSiteId(sakaiSiteID);
		smsAccount.setMessageTypeCode("3");
		smsAccount.setOverdraftLimit(0L);
		smsAccount.setCredits(100L);
		smsAccount.setAccountName("TestAccountName");
		smsAccount.setAccountEnabled(true);
		try {
			hibernateLogicLocator.getSmsAccountLogic().persistSmsAccount(
					smsAccount);
		} catch (Exception e) {
		}
		return smsAccount;
	}

	/**
	 * Gets a SmsAccount entity for the given sakai site id or sakai user id.
	 * <p>
	 * If the property account.checkSiteIdBeforeUserId == true in sms.properties
	 * then the method will first attempt to find the sms account by sakai site
	 * id. If the returned account is null then it will attempt to finf it by
	 * the sakai user id. If no account is found null will be returned.
	 * <p>
	 * If the property account.checkSiteIdBeforeUserId == false then it will
	 * behave as described above but will first try find the account by sakai
	 * user id before the sakai site id.
	 * 
	 * @param sakaiSiteId
	 *            the sakai site id. Can be null.
	 * @param sakaiUserId
	 *            the sakai user id. Can be null.
	 * 
	 * @return sms configuration
	 * 
	 */
	public SmsAccount getSmsAccount(String sakaiSiteId,
			String sakaiUserId) {
		final SmsConfig config = hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSystemSmsConfig();
		boolean useSiteAccount = config.getUseSiteAcc();
		SmsAccount account = null;
		
		if (SmsConstants.SMS_DEV_MODE) {
			if (useSiteAccount) {
				insertTestSmsAccount(sakaiSiteId, null);
			} else {
				insertTestSmsAccount(null, sakaiUserId);
			}
		}
		
		// If only 1 has been specified, use that to lookup

		if (sakaiSiteId != null && sakaiUserId == null) {
			account = getAccountBySakaiSiteId(sakaiSiteId);
		}

		if (sakaiUserId != null && sakaiSiteId == null) {
			account = getAccountBySakaiUserId(sakaiUserId);
		}

		// Resolve according to system preference
		
		if (sakaiUserId != null && sakaiSiteId != null) {
			if (useSiteAccount) {
				account = getAccountBySakaiSiteId(sakaiSiteId);
				if (account == null) {
					account = getAccountBySakaiUserId(sakaiUserId);					
				}
			} else {
				account = getAccountBySakaiUserId(sakaiUserId);
				if (account == null) {
					account = getAccountBySakaiSiteId(sakaiSiteId);					
				}
			}
		}

		if (account == null && SmsConstants.SAKAI_SMS_ADMIN_SITE.equals(sakaiSiteId)) {
			SmsAccount smsAccount = new SmsAccount();
			smsAccount.setAccountEnabled(true);
			smsAccount.setAccountName(SmsConstants.DEFAULT_MO_ACCOUNT_NAME);
			smsAccount.setCredits(0l);
			smsAccount.setOverdraftLimit(0l);
			smsAccount.setMessageTypeCode(SmsConstants.MESSAGE_TYPE_CODE_SO);
			smsAccount.setStartdate(new Date());
			smsAccount.setSakaiSiteId(SmsConstants.SAKAI_SMS_ADMIN_SITE);
			try {
				persistSmsAccount(smsAccount);
			} catch (IllegalArgumentException e) {
				log.warn(e.getLocalizedMessage(), e);
			}

			return getSmsAccount(sakaiSiteId, sakaiUserId);
		}

		// may return null if acc does not exist
		return account;
	}

	/**
	 * Gets the account by sakai site id.
	 * 
	 * @param sakaiSiteId
	 *            the sakai site id
	 * 
	 * @return the account by sakai site id
	 * 
	 * @throws MoreThanOneAccountFoundException
	 *             the more than one account found exception
	 */
	private SmsAccount getAccountBySakaiSiteId(String sakaiSiteId) {
		if (sakaiSiteId == null || sakaiSiteId.trim().equals("")) {
			return null;
		}
		SmsAccount account = null;
		StringBuilder hql = new StringBuilder();
		hql
				.append(" from SmsAccount account where (account.enddate is null or account.enddate > :today) ");
		hql.append(" and (account.sakaiSiteId = :sakaiSiteId)");
		List<SmsAccount> accounts = smsDao
				.runQuery(hql.toString(), new QueryParameter("sakaiSiteId",
						sakaiSiteId, StringType.INSTANCE), new QueryParameter(
						"today", new Date(), DateType.INSTANCE));

		if (accounts != null && !accounts.isEmpty()) {
			account = accounts.get(0);
		}
		return account;
	}

	/**
	 * Gets the account by sakai site id.
	 * 
	 * @param sakaiUserId
	 *            the sakai user id
	 * 
	 * @return the account by sakai site id
	 * 
	 */
	private SmsAccount getAccountBySakaiUserId(String sakaiUserId) {
		if (sakaiUserId == null || sakaiUserId.trim().equals("")) {
			return null;
		}
		SmsAccount account = null;
		StringBuilder hql = new StringBuilder();
		hql
				.append(" from SmsAccount account where (account.enddate is null or account.enddate > :today) ");
		hql.append(" and (account.sakaiUserId = :sakaiUserId)");
		List<SmsAccount> accounts = smsDao
				.runQuery(hql.toString(), new QueryParameter("today",
						new Date(), DateType.INSTANCE), new QueryParameter(
						"sakaiUserId", sakaiUserId, StringType.INSTANCE));
		if (accounts != null && !accounts.isEmpty()) {
			account = accounts.get(0);
		}
		return account;
	}

	/**
	 * Recalculate balance for a specific account.
	 * 
	 * @param accountId
	 *            the account id
	 * @param account
	 *            the account
	 */
	public void recalculateAccountBalance(Long accountId,
			SmsAccount account) {
		// Use account instead of id?
		if (account == null) {
			account = hibernateLogicLocator.getSmsAccountLogic().getSmsAccount(
					accountId);
		}

		List<SmsTransaction> transactions = hibernateLogicLocator
				.getSmsTransactionLogic().getSmsTransactionsForAccountId(
						account.getId());

		// Sort by transaction date
		Collections.sort(transactions, new Comparator<SmsTransaction>() {

			public int compare(SmsTransaction arg0, SmsTransaction arg1) {
				// TODO Auto-generated method stub
				return arg0.getTransactionDate().compareTo(
						arg1.getTransactionDate());
			}

		});

		// Calculate balance
		double credits = 0;
		for (SmsTransaction transaction : transactions) {
			credits += transaction.getTransactionCredits();
		}
		account.setCredits(credits);
		
		try {
			persistSmsAccount(account);
		} catch (IllegalArgumentException e) {
			log.warn(e.getLocalizedMessage(), e);
		}
	}

	public double getAccountBalance(double credits) {
		return smsBilling.convertCreditsToAmount(credits);
	}
}
