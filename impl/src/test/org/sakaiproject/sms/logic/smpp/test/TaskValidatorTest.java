package org.sakaiproject.sms.logic.smpp.test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.sakaiproject.sms.logic.smpp.impl.SmsBillingImpl;
import org.sakaiproject.sms.logic.smpp.impl.SmsCoreImpl;
import org.sakaiproject.sms.logic.smpp.validate.SmsTaskValidatorImpl;
import org.sakaiproject.sms.model.SmsAccount;
import org.sakaiproject.sms.model.SmsMessage;
import org.sakaiproject.sms.model.SmsTask;
import org.sakaiproject.sms.model.constants.SmsConst_DeliveryStatus;
import org.sakaiproject.sms.model.constants.SmsConstants;
import org.sakaiproject.sms.model.constants.ValidationConstants;
import org.sakaiproject.sms.util.AbstractBaseTestCase;

// TODO: Auto-generated Javadoc
/**
 * The Class SmsMessageValidationTest. Runs tests for {@link SmsMessage}
 * validation that is run by {@link SmsMessageTaskValidator}
 */
public class TaskValidatorTest extends AbstractBaseTestCase {

	/** The sms task. */
	private SmsTask smsTask;

	private SmsTaskValidatorImpl smsTaskValidator = null;

	/** The msg. */
	private SmsMessage msg;

	/** The VALI d_ ms g_ body. */
	private String VALID_MSG_BODY = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.";

	/** The account. */
	private SmsAccount account;

	List<String> errors = null;

	@BeforeClass
	public static void beforeClass(){
		if (!SmsConstants.isDbSchemaCreated) {
			smsDao.createSchema();
			SmsConstants.isDbSchemaCreated = true;
		}
	}

	@Before
	public void setUp() throws Exception {
		errors = new ArrayList<String>();
		account = new SmsAccount();
		account.setSakaiSiteId("TaskValidatorTest"
				+ SmsConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID);
		account.setMessageTypeCode("");
		account.setCredits(10);
		account.setAccountName("TaskValidatorTest account name");
		account.setStartdate(new Date());
		account.setAccountEnabled(true);
		hibernateLogicLocator.getSmsAccountLogic().persistSmsAccount(account);
		// Inject the required impl's into core impl for testing
		smsTaskValidator = new SmsTaskValidatorImpl();
		SmsCoreImpl smsCoreImpl = new SmsCoreImpl();
		SmsBillingImpl smsBillingImpl = new SmsBillingImpl();
		smsBillingImpl.setHibernateLogicLocator(hibernateLogicLocator);
		smsCoreImpl.setHibernateLogicLocator(hibernateLogicLocator);
		smsCoreImpl.setSmsBilling(smsBillingImpl);
		msg = new SmsMessage();
		// smsTask = new SmsTask();
		smsTask = smsCoreImpl.getPreliminaryTestTask(account.getSakaiSiteId(),
				SmsConstants.SMS_DEV_DEFAULT_SAKAI_TOOL_ID);
		smsTask.setSmsAccountId(account.getId());
		smsTask.setSakaiSiteId(account.getSakaiSiteId());
		smsTask.setDateCreated(new Timestamp(System.currentTimeMillis()));
		smsTask.setDateToSend(new Timestamp(System.currentTimeMillis()));
		smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		smsTask.setAttemptCount(2);
		smsTask.setMessageBody(SmsConstants.SMS_DEV_DEFAULT_SMS_MESSAGE_BODY);
		smsTask.setSenderUserName("senderUserName");
		smsTask.setMaxTimeToLive(1);
		smsTask.getSmsMessages().add(msg);
		smsTask.setCreditEstimate(5);
		smsTask.setDeliveryGroupId("delGrpId");
		msg.setSmsTask(smsTask);
	}

	@After
	public void tearDown() throws Exception {
		hibernateLogicLocator.getSmsAccountLogic().deleteSmsAccount(account);

	}

	/**
	 * Test valid message.
	 */
    @Test
	public void testValidMessage() {
		errors = smsTaskValidator.validateInsertTask(smsTask);
		assertTrue(errors.size() == 0);
	}

	/**
	 * Test date created.
	 */
    @Test
	public void testDateCreated() {

		// null
		smsTask.setDateCreated(null);
		errors = smsTaskValidator.validateInsertTask(smsTask);
		assertTrue(errors.size() > 0);
		assertTrue(errors.contains(ValidationConstants.TASK_DATE_CREATED_EMPTY));
	}

	/**
	 * Test date to send.
	 */
    @Test
	public void testDateToSend() {

		// null
		smsTask.setDateToSend(null);
		errors = smsTaskValidator.validateInsertTask(smsTask);
		assertTrue(errors.size() > 0);
		assertTrue(errors.contains(ValidationConstants.TASK_DATE_TO_SEND_EMPTY));
	}

