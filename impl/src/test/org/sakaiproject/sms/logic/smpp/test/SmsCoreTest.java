/***********************************************************************************
 * SmsCoreTest.java
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

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import org.apache.log4j.Level;
import org.sakaiproject.sms.logic.hibernate.exception.SmsTaskNotFoundException;
import org.sakaiproject.sms.logic.impl.hibernate.HibernateLogicFactory;
import org.sakaiproject.sms.logic.smpp.SmsTaskValidationException;
import org.sakaiproject.sms.logic.smpp.impl.SmsBillingImpl;
import org.sakaiproject.sms.logic.smpp.impl.SmsCoreImpl;
import org.sakaiproject.sms.logic.smpp.impl.SmsSmppImpl;
import org.sakaiproject.sms.model.hibernate.SmsAccount;
import org.sakaiproject.sms.model.hibernate.SmsMessage;
import org.sakaiproject.sms.model.hibernate.SmsTask;
import org.sakaiproject.sms.model.hibernate.constants.SmsConst_DeliveryStatus;
import org.sakaiproject.sms.model.hibernate.constants.SmsConst_SmscDeliveryStatus;
import org.sakaiproject.sms.model.hibernate.constants.SmsHibernateConstants;
import org.sakaiproject.sms.util.AbstractBaseTestCase;
import org.sakaiproject.sms.util.DateUtil;
import org.sakaiproject.sms.util.HibernateUtil;

/**
 * This test also send messages to the smpp simulator but it check the specific
 * statuses of sent messages. It also test the retrieval of the next sms task
 * from the SMS_TASK table.
 *
 * @author etienne@psybergate.co.za
 *
 */

public class SmsCoreTest extends AbstractBaseTestCase {

	static SmsSmppImpl smsSmppImpl = null;
	static SmsCoreImpl smsCoreImpl = null;
	static SmsAccount smsAccount = null;
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(SmsCoreTest.class);

	static {
		smsCoreImpl = new SmsCoreImpl();
		smsSmppImpl = new SmsSmppImpl();
		smsCoreImpl.setSmsBilling(new SmsBillingImpl());
		smsSmppImpl.init();
		smsSmppImpl.setLogLevel(Level.WARN);
		smsCoreImpl.setSmsSmpp(smsSmppImpl);
		smsCoreImpl.setLoggingLevel(Level.WARN);
		LOG.setLevel(Level.WARN);
		smsAccount = new SmsAccount();
		smsAccount
				.setSakaiUserId(SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_USER_ID);
		smsAccount
				.setSakaiSiteId(SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID);
		smsAccount.setMessageTypeCode("3");
		smsAccount.setOverdraftLimit(10000.00f);
		smsAccount.setBalance(1000f);
		smsAccount.setAccountName("accountname");
		smsAccount.setAccountEnabled(true);

	}

	public SmsCoreTest() {
	}

	public SmsCoreTest(String name) {
		super(name);
	}

