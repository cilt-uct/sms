/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.sms.logic.smpp.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.hibernate.HibernateLogicLocator;
import org.sakaiproject.sms.logic.hibernate.exception.SmsAccountNotFoundException;
import org.sakaiproject.sms.logic.hibernate.exception.SmsTaskNotFoundException;
import org.sakaiproject.sms.logic.incoming.ParsedMessage;
import org.sakaiproject.sms.logic.incoming.SmsIncomingLogicManager;
import org.sakaiproject.sms.logic.incoming.SmsMessageParser;
import org.sakaiproject.sms.logic.smpp.SmsBilling;
import org.sakaiproject.sms.logic.smpp.SmsCore;
import org.sakaiproject.sms.logic.smpp.SmsSmpp;
import org.sakaiproject.sms.logic.smpp.SmsTaskValidationException;
import org.sakaiproject.sms.logic.smpp.exception.MoDisabledForSiteException;
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
import org.sakaiproject.sms.model.hibernate.constants.SmsConstants;
import org.sakaiproject.sms.model.hibernate.constants.ValidationConstants;
import org.sakaiproject.sms.util.DateUtil;

/**
 * Handle all core logic regarding SMPP gateway communication.
 * 
 * @author etienne@psybergate.co.za
 * 
 */
public class SmsCoreImpl implements SmsCore {

	private static final Log LOG = LogFactory.getLog(SmsCoreImpl.class);

	private static final int MO_OVERDRAFT_EMAIL_INTERVAL = 2;

	private Calendar lastSendMoOverdraftEmail = null;

	public Calendar getLastSendMoOverdraftEmail() {
		return lastSendMoOverdraftEmail;
	}

	private ExternalLogic externalLogic;

	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	public void setLastSendMoOverdraftEmail(Calendar lastSendMoOverdraftEmail) {
		this.lastSendMoOverdraftEmail = lastSendMoOverdraftEmail;
	}

	private final ThreadGroup smsThreadGroup = new ThreadGroup(
			SmsConstants.SMS_TASK_PROCESSING_THREAD_GROUP_NAME);

	private SmsMessageParser smsMessageParser;

	public SmsMessageParser getSmsMessageParser() {
		return smsMessageParser;
	}

	public void setSmsMessageParser(SmsMessageParser smsMessageParser) {
		this.smsMessageParser = smsMessageParser;
	}

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

	private SmsIncomingLogicManager smsIncomingLogicManager;

	public void setSmsIncomingLogicManager(
			SmsIncomingLogicManager smsIncomingLogicManager) {
		this.smsIncomingLogicManager = smsIncomingLogicManager;
	}

	public SmsIncomingLogicManager getSmsIncomingLogicManager() {
		return smsIncomingLogicManager;
	}

	public SmsSmpp smsSmpp = null;

	public SmsBilling smsBilling = null;

	public SmsTask calculateEstimatedGroupSize(final SmsTask smsTask) {
		final Set<SmsMessage> messages = hibernateLogicLocator
				.getExternalLogic().getSakaiGroupMembers(smsTask, true);
		final int groupSize = messages.size();
		smsTask.setGroupSizeEstimate(groupSize);
		// one sms always costs one credit
		smsTask.setCreditEstimate(groupSize);
		smsTask.setCostEstimate(smsBilling.convertCreditsToAmount(groupSize)
				.doubleValue());
		smsTask.setCreditCost(hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSystemSmsConfig().getCreditCost());
		return smsTask;
	}

	/**
	 * Thread to handle all processing of tasks.
	 * 
	 * @author void
	 * 
	 */
	private class ProcessThread implements Runnable {

		private SmsTask smsTask;

		public SmsTask getSmsTask() {
			return smsTask;
		}

		public void setSmsTask(SmsTask smsTask) {
			this.smsTask = smsTask;
		}

		ProcessThread(SmsTask smsTask, ThreadGroup threadGroup) {
			setSmsTask(smsTask);
			final Thread thread = new Thread(threadGroup, this);
			thread.setDaemon(true);
			thread.start();

		}

		public void run() {
			work();
		}

		public void work() {
			processTask(smsTask);

		}
	}

