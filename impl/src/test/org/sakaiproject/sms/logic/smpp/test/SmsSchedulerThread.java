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
package org.sakaiproject.sms.logic.smpp.test;

import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;

import org.sakaiproject.sms.logic.impl.hibernate.HibernateLogicFactory;
import org.sakaiproject.sms.logic.smpp.impl.SmsBillingImpl;
import org.sakaiproject.sms.logic.smpp.impl.SmsCoreImpl;
import org.sakaiproject.sms.logic.smpp.impl.SmsSchedulerImpl;
import org.sakaiproject.sms.logic.smpp.impl.SmsSmppImpl;
import org.sakaiproject.sms.model.hibernate.SmsAccount;
import org.sakaiproject.sms.model.hibernate.SmsTask;
import org.sakaiproject.sms.model.hibernate.constants.SmsHibernateConstants;

/**
 * 
 * The Class SmsSchedulerThread. Used in the scheduler threading test.
 * 
 * @author Etienne@psybergate.co.za
 * 
 */
public class SmsSchedulerThread extends TestRunnable {

	private SmsCoreImpl smsCoreImpl = null;

	private SmsSchedulerImpl smsSchedulerImpl = null;

	private SmsSmppImpl smsSmppImpl = null;

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(SmsSchedulerThread.class);

	/** The session name. */
	private String sessionName;

	private static SmsAccount smsAccount = null;

	/**
	 * Sets up the required api's
	 * 
	 * @param sessionName
	 */
	public SmsSchedulerThread(String sessionName) {
		this.sessionName = sessionName;
		smsSchedulerImpl = new SmsSchedulerImpl();
		smsCoreImpl = new SmsCoreImpl();
		smsSmppImpl = new SmsSmppImpl();
		smsCoreImpl.setSmsBilling(new SmsBillingImpl());
		smsSmppImpl.init();
		smsCoreImpl.setSmsSmpp(smsSmppImpl);
		smsCoreImpl.setLoggingLevel(Level.WARN);
		smsSchedulerImpl.setSmsCore(smsCoreImpl);
		LOG.setLevel(Level.ALL);
		smsSmppImpl.setLogLevel(Level.WARN);
		smsSchedulerImpl.init();
		smsAccount = new SmsAccount();
		smsAccount.setSakaiUserId("Username" + Math.random());
		smsAccount.setSakaiSiteId("smsSiteId" + Math.random());
		smsAccount.setMessageTypeCode("3");
		smsAccount.setOverdraftLimit(10000.00f);
		smsAccount.setBalance(1000f);
		smsAccount.setAccountName("accountnamej");
		smsAccount.setAccountEnabled(true);
		HibernateLogicFactory.getAccountLogic().persistSmsAccount(smsAccount);
	}

	/**
	 * Inserts 3 new tasks to be processed. The test is successful if no more
	 * tasks exists to process.
	 */
	public void runTest() throws Throwable {
		LOG.info(sessionName + ": Inserting tasks ");
		smsSmppImpl.setLogLevel(Level.ALL);
		Calendar now = Calendar.getInstance();

		SmsTask smsTask3 = smsCoreImpl.getPreliminaryTask("SmsTask3"
				+ sessionName, new Date(now.getTimeInMillis()),
				"-ThreadingTest-SmsTask3MessageBody",
				SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID, null,
				SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_USER_ID);

		smsTask3.setSmsAccountId(smsAccount.getId());
		smsTask3.setSakaiSiteId(smsAccount.getSakaiSiteId());
		smsTask3.setDeliveryUserId(smsAccount.getSakaiUserId());
		smsCoreImpl.calculateEstimatedGroupSize(smsTask3);
		smsCoreImpl.insertTask(smsTask3);

		now.add(Calendar.MINUTE, -1);
		SmsTask smsTask2 = smsCoreImpl.getPreliminaryTask("SmsTask2"
				+ sessionName, new Date(now.getTimeInMillis()),
				"ThreadingTest-SmsTask2MessageBody",
				SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID, null,
				SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_USER_ID);
		smsTask2.setSmsAccountId(smsAccount.getId());
		smsTask2.setSakaiSiteId(smsAccount.getSakaiSiteId());
		smsTask2.setDeliveryUserId(smsAccount.getSakaiUserId());
		smsTask2.setSmsAccountId(smsAccount.getId());

		smsCoreImpl.calculateEstimatedGroupSize(smsTask2);
		smsCoreImpl.insertTask(smsTask2);

		now.add(Calendar.MINUTE, -3);

		SmsTask smsTask1 = smsCoreImpl.getPreliminaryTask("SmsTask1"
				+ sessionName, new Date(now.getTimeInMillis()),
				"ThreadingTest-SmsTask1MessageBody",
				SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID, null,
				SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_USER_ID);
		smsCoreImpl.calculateEstimatedGroupSize(smsTask1);
		smsTask1.setSmsAccountId(smsAccount.getId());
		smsTask1.setSakaiSiteId(smsAccount.getSakaiSiteId());
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