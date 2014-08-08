package org.sakaiproject.sms.logic.hibernate.test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.PropertyValueException;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sakaiproject.sms.bean.SearchFilterBean;
import org.sakaiproject.sms.bean.SearchResultContainer;
import org.sakaiproject.sms.logic.exception.SmsSearchException;
import org.sakaiproject.sms.model.SmsMessage;
import org.sakaiproject.sms.model.SmsTask;
import org.sakaiproject.sms.model.constants.SmsConst_DeliveryStatus;
import org.sakaiproject.sms.model.constants.SmsConstants;
import org.sakaiproject.sms.util.AbstractBaseTestCase;
import static org.sakaiproject.sms.util.AbstractBaseTestCase.hibernateLogicLocator;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * A task consists of a series of rules and must be executed on the scheduled
 * date-time. For example: Send message x to group y at time z. When the task is
 * executed (real-time or scheduled), the corresponding messages will be
 * inserted into SMS_MESSAGE with status PENDING. As delivery reports come in,
 * the message statuses will be updated.
 */
public class SmsTaskTest extends AbstractBaseTestCase {

	private static final Log log = LogFactory.getLog(SmsTaskTest.class);
	private static final String MOBILE_NUMBER_1 = "082 345 6789";
	private static final String MOBILE_NUMBER_2 = "083 345 6789";
	private static final String MOBILE_NUMBER_3 = "084 345 6789";

	/** The insert task. */
	private static SmsTask insertTask;

	/** The insert message1. */
	@SuppressWarnings("unused")
	private static SmsMessage insertMessage1;

	/** The insert message2. */
	@SuppressWarnings("unused")
	private static SmsMessage insertMessage2;

	@BeforeClass
	public static void beforeClass(){
		if (!SmsConstants.isDbSchemaCreated) {
			smsDao.createSchema();
			SmsConstants.isDbSchemaCreated = true;
		}
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
		testTask.setSakaiSiteId(SmsConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID);
		testTask.setSmsAccountId(1l);
		testTask.setDateCreated(new Date(System.currentTimeMillis()));
		testTask.setDateToSend(new Date(System.currentTimeMillis()));
		testTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		testTask.setAttemptCount(2);
		testTask.setMessageBody(SmsConstants.SMS_DEV_DEFAULT_SMS_MESSAGE_BODY);
		testTask.setSenderUserName("senderUserName");
		testTask.setMaxTimeToLive(1);
		testTask.setSmsMessagesOnTask(null);

		Calendar cal = Calendar.getInstance();
		cal.setTime(testTask.getDateToSend());
		cal.add(Calendar.SECOND, testTask.getMaxTimeToLive());
		// TODO, DateToExpire must be set from the UI as well
		testTask.setDateToExpire(cal.getTime());

		testTask.setDeliveryEntityList(Arrays.asList("1234", "1235", "1236"));
		return testTask;

	}
    
    /**
     * Make sure the testTask and messages are persisted before every test.
     */
    @Before
    public void setup(){
        //reset the id
        insertTask.setId(null);
        insertMessage1.setId(null);
        insertMessage2.setId(null);
        
		insertMessage1.setSmsTask(insertTask);
		insertMessage2.setSmsTask(insertTask);

		Set<SmsMessage> messages = new HashSet<SmsMessage>();
		messages.add(insertMessage1);
		messages.add(insertMessage2);

		insertTask.setSmsMessagesOnTask(messages);
		hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(insertTask);
        hibernateLogicLocator.getSmsMessageLogic().persistSmsMessage(insertMessage1);
        hibernateLogicLocator.getSmsMessageLogic().persistSmsMessage(insertMessage2);
    }
    
    /**
     * Make sure the testTask and messages are deleted after every test.
     */
    @After
    public void teardown(){
        hibernateLogicLocator.getSmsMessageLogic().deleteSmsMessage(insertMessage2);
        hibernateLogicLocator.getSmsMessageLogic().deleteSmsMessage(insertMessage1);
        hibernateLogicLocator.getSmsTaskLogic().deleteSmsTask(insertTask);
    }