	/**
	 * Method sets the sms Messages on the task and calculates the actual group
	 * size.
	 * 
	 * @param smsTask
	 * @return
	 */
	private SmsTask calculateActualGroupSize(SmsTask smsTask) {
		final Set<SmsMessage> messages = hibernateLogicLocator
				.getExternalLogic().getSakaiGroupMembers(smsTask, true);
		smsTask.setSmsMessagesOnTask(messages);
		smsTask.setGroupSizeActual(messages.size());
		return smsTask;
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
	public SmsTask getPreliminaryTestTask(String sakaiSiteID, String sakaiToolId) {
		return getPreliminaryTask(null, null, null, new Date(), "",
				sakaiSiteID, sakaiToolId, "", null);
	}

	private SmsTask getPreliminaryTask(String deliverGroupId,
			Set<String> mobileNumbers, Set<String> sakaiUserIds,
			Date dateToSend, String messageBody, String sakaiSiteID,
			String sakaiToolId, String sakaiSenderID,
			List<String> deliveryEntityList) {
		final SmsConfig siteConfig = hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSystemSmsConfig();
		final SmsConfig systemConfig = hibernateLogicLocator
				.getSmsConfigLogic().getOrCreateSystemSmsConfig();

		final SmsTask smsTask = new SmsTask();
		try {
			smsTask.setSmsAccountId(smsBilling.getAccountID(sakaiSiteID,
					sakaiSenderID));
		} catch (SmsAccountNotFoundException e) {
			LOG.error("Sms account not found  for sakaiSiteID:=" + sakaiSiteID
					+ " sakaiSenderID:= " + sakaiSenderID);
			LOG.error(e.getMessage(), e);
			return null;
		}
		smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		smsTask.setSakaiSiteId(sakaiSiteID);
		smsTask.setMessageTypeId(SmsConstants.MESSAGE_TYPE_SYSTEM_ORIGINATING);
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
		final Calendar cal = Calendar.getInstance();
		cal.setTime(smsTask.getDateToSend());
		cal.add(Calendar.SECOND, smsTask.getMaxTimeToLive());
		// TODO, DateToExpire must be set from the UI as well
		smsTask.setDateToExpire(cal.getTime());
		smsTask.setDelReportTimeoutDuration(systemConfig
				.getDelReportTimeoutDuration());
		smsTask.setDeliveryMobileNumbersSet(mobileNumbers);
		smsTask.setDeliveryEntityList(deliveryEntityList);
		smsTask.setSakaiUserIdsList(sakaiUserIds);
		smsTask.setCreditCost(smsBilling.convertCreditsToAmount(1));
		return smsTask;
	}

	// We answer back by creating a new sms task with one sms message attached
	// to it. The task will then be handled like any other MO task.
	private SmsTask getPreliminaryMOTask(String mobilenumber, Date dateToSend,
			String sakaiSiteID, String sakaiToolId, String sakaiSenderID) {
		final Set<String> number = new HashSet<String>();
		number.add(mobilenumber);
		final SmsTask smsTask = getPreliminaryTask(dateToSend, "", sakaiSiteID,
				sakaiToolId, sakaiSenderID, number);
		if (smsTask != null) {
			smsTask
					.setMessageTypeId(SmsConstants.MESSAGE_TYPE_MOBILE_ORIGINATING);
			smsTask.setGroupSizeEstimate(1);
			smsTask.setGroupSizeActual(1);
			smsTask.setCreditEstimate(1);
			try {
				smsTask.setSmsAccountId(smsBilling
						.getAccountID(sakaiSiteID, ""));
			} catch (SmsAccountNotFoundException e) {
				LOG.error(e.getMessage(), e);

			}
		}
		return smsTask;
	}

	public SmsBilling getSmsBilling() {
		return smsBilling;
	}

	public SmsSmpp getSmsSmpp() {
		return smsSmpp;
	}

	public void init() {
		LOG.info("SmsCoreImpl online");
	}

	public SmsTask insertTask(SmsTask smsTask)
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
			throw new SmsSendDisabledException(smsTask);
		}
		if (smsTask.getMessageTypeId() != SmsConstants.MESSAGE_TYPE_MOBILE_ORIGINATING) {
			smsTask
					.setMessageTypeId(SmsConstants.MESSAGE_TYPE_SYSTEM_ORIGINATING);
		}
		final Set<SmsMessage> smsMessages = (Set<SmsMessage>) ((HashSet<SmsMessage>) smsTask
				.getSmsMessages()).clone();

		smsTask.setSmsMessages(null);

