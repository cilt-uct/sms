package org.sakaiproject.sms.logic.hibernate.test;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sakaiproject.sms.bean.SearchFilterBean;
import org.sakaiproject.sms.bean.SearchResultContainer;
import org.sakaiproject.sms.logic.hibernate.exception.SmsSearchException;
import org.sakaiproject.sms.logic.impl.hibernate.HibernateLogicFactory;
import org.sakaiproject.sms.model.hibernate.SmsMessage;
import org.sakaiproject.sms.model.hibernate.SmsTask;
import org.sakaiproject.sms.model.hibernate.constants.SmsConst_DeliveryStatus;
import org.sakaiproject.sms.model.hibernate.constants.SmsHibernateConstants;
import org.sakaiproject.sms.util.AbstractBaseTestCase;
import org.sakaiproject.sms.util.HibernateUtil;

// TODO: Auto-generated Javadoc
/**
 * A task consists of a series of rules and must be executed on the scheduled
 * date-time. For example: Send message x to group y at time z. When the task is
 * executed (real-time or scheduled), the corresponding messages will be
 * inserted into SMS_MESSAGE with status PENDING. As delivery reports come in,
 * the message statuses will be updated.
 */
public class SmsTaskTest extends AbstractBaseTestCase {

	private static final String MOBILE_NUMBER_1 = "082 345 6789";
	private static final String MOBILE_NUMBER_2 = "083 345 6789";
	private static final String MOBILE_NUMBER_3 = "084 345 6789";

	/** The insert task. */
	private static SmsTask insertTask;

	/** The insert message1. */
	private static SmsMessage insertMessage1;

	/** The insert message2. */
	private static SmsMessage insertMessage2;

	static {

		insertTask = createTestTask();

		insertMessage1 = createTestMessage1();

		insertMessage2 = createTestMessage2();
	}

	private static SmsMessage createTestMessage1() {
		SmsMessage message = new SmsMessage();
		message.setMobileNumber("0721998919");
		message.setSmscMessageId("smscMessageId1Task");
		message.setSakaiUserId("sakaiUserId");
		message.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		return message;
	}

	private static SmsMessage createTestMessage2() {
		SmsMessage message = new SmsMessage();
		message.setMobileNumber("0823450983");
		message.setSmscMessageId("smscMessageId2Task");
		message.setSakaiUserId("sakaiUserId");
		message.setStatusCode(SmsConst_DeliveryStatus.STATUS_INCOMPLETE);
		return message;
	}

	private static SmsTask createTestTask() {

		SmsTask testTask = new SmsTask();
		testTask.setSakaiSiteId("sakaiSiteId");
		testTask.setSmsAccountId(1l);
		testTask.setDateCreated(new Date(System.currentTimeMillis()));
		testTask.setDateToSend(new Date(System.currentTimeMillis()));
		testTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		testTask.setAttemptCount(2);
		testTask.setMessageBody("messageBody");
		testTask.setSenderUserName("senderUserName");
		testTask.setMaxTimeToLive(1);
		testTask.setDelReportTimeoutDuration(1);

		testTask.setDeliveryEntityList(Arrays.asList("1234", "1235", "1236"));
		return testTask;
	}

	/**
	 * Instantiates a new sms task test.
	 */
	public SmsTaskTest() {

	}

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
	 * Instantiates a new sms task test.
	 * 
	 * @param name
	 *            the name
	 */
	public SmsTaskTest(String name) {
		super(name);
	}

	/**
	 * Test add sms messages to task.
	 */
	public void testAddSmsMessagesToTask() {
		insertMessage1.setSmsTask(insertTask);
		insertMessage2.setSmsTask(insertTask);
		insertTask.getSmsMessages().add(insertMessage1);
		insertTask.getSmsMessages().add(insertMessage2);
		HibernateLogicFactory.getTaskLogic().persistSmsTask(insertTask);
		SmsTask getSmsTask = HibernateLogicFactory.getTaskLogic().getSmsTask(
				insertTask.getId());
		assertNotNull(insertTask);
		assertTrue("Collection size not correct", getSmsTask.getSmsMessages()
				.size() == 2);
	}

