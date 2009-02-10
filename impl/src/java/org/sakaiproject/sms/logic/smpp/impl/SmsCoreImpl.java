/***********************************************************************************
 * SmsCoreImpl.java
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
package org.sakaiproject.sms.logic.smpp.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.hibernate.exception.SmsAccountNotFoundException;
import org.sakaiproject.sms.logic.hibernate.exception.SmsTaskNotFoundException;
import org.sakaiproject.sms.logic.impl.hibernate.HibernateLogicFactory;
import org.sakaiproject.sms.logic.smpp.SmsBilling;
import org.sakaiproject.sms.logic.smpp.SmsCore;
import org.sakaiproject.sms.logic.smpp.SmsSmpp;
import org.sakaiproject.sms.logic.smpp.SmsTaskValidationException;
import org.sakaiproject.sms.logic.smpp.util.MessageCatalog;
import org.sakaiproject.sms.logic.smpp.validate.TaskValidator;
import org.sakaiproject.sms.model.hibernate.SmsAccount;
import org.sakaiproject.sms.model.hibernate.SmsConfig;
import org.sakaiproject.sms.model.hibernate.SmsMessage;
import org.sakaiproject.sms.model.hibernate.SmsTask;
import org.sakaiproject.sms.model.hibernate.constants.SmsConst_DeliveryStatus;
import org.sakaiproject.sms.model.hibernate.constants.SmsConst_SmscDeliveryStatus;
import org.sakaiproject.sms.model.hibernate.constants.SmsHibernateConstants;
import org.sakaiproject.sms.util.DateUtil;

/**
 * Handle all core logic regarding SMPP gateway communication.
 *
 * @author etienne@psybergate.co.za
 *
 */
public class SmsCoreImpl implements SmsCore {

	private static final Logger LOG = Logger.getLogger(SmsCoreImpl.class);

	public ExternalLogic externalLogic; // public for unit test to use stub impl

	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	public SmsSmpp smsSmpp = null;

	public SmsBilling smsBilling = null;

	public SmsTask calculateEstimatedGroupSize(SmsTask smsTask) {
		int groupSize = 0;

		if (smsTask.getDeliveryEntityList() != null) {
			for (String reference : smsTask.getDeliveryEntityList()) {
				groupSize += externalLogic.getGroupMemberCount(reference);
			}
		} else if (smsTask.getDeliveryGroupId() != null) {
			groupSize += externalLogic.getGroupMemberCount(smsTask
					.getDeliveryGroupId());
		} else if (smsTask.getDeliveryMobileNumbersSet() != null) {
			groupSize += smsTask.getDeliveryMobileNumbersSet().size();
		} else if (smsTask.getDeliveryUserId() != null) {
			groupSize = 1;
		}

		smsTask.setGroupSizeEstimate(groupSize);
		smsTask.setCreditEstimate(groupSize);
		smsTask.setCostEstimate(smsBilling.convertCreditsToAmount(groupSize)
				.doubleValue());// TODO Change to Float?
		return smsTask;
	}

	/**
	 * Method sets the sms Messages on the task and calculates the actual group
	 * size.
	 *
	 * @param smsTask
	 * @return
	 */
	private SmsTask calculateActualGroupSize(SmsTask smsTask) {
		smsTask.setSmsMessagesOnTask(this.generateSmsMessages(smsTask));
		smsTask.setGroupSizeActual(smsTask.getSmsMessages().size());

		return smsTask;

	}

	/*
	 * Enables or disables the debug Information
	 *
	 * @param debug
	 */
	public void setLoggingLevel(Level level) {
		LOG.setLevel(level);

	}

	/**
	 * /** For now we just generate the list. Will get it from Sakai later on.
	 * So we generate a random number of users with random mobile numbers.
	 *
	 * @param smsTask
	 * @return
	 */
	private Set<SmsMessage> generateDummySmsMessages(SmsTask smsTask) {
		Set<SmsMessage> messages = new HashSet<SmsMessage>();

		String[] users;
		int numberOfMessages = (int) Math.round(Math.random() * 100);

		users = new String[100];

		String[] celnumbers = new String[100];
		for (int i = 0; i < users.length; i++) {

			users[i] = "SakaiUser" + i;

			celnumbers[i] = "+2773"
					+ (int) Math.round(Math.random() * 10000000);
		}
		for (int i = 0; i < numberOfMessages; i++) {

			SmsMessage message = new SmsMessage();
			message.setMobileNumber(celnumbers[(int) Math
					.round(Math.random() * 99)]);

			message.setSakaiUserId(users[(int) Math.round(Math.random() * 99)]);

			message.setSmsTask(smsTask);
			messages.add(message);
		}
		return messages;
	}

