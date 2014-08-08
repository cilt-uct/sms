/***********************************************************************************
 * SmsMessageValidationTest.java
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
package org.sakaiproject.sms.util;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.sakaiproject.sms.logic.smpp.impl.SmsBillingImpl;
import org.sakaiproject.sms.logic.smpp.impl.SmsCoreImpl;
import org.sakaiproject.sms.logic.smpp.validate.SmsTaskValidatorImpl;
import org.sakaiproject.sms.logic.stubs.ExternalLogicStub;
import org.sakaiproject.sms.model.SmsAccount;
import org.sakaiproject.sms.model.SmsMessage;
import org.sakaiproject.sms.model.SmsTask;
import org.sakaiproject.sms.model.constants.SmsConst_DeliveryStatus;
import org.sakaiproject.sms.model.constants.SmsConstants;
import org.sakaiproject.sms.model.constants.ValidationConstants;

// TODO: Auto-generated Javadoc
/**
 * The Class SmsTaskValidation test
 */
public class SmsTaskValidationTest extends AbstractBaseTestCase {

	/** The validator. */
	private SmsTaskValidatorImpl validator;

	/** The errors. */
	private List<String> errors;

	/** The sms task. */
	private SmsTask smsTask;

	/** The msg. */
	private SmsMessage msg;

	private int uniquenesIDNumber = 0;

	/** The VALI d_ ms g_ body. */
	private static String VALID_MSG_BODY = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.";

	/** The account. */
	private SmsAccount account;

	@BeforeClass
	public static void beforeClass(){
		if (!SmsConstants.isDbSchemaCreated) {
			smsDao.createSchema();
			SmsConstants.isDbSchemaCreated = true;
		}
	}

	/**
	 * setUp to run before every test. Create SmsMessage + validator + errors
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Before
	public void setUp() {
		uniquenesIDNumber++;
		// Inject the required impl's into core impl for testing
		SmsCoreImpl smsCoreImpl = new SmsCoreImpl();
		SmsBillingImpl smsBilling = new SmsBillingImpl();
		smsBilling.setHibernateLogicLocator(hibernateLogicLocator);
		smsCoreImpl.smsBilling = smsBilling;
		smsCoreImpl.setHibernateLogicLocator(hibernateLogicLocator);
		hibernateLogicLocator.setExternalLogic(new ExternalLogicStub());

		account = new SmsAccount();
		account.setSakaiSiteId("SmsTaskValidationTest"
				+ SmsConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID
				+ uniquenesIDNumber);
		account.setMessageTypeCode("");
		account.setCredits(10L);
		account.setAccountName("SmsTaskValidationTest"
				+ SmsConstants.SMS_DEV_DEFAULT_SAKAI_ACCOUNT_NAME
				+ uniquenesIDNumber);
		account.setStartdate(new Date());
		account.setAccountEnabled(true);
		hibernateLogicLocator.getSmsAccountLogic().persistSmsAccount(account);

		validator = new SmsTaskValidatorImpl();
		validator.setSmsBilling(smsBilling);

		msg = new SmsMessage();
		smsTask = smsCoreImpl.getPreliminaryTestTask(account.getSakaiSiteId(),
				SmsConstants.SMS_DEV_DEFAULT_SAKAI_USER_ID);

		smsTask.setSakaiSiteId(account.getSakaiSiteId());
		smsTask.setSmsAccountId(account.getId());
		smsTask.setDateCreated(new Timestamp(System.currentTimeMillis()));
		smsTask.setDateToSend(new Timestamp(System.currentTimeMillis()));
		smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		smsTask.setAttemptCount(2);
		smsTask.setMessageBody(SmsConstants.SMS_DEV_DEFAULT_SMS_MESSAGE_BODY);
		smsTask.setSenderUserName(SmsConstants.SMS_DEV_DEFAULT_SAKAI_USER_ID);
		smsTask.setMaxTimeToLive(1);
		smsTask.getSmsMessages().add(msg);
		smsTask.setCreditEstimate(5);
		smsTask.setDeliveryGroupId("delGrpId");
		msg.setSmsTask(smsTask);
		errors = new ArrayList<String>();
	}

	@After
	public void tearDown() {
		hibernateLogicLocator.getSmsAccountLogic().deleteSmsAccount(account);
	}

	/**
	 * Test account id.
	 */
    @Test
	public void testAccountId() {

		// account exists
		smsTask.setSmsAccountId(account.getId());
		errors = validator.validateInsertTask(smsTask);
		assertFalse(errors.size() > 0);

	}

	/**
	 * Test date created.
	 */
    @Test
	public void testDateCreated() {

		// null
		smsTask.setDateCreated(null);
		errors = validator.validateInsertTask(smsTask);
		assertTrue(errors.size() > 0);
		assertEquals(ValidationConstants.TASK_DATE_CREATED_EMPTY, errors.get(0));
	}

	/**
	 * Test date to send.
	 */
    @Test
	public void testDateToSend() {

		// null
		smsTask.setDateToSend(null);
		errors = validator.validateInsertTask(smsTask);
		assertTrue(errors.size() > 0);
		assertEquals(ValidationConstants.TASK_DATE_TO_SEND_EMPTY, errors.get(0));
	}

	/**
	 * Test max time to live.
	 */
    @Test
	public void testMaxTimeToLive() {
		// null
		smsTask.setMaxTimeToLive(null);
		errors = validator.validateInsertTask(smsTask);
		assertTrue(errors.size() > 0);
		assertEquals(ValidationConstants.TASK_MAX_TIME_TO_LIVE_INVALID, errors
				.get(0));

		// invalid
		smsTask.setMaxTimeToLive(0);
		errors = validator.validateInsertTask(smsTask);
		assertTrue(errors.size() > 0);
		assertEquals(ValidationConstants.TASK_MAX_TIME_TO_LIVE_INVALID, errors
				.get(0));
	}

