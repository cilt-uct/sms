package org.sakaiproject.sms.entity;

import java.util.List;
import java.util.Map;

import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.sms.logic.hibernate.SmsAccountLogic;
import org.sakaiproject.sms.model.hibernate.SmsAccount;

public class SmsAccountEntityProviderImp implements SmsAccountEntityProvider,
		RESTful, AutoRegisterEntityProvider {

	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}
	
	//TODO these need to come from the services
	private static String PERMISSION_MANAGE = "sms.manage";

	
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
	                allowedManage = developerHelperService.isUserAllowedInEntityReference(userReference, PERMISSION_MANAGE, "/site/" + account.getSakaiSiteId());
	                if (!allowedManage) {
	                    throw new SecurityException("User ("+userReference+") not allowed to access sms task: " + ref);
	                }
	            }
		 }
		
		return account;
	}
	
	public void updateEntity(EntityReference ref, Object entity,
			Map<String, Object> params) {
		// TODO Auto-generated method stub

	}

	

	public void deleteEntity(EntityReference ref, Map<String, Object> params) {
		// TODO Auto-generated method stub

	}

	public List<?> getEntities(EntityReference ref, Search search) {
		// TODO Auto-generated method stub
		return null;
	}

    public String[] getHandledOutputFormats() {
        return new String[] {Formats.XML, Formats.JSON};
    }

    public String[] getHandledInputFormats() {
        return new String[] {Formats.XML, Formats.JSON, Formats.HTML};
    }
	
}
