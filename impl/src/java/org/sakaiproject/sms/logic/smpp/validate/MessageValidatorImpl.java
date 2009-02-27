/***********************************************************************************
 * MessageValidator.java
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

import org.sakaiproject.sms.model.hibernate.SmsMessage;
import org.sakaiproject.sms.model.hibernate.constants.SmsHibernateConstants;
import org.sakaiproject.sms.model.hibernate.constants.ValidationConstants;

/**
 * This class is used to do varaious validations involving SmsTasks
 * 
 * @author julian@psybergate.com
 * @version 1.0
 * @created 12-Jan-2009
 */
public class MessageValidatorImpl implements MessageValidator {

	/**
	 * Validate message.
	 * 
	 * @param smsMessage
	 *            the sms message
	 * 
	 * @return the array list< string>
	 */
	public ArrayList<String> validateMessage(SmsMessage smsMessage) {
		ArrayList<String> errors = new ArrayList<String>();

		if (smsMessage.getMobileNumber() == null
				|| smsMessage.getMobileNumber().trim().equals("")) {
			errors.add(ValidationConstants.MOBILE_NUMBER_EMPTY);
		}

		if (smsMessage.getMobileNumber() != null
				&& !"".equals(smsMessage.getMobileNumber())) {
			String trimmedNumber = smsMessage.getMobileNumber().trim()
					.replaceAll(" ", "");
			// Check length of mobile number
			if (trimmedNumber.length() > SmsHibernateConstants.MAX_MOBILE_NR_LENGTH) {
				errors.add(ValidationConstants.MOBILE_NUMBER_TOO_LONG);
			}

			// Can start with + or not and can only contain digits
			if (!trimmedNumber.matches("[+]?[0-9 ]+")) {
				errors.add(ValidationConstants.MOBILE_NUMBER_INVALID);
			}
		}

		// Check sakai user id
		if (smsMessage.getSakaiUserId() == null
				|| smsMessage.getSakaiUserId().trim().equals("")) {
			errors.add(ValidationConstants.MESSAGE_SAKAI_USER_ID_EMPTY);
		}

		// Check status code
		if (smsMessage.getStatusCode() == null
				|| smsMessage.getStatusCode().trim().equals("")) {
			errors.add(ValidationConstants.MESSAGE_STATUS_CODE_EMPTY);
		}

		// Check task
		if (smsMessage.getSmsTask() == null) {
			errors.add(ValidationConstants.MESSAGE_TASK_ID_EMPTY);
		}
		return errors;
	}
}
