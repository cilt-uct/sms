package org.sakaiproject.sms.logic.smpp.validate;

import java.util.ArrayList;

import org.sakaiproject.sms.model.hibernate.SmsTask;

public interface SmsTaskValidator {

	public ArrayList<String> checkSufficientCredits(SmsTask smsTask,boolean overDraftCheck);

	public ArrayList<String> validateInsertTask(SmsTask smsTask);

}
