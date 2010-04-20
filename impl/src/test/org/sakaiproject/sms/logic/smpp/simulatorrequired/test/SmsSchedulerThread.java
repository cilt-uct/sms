/***********************************************************************************
 * SmsSchedulerThread.java
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
 * limitations under the License.s
 *
 **********************************************************************************/
package org.sakaiproject.sms.logic.smpp.simulatorrequired.test;

import java.util.Calendar;
import java.util.Date;

import net.sourceforge.groboutils.junit.v1.TestRunnable;

import org.apache.log4j.Level;
import org.sakaiproject.sms.dao.StandaloneSmsDaoImpl;
import org.sakaiproject.sms.logic.HibernateLogicLocator;
import org.sakaiproject.sms.logic.impl.hibernate.SmsAccountLogicImpl;
import org.sakaiproject.sms.logic.impl.hibernate.SmsConfigLogicImpl;
import org.sakaiproject.sms.logic.impl.hibernate.SmsMessageLogicImpl;
import org.sakaiproject.sms.logic.impl.hibernate.SmsTaskLogicImpl;
import org.sakaiproject.sms.logic.impl.hibernate.SmsTransactionLogicImpl;
import org.sakaiproject.sms.logic.smpp.impl.SmsBillingImpl;
import org.sakaiproject.sms.logic.smpp.impl.SmsCoreImpl;
import org.sakaiproject.sms.logic.smpp.impl.SmsSchedulerImpl;
import org.sakaiproject.sms.logic.smpp.impl.SmsSmppImpl;
import org.sakaiproject.sms.logic.smpp.validate.SmsTaskValidatorImpl;
import org.sakaiproject.sms.logic.stubs.ExternalLogicStub;
import org.sakaiproject.sms.model.SmsAccount;
import org.sakaiproject.sms.model.SmsConfig;
import org.sakaiproject.sms.model.SmsTask;

/**
 * 
 * The Class SmsSchedulerThread. Used in the scheduler threading test.
 * 
 * @author Etienne@psybergate.co.za
 * 
 */
public class SmsSchedulerThread extends TestRunnable {

	private SmsCoreImpl smsCoreImpl = null;

	private SmsBillingImpl smsBillingImpl = null;

	private SmsSchedulerImpl smsSchedulerImpl = null;

	private SmsSmppImpl smsSmppImpl = null;

	private final ExternalLogicStub externalLogicStub;

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(SmsSchedulerThread.class);
	private static HibernateLogicLocator hibernateLogicLocator = new HibernateLogicLocator();
	/** The session name. */
	private final String sessionName;

	private SmsAccount smsAccount = null;

	/**
	 * Sets up the required api's
	 * 
	 * @param sessionName
	 */
	public SmsSchedulerThread(String sessionName) {
		StandaloneSmsDaoImpl hibernateUtil = new StandaloneSmsDaoImpl(
				"hibernate-test.properties");
		smsBillingImpl = new SmsBillingImpl();
		externalLogicStub = new ExternalLogicStub();
		SmsAccountLogicImpl smsAccountLogicImpl = new SmsAccountLogicImpl();
		smsAccountLogicImpl.setHibernateLogicLocator(hibernateLogicLocator);
		smsAccountLogicImpl.setSmsDao(hibernateUtil);

		hibernateLogicLocator.setSmsAccountLogic(smsAccountLogicImpl);

		SmsConfigLogicImpl smsConfigLogicImpl = new SmsConfigLogicImpl();
		smsConfigLogicImpl.setSmsDao(hibernateUtil);
		hibernateLogicLocator.setSmsConfigLogic(smsConfigLogicImpl);

		SmsTaskLogicImpl smsTaskLogicImpl = new SmsTaskLogicImpl();
		smsTaskLogicImpl.setExternalLogic(new ExternalLogicStub());
		smsTaskLogicImpl.setHibernateLogicLocator(hibernateLogicLocator);
		smsTaskLogicImpl.setSmsDao(hibernateUtil);
		hibernateLogicLocator.setSmsTaskLogic(smsTaskLogicImpl);
		SmsMessageLogicImpl smsMessageLogicImpl = new SmsMessageLogicImpl();

		smsMessageLogicImpl.setExternalLogic(new ExternalLogicStub());
		smsMessageLogicImpl.setHibernateLogicLocator(hibernateLogicLocator);
		smsMessageLogicImpl.setSmsDao(hibernateUtil);
		hibernateLogicLocator.setSmsMessageLogic(smsMessageLogicImpl);

		SmsTransactionLogicImpl smsTransactionLogicImpl = new SmsTransactionLogicImpl();
		smsTransactionLogicImpl.setHibernateLogicLocator(hibernateLogicLocator);
		smsTransactionLogicImpl.setSmsDao(hibernateUtil);
		smsTransactionLogicImpl.setSmsBilling(smsBillingImpl);

		hibernateLogicLocator.setSmsTransactionLogic(smsTransactionLogicImpl);
		hibernateLogicLocator.setExternalLogic(new ExternalLogicStub());
		SmsConfig smsConfig = hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSmsConfigBySakaiSiteId(
						externalLogicStub.getCurrentSiteId());
		smsConfig.setSendSmsEnabled(true);
		hibernateLogicLocator.getSmsConfigLogic().persistSmsConfig(smsConfig);
		this.sessionName = sessionName;
		smsSchedulerImpl = new SmsSchedulerImpl();
		smsCoreImpl = new SmsCoreImpl();
		smsSmppImpl = new SmsSmppImpl();
		smsBillingImpl.setHibernateLogicLocator(hibernateLogicLocator);
		smsCoreImpl.setSmsBilling(smsBillingImpl);
		SmsTaskValidatorImpl smsTaskValidator = new SmsTaskValidatorImpl();
		smsTaskValidator.setSmsBilling(smsBillingImpl);
		smsCoreImpl.setSmsTaskValidator(smsTaskValidator);

		smsCoreImpl.setHibernateLogicLocator(hibernateLogicLocator);
		smsSmppImpl.setHibernateLogicLocator(hibernateLogicLocator);
		smsSmppImpl.init();
		smsCoreImpl.setSmsSmpp(smsSmppImpl);
		smsSchedulerImpl.setSmsCore(smsCoreImpl);
		smsSchedulerImpl.setHibernateLogicLocator(hibernateLogicLocator);
		LOG.setLevel(Level.ALL);
		smsSchedulerImpl.setHibernateLogicLocator(hibernateLogicLocator);
		smsSchedulerImpl.init();
		smsAccount = new SmsAccount();
		smsAccount.setSakaiUserId("Username" + Math.random());
		smsAccount.setSakaiSiteId("smsSiteId" + Math.random());
		smsAccount.setMessageTypeCode("3");
		smsAccount.setOverdraftLimit(1000L);
		smsAccount.setCredits(1000L);
		smsAccount.setAccountName("accountnamej");
		smsAccount.setAccountEnabled(true);
		hibernateLogicLocator.getSmsAccountLogic()
				.persistSmsAccount(smsAccount);
		SmsConfig config = hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSmsConfigBySakaiSiteId(smsAccount.getSakaiSiteId());
		config.setSendSmsEnabled(true);
		hibernateLogicLocator.getSmsConfigLogic().persistSmsConfig(config);

	}

