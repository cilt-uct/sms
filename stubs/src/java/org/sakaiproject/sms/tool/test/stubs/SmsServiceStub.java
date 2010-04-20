/***********************************************************************************
 * SmsServiceStub.java
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.sakaiproject.sms.logic.exception.SmsTaskNotFoundException;
import org.sakaiproject.sms.logic.smpp.SmsService;
import org.sakaiproject.sms.logic.smpp.SmsTaskValidationException;
import org.sakaiproject.sms.model.SmsTask;

public class SmsServiceStub implements SmsService {

	public boolean calculateCalled = false;
	public boolean sufficientCredits = false;

	/**
	 * Sets boolean to check if method called
	 */
	public SmsTask calculateEstimatedGroupSize(SmsTask arg0) {
		arg0.setCreditEstimate(2);
		calculateCalled = true;
		return arg0;
	}

	public boolean checkSufficientCredits(String arg0, String arg1, int arg2) {
		return sufficientCredits;
	}

	public SmsTask getPreliminaryTask(Set<String> arg0, Date arg1, String arg2,
			String arg3, String arg4, String arg5) {
		// TODO Auto-generated method stub
		return null;
	}

	public SmsTask getPreliminaryTask(String arg0, Date arg1, String arg2,
			String arg3, String arg4, String arg5) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<String> validateTask(SmsTask arg0) {
		// TODO Auto-generated method stub
		return null;
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

	public boolean checkSufficientCredits(SmsTask smsTask) {
		return false;
	}

	public void abortPendingTask(Long smsTaskID)
			throws SmsTaskNotFoundException {
		// TODO Auto-generated method stub

	}

	public SmsTask insertTask(SmsTask smsTask)
			throws SmsTaskValidationException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean checkSufficientCredits(String sakaiSiteID,
			String sakaiUserID, double creditsRequired, boolean overDraftCheck) {
		// TODO Auto-generated method stub
		return false;
	}

	public String[] sendSmsToUserIds(String[] userIds, String fromId, String siteId,
			String toolId, String message) {
		return userIds;
	}

	public String[] sendSmsToMobileNumbers(String[] mobileNrs, String fromId,
			String siteId, String toolId, String message) {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] sendSms(String[] userIds, String fromId, String siteId,
			String toolId, String message) {
		// TODO Auto-generated method stub
		return null;
	}
}
