package org.sakaiproject.sms.logic.smpp.test;

import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;

import org.sakaiproject.sms.logic.impl.hibernate.HibernateLogicFactory;
import org.sakaiproject.sms.logic.smpp.impl.SmsBillingImpl;
import org.sakaiproject.sms.logic.smpp.impl.SmsCoreImpl;
import org.sakaiproject.sms.logic.smpp.impl.SmsSchedulerImpl;
import org.sakaiproject.sms.logic.smpp.impl.SmsSmppImpl;
import org.sakaiproject.sms.logic.smpp.impl.SmsTaskValidationException;
import org.sakaiproject.sms.model.hibernate.SmsAccount;
import org.sakaiproject.sms.model.hibernate.SmsTask;
import org.sakaiproject.sms.model.hibernate.constants.SmsHibernateConstants;
import org.sakaiproject.sms.util.AbstractBaseTestCase;
import org.sakaiproject.sms.util.HibernateUtil;

/**
 * SmsScheduler Junit.This class will test various scheduling related scenarios.
 * 
 * @author Etienne@psybergate.co.za
 * 
 */
public class SmsScheduler extends AbstractBaseTestCase {

	static SmsCoreImpl smsCoreImpl = null;
	static SmsSchedulerImpl smsSchedulerImpl = null;
	static SmsSmppImpl smsSmppImpl = null;

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(SmsCoreTest.class);

	static {
		HibernateUtil.createSchema();
		smsSchedulerImpl = new SmsSchedulerImpl();
		smsCoreImpl = new SmsCoreImpl();
		smsSmppImpl = new SmsSmppImpl();
		smsSmppImpl.setLogLevel(Level.WARN);
		smsCoreImpl.setSmsBilling(new SmsBillingImpl());
		smsSmppImpl.init();
		smsCoreImpl.setSmsSmpp(smsSmppImpl);
		smsCoreImpl.setLoggingLevel(Level.WARN);
		smsSchedulerImpl.setSmsCore(smsCoreImpl);
		smsSchedulerImpl.init();
		LOG.setLevel(Level.WARN);
	}

	/**
	 * This tests will insert 3 tasks to to processed.The test succeeds if no
	 * tasks remain after 1 min.
	 */
	public void testTaskProcessing() {
		SmsAccount smsAccount = new SmsAccount();
		smsAccount
				.setSakaiUserId(SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_USER_ID);
		smsAccount
				.setSakaiSiteId(SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID);
		smsAccount.setMessageTypeCode("3");
		smsAccount.setOverdraftLimit(10000.00f);
		smsAccount.setBalance(1000f);
		smsAccount.setAccountName("accountname");
		smsAccount.setAccountEnabled(true);
		HibernateLogicFactory.getAccountLogic().persistSmsAccount(smsAccount);

		Calendar now = Calendar.getInstance();
		SmsTask smsTask3 = smsCoreImpl.getPreliminaryTask("smsTask3", new Date(
				now.getTimeInMillis()), "smsTask3",
				SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID, null,
				SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_USER_ID);

		smsTask3.setSmsAccountId(smsAccount.getId());
		smsCoreImpl.calculateEstimatedGroupSize(smsTask3);
		try {
			smsCoreImpl.insertTask(smsTask3);
		} catch (SmsTaskValidationException e1) {
			fail(e1.getMessage());
		}

		now.add(Calendar.MINUTE, -1);
		SmsTask smsTask2 = smsCoreImpl.getPreliminaryTask("smsTask2", new Date(
				now.getTimeInMillis()), "smsTask2MessageBody",
				SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID, null,
				SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_USER_ID);
		smsTask2.setSmsAccountId(smsAccount.getId());
		smsCoreImpl.calculateEstimatedGroupSize(smsTask2);
		try {
			smsCoreImpl.insertTask(smsTask2);
		} catch (SmsTaskValidationException e1) {
			fail(e1.getMessage());
		}

		now.add(Calendar.MINUTE, -3);
		SmsTask smsTask1 = smsCoreImpl.getPreliminaryTask("smsTask1", new Date(
				now.getTimeInMillis()), "smsTask1MessageBody",
				SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID, null,
				SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_USER_ID);
		smsTask1.setSmsAccountId(smsAccount.getId());
		smsCoreImpl.calculateEstimatedGroupSize(smsTask1);
		try {
			smsCoreImpl.insertTask(smsTask1);
		} catch (SmsTaskValidationException e1) {
			fail(e1.getMessage());
		}

		try {
			Thread.sleep(60000);
		} catch (InterruptedException e) {

			e.printStackTrace();
		}
		assertTrue(smsCoreImpl.getNextSmsTask() == null);
	}

	@Override
	protected void tearDown() throws Exception {
		smsSchedulerImpl.stopSmsScheduler();
	}
}
