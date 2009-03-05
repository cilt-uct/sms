/***********************************************************************************
 * SmsServiceImpl.java
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
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.sakaiproject.sms.logic.hibernate.exception.SmsAccountNotFoundException;
import org.sakaiproject.sms.logic.hibernate.exception.SmsTaskNotFoundException;
import org.sakaiproject.sms.logic.smpp.SmsBilling;
import org.sakaiproject.sms.logic.smpp.SmsCore;
import org.sakaiproject.sms.logic.smpp.SmsService;
import org.sakaiproject.sms.logic.smpp.SmsTaskValidationException;
import org.sakaiproject.sms.logic.smpp.exception.ReceiveIncomingSmsDisabledException;
import org.sakaiproject.sms.logic.smpp.exception.SmsSendDeniedException;
import org.sakaiproject.sms.logic.smpp.exception.SmsSendDisabledException;
import org.sakaiproject.sms.logic.smpp.validate.SmsTaskValidator;
import org.sakaiproject.sms.model.hibernate.SmsTask;

/**
 * This API allows for easy implementation of SMS services in an existing or new
 * Sakai tool.
 *
 * @author etienne@psybergate.co.za
 *
 */

public class SmsServiceImpl implements SmsService {

	public SmsCore smsCore = null;

	private SmsTaskValidator smsTaskValidator =null;

	public SmsTaskValidator getSmsTaskValidator() {
		return smsTaskValidator;
	}

	public void setSmsTaskValidator(SmsTaskValidator smsTaskValidator) {
		this.smsTaskValidator = smsTaskValidator;
	}

	public SmsBilling smsBilling = null;

	public SmsBilling getSmsBilling() {
		return smsBilling;
	}

	public void setSmsBilling(SmsBilling smsBilling) {
		this.smsBilling = smsBilling;
	}

	public SmsCore getSmsCore() {
		return smsCore;
	}

	public void setSmsCore(SmsCore smsCore) {
		this.smsCore = smsCore;
	}

	/**
	 * Get a new task with default attributes. The task is only a object. It is
	 * not yet persisted to the database. For eg. send message y to Sakai group
	 * X at time Z. If the task is future dated, then it be picked up by the sms
	 * task (job) scheduler for processing.
	 *
	 * @param sakaiGroupId
	 * @param dateToSend
	 * @param messageBody
	 * @param sakaiToolId
	 * @return
	 */
	public SmsTask getPreliminaryTask(String sakaiGroupId, Date dateToSend,
			String messageBody, String sakaiSiteId, String sakaiToolId,
			String sakaiSenderID) {
		return smsCore.getPreliminaryTask(sakaiGroupId, dateToSend,
				messageBody, sakaiSiteId, sakaiToolId, sakaiSenderID);

	}

	public SmsTask getPreliminaryTask(Date dateToSend, String messageBody,
			String sakaiSiteID, String sakaiToolId, String sakaiSenderID,
			List<String> deliveryEntityList) {
		return smsCore.getPreliminaryTask(dateToSend, messageBody, sakaiSiteID,
				sakaiToolId, sakaiSenderID, deliveryEntityList);
	}

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
			Set<String> deliveryMobileNumbers) {
		return smsCore.getPreliminaryTask(dateToSend, messageBody, sakaiSiteID,
				sakaiToolId, sakaiSenderID, deliveryMobileNumbers);

	}

	/**
	 * Add a new task to the sms task list. In this case you must supply a list
	 * of Sakai user ID's.
	 *
	 * @param sakaiUserIds
	 * @param dateToSend
	 * @param messageBody
	 *            , the actual sms body.
	 * @param sakaiToolId
	 *            , If the message originated from a sakai tool, then give id
	 *            here, otherwise use null.
	 * @return
	 */
	public SmsTask getPreliminaryTask(Set<String> sakaiUserIds,
			Date dateToSend, String messageBody, String sakaiSiteId,
			String sakaiToolId, String sakaiSenderID) {
		return smsCore.getPreliminaryTask(sakaiUserIds, dateToSend,
				messageBody, sakaiSiteId, sakaiToolId, sakaiSenderID);
	}

	/**
	 * Return true of the account has the required credits available to send the
	 * messages. The account number is calculated using either the Sakai site or
	 * the Sakai user. If this returns false, then the UI must not allow the
	 * user to proceed. If not handled by the UI, then the sms service will fail
	 * the sending of the message anyway.
	 *
	 * @param sakaiSiteID
	 *            , (e.g. "!admin")
	 * @param sakaiUserID
	 *            the sakai user id
	 * @param creditsRequired
	 *            the credits required
	 *
	 * @return true, if check sufficient credits
	 */
	public boolean checkSufficientCredits(String sakaiSiteID,
			String sakaiUserID, int creditsRequired) {
		Long smsAcountId;
		try {
			smsAcountId = smsBilling.getAccountID(sakaiSiteID, sakaiUserID);
		} catch (SmsAccountNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		return smsBilling.checkSufficientCredits(smsAcountId, creditsRequired);

	}

	/**
	 * Will calculate the all the group estimates.
	 *
	 * @param smsTask
	 * @return
	 */
	public SmsTask calculateEstimatedGroupSize(SmsTask smsTask) {
		return smsCore.calculateEstimatedGroupSize(smsTask);
	}

	/**
	 * Validate task.
	 *
	 * @param smsTask
	 *            the sms task
	 *
	 * @return the array list< string>
	 */
	public ArrayList<String> validateTask(SmsTask smsTask) {
		return smsTaskValidator.validateInsertTask(smsTask);
	}

	/**
	 * Return true of the account has the required credits available to send the
	 * messages. The account number is calculated using either the Sakai site or
	 * the Sakai user. If this returns false, then the UI must not allow the
	 * user to proceed. If not handled by the UI, then the sms service will fail
	 * the sending of the message anyway.
	 */
	public boolean checkSufficientCredits(SmsTask smsTask) {
		return smsBilling.checkSufficientCredits(smsTask);
	}

	/**
	 * Aborts the pending task.
	 *
	 * @param smsTaskID
	 * @throws SmsTaskNotFoundException
	 */
	public void abortPendingTask(Long smsTaskID)
			throws SmsTaskNotFoundException {
		smsCore.abortPendingTask(smsTaskID);
	}

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
			throws SmsTaskValidationException, SmsSendDeniedException, SmsSendDisabledException, ReceiveIncomingSmsDisabledException {

		return smsCore.insertTask(smsTask);
	}
}