	/**
	 * Test add sms messages to task_set messages.
	 */
	public void testAddSmsMessagesToTask_setMessages() {

		SmsMessage insertMessage1 = new SmsMessage();
		insertMessage1.setMobileNumber("0721998919");
		insertMessage1.setSmscMessageId("smscGetID1");
		insertMessage1.setSakaiUserId("sakaiUserId");
		insertMessage1.setStatusCode(SmsConst_DeliveryStatus.STATUS_DELIVERED);
		//
		SmsMessage insertMessage2 = new SmsMessage();
		insertMessage2.setMobileNumber("0823450983");
		insertMessage2.setSmscMessageId("smscGetID2");
		insertMessage2.setSakaiUserId("sakaiUserId");
		insertMessage2.setStatusCode(SmsConst_DeliveryStatus.STATUS_DELIVERED);

		insertMessage1.setSmsTask(insertTask);
		insertMessage2.setSmsTask(insertTask);

		Set<SmsMessage> messages = new HashSet<SmsMessage>();
		messages.add(insertMessage1);
		messages.add(insertMessage2);

		insertTask.setSmsMessagesOnTask(messages);
		HibernateLogicFactory.getTaskLogic().persistSmsTask(insertTask);
	}

	/**
	 * Test delete sms task.
	 */
	public void testDeleteSmsTask() {
		HibernateLogicFactory.getTaskLogic().deleteSmsTask(insertTask);
		SmsTask getSmsTask = HibernateLogicFactory.getTaskLogic().getSmsTask(
				insertTask.getId());
		assertNull("Object not removed", getSmsTask);
	}

	/**
	 * Test get next sms task. Checks if it is the oldest task to process (Being
	 * the next task to process)
	 */
	public void testGetNextSmsTask() {
		/*
		 * SmsTask nextTask = logic.getNextSmsTask();
		 * assertNotNull("Required record not found", nextTask);
		 * List<SmsMessage> messages =
		 * messageLogic.getSmsMessagesWithStatus(null,
		 * SmsConst_DeliveryStatus.STATUS_PENDING,
		 * SmsConst_DeliveryStatus.STATUS_INCOMPLETE);
		 * 
		 * Timestamp t = null; // Get the oldest date to send from the list; for
		 * (SmsMessage message : messages) { if (t == null) { t =
		 * message.getSmsTask().getDateToSend(); } if
		 * (message.getSmsTask().getDateToSend() != null &&
		 * message.getSmsTask().getDateToSend().getTime() < t .getTime()) { t =
		 * message.getSmsTask().getDateToSend(); break; } }
		 * assertNotNull("No records found", t);
		 * assertTrue("Did not get the correct task to be processed", nextTask
		 * .getDateToSend().getTime() == t.getTime());
		 */

	}

	/**
	 * Test get sms task by id.
	 */
	public void testGetSmsTaskById() {
		SmsTask taskToPersist = createTestTask();
		taskToPersist.setDeliveryGroupName("Maths department");
		HibernateLogicFactory.getTaskLogic().persistSmsTask(taskToPersist);
		SmsTask returnedSmsTask = HibernateLogicFactory.getTaskLogic()
				.getSmsTask(taskToPersist.getId());
		assertNotNull(returnedSmsTask);
		assertEquals(taskToPersist, returnedSmsTask);
	}

	/**
	 * Test get sms tasks.
	 */
	public void testGetSmsTasks() {
		List<SmsTask> tasks = HibernateLogicFactory.getTaskLogic()
				.getAllSmsTask();
		assertNotNull("Returned list is null", tasks);
		assertTrue("No records returned", tasks.size() > 0);
	}

