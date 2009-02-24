/***********************************************************************************
 * SmsCore.java
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

package org.sakaiproject.sms.logic.smpp;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.sakaiproject.sms.logic.hibernate.exception.SmsTaskNotFoundException;
import org.sakaiproject.sms.logic.smpp.exception.SmsSendDeniedException;
import org.sakaiproject.sms.logic.smpp.exception.SmsSendDisabledException;
import org.sakaiproject.sms.model.hibernate.SmsMessage;
import org.sakaiproject.sms.model.hibernate.SmsTask;

/**
 * The SMS service will handle all logic regarding the queueing, sending and
 * receiving of messages.
 *
 * @author louis@psybergate.com
 * @version 1.0
 * @created 12-Nov-2008
 */
public interface SmsCore {

	/**
	 * Find the next sms task to process from the task queue. Determine tasks
	 * with highest priority. Priority is based on message age and type.
	 *
	 * @return SmsTask
	 */
	public SmsTask getNextSmsTask();

	/**
	 * Add a new task to the sms task list, for eg. send message to all
	 * administrators at 10:00, or get latest announcements and send to mobile
	 * numbers of Sakai group x (phase II). Validation will be done to make sure
	 * that the preliminary values are supplied.
	 *
	 * @param smsTask
	 *            the sms task
	 *
	 * @return the sms task
	 *
	 * @throws SmsTaskValidationException
	 *             the sms task validation exception
	 * @throws SmsSendDeniedException
	 * @throws SmsSendDisabledException
	 */
	public SmsTask insertTask(SmsTask smsTask)
			throws SmsTaskValidationException, SmsSendDeniedException,
			SmsSendDisabledException;

	/**
	 * Add a new task to the sms task list, that contains a list of delivery
	 * entity id
	 *
	 * @param dateToSend
	 * @param messageBody
	 * @param sakaiSiteID
	 * @param sakaiToolId
	 * @param sakaiSenderID
	 * @param deliveryEntityList
	 * @return
	 */
	public SmsTask getPreliminaryTask(Date dateToSend, String messageBody,
			String sakaiSiteID, String sakaiToolId, String sakaiSenderID,
			List<String> deliveryEntityList);

	/**
	 * Add a new task to the sms task list, that will send sms messages to the
	 * specified list of mobile numbers
	 *
	 * @param dateToSend
	 * @param messageBody
	 * @param sakaiSiteID
	 * @param sakaiToolId
	 * @param sakaiSenderID
	 * @param deliveryMobileNumbers
	 * @return
	 */
	public SmsTask getPreliminaryTask(Date dateToSend, String messageBody,
			String sakaiSiteID, String sakaiToolId, String sakaiSenderID,
			Set<String> deliveryMobileNumbers);

	/**
	 * Get a new sms task object with default values. This step is required.
	 *
	 * @param sakaiUserIds
	 * @param dateToSend
	 * @param messageBody
	 * @param sakaiSiteID
	 * @param sakaiToolId
	 * @param sakaiSenderID
	 * @return
	 */
	public SmsTask getPreliminaryTask(Set<String> sakaiUserIds,
			Date dateToSend, String messageBody, String sakaiSiteID,
			String sakaiToolId, String sakaiSenderID);

	/**
	 * Get a new sms task object with default values. This step is required.
	 *
	 * @param deliverGroupId
	 * @param dateToSend
	 * @param messageBody
	 * @param sakaiSiteID
	 * @param sakaiToolId
	 * @param sakaiSenderID
	 * @return
	 */
	public SmsTask getPreliminaryTask(String deliverGroupId, Date dateToSend,
			String messageBody, String sakaiSiteID, String sakaiToolId,
			String sakaiSenderID);

	/**
	 * Some delivery report might arrive after the predefined timeout period of
	 * the task. Wee still need to handle these reports because the messages are
	 * now billable. We need to receive them, update SMS_MESSAGE and make a
	 * account entry of type TRANS_CREDIT_LATE_MESSAGE.
	 */
	public void processVeryLateDeliveryReports();

	/**
	 *
	 * Try to process an incoming message in real-time by inserting it into the
	 * queue and calling processMessage immediately. If unable to process, then
	 * leave in the queue for the job scheduler to handle. Incoming messages are
	 * for later development in phase II.
	 *
	 * @param smsMessagebody
	 * @param mobileNumber
	 */
	public void processIncomingMessage(String smsMessagebody,
			String mobileNumber);

	/**
	 * If we did not receive gateway delivery reports for messages that was sent
	 * out, then we mark those messages as time out after a predefined period as
	 * determined by DEL_REPORT_TIMEOUT_DURATION on the task. These messages are
	 * not billable. But they will be billable if the report comes in very late.
	 */
	public void processTimedOutDeliveryReports();

	/**
	 * Gets the next task to process. Based on specific criteria like status and
	 * date to sent. This is typically called by the sms scheduler.
	 */
	public void processNextTask();

	/**
	 * Process is specific persisted task. A task can be retried if a previous
	 * send attempt was unsuccessful due to gateway connection problems. A retry
	 * will be re-scheduled some time in the future. When the max retry attempts
	 * are reached or if credits are insufficient, the task is marked as failed.
	 *
	 * The task will also expire if it cannot be processed in a specified time.
	 * See http://jira.sakaiproject.org/jira/browse/SMS-9
	 *
	 * @param smsTask
	 */
	public void processTask(SmsTask smsTask);

	/**
	 * If a new task is scheduled for immediate processing, then we try to
	 * process it in real-time. If it is not possible (for e.g. too many
	 * threads) then the task will be handled by the scheduler. If the scheduler
	 * is too busy and the task is picked up too late, then the task is marked
	 * as STATUS_EXPIRE
	 *
	 * @param smsTask
	 */
	public void tryProcessTaskRealTime(SmsTask smsTask);

	/**
	 * Calculate the number of messages to be sent when the new sms task is
	 * created. Also populate other estimated values on the task.
	 *
	 * @param smsTask
	 * @return
	 */
	public SmsTask calculateEstimatedGroupSize(SmsTask smsTask);

	/**
	 * Send an email.
	 *
	 * @param toAddress
	 *            the to address
	 * @param subject
	 *            the subject
	 * @param body
	 *            the body
	 * @param smsTask
	 *            the sms task
	 *
	 * @return true, if send notification email
	 */
	public boolean sendNotificationEmail(SmsTask smsTask, String toAddress,
			String subject, String body);

	/**
	 * Checks for tasks that can be marked as complete. If the total messages
	 * processed equals the actual group size the task is marked as complete.
	 */
	public void checkAndSetTasksCompleted();

	/**
	 * Aborts the pending task.
	 *
	 * @param smsTaskID
	 * @throws SmsTaskNotFoundException
	 */
	public void abortPendingTask(Long smsTaskID)
			throws SmsTaskNotFoundException;

}