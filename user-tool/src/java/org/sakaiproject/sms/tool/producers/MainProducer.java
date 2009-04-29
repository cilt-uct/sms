package org.sakaiproject.sms.tool.producers;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.hibernate.SmsAccountLogic;
import org.sakaiproject.sms.logic.hibernate.SmsTaskLogic;
import org.sakaiproject.sms.model.hibernate.SmsAccount;
import org.sakaiproject.sms.model.hibernate.SmsTask;
import org.sakaiproject.sms.model.smpp.SmsPatternSearchResult;
import org.sakaiproject.sms.tool.params.SmsParams;
import org.sakaiproject.sms.tool.renderers.UserNavBarRenderer;
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
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
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
		
		//Top links
		userNavBarRenderer.makeNavBar(tofill, "navIntraTool:", VIEW_ID);
		
		//Render console summary
		if ( smsAccount != null){
			if(! "".equals(smsAccount.getCredits()) && smsAccount.getCredits() != 0 ){
			UIOutput.make(tofill, "send");
			UIInternalLink.make(tofill, "send-link", UIMessage.make("ui.create.sms.header"), new SmsParams(SendSMSProducer.VIEW_ID, null, StatusUtils.statusType_NEW));
			UIMessage.make(tofill, "console-credits", "ui.console.credits.available", new Object[] {smsAccount.getCredits().toString()});
			UIMessage.make(tofill, "console-value", "ui.console.value", new Object[] {smsAccount.getCredits().toString()}); //TODO: How to calculate value of credits
		}else{
			UIMessage.make(tofill, "console-credits", "ui.console.credits.none");
		}
		UIMessage.make(tofill, "console-purchase", "ui.console.help");
		UIOutput.make(tofill, "console-email"); //TODO show email for credit purchases
		
		if ( smsTasks.size() > 0 ){
			
			
			UIOutput.make(tofill, "tasks-table");
			fillTableHeaders( tofill, new String[] {"message", "status", "author", "time", "recipients", "cost"});

			//show table rows
			for (SmsTask sms : smsTasks){
				UIBranchContainer row = UIBranchContainer.make(tofill, "task-row:");
				String status = sms.getStatusCode();
				String detailView = statusUtils.getStatusProducer(status);
				SmsParams statusParams = new SmsParams();
				
				//Fix additional string in params. Used by the {@link ProgressSmsDetailProducer} to show either inprogress or scheduled task
				if ("inprogress".equals(detailView)){
					statusParams.viewID = ProgressSmsDetailProducer.VIEW_ID;
					statusParams.status = "inprogress";
				}else if ("scheduled".equals(detailView)){
					statusParams.viewID = ProgressSmsDetailProducer.VIEW_ID;
					statusParams.status = "scheduled";
				}else{
					statusParams.viewID = detailView;
					statusParams.status = "normal";
				}
				
				statusParams.setId(sms.getId() + "");
				UIInternalLink.make(row, "task-message", sms.getMessageBody(), statusParams);
				UILink statusIcon = UILink.make(row, "task-status", statusUtils.getStatusIcon(status));
				statusIcon.decorate(new UIAlternativeTextDecorator(statusUtils.getStatusFullName(status))); 
				statusIcon.decorate(new UITooltipDecorator(statusUtils.getStatusFullName(status))); 
				UIOutput.make(row, "task-author", sms.getSenderUserName());
				UIOutput.make(row, "task-time", dateUtil.formatDate(sms.getDateToSend()));
				UIMessage.make(row, "task-recipients", "ui.task.recipents", new Object[] {sms.getMessagesDelivered(), sms.getGroupSizeEstimate()}); //TODO Verify that these sms variables give what's expected
				UIOutput.make(row, "task-cost", sms.getCostEstimate() + "");				
			}
		}
		}else
		{
			UIMessage.make(tofill, "tasks-none", "ui.error.notasks");
		}		
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

