/***********************************************************************************
 * SmppThread.java
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
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.groboutils.junit.v1.TestRunnable;

import org.sakaiproject.sms.dao.StandaloneSmsDaoImpl;
import org.sakaiproject.sms.logic.HibernateLogicLocator;
import org.sakaiproject.sms.logic.impl.hibernate.SmsAccountLogicImpl;
import org.sakaiproject.sms.logic.impl.hibernate.SmsConfigLogicImpl;
import org.sakaiproject.sms.logic.impl.hibernate.SmsMessageLogicImpl;
import org.sakaiproject.sms.logic.impl.hibernate.SmsTaskLogicImpl;
import org.sakaiproject.sms.logic.smpp.impl.SmsBillingImpl;
import org.sakaiproject.sms.logic.smpp.impl.SmsCoreImpl;
import org.sakaiproject.sms.logic.smpp.impl.SmsSmppImpl;
import org.sakaiproject.sms.logic.stubs.ExternalLogicStub;
import org.sakaiproject.sms.model.SmsAccount;
import org.sakaiproject.sms.model.SmsMessage;
import org.sakaiproject.sms.model.SmsTask;
import org.sakaiproject.sms.model.constants.SmsConst_DeliveryStatus;
import org.sakaiproject.sms.model.constants.SmsConst_SmscDeliveryStatus;
import org.sakaiproject.sms.model.constants.SmsConstants;

/**
 * The Class SmppSession. Used in the threading test.
 */
public class SmppThread extends TestRunnable {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(SmppThread.class);
	/** some private stuff for each thread. */
	public int reportsRemainingAfterSleep, sent_count, message_count;

	/** The session name. */
	private final String sessionName;

	/** The sms smpp impl. */
	private final SmsSmppImpl smsSmppImpl;

	private final SmsCoreImpl smsCoreImpl;

	private static SmsAccount smsAccount = null;

	private static HibernateLogicLocator hibernateLogicLocator = null;
	static {
		StandaloneSmsDaoImpl hibernateUtil = new StandaloneSmsDaoImpl(
				"hibernate-test.properties");

		smsAccount = new SmsAccount();
		smsAccount.setSakaiUserId("Username" + Math.random());
		smsAccount.setSakaiSiteId("smsSiteId" + Math.random());
		smsAccount.setMessageTypeCode("3");
		smsAccount.setOverdraftLimit(1000L);
		smsAccount.setCredits(1000L);
		smsAccount.setAccountName("accountname");
		smsAccount.setAccountEnabled(true);
		hibernateLogicLocator = new HibernateLogicLocator();

		SmsConfigLogicImpl smsConfigLogicImpl = new SmsConfigLogicImpl();
		smsConfigLogicImpl.setSmsDao(hibernateUtil);
		hibernateLogicLocator.setSmsConfigLogic(smsConfigLogicImpl);

		SmsMessageLogicImpl smsMessageLogicImpl = new SmsMessageLogicImpl();

		smsMessageLogicImpl.setExternalLogic(new ExternalLogicStub());
		smsMessageLogicImpl.setHibernateLogicLocator(hibernateLogicLocator);
		smsMessageLogicImpl.setSmsDao(hibernateUtil);

		SmsTaskLogicImpl smsTaskLogicImpl = new SmsTaskLogicImpl();
		smsTaskLogicImpl.setExternalLogic(new ExternalLogicStub());
		smsTaskLogicImpl.setHibernateLogicLocator(hibernateLogicLocator);
		smsTaskLogicImpl.setSmsDao(hibernateUtil);

		hibernateLogicLocator.setExternalLogic(new ExternalLogicStub());
		hibernateLogicLocator.setSmsTaskLogic(smsTaskLogicImpl);
		hibernateLogicLocator.setSmsMessageLogic(smsMessageLogicImpl);
		SmsAccountLogicImpl smsAccountLogicImpl = new SmsAccountLogicImpl();
		smsAccountLogicImpl.setHibernateLogicLocator(hibernateLogicLocator);
		smsAccountLogicImpl.setSmsDao(hibernateUtil);

		hibernateLogicLocator.setSmsAccountLogic(smsAccountLogicImpl);
		hibernateLogicLocator.getSmsAccountLogic()
				.persistSmsAccount(smsAccount);
	}

	/**
	 * Instantiates a new smpp session.
	 * 
	 * @param sessionName
	 *            the session name
	 * @param messageCount
	 *            the batch_count
	 */
	public SmppThread(String sessionName, int messageCount, int messageDelay) {
		this.sessionName = sessionName;
		smsCoreImpl = new SmsCoreImpl();
		smsSmppImpl = new SmsSmppImpl();
		smsCoreImpl.setSmsBilling(new SmsBillingImpl());
		smsCoreImpl.setHibernateLogicLocator(hibernateLogicLocator);
		smsSmppImpl.setHibernateLogicLocator(hibernateLogicLocator);
		smsSmppImpl.init();
		smsCoreImpl.setSmsSmpp(smsSmppImpl);
		this.message_count = messageCount;
	}

