/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007 The Sakai Foundation
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

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.azeckoski.reflectutils.transcoders.JSONTranscoder;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Statisticable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sms.bean.SearchFilterBean;
import org.sakaiproject.sms.logic.SmsMessageLogic;
import org.sakaiproject.sms.logic.SmsTaskLogic;
import org.sakaiproject.sms.logic.exception.SmsSearchException;
import org.sakaiproject.sms.logic.exception.SmsTaskNotFoundException;
import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.smpp.SmsService;
import org.sakaiproject.sms.logic.smpp.SmsTaskValidationException;
import org.sakaiproject.sms.logic.smpp.exception.ReceiveIncomingSmsDisabledException;
import org.sakaiproject.sms.logic.smpp.exception.SmsSendDeniedException;
import org.sakaiproject.sms.logic.smpp.exception.SmsSendDisabledException;
import org.sakaiproject.sms.logic.smpp.util.MessageCatalog;
import org.sakaiproject.sms.logic.smpp.validate.SmsTaskValidator;
import org.sakaiproject.sms.model.SmsMessage;
import org.sakaiproject.sms.model.SmsTask;
import org.sakaiproject.sms.model.constants.SmsConst_DeliveryStatus;
import org.sakaiproject.sms.model.constants.ValidationConstants;
import org.sakaiproject.sms.util.SmsMessageUtil;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;

public class SmsTaskEntityProviderImpl implements SmsTaskEntityProvider, AutoRegisterEntityProvider, RESTful, Statisticable {

	private static Log log = LogFactory.getLog(SmsTaskEntityProvider.class);

	/**
	 *	Permission to send SMS messages in a site, therefore also create and manage the resulting tasks  
	 */
	private static String PERMISSION_SEND = ExternalLogic.SMS_SEND;
	
	private static String DATE_FORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss";
	
	//Used by the MessageCatalogue class to fetch the messages bundle from the pom.xml defined location
	private static String MESSAGE_BUNDLE = "messages.";
	
	// Statisticable for event visibility in SiteStats
	public final static String		TOOL_ID			= "sakai.sms.user";
	public final static String		EVENT_NEW		= "sms.task.new";
	public final static String		EVENT_EDIT		= "sms.task.revise";
	public final static String		EVENT_DELETE	= "sms.task.delete";
	public final static String[]	EVENT_KEYS		= 
							new String[] {
								EVENT_NEW, EVENT_EDIT, EVENT_DELETE
							};
	
	/**
	 * Inject services
	 */
	private SmsTaskLogic smsTaskLogic = null;
	public void setSmsTaskLogic(SmsTaskLogic smsTaskLogic) {
		this.smsTaskLogic = smsTaskLogic;
	}

	private SmsService smsService = null;
	public void setSmsService(SmsService smsService) {
		this.smsService = smsService;
	}

	private DeveloperHelperService developerHelperService = null;
	public void setDeveloperHelperService(
			DeveloperHelperService developerHelperService) {
		this.developerHelperService = developerHelperService;
	}
	
	private ExternalLogic externalLogic = null;
	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	private SmsMessageLogic smsMessageLogic = null;
	public void setSmsMessageLogic(SmsMessageLogic smsMessageLogic) {
		this.smsMessageLogic = smsMessageLogic;
	}
	
	private SmsTaskValidator smsTaskValidator;
	public void setSmsTaskValidator(SmsTaskValidator smsTaskValidator) {
		this.smsTaskValidator = smsTaskValidator;
	}

