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
import java.util.TreeSet;

import org.apache.log4j.Level;
import org.sakaiproject.sms.dao.StandaloneSmsDaoImpl;
import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.hibernate.HibernateLogicLocator;
import org.sakaiproject.sms.logic.hibernate.exception.SmsTaskNotFoundException;
import org.sakaiproject.sms.logic.smpp.SmsTaskValidationException;
import org.sakaiproject.sms.logic.smpp.exception.ReceiveIncomingSmsDisabledException;
import org.sakaiproject.sms.logic.smpp.exception.SmsSendDeniedException;
import org.sakaiproject.sms.logic.smpp.exception.SmsSendDisabledException;
import org.sakaiproject.sms.logic.smpp.impl.SmsBillingImpl;
import org.sakaiproject.sms.logic.smpp.impl.SmsCoreImpl;
import org.sakaiproject.sms.logic.smpp.impl.SmsSmppImpl;
import org.sakaiproject.sms.logic.smpp.validate.SmsTaskValidatorImpl;
import org.sakaiproject.sms.logic.stubs.ExternalLogicStub;
import org.sakaiproject.sms.model.hibernate.SmsAccount;
import org.sakaiproject.sms.model.hibernate.SmsConfig;
import org.sakaiproject.sms.model.hibernate.SmsMessage;
import org.sakaiproject.sms.model.hibernate.SmsTask;
import org.sakaiproject.sms.model.hibernate.constants.SmsConst_DeliveryStatus;
import org.sakaiproject.sms.model.hibernate.constants.SmsConst_SmscDeliveryStatus;
import org.sakaiproject.sms.model.hibernate.constants.SmsHibernateConstants;
import org.sakaiproject.sms.util.AbstractBaseTestCase;
import org.sakaiproject.sms.util.DateUtil;

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
	static ExternalLogic externalLogic = null;
	static SmsBillingImpl smsBillingImpl = new SmsBillingImpl();
	static SmsConfig SmsConfigImpl = new SmsConfig();

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(SmsCoreTest.class);

	static {
		hibernateLogicLocator.setExternalLogic(new ExternalLogicStub());

		externalLogic = hibernateLogicLocator.getExternalLogic();
		smsCoreImpl = new SmsCoreImpl();
		smsSmppImpl = new SmsSmppImpl();
		smsBillingImpl.setHibernateLogicLocator(hibernateLogicLocator);

		SmsTaskValidatorImpl smsTaskValidator = new SmsTaskValidatorImpl();
		smsTaskValidator.setSmsBilling(smsBillingImpl);
		smsCoreImpl.setSmsTaskValidator(smsTaskValidator);
		smsCoreImpl.setSmsBilling(smsBillingImpl);
		smsSmppImpl.setHibernateLogicLocator(hibernateLogicLocator);
		smsCoreImpl.setSmsBilling(smsBillingImpl);
		smsCoreImpl.setHibernateLogicLocator(hibernateLogicLocator);

		smsSmppImpl.init();
		smsSmppImpl.setLogLevel(Level.WARN);
		smsCoreImpl.setSmsSmpp(smsSmppImpl);
		smsCoreImpl.setLoggingLevel(Level.WARN);
		LOG.setLevel(Level.WARN);
		smsAccount = new SmsAccount();
		smsAccount.setSakaiUserId(externalLogic.getCurrentUserId());
		smsAccount.setSakaiSiteId(externalLogic.getCurrentSiteId());
		smsAccount.setMessageTypeCode("3");
		smsAccount.setOverdraftLimit(1000L);
		smsAccount.setCredits(smsCoreImpl.smsBilling
				.convertAmountToCredits(100f));
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
	@Override
	public void testOnetimeSetup() {
		StandaloneSmsDaoImpl.createSchema();
		hibernateLogicLocator.getSmsAccountLogic()
				.persistSmsAccount(smsAccount);
		SmsConfig config = hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSmsConfigBySakaiSiteId(
						externalLogic.getCurrentSiteId());
		config.setSendSmsEnabled(true);
		hibernateLogicLocator.getSmsConfigLogic().persistSmsConfig(config);
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
					"testProcessNextTask-smsTask3", externalLogic
							.getCurrentSiteId(), null, externalLogic
							.getCurrentUserId());

			now.add(Calendar.MINUTE, -1);
			SmsTask smsTask2 = smsCoreImpl.getPreliminaryTask(
					"testProcessNextTask-smsTask2", new Date(now
							.getTimeInMillis()),
					"testProcessNextTask-smsTask2MessageBody", externalLogic
							.getCurrentSiteId(), null, externalLogic
							.getCurrentUserId());

			smsTask2.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);

			now.add(Calendar.MINUTE, -3);
			SmsTask smsTask1 = smsCoreImpl.getPreliminaryTask(
					"testProcessNextTask-smsTask1", new Date(now
							.getTimeInMillis()),
					"testProcessNextTask-smsTask1MessageBody", externalLogic
							.getCurrentSiteId(), null, externalLogic
							.getCurrentUserId());

			smsTask1.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);

			now.add(Calendar.MINUTE, 60);
			SmsTask smsTask4 = smsCoreImpl.getPreliminaryTask(
					"testProcessNextTask-smsTask4", new Date(now
							.getTimeInMillis()),
					"testProcessNextTask-smsTask4MessageBody", externalLogic
							.getCurrentSiteId(), null, externalLogic
							.getCurrentUserId());

			smsTask4.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);

			smsTask1.setDateCreated(DateUtil.getCurrentDate());
			smsTask1.setSmsAccountId(smsAccount.getId());
			hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask1);
			smsTask2.setDateCreated(DateUtil.getCurrentDate());
			smsTask2.setSmsAccountId(smsAccount.getId());
			hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask2);
			smsTask3.setDateCreated(DateUtil.getCurrentDate());
			smsTask3.setSmsAccountId(smsAccount.getId());
			hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask3);
			smsTask4.setDateCreated(DateUtil.getCurrentDate());
			smsTask4.setSmsAccountId(smsAccount.getId());
			hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask4);

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

			SmsTask smsTask1Update = hibernateLogicLocator.getSmsTaskLogic()
					.getSmsTask(smsTask1.getId());
			SmsTask smsTask2Update = hibernateLogicLocator.getSmsTaskLogic()
					.getSmsTask(smsTask2.getId());
			SmsTask smsTask3Update = hibernateLogicLocator.getSmsTaskLogic()
					.getSmsTask(smsTask3.getId());
			SmsTask smsTask4Update = hibernateLogicLocator.getSmsTaskLogic()
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
	 * In this test the updating of the tasks statuses are tested.
	 */
	public void testTaskStatuses() {
		smsSmppImpl.connectToGateway();

		if (smsCoreImpl.getSmsSmpp().getConnectionStatus()) {

			Calendar now = Calendar.getInstance();
			now.add(Calendar.MINUTE, -1);
			SmsTask smsTask2 = smsCoreImpl.getPreliminaryTask(
					"TestTaskStatuses-SusscessFullTask", new Date(now
							.getTimeInMillis()),
					"TestTaskStatuses-SmsTask2MessageBody", externalLogic
							.getCurrentSiteId(), null, externalLogic
							.getCurrentUserId());

			smsTask2.setMaxTimeToLive(300);

			now.add(Calendar.MINUTE, -3);
			SmsTask smsTask1 = smsCoreImpl.getPreliminaryTask(
					"TestTaskStatuses-ExpiresTask", new Date(now
							.getTimeInMillis()),
					"TestTaskStatuses-ExpiresTask", externalLogic
							.getCurrentSiteId(), null, externalLogic
							.getCurrentUserId());
			smsTask1.setMaxTimeToLive(60);
			smsTask1.setSmsAccountId(smsAccount.getId());
			smsTask1.setDateCreated(DateUtil.getCurrentDate());
			Calendar cal = Calendar.getInstance();
			cal.setTime(smsTask1.getDateToSend());
			cal.add(Calendar.SECOND, smsTask1.getMaxTimeToLive());
			// TODO, DateToExpire must be set from the UI as well
			smsTask1.setDateToExpire(cal.getTime());
			hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask1);
			smsTask2.setDateCreated(DateUtil.getCurrentDate());
			smsTask2.setSmsAccountId(smsAccount.getId());
			hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask2);

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

			SmsTask smsTask1Update = hibernateLogicLocator.getSmsTaskLogic()
					.getSmsTask(smsTask1.getId());
			SmsTask smsTask2Update = hibernateLogicLocator.getSmsTaskLogic()
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
				"testProcessTaskFailMessageBody", externalLogic
						.getCurrentSiteId(), null, externalLogic
						.getCurrentUserId());

		smsTask.setDateCreated(DateUtil.getCurrentDate());
		smsTask.setSmsAccountId(smsAccount.getId());

		hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);

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
		SmsTask smsTaskUpdate = hibernateLogicLocator.getSmsTaskLogic()
				.getSmsTask(smsTask.getId());
		assertEquals(true, smsTaskUpdate.getStatusCode().equals(
				SmsConst_DeliveryStatus.STATUS_FAIL));
		assertEquals(true, smsTaskUpdate.getAttemptCount() == 5);
		assertEquals(true, smsTaskUpdate.getMessagesWithStatus(
				SmsConst_DeliveryStatus.STATUS_FAIL).size() == (smsTask
				.getSmsMessages().size()));

		hibernateLogicLocator.getSmsTaskLogic().deleteSmsTask(smsTask);
		LOG.info("Reconnecting to server after fail test ");
		smsSmppImpl.connectToGateway();
	}

	public void testProcessIncomingMessage() {
		smsSmppImpl.connectToGateway();
		SmsMessage smsMessage = hibernateLogicLocator.getSmsMessageLogic()
				.getNewTestSmsMessageInstance("Mobile number", "Message body");
		smsCoreImpl.processIncomingMessage(smsMessage.getMessageBody(),
				smsMessage.getMobileNumber());
	}

	/**
	 * In this test the updating of smsStatuses is tested. First a new task is
	 * created and populated with smsMessages. The total number of pending
	 * messages must equal 0 at the end. The total sent messages must equal the
	 * total messages on the task. Secondly a new task is created and the
	 * delivery report listener is switched off. After 1 minute the core service
	 * must mark all the messages on the task as timed out. The test is
	 * successful if a timed out message is found.
	 */
	public void testTimeoutAndMessageStatusUpdate() {
		try {
			smsSmppImpl.connectToGateway();
			smsSmppImpl.setLogLevel(Level.ALL);
			smsSmppImpl.getSession().setMessageReceiverListener(null);
			SmsTask statusUpdateTask = smsCoreImpl.getPreliminaryTask(
					"TestTimeoutAndMessageStatusUpdate-StatusUpdateTask",
					new Date(System.currentTimeMillis()),
					"TestTimeoutAndMessageStatusUpdate-StatusUpdateTask",
					externalLogic.getCurrentSiteId(), null, externalLogic
							.getCurrentUserId());
			statusUpdateTask
					.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
			statusUpdateTask.setAttemptCount(0);
			statusUpdateTask.setDateProcessed(new Date());
			Calendar cal = Calendar.getInstance();
			cal.setTime(statusUpdateTask.getDateToSend());
			cal.add(Calendar.SECOND, statusUpdateTask.getMaxTimeToLive());
			// TODO, DateToExpire must be set from the UI as well
			statusUpdateTask.setDateToExpire(cal.getTime());
			statusUpdateTask.setSmsMessagesOnTask(externalLogic
					.getSakaiGroupMembers(statusUpdateTask, true));
			statusUpdateTask.setSmsAccountId(smsAccount.getId());
			// statusUpdateTask.setMessageTypeId(SmsHibernateConstants.SMS_TASK_TYPE_PROCESS_NOW);
			smsCoreImpl.calculateEstimatedGroupSize(statusUpdateTask);
			SmsAccount smsAccount = hibernateLogicLocator.getSmsAccountLogic()
					.getSmsAccount(statusUpdateTask.getSmsAccountId());
			smsAccount.setCredits(1000L);
			hibernateLogicLocator.getSmsAccountLogic().persistSmsAccount(
					smsAccount);
			try {
				smsCoreImpl.insertTask(statusUpdateTask);
			} catch (SmsTaskValidationException e1) {
				fail(e1.getErrorMessagesAsBlock());
			} catch (ReceiveIncomingSmsDisabledException e) {
				fail(e.getErrorMessagesAsBlock());
			}
			smsSmppImpl
					.sendMessagesToGateway(statusUpdateTask.getSmsMessages());

			assertEquals(true, statusUpdateTask.getMessagesWithStatus(
					SmsConst_DeliveryStatus.STATUS_PENDING).size() == 0);

			SmsTask timeOutTask = smsCoreImpl.getPreliminaryTask(
					"testTimeoutAndMessageStatusUpdate-TIMEOUT", new Date(),
					"testTimeoutAndMessageStatusUpdate-TIMEOUT", externalLogic
							.getCurrentSiteId(), null, externalLogic
							.getCurrentUserId());
			timeOutTask.setDelReportTimeoutDuration(60);
			timeOutTask.setSmsMessagesOnTask(externalLogic
					.getSakaiGroupMembers(timeOutTask, true));
			timeOutTask.setSmsAccountId(smsAccount.getId());
			smsCoreImpl.calculateEstimatedGroupSize(timeOutTask);
			try {
				smsCoreImpl.insertTask(timeOutTask);
			} catch (SmsTaskValidationException e1) {
				fail(e1.getErrorMessagesAsBlock());
			} catch (ReceiveIncomingSmsDisabledException e) {
				e.getErrorMessagesAsBlock();
			}
			smsCoreImpl.processNextTask();

			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			smsCoreImpl.processTimedOutDeliveryReports();
			SmsTask smsTask3Update = hibernateLogicLocator.getSmsTaskLogic()
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
		} catch (SmsSendDeniedException se) {
			fail("SmsSendDeniedException caught");
		} catch (SmsSendDisabledException sd) {
			fail("SmsSendDisabledException caught");
		}

	}

	public void testVeryLateDeliveryReports() {
		SmsTask insertTask = new SmsTask();
		insertTask.setSakaiSiteId(externalLogic.getCurrentSiteId());
		insertTask.setSenderUserName(externalLogic.getCurrentUserId());
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
		Calendar cal = Calendar.getInstance();
		cal.setTime(insertTask.getDateToSend());
		cal.add(Calendar.SECOND, insertTask.getMaxTimeToLive());
		// TODO, DateToExpire must be set from the UI as well
		insertTask.setDateToExpire(cal.getTime());
		smsCoreImpl.calculateEstimatedGroupSize(insertTask);

		hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(insertTask);

		SmsMessage insertMessage1 = new SmsMessage();
		insertMessage1.setMobileNumber("0721998919");
		insertMessage1.setSmscMessageId("smscMessageId1");
		insertMessage1.setSmscId(SmsHibernateConstants.SMSC_ID);
		insertMessage1.setSakaiUserId("sakaiUserId");
		insertMessage1.setStatusCode(SmsConst_DeliveryStatus.STATUS_LATE);
		insertMessage1
				.setSmscDeliveryStatusCode(SmsConst_SmscDeliveryStatus.DELIVERED);
		insertMessage1.setSmsTask(insertTask);

		hibernateLogicLocator.getSmsMessageLogic().persistSmsMessage(
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

		hibernateLogicLocator.getSmsMessageLogic().persistSmsMessage(
				insertMessage2);

		smsCoreImpl.processVeryLateDeliveryReports();

		SmsMessage insertMessage2Update = hibernateLogicLocator
				.getSmsMessageLogic().getSmsMessage(insertMessage2.getId());

		assertEquals(insertMessage2Update.getStatusCode().equals(
				SmsConst_DeliveryStatus.STATUS_FAIL), true);

		SmsMessage insertMessage1Update = hibernateLogicLocator
				.getSmsMessageLogic().getSmsMessage(insertMessage1.getId());

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
		account.setOverdraftLimit(0L);
		account.setCredits(0L);
		account.setAccountName("accountName");
		account.setAccountEnabled(true);
		hibernateLogicLocator.getSmsAccountLogic().persistSmsAccount(account);

		SmsTask insertTask = new SmsTask();
		insertTask.setSakaiSiteId(externalLogic.getCurrentSiteId());
		insertTask.setSenderUserName(externalLogic.getCurrentUserId());
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
		Calendar cal = Calendar.getInstance();
		cal.setTime(insertTask.getDateToSend());
		cal.add(Calendar.SECOND, insertTask.getMaxTimeToLive());
		// TODO, DateToExpire must be set from the UI as well
		insertTask.setDateToExpire(cal.getTime());

		try {
			smsCoreImpl.insertTask(insertTask);
			fail("Excpected validation exception");
		} catch (SmsTaskValidationException e1) {
			assertTrue(e1.getErrorMessages().size() > 0);
			LOG.debug(e1.getErrorMessagesAsBlock());
		} catch (SmsSendDeniedException se) {
			fail("SmsSendDeniedException caught");
		} catch (SmsSendDisabledException sd) {
			fail("SmsSendDisabledException caught");
		} catch (ReceiveIncomingSmsDisabledException e) {
			fail("ReceiveIncomingSmsDisabledException caught");
		}
	}

	/**
	 * Test insert task for validation errors.
	 */
	public void testInsertTask_InsuficientCredit() {
		SmsAccount account = new SmsAccount();
		account.setSakaiSiteId("11111");
		account.setMessageTypeCode("1");
		account.setOverdraftLimit(1000L);
		account.setCredits(5000L);
		account.setAccountName("accountName");
		account.setAccountEnabled(true);
		hibernateLogicLocator.getSmsAccountLogic().persistSmsAccount(account);

		SmsTask insertTask = new SmsTask();
		insertTask.setMessageTypeId(1);
		insertTask.setSakaiSiteId(externalLogic.getCurrentSiteId());
		insertTask.setSenderUserName(externalLogic.getCurrentUserId());
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
		Calendar cal = Calendar.getInstance();
		cal.setTime(insertTask.getDateToSend());
		cal.add(Calendar.SECOND, insertTask.getMaxTimeToLive());
		// TODO, DateToExpire must be set from the UI as well
		insertTask.setDateToExpire(cal.getTime());

		try {
			smsCoreImpl.insertTask(insertTask);
			fail("Excpected validation exception");
		} catch (SmsTaskValidationException e1) {
			assertTrue(e1.getErrorMessages().size() > 0);
			assertTrue(e1.getErrorMessages().get(0).equals(
					"sms.errors.task.credit.insufficient"));
			LOG.debug(e1.getErrorMessagesAsBlock());
		} catch (SmsSendDeniedException se) {
			fail("SmsSendDeniedException caught");
		} catch (SmsSendDisabledException sd) {
			fail("SmsSendDisabledException caught");
		} catch (ReceiveIncomingSmsDisabledException e) {
			fail("ReceiveIncomingSmsDisabledException caught");
		}
	}

	/**
	 * Tests the aborting of a task
	 */
	public void testAbortTask() {

		SmsTask insertTask = new SmsTask();
		insertTask.setSakaiSiteId(externalLogic.getCurrentSiteId());
		insertTask.setSenderUserName(externalLogic.getCurrentUserId());
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
		Calendar cal = Calendar.getInstance();
		cal.setTime(insertTask.getDateToSend());
		cal.add(Calendar.SECOND, insertTask.getMaxTimeToLive());
		// TODO, DateToExpire must be set from the UI as well
		insertTask.setDateToExpire(cal.getTime());
		smsCoreImpl.calculateEstimatedGroupSize(insertTask);
		hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(insertTask);

		try {
			smsCoreImpl.abortPendingTask(insertTask.getId());
		} catch (SmsTaskNotFoundException e) {

			e.printStackTrace();
		}
		SmsTask insertTaskUpdate = hibernateLogicLocator.getSmsTaskLogic()
				.getSmsTask(insertTask.getId());

		assertEquals(insertTaskUpdate.getStatusCode(),
				SmsConst_DeliveryStatus.STATUS_ABORT);

		for (SmsMessage smsMessage : insertTaskUpdate.getSmsMessages()) {

			assertEquals(smsMessage.getStatusCode(),
					SmsConst_DeliveryStatus.STATUS_ABORT);
		}

	}

	public void testSmsSendDisabled() {
		SmsConfig config = hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSmsConfigBySakaiSiteId(
						externalLogic.getCurrentSiteId());
		config.setSendSmsEnabled(false);
		hibernateLogicLocator.getSmsConfigLogic().persistSmsConfig(config);

		SmsTask insertTask = new SmsTask();
		insertTask.setSakaiSiteId(externalLogic.getCurrentSiteId());
		insertTask.setSenderUserName(externalLogic.getCurrentUserId());

		try {
			smsCoreImpl.insertTask(insertTask);
			fail("SmsSendDisabledException shoud be thrown");
		} catch (SmsTaskValidationException e) {
			fail("SmsTaskValidationException caught");
		} catch (SmsSendDeniedException e) {
			fail("SmsSendDeniedException caught");
		} catch (SmsSendDisabledException e) {
			assertNotNull(e);
		} catch (ReceiveIncomingSmsDisabledException e) {
			fail("ReceiveIncomingSmsDisabledException caught");
		}

		// test shouldn't be dependant on eachother
		// we really must find time to fix it
		config.setSendSmsEnabled(true);
		hibernateLogicLocator.getSmsConfigLogic().persistSmsConfig(config);
	}

}
