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
package org.sakaiproject.sms.tool.renderers;

import java.util.List;

import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.SmsAccountLogic;
import org.sakaiproject.sms.model.SmsAccount;
import org.sakaiproject.sms.tool.producers.MainProducer;
import org.sakaiproject.sms.tool.producers.CreditTransferProducer;
import org.sakaiproject.sms.tool.producers.SmsPermissions;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class UserNavBarRenderer {
	
	private ExternalLogic externalLogic;
	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}
	
	private SmsAccountLogic smsAccountLogic;
	public void setSmsAccountLogic(SmsAccountLogic smsAccountLogic) {
		this.smsAccountLogic = smsAccountLogic;
	}
	
	/**
	 * Renderer for the nav bar. If @param currentUserId AND @param currentSiteId are null or invalid, nav bar will not render the permissions link
	 * @param tofill
	 * @param divID
	 * @param currentViewID
	 * @param currentUserId Optional param, can be null
	 * @param currentSiteId Optional param, can be null
	 */
	public void makeNavBar(UIContainer tofill, String divID,
			String currentViewID, String currentUserId, String currentSiteId) {
		if ( (currentSiteId != null && currentUserId !=null ) && externalLogic.isUserAllowedSiteUpdate(currentUserId, currentSiteId )){
			UIJointContainer joint = new UIJointContainer(tofill, divID,
					"sms-navigation:");
		
			List<SmsAccount> accounts = smsAccountLogic.getSmsAccountsForOwner(currentUserId);

			// Show Accounts menu for account owners
			if ( (accounts != null && accounts.size() > 1) || externalLogic.isUserAdmin(currentUserId)){
	
				renderBranch(joint, "1", currentViewID, CreditTransferProducer.VIEW_ID,
						"sms.navbar.transfer", true);
			}

			// Show Permissions menu for all site owners
			renderBranch(joint, "2", currentViewID, SmsPermissions.VIEW_ID,
						"sms.navbar.permissions", false);
		}
	}
	
	private void renderBranch(UIJointContainer joint, String id,
			String currentViewID, String linkViewID, String message,
			boolean renderSeperator) {

		UIBranchContainer cell = UIBranchContainer.make(joint,
				"navigation-cell:", id);

		if (currentViewID != null && currentViewID.equals(linkViewID)) {
			UIMessage.make(cell, "item-text", message);
		} else {
			UIInternalLink.make(cell, "item-link", UIMessage.make(message),
					new SimpleViewParameters(linkViewID));
		}

		if (renderSeperator) {
			UIOutput.make(cell, "item-separator");
		}
	}
}