	/**
	 * Tests the getMessagesForCriteria method.
	 */
	public void testGetTasksForCriteria() {
		SmsTask insertTask = new SmsTask();
		insertTask.setSakaiSiteId("sakaiSiteId");
		insertTask.setSmsAccountId(1l);
		insertTask.setDateCreated(new Date(System.currentTimeMillis()));
		insertTask.setDateToSend(new Date(System.currentTimeMillis()));
		insertTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		insertTask.setAttemptCount(2);
		insertTask.setMessageBody("taskCrit");
		insertTask.setSenderUserName("taskCrit");
		insertTask.setSakaiToolName("sakaiToolName");
		insertTask.setMaxTimeToLive(1);
		insertTask.setDelReportTimeoutDuration(1);

		try {
			HibernateLogicFactory.getTaskLogic().persistSmsTask(insertTask);

			SearchFilterBean bean = new SearchFilterBean();
			bean.setStatus(insertTask.getStatusCode());
			bean.setDateFrom(new Date());
			bean.setDateTo(new Date());
			bean.setToolName(insertTask.getSakaiToolName());
			bean.setSender(insertTask.getSenderUserName());

			List<SmsTask> tasks = HibernateLogicFactory.getTaskLogic()
					.getPagedSmsTasksForCriteria(bean).getPageResults();
			assertTrue("Collection returned has no objects", tasks.size() > 0);

			for (SmsTask task : tasks) {
				// We know that only one task should be returned
				assertEquals(task, insertTask);
			}
		} catch (SmsSearchException se) {
			fail(se.getMessage());
		} finally {
			HibernateLogicFactory.getTaskLogic().deleteSmsTask(insertTask);
		}
	}

	/**
	 * Test get tasks for criteria_ paging.
	 */
	public void testGetTasksForCriteria_Paging() {

		int recordsToInsert = 93;

		for (int i = 0; i < recordsToInsert; i++) {
			SmsTask insertTask = new SmsTask();
			insertTask.setSakaiSiteId("sakaiSiteId");
			insertTask.setSmsAccountId(1l);
			insertTask.setDateCreated(new Date(System.currentTimeMillis()));
			insertTask.setDateToSend(new Date(System.currentTimeMillis()));
			insertTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_FAIL);
			insertTask.setAttemptCount(2);
			insertTask.setMessageBody("taskCrit");
			insertTask.setSenderUserName("taskCrit");
			insertTask.setSakaiToolName("sakaiToolName");
			insertTask.setMaxTimeToLive(1);
			insertTask.setDelReportTimeoutDuration(i);
			HibernateLogicFactory.getTaskLogic().persistSmsTask(insertTask);
		}

