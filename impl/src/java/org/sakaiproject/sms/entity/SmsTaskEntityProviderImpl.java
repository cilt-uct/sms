package org.sakaiproject.sms.entity;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.sms.bean.SearchFilterBean;
import org.sakaiproject.sms.logic.hibernate.SmsTaskLogic;
import org.sakaiproject.sms.logic.hibernate.exception.SmsSearchException;
import org.sakaiproject.sms.logic.smpp.SmsService;
import org.sakaiproject.sms.logic.smpp.SmsTaskValidationException;
import org.sakaiproject.sms.logic.smpp.exception.ReceiveIncomingSmsDisabledException;
import org.sakaiproject.sms.logic.smpp.exception.SmsSendDeniedException;
import org.sakaiproject.sms.logic.smpp.exception.SmsSendDisabledException;
import org.sakaiproject.sms.model.hibernate.SmsTask;
import org.sakaiproject.sms.model.hibernate.constants.SmsConst_DeliveryStatus;

public class SmsTaskEntityProviderImpl implements SmsTaskEntityProvider, AutoRegisterEntityProvider, RESTful {

	private static Log log = LogFactory.getLog(SmsTaskEntityProvider.class);

	//TODO these need to come from the services
	private static String PERMISSION_MANAGE = "sms.manage";

	/**
	 * Inject services
	 */
	private SmsTaskLogic smsTaskLogic;
	public void setSmsTaskLogic(SmsTaskLogic smsTaskLogic) {
		this.smsTaskLogic = smsTaskLogic;
	}

	private SmsService smsService;
	public void setSmsService(SmsService smsService) {
		this.smsService = smsService;
	}

	private DeveloperHelperService developerHelperService;
	public void setDeveloperHelperService(
			DeveloperHelperService developerHelperService) {
		this.developerHelperService = developerHelperService;
	}


	/**
	 * Implemented
	 */

	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}


	public Object getSampleEntity() {
		return new SmsTask();
	}

	public Object getEntity(EntityReference ref) {
		String id = ref.getId();
		if (id == null)
				return new SmsTask();

		SmsTask task = smsTaskLogic.getSmsTask(new Long(id));
		 if (task == null) {
	            throw new IllegalArgumentException("No sms task found for the given reference: " + ref);
	        }

		 String currentUserId = developerHelperService.getCurrentUserId();
		 boolean allowedManage = false;
		 if (! developerHelperService.isEntityRequestInternal(ref+"")) {
	            // not an internal request so we require user to be logged in
	            if (currentUserId == null) {
	                throw new SecurityException("User must be logged in in order to access sms task: " + ref);
	            } else {
	                String userReference = developerHelperService.getCurrentUserReference();
	                allowedManage = developerHelperService.isUserAllowedInEntityReference(userReference, PERMISSION_MANAGE, "/site/" + task.getSakaiSiteId());
	                if (!allowedManage) {
	                    throw new SecurityException("User ("+userReference+") not allowed to access sms task: " + ref);
	                }
	            }
		 }

		return task;
	}

	public String createEntity(EntityReference ref, Object entity,
			Map<String, Object> params) {
		SmsTask task = (SmsTask) entity;
		if (task.getDateCreated() == null)
			task.setDateCreated(new Date());
		//Mark new task as a draft and future date it due to workflow when user creates a Task
		task.setStatusCode(SmsConst_DeliveryStatus.STATUS_DRAFT);
		if (task.getDateToSend() == null){
			Date future = new Date();
			future.setYear(2200);
			task.setDateToSend(future);
		}
		//set message body to '-------';
		if( "".equals(task.getMessageBody()) ){
			task.setMessageBody("------");
		}
		
		log.info("Send date is: "+task.getDateToSend());

		 String userReference = developerHelperService.getCurrentUserReference();
		 boolean allowedManage = developerHelperService.isUserAllowedInEntityReference(userReference, PERMISSION_MANAGE, "/site/" + task.getSakaiSiteId());
         if (!allowedManage) {
             throw new SecurityException("User ("+userReference+") not allowed to access sms task: " + ref);
         }

		smsService.calculateEstimatedGroupSize(task);
		if (!smsService.checkSufficientCredits(task.getSakaiSiteId(), task.getSenderUserId(), task.getGroupSizeEstimate(),false)) {
			throw new SecurityException("User ("+ task.getSenderUserId() +") has insuficient credit to send sms task: " + ref);
		}

		try {
			smsService.insertTask(task);
			return task.getId().toString();

		} catch (SmsTaskValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SmsSendDeniedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SmsSendDisabledException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ReceiveIncomingSmsDisabledException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
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


        SmsTask current = smsTaskLogic.getSmsTask(new Long(id));
        if (current == null) {
            throw new IllegalArgumentException("No sms task found to update for the given reference: " + ref);
        }


        SmsTask task = (SmsTask) entity;
        boolean allowedManage = developerHelperService.isUserAllowedInEntityReference(userReference, PERMISSION_MANAGE, "/site/" + task.getSakaiSiteId());
        if (!allowedManage) {
            throw new SecurityException("User ("+userReference+") not allowed to access sms task: " + ref);
        }

        developerHelperService.copyBean(task, current, 0, new String[] {"id", "creationDate"}, true);
        //TODO validate the task
        smsTaskLogic.persistSmsTask(task);

	}

	public void deleteEntity(EntityReference ref, Map<String, Object> params) {
		String id = ref.getId();
		if (id == null)
			throw new IllegalArgumentException("The reference must include an id for deletes (id is currently null)");

		SmsTask task = smsTaskLogic.getSmsTask(Long.valueOf(id));
		if (task == null) {
            throw new IllegalArgumentException("No poll found for the given reference: " + ref);
        }

		String userReference = developerHelperService.getCurrentUserReference();
		boolean allowedManage = developerHelperService.isUserAllowedInEntityReference(userReference, PERMISSION_MANAGE, "/site/" + task.getSakaiSiteId());
        if (!allowedManage) {
            throw new SecurityException("User ("+userReference+") not allowed to access sms task: " + ref);
        }

		smsTaskLogic.deleteSmsTask(task);
	}

    public String[] getHandledOutputFormats() {
        return new String[] {Formats.XML, Formats.JSON};
    }

    public String[] getHandledInputFormats() {
        return new String[] {Formats.XML, Formats.JSON, Formats.HTML};
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
                throw new SecurityException("User must be logged in in order to access sms task: " + ref);
        	}

        }
        try {
        	List<SmsTask> tasks = smsTaskLogic.getAllSmsTasksForCriteria(new SearchFilterBean());
			return tasks;
		} catch (SmsSearchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return null;
	}


}
