package org.sakaiproject.sms.logic.hibernate.test;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sakaiproject.sms.bean.SearchFilterBean;
import org.sakaiproject.sms.bean.SearchResultContainer;
import org.sakaiproject.sms.logic.hibernate.exception.SmsSearchException;
import org.sakaiproject.sms.model.hibernate.SmsMessage;
import org.sakaiproject.sms.model.hibernate.SmsTask;
import org.sakaiproject.sms.model.hibernate.constants.SmsConst_DeliveryStatus;
import org.sakaiproject.sms.model.hibernate.constants.SmsConstants;
import org.sakaiproject.sms.util.AbstractBaseTestCase;

/**
 * The Class SmsMessageTest.
 */
public class SmsMessageTest extends AbstractBaseTestCase {

	/** The insert task. */
	private static SmsTask insertTask;

	/** The insert message1. */
	private static SmsMessage insertMessage1;

	/** The insert message2. */
	private static SmsMessage insertMessage2;

	static {
		if (!SmsConstants.isDbSchemaCreated) {
			smsDao.createSchema();
			SmsConstants.isDbSchemaCreated = true;
		}
		insertTask = new SmsTask();
		insertTask.setSakaiSiteId(SmsConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID);
		insertTask.setSmsAccountId(1l);
		insertTask.setDateCreated(new Date(System.currentTimeMillis()));
		insertTask.setDateToSend(new Date(System.currentTimeMillis()));
		insertTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		insertTask.setAttemptCount(2);
		insertTask
				.setMessageBody(SmsConstants.SMS_DEV_DEFAULT_SMS_MESSAGE_BODY);
		insertTask.setSenderUserName("senderUserName");
		insertTask.setMaxTimeToLive(1);
		Calendar cal = Calendar.getInstance();
		cal.setTime(insertTask.getDateToSend());
		cal.add(Calendar.SECOND, insertTask.getMaxTimeToLive());
		insertTask.setDateToExpire(cal.getTime());
		// Insert the task so we can play with messages

		insertMessage1 = new SmsMessage();
		insertMessage1.setMobileNumber("0721998919");
		insertMessage1.setSmscMessageId("SmsMessageTest-smscMessageId1");
		insertMessage1.setSmscId(SmsConstants.SMSC_ID);
		insertMessage1.setSakaiUserId("sakaiUserId");
		insertMessage1.setStatusCode(SmsConst_DeliveryStatus.STATUS_DELIVERED);

		insertMessage2 = new SmsMessage();
		insertMessage2.setMobileNumber("0823450983");
		insertMessage2.setSmscMessageId("SmsMessageTest-smscMessageId2");
		insertMessage2.setSmscId(SmsConstants.SMSC_ID);
		insertMessage2.setSakaiUserId("sakaiUserId");
		insertMessage2.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
	}

	/**
	 * Instantiates a new sms message test.
	 */
	public SmsMessageTest() {
	}

	/**
	 * Instantiates a new sms message test.
	 * 
	 * @param name
	 *            the name
	 */
	public SmsMessageTest(String name) {
		super(name);
	}

	/**
	 * Test insert sms message.
	 */
	public void testInsertSmsMessage() {
		hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(insertTask);
		assertTrue("Task for message not created", insertTask.exists());
		insertMessage1.setSmsTask(insertTask);
		insertMessage2.setSmsTask(insertTask);
		hibernateLogicLocator.getSmsMessageLogic().persistSmsMessage(
				insertMessage1);
		hibernateLogicLocator.getSmsMessageLogic().persistSmsMessage(
				insertMessage2);
		assertTrue("Object not persisted", insertMessage1.exists());
		assertTrue("Object not persisted", insertMessage2.exists());
		Set<SmsMessage> smsMessages = new HashSet<SmsMessage>();
		smsMessages.add(insertMessage2);
		smsMessages.add(insertMessage1);
		insertTask.setSmsMessagesOnTask(smsMessages);
		assertTrue("", insertTask.getSmsMessages().contains(insertMessage1));
		assertTrue("", insertTask.getSmsMessages().contains(insertMessage2));
		hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(insertTask);

	}

