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
package org.sakaiproject.sms.tool.test;

//import java.sql.Timestamp;
//import java.util.Date;
//
//import org.sakaiproject.sms.logic.smpp.impl.SmsBillingImpl;
//import org.sakaiproject.sms.logic.smpp.impl.SmsCoreImpl;
//import org.sakaiproject.sms.logic.smpp.validate.SmsTaskValidatorImpl;
//import org.sakaiproject.sms.logic.stubs.ExternalLogicStub;
//import org.sakaiproject.sms.model.hibernate.SmsAccount;
//import org.sakaiproject.sms.model.hibernate.SmsMessage;
//import org.sakaiproject.sms.model.hibernate.SmsTask;
//import org.sakaiproject.sms.model.hibernate.constants.SmsConst_DeliveryStatus;
//import org.sakaiproject.sms.model.hibernate.constants.SmsHibernateConstants;
//import org.sakaiproject.sms.model.hibernate.constants.ValidationConstants;
//import org.sakaiproject.sms.tool.validators.SmsMessageValidator;
//import org.sakaiproject.sms.tool.validators.SmsTaskValidator;
//import org.sakaiproject.sms.util.AbstractBaseTestCase;
//import org.sakaiproject.sms.util.TestHibernateUtil;
//import org.springframework.validation.BindException;
//
//// TODO: Auto-generated Javadoc
///**
// * The Class SmsMessageValidationTest. Runs tests for {@link SmsMessage}
// * validation that is run by {@link SmsMessageValidator}
// */
//public class SmsTaskValidationTest extends AbstractBaseTestCase {
//
//	/** The validator. */
//	private SmsTaskValidator validator;
//
//	/** The errors. */
//	private BindException errors;
//
//	/** The sms task. */
//	private SmsTask smsTask;
//
//	/** The msg. */
//	private SmsMessage msg;
//
//	/** The VALI d_ ms g_ body. */
//	private static String VALID_MSG_BODY = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.";
//
//	static {
//		TestHibernateUtil.createSchema();
//	}
//
//	/** The account. */
//	private SmsAccount account;
//
//	/**
//	 * setUp to run before every test. Create SmsMessage + validator + errors
//	 *
//	 * @see junit.framework.TestCase#setUp()
//	 */
//	@Override
//	public void setUp() {
//
//		// Inject the required impl's into core impl for testing
//		SmsCoreImpl smsCoreImpl = new SmsCoreImpl();
//		SmsBillingImpl smsBilling = new SmsBillingImpl();
//		smsBilling.setHibernateLogicLocator(hibernateLogicLocator);
//		smsCoreImpl.smsBilling = smsBilling;
//		smsCoreImpl.setHibernateLogicLocator(hibernateLogicLocator);
//		hibernateLogicLocator.setExternalLogic(new ExternalLogicStub());
//
//		account = new SmsAccount();
//		account.setSakaiSiteId("sakaiSiteId");
//		account.setMessageTypeCode("");
//		account.setCredits(10L);
//		account.setAccountName("account name");
//		account.setStartdate(new Date());
//		account.setAccountEnabled(true);
//		hibernateLogicLocator.getSmsAccountLogic().persistSmsAccount(account);
//
//		validator = new SmsTaskValidator();
//
//		validator.setSmsTaskValidator(new SmsTaskValidatorImpl());
//		msg = new SmsMessage();
//		smsTask = smsCoreImpl.getPreliminaryTestTask();
//
//		smsTask.setSakaiSiteId("sakaiSiteId");
//		smsTask.setSmsAccountId(account.getId());
//		smsTask.setDateCreated(new Timestamp(System.currentTimeMillis()));
//		smsTask.setDateToSend(new Timestamp(System.currentTimeMillis()));
//		smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
//		smsTask.setAttemptCount(2);
//		smsTask.setMessageBody("messageBody");
//		smsTask.setSenderUserName("senderUserName");
//		smsTask.setMaxTimeToLive(1);
//		smsTask.setDelReportTimeoutDuration(1);
//		smsTask.getSmsMessages().add(msg);
//		smsTask.setCreditEstimate(5);
//		smsTask.setDeliveryGroupId("delGrpId");
//		msg.setSmsTask(smsTask);
//		errors = new BindException(smsTask, "SmsTask");
//
//	}
//
//	@Override
//	public void tearDown() {
//		hibernateLogicLocator.getSmsAccountLogic().deleteSmsAccount(account);
//	}
//
//	/**
//	 * Test account id.
//	 */
//	public void testAccountId() {
//
//		// account exists
//		smsTask.setSmsAccountId(account.getId());
//		validator.validate(smsTask, errors);
//		assertFalse(errors.hasGlobalErrors());
//
//	}
//
//	/**
//	 * Test date created.
//	 */
//	public void testDateCreated() {
//
//		// null
//		smsTask.setDateCreated(null);
//		validator.validate(smsTask, errors);
//		assertTrue(errors.hasGlobalErrors());
//		assertEquals(ValidationConstants.TASK_DATE_CREATED_EMPTY, errors
//				.getGlobalError().getCode());
//	}
//
//	/**
//	 * Test date to send.
//	 */
//	public void testDateToSend() {
//
//		// null
//		smsTask.setDateToSend(null);
//		validator.validate(smsTask, errors);
//		assertTrue(errors.hasGlobalErrors());
//		assertEquals(ValidationConstants.TASK_DATE_TO_SEND_EMPTY, errors
//				.getGlobalError().getCode());
//	}
//
//	/**
//	 * Test delivery group id.
//	 */
//	public void testDeliveryGroupId() {
//		// null
//		smsTask.setDeliveryGroupId(null);
//		validator.validate(smsTask, errors);
//		assertTrue(errors.hasGlobalErrors());
//		assertEquals(ValidationConstants.TASK_DELIVERY_GROUP_ID_EMPTY, errors
//				.getGlobalError().getCode());
//
//		// empty String
//		smsTask.setDeliveryGroupId("");
//		validator.validate(smsTask, errors);
//		assertTrue(errors.hasGlobalErrors());
//		assertEquals(ValidationConstants.TASK_DELIVERY_GROUP_ID_EMPTY, errors
//				.getGlobalError().getCode());
//
//		// Blank space
//		smsTask.setDeliveryGroupId("   ");
//		validator.validate(smsTask, errors);
//		assertTrue(errors.hasGlobalErrors());
//		assertEquals(ValidationConstants.TASK_DELIVERY_GROUP_ID_EMPTY, errors
//				.getGlobalError().getCode());
//	}
//
//	/**
//	 * Test delivery report timeout duration.
//	 */
//	public void testDelReportTimeoutDuration() {
//		// null
//		smsTask.setDelReportTimeoutDuration(null);
//		validator.validate(smsTask, errors);
//		assertTrue(errors.hasGlobalErrors());
//		assertEquals(ValidationConstants.TASK_DELIVERY_REPORT_TIMEOUT_INVALID,
//				errors.getGlobalError().getCode());
//
//		// invalid
//		smsTask.setDelReportTimeoutDuration(0);
//		validator.validate(smsTask, errors);
//		assertTrue(errors.hasGlobalErrors());
//		assertEquals(ValidationConstants.TASK_DELIVERY_REPORT_TIMEOUT_INVALID,
//				errors.getGlobalError().getCode());
//	}
//
//	/**
//	 * Test max time to live.
//	 */
//	public void testMaxTimeToLive() {
//		// null
//		smsTask.setMaxTimeToLive(null);
//		validator.validate(smsTask, errors);
//		assertTrue(errors.hasGlobalErrors());
//		assertEquals(ValidationConstants.TASK_MAX_TIME_TO_LIVE_INVALID, errors
//				.getGlobalError().getCode());
//
//		// invalid
//		smsTask.setMaxTimeToLive(0);
//		validator.validate(smsTask, errors);
//		assertTrue(errors.hasGlobalErrors());
//		assertEquals(ValidationConstants.TASK_MAX_TIME_TO_LIVE_INVALID, errors
//				.getGlobalError().getCode());
//	}
//
//	/**
//	 * Test empty message body.
//	 */
//	public void testMessageBody_empty() {
//		smsTask.setMessageBody("");
//		validator.validate(smsTask, errors);
//		assertTrue(errors.hasGlobalErrors());
//		assertEquals(ValidationConstants.MESSAGE_BODY_EMPTY, errors
//				.getGlobalError().getCode());
//	}
//
//	/**
//	 * Test null message body.
//	 */
//	public void testMessageBody_null() {
//		smsTask.setMessageBody(null);
//		validator.validate(smsTask, errors);
//		assertTrue(errors.hasGlobalErrors());
//		assertEquals(ValidationConstants.MESSAGE_BODY_EMPTY, errors
//				.getGlobalError().getCode());
//	}
//
//	/**
//	 * Test too long message body.
//	 */
//	public void testMessageBody_tooLong() {
//		smsTask.setMessageBody(VALID_MSG_BODY + VALID_MSG_BODY);
//		assertTrue(msg.getMessageBody().length() > SmsHibernateConstants.MAX_SMS_LENGTH);
//		validator.validate(smsTask, errors);
//		assertTrue(errors.hasGlobalErrors());
//		assertEquals(ValidationConstants.MESSAGE_BODY_TOO_LONG, errors
//				.getGlobalError().getCode());
//	}
//
//	/**
//	 * Test empty message body (with whitespace).
//	 */
//	public void testMessageBody_whitespace() {
//		smsTask.setMessageBody("   ");
//		validator.validate(smsTask, errors);
//		assertTrue(errors.hasGlobalErrors());
//		assertEquals(ValidationConstants.MESSAGE_BODY_EMPTY, errors
//				.getGlobalError().getCode());
//	}
//
//	/**
//	 * Test message type id.
//	 */
//	public void testMessageTypeId() {
//		// null
//		smsTask.setMessageTypeId(null);
//		validator.validate(smsTask, errors);
//		assertTrue(errors.hasGlobalErrors());
//		assertEquals(ValidationConstants.TASK_MESSAGE_TYPE_EMPTY, errors
//				.getGlobalError().getCode());
//	}
//
//	/**
//	 * Test sakai site id.
//	 */
//	public void testSakaiSiteId() {
//
//		// null
//		smsTask.setSakaiSiteId(null);
//		validator.validate(smsTask, errors);
//		assertTrue(errors.hasGlobalErrors());
//		assertEquals(ValidationConstants.TASK_SAKAI_SITE_ID_EMPTY, errors
//				.getGlobalError().getCode());
//
//		// empty String
//		smsTask.setSakaiSiteId("");
//		validator.validate(smsTask, errors);
//		assertTrue(errors.hasGlobalErrors());
//		assertEquals(ValidationConstants.TASK_SAKAI_SITE_ID_EMPTY, errors
//				.getGlobalError().getCode());
//
//		// Blank space
//		smsTask.setSakaiSiteId("   ");
//		validator.validate(smsTask, errors);
//		assertTrue(errors.hasGlobalErrors());
//		assertEquals(ValidationConstants.TASK_SAKAI_SITE_ID_EMPTY, errors
//				.getGlobalError().getCode());
//	}
//
//	/**
//	 * Test sender user name.
//	 */
//	public void testSenderUserName() {
//
//		// null
//		smsTask.setSenderUserName(null);
//		validator.validate(smsTask, errors);
//		assertTrue(errors.hasGlobalErrors());
//		assertEquals(ValidationConstants.TASK_SENDER_USER_NAME_EMPTY, errors
//				.getGlobalError().getCode());
//
//		// empty String
//		smsTask.setSenderUserName("");
//		validator.validate(smsTask, errors);
//		assertTrue(errors.hasGlobalErrors());
//		assertEquals(ValidationConstants.TASK_SENDER_USER_NAME_EMPTY, errors
//				.getGlobalError().getCode());
//
//		// Blank space
//		smsTask.setSenderUserName("   ");
//		validator.validate(smsTask, errors);
//		assertTrue(errors.hasGlobalErrors());
//		assertEquals(ValidationConstants.TASK_SENDER_USER_NAME_EMPTY, errors
//				.getGlobalError().getCode());
//	}
//
//	/**
//	 * Test status code.
//	 */
//	public void testStatusCode() {
//
//		// null
//		smsTask.setStatusCode(null);
//		validator.validate(smsTask, errors);
//		assertTrue(errors.hasGlobalErrors());
//		assertEquals(ValidationConstants.TASK_STATUS_CODE_EMPTY, errors
//				.getGlobalError().getCode());
//
//		// empty String
//		smsTask.setStatusCode("");
//		validator.validate(smsTask, errors);
//		assertTrue(errors.hasGlobalErrors());
//		assertEquals(ValidationConstants.TASK_STATUS_CODE_EMPTY, errors
//				.getGlobalError().getCode());
//
//		// Blank space
//		smsTask.setStatusCode("   ");
//		validator.validate(smsTask, errors);
//		assertTrue(errors.hasGlobalErrors());
//		assertEquals(ValidationConstants.TASK_STATUS_CODE_EMPTY, errors
//				.getGlobalError().getCode());
//	}
//
//	/**
//	 * Test valid message.
//	 */
//	public void testValidMessage() {
//		validator.validate(smsTask, errors);
//		assertFalse(errors.hasErrors());
//		assertFalse(errors.hasGlobalErrors());
//	}
//
//	@Override
//	public void testOnetimeSetup() {
//		// TODO Auto-generated method stub
//
//	}
//
//}

public class SmsTaskValidationTest {
	
	// TODO: FIX ME
}