	/**
	 * This is an helper method to insert a dummy smsTask into the Database. The
	 * sakaiID is used to identify the temp task.
	 */
	public SmsTask insertNewTask(String sakaiID, String status,
			Date dateToSend, int attemptCount) {
		SmsTask insertTask = new SmsTask();

		insertTask.setSakaiSiteId(smsAccount.getSakaiSiteId());
		insertTask.setDeliveryUserId(smsAccount.getSakaiUserId());

		insertTask.setDateCreated(new Date(System.currentTimeMillis()));
		insertTask.setDateToSend(dateToSend);
		insertTask.setStatusCode(status);
		insertTask.setAttemptCount(0);
		insertTask
				.setMessageBody(SmsConstants.SMS_DEV_DEFAULT_SMS_MESSAGE_BODY);
		insertTask.setSenderUserName("administrator");
		insertTask.setMessageTypeId(0);
		insertTask.setMaxTimeToLive(300);
		insertTask.setDateProcessed(new Date());
		Calendar cal = Calendar.getInstance();
		cal.setTime(insertTask.getDateToSend());
		cal.add(Calendar.SECOND, insertTask.getMaxTimeToLive());
		// TODO, DateToExpire must be set from the UI as well
		insertTask.setDateToExpire(cal.getTime());
		insertTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_SENT);
		// insertTask.setMessageTypeId(SmsHibernateConstants.SMS_TASK_TYPE_PROCESS_NOW);
		insertTask.setSmsAccountId(smsAccount.getId());
		hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(insertTask);
		return insertTask;
	}

	/**
	 * Send x messages to gateway inside a separate thread
	 */
	@Override
	public void runTest() throws Throwable {
		LOG.info(sessionName + ": sending " + message_count + " to gateway...");
		SmsTask insertTask = insertNewTask(this.sessionName,
				SmsConst_DeliveryStatus.STATUS_PENDING, new Date(System
						.currentTimeMillis()), 0);
		// insertTask.setMessagesProcessed(SmsHibernateConstants.SMS_TASK_TYPE_PROCESS_NOW);
		insertTask.setAttemptCount(2);
		Set<SmsMessage> messages = new HashSet<SmsMessage>();
		for (int i = 0; i < message_count; i++) {
			SmsMessage message = new SmsMessage();
			message.setMobileNumber("072199891" + i);
			message.setSakaiUserId(this.sessionName);
			message.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
			message.setSmsTask(insertTask);
			messages.add(message);
		}
		insertTask.setSmsMessagesOnTask(messages);
		hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(insertTask);
		smsCoreImpl.processTask(insertTask);
		LOG.info(sessionName + ": sent " + insertTask.getMessagesProcessed()
				+ " to gateway");

		// waiting for a-synchronise delivery reports to arrive. Every
		// second we check to see if new messages came in.If the
		// reportsReceivedAfterSleep == 0, then we
		// know all the reports were received from the simulator.
		SmsTask updatedSmsTask = null;
		boolean waitForDeliveries = true;
		long start = System.currentTimeMillis();
		long timeoutPeriod = (1000 * 120);
		while (waitForDeliveries) {

			Thread.sleep(1000);
			updatedSmsTask = hibernateLogicLocator.getSmsTaskLogic()
					.getSmsTask(insertTask.getId());
			reportsRemainingAfterSleep = updatedSmsTask
					.getMessagesWithSmscStatus(
							SmsConst_SmscDeliveryStatus.ENROUTE).size();
			long time = System.currentTimeMillis();
			long diff = time - start;
			if (reportsRemainingAfterSleep == 0 || diff >= timeoutPeriod) {
				waitForDeliveries = false;

			} else {
				LOG.info(sessionName + ": waiting for delivery reports ("
						+ (message_count - reportsRemainingAfterSleep) + " of "
						+ message_count + ")");
			}
		}
		smsSmppImpl.disconnectGateWay();
		LOG.info(sessionName
				+ " ended, received "
				+ (updatedSmsTask.getSmsMessages().size() - updatedSmsTask
						.getMessagesWithSmscStatus(
								SmsConst_SmscDeliveryStatus.ENROUTE).size())
				+ " reports");
		assertTrue(updatedSmsTask.getMessagesWithSmscStatus(
				SmsConst_SmscDeliveryStatus.ENROUTE).size() == 0);
		assertTrue(updatedSmsTask.getSmsMessages().size() == message_count);

	}
}