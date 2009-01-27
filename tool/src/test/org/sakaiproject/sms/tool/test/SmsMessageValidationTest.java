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

import org.sakaiproject.sms.model.hibernate.SmsAccount;
import org.sakaiproject.sms.model.hibernate.SmsMessage;
import org.sakaiproject.sms.model.hibernate.SmsTask;
import org.sakaiproject.sms.model.hibernate.constants.SmsConst_DeliveryStatus;
import org.sakaiproject.sms.model.hibernate.constants.SmsHibernateConstants;
import org.sakaiproject.sms.model.hibernate.constants.ValidationConstants;
import org.sakaiproject.sms.tool.validators.SmsMessageValidator;
import org.sakaiproject.sms.util.AbstractBaseTestCase;
import org.sakaiproject.sms.util.HibernateUtil;
import org.springframework.validation.BindException;

// TODO: Auto-generated Javadoc
/**
 * The Class SmsMessageValidationTest. Runs tests for {@link SmsMessage}
 * validation that is run by {@link SmsMessageValidator}
 */
public class SmsMessageValidationTest extends AbstractBaseTestCase {

	/** The validator. */
	private SmsMessageValidator validator;

	/** The errors. */
	private BindException errors;

	/** The sms task. */
	private SmsTask smsTask;

	/** The msg. */
	private SmsMessage msg;

	/** The VALI d_ ms g_ body. */
	private static String VALID_MSG_BODY = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.";

	static {
		HibernateUtil.createSchema();
	}

	/** The account. */
	private SmsAccount account;

	/**
	 * setUp to run before every test. Create SmsMessage + validator + errors
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	public void setUp() {

		msg = new SmsMessage();
		msg.setSakaiUserId("sakaiUserId");
		msg.setStatusCode(SmsConst_DeliveryStatus.STATUS_DELIVERED);
		msg.setSmsTask(new SmsTask());
		msg.setMobileNumber("072 1889 987");
		errors = new BindException(msg, "SmsMessage");
		validator = new SmsMessageValidator();
	}

	/**
	 * Test valid message.
	 */
	public void testValidMessage() {
		validator.validate(msg, errors);
		assertFalse(errors.hasErrors());
	}

	/**
	 * Test mobile number_empty.
	 */
	public void testMobileNumber_empty() {
		msg.setMobileNumber("");
		validator.validate(msg, errors);
		assertTrue(errors.hasGlobalErrors());
		assertEquals(ValidationConstants.MOBILE_NUMBER_EMPTY, errors
				.getGlobalError().getCode());
	}

	/**
	 * Test mobile number_invalid.
	 */
	public void testMobileNumber_invalid() {
		msg.setMobileNumber("this is text");
		validator.validate(msg, errors);
		assertTrue(errors.hasGlobalErrors());
		assertEquals(ValidationConstants.MOBILE_NUMBER_INVALID, errors
				.getGlobalError().getCode());
	}

	/**
	 * Test mobile number_invalid plus location.
	 */
	public void testMobileNumber_invalidPlusLocation() {
		msg.setMobileNumber("012345+678");
		validator.validate(msg, errors);
		assertTrue(errors.hasGlobalErrors());
		assertEquals(ValidationConstants.MOBILE_NUMBER_INVALID, errors
				.getGlobalError().getCode());
	}

	/**
	 * Test mobile number_null.
	 */
	public void testMobileNumber_null() {
		msg.setMobileNumber(null);
		validator.validate(msg, errors);
		assertTrue(errors.hasGlobalErrors());
		assertEquals(ValidationConstants.MOBILE_NUMBER_EMPTY, errors
				.getGlobalError().getCode());
	}

	/**
	 * Test mobile number_too long.
	 */
	public void testMobileNumber_tooLong() {
		msg.setMobileNumber("012345678901234567890123456789");
		assertTrue(msg.getMobileNumber().length() > SmsHibernateConstants.MAX_MOBILE_NR_LENGTH);
		validator.validate(msg, errors);
		assertTrue(errors.hasGlobalErrors());
		assertEquals(ValidationConstants.MOBILE_NUMBER_TOO_LONG, errors
				.getGlobalError().getCode());
	}

	/**
	 * Test mobile number_valid with plus.
	 */
	public void testMobileNumber_validWithPlus() {
		msg.setMobileNumber("+2712 345 6789");
		validator.validate(msg, errors);
		assertFalse(errors.hasErrors());
	}

	/**
	 * Test mobile number_valid with whitepsace.
	 */
	public void testMobileNumber_validWithWhitepsace() {
		msg.setMobileNumber(" 012 345 6785 ");
		validator.validate(msg, errors);
		assertFalse(errors.hasErrors());
	}

	/**
	 * Test sakai user id.
	 */
	public void testSakaiUserId() {

		// null
		msg.setSakaiUserId(null);
		validator.validate(msg, errors);
		assertTrue(errors.hasGlobalErrors());
		assertEquals(ValidationConstants.MESSAGE_SAKAI_USER_ID_EMPTY, errors
				.getGlobalError().getCode());

		// empty String
		msg.setSakaiUserId("");
		validator.validate(msg, errors);
		assertTrue(errors.hasGlobalErrors());
		assertEquals(ValidationConstants.MESSAGE_SAKAI_USER_ID_EMPTY, errors
				.getGlobalError().getCode());

		// Blank space
		msg.setSakaiUserId("   ");
		validator.validate(msg, errors);
		assertTrue(errors.hasGlobalErrors());
		assertEquals(ValidationConstants.MESSAGE_SAKAI_USER_ID_EMPTY, errors
				.getGlobalError().getCode());
	}

	/**
	 * Test status code.
	 */
	public void testStatusCode() {

		// null
		msg.setStatusCode(null);
		validator.validate(msg, errors);
		assertTrue(errors.hasGlobalErrors());
		assertEquals(ValidationConstants.MESSAGE_STATUS_CODE_EMPTY, errors
				.getGlobalError().getCode());

		// empty String
		msg.setStatusCode("");
		validator.validate(msg, errors);
		assertTrue(errors.hasGlobalErrors());
		assertEquals(ValidationConstants.MESSAGE_STATUS_CODE_EMPTY, errors
				.getGlobalError().getCode());

		// Blank space
		msg.setStatusCode("   ");
		validator.validate(msg, errors);
		assertTrue(errors.hasGlobalErrors());
		assertEquals(ValidationConstants.MESSAGE_STATUS_CODE_EMPTY, errors
				.getGlobalError().getCode());
	}

	/**
	 * Test sms task.
	 */
	public void testSmsTask() {

		msg.setSmsTask(null);
		validator.validate(msg, errors);
		assertTrue(errors.hasGlobalErrors());
		assertEquals(ValidationConstants.MESSAGE_TASK_ID_EMPTY, errors
				.getGlobalError().getCode());

	}

}
