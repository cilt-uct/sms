/***********************************************************************************
 * BillingAdminProducer.java
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

import java.text.NumberFormat;
import java.util.List;

import org.sakaiproject.sms.logic.hibernate.SmsAccountLogic;
import org.sakaiproject.sms.model.hibernate.SmsAccount;
import org.sakaiproject.sms.tool.params.IdParams;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class BillingAdminProducer implements ViewComponentProducer {
	public static final String VIEW_ID = "billing_admin";

	private SmsAccountLogic smsAccountLogic;

	/**
	 * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer,
	 *      uk.org.ponder.rsf.viewstate.ViewParameters,
	 *      uk.org.ponder.rsf.view.ComponentChecker)
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {
		UIMessage.make(tofill, "page-title", "sms.billing-admin.title");

		// TODO: Make it link to add new account screen. Perhaps a specific
		// permission must be checked?
		UIInternalLink.make(tofill, "new-account", new SimpleViewParameters(
				AccountProducer.VIEW_ID));

		UIMessage.make(tofill, "accounts-heading",
				"sms.billing-admin.accounts-heading");

		// Creates headers
		UIMessage.make(tofill, "account-name-title",
				"sms.billing-admin.account-name-title");
		UIMessage.make(tofill, "account-no-title",
				"sms.billing-admin.account-no-title");
		UIMessage.make(tofill, "sakai-site-title",
				"sms.billing-admin.sakai-site-title");
		UIMessage.make(tofill, "sakai-user-title",
				"sms.billing-admin.sakai-user-title");
		UIMessage.make(tofill, "overdraft-limit-title",
				"sms.billing-admin.overdraft-limit-title");
		UIMessage.make(tofill, "balance-title",
				"sms.billing-admin.balance-title");

		List<SmsAccount> accounts = smsAccountLogic.getAllSmsAccounts();

		// get number format for default locale
		NumberFormat nf = NumberFormat.getInstance();

		for (SmsAccount account : accounts) {

			UIBranchContainer entry = UIBranchContainer.make(tofill,
					"account-entry:");

			UIInternalLink.make(entry, "account-name-link", account
					.getAccountName(), new IdParams(AccountProducer.VIEW_ID,
					account.getId().toString()));
			UIOutput.make(entry, "account-no", account.getId().toString());
			// TODO: Use Sakai services to get site and username
			UIOutput.make(entry, "sakai-site", account.getSakaiSiteId());
			UIOutput.make(entry, "sakai-user",
					account.getSakaiUserId() == null ? "" : account
							.getSakaiUserId());
			Float overdraftLimit = account.getOverdraftLimit();
			UIOutput.make(entry, "overdraft-limit", overdraftLimit == null ? ""
					: nf.format(overdraftLimit));
			UIOutput.make(entry, "balance", nf.format(account.getBalance())
					.toString());
		}
	}

	/**
	 * Returns the view id of the Producer
	 */
	public String getViewID() {
		return VIEW_ID;
	}

	public void setSmsAccountLogic(SmsAccountLogic smsAccountLogic) {
		this.smsAccountLogic = smsAccountLogic;
	}
}
