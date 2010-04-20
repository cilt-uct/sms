package org.sakaiproject.sms.tool.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.sms.model.SmsMessage;
import org.sakaiproject.sms.model.SmsTask;
import org.sakaiproject.sms.model.constants.SmsConst_DeliveryStatus;

import uk.org.ponder.messageutil.MessageLocator;

public class StatusUtils {
	
	private static final String statusIconDirectory = "/library/image/silk/";
	private static final String localImageDirectory = "../images/";
	
	private static final String statusIconExtension = ".png";
	
	private static final String status_task = "task";
	private static final String status_message = "message";
	
	//Special case for custom progress icon that is not included in the silk library 
	private static final String inprogressIcon = "phone_go";
	
	public static final String statusType_NEW = "NEW";
	public static final String statusType_EDIT = "EDIT";
	public static final String statusType_REUSE = "REUSE";
	
	//Status keywords for the task statuses grouped and simplified
	public static final String key_tick = "tick";
	public static final String key_cross = "cross";
	public static final String key_time = "time";
	public static final String key_phone = inprogressIcon;
	
	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}

	//populate status icon library
	private Map<String, String> getStatusLibrary(String type){
		Map<String, String> lib = new HashMap<String, String>();
		if( status_task.equals(type)){
			lib.put(SmsConst_DeliveryStatus.STATUS_SENT, key_tick);
		}
		else{
			lib.put(SmsConst_DeliveryStatus.STATUS_SENT, key_phone);
		}
		//Add icons that are the same between a task and message
		lib.put(SmsConst_DeliveryStatus.STATUS_ABORT, key_cross);
		lib.put(SmsConst_DeliveryStatus.STATUS_BUSY, key_phone);
		lib.put(SmsConst_DeliveryStatus.STATUS_DELIVERED, key_tick);
		lib.put(SmsConst_DeliveryStatus.STATUS_ERROR, key_cross);
		lib.put(SmsConst_DeliveryStatus.STATUS_EXPIRE, key_cross);
		lib.put(SmsConst_DeliveryStatus.STATUS_FAIL, key_cross);
		lib.put(SmsConst_DeliveryStatus.STATUS_INCOMPLETE, key_phone);
		lib.put(SmsConst_DeliveryStatus.STATUS_PENDING, key_time);
		lib.put(SmsConst_DeliveryStatus.STATUS_RETRY, key_time);
		lib.put(SmsConst_DeliveryStatus.STATUS_TASK_COMPLETED, key_tick);
		lib.put(SmsConst_DeliveryStatus.STATUS_TIMEOUT, key_phone);
		return lib;	
	}
	
	//populate status library with messageKeys
	private Map<String, String> getStatusMessageKeys(){
		Map<String, String> lib = new HashMap<String, String>();
		lib.put(SmsConst_DeliveryStatus.STATUS_ABORT, "status.abort");
		lib.put(SmsConst_DeliveryStatus.STATUS_BUSY, "status.busy");
		lib.put(SmsConst_DeliveryStatus.STATUS_DELIVERED, "status.delivered");
		lib.put(SmsConst_DeliveryStatus.STATUS_ERROR, "status.error");
		lib.put(SmsConst_DeliveryStatus.STATUS_EXPIRE, "status.expire");
		lib.put(SmsConst_DeliveryStatus.STATUS_FAIL, "status.fail");
		lib.put(SmsConst_DeliveryStatus.STATUS_INCOMPLETE, "status.incomplete");
		lib.put(SmsConst_DeliveryStatus.STATUS_PENDING, "status.pending");
		lib.put(SmsConst_DeliveryStatus.STATUS_RETRY, "status.retry");
		lib.put(SmsConst_DeliveryStatus.STATUS_SENT, "status.sent");
		lib.put(SmsConst_DeliveryStatus.STATUS_TASK_COMPLETED, "status.completed");
		lib.put(SmsConst_DeliveryStatus.STATUS_TIMEOUT, "status.timeout");	
		return lib;	
	}
	
	/**
	 * Retrieve full path to status icon for A {@link SmsMessage}
	 * @param statusCode Can be any of the {@link SmsConst_DeliveryStatus.STATUS_*}
	 * @return full path to icon e.g. "/library/image/silk/tick.png"
	 */
	public String getMessageStatusIcon(String statusCode) {
		String icon = getStatusLibrary(status_message).get(statusCode);
		return inprogressIcon.equals(icon) ? 
				localImageDirectory + inprogressIcon + statusIconExtension 
				: statusIconDirectory + icon + statusIconExtension;
	}
	
	/**
	 * Retrieve full path to status icon FOR AN {@link SmsTask}
	 * @param statusCode Can be any of the {@link SmsConst_DeliveryStatus.STATUS_*}
	 * @return full path to icon e.g. "/library/image/silk/tick.png"
	 */
	public String getTaskStatusIcon(String statusCode) {
		String icon = getStatusLibrary(status_task).get(statusCode);
		return inprogressIcon.equals(icon) ? 
				localImageDirectory + inprogressIcon + statusIconExtension 
				: statusIconDirectory + icon + statusIconExtension;
	}
	
	/**
	 * Retrieve readable name of status code
	 * @param statusCode Can be any of the {@link SmsConst_DeliveryStatus.STATUS_*}
	 * @return Readable i18N name
	 */
	public String getStatusFullName(String statusCode) {
		String nameKey = getStatusMessageKeys().get(statusCode);
		return messageLocator.getMessage(nameKey);
	}	
	
	/**
	 * Retrieve a key representing the group of a status 
	 * eg: {@link StatusUtils.key_sent} = status codes corresponding to a successfully processed task
	 * @param statusCode Can be any of the {@link SmsConst_DeliveryStatus.STATUS_*}
	 * @return key can be any of the {@link StatusUtils.key_*}
	 */
	public String getStatusUIKey(String statusCode) {
		String view = getStatusLibrary(status_task).get(statusCode);
		return view;
	}
	
	/**
	 * Check is the UI should show this task as a 'busy' task.
	 * In this context, eg. the date/time column will not show a date/time but rather readable text version of the status.
	 * @param statusCode Can be any of the {@link SmsConst_DeliveryStatus.STATUS_*}
	 * @return 
	 */
	public boolean isTaskBusy(String statusCode){
		List<String> busyStatusCodes = new ArrayList<String>();
		//Store codes that the UI should read as 'busy' codes
		busyStatusCodes.add(SmsConst_DeliveryStatus.STATUS_INCOMPLETE);
		busyStatusCodes.add(SmsConst_DeliveryStatus.STATUS_BUSY);
		return busyStatusCodes.contains(statusCode);
	}
}