    @Test
	public void testAddLongMessage() {
		SmsTask longMessageTask = createTestTask();
		String text = "this is a message that will be way beyond the 160 character limit. We would not expect to be able to send it! It would be ridiculous to assume that users will never bee too verbose!";
		longMessageTask.setMessageBody(text);
		try {
			hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(longMessageTask);
			fail();
		}
		catch (IllegalArgumentException ex) {
			
		} 
		catch (Exception e) {
			fail();
		}
		
		SmsTask encodedTask = createTestTask();
		//this looks short but will encode above the limit
		String borderLine = "UCT supports the national Student Laptop Initiative which offers big savings on the latest laptops.  For more see www.icts.uct.ac.za | Student Laptop Initiative";
		encodedTask.setMessageBody(borderLine);
		try {
			hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(encodedTask);
			fail();
		}
		catch (IllegalArgumentException ex) {
			
		} 
		catch (Exception e) {
			fail();
		}
		
	}
	
	
	/**
	 * Test delete sms task.
	 */
    @Test
	public void testDeleteSmsTask() {
        try{
    		hibernateLogicLocator.getSmsTaskLogic().deleteSmsTask(insertTask);
            fail("Should throw integrity constraint violation exception");
        }catch(DataIntegrityViolationException e){
            //great
        }catch(Exception e){
            fail("Should be a DataIntegrityViolationException");
        }
        teardown();
		SmsTask getSmsTask = hibernateLogicLocator.getSmsTaskLogic()
				.getSmsTask(insertTask.getId());
		assertNull("Object not removed", getSmsTask);
	}

	/**
	 * Test get next sms task. Checks if it is the oldest task to process (Being
	 * the next task to process)
	 */
    @Test
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
    @Test
	public void testGetSmsTaskById() {
		SmsTask taskToPersist = createTestTask();
		taskToPersist.setDeliveryGroupName("Maths department");
		hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(taskToPersist);
		SmsTask returnedSmsTask = hibernateLogicLocator.getSmsTaskLogic()
				.getSmsTask(taskToPersist.getId());
		assertNotNull(returnedSmsTask);
		assertEquals(taskToPersist.getId(), returnedSmsTask.getId());
	}

	/**
	 * Test get sms tasks.
	 */
    @Test
	public void testGetSmsTasks() {
		List<SmsTask> tasks = hibernateLogicLocator.getSmsTaskLogic()
				.getAllSmsTask();
		assertNotNull("Returned list is null", tasks);
		assertTrue("No records returned", tasks.size() > 0);
	}

	/**
	 * Tests the getMessagesForCriteria method.
	 */
    @Test
	public void testGetTasksForCriteria() {
		SmsTask insertTask = new SmsTask();
		insertTask.setSakaiSiteId(SmsConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID);
		insertTask.setSmsAccountId(1l);
		insertTask.setDateCreated(new Date(System.currentTimeMillis()));
		insertTask.setDateToSend(new Date(System.currentTimeMillis()));
		insertTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		insertTask.setAttemptCount(2);
		insertTask.setMessageTypeId(0);
		insertTask.setMessageBody("taskCrit");
		insertTask.setSenderUserName("taskCrit");
		insertTask.setSakaiToolName("sakaiToolName");
		insertTask.setMaxTimeToLive(1);
		Calendar cal = Calendar.getInstance();
		cal.setTime(insertTask.getDateToSend());
		cal.add(Calendar.SECOND, insertTask.getMaxTimeToLive());
		// TODO, DateToExpire must be set from the UI as well
		insertTask.setDateToExpire(cal.getTime());

		try {
			hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(insertTask);

			SearchFilterBean bean = new SearchFilterBean();
			bean.setStatus(insertTask.getStatusCode());
			bean.setDateFrom(new Date());
			bean.setDateTo(new Date());
			bean.setToolName(insertTask.getSakaiToolName());
			bean.setSender(insertTask.getSenderUserName());

			List<SmsTask> tasks = hibernateLogicLocator.getSmsTaskLogic()
					.getPagedSmsTasksForCriteria(bean).getPageResults();
			assertTrue("Collection returned has no objects", tasks.size() > 0);
			assertEquals(1, tasks.size());

			for (SmsTask task : tasks) {
				// We know that only one task should be returned
				assertTrue(task.getId().equals(insertTask.getId()));
			}
		} catch (SmsSearchException se) {
			fail(se.getMessage());
		} finally {
			hibernateLogicLocator.getSmsTaskLogic().deleteSmsTask(insertTask);
		}
	}

