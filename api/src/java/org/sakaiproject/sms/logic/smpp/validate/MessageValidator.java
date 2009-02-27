package org.sakaiproject.sms.logic.smpp.validate;

import java.util.ArrayList;

import org.sakaiproject.sms.model.hibernate.SmsMessage;

public interface MessageValidator {

	public ArrayList<String> validateMessage(SmsMessage smsMessage);
}
