/***********************************************************************************
 * HelperActionBean.java
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
package org.sakaiproject.sms.tool.beans;

import org.sakaiproject.sms.logic.smpp.SmsBilling;
import org.sakaiproject.sms.logic.smpp.SmsCore;
import org.sakaiproject.sms.logic.smpp.SmsService;
import org.sakaiproject.sms.logic.smpp.SmsTaskValidationException;
import org.sakaiproject.sms.logic.smpp.exception.ReceiveIncomingSmsDisabledException;
import org.sakaiproject.sms.logic.smpp.exception.SmsSendDeniedException;
import org.sakaiproject.sms.logic.smpp.exception.SmsSendDisabledException;
import org.sakaiproject.sms.model.SmsTask;
import org.sakaiproject.sms.tool.otp.SmsTaskLocator;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

public class HelperActionBean {

	private SmsTaskLocator smsTaskLocator;
	private SmsService smsService;
	private SmsCore smsCore;
	private TargettedMessageList messages;
	private SmsBilling smsBilling;

	/**
	 * Cancel Action
	 * 
	 * @return {@link ActionResults}
	 */
	public String cancel() {
		smsTaskLocator.clearBeans();
		return ActionResults.CANCEL;
	}

	/**
	 * Calculates estimated group size and continues to next page
	 * 
	 * @return {@link ActionResults}
	 */
	public String doContinue() {
		if (smsTaskLocator.containsNew()) {

			final SmsTask smsTask = (SmsTask) smsTaskLocator
					.locateBean(SmsTaskLocator.NEW_1);
			smsService.calculateEstimatedGroupSize(smsTask);

			return ActionResults.CONTINUE;
		} else {
			// Unexpected error
			messages.addMessage(new TargettedMessage(
					"sms.errors.unexpected-error", null,
					TargettedMessage.SEVERITY_ERROR));
			return ActionResults.ERROR;
		}

	}

	/**
	 * Checks sufficient credits and then inserts task
	 * 
	 * @return {@link ActionResults}
	 */
	public String save() {
		if (smsTaskLocator.containsNew()) {
			final SmsTask smsTask = (SmsTask) smsTaskLocator
					.locateBean(SmsTaskLocator.NEW_1);

			// Check if credits available
			final boolean sufficientCredits = smsBilling
					.checkSufficientCredits(smsTask.getSmsAccountId(), smsTask
							.getCreditEstimate(), false);
			if (sufficientCredits) {
				// do sending
				try {
					smsCore.insertTask(smsTask);
				} catch (SmsTaskValidationException e) {

					for (String errorMsg : e.getErrorMessages()) {
						messages.addMessage(new TargettedMessage(errorMsg,
								null, TargettedMessage.SEVERITY_ERROR));
					}

					return ActionResults.ERROR;
				} catch (SmsSendDeniedException se) {
					messages.addMessage(new TargettedMessage(
							"sms.errors.task.permission-denied", null,
							TargettedMessage.SEVERITY_ERROR));
					return ActionResults.ERROR;
				} catch (SmsSendDisabledException sd) {
					messages.addMessage(new TargettedMessage(
							"sms.errors.task.sms-send-disabled",
							new Object[] { smsTask.getSakaiSiteId() },
							TargettedMessage.SEVERITY_ERROR));
					return ActionResults.ERROR;
				} catch (ReceiveIncomingSmsDisabledException e) {
					messages.addMessage(new TargettedMessage(
							"sms.errors.task.sms-incoming-disabled",
							new Object[] { smsTask.getSakaiSiteId() },
							TargettedMessage.SEVERITY_ERROR));
					return ActionResults.ERROR;
				}
				messages.addMessage(new TargettedMessage(
						"sms.helper.task-success", null,
						TargettedMessage.SEVERITY_INFO));
				smsTaskLocator.clearBeans();
				return ActionResults.SUCCESS;
			} else {
				messages.addMessage(new TargettedMessage(
						"sms.errors.insufficient-credits", null,
						TargettedMessage.SEVERITY_ERROR));
				smsTaskLocator.clearBeans();
				return ActionResults.ERROR;
			}

		} else {
			messages.addMessage(new TargettedMessage(
					"sms.errors.unexpected-error", null,
					TargettedMessage.SEVERITY_ERROR));
			return ActionResults.ERROR;
		}

	}

	public void setMessages(TargettedMessageList messages) {
		this.messages = messages;
	}

	public void setSmsCore(SmsCore smsCore) {
		this.smsCore = smsCore;
	}

	public void setSmsService(SmsService smsService) {
		this.smsService = smsService;
	}

	public void setSmsTaskLocator(SmsTaskLocator smsTaskLocator) {
		this.smsTaskLocator = smsTaskLocator;
	}

	public void setSmsBilling(SmsBilling smsBilling) {
		this.smsBilling = smsBilling;
	}
}