	/**
	 * Get the group list from Sakai, dummy data for now.
	 */

	public Set<SmsMessage> generateSmsMessages(SmsTask smsTask) {
		return generateDummySmsMessages(smsTask);
		// TODO must make a Sakai call here
	}

	public SmsTask getNextSmsTask() {
		return HibernateLogicFactory.getTaskLogic().getNextSmsTask();

	}

	public SmsTask getPreliminaryTask(Date dateToSend, String messageBody,
			String sakaiSiteID, String sakaiToolId, String sakaiSenderID,
			Set<String> deliveryMobileNumbers) {

		return getPreliminaryTask(null, deliveryMobileNumbers, null,
				dateToSend, messageBody, sakaiSiteID, sakaiToolId,
				sakaiSenderID, null);
	}

	public SmsTask getPreliminaryTask(Set<String> sakaiUserIds,
			Date dateToSend, String messageBody, String sakaiSiteID,
			String sakaiToolId, String sakaiSenderID) {

		return getPreliminaryTask(null, null, sakaiUserIds, dateToSend,
				messageBody, sakaiSiteID, sakaiToolId, sakaiSenderID, null);
	}

	public SmsTask getPreliminaryTask(String deliverGroupId, Date dateToSend,
			String messageBody, String sakaiSiteID, String sakaiToolId,
			String sakaiSenderID) {
		return getPreliminaryTask(deliverGroupId, null, null, dateToSend,
				messageBody, sakaiSiteID, sakaiToolId, sakaiSenderID, null);
	}

	public SmsTask getPreliminaryTask(Date dateToSend, String messageBody,
			String sakaiSiteID, String sakaiToolId, String sakaiSenderID,
			List<String> deliveryEntityList) {
		return getPreliminaryTask(null, null, null, dateToSend, messageBody,
				sakaiSiteID, sakaiToolId, sakaiSenderID, deliveryEntityList);
	}

	// ONLY FOR UNIT TESTS
	public SmsTask getPreliminaryTestTask() {
		return getPreliminaryTask(null, null, null, new Date(), "",
				"SakaiSiteID", "", "SakaiUserID", null);
	}

