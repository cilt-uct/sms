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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.bean.SearchFilterBean;
import org.sakaiproject.sms.logic.SmsAccountLogic;
import org.sakaiproject.sms.logic.SmsTaskLogic;
import org.sakaiproject.sms.logic.exception.SmsSearchException;
import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.model.SmsAccount;
import org.sakaiproject.sms.model.SmsTask;
import org.sakaiproject.sms.model.constants.SmsConstants;
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
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIAlternativeTextDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class MainProducer implements ViewComponentProducer, DefaultView {
	
	public static final String VIEW_ID = "index";
	
	public static final Log log = LogFactory.getLog(MainProducer.class);
		
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
		
		// get number format for default locale
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(1);
		
		//view variables
		String currentSiteId = externalLogic.getCurrentSiteId();
		String currentUserId = externalLogic.getCurrentUserId();
		SmsAccount smsAccount = smsAccountLogic.getSmsAccount(currentSiteId, currentUserId);
		boolean hasAccount = smsAccount != null;
		boolean hasAccountEnabled = Boolean.FALSE;
		if ( hasAccount ){
			hasAccountEnabled = smsAccount.getAccountEnabled().booleanValue();
		}
		boolean hasCredits = hasAccount && smsAccount.getCredits() != 0;
		double credits = hasAccount ? smsAccount.getCredits() : 0;
		boolean hasSendPermission = externalLogic.isUserAllowedInLocation(currentUserId, ExternalLogic.SMS_SEND, currentSiteId );
		//Do search with no date restrictions
		SearchFilterBean searchFilterBean = new SearchFilterBean(null, null, "dateToSend", SmsConstants.SORT_DESC);
		//Show only the current site's tasks
		searchFilterBean.setSakaiSiteId(currentSiteId);
		//Use only current users's tasks if user doesn't have the send permission
		if ( ! hasSendPermission ){
			searchFilterBean.setSenderUserId(currentUserId);
		}
		
		// Restrict to this tool
		searchFilterBean.setToolId("sakai.sms.user");
		
		List<SmsTask> smsTasks = new ArrayList<SmsTask>();
		try {
			smsTasks = smsTaskLogic.getAllSmsTasksForCriteria(searchFilterBean);
		} catch (SmsSearchException e) {
			log.info("SmsSearchException: " + e.getMessage());
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
					UIOutput.make(tofill, "console-credits", nf.format(credits) );
					UIOutput.make(tofill, "console-value", currencyUtil.toServerLocale( smsAccountLogic.getAccountBalance(credits)) );
				}
			}else{
				UIMessage.make(tofill, "error-credits", "ui.error.cannot.create");
			}
		}
		if ( hasTasks ){
			UIMessage.make(tofill, "tasks-title", "ui.tasks.title");
			UIOutput.make(tofill, "tasks-table");
			fillTableHeaders( tofill, new String[] {"message", "status", "author", "time", "recipients"});

			//show table rows
			for (SmsTask sms : smsTasks){
				UIBranchContainer row = UIBranchContainer.make(tofill, "task-row:");
				String statusCode = sms.getStatusCode();
				String statusFullName = statusUtils.getStatusFullName(statusCode);
				SmsParams statusParams = new SmsParams();
				statusParams.setId(sms.getId() + "");
				statusParams.viewID = SmsDetailProducer.VIEW_ID;
				UIInternalLink.make(row, "task-message", sms.getMessageBody(), statusParams);
				UILink statusIcon = UILink.make(row, "task-status", statusUtils.getTaskStatusIcon(statusCode));
				//show alt and tooltips for status detail on status icon
				DecoratorList iconDecorators = new DecoratorList();
				iconDecorators.add(new UIAlternativeTextDecorator(statusFullName));
				iconDecorators.add(new UITooltipDecorator(statusFullName));
				statusIcon.decorators = iconDecorators;
				
				UIOutput.make(row, "task-author", sms.getSenderUserName());
				
				if ( statusUtils.isTaskBusy(statusCode) ){
					UIOutput.make(row, "task-time", statusFullName);
				}else{
					UIOutput.make(row, "task-time", dateUtil.formatDate(sms.getDateToSend()));
				}

				UIMessage.make(row, "task-recipients", "ui.task.recipents", new Object[] {sms.getMessagesDelivered(), (sms.getGroupSizeActual() == null || sms.getGroupSizeActual() == 0) ? sms.getGroupSizeEstimate() : sms.getGroupSizeActual()}); 
			}
		}
	}
	
	private void fillTableHeaders(UIContainer tofill, String[] headers) {
		// Render table headers
		for (int i=0; i < headers.length; i++){
			String header = headers[i];
			UIMessage.make(tofill, "tasks-" + header, "ui.tasks.headers." + header);
		}
	}
}

