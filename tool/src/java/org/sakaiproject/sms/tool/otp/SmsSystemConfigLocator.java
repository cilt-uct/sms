/***********************************************************************************
 * SmsSystemConfigLocator.java
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


import org.apache.commons.math.util.MathUtils;
import org.sakaiproject.sms.logic.SmsConfigLogic;
import org.sakaiproject.sms.model.SmsConfig;


import uk.org.ponder.beanutil.BeanLocator;

public class SmsSystemConfigLocator implements BeanLocator {

	private SmsConfigLogic smsConfigLogic;

	public void setSmsConfigLogic(SmsConfigLogic smsConfigLogic) {
		this.smsConfigLogic = smsConfigLogic;
	}

	public static final String LOCATOR_NAME = "SmsSystemConfigLocator";

	public static final String NEW_PREFIX = "new ";

	public static final String NEW_1 = NEW_PREFIX + "1";

	private final Map<String, SmsConfig> delivered = new HashMap<String, SmsConfig>();

	public Object locateBean(String name) {
		SmsConfig togo = delivered.get(name);
		if (togo == null) {
			togo = smsConfigLogic.getOrCreateSystemSmsConfig();
			delivered.put(name, togo);
		}
		return togo;
	}

	public void save() {
		for (SmsConfig systemConfig : delivered.values()) {
			
			// round by 2 decimal places, if logic is placed in SMSConfig a
			// InvocationTargetException occurs
			Double rounded = MathUtils.round(systemConfig
					.getCreditCost(), 2);
			
			systemConfig.setCreditCost(rounded);
			smsConfigLogic.persistSmsConfig(systemConfig);
		}
	}

	
	
	
	
}
