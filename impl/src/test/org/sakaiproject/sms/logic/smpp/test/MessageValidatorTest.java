package org.sakaiproject.sms.logic.smpp.test;

import java.util.ArrayList;

import org.sakaiproject.sms.logic.smpp.validate.MessageValidator;
import org.sakaiproject.sms.model.hibernate.SmsMessage;
import org.sakaiproject.sms.model.hibernate.SmsTask;
import org.sakaiproject.sms.model.hibernate.constants.SmsConst_DeliveryStatus;
import org.sakaiproject.sms.model.hibernate.constants.SmsHibernateConstants;
import org.sakaiproject.sms.model.hibernate.constants.ValidationConstants;
import org.sakaiproject.sms.util.AbstractBaseTestCase;
import org.sakaiproject.sms.util.TestHibernateUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class SmsMessageValidationTest. Runs tests for {@link SmsMessage}
 * validation
 */
public class MessageValidatorTest extends AbstractBaseTestCase {

	/** The msg. */
	private SmsMessage msg;

	static {
		TestHibernateUtil.createSchema();
	}

	/** The errors. */
	ArrayList<String> errors = new ArrayList<String>();

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
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.sms.util.AbstractBaseTestCase#testOnetimeSetup()
	 */
	@Override
	public void testOnetimeSetup() {
		// No need to set call createSchema for this test
	}

	/**
	 * Test valid message.
	 */
	public void testValidMessage() {
		errors = MessageValidator.validateMessage(msg);
		assertTrue(errors.size() == 0);
	}

	/**
	 * Test mobile number_empty.
	 */
	public void testMobileNumber_empty() {
		msg.setMobileNumber("");
		errors = MessageValidator.validateMessage(msg);
		assertTrue(errors.size() > 0);
		assertTrue(errors.contains(ValidationConstants.MOBILE_NUMBER_EMPTY));
	}

	/**
	 * Test mobile number_invalid.
	 */
	public void testMobileNumber_invalid() {
		msg.setMobileNumber("this is text");
		errors = MessageValidator.validateMessage(msg);
		assertTrue(errors.size() > 0);
		assertTrue(errors.contains(ValidationConstants.MOBILE_NUMBER_INVALID));
	}

	/**
	 * Test mobile number_invalid plus location.
	 */
	public void testMobileNumber_invalidPlusLocation() {
		msg.setMobileNumber("012345+678");
		errors = MessageValidator.validateMessage(msg);
		assertTrue(errors.size() > 0);
		assertTrue(errors.contains(ValidationConstants.MOBILE_NUMBER_INVALID));
	}

	/**
	 * Test mobile number_null.
	 */
	public void testMobileNumber_null() {
		msg.setMobileNumber(null);
		errors = MessageValidator.validateMessage(msg);
		assertTrue(errors.size() > 0);
		assertTrue(errors.contains(ValidationConstants.MOBILE_NUMBER_EMPTY));
	}

	/**
	 * Test mobile number_too long.
	 */
	public void testMobileNumber_tooLong() {
		msg.setMobileNumber("012345678901234567890123456789");
		assertTrue(msg.getMobileNumber().length() > SmsHibernateConstants.MAX_MOBILE_NR_LENGTH);
		errors = MessageValidator.validateMessage(msg);
		assertTrue(errors.size() > 0);
		assertTrue(errors.contains(ValidationConstants.MOBILE_NUMBER_TOO_LONG));
	}

	/**
	 * Test mobile number_valid with plus.
	 */
	public void testMobileNumber_validWithPlus() {
		msg.setMobileNumber("+2712 345 6789");
		errors = MessageValidator.validateMessage(msg);
		assertTrue(errors.size() == 0);
	}

	/**
	 * Test mobile number_valid with whitepsace.
	 */
	public void testMobileNumber_validWithWhitepsace() {
		msg.setMobileNumber(" 012 345 6785 ");
		errors = MessageValidator.validateMessage(msg);
		assertTrue(errors.size() == 0);
	}

	/**
	 * Test sakai user id.
	 */
	public void testSakaiUserId() {

		// null
		msg.setSakaiUserId(null);
		errors = MessageValidator.validateMessage(msg);
		assertTrue(errors.size() > 0);
		assertTrue(errors
				.contains(ValidationConstants.MESSAGE_SAKAI_USER_ID_EMPTY));

		// empty String
		msg.setSakaiUserId("");
		errors = MessageValidator.validateMessage(msg);
		assertTrue(errors.size() > 0);
		assertTrue(errors
				.contains(ValidationConstants.MESSAGE_SAKAI_USER_ID_EMPTY));

		// Blank space
		msg.setSakaiUserId("   ");
		errors = MessageValidator.validateMessage(msg);
		assertTrue(errors.size() > 0);
		assertTrue(errors
				.contains(ValidationConstants.MESSAGE_SAKAI_USER_ID_EMPTY));
	}

	/**
	 * Test status code.
	 */
	public void testStatusCode() {

		// null
		msg.setStatusCode(null);
		errors = MessageValidator.validateMessage(msg);
		assertTrue(errors.size() > 0);
		assertTrue(errors
				.contains(ValidationConstants.MESSAGE_STATUS_CODE_EMPTY));

		// empty String
		msg.setStatusCode("");
		errors = MessageValidator.validateMessage(msg);
		assertTrue(errors.size() > 0);
		assertTrue(errors
				.contains(ValidationConstants.MESSAGE_STATUS_CODE_EMPTY));

		// Blank space
		msg.setStatusCode("   ");
		errors = MessageValidator.validateMessage(msg);
		assertTrue(errors.size() > 0);
		assertTrue(errors
				.contains(ValidationConstants.MESSAGE_STATUS_CODE_EMPTY));
	}

	/**
	 * Test sms task.
	 */
	public void testSmsTask() {

		msg.setSmsTask(null);
		errors = MessageValidator.validateMessage(msg);
		assertTrue(errors.size() > 0);
		assertTrue(errors.contains(ValidationConstants.MESSAGE_TASK_ID_EMPTY));

	}

}