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

import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.hibernate.HibernateLogicLocator;
import org.sakaiproject.sms.logic.hibernate.SmsTaskLogic;
import org.sakaiproject.sms.model.hibernate.SmsConfig;
import org.sakaiproject.sms.model.hibernate.SmsTask;
import org.sakaiproject.sms.tool.params.SmsParams;
import org.sakaiproject.sms.tool.renderers.SavedSelectionsRenderer;
import org.sakaiproject.sms.tool.renderers.SmsMessageRenderer;
import org.sakaiproject.sms.tool.renderers.UserNavBarRenderer;
import org.sakaiproject.sms.tool.util.CurrencyUtil;
import org.sakaiproject.sms.tool.util.DateUtil;
import org.sakaiproject.sms.tool.util.StatusUtils;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIAlternativeTextDecorator;
import uk.org.ponder.rsf.components.decorators.UIIDStrategyDecorator;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.request.EarlyRequestParser;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

public class ProgressSmsDetailProducer implements ViewComponentProducer, ViewParamsReporter {
	
	public static final String VIEW_ID = "inprogress-sms-detail";
	public static final String const_Inprogress = "inprogress";
	public static final String const_Scheduled = "scheduled";
	public static final String const_Normal = "normal";
	
	public String getViewID() {
		return VIEW_ID;
	}
	
	private SmsTaskLogic smsTaskLogic;
	public void setSmsTaskLogic(SmsTaskLogic smsTaskLogic) {
		this.smsTaskLogic = smsTaskLogic;
	}
	
	private DateUtil dateUtil;
	public void setDateUtil(DateUtil dateUtil) {
		this.dateUtil = dateUtil;
	}
	
	private StatusUtils statusUtils;
	public void setStatusUtils(StatusUtils statusUtils) {
		this.statusUtils = statusUtils;
	}
	
	private HibernateLogicLocator hibernateLogicLocator;
	public void setHibernateLogicLocator(
			HibernateLogicLocator hibernateLogicLocator) {
		this.hibernateLogicLocator = hibernateLogicLocator;
	}

	private SavedSelectionsRenderer savedSelectionsRenderer;
	public void setSavedSelectionsRenderer(
			SavedSelectionsRenderer savedSelectionsRenderer) {
		this.savedSelectionsRenderer = savedSelectionsRenderer;
	}
	
	private SmsMessageRenderer smsMessageRenderer;
	public void setSmsMessageRenderer(SmsMessageRenderer smsMessageRenderer) {
		this.smsMessageRenderer = smsMessageRenderer;
	}
	
	private CurrencyUtil currencyUtil;
	public void setCurrencyUtil(CurrencyUtil currencyUtil) {
		this.currencyUtil = currencyUtil;
	}
	
	private ExternalLogic externalLogic;
	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {
		
		if ( viewparams != null ){
			SmsParams statusParams = (SmsParams) viewparams;
			if ( statusParams != null && statusParams.id != null && statusParams.status != null ){
				
				Long smsId = Long.parseLong(statusParams.id);
				String currentUserId = externalLogic.getCurrentUserId();
				String currentSiteId = externalLogic.getCurrentSiteId();
				SmsTask smsTask = smsTaskLogic.getSmsTask(smsId);
				String statusToShow = statusParams.status;
		
				//Show message
				smsMessageRenderer.renderMessage(smsTask, tofill, "message:");
				
				UIBranchContainer status = UIBranchContainer.make(tofill, "status:");
				String statusCode = smsTask.getStatusCode();
				UILink.make(status, "sms-status", statusUtils.getStatusIcon(statusCode))
					.decorate(new UIAlternativeTextDecorator(statusUtils.getStatusFullName(statusCode)));
				SmsConfig siteConfig = hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSystemSmsConfig();
				
				UIOutput statusText = UIOutput.make(status, "sms-status-title", statusUtils.getStatusFullName(statusCode));
				
				UIMessage.make(tofill, "sms-status-retries", "ui.inprogress.retries", new Object[] { smsTask.getAttemptCount(), siteConfig.getSmsRetryMaxCount() } );
				
				//Insert original user selections
				savedSelectionsRenderer.renderSelections(smsTask, tofill, "savedSelections:");
				
				UIMessage.make(tofill, "cost", "ui.inprogress.sms.cost.title");
				UIOutput.make(tofill, "cost-credits", (smsTask.getCreditEstimate() == null) ? 0 + "" : smsTask.getCreditEstimate() + "");
				
				UIOutput.make(tofill, "cost-cost", currencyUtil.toServerLocale(( smsTask.getCostEstimate() )) );
				
				UIForm form = UIForm.make(tofill, "form", new SmsParams(SendSMSProducer.VIEW_ID, smsId.toString(), const_Scheduled.equals(statusToShow)? StatusUtils.statusType_EDIT : StatusUtils.statusType_REUSE));
				form.type = EarlyRequestParser.RENDER_REQUEST;
				//keep the id somewhere that the JS can grab and use it for processing actions edit or delete
				UIInput.make(tofill, "smsId", null, smsTask.getId() + "")
				.decorate(new UIIDStrategyDecorator("smsId"));
				UIInput.make(tofill, "statusToShow", null, statusToShow)
				.decorate(new UIIDStrategyDecorator("statusToShow"));
				/**
				 * The action buttons are handled by JS. RSF is only needed for i18N
				 */
				if ( const_Inprogress.equals(statusToShow)){
					statusText.decorate(new UIStyleDecorator("smsGreenish"));
					UIMessage.make(tofill, "sms-started", "ui.inprogress.sms.started", new Object[] { dateUtil.formatDate(smsTask.getDateToSend()) });
					UIMessage.make(tofill, "delivered", "ui.inprogress.sms.delivered", new Object[] { smsTask.getMessagesDelivered(), (smsTask.getGroupSizeActual() == null || smsTask.getGroupSizeActual() == 0) ? smsTask.getGroupSizeEstimate() : smsTask.getGroupSizeActual() });
				}else if( const_Scheduled.equals(statusToShow)){
					statusText.decorate(new UIStyleDecorator("smsOrange"));
					UIMessage.make(tofill, "sms-started", "ui.scheduled.sms.started", new Object[] { dateUtil.formatDate(smsTask.getDateToSend()) });
					//Check permissions before rendering control buttons
					if ( externalLogic.isUserAllowedInLocation(currentUserId, ExternalLogic.SMS_SEND, currentSiteId ) ) {
						UICommand.make(form, "edit", UIMessage.make("sms.general.edit.sms"))
						.decorate(new UIIDStrategyDecorator("smsEdit"));
						UICommand.make(form, "delete", UIMessage.make("sms.general.delete"));
			        	UIMessage.make(tofill, "actionDelete", "ui.action.confirm.sms.delete", new String[] { smsTask.getMessageBody() });
					}
				}else{
					throw new IllegalArgumentException("Cannot act on this status type: " + statusToShow);
				}
				UIMessage.make(tofill, "sms-finish", "ui.inprogress.sms.finish", new Object[] { dateUtil.formatDate(smsTask.getDateToExpire()) });
				
				UICommand.make(form, "back", UIMessage.make("sms.general.back"));
				
			}else{
				//TODO: show error message since sms.id() is not specified
			}
		}
	}

	public ViewParameters getViewParameters() {
		// TODO Auto-generated method stub
		return new SmsParams();
	}

}