	/**
	 * Test get sms message by id.
	 */
	public void testGetSmsMessageById() {
		SmsMessage getSmsMessage = hibernateLogicLocator.getSmsMessageLogic()
				.getSmsMessage(insertMessage1.getId());
		assertTrue("Object not persisted", insertMessage1.exists());
		assertNotNull(getSmsMessage);
		assertEquals(insertMessage1, getSmsMessage);

	}

	/**
	 * Test update sms message.
	 */
	public void testUpdateSmsMessage() {
		SmsMessage smsMessage = hibernateLogicLocator.getSmsMessageLogic()
				.getSmsMessage(insertMessage1.getId());
		smsMessage.setSakaiUserId("newSakaiUserId");
		hibernateLogicLocator.getSmsMessageLogic()
				.persistSmsMessage(smsMessage);
		smsMessage = hibernateLogicLocator.getSmsMessageLogic().getSmsMessage(
				insertMessage1.getId());
		assertEquals("newSakaiUserId", smsMessage.getSakaiUserId());
	}

	public void testGetallMessagesFormTask() {

		assertTrue("Collection returned has no objects",
				hibernateLogicLocator.getSmsMessageLogic()
						.getSmsMessagesForTask(insertTask.getId()) != null);
		assertTrue("Collection should not be null", hibernateLogicLocator
				.getSmsMessageLogic().getSmsMessagesForTask(888888888l) != null);

		assertTrue("Collection must be empty", hibernateLogicLocator
				.getSmsMessageLogic().getSmsMessagesForTask(888888888l)
				.isEmpty());

	}

	/**
	 * Tests the getMessagesForCriteria method
	 */
	public void testGetMessagesForCriteria() {

		SmsTask insertTask = new SmsTask();
		insertTask.setSakaiSiteId(SmsConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID);
		insertTask.setSmsAccountId(1l);
		insertTask.setDateCreated(new Date(System.currentTimeMillis()));
		insertTask.setDateToSend(new Date(System.currentTimeMillis()));
		insertTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		insertTask.setAttemptCount(2);
		insertTask
				.setMessageBody(SmsConstants.SMS_DEV_DEFAULT_SMS_MESSAGE_BODY);
		insertTask.setSenderUserName("messageCrit");
		insertTask.setSakaiToolName("sakaiToolName");
		insertTask.setMaxTimeToLive(1);
		Calendar cal = Calendar.getInstance();
		cal.setTime(insertTask.getDateToSend());
		cal.add(Calendar.SECOND, insertTask.getMaxTimeToLive());
		insertTask.setDateToExpire(cal.getTime());

		SmsMessage insertMessage = new SmsMessage();
		insertMessage.setMobileNumber("0721998919");
		insertMessage.setSmscMessageId("criterai");
		insertMessage.setSakaiUserId("criterai");
		insertMessage.setDateDelivered(new Date(System.currentTimeMillis()));
		insertMessage.setStatusCode(SmsConst_DeliveryStatus.STATUS_DELIVERED);
		insertMessage.setSmsTask(insertTask);
		insertTask.getSmsMessages().add(insertMessage);

		try {

			hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(insertTask);
			hibernateLogicLocator.getSmsMessageLogic().persistSmsMessage(
					insertMessage);

			SearchFilterBean bean = new SearchFilterBean();
			bean.setStatus(insertMessage.getStatusCode());
			bean.setDateFrom((new Date()));
			bean.setDateTo((new Date()));
			bean.setToolName(insertTask.getSakaiToolName());
			bean.setSender(insertTask.getSenderUserName());
			bean.setNumber(insertMessage.getMobileNumber());
			bean.setCurrentPage(1);

			SearchResultContainer<SmsMessage> con = hibernateLogicLocator
					.getSmsMessageLogic().getPagedSmsMessagesForCriteria(bean);
			List<SmsMessage> messages = con.getPageResults();
			assertTrue("Collection returned has no objects",
					messages.size() > 0);

			for (SmsMessage message : messages) {
				// We know that only one message should be returned becuase
				// we only added one with status ERROR.
				message.equals(insertMessage);
				assertEquals(message, insertMessage);
			}
		} catch (SmsSearchException se) {

		}

	}

