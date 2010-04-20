/***********************************************************************************
 * SampleSmsTaskFactory.java
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
package org.sakaiproject.sms.logic.hibernate.test.dataload;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.sms.model.SmsTask;
import org.sakaiproject.sms.model.constants.SmsConst_DeliveryStatus;

public class SampleSmsTaskFactory implements Listable {
	private static int theYear = 2009;

	List<SmsTask> smsTasks;
	RandomUtils randomUtils = new RandomUtils();

	public SampleSmsTaskFactory() {
		createSampleSmsTasks();
	}

	private void createSampleSmsTasks() {
		smsTasks = new ArrayList<SmsTask>();

		/*
		 * SmsTask task1 = new SmsTask("3", "SC", "CHEM100-05", 123456,
		 * "Test date moved form 12 Jan to 15 Jan");
		 */
		SmsTask task1 = new SmsTask();

		task1.setSakaiSiteId("3");
		task1.setDeliveryUserId("SC");
		task1.setDeliveryGroupId("CHEM100-05");
		task1.setSmsAccountId(123456l);
		task1.setMessageBody("Test date moved form 12 Jan to 15 Jan");
		task1 = setTaskProperties(task1);
		task1.setDateCreated(randomUtils.getBoundRandomDate(theYear));
		task1.setDateToSend(randomUtils.getBoundRandomDate(theYear));
		task1.setSenderUserName("Prof Blue");
		task1.setStatusCode(SmsConst_DeliveryStatus.STATUS_RETRY);
		smsTasks.add(task1);

		/*
		 * SmsTask task2 = new SmsTask("56", "GM", "EEE475-05", 123457,
		 * "Matlab tutorial move to Science labs D");
		 */
		SmsTask task2 = new SmsTask();
		task2.setSakaiSiteId("56");
		task2.setDeliveryUserId("GM");
		task2.setDeliveryGroupId("EEE475-05");
		task2.setSmsAccountId(123457l);
		task2.setMessageBody("Matlab tutorial move to Science labs D");
		task2 = setTaskProperties(task2);
		task2.setDateCreated(randomUtils.getBoundRandomDate(theYear));
		task2.setDateToSend(randomUtils.getBoundRandomDate(theYear));
		task2.setSenderUserName("Prof Green");
		task2.setStatusCode(SmsConst_DeliveryStatus.STATUS_SENT);
		smsTasks.add(task2);

		/*
		 * SmsTask task3 = new SmsTask("32", "RD", "MAM100-05", 123458,
		 * "Location of tut changed to Science Block");
		 */
		SmsTask task3 = new SmsTask();
		task3.setSakaiSiteId("32");
		task3.setDeliveryUserId("RD");
		task3.setDeliveryGroupId("MAM100-05");
		task3.setSmsAccountId(123458l);
		task3.setMessageBody("Location of tut changed to Science Block");
		task3 = setTaskProperties(task3);
		task3.setDateCreated(randomUtils.getBoundRandomDate(theYear));
		task3.setDateToSend(randomUtils.getBoundRandomDate(theYear));
		task3.setSenderUserName("Prof Red");
		task3.setStatusCode(SmsConst_DeliveryStatus.STATUS_DELIVERED);
		smsTasks.add(task3);

		/*
		 * SmsTask task4 = new SmsTask("67", "EO", "PHY131-05", 123459,
		 * "Problem set to be handed in by 15 Jan");
		 */
		SmsTask task4 = new SmsTask();
		task4.setSakaiSiteId("67");
		task4.setDeliveryUserId("EO");
		task4.setDeliveryGroupId("PHY131-05");
		task4.setSmsAccountId(123459l);
		task4.setMessageBody("Problem set to be handed in by 15 Jan");
		task4 = setTaskProperties(task4);
		task4.setDateCreated(randomUtils.getBoundRandomDate(theYear));
		task4.setDateToSend(randomUtils.getBoundRandomDate(theYear));
		task4.setSenderUserName("Prof Lime");
		task4.setStatusCode(SmsConst_DeliveryStatus.STATUS_FAIL);
		smsTasks.add(task4);

		/*
		 * SmsTask task5 = new SmsTask("42", "FQ", "BUS100-05", 123460,
		 * "No tutorial required this month");
		 */
		SmsTask task5 = new SmsTask();
		task5.setSakaiSiteId("42");
		task5.setDeliveryUserId("FQ");
		task5.setDeliveryGroupId("BUS100-05");
		task5.setSmsAccountId(123460l);
		task5.setMessageBody("No tutorial required this month");
		task5 = setTaskProperties(task5);
		task5.setDateCreated(randomUtils.getBoundRandomDate(theYear));
		task5.setDateToSend(randomUtils.getBoundRandomDate(theYear));
		task5.setSenderUserName("Prof Orange");
		task5.setStatusCode(SmsConst_DeliveryStatus.STATUS_INCOMPLETE);
		smsTasks.add(task5);

	}

	public List<SmsTask> getAllTestSmsTasks() {
		return smsTasks;
	}

	public Object getElementAt(int i) {
		return getTestSmsTask(i);
	}

	public SmsTask getTestSmsTask(int index) {

		if (index >= smsTasks.size())
			throw new RuntimeException("The specified index is too high");

		return smsTasks.get(index);
	}

	public int getTotalSmsTasks() {
		return smsTasks.size();
	}

	public void refreshList() {
		createSampleSmsTasks();

	}

	private SmsTask setTaskProperties(SmsTask task) {
		task.setGroupSizeEstimate((int) (Math.random() * 1000 + 10));
		task.setGroupSizeActual((int) (Math.random() * 1000 + 10));
		// task.setDateToSend(randomUtils.getBoundRandomDate(theYear));
		task.setDateProcessed(randomUtils.getBoundRandomDate(theYear));
		task.setSakaiToolName("Sakai tool");
		task.setMaxTimeToLive(1000);
		task.setAttemptCount(0);
		return task;
	}

}
