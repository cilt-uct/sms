/***********************************************************************************
 * SmsMessageValidator.java
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
package org.sakaiproject.sms.tool.validators;

import java.util.List;

import org.sakaiproject.sms.logic.smpp.validate.MessageValidator;
import org.sakaiproject.sms.model.SmsMessage;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * The Class SmsMessageValidator.
 */
public class SmsMessageValidator implements Validator {

	private MessageValidator validator;

	public void setValidator(MessageValidator validator) {
		this.validator = validator;
	}

	/**
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public boolean supports(Class clazz) {
		if (SmsMessage.class.equals(clazz.getClass())) {
			return true;
		}
		return false;
	}

	/**
	 * Only basic validation at this stage
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 */
	public void validate(Object obj, Errors err) {

		List<String> errors = validator.validateMessage((SmsMessage) obj);

		for (String error : errors) {
			err.reject(error, error);
		}
	}
}