	/**
	 * The tearDown method safely calls disconnectGateWay at the end of every
	 * test.
	 */
	@Override
	protected void tearDown() throws Exception {
		smsSmppImpl.disconnectGateWay();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.sms.util.AbstractBaseTestCase#testOnetimeSetup()
	 */
	public void testOnetimeSetup() {
		HibernateUtil.setTestConfiguration(true);
		HibernateUtil.createSchema();
		HibernateLogicFactory.getAccountLogic().persistSmsAccount(smsAccount);
	}

	/**
	 * In this test the ProcessNextTask method is tested. 4 smsTasks are created
	 * with different sending times and statuses. The ProcessNextTask method
	 * must pick up the oldest SmsTask with an (pending/incomplete/reply)
	 * status. The test succeeds if the Smstasks are returned in the proper
	 * order and the correct amount of delivery reports were received.
	 *
	 * NOTE: Make sure that the SMS_TASK table is empty before running this
	 * test, else it will fail.
	 */
	public void testProcessNextTask() {
		smsSmppImpl.connectToGateway();

		if (smsCoreImpl.getSmsSmpp().getConnectionStatus()) {

			Calendar now = Calendar.getInstance();
			SmsTask smsTask3 = smsCoreImpl.getPreliminaryTask(
					"testProcessNextTask-smsTask3", new Date(now
							.getTimeInMillis()),
					"testProcessNextTask-smsTask3",
					SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID, null,
					SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_USER_ID);

			now.add(Calendar.MINUTE, -1);
			SmsTask smsTask2 = smsCoreImpl.getPreliminaryTask(
					"testProcessNextTask-smsTask2", new Date(now
							.getTimeInMillis()),
					"testProcessNextTask-smsTask2MessageBody",
					SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID, null,
					SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_USER_ID);

			smsTask2.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);

			now.add(Calendar.MINUTE, -3);
			SmsTask smsTask1 = smsCoreImpl.getPreliminaryTask(
					"testProcessNextTask-smsTask1", new Date(now
							.getTimeInMillis()),
					"testProcessNextTask-smsTask1MessageBody",
					SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID, null,
					SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_USER_ID);

			smsTask1.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);

			now.add(Calendar.MINUTE, 60);
			SmsTask smsTask4 = smsCoreImpl.getPreliminaryTask(
					"testProcessNextTask-smsTask4", new Date(now
							.getTimeInMillis()),
					"testProcessNextTask-smsTask4MessageBody",
					SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID, null,
					SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_USER_ID);

			smsTask4.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);

			smsTask1.setDateCreated(DateUtil.getCurrentDate());
			smsTask1.setSmsAccountId(smsAccount.getId());
			HibernateLogicFactory.getTaskLogic().persistSmsTask(smsTask1);
			smsTask2.setDateCreated(DateUtil.getCurrentDate());
			smsTask2.setSmsAccountId(smsAccount.getId());
			HibernateLogicFactory.getTaskLogic().persistSmsTask(smsTask2);
			smsTask3.setDateCreated(DateUtil.getCurrentDate());
			smsTask3.setSmsAccountId(smsAccount.getId());
			HibernateLogicFactory.getTaskLogic().persistSmsTask(smsTask3);
			smsTask4.setDateCreated(DateUtil.getCurrentDate());
			smsTask4.setSmsAccountId(smsAccount.getId());
			HibernateLogicFactory.getTaskLogic().persistSmsTask(smsTask4);

			assertEquals(true, smsTask1.getId().equals(
					smsCoreImpl.getNextSmsTask().getId()));
			smsCoreImpl.processNextTask();
			assertEquals(true, smsTask2.getId().equals(
					smsCoreImpl.getNextSmsTask().getId()));
			smsCoreImpl.processNextTask();
			assertEquals(true, smsTask3.getId().equals(
					smsCoreImpl.getNextSmsTask().getId()));
			smsCoreImpl.processNextTask();
			assertEquals(true, smsCoreImpl.getNextSmsTask() == (null));