		// Cross-check account info. {@link
		// org.sakaiproject.sms.entity.SmsTaskEntityProviderImpl NEW does
		// not
		// set this
		if (smsTask.getSmsAccountId() == null) {
			SmsAccount account = hibernateLogicLocator.getSmsAccountLogic()
					.getSmsAccount(smsTask.getSakaiSiteId(),
							smsTask.getSenderUserId());
			if (account != null) {
				smsTask.setSmsAccountId(account.getId());
			}
		}

		// Set messageType, expiry date, TTL, report tiemout.
		SmsConfig siteConfig = hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSystemSmsConfig();
		SmsConfig systemConfig = hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSystemSmsConfig();

		// Set DateToExpire to getMaxTimeToLive if it aint set in the UI
		smsTask.setMaxTimeToLive(siteConfig.getSmsTaskMaxLifeTime());
		if (smsTask.getDateToExpire() == null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(smsTask.getDateToSend());
			cal.add(Calendar.SECOND, smsTask.getMaxTimeToLive());
			smsTask.setDateToExpire(cal.getTime());
		}
		smsTask.setDelReportTimeoutDuration(systemConfig
				.getDelReportTimeoutDuration());

		smsTask.setAttemptCount(0);

		ArrayList<String> errors = new ArrayList<String>();
		errors.addAll(smsTaskValidator.validateInsertTask(smsTask));
		if (!errors.isEmpty()) {
			// Do not persist, just throw exception
			SmsTaskValidationException validationException = new SmsTaskValidationException(
					errors,
					MessageCatalog
							.getMessage("messages.sms.errors.task.validationFailed"));
			LOG.error(MessageCatalog
					.getMessage("messages.sms.errors.task.validationFailed")
					+ ": " + validationException.getErrorMessagesAsBlock());
			throw validationException;
		}

