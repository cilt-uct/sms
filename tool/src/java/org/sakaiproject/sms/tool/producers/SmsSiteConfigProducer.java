/***********************************************************************************
 * SMSConfigProducer.java
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

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.sms.model.hibernate.constants.SmsHibernateConstants;
import org.sakaiproject.sms.tool.beans.ActionResults;
import org.sakaiproject.sms.tool.otp.SmsConfigLocator;
import org.sakaiproject.sms.tool.util.MessageFixupHelper;

import uk.org.ponder.rsf.components.ELReference;
import uk.org.ponder.rsf.components.UIBoundList;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class SmsSiteConfigProducer implements ViewComponentProducer,
		NavigationCaseReporter {

	public static final String VIEW_ID = "SmsSiteConfig";

	private MessageFixupHelper messageFixupHelper;

	public String getViewID() {
		return VIEW_ID;
	}

	public void setMessageFixupHelper(MessageFixupHelper messageFixupHelper) {
		this.messageFixupHelper = messageFixupHelper;
	}

	public void init() {
		messageFixupHelper.fixupMessages("sms-config-paging-size");
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {
		init();

		String smsConfigOTP = SmsConfigLocator.LOCATOR_NAME + "."
				+ SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID;

		UIInternalLink.make(tofill, "system-settings",
				new SimpleViewParameters(SmsSystemConfigProducer.VIEW_ID));

		UIMessage.make(tofill, "page-title", "sms.site.config.title");
		UIForm smsSiteConfigform = UIForm.make(tofill, "sms-site-config-form");
		UIMessage.make(tofill, "page-heading", "sms.site.config.title");

		UIMessage.make(smsSiteConfigform, "sms-enabled",
				"sms.site.config.enabled");
		UISelect combo = UISelect.make(smsSiteConfigform, "sms-config-enabled");
		combo.selection = new UIInput();
		combo.selection.valuebinding = new ELReference(smsConfigOTP
				+ ".sendSmsEnabled");
		UIBoundList comboValues = new UIBoundList();
		comboValues.setValue(new String[] { "true", "false" });
		combo.optionlist = comboValues;
		UIBoundList comboNames = new UIBoundList();
		comboNames.setValue(new String[] { "Yes", "No" });
		combo.optionnames = comboNames;

		UIMessage.make(smsSiteConfigform, "notification-email",
				"sms.site.config.notification.email");
		UIInput.make(smsSiteConfigform, "sms-config-notification-email",
				smsConfigOTP + ".notificationEmail");

		UIMessage.make(smsSiteConfigform, "paging-size",
				"sms.site.config.paging.size");
		UIInput pagingSizeInput = UIInput.make(smsSiteConfigform,
				"sms-config-paging-size", smsConfigOTP + ".pagingSize");
		pagingSizeInput.decorators = new DecoratorList(new UITooltipDecorator(
				UIMessage.make("sms.site.config.paging.size-tooltip")));

		UICommand.make(smsSiteConfigform, "save",
				"#{smsSiteConfigActionBean.save}");
		UICommand.make(smsSiteConfigform, "cancel", "#");
	}

	@SuppressWarnings("unchecked")
	public List reportNavigationCases() {
		List<NavigationCase> list = new ArrayList<NavigationCase>();
		list.add(new NavigationCase(ActionResults.SUCCESS,
				new SimpleViewParameters(SmsSiteConfigProducer.VIEW_ID),
				ARIResult.FLOW_ONESTEP));
		list.add(new NavigationCase(ActionResults.CANCEL,
				new SimpleViewParameters(SmsSiteConfigProducer.VIEW_ID)));
		return list;
	}

}