	private SecurityService securityService;
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	private SiteService siteService;	
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}
	
	private UserDirectoryService userDirectoryService;
	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
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

		SmsTask task = smsTaskLogic.getSmsTask(Long.valueOf(id));
		 if (task == null) {
				throw new IllegalArgumentException( getMessage( ValidationConstants.TASK_NOEXIST )); 
	        }

		 String currentUserId = developerHelperService.getCurrentUserId();
		 boolean allowedSend = false;
		 if (! developerHelperService.isEntityRequestInternal(ref+"")) {
	            // not an internal request so we require user to be logged in
	            if (currentUserId == null) {
	    			throw new SecurityException( getMessage(ValidationConstants.USER_NOTALLOWED_ACCESS_SMS, new String[] {ref.getId(), " --- "} ));
	            } else {
	                String userReference = developerHelperService.getCurrentUserReference();
	                allowedSend = developerHelperService.isUserAllowedInEntityReference(userReference, PERMISSION_SEND, siteService.siteReference(task.getSakaiSiteId()));
	                if (!allowedSend) {
	        			throw new SecurityException( getMessage(ValidationConstants.USER_NOTALLOWED_ACCESS_SMS, new String[] {ref.getId(), userReference} ));
	                }
	            }
		 }
		 task = anonymiseTask(task);
		//round off cost properties to 2 decimal places
         roundOffTaskCosts(task);
		return task;
	}

	private SmsTask anonymiseTask(SmsTask task) {
		
		// SMS-159 Use a helper method to avoid lazy init exception
		List<SmsMessage> messages = smsMessageLogic.getSmsMessagesForTask(task.getId());
		if (messages == null || messages.isEmpty())
			return task;
		
		Set<SmsMessage> redacted = new HashSet<SmsMessage>();
		
		for (SmsMessage message : messages) {
			SmsMessage redMessage = new SmsMessage();
			redMessage.setStatusCode(message.getStatusCode());
			if (message.getSakaiUserId() != null  && !"".equals(message.getSakaiUserId())) {
				redMessage.setSakaiUserId(message.getSakaiUserId());
			} else {
				redMessage.setMobileNumber(message.getMobileNumber());
			}
			redMessage.setDateDelivered(message.getDateDelivered());
			redMessage.setDateQueued(message.getDateQueued());
			redMessage.setDateSent(message.getDateSent());
			redMessage.setFailReason(message.getFailReason());
			redMessage.setSmscMessageId(message.getSmscMessageId());
			redMessage.setSmscId(message.getSmscId());
			redMessage.setSmscDeliveryStatusCode(message.getSmscDeliveryStatusCode());
			redacted.add(redMessage);
		}
		task.setSmsMessages(redacted);
		return task;
	}

	public String createEntity(EntityReference ref, Object entity,
			Map<String, Object> params) {
		SmsTask smsTask = (SmsTask) entity;
			
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		//Give a 15 minute leeway before deeming a task as a past task. Better this be done here than by client JS
		cal.add(Calendar.MINUTE, -15);
		Date simpleDate = cal.getTime();
		if (smsTask.getDateCreated() == null)
			smsTask.setDateCreated(simpleDate);

	
         if (!securityService.unlock(PERMISSION_SEND, siteService.siteReference(smsTask.getSakaiSiteId()))) {
             throw new SecurityException( getMessage(ValidationConstants.USER_NOTALLOWED_SEND_SMS ));
         }
         
		// Assert task properties not set by EB casting at SmsTask task = (SmsTask) entity.
		setPropertyFromParams(smsTask, params, ref);
		
		if (smsTask.getSakaiSiteId() == null){
			throw new IllegalArgumentException( getMessage(ValidationConstants.TASK_SAKAI_SITE_ID_EMPTY) );
		}
		if ( (smsTask.getDeliveryEntityList() == null || smsTask.getDeliveryEntityList().size() == 0) 
				&& (smsTask.getSakaiUserIdsList() == null || smsTask.getSakaiUserIdsList().size() == 0)
				&& (smsTask.getDeliveryMobileNumbersSet() == null || smsTask.getDeliveryMobileNumbersSet().size() == 0 ) ){
			throw new IllegalArgumentException( getMessage(ValidationConstants.TASK_RECIPIENTS_EMPTY) );
		}
		 smsService.calculateEstimatedGroupSize(smsTask);
		
		if (!smsService.checkSufficientCredits(smsTask.getSakaiSiteId(), smsTask.getSenderUserId(), smsTask.getCreditEstimate(), false)) {
			throw new IllegalArgumentException( getMessage( ValidationConstants.INSUFFICIENT_CREDIT )); 
		}

		// Set the tool ID to the user tool if not set
		
		if (smsTask.getSakaiToolId() == null) {
			smsTask.setSakaiToolId(TOOL_ID);
		}
		
		try {
			smsService.insertTask(smsTask);
		} catch (SmsTaskValidationException e) {
			throw new IllegalArgumentException(e.getErrorMessagesAsBlock());
		} catch (SmsSendDeniedException e) {
            throw new SecurityException( getMessage(ValidationConstants.USER_NOTALLOWED_SEND_SMS ));
		} catch (SmsSendDisabledException e) {
            throw new SecurityException( getMessage(ValidationConstants.TASK_SEND_DISABLED, new String[] {smsTask.getId().toString()} ));
		} catch (ReceiveIncomingSmsDisabledException e) {
            throw new SecurityException( getMessage(ValidationConstants.TASK_INCOMING_DISABLED, new String[] {smsTask.getId().toString()} ));
		}
		
		// Success
		
		return smsTask.getId().toString();
	}

	public void updateEntity(EntityReference ref, Object entity,
			Map<String, Object> params) {
		String id = ref.getId();
		if (id == null) {
			throw new IllegalArgumentException( getMessage( ValidationConstants.TASK_NOEXIST )); 
		}
        String userReference = developerHelperService.getCurrentUserReference();
        if (userReference == null) {
			throw new SecurityException( getMessage(ValidationConstants.USER_NOTALLOWED_ACCESS_SMS, new String[] {ref.getId(), null} ));
        }


        SmsTask current = smsTaskLogic.getSmsTask(Long.valueOf(id));
        if (current == null) {
			throw new IllegalArgumentException( getMessage( ValidationConstants.TASK_NOEXIST )); 
        }
        
        SmsTask task = (SmsTask) entity;

		//Assert task properties not set by EB casting at SmsTask task = (SmsTask) entity.
        setPropertyFromParams(task, params, ref);
        
        boolean allowedSend = developerHelperService.isUserAllowedInEntityReference(userReference, PERMISSION_SEND, siteService.siteReference(task.getSakaiSiteId()));
        if (!allowedSend) {
            throw new SecurityException("User ("+userReference+") not allowed to access sms task: " + ref);
        }

        // Assumes entities are only updated while they're still pending
        current.setSmsMessages(null);
        
        developerHelperService.copyBean(task, current, 0, new String[] {"id", "creationDate"}, true);

		// Sanitize the character set in the message body
		task.setMessageBody(SmsMessageUtil.sanitizeMessageBody(task.getMessageBody()));
		
		try{
			ArrayList<String> errors = new ArrayList<String>();
			errors.addAll(smsTaskValidator.validateInsertTask(task));
			if (!errors.isEmpty()) {
				throw new SmsTaskValidationException(
						errors,
						getMessage("messages.sms.errors.task.validationFailed"));
			}
		} catch (SmsTaskValidationException e) {
			StringBuilder errorBuilder = new StringBuilder();
			for ( String s : e.getErrorMessages()){
				errorBuilder.append(getMessage(s)).append("\n ");
			}
			throw new IllegalArgumentException(errorBuilder.toString());
		}

        smsTaskLogic.persistSmsTask(task);
        
        externalLogic.postEvent(ExternalLogic.SMS_EVENT_TASK_REVISE, "/sms-task/" + task.getId(), task.getSakaiSiteId());
	}

	public void deleteEntity(EntityReference ref, Map<String, Object> params) {
		String id = ref.getId();
		if (id == null)
			throw new IllegalArgumentException( getMessage( ValidationConstants.TASK_NOEXIST )); 

		SmsTask task = smsTaskLogic.getSmsTask(Long.valueOf(id));
		if (task == null) {
			throw new IllegalArgumentException( getMessage( ValidationConstants.TASK_NOEXIST )); 
        }

		String userReference = developerHelperService.getCurrentUserReference();
		boolean allowedSend = developerHelperService.isUserAllowedInEntityReference(userReference, PERMISSION_SEND, siteService.siteReference(task.getSakaiSiteId()));
        if (!allowedSend) {
			throw new SecurityException( getMessage(ValidationConstants.USER_NOTALLOWED_ACCESS_SMS, new String[] {ref.getId(), userReference} ));
        }

        if (!task.getStatusCode().equals(SmsConst_DeliveryStatus.STATUS_PENDING)) {
        	// TODO better error code
        	throw new IllegalArgumentException( getMessage( ValidationConstants.TASK_INVALID));    	
        }
        
    	try {
			log.debug("Starting abort and delete process for task:" + task.getId());
			smsService.abortPendingTask(task.getId());
    	} catch (SmsTaskNotFoundException e) {
			throw new IllegalArgumentException( getMessage( ValidationConstants.TASK_INVALID, new String[] {ref.getId()} )); 
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
			throw new SecurityException( getMessage(ValidationConstants.USER_NOTALLOWED_ACCESS_SMS, new String[] {ref.getId(), " --- "} ));
        }
        Restriction userRes = search.getRestrictionByProperty("userId");
        String userId = null;
        if (userRes == null || userRes.getSingleValue() == null) {
        	userId = developerHelperService.getCurrentUserId();
        	if (userId == null) {
                throw new SecurityException( getMessage(ValidationConstants.USER_NOTALLOWED_ACCESS_SMS, new String[] {ref.getId(), " --- "} ));

        	}
        }
        try {
        	List<SmsTask> tasks = smsTaskLogic.getAllSmsTasksForCriteria(new SearchFilterBean());
        	for ( SmsTask smsTask : tasks ){
        		 //round off cost properties to 2 decimal places
                roundOffTaskCosts(smsTask);
        	}
			return tasks;
		} catch (SmsSearchException e) {
			throw new IllegalArgumentException("invalid search parameters: " + e);
		}
	}
	
	//Custom action to handle /sms-task/calculate
	@EntityCustomAction(action=CUSTOM_ACTION_CALCULATE,viewKey=EntityView.VIEW_NEW)
	public String calculate(EntityReference ref, Map<String, Object> params) {
		SmsTask smsTask = (SmsTask) getSampleEntity();
		//Build smsTask object with received parameters since object casting will cause an NPE
		Iterator<Entry<String, Object>> selector = params.entrySet().iterator();
		while ( selector.hasNext() ) {
			Entry<String, Object> pairs = selector.next();
			String paramsKey = pairs.getKey();
			String paramsValue = pairs.getValue().toString();

			if( "sakaiSiteId".equals(paramsKey) && ! "".equals( paramsValue)){
				smsTask.setSakaiSiteId(paramsValue);
	    	}else 
	    	if( "deliveryEntityList".equals(paramsKey) && ! "".equals( paramsValue)){
	    		smsTask.setDeliveryEntityList(Arrays.asList(paramsValue.split(",")));
	    	}else 
	        if( "sakaiUserIds".equals(paramsKey) && ! "".equals( paramsValue)){
	        	List<String> tempListValues = Arrays.asList(paramsValue.split(","));
	    		Set<String> tempSetValues = new HashSet<String>(tempListValues);
	    		smsTask.setSakaiUserIdsList(tempSetValues);
	    	}else 
	    	if( "deliveryMobileNumbersSet".equals(paramsKey) && ! "".equals( paramsValue)){
	    		List<String> tempListValues = Arrays.asList(paramsValue.split(","));
	    		Set<String> tempSetValues = new HashSet<String>(tempListValues);
	    		smsTask.setDeliveryMobileNumbersSet(tempSetValues);
	    	}
		}
		
		setPropertyFromParams(smsTask, params, ref);
		
		String userReference = developerHelperService.getCurrentUserReference();
		String senderId = EntityReference.getIdFromRef(userReference);
		
		//Set sensitive sender info now
		smsTask.setSenderUserId(senderId);
		//At this stage we don't really need the correct sender username since this sms task is never persisted
		smsTask.setSenderUserName( "---" );
		log.debug("User with id="+ senderId +" is calculating a new SMS.");
		
		
		if (smsTask.getSakaiSiteId() == null){
			throw new IllegalArgumentException( getMessage(ValidationConstants.TASK_SAKAI_SITE_ID_EMPTY) );
		}
		if (smsTask.getDeliveryEntityList() == null && smsTask.getSakaiUserIdsList() == null && smsTask.getDeliveryMobileNumbersSet() == null ){
			throw new IllegalArgumentException( getMessage(ValidationConstants.TASK_RECIPIENTS_EMPTY) );
		}

		 boolean allowedSend = developerHelperService.isUserAllowedInEntityReference(userReference, PERMISSION_SEND, siteService.siteReference(smsTask.getSakaiSiteId()));
         if (!allowedSend) {
             throw new SecurityException( getMessage(ValidationConstants.USER_NOTALLOWED_SEND_SMS ));
         }

         smsService.calculateEstimatedGroupSize(smsTask);
       
         if (!smsService.checkSufficientCredits(smsTask.getSakaiSiteId(), smsTask.getSenderUserId(), smsTask.getCreditEstimate(), false)) {
 			throw new IllegalArgumentException( getMessage( ValidationConstants.INSUFFICIENT_CREDIT )); 
 		}
         
         //round off cost properties to 2 decimal places
        roundOffTaskCosts(smsTask);
        return JSONTranscoder.makeJSON(smsTask);
	}
	
	private SmsTask roundOffTaskCosts(final SmsTask smsTask) {
		DecimalFormat decimalFormat = new DecimalFormat("#0.00");
		
		smsTask.setCreditCost( Double.parseDouble(decimalFormat.format(smsTask.getCreditCost())) );
		smsTask.setCreditEstimate(Double.parseDouble(decimalFormat.format(smsTask.getCreditEstimate())) );

		return smsTask;
	}

	//Custom action to handle /sms-task/memberships
	@EntityCustomAction(action=CUSTOM_ACTION_USERS,viewKey=EntityView.VIEW_LIST)
	public List<SimpleUser> memberships(EntityView view, Map<String, Object> params) {
		String siteId = view.getPathSegment(3);
		if (siteId == null) {
        	siteId = (String) params.get("site");
            if (siteId == null) {
                throw new IllegalArgumentException("The site id must be set in order to get the site memberships, set in params or in the URL /sms-task/memberships/site/siteId");
            }
        }
		String currentUser = developerHelperService.getCurrentUserReference();
        if (currentUser == null) {
            throw new SecurityException( getMessage(ValidationConstants.USER_ANONYMOUS_CANNOT_VIEW_MEMBERS, new String[] { siteId } ));
        }
		
		String userId = developerHelperService.getCurrentUserId();
		
		if (!securityService.unlock(userId, PERMISSION_SEND, siteService.siteReference(siteId))) {
			throw new SecurityException( getMessage(ValidationConstants.USER_ANONYMOUS_CANNOT_VIEW_MEMBERS, new String[] {siteId, userId} ));
		}
		
        List<SimpleUser> users = new ArrayList<SimpleUser>();
		List<User> usersFull = externalLogic.getUsersWithMobileNumbersOnly(siteId);
		if (!usersFull.isEmpty()){
			for ( User user : usersFull ){
				//trim down user object to only show essential non-sensitive properties
				SimpleUser fakeUser = new SimpleUser( user.getId(), user.getDisplayId(), user.getSortName() );
				users.add(fakeUser);
			}
			 // handle the sorting
	        Collections.sort(users, new SortNameComparator());
		}
		return users;
	}
	
	private void setPropertyFromParams(SmsTask task, Map<String, Object> params, EntityReference ref) {
		//SMS-28 Only manually set 'copy to sender' and date properties passed via EB. Native object casting will set all other properties.
		
		//Assert date params to task object. Default EB casting to task doesn't set these. SMS-28
		SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT_ISO8601);
		
		//Save copy boolean and only set it when all params have been iterated and sender ids set
		boolean copy = false;

		Iterator<Entry<String, Object>> selector = params.entrySet().iterator();
		while ( selector.hasNext() ) {
			Entry<String, Object> pairs = selector.next();
			String paramsKey = pairs.getKey();
			String paramsValue = pairs.getValue().toString();
			
	    	if( (ref.getId() != null || task.getDateToSend() == null) && "dateToSend".equals(paramsKey) ){
    			try {
					task.setDateToSend( formatter.parse(paramsValue) );
					if ( task.getDateCreated().after( task.getDateToSend() )){
						throw new IllegalArgumentException( getMessage( ValidationConstants.DATE_SEND_IN_PAST, new String[] {task.getDateToSend().toString()} ));
					}
				} catch (ParseException e) {
					throw new IllegalArgumentException( getMessage( ValidationConstants.DATE_FORMAT_INCORRECT ));
				}
	    	}
	    	else if( (ref.getId() != null || task.getDateToExpire() == null) && "dateToExpire".equals(paramsKey) ){
	    		try {
					task.setDateToExpire( formatter.parse(paramsValue) );
				} catch (ParseException e) {
					throw new IllegalArgumentException( getMessage( ValidationConstants.DATE_FORMAT_INCORRECT ));
				}		
	    	}
	    	else if( "copyMe".equals(paramsKey) ){
	    		copy = Boolean.parseBoolean(paramsValue);	
	    	}
		}
        
		// Set user info - only allow sending as someone else if admin
		User user = userDirectoryService.getCurrentUser();

		String senderDisplayName = user.getDisplayName();
		if ( senderDisplayName == null || "".equals(senderDisplayName) ){
			senderDisplayName = user.getDisplayId();
		}
		task.setSenderUserName(senderDisplayName);
		task.setSenderUserId(user.getId());
		
		// Set copy me status
		Set<String> newUserIds = new HashSet<String>();
		if (copy && ! task.getSakaiUserIdsList().contains(task.getSenderUserId()) ) {
			newUserIds.add(task.getSenderUserId());
			if (task.getSakaiUserIdsList() != null) {
				newUserIds.addAll(task.getSakaiUserIdsList());
				task.setSakaiUserIdsList(newUserIds);
			} else {
				task.setSakaiUserIdsList(newUserIds);
			}
		}
	}
	
	public static class SortNameComparator implements Comparator<SimpleUser> {
        public static final long serialVersionUID = 31L;
        public int compare(SimpleUser o1, SimpleUser o2) {
            return o1.getSortName().compareTo(o2.getSortName());
        }
    }
	
	private String getMessage( String key ){
		String fullKey = MESSAGE_BUNDLE + key;
		return MessageCatalog.getMessage(fullKey);
	}
	
	private String getMessage( String key , String[] parameters){
		String fullKey = MESSAGE_BUNDLE + key;
		return MessageCatalog.getMessage(fullKey, parameters);
	}
	
	/**
	 * Inner class holds trimmed-down properties of the user object
	 * @author lovemorenalube
	 *
	 */
	public class SimpleUser {
		private String id, displayId, sortName;
		public SimpleUser (String id, String displayId, String sortName){
			this.id = id;
			this.displayId = displayId;
			this.sortName = sortName;
		}
		
		public String getSortName() {
			return this.sortName;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getDisplayId() {
			return displayId;
		}

		public void setDisplayId(String displayId) {
			this.displayId = displayId;
		}

		public void setSortName(String sortName) {
			this.sortName = sortName;
		}	
	}

	public String getAssociatedToolId() {
		return TOOL_ID;
	}

	public String[] getEventKeys() {
		return EVENT_KEYS;
	}

	public Map<String, String> getEventNames(Locale locale) {
		Map<String, String> localeEventNames = new HashMap<String, String>(); 
		ResourceLoader msgs = new ResourceLoader("Events");
		msgs.setContextLocale(locale);
		for(int i=0; i<EVENT_KEYS.length; i++) {
			localeEventNames.put(EVENT_KEYS[i], msgs.getString(EVENT_KEYS[i]));
		}
		return localeEventNames;
	}

}
