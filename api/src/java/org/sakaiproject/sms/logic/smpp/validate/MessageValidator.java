package org.sakaiproject.sms.logic.smpp.validate;

import java.util.List;

import org.sakaiproject.sms.model.hibernate.SmsMessage;

public interface MessageValidator {

	public List<String> validateMessage(SmsMessage smsMessage);
}
