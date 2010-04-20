/***********************************************************************************
 * SmsMessageLocator.java
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
package org.sakaiproject.sms.tool.otp;

import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.sms.logic.SmsMessageLogic;
import org.sakaiproject.sms.model.SmsMessage;

import uk.org.ponder.beanutil.BeanLocator;

/**
 * The Class SmsMessageLocator.
 */
public class SmsMessageLocator implements BeanLocator {

	/** The Constant LOCATOR_NAME. */
	public static final String LOCATOR_NAME = "SmsMessageLocator";

	public static final String NEW_PREFIX = "new ";

	/** The Constant NEW_1. */
	public static final String NEW_1 = NEW_PREFIX + "1";

	/** The delivered map (used to store beans). */
	private final Map<String, SmsMessage> delivered = new HashMap<String, SmsMessage>();

	private SmsMessageLogic smsMessageLogic;

	/**
	 * Retrieves SmsMessage bean
	 * 
	 * @see uk.org.ponder.beanutil.BeanLocator#locateBean(java.lang.String)
	 */
	public Object locateBean(String name) {
		SmsMessage togo = delivered.get(name);
		if (togo == null) {
			if (name.startsWith(NEW_PREFIX)) {
				togo = smsMessageLogic.getNewTestSmsMessageInstance("", "");
			} else {
				// TODO: Retrieve it from database
			}
			delivered.put(name, togo);
		}
		return togo;
	}

	public void setSmsMessageLogic(SmsMessageLogic smsMessageLogic) {
		this.smsMessageLogic = smsMessageLogic;
	}
}