	/**
	 * Inserts 3 new tasks to be processed. The test is successful if no more
	 * tasks exists to process.
	 */
	@Override
	public void runTest() throws Throwable {
		LOG.info(sessionName + ": Inserting tasks ");
		Calendar now = Calendar.getInstance();
		SmsTask smsTask3 = smsCoreImpl.getPreliminaryTask("SmsTask3"
				+ sessionName, new Date(now.getTimeInMillis()),
				"-ThreadingTest-SmsTask3MessageBody", smsAccount
						.getSakaiSiteId(), null, externalLogicStub
						.getCurrentUserId());

		smsTask3.setSmsAccountId(smsAccount.getId());
		smsTask3.setDeliveryUserId(smsAccount.getSakaiUserId());
		smsCoreImpl.calculateEstimatedGroupSize(smsTask3);
		smsCoreImpl.insertTask(smsTask3);
		now.add(Calendar.MINUTE, -1);
		SmsTask smsTask2 = smsCoreImpl.getPreliminaryTask("SmsTask2"
				+ sessionName, new Date(now.getTimeInMillis()),
				"ThreadingTest-SmsTask2MessageBody", smsAccount
						.getSakaiSiteId(), null, externalLogicStub
						.getCurrentUserId());
		smsTask2.setSmsAccountId(smsAccount.getId());
		// smsTask2.setSakaiSiteId(smsAccount.getSakaiSiteId());
		smsTask2.setDeliveryUserId(smsAccount.getSakaiUserId());
		smsTask2.setSmsAccountId(smsAccount.getId());

		smsCoreImpl.calculateEstimatedGroupSize(smsTask2);
		smsCoreImpl.insertTask(smsTask2);

		now.add(Calendar.MINUTE, -3);

		SmsTask smsTask1 = smsCoreImpl.getPreliminaryTask("SmsTask1"
				+ sessionName, new Date(now.getTimeInMillis()),
				"ThreadingTest-SmsTask1MessageBody", smsAccount
						.getSakaiSiteId(), null, externalLogicStub
						.getCurrentUserId());
		smsCoreImpl.calculateEstimatedGroupSize(smsTask1);
		smsTask1.setSmsAccountId(smsAccount.getId());
		// smsTask1.setSakaiSiteId(smsAccount.getSakaiSiteId());
		smsTask1.setDeliveryUserId(smsAccount.getSakaiUserId());
		smsTask1.setSmsAccountId(smsAccount.getId());
		smsCoreImpl.insertTask(smsTask1);

		try {
			Thread.sleep(60000);
		} catch (InterruptedException e) {

			e.printStackTrace();
		}
		LOG.info(sessionName + ": 1 min passed ");
		assertTrue(smsCoreImpl.getNextSmsTask() == null);
		LOG.info(sessionName + ": Success ");
		smsSchedulerImpl.stopSmsScheduler();
		smsSmppImpl.disconnectGateWay();

	}
}