			// we give the delivery reports time to get back.
			try {
				Thread.sleep(30000);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			SmsTask smsTask1Update = HibernateLogicFactory.getTaskLogic()
					.getSmsTask(smsTask1.getId());
			SmsTask smsTask2Update = HibernateLogicFactory.getTaskLogic()
					.getSmsTask(smsTask2.getId());
			SmsTask smsTask3Update = HibernateLogicFactory.getTaskLogic()
					.getSmsTask(smsTask3.getId());
			SmsTask smsTask4Update = HibernateLogicFactory.getTaskLogic()
					.getSmsTask(smsTask4.getId());

			assertEquals(true, smsTask1Update.getMessagesWithSmscStatus(
					SmsConst_SmscDeliveryStatus.ENROUTE).size() == 0);
			assertEquals(true, smsTask1Update.getMessagesWithStatus(
					SmsConst_DeliveryStatus.STATUS_PENDING).size() == 0);
			assertEquals(true, smsTask2Update.getMessagesWithSmscStatus(
					SmsConst_SmscDeliveryStatus.ENROUTE).size() == 0);
			assertEquals(true, smsTask2Update.getMessagesWithStatus(
					SmsConst_DeliveryStatus.STATUS_PENDING).size() == 0);
			assertEquals(true, smsTask3Update.getMessagesWithSmscStatus(
					SmsConst_SmscDeliveryStatus.ENROUTE).size() == 0);
			assertEquals(true, smsTask3Update.getMessagesWithStatus(
					SmsConst_DeliveryStatus.STATUS_PENDING).size() == 0);

		}
	}

	/**
	 * In this test the updating of the tasks statusses are tested.
	 */
	public void testTaskStatuses() {
		smsSmppImpl.connectToGateway();

		if (smsCoreImpl.getSmsSmpp().getConnectionStatus()) {

			Calendar now = Calendar.getInstance();
			now.add(Calendar.MINUTE, -1);
			SmsTask smsTask2 = smsCoreImpl.getPreliminaryTask(
					"TestTaskStatuses-SusscessFullTask", new Date(now
							.getTimeInMillis()),
					"TestTaskStatuses-SmsTask2MessageBody",
					SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID, null,
					SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_USER_ID);

			smsTask2.setMaxTimeToLive(300);
			now.add(Calendar.MINUTE, -3);
			SmsTask smsTask1 = smsCoreImpl.getPreliminaryTask(
					"TestTaskStatuses-ExpiresTask", new Date(now
							.getTimeInMillis()),
					"TestTaskStatuses-ExpiresTask",
					SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID, null,
					SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_USER_ID);
			smsTask1.setMaxTimeToLive(60);
			smsTask1.setSmsAccountId(smsAccount.getId());
			smsTask1.setDateCreated(DateUtil.getCurrentDate());
			HibernateLogicFactory.getTaskLogic().persistSmsTask(smsTask1);
			smsTask2.setDateCreated(DateUtil.getCurrentDate());
			smsTask2.setSmsAccountId(smsAccount.getId());
			HibernateLogicFactory.getTaskLogic().persistSmsTask(smsTask2);

			assertEquals(true, smsTask1.getId().equals(
					smsCoreImpl.getNextSmsTask().getId()));
			smsCoreImpl.processNextTask();
			assertEquals(true, smsTask2.getId().equals(
					smsCoreImpl.getNextSmsTask().getId()));
			smsCoreImpl.processNextTask();

			// we give the delivery reports time to get back.
			try {
				Thread.sleep(15000);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			SmsTask smsTask1Update = HibernateLogicFactory.getTaskLogic()
					.getSmsTask(smsTask1.getId());
			SmsTask smsTask2Update = HibernateLogicFactory.getTaskLogic()
					.getSmsTask(smsTask2.getId());

			assertEquals(true, smsTask1Update.getMessagesWithSmscStatus(
					SmsConst_SmscDeliveryStatus.ENROUTE).size() == 0);
			assertEquals(smsTask1Update.getStatusCode().equals(
					SmsConst_DeliveryStatus.STATUS_EXPIRE), true);

			assertEquals(smsTask2Update.getStatusCode().equals(
					SmsConst_DeliveryStatus.STATUS_SENT), true);
			assertEquals(true, smsTask2Update.getMessagesWithSmscStatus(
					SmsConst_SmscDeliveryStatus.ENROUTE).size() == 0);
			assertEquals(true, smsTask2Update.getMessagesWithStatus(
					SmsConst_DeliveryStatus.STATUS_PENDING).size() == 0);

		}
	}

	/**
	 * In this test the smsc (gateway) is not bound (disconnected). The task is
	 * executed 5 times to simulate the scheduler retrying and eventually
	 * failing.
	 */
	public void testProcessTaskFail() {
		smsSmppImpl.connectToGateway();
		SmsTask smsTask = smsCoreImpl.getPreliminaryTask("testProcessTaskFail",
				new Date(System.currentTimeMillis()),
				"testProcessTaskFailMessageBody",
				SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID, null,
				SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_USER_ID);

		smsTask.setDateCreated(DateUtil.getCurrentDate());
		smsTask.setSmsAccountId(smsAccount.getId());

		HibernateLogicFactory.getTaskLogic().persistSmsTask(smsTask);

		smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		smsTask.setAttemptCount(0);
		smsSmppImpl.setLogLevel(Level.OFF);
		LOG.info("Disconnecting from server for fail test ");
		smsSmppImpl.disconnectGateWay();
		for (int i = 0; i < 5; i++) {
			smsCoreImpl.processTask(smsTask);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		SmsTask smsTaskUpdate = HibernateLogicFactory.getTaskLogic()
				.getSmsTask(smsTask.getId());
		assertEquals(true, smsTaskUpdate.getStatusCode().equals(
				SmsConst_DeliveryStatus.STATUS_FAIL));
		assertEquals(true, smsTaskUpdate.getAttemptCount() == 5);
		assertEquals(true, smsTaskUpdate.getMessagesWithStatus(
				SmsConst_DeliveryStatus.STATUS_FAIL).size() == (smsTask
				.getSmsMessages().size()));

		HibernateLogicFactory.getTaskLogic().deleteSmsTask(smsTask);
		LOG.info("Reconnecting to server after fail test ");
		smsSmppImpl.connectToGateway();
	}

	public void testProcessIncomingMessage() {
		smsSmppImpl.connectToGateway();
		SmsMessage smsMessage = HibernateLogicFactory.getMessageLogic()
				.getNewTestSmsMessageInstance("Mobile number", "Message body");
		smsCoreImpl.processIncomingMessage(smsMessage);
	}

	/**
	 * In this test the updating of smsStatuses is tested. First a new task is
	 * created and populated with smsMessages.The total number of pending
	 * messages must equal 0 at the end.The total sent messages must equal the
	 * total messages on the task.Secondly a new task is created and the
	 * delivery report lister is switched off. After 1 min the core service must
	 * mark all the messages on the task as timedout.The test is successful if a
	 * timedout message is found.
	 */
	public void testTimeoutAndMessageStatusUpdate() {
		smsSmppImpl.connectToGateway();
		smsSmppImpl.setLogLevel(Level.ALL);
		smsSmppImpl.getSession().setMessageReceiverListener(null);
		SmsTask statusUpdateTask = smsCoreImpl.getPreliminaryTask(
				"TestTimeoutAndMessageStatusUpdate-StatusUpdateTask", new Date(
						System.currentTimeMillis()),
				"TestTimeoutAndMessageStatusUpdate-StatusUpdateTask",
				SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID, null,
				SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_USER_ID);
		statusUpdateTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		statusUpdateTask.setAttemptCount(0);
		statusUpdateTask.setDateProcessed(new Date());
		statusUpdateTask.setSmsMessagesOnTask(smsCoreImpl
				.generateSmsMessages(statusUpdateTask));
		statusUpdateTask.setSmsAccountId(smsAccount.getId());
		statusUpdateTask
				.setMessageTypeId(SmsHibernateConstants.SMS_TASK_TYPE_PROCESS_NOW);
		smsCoreImpl.calculateEstimatedGroupSize(statusUpdateTask);
		try {
			smsCoreImpl.insertTask(statusUpdateTask);
		} catch (SmsTaskValidationException e1) {
			fail(e1.getErrorMessagesAsBlock());
		}
		smsSmppImpl.sendMessagesToGateway(statusUpdateTask.getSmsMessages());

		assertEquals(true, statusUpdateTask.getMessagesWithStatus(
				SmsConst_DeliveryStatus.STATUS_PENDING).size() == 0);

		SmsTask timeOutTask = smsCoreImpl.getPreliminaryTask(
				"testTimeoutAndMessageStatusUpdate-TIMEOUT", new Date(),
				"testTimeoutAndMessageStatusUpdate-TIMEOUT",
				SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID, null,
				SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_USER_ID);
		timeOutTask.setDelReportTimeoutDuration(60);
		timeOutTask.setSmsMessagesOnTask(smsCoreImpl
				.generateSmsMessages(timeOutTask));
		timeOutTask.setSmsAccountId(smsAccount.getId());
		smsCoreImpl.calculateEstimatedGroupSize(timeOutTask);
		try {
			smsCoreImpl.insertTask(timeOutTask);
		} catch (SmsTaskValidationException e1) {
			fail(e1.getErrorMessagesAsBlock());
		}
		smsCoreImpl.processNextTask();

		try {
			Thread.sleep(60000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		smsCoreImpl.processTimedOutDeliveryReports();
		SmsTask smsTask3Update = HibernateLogicFactory.getTaskLogic()
				.getSmsTask(timeOutTask.getId());

		Set<SmsMessage> smsMessages = smsTask3Update.getSmsMessages();
		boolean timedOutMessagesFound = false;
		for (SmsMessage message : smsMessages) {
			if (message.getStatusCode().equals(
					SmsConst_DeliveryStatus.STATUS_TIMEOUT)) {
				timedOutMessagesFound = true;
				break;
			}

		}
		assertEquals(timedOutMessagesFound, true);

	}

	public void testVeryLateDeliveryReports() {
		SmsTask insertTask = new SmsTask();
		insertTask
				.setSakaiSiteId(SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID);
		insertTask
				.setSenderUserName(SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_USER_ID);
		insertTask
				.setSakaiToolId(SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_TOOL_ID);
		insertTask.setSmsAccountId(smsAccount.getId());
		insertTask.setDateCreated(new Date(System.currentTimeMillis()));
		insertTask.setDateToSend(new Date(System.currentTimeMillis()));
		insertTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		insertTask.setAttemptCount(2);
		insertTask.setMessageBody("messageBody");
		insertTask.setSenderUserName("senderUserName");
		insertTask.setMaxTimeToLive(60);
		insertTask.setDelReportTimeoutDuration(60);
		smsCoreImpl.calculateEstimatedGroupSize(insertTask);

		HibernateLogicFactory.getTaskLogic().persistSmsTask(insertTask);

		SmsMessage insertMessage1 = new SmsMessage();
		insertMessage1.setMobileNumber("0721998919");
		insertMessage1.setSmscMessageId("smscMessageId1");
		insertMessage1.setSmscId(SmsHibernateConstants.SMSC_ID);
		insertMessage1.setSakaiUserId("sakaiUserId");
		insertMessage1.setStatusCode(SmsConst_DeliveryStatus.STATUS_LATE);
		insertMessage1
				.setSmscDeliveryStatusCode(SmsConst_SmscDeliveryStatus.DELIVERED);
		insertMessage1.setSmsTask(insertTask);

		HibernateLogicFactory.getMessageLogic().persistSmsMessage(
				insertMessage1);

		SmsMessage insertMessage2 = new SmsMessage();
		insertMessage2.setMobileNumber("0823450983");
		insertMessage2.setSmscMessageId("smscMessageId2");
		insertMessage2.setSmscId(SmsHibernateConstants.SMSC_ID);
		insertMessage2.setSakaiUserId("sakaiUserId");
		insertMessage2.setStatusCode(SmsConst_DeliveryStatus.STATUS_LATE);
		insertMessage2
				.setSmscDeliveryStatusCode(SmsConst_SmscDeliveryStatus.REJECTED);
		insertMessage2.setSmsTask(insertTask);

		HibernateLogicFactory.getMessageLogic().persistSmsMessage(
				insertMessage2);

		smsCoreImpl.processVeryLateDeliveryReports();

		SmsMessage insertMessage2Update = HibernateLogicFactory
				.getMessageLogic().getSmsMessage(insertMessage2.getId());

		assertEquals(insertMessage2Update.getStatusCode().equals(
				SmsConst_DeliveryStatus.STATUS_FAIL), true);

		SmsMessage insertMessage1Update = HibernateLogicFactory
				.getMessageLogic().getSmsMessage(insertMessage1.getId());

		assertEquals(insertMessage1Update.getStatusCode().equals(
				SmsConst_DeliveryStatus.STATUS_DELIVERED), true);

	}

	/**
	 * Test insert task for validation errors.
	 */
	public void testInsertTask_ValidationErrors() {
		SmsAccount account = new SmsAccount();
		account.setSakaiSiteId("sakSitId");
		account.setMessageTypeCode("12345");
		account.setOverdraftLimit(0f);
		account.setBalance(0f);
		account.setAccountName("accountName");
		account.setAccountEnabled(true);
		HibernateLogicFactory.getAccountLogic().persistSmsAccount(account);

		SmsTask insertTask = new SmsTask();
		insertTask
				.setSakaiSiteId(SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID);
		insertTask
				.setSenderUserName(SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_USER_ID);
		insertTask
				.setSakaiToolId(SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_TOOL_ID);
		insertTask.setSmsAccountId(account.getId());
		insertTask.setDateCreated(new Date(System.currentTimeMillis()));
		insertTask.setDateToSend(new Date(System.currentTimeMillis()));
		insertTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		insertTask.setAttemptCount(2);
		insertTask.setMessageBody("messageBody");
		insertTask.setSenderUserName("senderUserName");
		insertTask.setMaxTimeToLive(60);
		insertTask.setDelReportTimeoutDuration(60);

		try {
			smsCoreImpl.insertTask(insertTask);
			fail("Excpected validation exception");
		} catch (SmsTaskValidationException e1) {
			assertTrue(e1.getErrorMessages().size() > 0);
			LOG.debug(e1.getErrorMessagesAsBlock());
		}
	}

	/**
	 * Test insert task for validation errors.
	 */
	public void testInsertTask_InsuficientCredit() {
		SmsAccount account = new SmsAccount();
		account.setSakaiSiteId("1");
		account.setMessageTypeCode("1");
		account.setOverdraftLimit(10000.00f);
		account.setBalance(5000.00f);
		account.setAccountName("accountName");
		account.setAccountEnabled(true);
		HibernateLogicFactory.getAccountLogic().persistSmsAccount(account);

		SmsTask insertTask = new SmsTask();
		insertTask
				.setSakaiSiteId(SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID);
		insertTask
				.setSenderUserName(SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_USER_ID);
		insertTask
				.setSakaiToolId(SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_TOOL_ID);
		insertTask.setSmsAccountId(account.getId());
		insertTask.setDateCreated(new Date(System.currentTimeMillis()));
		insertTask.setDateToSend(new Date(System.currentTimeMillis()));
		insertTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		insertTask.setAttemptCount(2);
		insertTask.setMessageBody("messageBody");
		insertTask.setSenderUserName("senderUserName");
		insertTask.setMaxTimeToLive(60);
		insertTask.setDelReportTimeoutDuration(60);
		insertTask.setDeliveryGroupId("delgrpid");

		try {
			smsCoreImpl.insertTask(insertTask);
			fail("Excpected validation exception");
		} catch (SmsTaskValidationException e1) {
			assertTrue(e1.getErrorMessages().size() > 0);
			assertTrue(e1.getMessage().equals(
					SmsHibernateConstants.INSUFFICIENT_CREDIT_MESSAGE));
			LOG.debug(e1.getErrorMessagesAsBlock());
		}
	}

	/**
	 * Tests the aborting of a task
	 */
	public void testAbortTask() {

		SmsTask insertTask = new SmsTask();
		insertTask
				.setSakaiSiteId(SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID);
		insertTask
				.setSenderUserName(SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_USER_ID);
		insertTask
				.setSakaiToolId(SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_TOOL_ID);
		insertTask.setSmsAccountId(smsAccount.getId());
		insertTask.setDateCreated(new Date(System.currentTimeMillis()));
		insertTask.setDateToSend(new Date(System.currentTimeMillis()));
		insertTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		insertTask.setAttemptCount(2);
		insertTask.setMessageBody("messageBody");
		insertTask.setSenderUserName("senderUserName");
		insertTask.setMaxTimeToLive(60);
		insertTask.setDelReportTimeoutDuration(60);
		smsCoreImpl.calculateEstimatedGroupSize(insertTask);
		HibernateLogicFactory.getTaskLogic().persistSmsTask(insertTask);

		try {
			smsCoreImpl.abortPendingTask(insertTask.getId());
		} catch (SmsTaskNotFoundException e) {

			e.printStackTrace();
		}
		SmsTask insertTaskUpdate = HibernateLogicFactory.getTaskLogic()
				.getSmsTask(insertTask.getId());

		assertEquals(insertTaskUpdate.getStatusCode(),
				SmsConst_DeliveryStatus.STATUS_ABORT);

		for (SmsMessage smsMessage : insertTaskUpdate.getSmsMessages()) {

			assertEquals(smsMessage.getStatusCode(),
					SmsConst_DeliveryStatus.STATUS_ABORT);
		}

	}
}
