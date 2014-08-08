package org.sakaiproject.sms.logic.smpp.simulatorrequired.test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Level;
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Test;
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
import org.sakaiproject.sms.model.SmsAccount;
import org.sakaiproject.sms.model.SmsConfig;
import org.sakaiproject.sms.model.SmsTask;
import org.sakaiproject.sms.model.constants.SmsConstants;
import org.sakaiproject.sms.util.AbstractBaseTestCase;

/**
 * SmsScheduler Junit.This class will test various scheduling related scenarios.
 * 
 * @author Etienne@psybergate.co.za
 * 
 */
public class SmsSchedulerTest extends AbstractBaseTestCase {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(SmsCoreTest.class);

	private SmsSchedulerImpl smsSchedulerImpl = new SmsSchedulerImpl();
	private SmsCoreImpl smsCoreImpl = new SmsCoreImpl();

	/**
	 * This tests will insert 3 tasks to to processed.The test succeeds if no
	 * tasks remain after 1 min.
	 */
    @Test
	public void testTaskProcessing() {
		if (!SmsConstants.isDbSchemaCreated) {
			smsDao.createSchema();
		}
		ExternalLogic EXTERNAL_LOGIC = new ExternalLogicStub();
		SmsConfigLogicImpl smsConfigLogic = new SmsConfigLogicImpl();
		SmsBillingImpl smsBilling = new SmsBillingImpl();
		smsBilling.setHibernateLogicLocator(hibernateLogicLocator);

		smsConfigLogic.setSmsDao(smsDao);

		smsSchedulerImpl.setHibernateLogicLocator(hibernateLogicLocator);

		smsCoreImpl.setHibernateLogicLocator(hibernateLogicLocator);
		SmsTaskValidatorImpl smsTaskValidatorImpl = new SmsTaskValidatorImpl();
		smsTaskValidatorImpl.setSmsBilling(smsBilling);

		smsCoreImpl.setSmsTaskValidator(smsTaskValidatorImpl);
		smsCoreImpl.setSmsBilling(smsBilling);
		SmsSmppImpl smsSmppImpl = new SmsSmppImpl();

		smsSmppImpl.setHibernateLogicLocator(hibernateLogicLocator);

		smsSmppImpl.init();
		smsCoreImpl.setSmsSmpp(smsSmppImpl);

		smsSchedulerImpl.setSmsCore(smsCoreImpl);

		LOG.setLevel(Level.WARN);
		SmsConfig config = smsConfigLogic
				.getOrCreateSmsConfigBySakaiSiteId(EXTERNAL_LOGIC
						.getCurrentSiteId());
		config.setSendSmsEnabled(true);
		smsConfigLogic.persistSmsConfig(config);
		smsSchedulerImpl.init();
		List<SmsTask> smsTasks = smsCoreImpl.getHibernateLogicLocator()
				.getSmsTaskLogic().getAllSmsTask();

		for (SmsTask smsTask : smsTasks) {
			smsCoreImpl.getHibernateLogicLocator().getSmsTaskLogic()
					.deleteSmsTask(smsTask);
		}
		SmsAccount smsAccount = new SmsAccount();
		smsAccount.setSakaiUserId(EXTERNAL_LOGIC.getCurrentUserId()
				+ Math.random());
		smsAccount.setSakaiSiteId(EXTERNAL_LOGIC.getCurrentSiteId()
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

	@After
	public void tearDown() throws Exception {
		smsSchedulerImpl.stopSmsScheduler();
		smsCoreImpl.smsSmpp.disconnectGateWay();
	}
}