	/**
	 * Test get tasks for criteria_ paging.
	 */
    @Test
	public void testGetTasksForCriteria_Paging() {

		int recordsToInsert = 93;

		for (int i = 0; i < recordsToInsert; i++) {
			SmsTask insertTask = new SmsTask();
			insertTask
					.setSakaiSiteId(SmsConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID);
			insertTask.setSmsAccountId(1l);
			insertTask.setDateCreated(new Date(System.currentTimeMillis()));
			insertTask.setDateToSend(new Date(System.currentTimeMillis()));
			insertTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_FAIL);
			insertTask.setAttemptCount(2);
			insertTask.setMessageTypeId(0);
			insertTask.setMessageBody("taskCrit");
			insertTask.setSenderUserName("taskCrit");
			insertTask.setSakaiToolName("sakaiToolName");
			insertTask.setMaxTimeToLive(1);
			Calendar cal = Calendar.getInstance();
			cal.setTime(insertTask.getDateToSend());
			cal.add(Calendar.SECOND, insertTask.getMaxTimeToLive());
			// TODO, DateToExpire must be set from the UI as well
			insertTask.setDateToExpire(cal.getTime());
			hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(insertTask);
		}

		try {

			SearchFilterBean bean = new SearchFilterBean();
			bean.setStatus(SmsConst_DeliveryStatus.STATUS_FAIL);
			bean.setDateFrom(new Date());
			bean.setDateTo(new Date());
			bean.setToolName("sakaiToolName");
			bean.setSender("taskCrit");

			bean.setCurrentPage(2);

			SearchResultContainer<SmsTask> con = hibernateLogicLocator
					.getSmsTaskLogic().getPagedSmsTasksForCriteria(bean);
			List<SmsTask> tasks = con.getPageResults();
			assertTrue("Incorrect collection size returned",
					tasks.size() == SmsConstants.DEFAULT_PAGE_SIZE);

			// Test last page. We know there are 124 records to this should
			// return a list of 4

			int pages = recordsToInsert / SmsConstants.DEFAULT_PAGE_SIZE;
			// set to last page
			if (recordsToInsert % SmsConstants.DEFAULT_PAGE_SIZE == 0) {
				bean.setCurrentPage(pages);
			} else {
				bean.setCurrentPage(pages + 1);
			}

			con = hibernateLogicLocator.getSmsTaskLogic()
					.getPagedSmsTasksForCriteria(bean);
			tasks = con.getPageResults();
			int lastPageRecordCount = recordsToInsert
					- (pages * SmsConstants.DEFAULT_PAGE_SIZE);
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
    @Test
	public void testMessagesProcessedAndGetTaskToMarkComplete() {

		// SmsTask testTask = createTestTask();
		// testTask.setGroupSizeActual(10);
		// hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(testTask);
		//
		// for (int i = 0; i < 10; i++) {
		// hibernateLogicLocator.getSmsTaskLogic().incrementMessagesProcessed(
		// testTask);
		// }
		//
		// assertTrue(hibernateLogicLocator.getSmsTaskLogic().getSmsTask(
		// testTask.getId()).getMessagesProcessed() == 10);
		// List<SmsTask> smsTasks = hibernateLogicLocator.getSmsTaskLogic()
		// .getTasksToMarkAsCompleted();
		//
		// assertTrue(smsTasks.get(0).getId().equals(testTask.getId()));
		//
		// hibernateLogicLocator.getSmsTaskLogic().deleteSmsTask(testTask);

	}

	/**
	 * Test insert sms task.
	 */
    @Test
	public void testInsertSmsTask() {
		SmsTask testTask = createTestTask();
		hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(testTask);
		assertTrue("Object not persisted", testTask.exists());
	}

	/**
	 * Test update sms task.
	 */
    @Test
	public void testUpdateSmsTask() {

		SmsTask testTask = createTestTask();
		hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(testTask);
		SmsTask smsTask = hibernateLogicLocator.getSmsTaskLogic().getSmsTask(
				testTask.getId());
		assertFalse(smsTask.getSakaiSiteId().equals("newSakaiSiteId"));
		smsTask.setSakaiSiteId("newSakaiSiteId");
		hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);
		smsTask = hibernateLogicLocator.getSmsTaskLogic().getSmsTask(
				smsTask.getId());
		assertEquals("newSakaiSiteId", smsTask.getSakaiSiteId());
	}