	/**
	 * Test empty message body.
	 */
    @Test
	public void testMessageBody_empty() {
		smsTask.setMessageBody("");
		errors = validator.validateInsertTask(smsTask);
		assertTrue(errors.size() == 0);

	}

	/**
	 * Test null message body.
	 */
    @Test
	public void testMessageBody_null() {
		smsTask.setMessageBody(null);
		errors = validator.validateInsertTask(smsTask);
		assertTrue(errors.size() > 0);
		assertEquals(ValidationConstants.MESSAGE_BODY_EMPTY, errors.get(0));
	}

	/**
	 * Test too long message body.
	 */
    @Test
	public void testMessageBody_tooLong() {
		smsTask.setMessageBody(VALID_MSG_BODY + VALID_MSG_BODY);
		assertTrue(msg.getMessageBody().length() > SmsConstants.MAX_SMS_LENGTH);
		errors = validator.validateInsertTask(smsTask);
		assertTrue(errors.size() > 0);
		assertEquals(ValidationConstants.MESSAGE_BODY_TOO_LONG, errors.get(0));
	}

	/**
	 * Test empty message body (with whitespace).
	 */
    @Test
	public void testMessageBody_whitespace() {
		smsTask.setMessageBody("   ");
		errors = validator.validateInsertTask(smsTask);
		assertTrue(errors.size() == 0);

	}

	/**
	 * Test message type id.
	 */
    @Test
	public void testMessageTypeId() {
		// null
		smsTask.setMessageTypeId(null);
		errors = validator.validateInsertTask(smsTask);
		assertTrue(errors.size() > 0);
		assertEquals(ValidationConstants.TASK_MESSAGE_TYPE_EMPTY, errors.get(0));
	}

	/**
	 * Test sakai site id.
	 */
    @Test
	public void testSakaiSiteId() {

		// null
		smsTask.setSakaiSiteId(null);
		errors = validator.validateInsertTask(smsTask);
		assertTrue(errors.size() > 0);
		assertEquals(ValidationConstants.TASK_SAKAI_SITE_ID_EMPTY, errors
				.get(0));

		// empty String
		smsTask.setSakaiSiteId("");
		errors = validator.validateInsertTask(smsTask);
		assertTrue(errors.size() > 0);
		assertEquals(ValidationConstants.TASK_SAKAI_SITE_ID_EMPTY, errors
				.get(0));

		// Blank space
		smsTask.setSakaiSiteId("   ");
		errors = validator.validateInsertTask(smsTask);
		assertTrue(errors.size() > 0);
		assertEquals(ValidationConstants.TASK_SAKAI_SITE_ID_EMPTY, errors
				.get(0));
	}

	/**
	 * Test sender user name.
	 */
    @Test
	public void testSenderUserName() {

		// null
		smsTask.setSenderUserName(null);
		errors = validator.validateInsertTask(smsTask);
		assertTrue(errors.size() > 0);
		assertEquals(ValidationConstants.TASK_SENDER_USER_NAME_EMPTY, errors
				.get(0));

		// empty String
		smsTask.setSenderUserName("");
		errors = validator.validateInsertTask(smsTask);
		assertTrue(errors.size() > 0);
		assertEquals(ValidationConstants.TASK_SENDER_USER_NAME_EMPTY, errors
				.get(0));

		// Blank space
		smsTask.setSenderUserName("   ");
		errors = validator.validateInsertTask(smsTask);
		assertTrue(errors.size() > 0);
		assertEquals(ValidationConstants.TASK_SENDER_USER_NAME_EMPTY, errors
				.get(0));
	}

	/**
	 * Test status code.
	 */
    @Test
	public void testStatusCode() {

		// null
		smsTask.setStatusCode(null);
		errors = validator.validateInsertTask(smsTask);
		assertTrue(errors.size() > 0);
		assertEquals(ValidationConstants.TASK_STATUS_CODE_EMPTY, errors.get(0));

		// empty String
		smsTask.setStatusCode("");
		errors = validator.validateInsertTask(smsTask);
		assertTrue(errors.size() > 0);
		assertEquals(ValidationConstants.TASK_STATUS_CODE_EMPTY, errors.get(0));

		// Blank space
		smsTask.setStatusCode("   ");
		errors = validator.validateInsertTask(smsTask);
		assertTrue(errors.size() > 0);
		assertEquals(ValidationConstants.TASK_STATUS_CODE_EMPTY, errors.get(0));
	}

    @Test
	public void testExpiredBeforeSend() {
		smsTask.setDateToSend(new Date());
		Calendar cal = new GregorianCalendar();
		cal.add(Calendar.HOUR, -1);
		smsTask.setDateToExpire(cal.getTime());
		errors = validator.validateInsertTask(smsTask);
		assertTrue(errors.size() > 0);
		assertEquals(ValidationConstants.TASK_DATE_TO_SEND_AFTER_EXPIRY, errors.get(0));

		
	}
	
    @Test
	public void testExpiredEqualsSend() {
		Date date = new Date();
		smsTask.setDateToSend(date);
		smsTask.setDateToExpire(date);
		assertTrue(smsTask.getDateToExpire().equals(smsTask.getDateToSend()));
		errors = validator.validateInsertTask(smsTask);
		System.out.println("errors size:" + errors.size());
		
		assertTrue(errors.size() > 0);
		assertEquals(ValidationConstants.TASK_DATE_TO_SEND_EQUALS_EXPIRY, errors.get(0));

		
	}
	
	/**
	 * Test valid message.
	 */
    @Test
	public void testValidMessage() {
		errors = validator.validateInsertTask(smsTask);
		assertFalse(errors.size() > 0);
	}

}
