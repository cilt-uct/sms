package org.sakaiproject.sms.logic.hibernate.test;

import java.util.Date;

import org.hibernate.Session;
import org.sakaiproject.sms.logic.impl.hibernate.HibernateLogicFactory;
import org.sakaiproject.sms.model.hibernate.SmsMessage;
import org.sakaiproject.sms.model.hibernate.SmsTask;
import org.sakaiproject.sms.model.hibernate.constants.SmsConst_DeliveryStatus;
import org.sakaiproject.sms.util.AbstractBaseTestCase;
import org.sakaiproject.sms.util.HibernateUtil;

/**
 * The Class smsHibernateStressTest. We work with many messages to see how the
 * database performs. The following steps are executed: 1) Create a sms task 2)
 * create [messageCount] messages for the task 3) Read back the task with all
 * its messages, 4) read back one of the messages, 5)remove the task and its
 * messages
 */
public class SmsDatabaseStressTest extends AbstractBaseTestCase {

	/** The number of messages to insert, change as required. */
	private static int messageCount = 5000;

	/** The first message id. */
	private static long firstMessageID;

	/** The is of the new task. */
	private static long smsTaskID;

	/** The sms task. */
	private static SmsTask smsTask;

	/**
	 * we want to flush the hibernate cache
	 * 
	 */
	private static Session session;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.sms.util.AbstractBaseTestCase#testOnetimeSetup()
	 */
	public void testOnetimeSetup() {
		HibernateUtil.setTestConfiguration(true);
		HibernateUtil.createSchema();
	}

	/**
	 * Instantiates a new sms hibernate stress test.
	 */
	public SmsDatabaseStressTest() {
		smsTask = new SmsTask();
		smsTask.setSakaiSiteId("sakaiSiteId");
		smsTask.setSmsAccountId(1l);
		smsTask.setDateCreated(new Date(System.currentTimeMillis()));
		smsTask.setDateToSend(new Date(System.currentTimeMillis()));
		smsTask.setStatusCode("SC");
		smsTask.setAttemptCount(2);
		smsTask.setMessageBody("messageBody");
		smsTask.setSenderUserName("senderUserName");
		smsTask.setMaxTimeToLive(1);
		smsTask.setDelReportTimeoutDuration(1);
	}

	/**
	 * Test many messages insert.
	 */
	public void testInsertManyMessages() {

		for (int i = 0; i < messageCount; i++) {
			SmsMessage smsMessage = new SmsMessage();
			smsMessage.setMobileNumber("0823450983");
			smsMessage.setSmscMessageId("smscMessage_" + i);
			smsMessage.setSakaiUserId("sakaiUserId");
			smsMessage.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
			smsMessage.setSmsTask(smsTask);
			smsTask.getSmsMessages().add(smsMessage);
		}
		HibernateLogicFactory.getTaskLogic().persistSmsTask(smsTask);
		smsTaskID = smsTask.getId();
		assertTrue("Not all messages returned",
				smsTask.getSmsMessages().size() == messageCount);
	}

	// TODO: Careful, the attached task read all the messages attached to it, we
	// don't want this here!

	/**
	 * Test get task messages.
	 */
	public void testGetTaskMessages() {
		SmsTask theSmsTask = HibernateLogicFactory.getTaskLogic().getSmsTask(
				smsTaskID);
		firstMessageID = ((SmsMessage) theSmsTask.getSmsMessages().toArray()[0])
				.getId();
		assertNotNull(theSmsTask);
		assertTrue("Message size not correct", theSmsTask.getSmsMessages()
				.size() == messageCount);

	}

	/**
	 * Test delete tasks.
	 */
	public void testDeleteTasks() {

		HibernateLogicFactory.getTaskLogic().deleteSmsTask(smsTask);
		SmsTask getSmsTask = HibernateLogicFactory.getTaskLogic().getSmsTask(
				smsTaskID);
		assertNull("Task not removed", getSmsTask);
		SmsMessage theMessage = HibernateLogicFactory.getMessageLogic()
				.getSmsMessage(firstMessageID);
		assertNull("Messages not removed", theMessage);
	}
}