	public void testGetMessageWithNullDeliveryDate() {

		SmsTask insertTask = new SmsTask();
		insertTask.setSakaiSiteId(SmsConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID);
		insertTask.setSmsAccountId(1l);
		insertTask.setDateCreated(new Date(System.currentTimeMillis()));
		insertTask.setDateToSend(new Date(System.currentTimeMillis()));
		insertTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		insertTask.setAttemptCount(2);
		insertTask.setMessageBody("messageCrit");
		insertTask.setSenderUserName("messageCrit");
		insertTask.setSakaiToolName("sakaiToolName");
		insertTask.setMaxTimeToLive(1);
		Calendar cal = Calendar.getInstance();
		cal.setTime(insertTask.getDateToSend());
		cal.add(Calendar.SECOND, insertTask.getMaxTimeToLive());
		insertTask.setDateToExpire(cal.getTime());

		SmsMessage insertMessage = new SmsMessage();
		insertMessage.setMobileNumber("0721998919");
		insertMessage.setSmscMessageId("criterai");
		insertMessage.setSakaiUserId("criterai");
		insertMessage.setDateDelivered(null);
		insertMessage.setStatusCode(SmsConst_DeliveryStatus.STATUS_TIMEOUT);

		insertMessage.setSmsTask(insertTask);
		insertTask.getSmsMessages().add(insertMessage);

		hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(insertTask);
		hibernateLogicLocator.getSmsMessageLogic().persistSmsMessage(
				insertMessage);

		SearchFilterBean bean = new SearchFilterBean();
		bean.setStatus(insertMessage.getStatusCode());
		bean.setDateFrom(null);
		bean.setDateTo(null);
		bean.setToolName(insertTask.getSakaiToolName());
		bean.setSender(insertTask.getSenderUserName());
		bean.setNumber(insertMessage.getMobileNumber());
		bean.setCurrentPage(1);

		try {
			SearchResultContainer<SmsMessage> con = hibernateLogicLocator
					.getSmsMessageLogic().getPagedSmsMessagesForCriteria(bean);
			List<SmsMessage> messages = con.getPageResults();
			assertTrue("Message not returned", messages.size() == 1);
			assertNull(messages.get(0).getDateDelivered());
		} catch (SmsSearchException se) {
			fail(se.getMessage());
		}

	}

	/**
	 * Tests the getMessagesForCriteria method
	 */
	public void testGetMessagesForCriteria_Paging() {

		int recordsToInsert = 93;

		SmsTask insertTask = new SmsTask();
		insertTask.setSakaiSiteId(SmsConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID);
		insertTask.setSmsAccountId(1l);
		insertTask.setDateCreated(new Date(System.currentTimeMillis()));
		insertTask.setDateToSend(new Date(System.currentTimeMillis()));
		insertTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		insertTask.setAttemptCount(2);
		insertTask.setMessageBody("messageCrit");
		insertTask.setSenderUserName("messageCrit");
		insertTask.setSakaiToolName("sakaiToolName");
		insertTask.setMaxTimeToLive(1);
		Calendar cal = Calendar.getInstance();
		cal.setTime(insertTask.getDateToSend());
		cal.add(Calendar.SECOND, insertTask.getMaxTimeToLive());
		insertTask.setDateToExpire(cal.getTime());

		for (int i = 0; i < recordsToInsert; i++) {
			SmsMessage insertMessage = new SmsMessage();
			insertMessage.setMobileNumber("0721998919");
			insertMessage.setSmscMessageId("criterai");
			insertMessage.setSakaiUserId("crit");
			insertMessage.setStatusCode(SmsConst_DeliveryStatus.STATUS_ERROR);
			insertMessage.setSmsTask(insertTask);
			insertTask.getSmsMessages().add(insertMessage);
			insertMessage.setSmscMessageId("" + i);// To make unique
			insertMessage
					.setDateDelivered(new Date(System.currentTimeMillis()));
		}

		try {
			hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(insertTask);

			for (SmsMessage sms : insertTask.getSmsMessages()) {

				hibernateLogicLocator.getSmsMessageLogic().persistSmsMessage(
						sms);
			}

			SearchFilterBean bean = new SearchFilterBean();
			bean.setStatus(SmsConst_DeliveryStatus.STATUS_ERROR);
			bean.setDateFrom(new Date(System.currentTimeMillis() - 10000));
			bean.setDateTo(new Date());
			bean.setToolName(insertTask.getSakaiToolName());
			bean.setSender(insertTask.getSenderUserName());
			bean.setNumber("0721998919");

			bean.setCurrentPage(2);

			SearchResultContainer<SmsMessage> con = hibernateLogicLocator
					.getSmsMessageLogic().getPagedSmsMessagesForCriteria(bean);
			List<SmsMessage> messages = con.getPageResults();
			assertTrue("Incorrect collection size returned",
					messages.size() == SmsConstants.DEFAULT_PAGE_SIZE);

			// Test last page. We know there are 124 records to this should
			// return a list of 4

			int pages = recordsToInsert / SmsConstants.DEFAULT_PAGE_SIZE;
			// set to last page
			if (recordsToInsert % SmsConstants.DEFAULT_PAGE_SIZE == 0) {
				bean.setCurrentPage(pages);
			} else {
				bean.setCurrentPage(pages + 1);
			}

			con = hibernateLogicLocator.getSmsMessageLogic()
					.getPagedSmsMessagesForCriteria(bean);
			messages = con.getPageResults();
			int lastPageRecordCount = recordsToInsert
					- (pages * SmsConstants.DEFAULT_PAGE_SIZE);
			assertTrue("Incorrect collection size returned",
					messages.size() == lastPageRecordCount);

		} catch (SmsSearchException se) {
			fail(se.getMessage());
		}
	}

