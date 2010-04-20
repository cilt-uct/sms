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
import org.sakaiproject.sms.model.SmsTask;

/**
 * This API allows for implementation of SMS services in an existing or new
 * Sakai tool. Management of billing accounts and transactions, the task queue
 * and SMS configuration is all handled via the SMS tool windows. These windows
 * are only available for administrators.
 * 
 * To sms-enabled a existing Sakai tool, the following guidelines must be
 * followed:
 * 
 * (1)Call sms.getPreliminaryTask to get a new sms task. If this is null, then
 * the sms account is not set up. (2)You display the sms window. (3)User press
 * the "continue" button. (4)Post UI values to smsTask (like the sms body).
 * (5)call sms.validateTask(smsTask) and show any errors in UI. (6)call
 * sms.calculateGroupSize to calculate estimated group size on smsTask. (7)You
 * display estimated group size and cost in UI. (8)You change button test to
 * "Save". (9)User press the "Save" button. (10)call sms.checkSufficientCredits.
 * (11)You report insufficient credits in the UI. (12)call
 * sms.insertTask(smsTask) for scheduler to handle. If the delivery date-time is
 * now, the task will be processed immediately.
 * 
 * 
 * @author etienne@psybergate.co.za
 * 
 */
public interface SmsService {

	/**
	 * Get a new sms task object with default values. The caller must supply a
	 * list of sakai delivery entities like /site/group1/
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
	 * Get a new sms task object with default values. The caller must supply a
	 * list of mobile numbers.
	 * 
	 * @param dateToSend
	 * @param messageBody
	 * @param sakaiSiteID
	 * @param sakaiToolId
	 * @param sakaiSenderID
	 * @param deliveryMobileNumbers
	 * @return
	 * @throws SmsSendDeniedException
	 */
	public SmsTask getPreliminaryTask(Date dateToSend, String messageBody,
			String sakaiSiteID, String sakaiToolId, String sakaiSenderID,
			Set<String> deliveryMobileNumbers);

	/**
	 * Get a new sms task object with default values. The caller must supply a
	 * single sakai group id.
	 * 
	 * @param sakaiGroupId
	 * @param dateToSend
	 * @param messageBody
	 * @param sakaiSiteId
	 * @param sakaiToolId
	 * @param sakaiSenderID
	 * @return
	 */
	public SmsTask getPreliminaryTask(String sakaiGroupId, Date dateToSend,
			String messageBody, String sakaiSiteId, String sakaiToolId,
			String sakaiSenderID);

	/**
	 * Get a new sms task object with default values. The caller must supply a
	 * list of Sakai user ID's.
	 * 
	 * @param sakaiUserIds
	 * @param dateToSend
	 * @param messageBody
	 * @param sakaiSiteId
	 * @param sakaiToolId
	 * @param sakaiSenderID
	 * @return
	 */
	public SmsTask getPreliminaryTask(Set<String> sakaiUserIds,
			Date dateToSend, String messageBody, String sakaiSiteId,
			String sakaiToolId, String sakaiSenderID);

	/**
	 * Return true of the account has the required credits available to send the
	 * messages. The account number is calculated using either the Sakai site or
	 * the Sakai user. If this returns false, then the UI must not allow the
	 * user to proceed.
	 * 
	 * @param sakaiSiteID
	 *            , (e.g. "!admin")
	 * @param sakaiUserID
	 *            the sakai user id
	 * @param creditsRequired
	 *            the credits required
	 * @param overDraftCheck
	 *            the overDraftCheck
	 * @return true, if check sufficient credits
	 */
	public boolean checkSufficientCredits(String sakaiSiteID,
			String sakaiUserID, double creditsRequired, boolean overDraftCheck);

	/**
	 * 
	 * Return true of the account has the required credits available. Take into
	 * account overdraft limits, if applicable.
	 * 
	 * @param smsTask
	 * @return
	 */
	public boolean checkSufficientCredits(SmsTask smsTask);

	/**
	 * Calculate the estimated group size. If this is not set on the task, the
	 * Persistence of the task will fail.
	 * 
	 * @param smsTask
	 * @return
	 */
	public SmsTask calculateEstimatedGroupSize(SmsTask smsTask);

	/**
	 * Validate task. Validation must be done in any UI implementing the sms
	 * service. It is also done before a task is persisted (See
	 * SmsCore.insertTask).
	 * 
	 * @param smsTask
	 *            the sms task
	 * 
	 * @return the array list< string>
	 */
	public List<String> validateTask(SmsTask smsTask);

	/**
	 * Aborts the pending task.
	 * 
	 * @param smsTaskID
	 * @throws SmsTaskNotFoundException
	 */
	public void abortPendingTask(Long smsTaskID)
			throws SmsTaskNotFoundException;

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
	 * @throws ReceiveIncomingSmsDisabledException
	 */
	public SmsTask insertTask(SmsTask smsTask)
			throws SmsTaskValidationException, SmsSendDeniedException,
			SmsSendDisabledException, ReceiveIncomingSmsDisabledException;

	/**
	 * Send SMS to array of userIds. Tools that implement SMS must use this
	 * method to send out messages. For example the posting of an announcement
	 * may also trigger SMS delivery.
	 * 
	 * @param userId
	 *            array of user ids to send to
	 * @param fromId
	 *            user id to set as sender
	 * @param siteId
	 *            site id
	 * @param toolId
	 *            tool id
	 * @param message
	 *            message to send
	 * @return an array of userIds SMS was sent to
	 */
	public String[] sendSmsToUserIds(String[] userIds, String fromId,
			String siteId, String toolId, String message);

	/**
	 * Send SMS to array of mobile numbers
	 * 
	 * @param mobileNrs
	 *            array of mobile numbers
	 * @param fromId
	 *            user id to set as sender
	 * @param siteId
	 *            site id
	 * @param toolId
	 *            tool id
	 * @param message
	 *            message to send
	 * @return an array of mobile numbers SMS was sent to
	 */
	public String[] sendSmsToMobileNumbers(String[] mobileNrs, String fromId,
			String siteId, String toolId, String message);

}