	private SmsTask getPreliminaryTask(String deliverGroupId,
			Set<String> mobileNumbers, Set<String> sakaiUserIds,
			Date dateToSend, String messageBody, String sakaiSiteID,
			String sakaiToolId, String sakaiSenderID,
			List<String> deliveryEntityList) {

		SmsConfig siteConfig = HibernateLogicFactory.getConfigLogic()
				.getOrCreateSystemSmsConfig();
		SmsConfig systemConfig = HibernateLogicFactory.getConfigLogic()
				.getOrCreateSystemSmsConfig();

		SmsTask smsTask = new SmsTask();
		try {
			smsTask.setSmsAccountId(smsBilling.getAccountID(sakaiSiteID,
					sakaiSenderID));
		} catch (SmsAccountNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		smsTask.setSakaiSiteId(sakaiSiteID);
		smsTask.setMessageTypeId(SmsHibernateConstants.MESSAGE_TYPE_OUTGOING);
		smsTask.setSakaiToolId(sakaiToolId);
		smsTask.setSenderUserName(externalLogic
				.getSakaiUserDisplayName(sakaiSenderID));
		smsTask.setSenderUserId(sakaiSenderID);
		smsTask.setDeliveryGroupName(deliverGroupId);
		smsTask.setDeliveryGroupId(deliverGroupId);
		smsTask.setDateCreated(new Date());
		smsTask.setDateToSend(dateToSend);
		smsTask.setAttemptCount(0);
		smsTask.setMessageBody(messageBody);
		smsTask.setMaxTimeToLive(siteConfig.getSmsTaskMaxLifeTime());
		Calendar cal = Calendar.getInstance();
		cal.setTime(smsTask.getDateToSend());
		cal.add(Calendar.SECOND, smsTask.getMaxTimeToLive());
		smsTask.setDateToExpire(cal.getTime());
		smsTask.setDelReportTimeoutDuration(systemConfig
				.getDelReportTimeoutDuration());
		smsTask.setDeliveryMobileNumbersSet(mobileNumbers);
		smsTask.setDeliveryEntityList(deliveryEntityList);
		smsTask.setCreditCost(smsBilling.convertCreditsToAmount(1));
		return smsTask;
	}

	/**
	 * Get Sakai user's mobile number from member profile. Return the mobile
	 * number, null if not found.
	 *
	 * @param sakaiUserID
	 */
	public String getSakaiMobileNumber(String sakaiUserID) {
		// TODO Auto-generated method stub
		return null;
	}

	public SmsBilling getSmsBilling() {
		return smsBilling;
	}

	public SmsSmpp getSmsSmpp() {
		return smsSmpp;
	}

	public void init() {

	}

	public synchronized SmsTask insertTask(SmsTask smsTask)
			throws SmsTaskValidationException {

		ArrayList<String> errors = new ArrayList<String>();
		errors.addAll(TaskValidator.validateInsertTask(smsTask));
		if (errors.size() > 0) {
			// Do not persist, just throw exception
			SmsTaskValidationException validationException = new SmsTaskValidationException(
					errors,
					(MessageCatalog
							.getMessage("messages.sms.errors.task.validationFailed")));
			LOG.error(MessageCatalog
					.getMessage("messages.sms.errors.task.validationFailed")
					+ ": " + validationException.getErrorMessagesAsBlock());
			throw validationException;
		}

		// we set the date again due to time laps between getPreliminaryTask and
		// insertask
		smsTask.setDateCreated(DateUtil.getCurrentDate());
		// We do this because if there the invalid values in the task then the
		// checkSufficientCredits() will throw unexpected exceptions. Check for
		// sufficient credit only if the task is valid
		errors.clear();
		errors.addAll(TaskValidator.checkSufficientCredits(smsTask));
		if (errors.size() > 0) {
			SmsTaskValidationException validationException = new SmsTaskValidationException(
					errors,
					MessageCatalog
							.getMessage("messages.sms.errors.task.validationFailed"));
			smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_FAIL);
			smsTask
					.setFailReason(validationException
							.getErrorMessagesAsBlock());
			LOG.error(MessageCatalog
					.getMessage("messages.sms.errors.task.validationFailed")
					+ ": " + validationException.getErrorMessagesAsBlock());
			throw validationException;
		}
		HibernateLogicFactory.getTaskLogic().persistSmsTask(smsTask);
		smsBilling.reserveCredits(smsTask);
		tryProcessTaskRealTime(smsTask);
		return smsTask;
	}

	public void processIncomingMessage(SmsMessage smsMessage) {
		// TODO For phase 2
	}

	public synchronized void processNextTask() {
		SmsTask smsTask = HibernateLogicFactory.getTaskLogic().getNextSmsTask();
		if (smsTask != null) {
			this.processTask(smsTask);
		}
	}

	public void processTask(SmsTask smsTask) {
		SmsConfig systemConfig = HibernateLogicFactory.getConfigLogic()
				.getOrCreateSystemSmsConfig();
		smsTask.setDateProcessed(new Date());
		smsTask.setAttemptCount((smsTask.getAttemptCount()) + 1);

		if (smsTask.getDateToExpire().before(new Date())) {
			smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_EXPIRE);
			smsTask.setStatusForMessages(
					SmsConst_DeliveryStatus.STATUS_PENDING,
					SmsConst_DeliveryStatus.STATUS_EXPIRE);
			sendEmailNotification(smsTask,
					SmsHibernateConstants.TASK_NOTIFICATION_FAILED);
			smsBilling.cancelPendingRequest(smsTask.getId());
			HibernateLogicFactory.getTaskLogic().persistSmsTask(smsTask);
			return;
		}

		smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_BUSY);
		HibernateLogicFactory.getTaskLogic().persistSmsTask(smsTask);
		if (smsTask.getAttemptCount() < systemConfig.getSmsRetryMaxCount()) {
			if (smsTask.getAttemptCount() <= 1) {
				calculateActualGroupSize(smsTask);
				HibernateLogicFactory.getTaskLogic().persistSmsTask(smsTask);
			}
			String submissionStatus = smsSmpp
					.sendMessagesToGateway(smsTask
							.getMessagesWithStatus(SmsConst_DeliveryStatus.STATUS_PENDING));
			smsTask = HibernateLogicFactory.getTaskLogic().getSmsTask(
					smsTask.getId());
			smsTask.setStatusCode(submissionStatus);

			if (smsTask.getStatusCode().equals(
					SmsConst_DeliveryStatus.STATUS_INCOMPLETE)
					|| smsTask.getStatusCode().equals(
							SmsConst_DeliveryStatus.STATUS_RETRY)) {
				Calendar now = Calendar.getInstance();
				now.add(Calendar.SECOND, +(systemConfig
						.getSmsRetryScheduleInterval()));
				smsTask.rescheduleDateToSend(new Date(now.getTimeInMillis()));
			}

		} else {
			smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_FAIL);
			smsTask.setStatusForMessages(
					SmsConst_DeliveryStatus.STATUS_PENDING,
					SmsConst_DeliveryStatus.STATUS_FAIL);
			sendEmailNotification(smsTask,
					SmsHibernateConstants.TASK_NOTIFICATION_FAILED);
			smsTask.setFailReason((MessageCatalog.getMessage(
					"messages.taskRetryFailure", String.valueOf(systemConfig
							.getSmsRetryMaxCount()))));
			smsBilling.cancelPendingRequest(smsTask.getId());
		}
		HibernateLogicFactory.getTaskLogic().persistSmsTask(smsTask);
	}

	public void processTimedOutDeliveryReports() {
		List<SmsMessage> smsMessages = HibernateLogicFactory.getMessageLogic()
				.getSmsMessagesWithStatus(null,
						SmsConst_DeliveryStatus.STATUS_SENT);

		if (smsMessages != null) {
			for (SmsMessage message : smsMessages) {
				SmsTask task = message.getSmsTask();
				if (task.getDateProcessed() != null) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(task.getDateProcessed());
					cal
							.add(Calendar.SECOND, task
									.getDelReportTimeoutDuration());
					if (cal.getTime().before(new Date())) {
						message
								.setStatusCode(SmsConst_DeliveryStatus.STATUS_TIMEOUT);
						HibernateLogicFactory.getMessageLogic()
								.persistSmsMessage(message);
						HibernateLogicFactory.getTaskLogic()
								.incrementMessagesProcessed(
										message.getSmsTask());
					}

				}
			}
		}

	}

	public boolean sendNotificationEmail(String toAddress, String subject,
			String body) {
		// TODO Call sakai service to send the email
		return true;
	}

	/**
	 * Send a email notification out.
	 *
	 * @param smsTask
	 *            the sms task
	 * @param taskMessageType
	 *            the task message type
	 *
	 * @return true, if successful
	 */
	private boolean sendEmailNotification(SmsTask smsTask,
			Integer taskMessageType) {

		String subject = null;
		String body = null;
		String toAddress = null;

		SmsConfig config = HibernateLogicFactory.getConfigLogic()
				.getOrCreateSmsConfigBySakaiSiteId(smsTask.getSakaiSiteId());
		// Get the balance available to calculate the available credit.
		SmsAccount account = HibernateLogicFactory.getAccountLogic()
				.getSmsAccount(smsTask.getSmsAccountId());
		if (account == null) {
			return false;
		}
		Float amount = account.getBalance();

		if (!account.getAccountEnabled()) {
			amount = 0.0f;
		} else if (account.getOverdraftLimit() != null) {
			// Add the overdraft to the available balance
			amount += account.getOverdraftLimit();
		}

		String creditsAvailable = smsBilling.convertAmountToCredits(amount)
				+ "";
		String creditsRequired = smsTask.getCreditEstimate() + "";

		if (taskMessageType
				.equals(SmsHibernateConstants.TASK_NOTIFICATION_STARTED)) {
			subject = MessageCatalog.getMessage(
					"messages.notificationSubjectStarted", smsTask.getId()
							.toString());
			body = MessageCatalog.getMessage(
					"messages.notificationBodyStarted", creditsRequired,
					creditsAvailable);
			toAddress = config.getNotificationEmail();

		} else if (taskMessageType
				.equals(SmsHibernateConstants.TASK_NOTIFICATION_SENT)) {
			subject = MessageCatalog.getMessage(
					"messages.notificationSubjectSent", smsTask.getId()
							.toString());
			body = MessageCatalog.getMessage("messages.notificationBodySent",
					creditsRequired, creditsAvailable);
			toAddress = config.getNotificationEmailSent();

		} else if (taskMessageType
				.equals(SmsHibernateConstants.TASK_NOTIFICATION_FAILED)) {
			subject = MessageCatalog.getMessage(
					"messages.notificationSubjectFailed", smsTask.getId()
							.toString());
			body = MessageCatalog.getMessage("messages.notificationBodyFailed",
					creditsRequired, creditsAvailable);
			toAddress = config.getNotificationEmail();
		} else if (taskMessageType
				.equals(SmsHibernateConstants.TASK_NOTIFICATION_COMPLETED)) {
			subject = MessageCatalog.getMessage(
					"messages.notificationSubjectCompleted", smsTask.getId()
							.toString());
			body = MessageCatalog.getMessage(
					"messages.notificationBodyCompleted", creditsRequired,
					creditsAvailable);
			toAddress = config.getNotificationEmail();
		} else if (taskMessageType
				.equals(SmsHibernateConstants.TASK_INSUFFICIENT_CREDITS)) {
			subject = MessageCatalog
					.getMessage("messages.notificationSubjectTaskInsufficientCredits");
			body = MessageCatalog.getMessage(
					"messages.notificationBodyTaskInsufficientCredits", String
							.valueOf(account.getOverdraftLimit()), String
							.valueOf(account.getOverdraftLimit()
									+ account.getBalance()));
			toAddress = account.getNotificationEmail();

			if (toAddress == null || toAddress.length() == 0) {

				toAddress = externalLogic.getSakaiSiteContactEmail();
			}
			if (toAddress == null || toAddress.length() == 0) {
				return false;
			}
					}
		return sendNotificationEmail(toAddress, subject, body);

	}

	public void setSmsBilling(SmsBilling smsBilling) {
		this.smsBilling = smsBilling;
	}

	public void setSmsSmpp(SmsSmpp smsSmpp) {
		this.smsSmpp = smsSmpp;
	}

	public void tryProcessTaskRealTime(SmsTask smsTask) {

		// TODO also check number of process threads
		if (smsTask.getDateToSend().getTime() <= System.currentTimeMillis()) {
			this.processTask(smsTask);
		}
	}

	public void checkAndSetTasksCompleted() {

		List<SmsTask> smsTasks = HibernateLogicFactory.getTaskLogic()
				.checkAndSetTasksCompleted();

		for (SmsTask smsTask : smsTasks) {
			smsBilling.settleCreditDifference(smsTask);
			checkOverdraft(smsTask);
			sendEmailNotification(smsTask,
					SmsHibernateConstants.TASK_NOTIFICATION_COMPLETED);
		}

	}

	private void checkOverdraft(SmsTask smsTask) {
		SmsAccount account = HibernateLogicFactory.getAccountLogic()
				.getSmsAccount(smsTask.getSmsAccountId());
		if ((account.getBalance() < (-1 * account.getOverdraftLimit()))) {
			sendEmailNotification(smsTask,
					SmsHibernateConstants.TASK_INSUFFICIENT_CREDITS);
		}

	}

	public void processVeryLateDeliveryReports() {
		List<SmsMessage> messages = HibernateLogicFactory.getMessageLogic()
				.getSmsMessagesWithStatus(null,
						SmsConst_DeliveryStatus.STATUS_LATE);

		for (SmsMessage smsMessage : messages) {

			if ((smsMessage.getSmscDeliveryStatusCode()) != SmsConst_SmscDeliveryStatus.DELIVERED) {
				smsMessage.setStatusCode(SmsConst_DeliveryStatus.STATUS_FAIL);
			} else {
				smsMessage
						.setStatusCode(SmsConst_DeliveryStatus.STATUS_DELIVERED);
				HibernateLogicFactory.getTaskLogic()
						.incrementMessagesDelivered(smsMessage.getSmsTask());
				smsBilling.creditLateMessage(smsMessage);
			}
			HibernateLogicFactory.getMessageLogic().persistSmsMessage(
					smsMessage);
		}

	}

	public void abortPendingTask(Long smsTaskID)
			throws SmsTaskNotFoundException {
		SmsTask smsTask = HibernateLogicFactory.getTaskLogic().getSmsTask(
				smsTaskID);
		if (smsTask == null) {
			throw new SmsTaskNotFoundException();
		} else {
			smsBilling.cancelPendingRequest(smsTaskID);
			smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_ABORT);
			smsTask.setStatusForMessages(
					SmsConst_DeliveryStatus.STATUS_PENDING,
					SmsConst_DeliveryStatus.STATUS_ABORT);
			HibernateLogicFactory.getTaskLogic().persistSmsTask(smsTask);

		}

	}
}
