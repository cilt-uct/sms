/***********************************************************************************
 * SmsAccountHelper.java
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
package org.sakaiproject.sms.tool.util;

import org.sakaiproject.sms.logic.SmsAccountLogic;
import org.sakaiproject.sms.model.SmsAccount;

import uk.org.ponder.beanutil.BeanGetter;

public class SmsAccountHelper {
	private BeanGetter ELEvaluator;
	private SmsAccountLogic smsAccountLogic;

	/**
	 * Retrieves the EL path (which must be the accountId of a SmsTask.
	 * Retrieves the SmsAccount that corresponds to the id and returns the
	 * credits.
	 */
	public SmsAccount retrieveAccount(String path) {
		Long accountId = (Long) ELEvaluator.getBean(path);
		SmsAccount smsAccount = smsAccountLogic.getSmsAccount(accountId);
		return smsAccount;
	}

	public void setELEvaluator(BeanGetter ELEvaluator) {
		this.ELEvaluator = ELEvaluator;
	}

	public void setSmsAccountLogic(SmsAccountLogic smsAccountLogic) {
		this.smsAccountLogic = smsAccountLogic;
	}
}
