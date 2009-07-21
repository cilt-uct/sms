package org.sakaiproject.sms.tool.util;

import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.sms.model.hibernate.constants.SmsConst_DeliveryStatus;
import org.sakaiproject.sms.tool.producers.FailedSmsDetailProducer;
import org.sakaiproject.sms.tool.producers.ProgressSmsDetailProducer;
import org.sakaiproject.sms.tool.producers.SendSMSProducer;
import org.sakaiproject.sms.tool.producers.SentSmsDetailProducer;

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
	
	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}

	//populate status icon library
	private Map<String, String> getStatusLibrary(){
		Map<String, String> lib = new HashMap<String, String>();
		lib.put(SmsConst_DeliveryStatus.STATUS_ABORT, "cross");
		lib.put(SmsConst_DeliveryStatus.STATUS_BUSY, inprogressIcon);
		lib.put(SmsConst_DeliveryStatus.STATUS_DELIVERED, "tick");
		lib.put(SmsConst_DeliveryStatus.STATUS_ERROR, "cross");
		lib.put(SmsConst_DeliveryStatus.STATUS_EXPIRE, "cross");
		lib.put(SmsConst_DeliveryStatus.STATUS_FAIL, "cross");
		lib.put(SmsConst_DeliveryStatus.STATUS_INCOMPLETE, inprogressIcon);
		lib.put(SmsConst_DeliveryStatus.STATUS_LATE, inprogressIcon);
		lib.put(SmsConst_DeliveryStatus.STATUS_PENDING, "time");
		lib.put(SmsConst_DeliveryStatus.STATUS_RETRY, inprogressIcon);
		lib.put(SmsConst_DeliveryStatus.STATUS_SENT, inprogressIcon);
		lib.put(SmsConst_DeliveryStatus.STATUS_TASK_COMPLETED, "tick");
		lib.put(SmsConst_DeliveryStatus.STATUS_TIMEOUT, "cross");	
		lib.put(SmsConst_DeliveryStatus.STATUS_DRAFT, "comment_edit");	
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
	//populate path to specific view producer depending on status code
	private Map<String, String> getStatusProducer(){
		Map<String, String> lib = new HashMap<String, String>();
		lib.put(SmsConst_DeliveryStatus.STATUS_ABORT, FailedSmsDetailProducer.VIEW_ID);
		lib.put(SmsConst_DeliveryStatus.STATUS_BUSY, ProgressSmsDetailProducer.const_Inprogress);
		lib.put(SmsConst_DeliveryStatus.STATUS_DELIVERED, SentSmsDetailProducer.VIEW_ID);
		lib.put(SmsConst_DeliveryStatus.STATUS_ERROR, FailedSmsDetailProducer.VIEW_ID);
		lib.put(SmsConst_DeliveryStatus.STATUS_EXPIRE, FailedSmsDetailProducer.VIEW_ID);
		lib.put(SmsConst_DeliveryStatus.STATUS_FAIL, FailedSmsDetailProducer.VIEW_ID);
		lib.put(SmsConst_DeliveryStatus.STATUS_INCOMPLETE, ProgressSmsDetailProducer.const_Inprogress);
		lib.put(SmsConst_DeliveryStatus.STATUS_LATE, ProgressSmsDetailProducer.const_Inprogress);
		lib.put(SmsConst_DeliveryStatus.STATUS_PENDING, ProgressSmsDetailProducer.const_Scheduled);
		lib.put(SmsConst_DeliveryStatus.STATUS_RETRY, ProgressSmsDetailProducer.const_Inprogress);
		lib.put(SmsConst_DeliveryStatus.STATUS_SENT, SentSmsDetailProducer.VIEW_ID);
		lib.put(SmsConst_DeliveryStatus.STATUS_TASK_COMPLETED, SentSmsDetailProducer.VIEW_ID);
		lib.put(SmsConst_DeliveryStatus.STATUS_TIMEOUT, FailedSmsDetailProducer.VIEW_ID);
		lib.put(SmsConst_DeliveryStatus.STATUS_DRAFT, SendSMSProducer.VIEW_ID);	
		
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
	 * Retrieve readable name of status code
	 * @param statusCode Can be any of the {@link SmsConst_DeliveryStatus.STATUS_*}
	 * @return Readable i18N name
	 */
	public String getStatusProducer(String statusCode) {
		String view = getStatusProducer().get(statusCode);
		return view;
	}
}
