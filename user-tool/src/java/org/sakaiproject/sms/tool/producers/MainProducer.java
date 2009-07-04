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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.hibernate.SmsAccountLogic;
import org.sakaiproject.sms.logic.hibernate.SmsTaskLogic;
import org.sakaiproject.sms.model.hibernate.SmsAccount;
import org.sakaiproject.sms.model.hibernate.SmsTask;
import org.sakaiproject.sms.tool.params.SmsParams;
import org.sakaiproject.sms.tool.renderers.UserNavBarRenderer;
import org.sakaiproject.sms.tool.util.CurrencyUtil;
import org.sakaiproject.sms.tool.util.DateUtil;
import org.sakaiproject.sms.tool.util.StatusUtils;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIAlternativeTextDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class MainProducer implements ViewComponentProducer, DefaultView {
	
	public static final String VIEW_ID = "index";
	
	public static Log log = LogFactory.getLog(MainProducer.class);
		
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
	
	private DateUtil dateUtil;
	public void setDateUtil(DateUtil dateUtil) {
		this.dateUtil = dateUtil;
	}

	private CurrencyUtil currencyUtil;
	public void setCurrencyUtil(CurrencyUtil currencyUtil) {
		this.currencyUtil = currencyUtil;
	}

	private SmsTaskLogic smsTaskLogic;
	public void setSmsTaskLogic(SmsTaskLogic smsTaskLogic) {
		this.smsTaskLogic = smsTaskLogic;
	}
	
	private StatusUtils statusUtils;
	public void setStatusUtils(StatusUtils statusUtils) {
		this.statusUtils = statusUtils;
	}
	
	private UserNavBarRenderer userNavBarRenderer;
	public void setUserNavBarRenderer(UserNavBarRenderer userNavBarRenderer) {
		this.userNavBarRenderer = userNavBarRenderer;
	}
	
	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {
		
		//view variables
		String currentSiteId = externalLogic.getCurrentSiteId();
		String currentUserId = externalLogic.getCurrentUserId();
		SmsAccount smsAccount = smsAccountLogic.getSmsAccount(currentSiteId, currentUserId);
		List<SmsTask> smsTasks = smsTaskLogic.getAllSmsTask();
		//Show only the current site's tasks
		smsTasks = filterTasksBySite(currentSiteId, smsTasks);
		boolean hasAccount = smsAccount != null;
		boolean hasAccountEnabled = Boolean.FALSE;
		if ( hasAccount ){
			hasAccountEnabled = smsAccount.getAccountEnabled().booleanValue();
		}
		boolean hasCredits = hasAccount && smsAccount.getCredits() != 0;
		Long credits = hasAccount ? smsAccount.getCredits() : 0l;
		boolean hasSendPermission = externalLogic.isUserAllowedInLocation(currentUserId, ExternalLogic.SMS_SEND, currentSiteId );
		//Use only current users's tasks if user doesn't have the send permission
		if ( ! hasSendPermission ){
			smsTasks = filterTasksBySender(currentUserId, smsTasks);
		}
		boolean hasTasks = smsTasks.size() > 0;
		
		if (! hasAccount ){
			UIMessage.make(tofill, "error-account", "ui.error.no.account");
		}else if(! hasAccountEnabled ){
			UIMessage.make(tofill, "error-account-disabled", "ui.error.bisabled.account");
		}else{
			if ( hasCredits ){
				//Top links
				userNavBarRenderer.makeNavBar(tofill, "navIntraTool:", VIEW_ID, currentUserId, currentSiteId);
				if ( hasSendPermission ){
					UIOutput.make(tofill, "send");
					UIInternalLink.make(tofill, "send-link", UIMessage.make("ui.create.sms.header"), new SmsParams(SendSMSProducer.VIEW_ID, null, StatusUtils.statusType_NEW));
					UIOutput.make(tofill, "reportConsole");
					UIOutput.make(tofill, "console-credits", credits.toString() );
					UIOutput.make(tofill, "console-value", currencyUtil.toServerLocale(smsAccountLogic.getAccountBalance(credits)) );
				}
			}else{
				UIMessage.make(tofill, "error-credits", "ui.error.cannot.create");
			}
		}
		if ( hasTasks ){
			UIMessage.make(tofill, "tasks-title", "ui.tasks.title");
			UIOutput.make(tofill, "tasks-table");
			fillTableHeaders( tofill, new String[] {"message", "status", "author", "time", "recipients", "cost"});

			//show table rows
			for (SmsTask sms : smsTasks){
				UIBranchContainer row = UIBranchContainer.make(tofill, "task-row:");
				String status = sms.getStatusCode();
				String detailView = statusUtils.getStatusProducer(status);
				String statusFullName = statusUtils.getStatusFullName(status);
				SmsParams statusParams = new SmsParams();
				
				//Fix additional parameter. Used by the {@link ProgressSmsDetailProducer} to show either inprogress or scheduled task
				if (ProgressSmsDetailProducer.const_Inprogress.equals(detailView)){
					statusParams.viewID = ProgressSmsDetailProducer.VIEW_ID;
					statusParams.status = ProgressSmsDetailProducer.const_Inprogress;
				}else if (ProgressSmsDetailProducer.const_Scheduled.equals(detailView)){
					statusParams.viewID = ProgressSmsDetailProducer.VIEW_ID;
					statusParams.status = ProgressSmsDetailProducer.const_Scheduled;
				}else{
					statusParams.viewID = detailView;
					statusParams.status = ProgressSmsDetailProducer.const_Normal;
				}
				
				statusParams.setId(sms.getId() + "");
				UIInternalLink.make(row, "task-message", sms.getMessageBody(), statusParams);
				UILink statusIcon = UILink.make(row, "task-status", statusUtils.getStatusIcon(status));
				statusIcon.decorate(new UIAlternativeTextDecorator(statusFullName)); 
				statusIcon.decorate(new UITooltipDecorator(statusFullName)); 
				UIOutput.make(row, "task-author", sms.getSenderUserName());
				
				if ( ProgressSmsDetailProducer.const_Inprogress.equals(detailView)){
					UIOutput.make(row, "task-time", statusFullName);
				}else{
					UIOutput.make(row, "task-time", dateUtil.formatDate(sms.getDateToSend()));
				}

				UIMessage.make(row, "task-recipients", "ui.task.recipents", new Object[] {sms.getMessagesDelivered(), (sms.getGroupSizeActual() == null || sms.getGroupSizeActual() == 0) ? sms.getGroupSizeEstimate() : sms.getGroupSizeActual()}); 
				UIOutput.make(row, "task-cost", sms.getCreditCost() == 0 ?  sms.getCreditEstimate().toString():  (int) sms.getCreditCost() + "" );
			}
		}
	}
	private List<SmsTask> filterTasksBySite(String currentSiteId,
			List<SmsTask> smsTasks) {
		List<SmsTask> taskList = new ArrayList<SmsTask>();
		for (SmsTask smsTask : smsTasks){
			if ( smsTask.getSakaiSiteId().equals(currentSiteId) ){
				taskList.add(smsTask);
			}
		}
		return taskList;
	}

	private List<SmsTask> filterTasksBySender(String currentUserId,
			List<SmsTask> smsTasks) {
		List<SmsTask> taskList = new ArrayList<SmsTask>();
		for (SmsTask smsTask : smsTasks){
			if ( smsTask.getSenderUserId().equals(currentUserId) ){
				taskList.add(smsTask);
			}
		}
		return taskList;
	}

	private void fillTableHeaders(UIContainer tofill, String[] headers) {
		// Render table headers
		for (int i=0; i < headers.length; i++){
			String header = headers[i];
			UIMessage.make(tofill, "tasks-" + header, "ui.tasks.headers." + header)
				.decorate(new UITooltipDecorator(UIMessage.make("ui.tasks.headers." + header + ".tooltip")));
		}
	}
}

