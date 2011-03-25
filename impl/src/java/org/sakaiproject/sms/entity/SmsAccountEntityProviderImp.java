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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sms.logic.SmsAccountLogic;
import org.sakaiproject.sms.logic.exception.DuplicateUniqueFieldException;
import org.sakaiproject.sms.logic.exception.SmsInsufficientCreditsException;
import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.model.SmsAccount;
import org.sakaiproject.sms.model.constants.SmsConstants;

import org.sakaiproject.sms.logic.smpp.SmsBilling;

public class SmsAccountEntityProviderImp implements SmsAccountEntityProvider,
		RESTful, AutoRegisterEntityProvider {

	private static final Log LOG = LogFactory.getLog(SmsAccountEntityProviderImp.class);

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
	
	
	private ExternalLogic externalLogic;	
	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	private SmsBilling smsBilling;

	public void setSmsBilling(SmsBilling smsBilling) {
		this.smsBilling = smsBilling;
	}
	
	private SecurityService securityService;
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	private SiteService siteService;	
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public String createEntity(EntityReference ref, Object entity,
			Map<String, Object> params) {

        if (!securityService.isSuperUser()) {
        	throw new SecurityException("Only admin users may manage accounts");	
        }
        
        // Validate the account fields
        
		SmsAccount smsAccount = (SmsAccount) entity;
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		
		if (smsAccount.getStartdate() == null)
			smsAccount.setStartdate(cal.getTime());
				
		if (smsAccount.getSakaiSiteId() == null || "".equals(smsAccount.getSakaiSiteId().trim())) {
			throw new IllegalArgumentException( "must have a site id" );
		}
		
		if (smsAccount.getMessageTypeCode() == null) {
			smsAccount.setMessageTypeCode(SmsConstants.MESSAGE_TYPE_CODE_SO); 
		}

		if (smsAccount.getAccountName() == null) {
			throw new IllegalArgumentException( "must have an account name" );			
		}
		
		// 0 credits for opening balance
		smsAccount.setCredits(0);
		
		smsAccount.setAccountEnabled(true);
		
		
		//does the userId supplied match a sakai user?
		if (smsAccount.getOwnerId() != null && smsAccount.getOwnerId().length() >0) {
			if (!externalLogic.userExists(smsAccount.getOwnerId())) {
				throw new IllegalArgumentException(smsAccount.getOwnerId() + "  doesnt match a valid user");
			}
		}
		
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
		
		if (!developerHelperService.isEntityRequestInternal(ref + "")) {
			// not an internal request so we require user to be logged in
			if (currentUserId == null) {
				throw new SecurityException(
						"User must be logged in in order to access sms task: "
								+ ref);
			} else if (!currentUserId.equals(account.getOwnerId()) && !securityService.isSuperUser()) { 
				throw new SecurityException("User (" + currentUserId
						+ ") not allowed to access sms task: " + ref);
			}
		}

		return account;
	}

	public void updateEntity(EntityReference ref, Object entity,
			Map<String, Object> params) {

        if (!securityService.isSuperUser()) {
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
		
        if (!securityService.isSuperUser()) {
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
					"No account found for the given account "+ id);
		}

		smsAccountLogic.deleteSmsAccount(account);
	}

	public List<?> getEntities(EntityReference ref, Search search) {

		String currentUser = developerHelperService.getCurrentUserReference();
		if (currentUser == null) {
			throw new SecurityException("Anonymous users cannot view accounts: "
					+ ref);
		}
		
		// Return all accounts, or the account for a specific user or site
		
		Restriction userRes = search.getRestrictionByProperty("userId");
		Restriction siteRes = search.getRestrictionByProperty("siteId");

		// All accounts - admin only
		if (userRes == null && siteRes == null) {

			if (securityService.isSuperUser()) {
				return smsAccountLogic.getAllSmsAccounts();	
			}else{
				
				String currentUserId = developerHelperService.getCurrentUserId();
				return smsAccountLogic.getSmsAccountsForOwner(currentUserId);
			}
		}

		// Site - if the user has send permission in this site
		
		if (siteRes != null && siteRes.getStringValue() != null) {
			
			String siteId = siteRes.getStringValue();
			
			if (!securityService.unlock(ExternalLogic.SMS_SEND, siteService.siteReference(siteId))) {
				throw new SecurityException("No permission to view account info for this site");
			}

			LOG.info("looking for "  + siteId);
			
			List<SmsAccount> accounts = new ArrayList<SmsAccount>();
			SmsAccount account = smsAccountLogic.getSmsAccount(siteId, null);
			
			if (account != null) {
				accounts.add(account);
				LOG.info("added account");
			}
			
			return accounts;			
		}
		
		// User - if admin a given userid, otherwise current user
		
		// TODO - allow search by user eid as well
		
		String userId = developerHelperService.getCurrentUserId();
		
		if (userRes != null && userRes.getStringValue() != null && securityService.isSuperUser()) {
			userId = userRes.getStringValue();
		}
			
		return smsAccountLogic.getSmsAccountsForOwner(userId);
	
	}

	//Custom action to handle /sms-account/:ID:/credit 
	@EntityCustomAction(action=CUSTOM_ACTION_CREDIT,viewKey="")
	public String creditAccount(EntityReference ref, Map<String, Object> params) {

		if (!securityService.isSuperUser()) {
        	throw new SecurityException("Only admin users may manage accounts");	
        }

		String id = ref.getId();
		SmsAccount account = smsAccountLogic.getSmsAccount(Long.valueOf(id));
		if (account == null) {
			throw new IllegalArgumentException(
					"No account found for the given reference "  + id);
		}
		
		try {
			String credit = (String) params.get("credits");
			if (credit == null) {
				throw new IllegalArgumentException("No credit value given");
			}
			
			double cred = Double.valueOf(credit).doubleValue();
			
			String description = (String) params.get("description");
			
			 smsBilling.creditAccount(account.getId(), cred, description);
			
 		} catch (NumberFormatException e){
 			throw new IllegalArgumentException("Invalid credit value");
 		}
		
 		// Update balance
 		account = smsAccountLogic.getSmsAccount(Long.valueOf(id));
 		
		// return new balance of account

 		// get number format for default locale
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(1);

		return nf.format(account.getCredits());
	}

	//Custom action to handle /sms-account/transfer
	@EntityCustomAction(action=CUSTOM_ACTION_TRANSFER,viewKey=EntityView.VIEW_NEW)
	public void transferAccountCredit(EntityReference ref, Map<String, Object> params) {

		Long fromAccountId;
		Long toAccountId;
		try{
			fromAccountId = Long.parseLong(params.get("fromAccount").toString());
			toAccountId = Long.parseLong(params.get("toAccount").toString());
		} catch (NumberFormatException e){
 			throw new IllegalArgumentException("An account id value is invalid.");
 		}
		
		SmsAccount fromAccount = smsAccountLogic.getSmsAccount(fromAccountId);
		SmsAccount toAccount = smsAccountLogic.getSmsAccount(toAccountId);
		if (fromAccountId == null) {
			throw new IllegalArgumentException(
					"No account found to transfer FROM, given id: NULL");
		}
		if (toAccountId == null) {
			throw new IllegalArgumentException(
					"No account found to transfer TO, given id: NULL");
		}
		
		if(fromAccount == toAccount){
			throw new IllegalArgumentException(
					"Cannot transfer to same account: "  + fromAccountId);
		}

		try {
			String credit = (String) params.get("credits");
			if (credit == null) {
				throw new IllegalArgumentException("No credit value given");
			}
			double cred = Double.valueOf(credit).doubleValue();
			
			//Check permissions
			String currentUserId = developerHelperService.getCurrentUserId();
			boolean isFromAccountOwner = currentUserId.equals(fromAccount.getOwnerId());
			boolean isToAccountOwner = currentUserId.equals(toAccount.getOwnerId());
			
			if( (isFromAccountOwner && isToAccountOwner) || securityService.isSuperUser()){
				try{
					//Call transfer method
					smsBilling.transferAccountCredits(fromAccountId, toAccountId, cred);
				} catch (SmsInsufficientCreditsException e) {
					throw new IndexOutOfBoundsException(cred + "credits is too much to move from account: " + fromAccountId);
				}
			}else{
				throw new SecurityException("You do not own any valid accounts to make this transfer.");
			}
			
 		} catch (NumberFormatException e){
 			throw new IllegalArgumentException("Invalid credit value");
 		}
 		
	}
	
	public String[] getHandledOutputFormats() {
		return new String[] { Formats.XML, Formats.JSON };
	}

	public String[] getHandledInputFormats() {
		return new String[] { Formats.XML, Formats.JSON, Formats.HTML };
	}

}
