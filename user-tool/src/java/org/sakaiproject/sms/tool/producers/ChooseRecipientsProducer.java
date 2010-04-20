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
package org.sakaiproject.sms.tool.producers;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sakaiproject.sms.logic.SmsAccountLogic;
import org.sakaiproject.sms.logic.SmsTaskLogic;
import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.model.SmsAccount;
import org.sakaiproject.sms.model.SmsTask;
import org.sakaiproject.sms.tool.params.SmsParams;
import org.sakaiproject.sms.tool.util.CurrencyUtil;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIOutputMany;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UISelectLabel;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UIIDStrategyDecorator;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

public class ChooseRecipientsProducer implements ViewComponentProducer,
		ViewParamsReporter {

	public static final String VIEW_ID = "choose-recipients";

	public String getViewID() {
		return VIEW_ID;
	}

	private ExternalLogic externalLogic;

	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	private SmsAccountLogic smsAccountLogic;

	public void setSmsAccountLogic(SmsAccountLogic smsAccountLogic) {
		this.smsAccountLogic = smsAccountLogic;
	}

	private MessageLocator messageLocator;

	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}

	private SmsTaskLogic smsTaskLogic;

	public void setSmsTaskLogic(SmsTaskLogic smsTaskLogic) {
		this.smsTaskLogic = smsTaskLogic;
	}

	private CurrencyUtil currencyUtil;

	public void setCurrencyUtil(CurrencyUtil currencyUtil) {
		this.currencyUtil = currencyUtil;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {
		
		// get number format for default locale
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(1);
		
		// view variables
		String currentSiteId = externalLogic.getCurrentSiteId();
		String currentUserId = externalLogic.getCurrentUserId();

		if (!externalLogic.isUserAllowedInLocation(currentUserId,
				ExternalLogic.SMS_SEND, currentSiteId)) {
			UIMessage.make(tofill, "error-account-disabled",
					"ui.error.not.allowed");
		} else {
			SmsAccount smsAccount = smsAccountLogic.getSmsAccount(
					currentSiteId, currentUserId);
			SmsParams smsParams = (SmsParams) viewparams;
			SmsTask smsTask = new SmsTask();
			if (smsParams.id != null && !"".equals(smsParams.id)) {
				smsTask = smsTaskLogic.getSmsTask(Long.parseLong(smsParams.id));
			}

			// Filling tab areas on condition
			fillTabs(tofill, new String[] { "Roles", "Groups", "Names",
					"Numbers" });

			UIForm form = UIForm.make(tofill, "chooseForm");
			form.targetURL = "/direct/sms-task/new";
			form.decorate(new UIIDStrategyDecorator("chooseForm"));
			// Checkboxes for Groups and Roles
			Map<String, String> roles = externalLogic.getSakaiRolesForSite(currentSiteId);
			if (roles.size() > 0) {
				renderCheckBoxes("role", roles, form, smsTask);
			} else {
				UIMessage.make(form, "error-no-roles", "ui.error.no.roles")
						.decorate(new UIIDStrategyDecorator("error-no-roles"));
			}
			Map<String, String> groups = externalLogic.getSakaiGroupsForSite(currentSiteId);
			if (groups.size() > 0) {
				renderCheckBoxes("group", groups, form, smsTask);
			} else {
				UIMessage.make(form, "error-no-groups", "ui.error.no.groups")
						.decorate(new UIIDStrategyDecorator("error-no-groups"));
			}
			// Textearee for the numbers
			UIInput textarea = UIInput.make(form, "peopleListNumbersBox", null);
			textarea
					.decorate(new UIIDStrategyDecorator("peopleListNumbersBox"));
			textarea.decorate(new UIFreeAttributeDecorator("name",
					"deliveryMobileNumbersSet"));

			UILink.make(form, "checkNumbers",
					UIMessage.make("ui.recipients.choose.numbers.check"), null)
					.decorate(new UIIDStrategyDecorator("checkNumbers"));

			// UIInput names = UIInput.make(form, "names-box", null, "");
			// names.decorate(new UIFreeAttributeDecorator("name", null));
			// names.decorate(new UIIDStrategyDecorator("calculateCmd"));
			UICommand.make(form, "calculate",
					UIMessage.make("ui.recipients.choose.calculate"), null)
					.decorate(new UIIDStrategyDecorator("calculateCmd"));

			// copy me checkbox
			UIBoundBoolean copy = UIBoundBoolean.make(form, "copy-me", smsTask
					.getSakaiUserIdsList().contains(currentUserId));
			copy.fossilize = false;
			UIMessage.make(form, "copy-me-label", "ui.recipients.choose.copy")
					.decorate(new UILabelTargetDecorator(copy));

			// Render billing report
			UIOutput.make(tofill, "console-selected", (smsTask
					.getGroupSizeEstimate() == null) ? 0 + "" : smsTask
					.getGroupSizeEstimate()
					+ "");
			UIOutput.make(tofill, "console-credits", nf.format(smsTask.getCreditEstimate()));
			UIOutput.make(tofill, "console-cost", currencyUtil.toServerLocale((smsTask.getCostEstimate())));
			if (smsAccount != null) {
				UIOutput.make(tofill, "console-total", nf.format(smsAccount.getCredits()));
			}

			if (smsTask.getId() != null) {
				if (smsTask.getDeliveryEntityList() != null) {
					UIInput
							.make(
									tofill,
									"savedEntityList",
									null,
									toJSONarray(smsTask.getDeliveryEntityList()
											.toArray(new String[] {})))
							// turn entity list into a JS Array object
							.decorate(
									new UIIDStrategyDecorator("savedEntityList"));
				}
				if (smsTask.getSakaiUserIds() != null) {
					UIInput
							.make(
									tofill,
									"savedUserIds",
									null,
									toJSONarray(smsTask.getSakaiUserIdsList()
											.toArray(new String[] {})))
							// turn user ids into a JS Array object
							.decorate(new UIIDStrategyDecorator("savedUserIds"));
				}
				if (smsTask.getDeliveryMobileNumbersSet() != null) {
					UIInput.make(
							tofill,
							"savedDeliveryMobileNumbersSet",
							null,
							toJSONarray(smsTask.getDeliveryMobileNumbersSet()
									.toArray(new String[] {})))// turn
							// DeliveryMobileNumbersSet
							// into a JS
							// Array object
							.decorate(
									new UIIDStrategyDecorator(
											"savedDeliveryMobileNumbersSet"));
				}
				UIInput.make(tofill, "id", smsTask.getId() + "", null)
						.decorate(new UIIDStrategyDecorator("id"));
			}
			UIInput.make(tofill, "sakaiSiteId", null, currentSiteId).fossilize = false;
			UIInput.make(tofill, "senderUserName", null, externalLogic
					.getSakaiUserDisplayName(currentUserId)).fossilize = false;
			UIInput.make(tofill, "senderUserId", null, currentUserId).fossilize = false;
			UIInput currencyVal = UIInput.make(tofill, "currency", null,
					currencyUtil.currency);
			currencyVal.fossilize = false;
			currencyVal.decorate(new UIIDStrategyDecorator("currency"));
			UIMessage.make(tofill, "errorNoNames", "ui.error.no.names");

			UIMessage.make(tofill, "cancel", "sms.general.cancel");
			UICommand.make(form, "continue",
					UIMessage.make("ui.recipients.choose.continue"), null)
					.decorate(new UIIDStrategyDecorator("recipientsCmd"));
		}

	}

	private void renderCheckBoxes(String type, Map<String, String> typeMap,
			UIForm form, SmsTask smsTask) {
		List<String> boxValues = new ArrayList<String>();
		List<String> boxLabels = new ArrayList<String>();
		UISelect boxes = UISelect.makeMultiple(form, type + "-holder",
				new String[] {}, null, new String[] {});
		String boxesId = boxes.getFullID();

		Iterator<Map.Entry<String, String>> selector = typeMap.entrySet()
				.iterator();
		int count = 0;
		while (selector.hasNext()) {
			Map.Entry<String, String> pairs = selector.next();
			String id = pairs.getKey();
			boxValues.add(id);
			String name = pairs.getValue();
			boxLabels.add(name);

			UIBranchContainer row = UIBranchContainer.make(form,
					type + "-row:", count + "");
			UISelectChoice choice = UISelectChoice.make(row, type + "-box",
					boxesId, count);
			choice.decorate(new UITooltipDecorator(name));
			UISelectLabel label = UISelectLabel.make(row, type + "-label",
					boxesId, count);
			label.decorate(new UIFreeAttributeDecorator(type + "sname", name));
			label.decorate(new UIFreeAttributeDecorator("name", name));
			label.decorate(new UIFreeAttributeDecorator(type + "sid", id));
			UILabelTargetDecorator.targetLabel(label, choice);
			count++;
		}
		boxes.optionlist = UIOutputMany.make(boxValues
				.toArray(new String[boxValues.size()]));
		boxes.optionnames = UIOutputMany.make(boxLabels
				.toArray(new String[boxLabels.size()]));
		boxes.selection.fossilize = false;
	}

	private String toJSONarray(String[] entities) {
		if (entities != null) {
			StringBuilder sb = new StringBuilder();
			int count = 1;
			for (String entity : entities) {
				sb.append(entity);
				if (count != entities.length) {
					sb.append(",");
				}
				count++;
			}
			return sb.toString();
		}
		return null;
	}

	private void fillTabs(UIContainer tofill, String[] tabs) {
		for (int i = 0; i < tabs.length; i++) {
			String tabCapitalised = tabs[i];
			String tab = tabCapitalised.toLowerCase();
			UIOutput.make(tofill, tab).decorate(
					new UIIDStrategyDecorator("peopleTabs" + tabCapitalised));
			if("Names".equals(tabCapitalised)){
				UILink.make(tofill, "names-title-loading", messageLocator
						.getMessage("ui.recipients.choose.names.title.loading"), null);
			}
			UILink
					.make(tofill, tab + "-title", messageLocator
							.getMessage("ui.recipients.choose." + tab
									+ ".title"), null);
			UIOutput.make(tofill, tab + "-selected", 0 + "").decorate(
					new UITooltipDecorator(UIMessage.make(
							"ui.recipients.choose.selected.tooltip",
							new Object[] { messageLocator
									.getMessage("ui.recipients.choose." + tab
											+ ".title") })));
		}
	}

	public ViewParameters getViewParameters() {
		// TODO Auto-generated method stub
		return new SmsParams();
	}

}
