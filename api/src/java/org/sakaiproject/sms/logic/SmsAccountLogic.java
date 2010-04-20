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
package org.sakaiproject.sms.logic;

import java.util.List;


import org.sakaiproject.sms.model.SmsAccount;

/**
 * The data service will handle all sms Account database transactions for the
 * sms tool in Sakai.
 * 
 * @author julian@psybergate.com
 * @version 1.0
 * @created 25-Nov-2008 08:12:41 AM
 */
public interface SmsAccountLogic {

	/**
	 * Deletes and the given entity from the DB
	 */
	public void deleteSmsAccount(SmsAccount smsAccount);

	/**
	 * Gets a SmsAccount entity for the given id
	 * 
	 * @param Long
	 *            sms account id
	 * @return sms congiguration
	 */
	public SmsAccount getSmsAccount(Long smsAccountId);

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
	 * @return sms congiguration
	 */
	public SmsAccount getSmsAccount(String sakaiSiteId, String sakaiUserId);

	/**
	 * Gets all the sms account records
	 * 
	 * @return List of SmsAccount objects
	 */
	public List<SmsAccount> getAllSmsAccounts();

	/**
	 * Get all the accounts the bellong to that owner
	 * @param sakaiUserId
	 * @return a list of sms accounts
	 */
	public List<SmsAccount> getSmsAccountsForOwner(String sakaiUserId);
	
	
	/**
	 * This method will persists the given object.
	 * 
	 * If the object is a new entity then it will be created on the DB. If it is
	 * an existing entity then the record will be updated.
	 * 
	 * @param sms
	 *            account to be persisted
	 */
	public void persistSmsAccount(SmsAccount smsAccount);

	/**
	 * Recalculate balance for a specific account.
	 * 
	 * @param accountId
	 *            the account id
	 * @param account
	 *            the account
	 */
	public void recalculateAccountBalance(Long accountId, SmsAccount account);

	/**
	 * Get currency value of a credit amount
	 * @param credits
	 * @return Credit value in currency value
	 */
	public double getAccountBalance(double credits);

}
