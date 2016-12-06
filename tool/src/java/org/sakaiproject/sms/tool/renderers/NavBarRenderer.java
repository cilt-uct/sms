/***********************************************************************************
 * NavBarRenderer.java
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

package org.sakaiproject.sms.tool.renderers;

import org.sakaiproject.sms.tool.producers.BillingAdminProducer;
import org.sakaiproject.sms.tool.producers.MessageLogProducer;
import org.sakaiproject.sms.tool.producers.RegisteredCommandProducer;
import org.sakaiproject.sms.tool.producers.SmsSiteConfigProducer;
import org.sakaiproject.sms.tool.producers.SmsSystemConfigProducer;
import org.sakaiproject.sms.tool.producers.TaskListProducer;
import org.sakaiproject.sms.tool.producers.TransactionLogProducer;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class NavBarRenderer {

	/**
	 * Renders navigation bar
	 * 
	 * @param tofill
	 *            {@link UIContainer} to fill
	 * @param divID
	 *            ID of div
	 * @param currentViewID
	 *            View ID currently being viewed
	 */
	public void makeNavBar(UIContainer tofill, String divID,
			String currentViewID) {
		// TODO: Take permissions into account

		UIJointContainer joint = new UIJointContainer(tofill, divID,
				"sms-navigation:");

		renderBranch(joint, "1", currentViewID, TaskListProducer.VIEW_ID,
				"sms.navbar.tasks", true);
		renderBranch(joint, "2", currentViewID, MessageLogProducer.VIEW_ID,
				"sms.navbar.messages", true);
		renderBranch(joint, "3", currentViewID, BillingAdminProducer.VIEW_ID,
				"sms.navbar.accounts", true);
		renderBranch(joint, "4", currentViewID, TransactionLogProducer.VIEW_ID,
				"sms.navbar.transactions", true);
		renderBranch(joint, "5", currentViewID, SmsSiteConfigProducer.VIEW_ID,
				"sms.navbar.site-config", true);
		renderBranch(joint, "6", currentViewID,
				SmsSystemConfigProducer.VIEW_ID, "sms.navbar.system-config",
				false);
		renderBranch(joint, "7", currentViewID,
				RegisteredCommandProducer.VIEW_ID, "sms.navbar.commands",
				false);

	}

	private void renderBranch(UIJointContainer joint, String id,
			String currentViewID, String linkViewID, String message,
			boolean renderSeperator) {

		UIBranchContainer cell = UIBranchContainer.make(joint, "navigation-cell:", id);		
		UIInternalLink link = UIInternalLink.make(cell, "item-link", UIMessage.make(message), new SimpleViewParameters(linkViewID));

		if (currentViewID != null && currentViewID.equals(linkViewID)) {
			link.decorate( new UIStyleDecorator("inactive"));
		}

		if (renderSeperator) {
			UIOutput.make(cell, "item-separator");
		}
	}
}
