package org.sakaiproject.sms.logic.hibernate.test;

import java.util.Calendar;
import java.util.Date;

import org.sakaiproject.sms.model.hibernate.SmsMessage;
import org.sakaiproject.sms.model.hibernate.SmsTask;
import org.sakaiproject.sms.model.hibernate.constants.SmsConst_DeliveryStatus;
import org.sakaiproject.sms.model.hibernate.constants.SmsConstants;
import org.sakaiproject.sms.util.AbstractBaseTestCase;

/**
 * The Class smsHibernateStressTest. We work with many messages to see how the
 * database performs. The following steps are executed: 1) Create a sms task 2)
 * create [messageCount] messages for the task 3) Read back the task with all
 * its messages, 4) read back one of the messages, 5)remove the task and its
 * messages
 */
public class SmsDatabaseStressTest extends AbstractBaseTestCase {

	/** The number of messages to insert, change as required. */
	private static final int MESSAGECOUNT = 5000;

	/** The sms task. */
	private static SmsTask smsTask;

	static {
		if (!SmsConstants.isDbSchemaCreated) {
			smsDao.createSchema();
			SmsConstants.isDbSchemaCreated = true;
		}
		smsTask = new SmsTask();
		smsTask.setSakaiSiteId(SmsConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID);
		smsTask.setSmsAccountId(1l);
		smsTask.setDateCreated(new Date(System.currentTimeMillis()));
		smsTask.setDateToSend(new Date(System.currentTimeMillis()));
		smsTask.setStatusCode("SC");
		smsTask.setAttemptCount(2);
		smsTask.setMessageBody(SmsConstants.SMS_DEV_DEFAULT_SMS_MESSAGE_BODY);
		smsTask.setSenderUserName("senderUserName");
		smsTask.setMaxTimeToLive(1);
		final Calendar cal = Calendar.getInstance();
		cal.setTime(smsTask.getDateToSend());
		cal.add(Calendar.SECOND, smsTask.getMaxTimeToLive());
		smsTask.setDateToExpire(cal.getTime());
		smsTask.setDelReportTimeoutDuration(1);
	}

	/**
	 * Instantiates a new sms hibernate stress test.
	 */
	public SmsDatabaseStressTest() {

	}

	/**
	 * Test many messages insert.
	 */
	public void testInsertManyMessages() {

		for (int i = 0; i < MESSAGECOUNT; i++) {
			final SmsMessage smsMessage = new SmsMessage();
			smsMessage.setMobileNumber("0823450983");
			smsMessage.setSmscMessageId("smscMessage_" + i);
			smsMessage.setSakaiUserId("sakaiUserId");
			smsMessage.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
			smsMessage.setSmsTask(smsTask);
			smsTask.getSmsMessages().add(smsMessage);
		}
		hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);
		assertTrue("Not all messages returned",
				smsTask.getSmsMessages().size() == MESSAGECOUNT);
	}

	// TODO: Careful, the attached task read all the messages attached to it, we
	// don't want this here!

	/**
	 * Test get task messages.
	 */
	public void testGetTaskMessages() {
		final SmsTask theSmsTask = hibernateLogicLocator.getSmsTaskLogic()
				.getSmsTask(smsTask.getId());

		assertNotNull("theSmsTask may not be null", theSmsTask);
		assertTrue("Message size not correct", theSmsTask.getSmsMessages()
				.size() == MESSAGECOUNT);

	}

	/**
	 * Test delete tasks.
	 */
	public void testDeleteTasks() {
		final SmsTask theSmsTask = hibernateLogicLocator.getSmsTaskLogic()
				.getSmsTask(smsTask.getId());
		Long firstMessageID = ((SmsMessage) theSmsTask.getSmsMessages()
				.toArray()[0]).getId();
		hibernateLogicLocator.getSmsTaskLogic().deleteSmsTask(smsTask);
		final SmsTask getSmsTask = hibernateLogicLocator.getSmsTaskLogic()
				.getSmsTask(theSmsTask.getId());
		assertNull("Task not removed", getSmsTask);
		final SmsMessage theMessage = hibernateLogicLocator
				.getSmsMessageLogic().getSmsMessage(firstMessageID);
		assertNull("Messages not removed", theMessage);
	}
}
