package org.sakaiproject.sms.entity;

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.azeckoski.reflectutils.transcoders.XMLTranscoder;
import org.jsmpp.util.TimeFormatter;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
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

public class SmsTaskEntityProviderImpl implements SmsTaskEntityProvider, AutoRegisterEntityProvider, RESTful{

	private static Log log = LogFactory.getLog(SmsTaskEntityProvider.class);

	//TODO these need to come from the services
	private static String PERMISSION_MANAGE = "sms.manage";
	private static String DATE_FORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss";

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
		Date simpleDate = new Date();
		if (task.getDateCreated() == null)
			task.setDateCreated(simpleDate);
		//Assert date params to task object. Default EB casting to task doesn't set these. SMS-28
		SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT_ISO8601);
		Iterator<Entry<String, Object>> selector = params.entrySet().iterator();
		while ( selector.hasNext() ) {
        	Entry<String, Object> pairs = selector.next();
        	String paramsKey = pairs.getKey();
        	String paramsValue = pairs.getValue().toString();
        	if( "dateToSend".equals(paramsKey) ){
    			try {
					task.setDateToSend( formatter.parse(paramsValue) );
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    	}
    	if( "dateToExpire".equals(paramsKey) ){
			try {
				task.setDateToExpire( formatter.parse(paramsValue) );
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
    	if( "copyMe".equals(paramsKey) ){
    		boolean addMe = Boolean.parseBoolean(paramsValue);
    		Set<String> newUserIds = new HashSet<String>();
    		if( addMe ){
    			newUserIds.add(task.getSenderUserId());
	    		if (task.getSakaiUserIds() != null){
	    			newUserIds.addAll(task.getSakaiUserIds());
	    			task.setSakaiUserIds(newUserIds);
	    		}else{
	    			task.setSakaiUserIds(newUserIds);
	    		}
    		}else{
    			if( task.getSakaiUserIds().contains(task.getSenderUserId()) ){
    				newUserIds = task.getSakaiUserIds();
    				newUserIds.remove(task.getSenderUserId());
    				task.setSakaiUserIds(newUserIds);
    			}
    		}
    	}
		}
		
		for (String u : task.getSakaiUserIds()){
			log.info("NEW USER id LIST: "+ u);
		}
		
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
        return new String[] {Formats.XML};
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

	//Custom action to handle /sms-task/calculate
	@EntityCustomAction(action=CUSTOM_ACTION,viewKey=EntityView.VIEW_NEW)
	public String calculate(EntityReference ref, Map<String, Object> params) {
		SmsTask smsTask = (SmsTask) getSampleEntity();
		//Build smsTask object with received parameters
		Iterator<Entry<String, Object>> selector = params.entrySet().iterator();
		while ( selector.hasNext() ) {
        	Entry<String, Object> pairs = selector.next();
        	String paramsKey = pairs.getKey();
        	String paramsValue = pairs.getValue().toString();
        	log.info("key: "+ paramsKey);
            log.info("Value: "+paramsValue.toString());
            if( "sakaiSiteId".equals(paramsKey) && ! "".equals( paramsValue)){
        		smsTask.setSakaiSiteId(paramsValue.toString());
        	}
        	if( "senderUserId".equals(paramsKey) && ! "".equals( paramsValue)){
        		smsTask.setSenderUserId(paramsValue.toString());
        	} 
        	if( "senderUserName".equals(paramsKey) && ! "".equals( paramsValue)){
        		smsTask.setSenderUserName(paramsValue.toString());
        	}  
        	if( "deliveryEntityList".equals(paramsKey) && ! "".equals( paramsValue)){
        		smsTask.setDeliveryEntityList(Arrays.asList(paramsValue.split(",")));
        		log.info("key: "+ paramsKey);
            	log.info("Value: "+paramsValue.toString());
        	} 
            if( "sakaiUserIds".equals(paramsKey) && ! "".equals( paramsValue)){
            	log.info("key: "+ paramsKey);
            	log.info("Value: "+paramsValue.toString());
            	List<String> tempListValues = Arrays.asList(paramsValue.split(","));
        		Set<String> tempSetValues = new HashSet<String>(tempListValues);
            	smsTask.setSakaiUserIds(tempSetValues);
        	} 
        	if( "deliveryMobileNumbersSet".equals(paramsKey) && ! "".equals( paramsValue)){
        		List<String> tempListValues = Arrays.asList(paramsValue.split(","));
        		Set<String> tempSetValues = new HashSet<String>(tempListValues);
        		smsTask.setDeliveryMobileNumbersSet(tempSetValues);
        	}
        	
		}
		
		if (smsTask.getSakaiSiteId() == null || smsTask.getSenderUserId() == null || smsTask.getSenderUserName() == null){
			throw new IllegalArgumentException("ALL of these parameters need to be set: sakaiSiteId or senderUserId or senderUserName");
		}
		if (smsTask.getDeliveryEntityList() == null && smsTask.getSakaiUserIds() == null && smsTask.getDeliveryMobileNumbersSet() == null ){
			throw new IllegalArgumentException("At least one of these parameters need to be set: deliveryMobileNumbersSet or sakaiUserIds or deliveryEntityList");
		}
		
		String userReference = developerHelperService.getCurrentUserReference();
		 boolean allowedManage = developerHelperService.isUserAllowedInEntityReference(userReference, PERMISSION_MANAGE, "/site/" + smsTask.getSakaiSiteId());
         if (!allowedManage) {
             throw new SecurityException("User ("+userReference+") not allowed to access sms task: " + ref);
         }

         smsService.calculateEstimatedGroupSize(smsTask);
         
        if (!smsService.checkSufficientCredits(smsTask.getSakaiSiteId(), smsTask.getSenderUserId(), smsTask.getGroupSizeEstimate(),false)) {
			throw new SecurityException("User ("+ smsTask.getSenderUserId() +") has insuficient credit to send sms task: " + ref);
		}
		return XMLTranscoder.makeXML(smsTask);
	}
}
