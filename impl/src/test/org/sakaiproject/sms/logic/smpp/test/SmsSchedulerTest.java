package org.sakaiproject.sms.logic.smpp.test;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Level;
import org.sakaiproject.sms.dao.StandaloneSmsDaoImpl;
import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.impl.hibernate.SmsConfigLogicImpl;
import org.sakaiproject.sms.logic.smpp.SmsTaskValidationException;
import org.sakaiproject.sms.logic.smpp.exception.SmsSendDeniedException;
import org.sakaiproject.sms.logic.smpp.exception.SmsSendDisabledException;
import org.sakaiproject.sms.logic.smpp.impl.SmsBillingImpl;
import org.sakaiproject.sms.logic.smpp.impl.SmsCoreImpl;
import org.sakaiproject.sms.logic.smpp.impl.SmsSchedulerImpl;
import org.sakaiproject.sms.logic.smpp.impl.SmsSmppImpl;
import org.sakaiproject.sms.logic.smpp.validate.SmsTaskValidatorImpl;
import org.sakaiproject.sms.logic.stubs.ExternalLogicStub;
import org.sakaiproject.sms.model.hibernate.SmsAccount;
import org.sakaiproject.sms.model.hibernate.SmsConfig;
import org.sakaiproject.sms.model.hibernate.SmsTask;
import org.sakaiproject.sms.util.AbstractBaseTestCase;

/**
 * SmsScheduler Junit.This class will test various scheduling related scenarios.
 * 
 * @author Etienne@psybergate.co.za
 * 
 */
public class SmsSchedulerTest extends AbstractBaseTestCase {

	static SmsCoreImpl smsCoreImpl = null;
	static SmsBillingImpl smsBilling = null;
	static SmsSchedulerImpl smsSchedulerImpl = null;
	static SmsSmppImpl smsSmppImpl = null;
	static SmsConfigLogicImpl smsConfigLogic = null;
	static StandaloneSmsDaoImpl hibernateUtil = null;
	private final ExternalLogic externalLogic = new ExternalLogicStub();

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(SmsCoreTest.class);

	static {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.sms.util.AbstractBaseTestCase#testOnetimeSetup()
	 */
	@Override
	public void testOnetimeSetup() {
		StandaloneSmsDaoImpl.createSchema();
		smsConfigLogic = new SmsConfigLogicImpl();
		smsBilling = new SmsBillingImpl();
		smsBilling.setHibernateLogicLocator(hibernateLogicLocator);
		hibernateUtil = new StandaloneSmsDaoImpl("hibernate-test.properties");
		smsConfigLogic.setSmsDao(hibernateUtil);
		smsSchedulerImpl = new SmsSchedulerImpl();
		smsSchedulerImpl.setHibernateLogicLocator(hibernateLogicLocator);
		smsCoreImpl = new SmsCoreImpl();
		smsCoreImpl.setHibernateLogicLocator(hibernateLogicLocator);
		SmsTaskValidatorImpl smsTaskValidatorImpl = new SmsTaskValidatorImpl();
		smsTaskValidatorImpl.setSmsBilling(smsBilling);

		smsCoreImpl.setSmsTaskValidator(smsTaskValidatorImpl);
		smsCoreImpl.setSmsBilling(smsBilling);
		smsSmppImpl = new SmsSmppImpl();

		smsSmppImpl.setHibernateLogicLocator(hibernateLogicLocator);

		smsSmppImpl.init();
		smsCoreImpl.setSmsSmpp(smsSmppImpl);

		smsSchedulerImpl.setSmsCore(smsCoreImpl);
		smsSchedulerImpl.init();
		LOG.setLevel(Level.WARN);
		SmsConfig config = smsConfigLogic
				.getOrCreateSmsConfigBySakaiSiteId(externalLogic
						.getCurrentSiteId());
		config.setSendSmsEnabled(true);
		smsConfigLogic.persistSmsConfig(config);
	}

	/**
	 * This tests will insert 3 tasks to to processed.The test succeeds if no
	 * tasks remain after 1 min.
	 */
	public void testTaskProcessing() {

		SmsAccount smsAccount = new SmsAccount();
		smsAccount.setSakaiUserId(externalLogic.getCurrentUserId()
				+ Math.random());
		smsAccount.setSakaiSiteId(externalLogic.getCurrentSiteId()
				+ Math.random());
		smsAccount.setMessageTypeCode("3");
		smsAccount.setOverdraftLimit(1000L);
		smsAccount.setCredits(1000L);
		smsAccount.setAccountName("accountname" + Math.random());
		smsAccount.setAccountEnabled(true);
		hibernateLogicLocator.getSmsAccountLogic()
				.persistSmsAccount(smsAccount);

		Calendar now = Calendar.getInstance();
		SmsTask smsTask3 = smsCoreImpl.getPreliminaryTask("smsTask3", new Date(
				now.getTimeInMillis()), "smsTask3",
				smsAccount.getSakaiSiteId(), null, smsAccount.getSakaiUserId());

		smsTask3.setSmsAccountId(smsAccount.getId());
		smsCoreImpl.calculateEstimatedGroupSize(smsTask3);
		try {
			smsCoreImpl.insertTask(smsTask3);
		} catch (SmsTaskValidationException e1) {
			fail(e1.getMessage());
		} catch (SmsSendDeniedException e) {
			fail("SmsSendDeniedException caught");
		} catch (SmsSendDisabledException sd) {
			fail("SmsSendDisabledException caught");
		}

		now.add(Calendar.MINUTE, -1);
		SmsTask smsTask2 = smsCoreImpl.getPreliminaryTask("smsTask2", new Date(
				now.getTimeInMillis()), "smsTask2MessageBody", smsAccount
				.getSakaiSiteId(), null, smsAccount.getSakaiUserId());
		smsTask2.setSmsAccountId(smsAccount.getId());
		smsCoreImpl.calculateEstimatedGroupSize(smsTask2);
		try {
			smsCoreImpl.insertTask(smsTask2);
		} catch (SmsTaskValidationException e1) {
			fail(e1.getMessage());
		} catch (SmsSendDeniedException e) {
			fail("SmsSendDeniedException caught");
		} catch (SmsSendDisabledException sd) {
			fail("SmsSendDisabledException caught");
		}

		now.add(Calendar.MINUTE, -3);
		SmsTask smsTask1 = smsCoreImpl.getPreliminaryTask("smsTask1", new Date(
				now.getTimeInMillis()), "smsTask1MessageBody", smsAccount
				.getSakaiSiteId(), null, smsAccount.getSakaiUserId());
		smsTask1.setSmsAccountId(smsAccount.getId());
		smsCoreImpl.calculateEstimatedGroupSize(smsTask1);
		try {
			smsCoreImpl.insertTask(smsTask1);
		} catch (SmsTaskValidationException e1) {
			fail(e1.getMessage());
		} catch (SmsSendDeniedException e) {
			fail("SmsSendDeniedException caught");
		} catch (SmsSendDisabledException sd) {
			fail("SmsSendDisabledException caught");
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
