/***********************************************************************************
 * SmppAPITest.java
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
 * limitations under the License.s
 *
 **********************************************************************************/
package org.sakaiproject.sms.logic.smpp.test;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.sakaiproject.sms.logic.impl.hibernate.HibernateLogicFactory;
import org.sakaiproject.sms.logic.smpp.impl.SmsSmppImpl;
import org.sakaiproject.sms.model.hibernate.SmsMessage;
import org.sakaiproject.sms.model.hibernate.SmsTask;
import org.sakaiproject.sms.model.hibernate.constants.SmsConst_DeliveryStatus;
import org.sakaiproject.sms.model.hibernate.constants.SmsConst_SmscDeliveryStatus;
import org.sakaiproject.sms.model.hibernate.constants.SmsHibernateConstants;
import org.sakaiproject.sms.util.AbstractBaseTestCase;
import org.sakaiproject.sms.util.HibernateUtil;

/**
 * Test some api function on the smpp api. For example successful connect and
 * disconnect to the remote gateway. Both group and single message sending is
 * tested. It also waits for all the smmp delivery reports to come in and
 * verifies that all deliveries was successful.
 */
public class SmppAPITest extends AbstractBaseTestCase {

	private static SmsSmppImpl smsSmppImpl = null;

	static {
		System.out.println("Static setUp");
		HibernateUtil.createSchema();
		smsSmppImpl = new SmsSmppImpl();
		smsSmppImpl.init();
		smsSmppImpl.setLogLevel(Level.WARN);
	}

	public SmppAPITest() {
	}

	public SmppAPITest(String name) {
		super(name);
	}

	/**
	 * This is an helper method to insert a dummy smsTask into the Database. The
	 * sakaiID is used to identify the temp task.
	 * 
	 * @param sakaiID
	 * @param status
	 * @param dateToSend
	 * @param attemptCount
	 * @return
	 */
	public SmsTask insertNewTask(String sakaiID, String status,
			Date dateToSend, int attemptCount) {
		SmsTask insertTask = new SmsTask();
		insertTask.setSakaiSiteId(sakaiID);
		insertTask.setSmsAccountId(0l);
		insertTask.setDateCreated(new Date(System.currentTimeMillis()));
		insertTask.setDateToSend(dateToSend);
		insertTask.setStatusCode(status);
		insertTask.setAttemptCount(0);
		insertTask.setMessageBody("testing1234567");
		insertTask.setSenderUserName("administrator");
		insertTask.setMaxTimeToLive(300);
		insertTask.setDelReportTimeoutDuration(300);
		insertTask
				.setMessageTypeId(SmsHibernateConstants.SMS_TASK_TYPE_PROCESS_NOW);
		insertTask.setDateProcessed(new Date());
		insertTask
				.setMessageTypeId(SmsHibernateConstants.SMS_TASK_TYPE_PROCESS_NOW);
		HibernateLogicFactory.getTaskLogic().persistSmsTask(insertTask);
		return insertTask;
	}

	/*
	 * The tearDown method safely calls disconnectGateWay at the end of every
	 * test.
	 */
	@Override
	protected void tearDown() throws Exception {
		smsSmppImpl.disconnectGateWay();

	}

	/**
	 * Testing the connect and disconnecting from the smsc. The test succeeds if
	 * the correct connectionStatus is returned.
	 */

	public void testGetConnectionStatus() {

		smsSmppImpl.disconnectGateWay();
		assertEquals(true, (!smsSmppImpl.getConnectionStatus()));

		smsSmppImpl.connectToGateway();
		assertEquals(true, (smsSmppImpl.getConnectionStatus()));

	}

	/**
	 * The gateway return some information to us.
	 */
	public void testGetGatewayInfo() {
		smsSmppImpl.connectToGateway();
		System.out.println(smsSmppImpl.getGatewayInfo());
	}

	/**
	 * Test process outgoing message remotely.
	 */
	public void testProcessOutgoingMessageRemotely() {
		SmsTask task = new SmsTask();

		SmsMessage message = new SmsMessage();
		message.setSmsTask(task);
		message.setMobileNumber("0721998919");
		message.setMessageBody("This is message body text");
		boolean processed = smsSmppImpl.processOutgoingMessageRemotely(message);
		// Disabled
		assertFalse(processed);
	}

	/**
	 * Test to send 10 smsMessages to the SMSC The test succeeds if the returned
	 * status is STATUS_SENT and all the delivery reports are returned.
	 */
	public void testSendMessagesToGateway() {
		smsSmppImpl.connectToGateway();
		Set<SmsMessage> smsMessages = new HashSet<SmsMessage>();

		SmsTask insertTask = insertNewTask("testSendMessagesToGateway",
				SmsConst_DeliveryStatus.STATUS_PENDING, new Date(System
						.currentTimeMillis()), 0);

		assertTrue("Task for message not created", insertTask.exists());

		for (int i = 0; i < 10; i++) {
			SmsMessage message = new SmsMessage();
			message.setMobileNumber("072199891" + i);
			message.setSakaiUserId("sakaiUserId");
			message.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
			message.setSmsTask(insertTask);
			smsMessages.add(message);

		}
		insertTask.setSmsMessagesOnTask(smsMessages);
		// smsTaskLogicImpl.persistSmsTask(insertTask);
		assertEquals(true, smsSmppImpl.sendMessagesToGateway(smsMessages)
				.equals(SmsConst_DeliveryStatus.STATUS_SENT));
		try {
			Thread.sleep(25000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		SmsTask insertTask1update = HibernateLogicFactory.getTaskLogic()
				.getSmsTask(insertTask.getId());
		assertEquals(true, insertTask1update.getMessagesWithSmscStatus(
				SmsConst_SmscDeliveryStatus.ENROUTE).size() == 0);

	}

	/**
	 * Test to send an single smsMessage to the SMSC.The test succeeds if an
	 * SmscID is populated on the message and a delivery report is returned.
	 */

	public void testSendMessageToGateway() {
		smsSmppImpl.connectToGateway();
		SmsTask insertTask1 = insertNewTask("testSendMessageToGateway2",
				SmsConst_DeliveryStatus.STATUS_PENDING, new Date(System
						.currentTimeMillis()), 0);
		Set<SmsMessage> smsMessages = new HashSet<SmsMessage>();
		SmsMessage insertMessage1 = new SmsMessage();
		insertMessage1.setMobileNumber("0731998919");
		insertMessage1.setSakaiUserId("sakaiUserId");
		insertMessage1.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		insertMessage1.setSmsTask(insertTask1);
		smsMessages.add(insertMessage1);
		insertTask1.setSmsMessagesOnTask(smsMessages);
		// smsTaskLogicImpl.persistSmsTask(insertTask1);
		assertEquals(true, smsSmppImpl.sendMessageToGateway(insertMessage1)
				.getSmscMessageId() != null);
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		SmsTask insertTask1update = HibernateLogicFactory.getTaskLogic()
				.getSmsTask(insertTask1.getId());
		assertEquals(true, insertTask1update.getMessagesWithSmscStatus(
				SmsConst_SmscDeliveryStatus.ENROUTE).size() == 0);
		HibernateLogicFactory.getTaskLogic().deleteSmsTask(insertTask1update);

	}
}
