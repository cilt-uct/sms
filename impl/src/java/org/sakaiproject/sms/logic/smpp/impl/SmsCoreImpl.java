/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009 The Sakai Foundation
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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.sakaiproject.sms.bean.SearchFilterBean;
import org.sakaiproject.sms.logic.HibernateLogicLocator;
import org.sakaiproject.sms.logic.exception.SmsAccountNotFoundException;
import org.sakaiproject.sms.logic.exception.SmsTaskNotFoundException;
import org.sakaiproject.sms.logic.external.ExternalEmailLogic;
import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.external.ExternalMessageSending;
import org.sakaiproject.sms.logic.external.NumberRoutingHelper;
import org.sakaiproject.sms.logic.incoming.ParsedMessage;
import org.sakaiproject.sms.logic.incoming.SmsIncomingLogicManager;
import org.sakaiproject.sms.logic.incoming.SmsMessageParser;
import org.sakaiproject.sms.logic.smpp.SmsBilling;
import org.sakaiproject.sms.logic.smpp.SmsCore;
import org.sakaiproject.sms.logic.smpp.SmsSmpp;
import org.sakaiproject.sms.logic.smpp.SmsTaskValidationException;
import org.sakaiproject.sms.logic.smpp.exception.SmsSendDeniedException;
import org.sakaiproject.sms.logic.smpp.exception.SmsSendDisabledException;
import org.sakaiproject.sms.logic.smpp.util.MessageCatalog;
import org.sakaiproject.sms.logic.smpp.validate.SmsTaskValidator;
import org.sakaiproject.sms.model.SmsAccount;
import org.sakaiproject.sms.model.SmsConfig;
import org.sakaiproject.sms.model.SmsMOMessage;
import org.sakaiproject.sms.model.SmsMessage;
import org.sakaiproject.sms.model.SmsTask;
import org.sakaiproject.sms.model.constants.SmsConst_DeliveryStatus;
import org.sakaiproject.sms.model.constants.SmsConstants;
import org.sakaiproject.sms.model.constants.ValidationConstants;
import org.sakaiproject.sms.util.DateUtil;
import org.sakaiproject.sms.util.SmsMessageUtil;

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

	private ExternalEmailLogic externalEmailLogic;	
	public void setExternalEmailLogic(ExternalEmailLogic externalEmailLogic) {
		this.externalEmailLogic = externalEmailLogic;
	}

	public void setLastSendMoOverdraftEmail(Calendar lastSendMoOverdraftEmail) {
		this.lastSendMoOverdraftEmail = lastSendMoOverdraftEmail;
	}

	private NumberRoutingHelper numberRoutingHelper = null;
	
	public void setNumberRoutingHelper(NumberRoutingHelper numberRoutingHelper) {
		this.numberRoutingHelper = numberRoutingHelper;
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

	public ExternalMessageSending externalMessageSending;	
	public void setExternalMessageSending(
			ExternalMessageSending externalMessageSending) {
		this.externalMessageSending = externalMessageSending;
	}

	public SmsSmpp smsSmpp = null;

	public SmsBilling smsBilling = null;

	public SmsTask calculateEstimatedGroupSize(final SmsTask smsTask) {
		final Set<SmsMessage> messages = hibernateLogicLocator
				.getExternalLogic().getSakaiGroupMembers(smsTask, true);

		// iterate through messages and calculate total cost
		double credits = 0;
		int routable = 0;
		
		for (SmsMessage message : messages) {
			if (numberRoutingHelper.getRoutingInfo(message)) {
				credits += message.getCredits();
				routable++;
			}
		}

		smsTask.setGroupSizeEstimate(routable);
		smsTask.setCreditEstimate(credits);
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
	private Set<SmsMessage> calculateActualGroupSize(SmsTask smsTask) {
		final Set<SmsMessage> messages = hibernateLogicLocator
				.getExternalLogic().getSakaiGroupMembers(smsTask, true);
		return messages;
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

		final SmsTask smsTask = new SmsTask();
		try {
			smsTask.setSmsAccountId(smsBilling.getAccountID(sakaiSiteID,
					sakaiSenderID));
		} catch (SmsAccountNotFoundException e) {
			LOG.error("Sms account not found  for sakaiSiteID:=" + sakaiSiteID
					+ " sakaiSenderID:= " + sakaiSenderID);
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
		smsTask.setDateToExpire(cal.getTime());
		smsTask.setDeliveryMobileNumbersSet(mobileNumbers);
		smsTask.setDeliveryEntityList(deliveryEntityList);
		smsTask.setSakaiUserIdsList(sakaiUserIds);
		smsTask.setCreditCost(smsBilling.convertCreditsToAmount(1));
		return smsTask;
	}

	/**
	 * 	Create a new sms task with one sms message attached to it. The task will then be handled like any other MO task.
	 * @param mobilenumber
	 * @param dateToSend
	 * @param sakaiSiteID
	 * @param sakaiToolId
	 * @param sakaiSenderID
	 * @return
	 */
	private SmsTask getPreliminaryMOTask(String mobilenumber, Date dateToSend,
			String sakaiSiteID, String sakaiToolId, String sakaiSenderID) {
		final Set<String> number = new HashSet<String>();
		number.add(mobilenumber);
		final SmsTask smsTask = getPreliminaryTask(dateToSend, "", sakaiSiteID,
				sakaiToolId, sakaiSenderID, number);
		if (smsTask != null) {
			smsTask.setMessageTypeId(SmsConstants.MESSAGE_TYPE_MOBILE_ORIGINATING);
			smsTask.setGroupSizeEstimate(1);
			smsTask.setGroupSizeActual(1);
			calculateEstimatedGroupSize(smsTask);
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

	@SuppressWarnings("unchecked")
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
		if (!SmsConstants.MESSAGE_TYPE_MOBILE_ORIGINATING.equals(smsTask.getMessageTypeId())) {
			smsTask.setMessageTypeId(SmsConstants.MESSAGE_TYPE_SYSTEM_ORIGINATING);
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

		// Set messageType, expiry date
		SmsConfig siteConfig = hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSystemSmsConfig();

		// Set DateToSend to now if not set
		if (smsTask.getDateToSend() == null) {
			Calendar cal = Calendar.getInstance();
			smsTask.setDateToSend(cal.getTime());
		}
		
		// Set DateToExpire to getMaxTimeToLive if it's not set in the UI
		smsTask.setMaxTimeToLive(siteConfig.getSmsTaskMaxLifeTime());
		if (smsTask.getDateToExpire() == null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(smsTask.getDateToSend());
			cal.add(Calendar.SECOND, smsTask.getMaxTimeToLive());
			smsTask.setDateToExpire(cal.getTime());
		}

		smsTask.setAttemptCount(0);

		// Sanitize the character set in the message body
		smsTask.setMessageBody(SmsMessageUtil.sanitizeMessageBody(smsTask.getMessageBody()));
		
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

		// we set the date again due to time lapse between getPreliminaryTask
		// and insert task into the database
		
		smsTask.setDateCreated(DateUtil.getCurrentDate());
		errors.clear();
		smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_BUSY);
		hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);

		externalLogic.postEvent(ExternalLogic.SMS_EVENT_TASK_CREATE,
				"/sms-task/" + smsTask.getId(), smsTask.getSakaiSiteId());
		
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
			smsTask.setFailReason(validationException
							.getErrorMessagesAsBlock());

			hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);
			LOG.error(MessageCatalog
					.getMessage("messages.sms.errors.task.validationFailed")
					+ ": " + validationException.getErrorMessagesAsBlock());

			throw validationException;
		} else {
			smsTask.setSmsMessages(smsMessages);

			for (SmsMessage message : smsTask.getSmsMessages()) {
				hibernateLogicLocator.getSmsMessageLogic().persistSmsMessage(
						message);
			}

			// Deliver in real time if possible, otherwise queue for the scheduler
			
			if (externalLogic.isNodeBindToGateway() &&
			  smsTask.getDateToSend().getTime() <= System.currentTimeMillis()) {
				tryProcessTaskRealTime(smsTask);
			} else {
				smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
				hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);
			}
		}

		return smsTask;
	}

	/**
	 * {@inheritDoc}
	 */
	public void processIncomingMessage(SmsMOMessage inMessage) {

		String smsMessagebody = inMessage.getSmsMessagebody();
		String mobileNumber = inMessage.getMobileNumber();
		
		
		// Allocate the cost of incoming messages, default to admin account

		double routingCredits = numberRoutingHelper.getIncomingMessageCost(inMessage.getSmscId());
		String defaultBillingSite = SmsConstants.SAKAI_SMS_ADMIN_SITE;		
		
		String smsMessageReplyBody = "";
				
		ParsedMessage parsedMessage = getSmsIncomingLogicManager().process(smsMessagebody, mobileNumber);
		
		if (parsedMessage == null) {
			// We don't ever expect a null return value here
			LOG.error("Error parsing incoming message from " + mobileNumber);

			billIncomingMessage(routingCredits, defaultBillingSite, null, null, null);
			return;		
		}

		
		// Check for empty reply body
		if (parsedMessage.getBodyReply() == null
			|| parsedMessage.getBodyReply().equals(
						SmsConstants.SMS_MO_EMPTY_REPLY_BODY)) {
			LOG.debug("No reply to this incoming message.");

			billIncomingMessage(routingCredits, defaultBillingSite, parsedMessage.getSite(), null, parsedMessage.getAccountId());
			return;
		}
		
		smsMessageReplyBody = parsedMessage.getBodyReply();
			
		LOG.debug((parsedMessage.getCommand() != null ? "Command "
					+ parsedMessage.getCommand() : "System")
					+ " answered back with: " + smsMessageReplyBody);

		/*
		// TODO what if we have no site?
		final SmsConfig configSite = hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSmsConfigBySakaiSiteId(parsedMessage.getSite());

		if (!configSite.isReceiveIncomingEnabled()) {
			LOG.info("Receiving of Mobile Originating messages is disabled for site:"
							+ configSite.getSakaiSiteId());
			
			// Bill default site rather than this site
			billIncomingMessage(routingCredits, defaultBillingAccount, null, null);
			return;
		}
		*/
		
		SmsMessage smsMessage = new SmsMessage(mobileNumber);

		SmsTask smsTask = getPreliminaryMOTask(smsMessage.getMobileNumber(),
				new Date(), parsedMessage.getSite() == null ? defaultBillingSite : parsedMessage.getSite(), 
				null, SmsConstants.DEFAULT_MO_SENDER_USERNAME);

		if (smsTask == null) {
			// Only failure case here is account not found but should never happen because it will
			// use the default MO billing account if necessary
			billIncomingMessage(routingCredits, defaultBillingSite, parsedMessage.getSite(), null, parsedMessage.getAccountId());
			return;
		}

		smsMessage.setSmsTask(smsTask);

		smsMessage.setSakaiUserId(parsedMessage.getIncomingUserId());
		smsMessage.setMessageReplyBody(smsMessageReplyBody);
		smsMessage.setMessageBody(smsMessagebody);

		Set<SmsMessage> smsMessages = new HashSet<SmsMessage>();
		smsMessages.add(smsMessage);
		smsTask.setSmsMessagesOnTask(smsMessages);

		// Send reply
		
		SmsTask outTask = null;
		
		try {
			outTask = insertTask(smsTask);
		} catch (SmsTaskValidationException e) {
			LOG.error("Task validation failed: ", e);
		} catch (SmsSendDeniedException e) {
			LOG.error(getExceptionStackTraceAsString(e), e);
		} catch (SmsSendDisabledException e) {
			LOG.error(getExceptionStackTraceAsString(e), e);
		}
		
		billIncomingMessage(routingCredits, defaultBillingSite, parsedMessage.getSite(),
				outTask != null ? outTask.getId() : null, parsedMessage.getAccountId());
		
		return;
	}

	private void billIncomingMessage(double credits, String defaultSiteId, String parsedSiteId, Long taskId, Long accountId) {

		if ((defaultSiteId == null && parsedSiteId == null) || (credits == 0)) {
			// nothing to do 
			return;
		}
		SmsAccount account = null;
		if (accountId != null) {
			account = hibernateLogicLocator.getSmsAccountLogic().getSmsAccount(accountId);
		} else {
			account = hibernateLogicLocator.getSmsAccountLogic().getSmsAccount(
				parsedSiteId != null ? parsedSiteId : defaultSiteId, null);
		}
		
		
		if (account != null) {
			smsBilling.debitIncomingMessage(account, credits, taskId);	
		} else {
			account = hibernateLogicLocator.getSmsAccountLogic().getSmsAccount(defaultSiteId, null);
			if (account != null) {
				smsBilling.debitIncomingMessage(account, credits, taskId);
			} else {
				LOG.warn("Unable to debit cost of incoming message");
			}
		}
		
		return;
	}
	
	public void processNextTask() {
		synchronized (this) {
			if (externalMessageSending == null && !externalLogic.isNodeBindToGateway()) {
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

	public void processSOTasks() {
		synchronized (this) {
			
			if (externalMessageSending == null && !externalLogic.isNodeBindToGateway()) {
				return;
			}

			SmsTask smsTask = hibernateLogicLocator.getSmsTaskLogic()
			.getNextSmsTask();
			while (smsTask != null) {
				processTaskInThread(smsTask, smsThreadGroup);
				
				smsTask = hibernateLogicLocator.getSmsTaskLogic()
				.getNextSmsTask();
			}
		}
	}
	
	
	public void processTask(SmsTask smsTask) {

		if (!SmsConst_DeliveryStatus.STATUS_BUSY.equals(smsTask.getStatusCode())) {
			throw new IllegalStateException("Task " + smsTask.getId() + 
					" handed to processTask() but is not in BUSY state");
		}

		// Has the task expired ?
		
		if (smsTask.getDateToExpire().before(new Date())) {

			LOG.info("Task expired: id = " + 
					smsTask.getId() + " expiry time " + smsTask.getDateToExpire());
			
			hibernateLogicLocator.getSmsMessageLogic().updateStatusForMessages(
					smsTask.getId(),
					SmsConst_DeliveryStatus.STATUS_PENDING,
					SmsConst_DeliveryStatus.STATUS_EXPIRE);

			smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_EXPIRE);
			smsTask.setFailReason(MessageCatalog.getMessage("messages.taskExpired"));
			hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);

			smsBilling.cancelPendingRequest(smsTask.getId());
			sendEmailNotification(smsTask,
					SmsConstants.TASK_NOTIFICATION_EXPIRED);

			return;
		}

		// Has the task exceeded its retry count?

		SmsConfig systemConfig = hibernateLogicLocator.getSmsConfigLogic()
			.getOrCreateSystemSmsConfig();

		if (smsTask.getAttemptCount() >= systemConfig.getSmsRetryMaxCount()) {

			LOG.info("Task exceeded retry count: id = " + 
					smsTask.getId() + " attempts = " + smsTask.getAttemptCount());

			hibernateLogicLocator.getSmsMessageLogic().updateStatusForMessages(
					smsTask.getId(),
					SmsConst_DeliveryStatus.STATUS_PENDING,
					SmsConst_DeliveryStatus.STATUS_EXPIRE);

			smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_FAIL);
			smsTask.setFailReason(MessageCatalog.getMessage(
					"messages.taskRetryFailure", String
							.valueOf(systemConfig.getSmsRetryMaxCount())));
			hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);				
			smsBilling.cancelPendingRequest(smsTask.getId());
			sendEmailNotification(smsTask,
					SmsConstants.TASK_NOTIFICATION_FAILED);
			
			return;
		}
			
		// Send the task's messages

		LOG.info("Processing task: " + smsTask.getId());

		Session session = null;
		Transaction tx = null;

		try {
			
			// Expand and persist the message set if this is the first time round
			
			if ((!smsTask.getMessageTypeId().equals(
					SmsConstants.MESSAGE_TYPE_MOBILE_ORIGINATING) 
					&& smsTask.getGroupSizeActual() == null)) {
				Set<SmsMessage> messages = calculateActualGroupSize(smsTask);
				
				// Persist message set
				for (SmsMessage message : messages) {
					hibernateLogicLocator.getSmsMessageLogic()
							.persistSmsMessage(message);
				}
				
				smsTask.setGroupSizeActual(messages.size());
				hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);
				
				smsTask.setSmsMessages(messages);
			}

			// Do the actual sending to the gateway
			
			Set<SmsMessage> messageList = new HashSet<SmsMessage>(
							hibernateLogicLocator
									.getSmsMessageLogic()
									.getSmsMessagesWithStatus(
											smsTask.getId(),
											SmsConst_DeliveryStatus.STATUS_PENDING));

			String submissionStatus = null;
			
			//if set use an external service
			if (externalMessageSending != null) {
				submissionStatus = externalMessageSending.sendMessagesToService(messageList);
			} else {
				submissionStatus = smsSmpp.sendMessagesToGateway(messageList);
			}

			// Calculate number of messages actually sent and the total cost
			double credits = 0;
			int sent = 0;
			int errors = 0;
			int delivered = 0;
			
			for (SmsMessage sentMessage : messageList) {
				if (SmsConst_DeliveryStatus.STATUS_SENT.equals(sentMessage.getStatusCode())) {
					credits += sentMessage.getCredits();
					sent++;
				}

				//if sending externaly the status may be delivered
				if (SmsConst_DeliveryStatus.STATUS_DELIVERED.equals(sentMessage.getStatusCode())) {
					credits += sentMessage.getCredits();
					delivered++;
				}
				
				if (SmsConst_DeliveryStatus.STATUS_ERROR.equals(sentMessage.getStatusCode())) {
					errors++;
				}
			}
			
			// Lock and update the task 
			
			session = getHibernateLogicLocator().getSmsTaskLogic().getNewHibernateSession();
			tx = session.beginTransaction();

			smsTask = (SmsTask) session.get(SmsTask.class, smsTask.getId(), LockMode.UPGRADE);
			
			smsTask.setStatusCode(submissionStatus);
			smsTask.setDateProcessed(new Date());
			
			smsTask.setCreditsActual(smsTask.getCreditsActual() + credits);
			smsTask.setMessagesProcessed(smsTask.getMessagesProcessed() + sent + errors);

			smsTask.setMessagesDelivered(delivered);
			
			if (SmsConst_DeliveryStatus.STATUS_INCOMPLETE.equals(smsTask.getStatusCode()) ||
					SmsConst_DeliveryStatus.STATUS_RETRY.equals(smsTask.getStatusCode())) {
				
				// Reschedule for later delivery if necessary
				smsTask.setAttemptCount((smsTask.getAttemptCount()) + 1);
				smsTask.setNextRetryTime(getNextRetryTime(smsTask.getAttemptCount()));
				
			}
			
			session.update(smsTask);
			tx.commit();
			session.close();
			
		} catch (Exception e) {
			
			LOG.error(getExceptionStackTraceAsString(e), e);
			
			if (tx != null) {
				tx.rollback();
			}
			if (session != null) {
				session.close();
			}
			
			smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_FAIL);
			smsTask.setFailReason(e.toString());
			smsBilling.settleCreditDifference(smsTask, smsTask.getCreditEstimate(), 0);
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
		
		// TODO - SENT messages are billed upfront. If there's no billing
		// change from the lack of a delivery report, then this state
		// transition is not really meaningful.
		
		SmsConfig systemConfig = hibernateLogicLocator.getSmsConfigLogic()
			.getOrCreateSystemSmsConfig();

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, -1 * systemConfig.getDelReportTimeoutDuration());

		LOG.debug("Timing out SENT messages older than " + cal.getTime());
		
		List<SmsMessage> smsMessages = hibernateLogicLocator
				.getSmsMessageLogic().getSmsMessagesForTimeout(cal.getTime());

		if (smsMessages != null && !smsMessages.isEmpty()) {

			LOG.debug("Updating " + smsMessages.size() + " messages from SENT to TIMEOUT");

			Session session = getHibernateLogicLocator().getSmsMessageLogic()
				.getNewHibernateSession();

			for (SmsMessage message : smsMessages) {

				// Change message from SENT to TIMEOUT
				Transaction tx = null;

				try {
					tx = session.beginTransaction();

					// Lock the message row
					SmsMessage smsMessage = (SmsMessage) session.get(
							SmsMessage.class, message.getId(), LockMode.UPGRADE);

					if (SmsConst_DeliveryStatus.STATUS_SENT.equals(smsMessage.getStatusCode())) {
						smsMessage.setStatusCode(SmsConst_DeliveryStatus.STATUS_TIMEOUT);
						session.update(smsMessage);
						tx.commit();
					} else {
						// another process has updated this message, ignore it
						tx.rollback();
					}
				} catch (HibernateException e) {
					LOG.error("Error processing late delivery report for message "
									+ message.getId() + ": ", e);
					if (tx != null && tx.isActive()) {
						tx.rollback();
					}
				}	
				
			} // for
					
			session.close();
			
		} // if

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
			LOG
					.error("sendEmailNotification: smsTask or taskMessageType may not to null");
			return false;
		}

		if (!smsTask.getMessageTypeId().equals(
				SmsConstants.MESSAGE_TYPE_SYSTEM_ORIGINATING)) {
			LOG
					.debug("Email notifications only sent for system originating messages, ignoring task id = "
							+ smsTask.getId());
			return false;
		}

		LOG.debug("sendEmailNotification: task id = " + smsTask.getId());

		if (additionInformation == null) {
			additionInformation = "";
		}

		String ownerToAddress = null;
		String notiToAddress = null;

		SmsConfig configSystem = hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSystemSmsConfig();

		// Get the balance available to calculate the available credit.
		SmsAccount account = hibernateLogicLocator.getSmsAccountLogic()
				.getSmsAccount(smsTask.getSmsAccountId());
		if (account == null) {
			LOG
					.debug("No account associated with task id = "
							+ smsTask.getId());
			return false;
		}
		double credits = account.getCredits();

		if (!account.getAccountEnabled()) {
			credits = 0;
		} else {
			// Add the overdraft to the available balance
			credits += account.getOverdraftLimit();
		}

		// get number format for default locale
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(1);
		
		String creditsAvailable = nf.format(credits);
		String creditsRequired = "";
		creditsRequired = nf.format(smsTask.getCreditEstimate());

		// Email address for the task owner
		ownerToAddress = hibernateLogicLocator.getExternalLogic()
				.getSakaiEmailAddressForUserId(smsTask.getSenderUserId());

		
		
		// Email address for the Account
		String notiUser = account.getNotificationUserId();
		if (notiUser != null) {
			notiToAddress = externalLogic.getSakaiEmailAddressForUserId(notiUser);
		}
			
		

		// TODO - Use the EmailTemplateService to construct message bodies

		Map<String, String> repValues = new HashMap<String, String>();
		repValues.put("taskId", smsTask.getId().toString());
		
		
		String templateKey = null;
		
		if (taskMessageType.equals(SmsConstants.TASK_NOTIFICATION_STARTED)) {
			
			repValues.put("creditsRequired", creditsRequired);
			repValues.put("creditsAvailable", creditsAvailable);
			templateKey = ExternalEmailLogic.TEMPLATE_TASK_STARTED;
			
		} else if (taskMessageType.equals(SmsConstants.TASK_NOTIFICATION_SENT)) {
			
			repValues.put("messagesProcessed", Integer.valueOf(smsTask.getMessagesProcessed()).toString());
			repValues.put("messagesDelivered", Integer.valueOf(smsTask.getMessagesDelivered()).toString());
			templateKey = ExternalEmailLogic.TEMPLATE_TASK_SENT;
			
			} else if (taskMessageType
				.equals(SmsConstants.TASK_NOTIFICATION_EXCEPTION)) {
			
			
			templateKey = ExternalEmailLogic.TEMPLATE_TASK_EXCEPTION;
			repValues.put("moreInfo", additionInformation);
			
		} else if (taskMessageType
				.equals(SmsConstants.ACCOUNT_OVERDRAFT_LIMIT_EXCEEDED)) {
			
			
			repValues.put("creditsAvailable", creditsAvailable);
			repValues.put("overDraftLimit", String.valueOf(account.getOverdraftLimit()));
			templateKey = ExternalEmailLogic.TEMPLATE_TASK_OVER_QUOTA;
			
		} else if (taskMessageType
				.equals(SmsConstants.ACCOUNT_OVERDRAFT_LIMIT_EXCEEDED_MO)) {
			
			
			repValues.put("creditsAvailable", String.valueOf(account.getCredits()));
			templateKey = ExternalEmailLogic.TEMPLATE_TASK_OVER_QUOTA_MO;
			

		} else if (taskMessageType
				.equals(SmsConstants.TASK_NOTIFICATION_EXPIRED)) {
			
			
			templateKey = ExternalEmailLogic.TEMPLATE_TASK_EXPIRED;
			

		} else if (taskMessageType
				.equals(SmsConstants.TASK_NOTIFICATION_COMPLETED)) {
			
			repValues.put("messagesProccessed", String.valueOf(smsTask.getMessagesProcessed()));
			repValues.put("messagesDelivered", String.valueOf(smsTask.getMessagesDelivered()));
			
			templateKey = ExternalEmailLogic.TEMPLATE_TASK_COMPLETED;
			

		} else if (taskMessageType
				.equals(SmsConstants.TASK_NOTIFICATION_ABORTED)) {
			
			
			repValues.put("SenderName", smsTask.getSenderUserName());
			templateKey = ExternalEmailLogic.TEMPLATE_TASK_ABORTED;
			

		} else if (taskMessageType
				.equals(SmsConstants.TASK_NOTIFICATION_FAILED)) {
			
			
			repValues.put("maxTried", String.valueOf(configSystem.getSmsRetryMaxCount()));
			templateKey = ExternalEmailLogic.TEMPLATE_TASK_FAILED;
			
			
			

		} else if (taskMessageType
				.equals(SmsConstants.TASK_INSUFFICIENT_CREDITS)) {
			
			
			repValues.put("required", String.valueOf(smsTask.getCreditCost()));
			repValues.put("available", String
					.valueOf(account.getOverdraftLimit()
							+ account.getCredits()));
		
			templateKey = ExternalEmailLogic.TEMPLATE_TASK_INSUFICIENT_CREDITS;

		}
		boolean accountNotification = false;
		boolean ownerNotification = false;


		if (notiToAddress != null && notiToAddress.length() > 0) {
		 					
			List<String> toList = new ArrayList<String>();
			toList.add(notiToAddress);
			externalEmailLogic.sendEmailTemplate(null, toList, templateKey, repValues);
		}

		if (ownerToAddress != null && ownerToAddress.length() > 0
				&& !ownerToAddress.equals(notiToAddress)) {
			
			List<String> toList = new ArrayList<String>();
			toList.add(ownerToAddress);
			externalEmailLogic.sendEmailTemplate(null, toList, templateKey, repValues);
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

		LOG.debug("Processing task in realtime: task id = " + smsTask.getId());			
		processTaskInThread(smsTask, smsThreadGroup);
	}

	public void checkAndSetTasksCompleted() {
		LOG.debug("checkAndSetTasksCompleted()");
		List<SmsTask> smsTasks = hibernateLogicLocator.getSmsTaskLogic()
				.getTasksToMarkAsCompleted();

		for (SmsTask task : smsTasks) {

			Session session = null;
			Transaction tx = null;

			try {
				session = getHibernateLogicLocator().getSmsTaskLogic()
						.getNewHibernateSession();
				tx = session.beginTransaction();

				// SMS-128/113 : lock the task so that other schedulers won't
				// pick it up causing duplicate settlements

				SmsTask smsTask = (SmsTask) session.get(SmsTask.class, task
						.getId(), LockMode.UPGRADE);
				LOG.debug(smsTask.getId() + " was in status : "
						+ smsTask.getStatusCode());

				if (smsTask.getStatusCode().equals(
						SmsConst_DeliveryStatus.STATUS_SENT)) {
					
					LOG.debug("Marking task as completed: taskId = "
							+ smsTask.getId() + " its status was "
							+ smsTask.getStatusCode());
					
					//check external messages
					if (externalMessageSending != null) {
						List<SmsMessage> messages = hibernateLogicLocator.getSmsMessageLogic().getSmsMessagesWithStatus(task.getId(), SmsConst_DeliveryStatus.STATUS_SENT);
						externalMessageSending.updateMessageStatuses(messages);
						
						//we need to check the message stats
						int delivered = smsTask.getMessagesDelivered();
						for (int q=0; q < messages.size(); q++) {
							SmsMessage message = messages.get(q);
							LOG.debug("got message of status " + message.getStatusCode());
							if (SmsConst_DeliveryStatus.STATUS_DELIVERED.equals(message.getStatusCode())) {
								delivered++;
							}
						}
						smsTask.setMessagesDelivered(delivered);
					}
					
					// We need to get these values inside the transaction
					double creditEstimate = smsTask.getCreditEstimate();
					double actualCreditsUsed = smsTask.getCreditsActual();
						
					smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_TASK_COMPLETED);
					smsTask.setBilledCredits(actualCreditsUsed);
					session.update(smsTask);
					tx.commit();
					
					if (creditEstimate != actualCreditsUsed) {
						smsBilling.settleCreditDifference(smsTask, creditEstimate, actualCreditsUsed);
					}
					checkOverdraft(smsTask);
					
					if (smsTask.getMessageTypeId().equals(
							SmsConstants.MESSAGE_TYPE_SYSTEM_ORIGINATING)) {
						sendEmailNotification(smsTask,
								SmsConstants.TASK_NOTIFICATION_COMPLETED);
					} else {
						// TODO what is this for?
						if (smsTask.getSmsMessages() != null) {
							for (SmsMessage smsMessages : smsTask.getSmsMessages()) {
								if (smsMessages.getStatusCode().equals(
										SmsConst_DeliveryStatus.STATUS_ERROR)
									|| smsMessages.getStatusCode().equals(
											SmsConst_DeliveryStatus.STATUS_FAIL)) {
									smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_FAIL);
									smsTask.setFailReason(smsMessages.getFailReason());
									session.update(smsTask);
								}
							}
						}
					}
					
				} else {
					tx.rollback();
				}

			} catch (HibernateException e) {
				LOG.error("Error checking task " + task.getId() + ": ", e);
				if (tx != null) {
					tx.rollback();
				}
			} finally {
				if (session != null) {
					session.close();
				}				
			}
		}
	}

	public void adjustLateDeliveryBilling() {
	
		List<SmsTask> smsTasks = hibernateLogicLocator.getSmsTaskLogic()
		.getTasksWithLateBilling();

		if (smsTasks != null && !smsTasks.isEmpty()) {

			LOG.debug("Adjusting billing for late deliveries for " + smsTasks.size() + " tasks");
			
			for (SmsTask task : smsTasks) {

				Session session = null;
				Transaction tx = null;

				try {
					session = getHibernateLogicLocator().getSmsTaskLogic()
					.getNewHibernateSession();
					tx = session.beginTransaction();

					// SMS-128/113 : lock the task so that other schedulers won't
					// pick it up causing duplicate settlements

					SmsTask smsTask = (SmsTask) session.get(SmsTask.class, task
							.getId(), LockMode.UPGRADE);

					if (smsTask.getStatusCode().equals(SmsConst_DeliveryStatus.STATUS_TASK_COMPLETED) &&
							smsTask.getBilledCredits() < smsTask.getCreditsActual()) {

						// We need to get these values inside the transaction
						double adjustment = smsTask.getCreditsActual() - smsTask.getBilledCredits();

						smsTask.setBilledCredits(smsTask.getCreditsActual());
						session.update(smsTask);
						tx.commit();

						smsBilling.debitLateMessages(smsTask, adjustment);
					} else {
						tx.rollback();
					}

				} catch (HibernateException e) {
					LOG.error("Error updating billing for task " + task.getId() + ": ", e);
					if (tx != null) {
						tx.rollback();
					}
				} finally {
					if (session != null) {
						session.close();
					}				
				}
			}
		}
	}

	private void checkOverdraft(SmsTask smsTask) {
		SmsAccount account = hibernateLogicLocator.getSmsAccountLogic()
				.getSmsAccount(smsTask.getSmsAccountId());
		if (account.getCredits() < (-1 * account.getOverdraftLimit())) {

			sendEmailNotification(smsTask,
					SmsConstants.ACCOUNT_OVERDRAFT_LIMIT_EXCEEDED);

		}
	}

	public void abortPendingTask(Long smsTaskID)
			throws SmsTaskNotFoundException {
		SmsTask smsTask = hibernateLogicLocator.getSmsTaskLogic().getSmsTask(
				smsTaskID);
		if (smsTask == null) {
			throw new SmsTaskNotFoundException();
		}
		
		if (smsTask.getStatusCode().equals(
				SmsConst_DeliveryStatus.STATUS_PENDING)) {

			smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_ABORT);
			smsTask.setFailReason(MessageCatalog
					.getMessage("messages.taskAborted"));
			hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);

			smsBilling.cancelPendingRequest(smsTaskID);
			
			// If it really is pending, there won't be any messages, but
			// keep this for future ref for more general abort code.
			hibernateLogicLocator.getSmsMessageLogic().updateStatusForMessages(
					smsTask.getId(),
					SmsConst_DeliveryStatus.STATUS_PENDING,
					SmsConst_DeliveryStatus.STATUS_ABORT);
			sendEmailNotification(smsTask,
					SmsConstants.TASK_NOTIFICATION_ABORTED);
		}
	
	}

	public void processMOTasks() {
		if (!externalLogic.isNodeBindToGateway()) {
			return;
		}

		List<SmsTask> moTasks = hibernateLogicLocator.getSmsTaskLogic()
				.getAllMOTasks();
		Session session = null;
		Transaction tx = null;
		if (moTasks != null) {
			
				for (SmsTask smsTask : moTasks) {
				try {
					session = getHibernateLogicLocator().getSmsTaskLogic()
							.getNewHibernateSession();
					tx = session.beginTransaction();

					SmsTask smsTaskUpgrade = (SmsTask) session.get(
							SmsTask.class, smsTask.getId(), LockMode.UPGRADE);

					if (smsTaskUpgrade.getStatusCode().equals(
							SmsConst_DeliveryStatus.STATUS_RETRY)
							|| smsTaskUpgrade.getStatusCode().equals(
									SmsConst_DeliveryStatus.STATUS_PENDING)
							|| smsTaskUpgrade.getStatusCode().equals(
									SmsConst_DeliveryStatus.STATUS_INCOMPLETE)) {
						smsTaskUpgrade
								.setStatusCode(SmsConst_DeliveryStatus.STATUS_BUSY);

					} else {
						tx.rollback();
						session.close();
						return;
					}

					session.update(smsTaskUpgrade);
					tx.commit();
					session.close();
					processTaskInThread(smsTask, smsThreadGroup);
				
			} catch (HibernateException e) {
				LOG.error("Error processing MO Message: ", e);
				if (tx != null) {
					tx.rollback();
				}
				if (session != null) {
					session.close();
				}
			}}
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
	
	
	private Date getNextRetryTime(Integer retryCount) {

		SmsConfig systemConfig = hibernateLogicLocator.getSmsConfigLogic()
		.getOrCreateSystemSmsConfig();
		//is a number of seconds
		Integer offset = systemConfig.getSmsRetryScheduleInterval();

		if (retryCount == null) {
			retryCount = Integer.valueOf(1);
		}

		if (retryCount > 9 && retryCount < 20) {
			//with default settings this will be 240s
			offset = offset * 2;
		} else if (retryCount > 19 && retryCount < 30) {
			//with default settings this will be 480s
			offset = offset * 4;
		} else if (retryCount > 29 && retryCount < 40) {
			//with default settings this will be 960
			offset = offset * 8;
		} else if (retryCount > 39 && retryCount < 50) {
			//with default settings this will be 1920s
			offset = offset * 16;
		} else if (retryCount > 49 && retryCount < 60) {
			//with default settings this will be 3840s (60m)
			offset = offset * 32;
		} else if (retryCount > 59) {
			//with default settings this will be 7680s (128m)
			offset = offset * 64;
		} 


		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, offset); 
		return new Date();
	}

	public void updateExternalMessageStatuses() {
		if (externalMessageSending == null) {
			return;
		}
		LOG.info("updateExternalMessageStatuses()");
		
		//we need all tasks in progress
		SearchFilterBean searchBean  = new SearchFilterBean();
		searchBean.setStatus(SmsConst_DeliveryStatus.STATUS_SENT);

		
		List<SmsTask> tasks = hibernateLogicLocator.getSmsTaskLogic().getTasksNotComplete();
		LOG.info("got a list of " + tasks.size() + " tasks");
		for (int i =0; i < tasks.size(); i++) {
			SmsTask task = tasks.get(i);
			List<SmsMessage> messages = hibernateLogicLocator.getSmsMessageLogic().getSmsMessagesWithStatus(task.getId(), SmsConst_DeliveryStatus.STATUS_SENT);
			LOG.info("checking " + messages.size() + " from task " + task.getId());
			externalMessageSending.updateMessageStatuses(messages);
			//we need to update the task details for the message
			int delivered = task.getMessagesDelivered();
			for (int q=0; q < messages.size(); q++) {
				SmsMessage message = messages.get(q);
				if (SmsConst_DeliveryStatus.STATUS_DELIVERED.equals(message.getStatusCode())) {
					delivered++;
				}
			}
			
			task.setMessagesDelivered(delivered);
			hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(task);
		}

	}
}
