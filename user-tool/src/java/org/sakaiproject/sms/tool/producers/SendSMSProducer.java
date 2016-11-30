/**********************************************************************************
 * $URL$
 * $Id$
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
import java.util.Calendar;
import java.util.Date;

import org.sakaiproject.sms.logic.HibernateLogicLocator;
import org.sakaiproject.sms.logic.SmsAccountLogic;
import org.sakaiproject.sms.logic.SmsTaskLogic;
import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.model.SmsAccount;
import org.sakaiproject.sms.model.SmsConfig;
import org.sakaiproject.sms.model.SmsTask;
import org.sakaiproject.sms.tool.params.SmsParams;
import org.sakaiproject.sms.tool.util.CurrencyUtil;
import org.sakaiproject.sms.tool.util.DateUtil;
import org.sakaiproject.sms.tool.util.StatusUtils;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBoundString;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UIIDStrategyDecorator;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.evolvers.FormatAwareDateInputEvolver;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

public class SendSMSProducer implements ViewComponentProducer,
		ViewParamsReporter {

	public static final String VIEW_ID = "create-sms";

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

	private SmsTaskLogic smsTaskLogic;

	public void setSmsTaskLogic(SmsTaskLogic smsTaskLogic) {
		this.smsTaskLogic = smsTaskLogic;
	}
	
	private DateUtil dateUtil;

	public void setDateUtil(DateUtil dateUtil) {
		this.dateUtil = dateUtil;
	}

	private FormatAwareDateInputEvolver dateEvolver;

	public void setDateEvolver(FormatAwareDateInputEvolver dateEvolver) {
		this.dateEvolver = dateEvolver;
	}

	private HibernateLogicLocator hibernateLogicLocator;

	public void setHibernateLogicLocator(
			HibernateLogicLocator hibernateLogicLocator) {
		this.hibernateLogicLocator = hibernateLogicLocator;
	}

	private CurrencyUtil currencyUtil;

	public void setCurrencyUtil(CurrencyUtil currencyUtil) {
		this.currencyUtil = currencyUtil;
	}

	private MessageLocator messageLocator;

	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
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
			renderFooter(tofill);
		} else {
			SmsAccount smsAccount = smsAccountLogic.getSmsAccount(
					currentSiteId, currentUserId);
			SmsParams smsParams = (SmsParams) viewparams;
			SmsTask smsTask = new SmsTask();
			if (smsParams.id != null && !"".equals(smsParams.id)) {
				smsTask = smsTaskLogic.getSmsTask(Long.parseLong(smsParams.id));
			}
			boolean hasAccount = smsAccount != null;
			boolean hasAccountEnabled = Boolean.FALSE;
			if (hasAccount) {
				hasAccountEnabled = smsAccount.getAccountEnabled()
						.booleanValue();
			}
			boolean hasCredits = hasAccount && smsAccount.getCredits() != 0;
			boolean isEditing = StatusUtils.statusType_EDIT
					.equals(smsParams.status);

			if (!hasAccount && !isEditing) {
				UIMessage.make(tofill, "error-account", "ui.error.no.account");
				renderFooter(tofill);
			} else if (!hasAccountEnabled && !isEditing) {
				UIMessage.make(tofill, "error-account-disabled",
						"ui.error.bisabled.account");
				renderFooter(tofill);
			} else {
				if (hasCredits || isEditing) {

					if (smsParams.id != null && !"".equals(smsParams.id)) {
						smsTask = smsTaskLogic.getSmsTask(Long
								.parseLong(smsParams.id));
						UIMessage.make(tofill, "sms-header",
								"ui.edit.sms.header");
					} else {
						UIMessage.make(tofill, "sms-header",
								"ui.create.sms.header");
					}

					UIForm form = UIForm.make(tofill, "form");

					// textarea
					UIInput.make(form, "messageBody", null,
							smsTask.getId() == null ? null : smsTask
									.getMessageBody());

					if (smsTask.getId() == null) {
						UIInternalLink smsAddRecipients = UIInternalLink
								.make(
										form,
										"form-add-recipients",
										UIMessage.make("ui.send.message.add"),
										new SmsParams(
												ChooseRecipientsProducer.VIEW_ID));
						DecoratorList addLinkDecoratorList = new DecoratorList();
						addLinkDecoratorList.add(new UIIDStrategyDecorator(
								"smsAddRecipients"));
						addLinkDecoratorList
								.add(new UIFreeAttributeDecorator(
										"rel",
										messageLocator
												.getMessage("ui.send.message.add")
												+ ","
												+ messageLocator
														.getMessage("ui.send.message.edit")));
						smsAddRecipients.decorators = addLinkDecoratorList;
					} else {
						UIInternalLink.make(
								form,
								"form-add-recipients",
								UIMessage.make("ui.send.message.edit"),
								new SmsParams(ChooseRecipientsProducer.VIEW_ID,
										smsTask.getId() + "")).decorate(
								new UIIDStrategyDecorator("smsAddRecipients"));
					}

					if (hasAccount) {
						// Render billing report
						UIOutput.make(tofill, "console-selected", (smsTask
								.getGroupSizeEstimate() == null) ? 0 + ""
								: smsTask.getGroupSizeEstimate() + "");
						UIOutput.make(tofill, "console-credits", nf.format(smsTask.getCreditEstimate()));
						UIOutput.make(tofill, "console-cost", currencyUtil.toServerLocale((smsTask
								.getCostEstimate())));
					}

					dateEvolver.setStyle(FormatAwareDateInputEvolver.DATE_TIME_INPUT);
					if (smsTask.getDateToSend() == null) {
						UIBoundBoolean boolSchedule = UIBoundBoolean.make(form,
								"booleanSchedule", Boolean.FALSE);
						UIMessage.make(form, "booleanSchedule-label",
								"ui.send.date.schedule").decorate(
								new UILabelTargetDecorator(boolSchedule));
						UIInput dateToSend = UIInput.make(form, "smsDatesScheduleDate-iso8601", null, dateUtil.getISO8601FormattedDateStr(smsTask.getDateToSend()));
					} else {
						UIOutput.make(form, "schedule.date", dateUtil.formatDate(smsTask
								.getDateToSend()) );
						UIBoundBoolean boolSchedule = UIBoundBoolean.make(form,
								"booleanSchedule", Boolean.TRUE);
						UIMessage.make(form, "booleanSchedule-label",
								"ui.send.date.schedule").decorate(
								new UILabelTargetDecorator(boolSchedule));						
						UIInput dateToSend = UIInput.make(form, "smsDatesScheduleDate-iso8601", null, dateUtil.getISO8601FormattedDateStr(smsTask.getDateToSend()));
					}
					
					if (smsTask.getDateToExpire() == null) {
						UIBoundBoolean boolSchedule = UIBoundBoolean.make(form,
								"booleanExpiry", Boolean.FALSE);
						UIMessage.make(form, "booleanExpiry-label",
								"ui.send.date.expiry").decorate(
								new UILabelTargetDecorator(boolSchedule));
						// Set default expiry time
						SmsConfig siteConfig = hibernateLogicLocator
								.getSmsConfigLogic()
								.getOrCreateSystemSmsConfig();
						Calendar cal = Calendar.getInstance();
						cal.setTime(new Date());
						cal.add(Calendar.SECOND, siteConfig
								.getSmsTaskMaxLifeTime());
						UIInput expireDate = UIInput.make(form, "smsDatesExpiryDate-iso8601", null, dateUtil.getISO8601FormattedDateStr(cal.getTime()));
					} else {
						UIOutput.make(form, "expiry.date", dateUtil.formatDate(smsTask.getDateToExpire()) );
						UIBoundBoolean boolExpiry = UIBoundBoolean.make(form,
								"booleanExpiry", Boolean.TRUE);
						UIMessage.make(form, "booleanExpiry-label",
								"ui.send.date.expiry").decorate(
								new UILabelTargetDecorator(boolExpiry));
						UIInput expireDate = UIInput.make(form, "smsDatesExpiryDate-iso8601", null, dateUtil.getISO8601FormattedDateStr(smsTask.getDateToExpire()));
					}

					if (smsTask.getId() != null) {
						if (smsTask.getDeliveryEntityList() != null) {
							UIInput.make(
									tofill,
									"taskdeliveryEntityList",
									null,
									toJSONarray(smsTask.getDeliveryEntityList()
											.toArray(new String[] {}))) // turn
																		// entity
																		// list
																		// into
																		// a JS
																		// Array
																		// object
									.decorate(
											new UIIDStrategyDecorator(
													"taskdeliveryEntityList"));
						}
						if (smsTask.getSakaiUserIds() != null) {
							UIInput.make(
									tofill,
									"taskuserIds",
									null,
									toJSONarray(smsTask.getSakaiUserIdsList()
											.toArray(new String[] {}))) // turn
																		// user
																		// ids
																		// into
																		// a JS
																		// Array
																		// object
									.decorate(
											new UIIDStrategyDecorator(
													"taskuserIds"));
						}
						if (smsTask.getDeliveryMobileNumbersSet() != null) {
							UIInput
									.make(
											tofill,
											"taskdeliveryMobileNumbersSet",
											null,
											toJSONarray(smsTask
													.getDeliveryMobileNumbersSet()
													.toArray(new String[] {})))
									// turn DeliveryMobileNumbersSet into a JS
									// Array object
									.decorate(
											new UIIDStrategyDecorator(
													"taskdeliveryMobileNumbersSet"));
						}
						UIInput smsId = UIInput.make(form, "id", null, smsTask
								.getId()
								+ "");
						smsId.fossilize = false;
						smsId.decorate(new UIIDStrategyDecorator("smsId"));

						UIInput.make(form, "taskcopyMe", null,
								smsTask.getSakaiUserIdsList().contains(
										currentUserId) ? Boolean
										.toString(Boolean.TRUE) : Boolean
										.toString(Boolean.FALSE)).fossilize = false;
					}

					UIInput statusType = UIInput
							.make(
									form,
									"statusType",
									null,
									smsParams.status == null ? StatusUtils.statusType_NEW
											: smsParams.status);
					statusType.fossilize = false;
					statusType
							.decorate(new UIIDStrategyDecorator("statusType"));
					UIInput.make(tofill, "sakaiSiteId", null, currentSiteId).fossilize = false;
					UICommand.make(form, "form-send",
							getCommandText(smsParams.status), null).decorate(
							new UIIDStrategyDecorator("smsSend"));
					UIInternalLink.make(form, "goto-home", new SmsParams(
							MainProducer.VIEW_ID));
					UICommand.make(form, "back", UIMessage
							.make("sms.general.cancel"));

				} else {
					UIMessage.make(tofill, "error", "ui.error.cannot.create");
					renderFooter(tofill);
				}
			}
		}

	}

	private void renderFooter(UIContainer tofill) {
		UICommand.make(tofill, "back2", UIMessage.make("sms.general.back"));
		UIMessage.make(tofill, "error-help", "ui.console.help");
		// IOutput.make(tofill, "error-email"); //TODO show email for credit
		// purchases
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

	private UIBoundString getCommandText(String statusType) {
		if (StatusUtils.statusType_NEW.equals(statusType)) {
			return UIMessage.make("sms.general.send");
		} else if (StatusUtils.statusType_EDIT.equals(statusType)) {
			return UIMessage.make("ui.send.save");
		} else if (StatusUtils.statusType_REUSE.equals(statusType)) {
			return UIMessage.make("ui.send.reused");
		} else {
			return null;
		}
	}

	public ViewParameters getViewParameters() {
		// TODO Auto-generated method stub
		return new SmsParams();
	}

}
