package org.sakaiproject.sms.logic.smpp.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.sakaiproject.sms.logic.smpp.validate.MessageValidator;
import org.sakaiproject.sms.logic.smpp.validate.MessageValidatorImpl;
import org.sakaiproject.sms.model.SmsMessage;
import org.sakaiproject.sms.model.SmsTask;
import org.sakaiproject.sms.model.constants.SmsConst_DeliveryStatus;
import org.sakaiproject.sms.model.constants.SmsConstants;
import org.sakaiproject.sms.model.constants.ValidationConstants;

// TODO: Auto-generated Javadoc
/**
 * The Class SmsMessageValidationTest. Runs tests for {@link SmsMessage}
 * validation
 */
public class MessageValidatorTest extends TestCase {

	/** The msg. */
	private SmsMessage msg;

	/** The errors. */
	List<String> errors = new ArrayList<String>();

	private final MessageValidator validator = new MessageValidatorImpl();

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

	/**
	 * Test valid message.
	 */
	public void testValidMessage() {
		errors = validator.validateMessage(msg);
		assertTrue(errors.size() == 0);
	}

	/**
	 * Test mobile number_empty.
	 */
	public void testMobileNumber_empty() {
		msg.setMobileNumber("");
		errors = validator.validateMessage(msg);
		assertTrue(errors.size() > 0);
		assertTrue(errors.contains(ValidationConstants.MOBILE_NUMBER_EMPTY));
	}

	/**
	 * Test mobile number_invalid.
	 */
	public void testMobileNumber_invalid() {
		msg.setMobileNumber("this is text");
		errors = validator.validateMessage(msg);
		assertTrue(errors.size() > 0);
		assertTrue(errors.contains(ValidationConstants.MOBILE_NUMBER_INVALID));
	}

	/**
	 * Test mobile number_invalid plus location.
	 */
	public void testMobileNumber_invalidPlusLocation() {
		msg.setMobileNumber("012345+678");
		errors = validator.validateMessage(msg);
		assertTrue(errors.size() > 0);
		assertTrue(errors.contains(ValidationConstants.MOBILE_NUMBER_INVALID));
	}

	/**
	 * Test mobile number_null.
	 */
	public void testMobileNumber_null() {
		msg.setMobileNumber(null);
		errors = validator.validateMessage(msg);
		assertTrue(errors.size() > 0);
		assertTrue(errors.contains(ValidationConstants.MOBILE_NUMBER_EMPTY));
	}

	/**
	 * Test mobile number_too long.
	 */
	public void testMobileNumber_tooLong() {
		msg.setMobileNumber("012345678901234567890123456789");
		assertTrue(msg.getMobileNumber().length() > SmsConstants.MAX_MOBILE_NR_LENGTH);
		errors = validator.validateMessage(msg);
		assertTrue(errors.size() > 0);
		assertTrue(errors.contains(ValidationConstants.MOBILE_NUMBER_TOO_LONG));
	}

	/**
	 * Test mobile number_valid with plus.
	 */
	public void testMobileNumber_validWithPlus() {
		msg.setMobileNumber("+2712 345 6789");
		errors = validator.validateMessage(msg);
		assertTrue(errors.size() == 0);
	}

	/**
	 * Test mobile number_valid with whitepsace.
	 */
	public void testMobileNumber_validWithWhitepsace() {
		msg.setMobileNumber(" 012 345 6785 ");
		errors = validator.validateMessage(msg);
		assertTrue(errors.size() == 0);
	}

	/**
	 * Test sakai user id.
	 */
	public void testSakaiUserId() {

		// null
		msg.setSakaiUserId(null);
		errors = validator.validateMessage(msg);
		assertTrue(errors.size() > 0);
		assertTrue(errors
				.contains(ValidationConstants.MESSAGE_SAKAI_USER_ID_EMPTY));

		// empty String
		msg.setSakaiUserId("");
		errors = validator.validateMessage(msg);
		assertTrue(errors.size() > 0);
		assertTrue(errors
				.contains(ValidationConstants.MESSAGE_SAKAI_USER_ID_EMPTY));

		// Blank space
		msg.setSakaiUserId("   ");
		errors = validator.validateMessage(msg);
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
		errors = validator.validateMessage(msg);
		assertTrue(errors.size() > 0);
		assertTrue(errors
				.contains(ValidationConstants.MESSAGE_STATUS_CODE_EMPTY));

		// empty String
		msg.setStatusCode("");
		errors = validator.validateMessage(msg);
		assertTrue(errors.size() > 0);
		assertTrue(errors
				.contains(ValidationConstants.MESSAGE_STATUS_CODE_EMPTY));

		// Blank space
		msg.setStatusCode("   ");
		errors = validator.validateMessage(msg);
		assertTrue(errors.size() > 0);
		assertTrue(errors
				.contains(ValidationConstants.MESSAGE_STATUS_CODE_EMPTY));
	}

	/**
	 * Test sms task.
	 */
	public void testSmsTask() {

		msg.setSmsTask(null);
		errors = validator.validateMessage(msg);
		assertTrue(errors.size() > 0);
		assertTrue(errors.contains(ValidationConstants.MESSAGE_TASK_ID_EMPTY));

	}

}