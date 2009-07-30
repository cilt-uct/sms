package org.sakaiproject.sms.tool.util;

import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.sms.model.hibernate.constants.SmsConst_DeliveryStatus;
import uk.org.ponder.messageutil.MessageLocator;

public class StatusUtils {
	
	private static final String statusIconDirectory = "/library/image/silk/";
	private static final String localImageDirectory = "../images/";
	
	private static final String statusIconExtension = ".png";
	
	//Special case for custom progress icon that is not included in the silk library 
	private static final String inprogressIcon = "phone_go";
	
	public static final String statusType_NEW = "NEW";
	public static final String statusType_EDIT = "EDIT";
	public static final String statusType_REUSE = "REUSE";
	
	//Status keywords for the task statuses grouped and simplified
	public static final String key_sent = "tick";
	public static final String key_failed = "cross";
	public static final String key_pending = "time";
	public static final String key_inprogress = inprogressIcon;
	
	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}

	//populate status icon library
	private Map<String, String> getStatusLibrary(){
		Map<String, String> lib = new HashMap<String, String>();
		lib.put(SmsConst_DeliveryStatus.STATUS_ABORT, key_failed);
		lib.put(SmsConst_DeliveryStatus.STATUS_BUSY, key_inprogress);
		lib.put(SmsConst_DeliveryStatus.STATUS_DELIVERED, key_sent);
		lib.put(SmsConst_DeliveryStatus.STATUS_ERROR, key_failed);
		lib.put(SmsConst_DeliveryStatus.STATUS_EXPIRE, key_failed);
		lib.put(SmsConst_DeliveryStatus.STATUS_FAIL, key_failed);
		lib.put(SmsConst_DeliveryStatus.STATUS_INCOMPLETE, key_inprogress);
		lib.put(SmsConst_DeliveryStatus.STATUS_LATE, key_inprogress);
		lib.put(SmsConst_DeliveryStatus.STATUS_PENDING, "time");
		lib.put(SmsConst_DeliveryStatus.STATUS_RETRY, key_inprogress);
		lib.put(SmsConst_DeliveryStatus.STATUS_SENT, key_sent);
		lib.put(SmsConst_DeliveryStatus.STATUS_TASK_COMPLETED, key_sent);
		lib.put(SmsConst_DeliveryStatus.STATUS_TIMEOUT, key_failed);	
		lib.put(SmsConst_DeliveryStatus.STATUS_DRAFT, "comment_edit");	//Drafts not yet supported
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
		lib.put(SmsConst_DeliveryStatus.STATUS_LATE, "status.late");
		lib.put(SmsConst_DeliveryStatus.STATUS_PENDING, "status.pending");
		lib.put(SmsConst_DeliveryStatus.STATUS_RETRY, "status.retry");
		lib.put(SmsConst_DeliveryStatus.STATUS_SENT, "status.sent");
		lib.put(SmsConst_DeliveryStatus.STATUS_TASK_COMPLETED, "status.completed");
		lib.put(SmsConst_DeliveryStatus.STATUS_TIMEOUT, "status.timeout");	
		lib.put(SmsConst_DeliveryStatus.STATUS_DRAFT, "status.draft");	
		return lib;	
	}
	
	/**
	 * Retrieve full path to status icon 
	 * @param statusCode Can be any of the {@link SmsConst_DeliveryStatus.STATUS_*}
	 * @return full path to icon e.g. "/library/image/silk/tick.png"
	 */
	public String getStatusIcon(String statusCode) {
		String icon = getStatusLibrary().get(statusCode);
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
		String view = getStatusLibrary().get(statusCode);
		return view;
	}
}
