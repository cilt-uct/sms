package org.sakaiproject.sms.logic.smpp.validate;

import java.util.List;

import org.sakaiproject.sms.model.hibernate.SmsTask;

public interface SmsTaskValidator {

	public List<String> checkSufficientCredits(SmsTask smsTask,
			boolean overDraftCheck);

	public List<String> validateInsertTask(SmsTask smsTask);

}
