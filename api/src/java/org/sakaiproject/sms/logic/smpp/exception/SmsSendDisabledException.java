package org.sakaiproject.sms.logic.smpp.exception;

import org.sakaiproject.sms.model.hibernate.SmsTask;

public class SmsSendDisabledException extends Exception {

	private static final long serialVersionUID = 1L;

	public SmsSendDisabledException(SmsTask smsTask) {
		super("SMS sending is disabled for site :"
				+ smsTask.getSakaiSiteId().toString());
	}
}
