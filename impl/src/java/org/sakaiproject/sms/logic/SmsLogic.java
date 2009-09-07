/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007 The Sakai Foundation
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

import java.util.Calendar;
import java.util.Date;

public class SmsLogic extends BaseLogic {

	/**
	 * Gets the current Date.
	 * 
	 * @return the current Date
	 */
	protected Date getCurrentDate() {
		return new Date(System.currentTimeMillis());
	}

	/**
	 * Gets the current Date with an delay.
	 * 
	 * @return the current Date
	 */
	protected Date getDelayedCurrentDate(int secondsDelay) {
		final Calendar cal = Calendar.getInstance();

		cal.add(Calendar.SECOND, secondsDelay);

		return cal.getTime();
	}

}
