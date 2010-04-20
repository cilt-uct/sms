/***********************************************************************************
 * SmsAccountValidator.java
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

import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.model.SmsAccount;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class SmsAccountValidator implements Validator {

	private ExternalLogic externalLogic;

	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}


	private boolean isEmptyOrNull(String field) {
		if (field == null) {
			return true;
		} else {
			return (field.trim().length() == 0);
		}
	}

	private boolean isTooLong(String field, int max) {
		if (field != null) {
			if (field.length() > max) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public boolean supports(Class clazz) {
		if (SmsAccount.class.equals(clazz.getClass())) {
			return true;
		}
		return false;
	}

	public void validate(Object obj, Errors err) {
		SmsAccount smsAccount = (SmsAccount) obj;

		// I feel so dirty writing this but we have to have some way to know if
		// the bean has been submitted for saving or if the datepicker/cancel
		// button was used see AccountProducer
		if (smsAccount.getMessageTypeCode() == null) {
			return;
		}

		ValidationUtils.rejectIfEmptyOrWhitespace(err, "accountName",
				"sms.errors.accountName.empty");

		if (isEmptyOrNull(smsAccount.getSakaiUserId())
				&& isEmptyOrNull(smsAccount.getSakaiSiteId())) {
			err.reject("sms.errors.site-user-id.empty");
		}

		ValidationUtils.rejectIfEmpty(err, "credits",
				"sms.errors.credits.invalid");

		if (isTooLong(smsAccount.getAccountName(), 99)) {
			err.rejectValue("accountName", "sms.errors.accountName.tooLong");
		}

		if (isTooLong(smsAccount.getSakaiSiteId(), 99)) {
			err.rejectValue("sakaiSiteId", "sms.errors.sakaiSiteId.tooLong");
		}

		if (isTooLong(smsAccount.getSakaiUserId(), 99)) {
			err.rejectValue("sakaiUserId", "sms.errors.sakaiUserId.tooLong");
		}

		// TODO: Validate site id and user id against Sakai
		if (!isEmptyOrNull(smsAccount.getSakaiSiteId())) {
			if (!externalLogic.isValidSite(smsAccount.getSakaiSiteId())) {
				err
						.rejectValue("sakaiSiteId",
								"sms.errors.sakaiSiteId.invalid");
			}
		}

		if (!isEmptyOrNull(smsAccount.getSakaiUserId())) {
			if (!externalLogic.isValidUser(smsAccount.getSakaiUserId())) {
				err
						.rejectValue("sakaiUserId",
								"sms.errors.sakaiUserId.invalid");
			}
		}
	}
}