		// we set the date again due to time laps between getPreliminaryTask
		// and
		// insertask
		smsTask.setDateCreated(DateUtil.getCurrentDate());
		errors.clear();
		smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_BUSY);
		hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);
		if (!smsBilling.reserveCredits(smsTask)) {
			ArrayList<String> insufficientCredit = new ArrayList<String>();
			insufficientCredit.add(ValidationConstants.INSUFFICIENT_CREDIT
					+ " in account id " + smsTask.getSmsAccountId());
			if (smsTask.getMessageTypeId().equals(
					SmsConstants.MESSAGE_TYPE_MOBILE_ORIGINATING)) {
				if (lastSendMoOverdraftEmail == null) {
					sendEmailNotification(smsTask,
							SmsConstants.ACCOUNT_OVERDRAFT_LIMIT_EXCEEDED_MO);
					setLastSendMoOverdraftEmail(Calendar.getInstance());
				} else {
					Calendar now = Calendar.getInstance();
					Calendar previousSendmail = Calendar.getInstance();
					previousSendmail.setTime(getLastSendMoOverdraftEmail()
							.getTime());
					previousSendmail.add(Calendar.HOUR,
							MO_OVERDRAFT_EMAIL_INTERVAL);
					if (previousSendmail.before(now)) {
						sendEmailNotification(
								smsTask,
								SmsConstants.ACCOUNT_OVERDRAFT_LIMIT_EXCEEDED_MO);
						setLastSendMoOverdraftEmail(Calendar.getInstance());
					}

				}
			}
			errors.addAll(insufficientCredit);

		}

		if (!errors.isEmpty()) {
			SmsTaskValidationException validationException = new SmsTaskValidationException(
					errors,
					MessageCatalog
							.getMessage("messages.sms.errors.task.validationFailed"));
			smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_FAIL);
			smsTask
					.setFailReason(validationException
							.getErrorMessagesAsBlock());

			hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);
			LOG.error(MessageCatalog
					.getMessage("messages.sms.errors.task.validationFailed")
					+ ": " + validationException.getErrorMessagesAsBlock());

			throw validationException;
		} else {
			smsTask.setSmsMessages(smsMessages);
			smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
			hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);
			// only try this if this node is designated to bind
			if (externalLogic.isNodeBindToGateway()) {
				tryProcessTaskRealTime(smsTask);
			}
		}

		externalLogic.postEvent(ExternalLogic.SMS_EVENT_TASK_CREATE, "/sms-task/" + smsTask.getId(), smsTask.getSakaiSiteId());
		
		return smsTask;
	}

	/**
	 * {@inheritDoc}
	 */
	public void processIncomingMessage(String smsMessagebody,
			String mobileNumber) {

		String smsMessageReplyBody = "";
		ParsedMessage parsedMessage = null;
		try {
			parsedMessage = getSmsIncomingLogicManager().process(
					smsMessagebody, mobileNumber);
		} catch (MoDisabledForSiteException exeption) {
			LOG.error(exeption.getMessage());
			return;

		}
		if (parsedMessage != null) {
			if (parsedMessage.getBodyReply() != null
					&& !parsedMessage.getBodyReply().equals(
							SmsConstants.SMS_MO_EMPTY_REPLY_BODY)) {
				smsMessageReplyBody = parsedMessage.getBodyReply();
				LOG.debug((parsedMessage.getCommand() != null ? "Command "
						+ parsedMessage.getCommand() : "System")
						+ " answered back with: " + smsMessageReplyBody);

			} else if ((parsedMessage.getBodyReply() == null)
					|| parsedMessage.getBodyReply().equals(
							SmsConstants.SMS_MO_EMPTY_REPLY_BODY)) {
				return;
			} else {
				smsMessageReplyBody = "No tool found.";
			}
		} else {
			return;
		}
		final SmsConfig configSite = hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSmsConfigBySakaiSiteId(parsedMessage.getSite());
		if (!configSite.isReceiveIncomingEnabled()) {
			LOG
					.info("Receiving of Mobile Originating messages is disabled for site:"
							+ configSite.getSakaiSiteId());
			return;
		}
		SmsMessage smsMessage = new SmsMessage(mobileNumber);
		// TODO Who will be the sakai user that will "send" the reply
		SmsTask smsTask = getPreliminaryMOTask(smsMessage.getMobileNumber(),
				new Date(), parsedMessage.getSite(), null,
				SmsConstants.DEFAULT_MO_SENDER_USERNAME);

		if (smsTask == null) {
			return;
		}
		smsMessage.setSmsTask(smsTask);
		// TODO: who must te sakai user Id be for the reply?
		smsMessage.setSakaiUserId(SmsConstants.DEFAULT_MO_SENDER_USERNAME);
		Set<SmsMessage> smsMessages = new HashSet<SmsMessage>();
		smsMessage.setMessageReplyBody(smsMessageReplyBody);
		smsMessage.setMessageBody(smsMessagebody);
		smsMessages.add(smsMessage);
		smsTask.setSmsMessagesOnTask(smsMessages);

		try {
			insertTask(smsTask);
		} catch (SmsTaskValidationException e) {

		} catch (SmsSendDeniedException e) {
			LOG.error(getExceptionStackTraceAsString(e), e);

		} catch (SmsSendDisabledException e) {
			LOG.error(getExceptionStackTraceAsString(e), e);

		}

	}

	public void processNextTask() {
		synchronized (this) {
			if (!externalLogic.isNodeBindToGateway()) {
				return;
			}

			SmsTask smsTask = hibernateLogicLocator.getSmsTaskLogic()
					.getNextSmsTask();

			if (smsTask != null) {
				LOG.debug("Processing next task");
				processTaskInThread(smsTask, smsThreadGroup);
			}
		}
	}

	public void processTask(SmsTask smsTask) {

		LOG.debug("Processing task:" + smsTask.getId());
		smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_BUSY);
		hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);
		try {
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
						SmsConstants.TASK_NOTIFICATION_FAILED);
				smsBilling.cancelPendingRequest(smsTask.getId());
				smsTask.setFailReason(MessageCatalog
						.getMessage("messages.taskExpired"));
				hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);
				return;
			}

			// /hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);
			if (smsTask.getAttemptCount() < systemConfig.getSmsRetryMaxCount()) {
				if ((!smsTask.getMessageTypeId().equals(
						SmsConstants.MESSAGE_TYPE_MOBILE_ORIGINATING) && smsTask
						.getAttemptCount() <= 1)) {
					calculateActualGroupSize(smsTask);
					hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(
							smsTask);
				}
				// ========================== Do the actual sending to the
				// gateway

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
					smsTask
							.rescheduleDateToSend(new Date(now
									.getTimeInMillis()));
				}

			} else {
				smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_FAIL);
				smsTask.setStatusForMessages(
						SmsConst_DeliveryStatus.STATUS_PENDING,
						SmsConst_DeliveryStatus.STATUS_FAIL);
				sendEmailNotification(smsTask,
						SmsConstants.TASK_NOTIFICATION_FAILED);
				smsTask.setFailReason(MessageCatalog.getMessage(
						"messages.taskRetryFailure", String
								.valueOf(systemConfig.getSmsRetryMaxCount())));
				smsBilling.cancelPendingRequest(smsTask.getId());
			}
			hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);
		} catch (Exception e) {
			LOG.error(getExceptionStackTraceAsString(e), e);
			smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_FAIL);
			smsTask.setFailReason(e.toString());
			smsBilling.settleCreditDifference(smsTask);
			hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);
			sendEmailNotification(smsTask,
					SmsConstants.TASK_NOTIFICATION_EXCEPTION,
					getExceptionStackTraceAsString(e));

		}

	}

	private String getExceptionStackTraceAsString(Exception exception) {
		StringWriter stringWriter = new StringWriter();
		exception.printStackTrace(new PrintWriter(stringWriter));
		return stringWriter.toString();
	}

	public void processTimedOutDeliveryReports() {
		List<SmsMessage> smsMessages = hibernateLogicLocator
				.getSmsMessageLogic().getSmsMessagesWithStatus(null,
						SmsConst_DeliveryStatus.STATUS_SENT);

		if (smsMessages != null) {
			Calendar currentTime = Calendar.getInstance();
			for (SmsMessage message : smsMessages) {
				SmsTask task = message.getSmsTask();
				if (task.getDateProcessed() != null) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(task.getDateProcessed());
					cal
							.add(Calendar.SECOND, task
									.getDelReportTimeoutDuration());
					if (cal.getTime().before(currentTime.getTime())) {
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
		return sendEmailNotification(smsTask, taskMessageType, "");
	}

	/**
	 * Send a email notification out.
	 * 
	 * @param smsTask
	 * @param taskMessageType
	 * @param additionInformation
	 * @return
	 */
	private boolean sendEmailNotification(SmsTask smsTask,
			Integer taskMessageType, String additionInformation) {

		if (smsTask == null || smsTask.getMessageBody() == null
				|| smsTask.getMessageBody().equals("")
				|| taskMessageType == null) {
			LOG.error("sendEmailNotification: smsTask or taskMessageType may not to null");
			return false;
		}

		if (!smsTask.getMessageTypeId().equals(SmsConstants.MESSAGE_TYPE_SYSTEM_ORIGINATING)) {
			LOG.debug("Email notifications only sent for system originating messages, ignoring task id = " + smsTask.getId());
			return false;
		}
		
		LOG.debug("sendEmailNotification: task id = " + smsTask.getId());
		
		if (additionInformation == null) {
			additionInformation = "";
		}

		String subject = null;
		String body = null;
		String ownerToAddress = null;
		String notiToAddress = null;

		SmsConfig configSystem = hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSystemSmsConfig();

		// Get the balance available to calculate the available credit.
		SmsAccount account = hibernateLogicLocator.getSmsAccountLogic()
				.getSmsAccount(smsTask.getSmsAccountId());
		if (account == null) {
			LOG.debug("No account associated with task id = " + smsTask.getId());
			return false;
		}
		Long credits = account.getCredits();

		if (!account.getAccountEnabled()) {
			credits = 0L;
		} else if (account.getOverdraftLimit() != null) {
			// Add the overdraft to the available balance
			credits += account.getOverdraftLimit();
		}

		String creditsAvailable = Long.toString(credits);
		String creditsRequired = "";
		if (smsTask.getCreditEstimate() != null) {
			creditsRequired = Long.toString(smsTask.getCreditEstimate());
		}
		
		// Email address for the task owner
		ownerToAddress = hibernateLogicLocator.getExternalLogic()
				.getSakaiEmailAddressForUserId(smsTask.getSenderUserId());
		
		// Email address for the site
		notiToAddress = account.getNotificationEmail();

		// TODO - Use the EmailTemplateService to construct message bodies
		
		if (taskMessageType.equals(SmsConstants.TASK_NOTIFICATION_STARTED)) {
			subject = MessageCatalog.getMessage(
					"messages.notificationSubjectStarted", smsTask.getId()
							.toString());
			body = MessageCatalog.getMessage(
					"messages.notificationBodyStarted", creditsRequired,
					creditsAvailable);

		} else if (taskMessageType.equals(SmsConstants.TASK_NOTIFICATION_SENT)) {
			subject = MessageCatalog.getMessage(
					"messages.notificationSubjectSent", smsTask.getId()
							.toString());
			body = MessageCatalog.getMessage("messages.notificationBodySent",
					creditsRequired, creditsAvailable);

		} else if (taskMessageType
				.equals(SmsConstants.TASK_NOTIFICATION_EXCEPTION)) {
			subject = MessageCatalog.getMessage(
					"messages.notificationSubjectException", smsTask.getId()
							.toString());
			body = additionInformation;

		} else if (taskMessageType
				.equals(SmsConstants.ACCOUNT_OVERDRAFT_LIMIT_EXCEEDED)) {
			subject = MessageCatalog
					.getMessage("messages.notificationSubjectOverdraftLimitExceeded");
			body = MessageCatalog.getMessage(
					"messages.notificationOverdraftLimitExceeded", String
							.valueOf(account.getCredits()), String.valueOf((-1)
							* (account.getOverdraftLimit() + account
									.getCredits())));

		} else if (taskMessageType
				.equals(SmsConstants.ACCOUNT_OVERDRAFT_LIMIT_EXCEEDED_MO)) {
			subject = MessageCatalog
					.getMessage("messages.notificationSubjectOverdraftLimitExceeded");
			body = MessageCatalog.getMessage(
					"messages.notificationMOSubjectOverdraftLimitExceeded",
					String.valueOf(account.getCredits()));

		} else if (taskMessageType.equals(SmsConstants.TASK_NOTIFICATION_EXPIRED)) {
			subject = MessageCatalog.getMessage(
					"messages.notificationSubjectExpired", smsTask.getId()
							.toString());
			body = MessageCatalog
					.getMessage("messages.notificationBodyExpired");

		} else if (taskMessageType
				.equals(SmsConstants.TASK_NOTIFICATION_COMPLETED)) {
			subject = MessageCatalog.getMessage(
					"messages.notificationSubjectCompleted", smsTask.getId()
							.toString());
			body = MessageCatalog.getMessage(
					"messages.notificationBodyCompleted", String
							.valueOf(smsTask.getMessagesProcessed()), String
							.valueOf(smsTask.getMessagesDelivered()));

		} else if (taskMessageType
				.equals(SmsConstants.TASK_NOTIFICATION_ABORTED)) {
			subject = MessageCatalog.getMessage(
					"messages.notificationSubjectAborted", smsTask.getId()
							.toString());
			body = MessageCatalog.getMessage(
					"messages.notificationBodyAborted", smsTask
							.getSenderUserName());

		} else if (taskMessageType
				.equals(SmsConstants.TASK_NOTIFICATION_FAILED)) {
			subject = MessageCatalog.getMessage(
					"messages.notificationSubjectFailed", smsTask.getId()
							.toString());
			body = MessageCatalog.getMessage("messages.notificationBodyFailed",
					String.valueOf(configSystem.getSmsRetryMaxCount()));

		} else if (taskMessageType
				.equals(SmsConstants.TASK_INSUFFICIENT_CREDITS)) {
			subject = MessageCatalog
					.getMessage("messages.notificationSubjectTaskInsufficientCredits");
			body = MessageCatalog.getMessage(
					"messages.notificationBodyTaskInsufficientCredits", String
							.valueOf(account.getOverdraftLimit()), String
							.valueOf(account.getOverdraftLimit()
									+ account.getCredits()));

		}
		boolean accountNotification = false;
		boolean ownerNotification = false;
		
		if (notiToAddress != null && notiToAddress.length() > 0) {
			accountNotification = sendNotificationEmail(smsTask, notiToAddress, subject, body);
		}

		if (ownerToAddress != null && ownerToAddress.length() > 0 && !ownerToAddress.equals(notiToAddress)) {
			ownerNotification = sendNotificationEmail(smsTask, ownerToAddress, subject, body);
		}

		return (accountNotification && ownerNotification);
	}

	public void setSmsBilling(SmsBilling smsBilling) {
		this.smsBilling = smsBilling;
	}

	public void setSmsSmpp(SmsSmpp smsSmpp) {
		this.smsSmpp = smsSmpp;
	}

	public void tryProcessTaskRealTime(SmsTask smsTask) {

		if (!externalLogic.isNodeBindToGateway()) {
			return;
		}

		if (smsTask.getDateToSend().getTime() <= System.currentTimeMillis()) {
			processTaskInThread(smsTask, smsThreadGroup);
		}
	}

	public void checkAndSetTasksCompleted() {

		List<SmsTask> smsTasks = hibernateLogicLocator.getSmsTaskLogic()
				.checkAndSetTasksCompleted();

		for (SmsTask smsTask : smsTasks) {
			smsBilling.settleCreditDifference(smsTask);
			checkOverdraft(smsTask);
			if (smsTask.getMessageTypeId().equals(
					SmsConstants.MESSAGE_TYPE_SYSTEM_ORIGINATING)) {
				sendEmailNotification(smsTask,
						SmsConstants.TASK_NOTIFICATION_COMPLETED);
			} else {
				if (smsTask.getSmsMessages() != null) {
					for (SmsMessage smsMessages : smsTask.getSmsMessages()) {
						if (smsMessages.getStatusCode().equals(
								SmsConst_DeliveryStatus.STATUS_ERROR)
								|| smsMessages.getStatusCode().equals(
										SmsConst_DeliveryStatus.STATUS_FAIL)) {
							smsTask
									.setStatusCode(SmsConst_DeliveryStatus.STATUS_FAIL);
							smsTask.setFailReason(smsMessages.getFailReason());
							hibernateLogicLocator.getSmsTaskLogic()
									.persistSmsTask(smsTask);
						}
					}
				}
			}
		}
	}

	private void checkOverdraft(SmsTask smsTask) {
		SmsAccount account = hibernateLogicLocator.getSmsAccountLogic()
				.getSmsAccount(smsTask.getSmsAccountId());
		if (account.getOverdraftLimit() != null
				&& (account.getCredits() < (-1 * account.getOverdraftLimit()))) {

			sendEmailNotification(smsTask,
					SmsConstants.ACCOUNT_OVERDRAFT_LIMIT_EXCEEDED);

		}
	}

	public void processVeryLateDeliveryReports() {
		List<SmsMessage> messages = hibernateLogicLocator.getSmsMessageLogic()
				.getSmsMessagesWithStatus(null,
						SmsConst_DeliveryStatus.STATUS_LATE);

		for (SmsMessage smsMessage : messages) {
			if (smsMessage.getSmscDeliveryStatusCode() == SmsConst_SmscDeliveryStatus.DELIVERED) {
				smsMessage
						.setStatusCode(SmsConst_DeliveryStatus.STATUS_DELIVERED);
				hibernateLogicLocator.getSmsTaskLogic()
						.incrementMessagesDelivered(smsMessage.getSmsTask());
				smsBilling.debitLateMessage(smsMessage);
			} else {
				smsMessage.setStatusCode(SmsConst_DeliveryStatus.STATUS_FAIL);
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
			if (smsTask.getStatusCode().equals(
					SmsConst_DeliveryStatus.STATUS_PENDING)) {
				smsBilling.cancelPendingRequest(smsTaskID);
				smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_ABORT);
				smsTask.setStatusForMessages(
						SmsConst_DeliveryStatus.STATUS_PENDING,
						SmsConst_DeliveryStatus.STATUS_ABORT);
				smsTask.setFailReason(MessageCatalog
						.getMessage("messages.taskAborted"));
				hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);
				sendEmailNotification(smsTask,
						SmsConstants.TASK_NOTIFICATION_ABORTED);
			}
		}

	}

	public void processMOTasks() {
		if (!externalLogic.isNodeBindToGateway()) {
			return;
		}

		List<SmsTask> moTasks = hibernateLogicLocator.getSmsTaskLogic()
				.getAllMOTasks();
		if (moTasks != null) {
			for (SmsTask smsTask : moTasks) {
				processTaskInThread(smsTask, smsThreadGroup);
			}
		}
	}

	public void processTaskInThread(SmsTask smsTask, ThreadGroup threadGroup) {

		LOG.debug("Number of active threads in processTaskInThread:"
				+ threadGroup.activeCount());
		int maxThreadCount = hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSystemSmsConfig().getMaxActiveThreads();
		if ((threadGroup.activeCount() < maxThreadCount)) {
			new ProcessThread(smsTask, threadGroup);
		} else {
			smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_RETRY);
			hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);
			LOG.debug("Maximum allowed SMS threads of " + maxThreadCount
					+ " reached. Task will be scheduled for later processing.");
		}

	}
}
