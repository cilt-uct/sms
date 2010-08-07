/**********************************************************************************
 * $URL:$
 * $Id:$
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

package org.sakaiproject.sms.logic.smpp;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.sakaiproject.sms.logic.exception.SmsTaskNotFoundException;
import org.sakaiproject.sms.logic.smpp.exception.ReceiveIncomingSmsDisabledException;
import org.sakaiproject.sms.logic.smpp.exception.SmsSendDeniedException;
import org.sakaiproject.sms.logic.smpp.exception.SmsSendDisabledException;
import org.sakaiproject.sms.model.SmsMOMessage;
import org.sakaiproject.sms.model.SmsTask;

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
	 * Add a new task to the sms task list Validation will be done to make sure
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
	 * @throws ReceiveIncomingSmsDisabledException
	 */
	public SmsTask insertTask(SmsTask smsTask)
			throws SmsTaskValidationException, SmsSendDeniedException,
			SmsSendDisabledException, ReceiveIncomingSmsDisabledException;

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
	 * the task. We still need to handle these reports because the messages are
	 * now billable. Create an account entry for any delivery reports received
	 * when the task is in COMPLETE state, since the last accounting adjustment.
	 */
	public void adjustLateDeliveryBilling();

	/**
	 * 
	 * Our SMPP listener received an incoming message. Try to process message in
	 * real-time by inserting it into the queue and calling processMessage
	 * immediately. If unable to process in real-time due to high thread count,
	 * we leave it in queue for the scheduler to handle.
	 * 
	 * @param smsMessagebody
	 * @param mobileNumber
	 */
	public void processIncomingMessage(SmsMOMessage message);
	
	/**
	 * If we did not receive gateway delivery reports for messages that was sent
	 * out, then we mark those messages as time out after a predefined period as
	 * determined by the global DEL_REPORT_TIMEOUT_DURATION setting. These
	 * messages are not billable. But they will be billable if the report comes
	 * in very late.
	 */
	public void processTimedOutDeliveryReports();

	/**
	 * Gets the next task to process. Based on specific criteria like status and
	 * date to sent. This is typically called by the sms scheduler.
	 */
	public void processNextTask();
	
	/**
	 * process all pending SO tasks
	 */
	public void processSOTasks();

	/**
	 * Processes all the queued MO tasks.
	 */
	public void processMOTasks();

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
	 * Process is specific persisted task in a separate thread. A task can be
	 * retried if a previous send attempt was unsuccessful due to gateway
	 * connection problems. A retry will be re-scheduled some time in the
	 * future. When the max retry attempts are reached or if credits are
	 * insufficient, the task is marked as failed.
	 * 
	 * The task will also expire if it cannot be processed in a specified time.
	 * See http://jira.sakaiproject.org/jira/browse/SMS-9
	 * 
	 * @param smsTask
	 */
	public void processTaskInThread(SmsTask smsTask, ThreadGroup threadGroup);

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
	 * Checks for tasks that can be marked as complete. If the total messages
	 * processed equals the actual group size the task is marked as complete.
	 */
	public void checkAndSetTasksCompleted();

	/**
	 * Aborts the pending task.The task will only be aborted if its status is
	 * pending.
	 * 
	 * @param smsTaskID
	 * @throws SmsTaskNotFoundException
	 */
	public void abortPendingTask(Long smsTaskID)
			throws SmsTaskNotFoundException;

	/**
	 * Update the statuses of messages delivered to an external gateway
	 */
	public void updateExternalMessageStatuses();
	
}