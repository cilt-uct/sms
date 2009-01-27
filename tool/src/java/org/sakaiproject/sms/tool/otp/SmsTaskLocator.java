/***********************************************************************************
 * SmsTaskLocator.java
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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.sms.api.SmsCore;
import org.sakaiproject.sms.hibernate.model.SmsTask;
import org.sakaiproject.sms.hibernate.model.constants.SmsHibernateConstants;

import uk.org.ponder.beanutil.BeanLocator;

/**
 * Locator for {@link SmsTask}
 * 
 */
public class SmsTaskLocator implements BeanLocator {

	private SmsCore smsCore;

	/** The Constant LOCATOR_NAME. */
	public static final String LOCATOR_NAME = "SmsTaskLocator";

	public static final String NEW_PREFIX = "new ";

	/** The Constant NEW_1. */
	public static final String NEW_1 = NEW_PREFIX + "1";

	/** The delivered map (used to store beans). */
	private final Map<String, SmsTask> delivered = new HashMap<String, SmsTask>();

	/**
	 * Clear all beans on this locator
	 */
	public void clearBeans() {
		delivered.clear();
	}

	/**
	 * Checks if locator already contains NEW_1 entry
	 * 
	 * @return {@link Boolean}
	 */
	public boolean containsNew() {
		return delivered.containsKey(NEW_1);
	}

	public Object locateBean(String name) {
		SmsTask togo = delivered.get(name);
		if (togo == null) {
			if (name.startsWith(NEW_PREFIX)) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date());
				cal.add(Calendar.SECOND, 30);
				Date dateToProcess = cal.getTime();
				togo = smsCore
						.getPreliminaryTask(
								"deliveryGroup",
								dateToProcess,
								"",
								SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID,
								"",
								SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_USER_ID);
			} else {
				// TODO: Code to retrieve existing SmsTask
			}
			delivered.put(name, togo);
		}
		return togo;
	}

	public void setSmsCore(SmsCore smsCore) {
		this.smsCore = smsCore;
	}

}
