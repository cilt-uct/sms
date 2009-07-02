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

package org.sakaiproject.sms.logic.smpp;


/**
 * Use Quartz to run at predefined intervals to process the SMS task queue. The
 * service can run in a concurrent environment.
 * 
 * @author louis@psybergate.com
 * @version 1.0
 * @created 12-Nov-2008
 */
public interface SmsScheduler {

	/**
	 * Starts the SmsScheduler.It is started by default.
	 */
	public void startSmsScheduler();

	/**
	 * Stops the SmsScheduler.
	 */
	public void stopSmsScheduler();

	/**
	 * Set the interval of the scheduler.
	 */
	public void setInterval(int seconds);
}