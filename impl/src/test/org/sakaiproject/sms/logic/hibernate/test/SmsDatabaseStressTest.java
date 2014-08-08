package org.sakaiproject.sms.logic.hibernate.test;

import java.util.Calendar;
import java.util.Date;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.sakaiproject.sms.model.SmsMessage;
import org.sakaiproject.sms.model.SmsTask;
import org.sakaiproject.sms.model.constants.SmsConst_DeliveryStatus;
import org.sakaiproject.sms.model.constants.SmsConstants;
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

	@BeforeClass
	public static void beforeClass(){
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
	}
    
    /**
     * Make sure the smsTask is persisted before every test.
     */
    @Before
    public void setup(){
        //reset the id
        smsTask.setId(null);
        hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);
    }
    
    /**
     * Make sure the smsTask is deleted after every test.
     */
    @After
    public void teardown(){
        hibernateLogicLocator.getSmsTaskLogic().deleteSmsTask(smsTask);
    }

	/**
	 * Test many messages insert.
	 */
    @Test
	public void testInsertManyMessages() {
		for (int i = 0; i < MESSAGECOUNT; i++) {
			final SmsMessage smsMessage = new SmsMessage();
			smsMessage.setMobileNumber("0823450983");
			smsMessage.setSmscMessageId("smscMessage_" + i);
			smsMessage.setSakaiUserId("sakaiUserId");
			smsMessage.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
			smsMessage.setSmsTask(smsTask);
			smsTask.getSmsMessages().add(smsMessage);
			hibernateLogicLocator.getSmsMessageLogic().persistSmsMessage(
					smsMessage);
		}

		assertTrue("Not all messages returned",
				smsTask.getSmsMessages().size() == MESSAGECOUNT);
        
        
		final SmsTask theSmsTask = hibernateLogicLocator.getSmsTaskLogic()
				.getSmsTask(smsTask.getId());
		assertNotNull("theSmsTask may not be null", theSmsTask);
		assertEquals("Message size not correct", MESSAGECOUNT, 
                hibernateLogicLocator.getSmsMessageLogic().getSmsMessagesForTask(theSmsTask.getId()).size());
        
        for(SmsMessage m: hibernateLogicLocator.getSmsMessageLogic().getAllSmsMessages()){
            hibernateLogicLocator.getSmsMessageLogic().deleteSmsMessage(m);
        }
	}
}
