/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009 The Sakai Foundation
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
package org.sakaiproject.sms.logic;

import org.sakaiproject.sms.logic.external.ExternalLogic;

/**
 * Factory class that is used to get instances of the logic classes.
 *
 * @author julian@psybergate.com
 * @version 1.0
 * @created 12-Jan-2009
 */
public class HibernateLogicLocator{

	/** The message logic. */
	private SmsMessageLogic smsMessageLogic;

	/** The task logic. */
	private SmsTaskLogic smsTaskLogic;

	/** The account logic. */
	private SmsAccountLogic smsAccountLogic;

	/** The transaction logic. */
	private SmsTransactionLogic smsTransactionLogic;

	/** The config logic. */
	private SmsConfigLogic smsConfigLogic;

	/** The external logic. */
	private ExternalLogic externalLogic;

	public SmsMessageLogic getSmsMessageLogic() {
		return smsMessageLogic;
	}

	public void setSmsMessageLogic(SmsMessageLogic smsMessageLogic) {
		this.smsMessageLogic = smsMessageLogic;
	}

	public SmsTaskLogic getSmsTaskLogic() {
		return smsTaskLogic;
	}

	public void setSmsTaskLogic(SmsTaskLogic smsTaskLogic) {
		this.smsTaskLogic = smsTaskLogic;
	}

	public SmsAccountLogic getSmsAccountLogic() {
		return smsAccountLogic;
	}

	public void setSmsAccountLogic(SmsAccountLogic smsAccountLogic) {
		this.smsAccountLogic = smsAccountLogic;
	}

	public SmsTransactionLogic getSmsTransactionLogic() {
		return smsTransactionLogic;
	}

	public void setSmsTransactionLogic(SmsTransactionLogic smsTransactionLogic) {
		this.smsTransactionLogic = smsTransactionLogic;
	}

	public SmsConfigLogic getSmsConfigLogic() {
		return smsConfigLogic;
	}

	public void setSmsConfigLogic(SmsConfigLogic smsConfigLogic) {
		this.smsConfigLogic = smsConfigLogic;
	}

	public ExternalLogic getExternalLogic() {
		return externalLogic;
	}

	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

}
