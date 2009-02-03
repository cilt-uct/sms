/***********************************************************************************
 * AbortTaskActionBean.java
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
package org.sakaiproject.sms.tool.beans;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.logic.hibernate.exception.SmsTaskNotFoundException;
import org.sakaiproject.sms.logic.smpp.SmsService;

public class AbortTaskActionBean {

	private static Log LOG = LogFactory.getLog(AbortTaskActionBean.class);

	private String taskToAbort;
	private SmsService smsService;

	public void setTaskToAbort(String taskToAbort) {
		this.taskToAbort = taskToAbort;
	}

	public void setSmsService(SmsService smsService) {
		this.smsService = smsService;
	}

	/**
	 * Abort specified task
	 * 
	 * @return {@link ActionResults}
	 */
	public String abortTask() {
		if (taskToAbort != null) {
			try {
				smsService.abortPendingTask(Long.parseLong(taskToAbort));
			} catch (SmsTaskNotFoundException e) {
				LOG.error(e);
			}
		}

		return ActionResults.SUCCESS;
	}
}
