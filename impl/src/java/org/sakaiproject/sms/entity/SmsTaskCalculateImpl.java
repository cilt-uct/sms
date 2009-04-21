package org.sakaiproject.sms.entity;

import java.util.Map;

import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Createable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Inputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.sms.logic.hibernate.SmsAccountLogic;
import org.sakaiproject.sms.logic.smpp.SmsService;
import org.sakaiproject.sms.model.hibernate.SmsTask;

public class SmsTaskCalculateImpl implements SmsTaskCalculate, AutoRegisterEntityProvider, Outputable, Inputable, Createable, Resolvable {
	//TODO these need to come from the services
	private static String PERMISSION_MANAGE = "sms.manage";

	/**
	 * Inject services
	 */
	private SmsService smsService;
	public void setSmsService(SmsService smsService) {
		this.smsService = smsService;
	}
	
	private SmsAccountLogic smsAccountLogic;
	public void setSmsAccountLogic(SmsAccountLogic smsAccountLogic) {
		this.smsAccountLogic = smsAccountLogic;
	}

	private DeveloperHelperService developerHelperService;
	public void setDeveloperHelperService(
			DeveloperHelperService developerHelperService) {
		this.developerHelperService = developerHelperService;
	}

	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	public String[] getHandledOutputFormats() {
        return new String[] {Formats.XML, Formats.JSON};
    }

    public String[] getHandledInputFormats() {
        return new String[] {Formats.XML, Formats.JSON, Formats.HTML};
    }

	public Object getSampleEntity() {
		return new SmsTask();
	}

	public String createEntity(EntityReference ref, Object entity,
			Map<String, Object> params) {
		SmsTask task = (SmsTask) entity;
		String userReference = developerHelperService.getCurrentUserReference();
		 boolean allowedManage = developerHelperService.isUserAllowedInEntityReference(userReference, PERMISSION_MANAGE, "/site/" + task.getSakaiSiteId());
         if (!allowedManage) {
             throw new SecurityException("User ("+userReference+") not allowed to access sms task: " + ref);
         }

		smsService.calculateEstimatedGroupSize(task);
		
        if (!smsService.checkSufficientCredits(task.getSakaiSiteId(), task.getSenderUserId(), task.getGroupSizeEstimate(),false)) {
			throw new SecurityException("User ("+ task.getSenderUserId() +") has insuficient credit to send sms task: " + ref);
		}

		return "{ selected:"+task.getGroupSizeEstimate()+", credits:"+task.getCreditEstimate()+", cost:"+task.getCostEstimate()+", accountCredits:"+smsAccountLogic.getSmsAccount(Long.parseLong(task.getSenderUserId()))+"}";
	}

	public Object getEntity(EntityReference ref) {
		// TODO Auto-generated method stub
		return null;
	}

}
