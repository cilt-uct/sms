/***********************************************************************************
 * TaskValidator.java
 * Copyright (c) 2008 Sakai Project/Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.osedu.org/licenses/ECL-2.0
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

import org.sakaiproject.sms.logic.smpp.SmsBilling;
import org.sakaiproject.sms.model.hibernate.SmsTask;
import org.sakaiproject.sms.model.hibernate.constants.SmsHibernateConstants;
import org.sakaiproject.sms.model.hibernate.constants.ValidationConstants;

/**
 * This class is used to do various validations involving SmsTasks. It is called
 * from the UI and also when a test is persisted.
 *
 * @author julian@psybergate.com
 * @version 1.0
 * @created 12-Jan-2009
 */
public class SmsTaskValidatorImpl implements SmsTaskValidator {

	private SmsBilling smsBilling;

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
	public ArrayList<String> checkSufficientCredits(SmsTask smsTask) {
		ArrayList<String> errors = new ArrayList<String>();
		// check for sufficient balance
		;
		boolean sufficientCredits = smsBilling.checkSufficientCredits(smsTask
				.getSmsAccountId(), smsTask.getCreditEstimate());
		if (!sufficientCredits) {
			errors.add(ValidationConstants.INSUFFICIENT_CREDIT);
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
	public ArrayList<String> validateInsertTask(SmsTask smsTask) {
		// called by getPrelimTask()
		ArrayList<String> errors = new ArrayList<String>();

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
		if (smsTask.getMessageBody() != null
				&& !smsTask.getMessageBody().trim().equals("")) {
			// Check length of messageBody
			if (smsTask.getMessageBody().length() > SmsHibernateConstants.MAX_SMS_LENGTH) {
				errors.add(ValidationConstants.MESSAGE_BODY_TOO_LONG);
			}
		} else {
			errors.add(ValidationConstants.MESSAGE_BODY_EMPTY);
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

		// Check delivery report timeout
		if (smsTask.getDelReportTimeoutDuration() == null
				|| smsTask.getDelReportTimeoutDuration() < 1) {
			errors
					.add(ValidationConstants.TASK_DELIVERY_REPORT_TIMEOUT_INVALID);
		}

		// Check delivery group id
		if (smsTask.getDeliveryGroupId() == null
				|| smsTask.getDeliveryGroupId().trim().equals("")) {
			errors.add(ValidationConstants.TASK_DELIVERY_GROUP_ID_EMPTY);
		}

		return errors;
	}

}
