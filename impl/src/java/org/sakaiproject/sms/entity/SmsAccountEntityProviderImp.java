package org.sakaiproject.sms.entity;

import java.util.List;
import java.util.Map;

import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.sms.logic.hibernate.SmsAccountLogic;
import org.sakaiproject.sms.model.hibernate.SmsAccount;

public class SmsAccountEntityProviderImp implements SmsAccountEntityProvider,
		RESTful, AutoRegisterEntityProvider {

	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	private DeveloperHelperService developerHelperService;
	public void setDeveloperHelperService(DeveloperHelperService developerHelperService) {
		this.developerHelperService = developerHelperService;
	}

	private SmsAccountLogic smsAccountLogic;
	
	public void setSmsAccountLogic(SmsAccountLogic smsAccountLogic) {
		this.smsAccountLogic = smsAccountLogic;
	}

	public String createEntity(EntityReference ref, Object entity,
			Map<String, Object> params) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 *  entity methods
	 */
	
	public Object getSampleEntity() {
		return new SmsAccount();
	}

	public Object getEntity(EntityReference ref) {
		String id = ref.getId();
		if (id == null)
			return new SmsAccount();
		
		SmsAccount account = smsAccountLogic.getSmsAccount(Long.valueOf(id));
		if (account == null) {
			throw new IllegalArgumentException("No sms account found for the given reference: " + ref);
		}
		
		String currentUserId = developerHelperService.getCurrentUserId();
		boolean allowedManage = false;
		 if (! developerHelperService.isEntityRequestInternal(ref+"")) {
	            // not an internal request so we require user to be logged in
	            if (currentUserId == null) {
	                throw new SecurityException("User must be logged in in order to access sms task: " + ref);
	            } else {
	                String userReference = developerHelperService.getCurrentUserReference();
	                allowedManage = developerHelperService.isUserAdmin(developerHelperService.getCurrentUserReference());
	                if (!allowedManage && !account.getSakaiUserId().equals(currentUserId)) {
	                    throw new SecurityException("User ("+userReference+") not allowed to access sms task: " + ref);
	                }
	            }
		 }
		
		return account;
	}
	
	public void updateEntity(EntityReference ref, Object entity,
			Map<String, Object> params) {
		String id = ref.getId();
		if (id == null) {
			throw new IllegalArgumentException("The reference must include an id for updates (id is currently null)");
		}
        String userReference = developerHelperService.getCurrentUserReference();
        if (userReference == null) {
            throw new SecurityException("anonymous user cannot update task: " + ref);
        }
        
        SmsAccount current = smsAccountLogic.getSmsAccount(Long.valueOf(id));
        if (current == null) {
            throw new IllegalArgumentException("No sms task found to update for the given reference: " + ref);
        }
        
        SmsAccount account = (SmsAccount) entity;
        boolean allowedManage = developerHelperService.isUserAdmin(developerHelperService.getCurrentUserReference());
        if (!allowedManage) {
            throw new SecurityException("User ("+userReference+") not allowed to access sms task: " + ref);
        }
        
        developerHelperService.copyBean(account, current, 0, new String[] {"id", "creationDate"}, true);
        //TODO validate the task
        smsAccountLogic.persistSmsAccount(account);
        
	}

	

	public void deleteEntity(EntityReference ref, Map<String, Object> params) {
		String id = ref.getId();
		if (id == null)
			throw new IllegalArgumentException("The reference must include an id for deletes (id is currently null)");
		
		SmsAccount account = smsAccountLogic.getSmsAccount(Long.valueOf(id));
		if (account == null) {
            throw new IllegalArgumentException("No poll found for the given reference: " + ref);
        }
		
		String userReference = developerHelperService.getCurrentUserReference();
		boolean allowedManage = developerHelperService.isUserAdmin(userReference);
        if (!allowedManage) {
            throw new SecurityException("User (" + userReference + ") not allowed to access sms account: " + ref);
        }
        
        smsAccountLogic.deleteSmsAccount(account);
	}

	public List<?> getEntities(EntityReference ref, Search search) {
		String currentUser = developerHelperService.getCurrentUserReference();
        if (currentUser == null) {
            throw new SecurityException("Anonymous users cannot view votes: " + ref);
        }
        Restriction userRes = search.getRestrictionByProperty("userId");
        String userId = null;
        if (userRes == null || userRes.getSingleValue() == null) {
        	userId = developerHelperService.getCurrentUserId();
        	if (userId == null) {
                throw new SecurityException("User must be logged in in order to access sms accounts");
        	}
        	String userReference = developerHelperService.getCurrentUserReference();
        	boolean allowedManage = developerHelperService.isUserAdmin(userReference);
            if (!allowedManage) {
                throw new SecurityException("User (" + userReference + ") not allowed to access sms task: " + ref);
            }
        	
        }
             	List<SmsAccount> tasks = smsAccountLogic.getAllSmsAccounts();
			return tasks;
		
	}

    public String[] getHandledOutputFormats() {
        return new String[] {Formats.XML, Formats.JSON};
    }

    public String[] getHandledInputFormats() {
        return new String[] {Formats.XML, Formats.JSON, Formats.HTML};
    }
	
}
