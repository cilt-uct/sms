/***********************************************************************************
 * TaskListPopupProducer.java
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

package org.sakaiproject.sms.tool.producers;

import org.sakaiproject.sms.logic.SmsTaskLogic;
import org.sakaiproject.sms.model.SmsTask;
import org.sakaiproject.sms.tool.params.IdParams;
import org.sakaiproject.sms.tool.util.NullHandling;

import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

public class TaskListPopupProducer implements ViewComponentProducer,
		ViewParamsReporter {

	public static final String VIEW_ID = "TaskListPopup";

	private SmsTaskLogic smsTaskLogic;

	public void setSmsTaskLogic(SmsTaskLogic smsTaskLogic) {
		this.smsTaskLogic = smsTaskLogic;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {

		IdParams params = (IdParams) viewparams;
		if (params.id != null) {

			SmsTask smsTask = smsTaskLogic.getSmsTask(Long.valueOf(params.id));
			if (smsTask != null) {

				UIMessage.make(tofill, "credit-estimate-label",
						"sms.sms-task.credit.estimate");
				UIOutput.make(tofill, "credit-estimate", " "
						+ NullHandling
								.safeToString(smsTask.getCreditEstimate()));

				UIMessage.make(tofill, "date-processed-label",
						"sms.sms-task.date.processed");
				UIOutput.make(tofill, "date-processed", " "
						+ NullHandling.safeToStringFormated(smsTask
								.getDateProcessed()));

				UIMessage.make(tofill, "date-to-send-label",
						"sms.sms-task.date.to.send");
				UIOutput.make(tofill, "date-to-send", " "
						+ NullHandling.safeToStringFormated(smsTask
								.getDateToSend()));

				UIMessage.make(tofill, "date-to-expire-label",
				"sms.sms-task.date.to.expire");
		UIOutput.make(tofill, "date-to-expire", " "
				+ NullHandling.safeToStringFormated(smsTask
						.getDateToExpire()));
				
				
				UIMessage.make(tofill, "delivery-group-name-label",
						"sms.sms-task.delivery.group.name");
				UIOutput.make(tofill, "delivery-group-name", " "
						+ NullHandling.safeToString(smsTask
								.getDeliveryGroupName()));

				UIMessage.make(tofill, "delivery-user-id-label",
						"sms.sms-task.delivery.user.id");
				UIOutput.make(tofill, "delivery-user-id", " "
						+ NullHandling
								.safeToString(smsTask.getDeliveryUserId()));

				UIMessage.make(tofill, "group-size-estimate-label",
						"sms.sms-task.group.size.estimate");
				UIOutput.make(tofill, "group-size-estimate", NullHandling
						.safeToString(smsTask.getGroupSizeEstimate()));

				UIMessage.make(tofill, "group-size-actual-label",
						"sms.sms-task.group.size.actual");
				UIOutput.make(tofill, "group-size-actual", NullHandling
						.safeToString(smsTask.getGroupSizeActual()));

				UIMessage.make(tofill, "messsage-body-label",
						"sms.sms-task.message.body");
				UIOutput.make(tofill, "messsage-body", NullHandling
						.safeToString(smsTask.getMessageBody()));

				UIMessage.make(tofill, "messsage-reply-body-label",
						"sms.sms-task.message.reply.body");
				UIOutput.make(tofill, "messsage-reply-body", NullHandling
						.safeToString(smsTask.getMessageReplyBody()));
				
				UIMessage.make(tofill, "message-type-id-label",
						"sms.sms-task.message.type.id");
				UIOutput.make(tofill, "message-type-id", NullHandling
						.safeToString(smsTask.getMessageTypeId()));

				UIMessage.make(tofill, "attempt-count-label",
						"sms.sms-task.attempt.count");
				UIOutput.make(tofill, "attempt-count", NullHandling
						.safeToString(smsTask.getAttemptCount()));

				UIMessage.make(tofill, "sakai-site-id-label",
						"sms.sms-task.sakai.site.id");
				UIOutput.make(tofill, "sakai-site-id", NullHandling
						.safeToString(smsTask.getSakaiSiteId()));

				UIMessage.make(tofill, "sakai-tool-id-label",
						"sms.sms-task.sakai.tool.id");
				UIOutput.make(tofill, "sakai-tool-id", NullHandling
						.safeToString(smsTask.getSakaiToolId()));

				UIMessage.make(tofill, "sakai-tool-name-label",
						"sms.sms-task.sakai.tool.name");
				UIOutput.make(tofill, "sakai-tool-name", NullHandling
						.safeToString(smsTask.getSakaiToolName()));

				UIMessage.make(tofill, "sender-user-name-label",
						"sms.sms-task.sender.user.name");
				UIOutput.make(tofill, "sender-user-name", NullHandling
						.safeToString(smsTask.getSenderUserName()));

				UIMessage.make(tofill, "sms-account-id-label",
						"sms.sms-task.sms.account.id");
				UIOutput.make(tofill, "sms-account-id", NullHandling
						.safeToString(smsTask.getSmsAccountId()));

				UIMessage.make(tofill, "sms-account-status-code-label",
						"sms.sms-task.sms.status.code");
				UIOutput.make(tofill, "sms-account-status-code", NullHandling
						.safeToString(smsTask.getStatusCode()));

				UIMessage.make(tofill, "fail-reason-label",
						"sms.sms-task.sms.fail-reason");
				UIOutput.make(tofill, "fail-reason", NullHandling
						.safeToString(smsTask.getFailReason()));

			}
		}
	}

	public String getViewID() {
		return VIEW_ID;
	}

	public ViewParameters getViewParameters() {
		return new IdParams();
	}

}