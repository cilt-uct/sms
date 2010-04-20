/***********************************************************************************
 * HelperProducer.java
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
import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.model.SmsAccount;
import org.sakaiproject.sms.model.constants.SmsConstants;
import org.sakaiproject.sms.tool.beans.ActionResults;
import org.sakaiproject.sms.tool.otp.SmsTaskLocator;
import org.sakaiproject.sms.tool.util.SmsAccountHelper;

import uk.org.ponder.beanutil.BeanGetter;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInitBlock;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.decorators.UIDisabledDecorator;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class HelperProducer implements ViewComponentProducer,
		NavigationCaseReporter {

	public static final String VIEW_ID = "helper";

	private SmsAccountHelper accountHelper;
	private SmsTaskLocator smsTaskLocator;
	private BeanGetter ELEvaluator;
	private ExternalLogic externalLogic;

	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {

		// get number format for default locale
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(1);

		String smsTaskOTP = SmsTaskLocator.LOCATOR_NAME + "."
				+ SmsTaskLocator.NEW_1;

		UIMessage.make(tofill, "page-title", "sms.helper.title");
		UIMessage.make(tofill, "sms-helper-heading", "sms.helper.heading");
		
		// check if locator has new 1 value before EL are evaluated 
		boolean containsNewPreEval = smsTaskLocator.containsNew();
		
		if (ELEvaluator.getBean(smsTaskOTP) == null) { // Prelim task is Null (probably account not found)
			UIMessage.make(tofill, "invalid-account-msg", "sms.helper.invalid-account-msg",
							new Object[] { externalLogic.getCurrentSiteId(), externalLogic.getCurrentUserId()});
		} else {
			UIForm form = UIForm.make(tofill, "helper-form");

			UIMessage.make(form, "message-body-label", "sms.helper.message-body");
			UIInput messageBody = UIInput.make(form, "message-body", smsTaskOTP
					+ ".messageBody");
			messageBody.mustapply = true;

			UIMessage.make(form, "chars-remaining-label",
					"sms.helper.chars-remaining");
			UIInput charsRemaining = UIInput.make(form, "chars-remaining", null,
					Integer.toString(SmsConstants.MAX_SMS_LENGTH));
			// Disables the characters remaining input
			charsRemaining.decorate(new UIDisabledDecorator());

			if (containsNewPreEval) {
				UICommand.make(form, "action-button", UIMessage
						.make("sms.general.save"), "HelperActionBean.save");
			} else {
				UICommand.make(form, "action-button", UIMessage
						.make("sms.general.continue"),
						"HelperActionBean.doContinue");
			}
			UICommand.make(form, "cancel-button", UIMessage
					.make("sms.general.cancel"));

			UIMessage.make(form, "estimated-group-size-label",
					"sms.helper.estimated-group-size");
			UIInput groupSize = UIInput.make(form, "estimated-group-size",
					smsTaskOTP + ".groupSizeEstimate");
			groupSize.decorate(new UIDisabledDecorator());
			groupSize.fossilize = false;

			UIMessage.make(form, "account-credits-label",
					"sms.helper.account-credits");
			SmsAccount account = accountHelper.retrieveAccount(smsTaskOTP
					+ ".smsAccountId");
			UIInput accountCredits = UIInput.make(form, "account-credits", null,
					nf.format(account.getCredits()) );
			accountCredits.decorate(new UIDisabledDecorator());
			accountCredits.fossilize = false;

			UIMessage.make(form, "estimated-credits-label",
					"sms.helper.estimated-credits");
			UIInput estimatedCost = UIInput.make(form, "estimated-credits",
					smsTaskOTP + ".groupSizeEstimate");
			estimatedCost.decorate(new UIDisabledDecorator());
			estimatedCost.fossilize = false;

			UIInitBlock
					.make(
							tofill,
							"init-msg-body-change",
							"initMsgBodyChange",
							new Object[] {
									messageBody,
									charsRemaining,
									Integer
									.toString(SmsConstants.MAX_SMS_LENGTH) });			
		}
	}

	public String getViewID() {
		return VIEW_ID;
	}

	/**
	 * @see NavigationCaseReporter#reportNavigationCases()
	 */
	public List<NavigationCase> reportNavigationCases() {
		List<NavigationCase> list = new ArrayList<NavigationCase>();
		list.add(new NavigationCase(ActionResults.CANCEL,
				new SimpleViewParameters(HelperProducer.VIEW_ID)));
		list.add(new NavigationCase(ActionResults.ERROR,
				new SimpleViewParameters(HelperProducer.VIEW_ID)));
		list.add(new NavigationCase(ActionResults.CONTINUE,
				new SimpleViewParameters(HelperProducer.VIEW_ID),
				ARIResult.FLOW_FASTSTART));
		list.add(new NavigationCase(ActionResults.SUCCESS,
				new SimpleViewParameters(HelperProducer.VIEW_ID)));
		return list;
	}

	public void setAccountHelper(SmsAccountHelper accountHelper) {
		this.accountHelper = accountHelper;
	}

	public void setSmsTaskLocator(SmsTaskLocator smsTaskLocator) {
		this.smsTaskLocator = smsTaskLocator;
	}
	
	public void setELEvaluator(BeanGetter ELEvaluator) {
		this.ELEvaluator = ELEvaluator;
	}
	
	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

}