    @Test
	public void testSaveDeliveryMobileNumbers() throws Exception {

		SmsTask taskToSave = createTestTask();
		Set<String> mobileNumbers = new HashSet<String>();
		// check null assignment
		taskToSave.setDeliveryMobileNumbersSet(null);

		mobileNumbers.add(MOBILE_NUMBER_1);
		mobileNumbers.add(MOBILE_NUMBER_2);
		mobileNumbers.add(MOBILE_NUMBER_3);
		taskToSave.setDeliveryMobileNumbersSet(mobileNumbers);

		hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(taskToSave);

		SmsTask taskReturned = hibernateLogicLocator.getSmsTaskLogic()
				.getSmsTask(taskToSave.getId());

		assertEquals("Three mobile numbers should have been obtained", 3,
				taskReturned.getDeliveryMobileNumbersSet().size());
		assertTrue(taskReturned.getDeliveryMobileNumbersSet().contains(
				MOBILE_NUMBER_1));
		assertTrue(taskReturned.getDeliveryMobileNumbersSet().contains(
				MOBILE_NUMBER_2));
		assertTrue(taskReturned.getDeliveryMobileNumbersSet().contains(
				MOBILE_NUMBER_3));
	}
	
    @Test
	public void testSaveNullFields() {
		//this has a null site Id
		SmsTask task = new SmsTask();
		task.setMessageBody("Happy Birthday from the Vula Team at UCT!");
	    task.setDateCreated(new Date());
	    Calendar cal = new GregorianCalendar();
	    cal.set(Calendar.HOUR_OF_DAY, 9);
	    cal.set(Calendar.MINUTE, 0);
	    Date dateToSend = cal.getTime();
	    task.setDateToSend(dateToSend);
	    task.setSmsAccountId(Long.valueOf(1));
	    try {
	    	hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(task);
	    	fail("cant save with a null siteId");
	    }
	    catch (PropertyValueException e) {
	    	fail("cant save with a null siteId");
	    }
	    catch (IllegalArgumentException e) {
	    	//we ecpect this!
	    } 
	    catch (Exception e) {
			e.printStackTrace();
			fail("Unkown Exception throws");
		}
	    
		task = new SmsTask();
		task.setMessageBody("Happy Birthday from the Vula Team at UCT!");
	    task.setDateCreated(new Date());
	    task.setDateToSend(dateToSend);
	    task.setSakaiSiteId("~admin");
	    try {
	    	hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(task);
	    	fail("cant save with a null siteId");
	    }
	    catch (PropertyValueException e) {
	    	fail("cant save with a null siteId");
	    }
	    catch (IllegalArgumentException e) {
	    	//we expect this!
	    } 
	    catch (Exception e) {
			e.printStackTrace();
			fail("Unkown Exception throws");
		}
	}

}
