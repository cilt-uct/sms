/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2009 The Sakai Foundation
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
package org.sakaiproject.sms.entity;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.sms.logic.hibernate.SmsAccountLogic;
import org.sakaiproject.sms.logic.hibernate.exception.DuplicateUniqueFieldException;
import org.sakaiproject.sms.model.hibernate.SmsAccount;
import org.sakaiproject.sms.model.hibernate.constants.SmsConstants;

public class SmsAccountEntityProviderImp implements SmsAccountEntityProvider,
		RESTful, AutoRegisterEntityProvider {

	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	private DeveloperHelperService developerHelperService;

	public void setDeveloperHelperService(
			DeveloperHelperService developerHelperService) {
		this.developerHelperService = developerHelperService;
	}

	private SmsAccountLogic smsAccountLogic;

	public void setSmsAccountLogic(SmsAccountLogic smsAccountLogic) {
		this.smsAccountLogic = smsAccountLogic;
	}

	public String createEntity(EntityReference ref, Object entity,
			Map<String, Object> params) {

        if (!SecurityService.isSuperUser()) {
        	throw new SecurityException("Only admin users may manage accounts");	
        }
        
        // Validate the account fields
        
		SmsAccount smsAccount = (SmsAccount) entity;
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		
		if (smsAccount.getStartdate() == null)
			smsAccount.setStartdate(cal.getTime());
				
		if (smsAccount.getSakaiSiteId() == null){
			throw new IllegalArgumentException( "must have a site id" );
		}
		
		if (smsAccount.getMessageTypeCode() == null) {
			smsAccount.setMessageTypeCode(SmsConstants.MESSAGE_TYPE_CODE_SO); 
		}

		if (smsAccount.getAccountName() == null) {
			throw new IllegalArgumentException( "must have an account name" );			
		}
		
		// 0 credits for opening balance
		smsAccount.setCredits(0L);
		smsAccount.setOverdraftLimit(0L);
		
		smsAccount.setAccountEnabled(true);
		
		// Save the new account
		
		try {
			smsAccountLogic.persistSmsAccount(smsAccount);
		} catch (DuplicateUniqueFieldException e) {
			throw new IllegalArgumentException("An account for this site already exists");
		}

		// Success
		
		return smsAccount.getId().toString();		
	}

	/**
	 * entity methods
	 */

	public Object getSampleEntity() {
		return new SmsAccount();
	}

	public Object getEntity(EntityReference ref) {
		
		String id = ref.getId();
		if (id == null) {
			return new SmsAccount();
		}

		SmsAccount account = smsAccountLogic.getSmsAccount(Long.valueOf(id));
		if (account == null) {
			throw new IllegalArgumentException(
					"No sms account found for the given reference: " + ref);
		}

		String currentUserId = developerHelperService.getCurrentUserId();
		boolean allowedManage = false;
		
		if (!developerHelperService.isEntityRequestInternal(ref + "")) {
			// not an internal request so we require user to be logged in
			
			// TODO - relax these to permit users to retreive their own
			// account info. This is slightly unclear for accounts associated
			// with sites because we don't have a concept of account owner.
			
			if (currentUserId == null) {
				throw new SecurityException(
						"User must be logged in in order to access sms task: "
								+ ref);
			} else {
				String userReference = developerHelperService
						.getCurrentUserReference();
				allowedManage = developerHelperService
						.isUserAdmin(developerHelperService
								.getCurrentUserReference());
				if (!allowedManage
						&& !account.getSakaiUserId().equals(currentUserId)) {
					throw new SecurityException("User (" + userReference
							+ ") not allowed to access sms task: " + ref);
				}
			}
		}

		return account;
	}

	public void updateEntity(EntityReference ref, Object entity,
			Map<String, Object> params) {

        if (!SecurityService.isSuperUser()) {
        	throw new SecurityException("Only admin users may manage accounts");	
        }
		
		final String id = ref.getId();
		if (id == null) {
			throw new IllegalArgumentException(
					"The reference must include an id for updates (id is currently null)");
		}
		
		SmsAccount current = smsAccountLogic.getSmsAccount(Long.valueOf(id));
		if (current == null) {
			throw new IllegalArgumentException(
					"No sms task found to update for the given reference: "
							+ ref);
		}
		SmsAccount account = (SmsAccount) entity;

		// Update any fields except id and credits
		developerHelperService.copyBean(account, current, 0, new String[] {
				"id", "credits" }, true);
		
		// Update account
		smsAccountLogic.persistSmsAccount(account);
	}

	public void deleteEntity(EntityReference ref, Map<String, Object> params) {
		
        if (!SecurityService.isSuperUser()) {
        	throw new SecurityException("Only admin users may manage accounts");	
        }
		
		String id = ref.getId();
		if (id == null) {
			throw new IllegalArgumentException(
					"The reference must include an id for deletes (id is currently null)");
		}
		SmsAccount account = smsAccountLogic.getSmsAccount(Long.valueOf(id));
		if (account == null) {
			throw new IllegalArgumentException(
					"No account found for the given reference: " + ref);
		}

		smsAccountLogic.deleteSmsAccount(account);
	}

	public List<?> getEntities(EntityReference ref, Search search) {

		String currentUser = developerHelperService.getCurrentUserReference();
		if (currentUser == null) {
			throw new SecurityException("Anonymous users cannot view accounts: "
					+ ref);
		}
		
		// TODO support searching by site id
		
		Restriction userRes = search.getRestrictionByProperty("userId");
		String userId = null;
		if (userRes == null || userRes.getSingleValue() == null) {
			userId = developerHelperService.getCurrentUserId();
			if (userId == null) {
				throw new SecurityException(
						"User must be logged in in order to access sms accounts");
			}
			String userReference = developerHelperService
					.getCurrentUserReference();
			boolean allowedManage = developerHelperService
					.isUserAdmin(userReference);
			if (!allowedManage) {
				throw new SecurityException("User (" + userReference
						+ ") not allowed to access sms account: " + ref);
			}

		}
		List<SmsAccount> tasks = smsAccountLogic.getAllSmsAccounts();
		return tasks;

	}

	public String[] getHandledOutputFormats() {
		return new String[] { Formats.XML, Formats.JSON };
	}

	public String[] getHandledInputFormats() {
		return new String[] { Formats.XML, Formats.JSON, Formats.HTML };
	}

}
