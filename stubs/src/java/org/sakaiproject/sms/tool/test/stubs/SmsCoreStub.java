/***********************************************************************************
 * SmsCoreStub.java
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
package org.sakaiproject.sms.tool.test.stubs;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.sakaiproject.sms.logic.exception.SmsTaskNotFoundException;
import org.sakaiproject.sms.logic.smpp.SmsCore;
import org.sakaiproject.sms.model.SmsMOMessage;
import org.sakaiproject.sms.model.SmsTask;

public class SmsCoreStub implements SmsCore {

	public boolean insertTaskCalled = false;

	public SmsTask calculateEstimatedGroupSize(SmsTask arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	public Set generateSmsMessages(SmsTask arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public SmsTask getNextSmsTask() {
		// TODO Auto-generated method stub
		return null;
	}

	public SmsTask getPreliminaryTask(Set<String> arg0, Date arg1, String arg2,
			String arg3, String arg4, String arg5) {
		return new SmsTask();
	}

	public SmsTask getPreliminaryTask(String arg0, Date arg1, String arg2,
			String arg3, String arg4, String arg5) {
		return new SmsTask();
	}

	public String getSakaiMobileNumber(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public SmsTask insertTask(SmsTask arg0) {
		insertTaskCalled = true;
		return arg0;
	}

	public void processNextTask() {
		// TODO Auto-generated method stub

	}

	public void processTask(SmsTask arg0) {
		// TODO Auto-generated method stub

	}

	public void processTimedOutDeliveryReports() {
		// TODO Auto-generated method stub

	}

	public void processVeryLateDeliveryReports() {
		// TODO Auto-generated method stub

	}

	public boolean sendNotificationEmail(SmsTask smsTask, String arg0,
			String arg1, String arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	public void tryProcessTaskRealTime(SmsTask arg0) {
		// TODO Auto-generated method stub

	}

	public SmsTask getPreliminaryTask(Date arg0, String arg1, String arg2,
			String arg3, String arg4, Set<String> arg5) {
		// TODO Auto-generated method stub
		return null;
	}

	public SmsTask getPreliminaryTask(Date arg0, String arg1, String arg2,
			String arg3, String arg4, List<String> arg5) {
		// TODO Auto-generated method stub
		return null;
	}

	public void checkAndSetTasksCompleted() {
		// TODO Auto-generated method stub

	}

	public void abortPendingTask(Long smsTaskID)
			throws SmsTaskNotFoundException {
		// TODO Auto-generated method stub

	}

	public void processMOTasks() {
		// TODO Auto-generated method stub

	}

	public void processTaskInThread(SmsTask smsTask, ThreadGroup threadGroup) {
		// TODO Auto-generated method stub

	}

	public void adjustLateDeliveryBilling() {
		// TODO Auto-generated method stub
		
	}

	public void processIncomingMessage(SmsMOMessage message) {
		// TODO Auto-generated method stub
		
	}

	public void processSOTasks() {
		// TODO Auto-generated method stub
		
	}

	public void updateExternalMessageStatuses() {
		// TODO Auto-generated method stub
		
	}



}
