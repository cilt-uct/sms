/***********************************************************************************
 * HibernateLogicFactory.java
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

package org.sakaiproject.sms.logic.impl.hibernate;

import org.sakaiproject.sms.logic.hibernate.ExternalLogic;
import org.sakaiproject.sms.logic.hibernate.SmsAccountLogic;
import org.sakaiproject.sms.logic.hibernate.SmsConfigLogic;
import org.sakaiproject.sms.logic.hibernate.SmsMessageLogic;
import org.sakaiproject.sms.logic.hibernate.SmsTaskLogic;
import org.sakaiproject.sms.logic.hibernate.SmsTransactionLogic;

/**
 * Factory class that is used to get instances of the logc classes.
 * 
 * @author julian@psybergate.com
 * @version 1.0
 * @created 12-Jan-2009
 */
public class HibernateLogicFactory {

	/** The message logic. */
	private static SmsMessageLogic messageLogic;

	/** The task logic. */
	private static SmsTaskLogic taskLogic;

	/** The account logic. */
	private static SmsAccountLogic accountLogic;

	/** The transaction logic. */
	private static SmsTransactionLogic transactionLogic;

	/** The config logic. */
	private static SmsConfigLogic configLogic;

	/** The external logic. */
	private static ExternalLogicImpl externalLogic;

	/**
	 * Gets the message logic.
	 * 
	 * @return the message logic
	 */
	public static SmsMessageLogic getMessageLogic() {
		if (messageLogic == null) {
			messageLogic = new SmsMessageLogicImpl();
		}
		return messageLogic;
	}

	/**
	 * Gets the task logic.
	 * 
	 * @return the task logic
	 */
	public static SmsTaskLogic getTaskLogic() {
		if (taskLogic == null) {
			taskLogic = new SmsTaskLogicImpl();
		}
		return taskLogic;
	}

	/**
	 * Gets the account logic.
	 * 
	 * @return the account logic
	 */
	public static SmsAccountLogic getAccountLogic() {
		if (accountLogic == null) {
			accountLogic = new SmsAccountLogicImpl();
		}
		return accountLogic;
	}

	/**
	 * Gets the transaction logic.
	 * 
	 * @return the transaction logic
	 */
	public static SmsTransactionLogic getTransactionLogic() {
		if (transactionLogic == null) {
			transactionLogic = new SmsTransactionLogicImpl();
		}
		return transactionLogic;
	}

	/**
	 * Gets the config logic.
	 * 
	 * @return the config logic
	 */
	public static SmsConfigLogic getConfigLogic() {
		if (configLogic == null) {
			configLogic = new SmsConfigLogicImpl();
		}
		return configLogic;
	}

	/**
	 * Gets the external logic.
	 * 
	 * @return the external logic
	 */
	public static ExternalLogic getExternalLogic() {
		if (externalLogic == null) {
			externalLogic = new ExternalLogicImpl();
		}
		return externalLogic;
	}

}