	/**
	 * Test max time to live.
	 */
    @Test
	public void testMaxTimeToLive() {
		// null
		smsTask.setMaxTimeToLive(null);
		errors = smsTaskValidator.validateInsertTask(smsTask);
		assertTrue(errors.size() > 0);
		assertTrue(errors
				.contains(ValidationConstants.TASK_MAX_TIME_TO_LIVE_INVALID));

		// invalid
		smsTask.setMaxTimeToLive(0);
		errors = smsTaskValidator.validateInsertTask(smsTask);
		assertTrue(errors.size() > 0);
		assertTrue(errors
				.contains(ValidationConstants.TASK_MAX_TIME_TO_LIVE_INVALID));
	}

	/**
	 * Test empty message body.
	 */
    @Test
	public void testMessageBody_empty() {
		smsTask.setMessageBody(null);
		errors = smsTaskValidator.validateInsertTask(smsTask);
		assertTrue(errors.size() > 0);
		assertTrue(errors.contains(ValidationConstants.MESSAGE_BODY_EMPTY));
	}

	/**
	 * Test null message body.
	 */
    @Test
	public void testMessageBody_null() {
		smsTask.setMessageBody(null);
		errors = smsTaskValidator.validateInsertTask(smsTask);
		assertTrue(errors.size() > 0);
		assertTrue(errors.contains(ValidationConstants.MESSAGE_BODY_EMPTY));
	}

	/**
	 * Test too long message body.
	 */
    @Test
	public void testMessageBody_tooLong() {
		smsTask.setMessageBody(VALID_MSG_BODY + VALID_MSG_BODY);
		assertTrue(msg.getMessageBody().length() > SmsConstants.MAX_SMS_LENGTH);
		errors = smsTaskValidator.validateInsertTask(smsTask);
		assertTrue(errors.size() > 0);
		assertTrue(errors.contains(ValidationConstants.MESSAGE_BODY_TOO_LONG));
	}

	/**
	 * Test message type id.
	 */
    @Test
	public void testMessageTypeId() {
		// null
		smsTask.setMessageTypeId(null);
		errors = smsTaskValidator.validateInsertTask(smsTask);
		assertTrue(errors.size() > 0);
		assertTrue(errors.contains(ValidationConstants.TASK_MESSAGE_TYPE_EMPTY));
	}

	/**
	 * Test sakai site id.
	 */
    @Test
	public void testSakaiSiteId() {

		// null
		smsTask.setSakaiSiteId(null);
		errors = smsTaskValidator.validateInsertTask(smsTask);
		assertTrue(errors.size() > 0);
		assertTrue(errors
				.contains(ValidationConstants.TASK_SAKAI_SITE_ID_EMPTY));

		// empty String
		smsTask.setSakaiSiteId("");
		errors = smsTaskValidator.validateInsertTask(smsTask);
		assertTrue(errors.size() > 0);
		assertTrue(errors
				.contains(ValidationConstants.TASK_SAKAI_SITE_ID_EMPTY));

		// Blank space
		smsTask.setSakaiSiteId("   ");
		errors = smsTaskValidator.validateInsertTask(smsTask);
		assertTrue(errors.size() > 0);
		assertTrue(errors
				.contains(ValidationConstants.TASK_SAKAI_SITE_ID_EMPTY));
	}

	/**
	 * Test sender user name.
	 */
    @Test
	public void testSenderUserName() {

		// null
		smsTask.setSenderUserName(null);
		errors = smsTaskValidator.validateInsertTask(smsTask);
		assertTrue(errors.size() > 0);
		assertTrue(errors
				.contains(ValidationConstants.TASK_SENDER_USER_NAME_EMPTY));

		// empty String
		smsTask.setSenderUserName("");
		errors = smsTaskValidator.validateInsertTask(smsTask);
		assertTrue(errors.size() > 0);
		assertTrue(errors
				.contains(ValidationConstants.TASK_SENDER_USER_NAME_EMPTY));

		// Blank space
		smsTask.setSenderUserName("   ");
		errors = smsTaskValidator.validateInsertTask(smsTask);
		assertTrue(errors.size() > 0);
		assertTrue(errors
				.contains(ValidationConstants.TASK_SENDER_USER_NAME_EMPTY));
	}

	/**
	 * Test status code.
	 */
    @Test
	public void testStatusCode() {

		// null
		smsTask.setStatusCode(null);
		errors = smsTaskValidator.validateInsertTask(smsTask);
		assertTrue(errors.size() > 0);
		assertTrue(errors.contains(ValidationConstants.TASK_STATUS_CODE_EMPTY));

		// empty String
		smsTask.setStatusCode("");
		errors = smsTaskValidator.validateInsertTask(smsTask);
		assertTrue(errors.size() > 0);
		assertTrue(errors.contains(ValidationConstants.TASK_STATUS_CODE_EMPTY));

		// Blank space
		smsTask.setStatusCode("   ");
		errors = smsTaskValidator.validateInsertTask(smsTask);
		assertTrue(errors.size() > 0);
		assertTrue(errors.contains(ValidationConstants.TASK_STATUS_CODE_EMPTY));
	}

}