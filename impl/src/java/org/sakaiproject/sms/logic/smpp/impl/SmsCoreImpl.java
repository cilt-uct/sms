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
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.hibernate.HibernateLogicLocator;
import org.sakaiproject.sms.logic.hibernate.exception.SmsAccountNotFoundException;
import org.sakaiproject.sms.logic.hibernate.exception.SmsTaskNotFoundException;
import org.sakaiproject.sms.logic.smpp.SmsBilling;
import org.sakaiproject.sms.logic.smpp.SmsCore;
import org.sakaiproject.sms.logic.smpp.SmsSmpp;
import org.sakaiproject.sms.logic.smpp.SmsTaskValidationException;
import org.sakaiproject.sms.logic.smpp.exception.SmsSendDeniedException;
import org.sakaiproject.sms.logic.smpp.exception.SmsSendDisabledException;
import org.sakaiproject.sms.logic.smpp.util.MessageCatalog;
import org.sakaiproject.sms.logic.smpp.validate.SmsTaskValidator;
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

	private SmsTaskValidator smsTaskValidator;

	public SmsTaskValidator getSmsTaskValidator() {
		return smsTaskValidator;
	}

	public void setSmsTaskValidator(SmsTaskValidator smsTaskValidator) {
		this.smsTaskValidator = smsTaskValidator;
	}

	private HibernateLogicLocator hibernateLogicLocator;

	public HibernateLogicLocator getHibernateLogicLocator() {
		return hibernateLogicLocator;
	}

	public void setHibernateLogicLocator(
			HibernateLogicLocator hibernateLogicLocator) {
		this.hibernateLogicLocator = hibernateLogicLocator;
	}

	public SmsSmpp smsSmpp = null;

	public SmsBilling smsBilling = null;

	public SmsTask calculateEstimatedGroupSize(SmsTask smsTask) {
		int groupSize = 0;
		Set<SmsMessage> messages = hibernateLogicLocator.getExternalLogic()
				.getSakaiGroupMembers(smsTask, false);
		groupSize = messages.size();
		smsTask.setGroupSizeEstimate(groupSize);
		// one sms always costs one credit
		smsTask.setCreditEstimate(groupSize);
		smsTask.setCostEstimate(smsBilling.convertCreditsToAmount(groupSize)
				.doubleValue());
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
		Set<SmsMessage> messages = hibernateLogicLocator.getExternalLogic()
				.getSakaiGroupMembers(smsTask, true);
		smsTask.setSmsMessagesOnTask(messages);
		smsTask.setGroupSizeActual(messages.size());
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

	public SmsTask getNextSmsTask() {
		return hibernateLogicLocator.getSmsTaskLogic().getNextSmsTask();

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

		SmsConfig siteConfig = hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSystemSmsConfig();
		SmsConfig systemConfig = hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSystemSmsConfig();

		SmsTask smsTask = new SmsTask();
		try {
			smsTask.setSmsAccountId(smsBilling.getAccountID(sakaiSiteID,
					sakaiSenderID));
		} catch (SmsAccountNotFoundException e) {
			LOG.error(e);
			return null;
		}
		smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		smsTask.setSakaiSiteId(sakaiSiteID);
		smsTask.setMessageTypeId(SmsHibernateConstants.MESSAGE_TYPE_OUTGOING);
		smsTask.setSakaiToolId(sakaiToolId);
		smsTask.setSenderUserName(hibernateLogicLocator.getExternalLogic()
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
		// TODO, DateToExpire must be set from the UI as well
		smsTask.setDateToExpire(cal.getTime());
		smsTask.setDelReportTimeoutDuration(systemConfig
				.getDelReportTimeoutDuration());
		smsTask.setDeliveryMobileNumbersSet(mobileNumbers);
		smsTask.setDeliveryEntityList(deliveryEntityList);
		smsTask.setCreditCost(smsBilling.convertCreditsToAmount(1));
		return smsTask;
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
			throws SmsTaskValidationException, SmsSendDeniedException,
			SmsSendDisabledException {

		if (!hibernateLogicLocator.getExternalLogic().isUserAllowedInLocation(
				smsTask.getSenderUserId(), ExternalLogic.SMS_SEND,
				smsTask.getSakaiSiteId())) {
			throw new SmsSendDeniedException();
		}
		if (!hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSmsConfigBySakaiSiteId(smsTask.getSakaiSiteId())
				.isSendSmsEnabled()) {
			throw new SmsSendDisabledException();
		}

		ArrayList<String> errors = new ArrayList<String>();
		errors.addAll(smsTaskValidator.validateInsertTask(smsTask));
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
		errors.addAll(smsTaskValidator.checkSufficientCredits(smsTask));
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
		hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);
		smsBilling.reserveCredits(smsTask);
		tryProcessTaskRealTime(smsTask);
		return smsTask;
	}

	public void processIncomingMessage(SmsMessage smsMessage) {
		// TODO For phase 2
	}

	public synchronized void processNextTask() {
		SmsTask smsTask = hibernateLogicLocator.getSmsTaskLogic()
				.getNextSmsTask();
		if (smsTask != null) {
			this.processTask(smsTask);
		}
	}

	public void processTask(SmsTask smsTask) {
		SmsConfig systemConfig = hibernateLogicLocator.getSmsConfigLogic()
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
			smsTask.setFailReason(MessageCatalog
					.getMessage("messages.taskExpired"));
			hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);
			return;
		}

		smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_BUSY);
		hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);
		if (smsTask.getAttemptCount() < systemConfig.getSmsRetryMaxCount()) {
			if (smsTask.getAttemptCount() <= 1) {
				calculateActualGroupSize(smsTask);
				hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);
			}
			String submissionStatus = smsSmpp
					.sendMessagesToGateway(smsTask
							.getMessagesWithStatus(SmsConst_DeliveryStatus.STATUS_PENDING));
			smsTask = hibernateLogicLocator.getSmsTaskLogic().getSmsTask(
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
		hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);
	}

	public void processTimedOutDeliveryReports() {
		List<SmsMessage> smsMessages = hibernateLogicLocator
				.getSmsMessageLogic().getSmsMessagesWithStatus(null,
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
						hibernateLogicLocator.getSmsMessageLogic()
								.persistSmsMessage(message);
						hibernateLogicLocator.getSmsTaskLogic()
								.incrementMessagesProcessed(
										message.getSmsTask());
					}

				}
			}
		}

	}

	public boolean sendNotificationEmail(SmsTask smsTask, String toAddress,
			String subject, String body) {
		// TODO Call sakai service to send the email
		hibernateLogicLocator.getExternalLogic().sendEmail(smsTask, toAddress,
				subject, body);
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

		SmsConfig configSite = hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSmsConfigBySakaiSiteId(smsTask.getSakaiSiteId());
		SmsConfig configSystem = hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSystemSmsConfig();
		// Get the balance available to calculate the available credit.
		SmsAccount account = hibernateLogicLocator.getSmsAccountLogic()
				.getSmsAccount(smsTask.getSmsAccountId());
		if (account == null) {
			return false;
		}
		Long credits = account.getCredits();

		if (!account.getAccountEnabled()) {
			credits = 0L;
		} else if (account.getOverdraftLimit() != null) {
			// Add the overdraft to the available balance
			credits += account.getOverdraftLimit();
		}

		String creditsAvailable = credits + "";
		String creditsRequired = smsTask.getCreditEstimate() + "";
		toAddress = configSite.getNotificationEmail();
		if (taskMessageType
				.equals(SmsHibernateConstants.TASK_NOTIFICATION_STARTED)) {
			subject = MessageCatalog.getMessage(
					"messages.notificationSubjectStarted", smsTask.getId()
							.toString());
			body = MessageCatalog.getMessage(
					"messages.notificationBodyStarted", creditsRequired,
					creditsAvailable);

		} else if (taskMessageType
				.equals(SmsHibernateConstants.TASK_NOTIFICATION_SENT)) {
			subject = MessageCatalog.getMessage(
					"messages.notificationSubjectSent", smsTask.getId()
							.toString());
			body = MessageCatalog.getMessage("messages.notificationBodySent",
					creditsRequired, creditsAvailable);

		} else if (taskMessageType
				.equals(SmsHibernateConstants.ACCOUNT_OVERDRAFT_LIMIT_EXCEEDED)) {
			subject = MessageCatalog.getMessage(
					"messages.notificationSubjectOverdraftLimitExceeded",
					smsTask.getId().toString());
			body = MessageCatalog.getMessage(
					"messages.notificationOverdraftLimitExceeded", String
							.valueOf(account.getCredits()), String
							.valueOf(account.getOverdraftLimit()
									+ (-1*account.getCredits())));

		}

		else if (taskMessageType
				.equals(SmsHibernateConstants.TASK_NOTIFICATION_EXPIRED)) {
			subject = MessageCatalog.getMessage(
					"messages.notificationSubjectExpired", smsTask.getId()
							.toString());
			body = MessageCatalog
					.getMessage("messages.notificationBodyExpired");

		} else if (taskMessageType
				.equals(SmsHibernateConstants.TASK_NOTIFICATION_COMPLETED)) {
			subject = MessageCatalog.getMessage(
					"messages.notificationSubjectCompleted", smsTask.getId()
							.toString());
			body = MessageCatalog.getMessage(
					"messages.notificationBodyCompleted", String
							.valueOf(smsTask.getMessagesProcessed()), String
							.valueOf(smsTask.getMessagesDelivered()));

		} else if (taskMessageType
				.equals(SmsHibernateConstants.TASK_NOTIFICATION_ABORTED)) {
			subject = MessageCatalog.getMessage(
					"messages.notificationSubjectAborted", smsTask.getId()
							.toString());
			body = MessageCatalog.getMessage(
					"messages.notificationBodyAborted", smsTask
							.getSenderUserName());

		} else if (taskMessageType
				.equals(SmsHibernateConstants.TASK_NOTIFICATION_FAILED)) {
			subject = MessageCatalog.getMessage(
					"messages.notificationSubjectFailed", smsTask.getId()
							.toString());
			body = MessageCatalog.getMessage("messages.notificationBodyFailed",
					String.valueOf(configSystem.getSmsRetryMaxCount()));

		} else if (taskMessageType
				.equals(SmsHibernateConstants.TASK_INSUFFICIENT_CREDITS)) {
			subject = MessageCatalog
					.getMessage("messages.notificationSubjectTaskInsufficientCredits");
			body = MessageCatalog.getMessage(
					"messages.notificationBodyTaskInsufficientCredits", String
							.valueOf(account.getOverdraftLimit()), String
							.valueOf(account.getOverdraftLimit()
									+ account.getCredits()));
			if (toAddress == null || toAddress.length() == 0) {

				toAddress = hibernateLogicLocator.getExternalLogic()
						.getSakaiSiteContactEmail();
			}
			if (toAddress == null || toAddress.length() == 0) {
				return false;
			}
		}
		boolean systemNotification = sendNotificationEmail(smsTask, toAddress,
				subject, body);
		boolean ownerNotification = sendNotificationEmail(smsTask,
				hibernateLogicLocator.getExternalLogic()
						.getSakaiEmailAddressForUserId(
								smsTask.getSenderUserId()), subject, body);

		return (systemNotification && ownerNotification);

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

		List<SmsTask> smsTasks = hibernateLogicLocator.getSmsTaskLogic()
				.checkAndSetTasksCompleted();

		for (SmsTask smsTask : smsTasks) {
			smsBilling.settleCreditDifference(smsTask);
			checkOverdraft(smsTask);
			sendEmailNotification(smsTask,
					SmsHibernateConstants.TASK_NOTIFICATION_COMPLETED);

		}
	}

	private void checkOverdraft(SmsTask smsTask) {
		SmsAccount account = hibernateLogicLocator.getSmsAccountLogic()
				.getSmsAccount(smsTask.getSmsAccountId());
		if ((account.getCredits()) < (-1 * account.getOverdraftLimit())) {
			sendEmailNotification(smsTask,
					SmsHibernateConstants.ACCOUNT_OVERDRAFT_LIMIT_EXCEEDED);
		}

	}

	public void processVeryLateDeliveryReports() {
		List<SmsMessage> messages = hibernateLogicLocator.getSmsMessageLogic()
				.getSmsMessagesWithStatus(null,
						SmsConst_DeliveryStatus.STATUS_LATE);

		for (SmsMessage smsMessage : messages) {

			if ((smsMessage.getSmscDeliveryStatusCode()) != SmsConst_SmscDeliveryStatus.DELIVERED) {
				smsMessage.setStatusCode(SmsConst_DeliveryStatus.STATUS_FAIL);
			} else {
				smsMessage
						.setStatusCode(SmsConst_DeliveryStatus.STATUS_DELIVERED);
				hibernateLogicLocator.getSmsTaskLogic()
						.incrementMessagesDelivered(smsMessage.getSmsTask());
				smsBilling.debitLateMessage(smsMessage);
			}
			hibernateLogicLocator.getSmsMessageLogic().persistSmsMessage(
					smsMessage);
		}

	}

	public void abortPendingTask(Long smsTaskID)
			throws SmsTaskNotFoundException {
		SmsTask smsTask = hibernateLogicLocator.getSmsTaskLogic().getSmsTask(
				smsTaskID);
		if (smsTask == null) {
			throw new SmsTaskNotFoundException();
		} else {
			smsBilling.cancelPendingRequest(smsTaskID);
			smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_ABORT);
			smsTask.setStatusForMessages(
					SmsConst_DeliveryStatus.STATUS_PENDING,
					SmsConst_DeliveryStatus.STATUS_ABORT);
			smsTask.setFailReason(MessageCatalog
					.getMessage("messages.taskAborted"));
			hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);
			sendEmailNotification(smsTask,
					SmsHibernateConstants.TASK_NOTIFICATION_ABORTED);
		}

	}
}