	/**
	 * Test get new test sms message instance.
	 */
	public void testGetNewTestSmsMessageInstance() {
		SmsMessage message = hibernateLogicLocator.getSmsMessageLogic()
				.getNewTestSmsMessageInstance("0721999988", "Message body");
		hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(
				message.getSmsTask());
		assertNotNull("Message returned was null", message);
		assertNotNull("", message.getSmsTask());
		assertTrue("Associated SmsTask not created successfully", message
				.getSmsTask().exists());
	}

	/**
	 * Test get sms message by smsc message id.
	 */
	public void testGetSmsMessageBySmscMessageId() {
		SmsMessage smsMessage = hibernateLogicLocator
				.getSmsMessageLogic()
				.getSmsMessageBySmscMessageId(
						insertMessage2.getSmscMessageId(), SmsConstants.SMSC_ID);
		assertTrue(smsMessage.equals(insertMessage2));
		assertEquals(smsMessage.getSmscMessageId(), insertMessage2
				.getSmscMessageId());
	}

	/**
	 * Test get sms messages.
	 */
	public void testGetSmsMessages() {
		List<SmsMessage> messages = hibernateLogicLocator.getSmsMessageLogic()
				.getAllSmsMessages();
		assertNotNull("Returned collection is null", messages);
		assertTrue("No records returned", messages.size() > 0);
	}

	/**
	 * Tests getSmsMessagesWithStatus returns only messages with the specifed
	 * status codes
	 */
	public void testGetSmsMessagesWithStatus() {

		// Assert that messages exist for this task that have a status other
		// than PENDING

		boolean otherStatusFound = false;
		for (SmsMessage message : hibernateLogicLocator.getSmsMessageLogic()
				.getSmsMessagesWithStatus(insertTask.getId(),
						SmsConst_DeliveryStatus.STATUS_PENDING,
						SmsConst_DeliveryStatus.STATUS_SENT,
						SmsConst_DeliveryStatus.STATUS_RETRY,
						SmsConst_DeliveryStatus.STATUS_DELIVERED)) {
			if (!message.getStatusCode().equals(
					SmsConst_DeliveryStatus.STATUS_PENDING)) {
				otherStatusFound = true;
				break;
			}
		}
		assertTrue(
				"This test requires that messages exist for this task that have a status other than PENDING",
				otherStatusFound);

		List<SmsMessage> messages = hibernateLogicLocator.getSmsMessageLogic()
				.getSmsMessagesWithStatus(insertTask.getId(),
						SmsConst_DeliveryStatus.STATUS_PENDING);

		// We expect some records to be returned
		assertTrue("Expected objects in collection", messages.size() > 0);

		// We know there are messages for this task that have status codes other
		// than the one specifies above
		// So assert that the method only returned ones with the specified
		// status.
		for (SmsMessage message : messages) {
			assertTrue("Incorrect value returned for object", message
					.getStatusCode().equals(
							SmsConst_DeliveryStatus.STATUS_PENDING));
		}
	}
}