		try {

			SearchFilterBean bean = new SearchFilterBean();
			bean.setStatus(SmsConst_DeliveryStatus.STATUS_FAIL);
			bean.setDateFrom(new Date());
			bean.setDateTo(new Date());
			bean.setToolName("sakaiToolName");
			bean.setSender("taskCrit");

			bean.setCurrentPage(2);

			SearchResultContainer<SmsTask> con = HibernateLogicFactory
					.getTaskLogic().getPagedSmsTasksForCriteria(bean);
			List<SmsTask> tasks = con.getPageResults();
			assertTrue("Incorrect collection size returned",
					tasks.size() == SmsHibernateConstants.DEFAULT_PAGE_SIZE);

			// Test last page. We know there are 124 records to this should
			// return a list of 4

			int pages = recordsToInsert
					/ SmsHibernateConstants.DEFAULT_PAGE_SIZE;
			// set to last page
			if (recordsToInsert % SmsHibernateConstants.DEFAULT_PAGE_SIZE == 0) {
				bean.setCurrentPage(pages);
			} else {
				bean.setCurrentPage(pages + 1);
			}

			con = HibernateLogicFactory.getTaskLogic()
					.getPagedSmsTasksForCriteria(bean);
			tasks = con.getPageResults();
			int lastPageRecordCount = recordsToInsert
					- (pages * SmsHibernateConstants.DEFAULT_PAGE_SIZE);
			assertTrue("Incorrect collection size returned",
					tasks.size() == lastPageRecordCount);

		} catch (Exception se) {
			fail(se.getMessage());
		}
	}

	/**
	 * Tests the incrementing of messages processed and the updating of the
	 * tasks status to complete.
	 */
	public void testMessagesProcessedAndSetCompleteTasks() {
		SmsTask testTask = createTestTask();
		testTask.setGroupSizeActual(10);
		HibernateLogicFactory.getTaskLogic().persistSmsTask(testTask);

		for (int i = 0; i < 10; i++) {
			HibernateLogicFactory.getTaskLogic().incrementMessagesProcessed(
					testTask);
		}

		assertTrue(HibernateLogicFactory.getTaskLogic().getSmsTask(
				testTask.getId()).getMessagesProcessed() == 10);

		HibernateLogicFactory.getTaskLogic().checkAndSetTasksCompleted();
		assertTrue(HibernateLogicFactory.getTaskLogic().getSmsTask(
				testTask.getId()).getStatusCode().equals(
				SmsConst_DeliveryStatus.STATUS_TASK_COMPLETED));

		HibernateLogicFactory.getTaskLogic().deleteSmsTask(testTask);

	}

	/**
	 * Test insert sms task.
	 */
	public void testInsertSmsTask() {
		SmsTask testTask = createTestTask();
		HibernateLogicFactory.getTaskLogic().persistSmsTask(testTask);
		assertTrue("Object not persisted", testTask.exists());
	}

	/**
	 * Test remove sms messages from task.
	 */
	public void testRemoveSmsMessagesFromTask() {

		SmsTask taskWithCollections = createTestTask();
		taskWithCollections.setSakaiSiteId("oldSakaiSiteId");

		SmsMessage testMessage1 = createTestMessage1();
		testMessage1.setSmsTask(taskWithCollections);
		SmsMessage testMessage2 = createTestMessage2();
		testMessage2.setSmsTask(taskWithCollections);

		taskWithCollections.getSmsMessages().add(testMessage1);
		taskWithCollections.getSmsMessages().add(testMessage2);

		HibernateLogicFactory.getTaskLogic()
				.persistSmsTask(taskWithCollections);
		SmsTask getSmsTask = HibernateLogicFactory.getTaskLogic().getSmsTask(
				taskWithCollections.getId());

		assertEquals("Collection size not correct", 2, getSmsTask
				.getSmsMessages().size());

		getSmsTask.getSmsMessages().remove(testMessage1);
		HibernateLogicFactory.getTaskLogic().persistSmsTask(getSmsTask);
		getSmsTask = HibernateLogicFactory.getTaskLogic().getSmsTask(
				getSmsTask.getId());

		assertTrue("Object not removed from collection", getSmsTask
				.getSmsMessages().size() == 1);
		// Check the right object was removed
		assertFalse("The expected object was not removed from the collection",
				getSmsTask.getSmsMessages().contains(testMessage1));
		assertTrue("The incorrect object was removed from the collection",
				getSmsTask.getSmsMessages().contains(testMessage2));
	}

	/**
	 * Test update sms task.
	 */
	public void testUpdateSmsTask() {

		SmsTask testTask = createTestTask();
		HibernateLogicFactory.getTaskLogic().persistSmsTask(testTask);
		SmsTask smsTask = HibernateLogicFactory.getTaskLogic().getSmsTask(
				testTask.getId());
		assertFalse(smsTask.getSakaiSiteId().equals("newSakaiSiteId"));
		smsTask.setSakaiSiteId("newSakaiSiteId");
		HibernateLogicFactory.getTaskLogic().persistSmsTask(smsTask);
		smsTask = HibernateLogicFactory.getTaskLogic().getSmsTask(
				smsTask.getId());
		assertEquals("newSakaiSiteId", smsTask.getSakaiSiteId());
	}

	public void testSaveDeliveryMobileNumbers() throws Exception {

		SmsTask taskToSave = createTestTask();
		Set<String> mobileNumbers = new HashSet<String>();
		// check null assignment
		taskToSave.setDeliveryMobileNumbersSet(null);

		mobileNumbers.add(MOBILE_NUMBER_1);
		mobileNumbers.add(MOBILE_NUMBER_2);
		mobileNumbers.add(MOBILE_NUMBER_3);
		taskToSave.setDeliveryMobileNumbersSet(mobileNumbers);

		HibernateLogicFactory.getTaskLogic().persistSmsTask(taskToSave);

		SmsTask taskReturned = HibernateLogicFactory.getTaskLogic().getSmsTask(
				taskToSave.getId());

		assertEquals("Three mobile numbers should have been obtained", 3,
				taskReturned.getDeliveryMobileNumbersSet().size());
		assertTrue(taskReturned.getDeliveryMobileNumbersSet().contains(
				MOBILE_NUMBER_1));
		assertTrue(taskReturned.getDeliveryMobileNumbersSet().contains(
				MOBILE_NUMBER_2));
		assertTrue(taskReturned.getDeliveryMobileNumbersSet().contains(
				MOBILE_NUMBER_3));
	}
}
