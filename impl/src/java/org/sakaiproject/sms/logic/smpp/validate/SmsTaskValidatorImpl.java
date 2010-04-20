/**********************************************************************************
 * $URL: $
 * $Id: $
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
package org.sakaiproject.sms.logic.smpp.validate;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.sms.logic.smpp.SmsBilling;
import org.sakaiproject.sms.model.SmsTask;
import org.sakaiproject.sms.model.constants.SmsConstants;
import org.sakaiproject.sms.model.constants.ValidationConstants;

/**
 * This class is used to do various validations involving SmsTasks. It is called
 * from the UI and also when a test is persisted.
 * 
 * @author julian@psybergate.com
 * @version 1.0
 * @created 12-Jan-2009
 */
public class SmsTaskValidatorImpl implements SmsTaskValidator {

	private SmsBilling smsBilling = null;

	public SmsBilling getSmsBilling() {
		return smsBilling;
	}

	public void setSmsBilling(SmsBilling smsBilling) {
		this.smsBilling = smsBilling;
	}

	/**
	 * Check sufficient credits.
	 * 
	 * @param smsTask
	 *            the sms task
	 * 
	 * @return the array list< string>
	 */
	public List<String> checkSufficientCredits(SmsTask smsTask,
			boolean overDraftCheck) {
		List<String> errors = new ArrayList<String>();
		// check for sufficient balance

		final boolean sufficientCredits = smsBilling.checkSufficientCredits(
				smsTask.getSmsAccountId(), smsTask.getCreditEstimate(),
				overDraftCheck);
		if (!sufficientCredits) {
			errors.add(ValidationConstants.INSUFFICIENT_CREDIT
					+ " in account id " + smsTask.getSmsAccountId());
		}
		return errors;
	}

	/**
	 * Validate insert task.
	 * 
	 * @param smsTask
	 *            the sms task
	 * 
	 * @return the array list< string>
	 */
	public List<String> validateInsertTask(SmsTask smsTask) {
		// called by getPrelimTask()
		List<String> errors = new ArrayList<String>();

		// Check sakai site id
		if (smsTask.getSakaiSiteId() == null
				|| smsTask.getSakaiSiteId().trim().equals("")) {
			errors.add(ValidationConstants.TASK_SAKAI_SITE_ID_EMPTY);
		}
		if (smsTask.getDateToExpire() == null) {
			errors.add(ValidationConstants.TASK_DATE_TO_EXPIRE_EMPTY);
		}

		// Check account id
		if (smsTask.getSmsAccountId() == null) {
			errors.add(ValidationConstants.TASK_ACCOUNT_EMPTY);
		}

		// Check date created
		if (smsTask.getDateCreated() == null) {
			errors.add(ValidationConstants.TASK_DATE_CREATED_EMPTY);
		}

		// Check date to send
		if (smsTask.getDateToSend() == null) {
			errors.add(ValidationConstants.TASK_DATE_TO_SEND_EMPTY);
		}

		// Check status code
		if (smsTask.getStatusCode() == null
				|| smsTask.getStatusCode().trim().equals("")) {
			errors.add(ValidationConstants.TASK_STATUS_CODE_EMPTY);
		}

		// Check message type
		if (smsTask.getMessageTypeId() == null) {
			errors.add(ValidationConstants.TASK_MESSAGE_TYPE_EMPTY);
		}

		// Check message body
		if (smsTask.getMessageBody() == null) {
			errors.add(ValidationConstants.MESSAGE_BODY_EMPTY);

		} else {
			// Check length of messageBody
			if (smsTask.getMessageBody().length() > SmsConstants.MAX_SMS_LENGTH) {
				errors.add(ValidationConstants.MESSAGE_BODY_TOO_LONG);
			}
		}
		
		// TODO also check character set on message body

		// Check sender user name
		if (smsTask.getSenderUserName() == null
				|| smsTask.getSenderUserName().trim().equals("")) {
			errors.add(ValidationConstants.TASK_SENDER_USER_NAME_EMPTY);
		}

		// Check max time to live
		if (smsTask.getMaxTimeToLive() == null
				|| smsTask.getMaxTimeToLive() < 1) {
			errors.add(ValidationConstants.TASK_MAX_TIME_TO_LIVE_INVALID);
		}

		// Check that date to send comes after expiry date
		if ( smsTask.getDateToSend() != null && smsTask.getDateToExpire() != null && smsTask.getDateToExpire().before(smsTask.getDateToSend()) ){
			errors.add(ValidationConstants.TASK_DATE_TO_SEND_AFTER_EXPIRY);
		}

		// Check that date to send is not equal to the expiry date
		if ( smsTask.getDateToSend() != null && smsTask.getDateToExpire() != null && smsTask.getDateToExpire().equals(smsTask.getDateToSend()) ){
			errors.add(ValidationConstants.TASK_DATE_TO_SEND_EQUALS_EXPIRY);
		}

		return errors;
	}

}
