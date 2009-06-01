package org.sakaiproject.sms.tool.producers;

import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.hibernate.SmsTaskLogic;
import org.sakaiproject.sms.model.hibernate.SmsMessage;
import org.sakaiproject.sms.model.hibernate.SmsTask;
import org.sakaiproject.sms.tool.params.SmsParams;
import org.sakaiproject.sms.tool.renderers.SmsMessageRenderer;
import org.sakaiproject.sms.tool.renderers.UserNavBarRenderer;
import org.sakaiproject.sms.tool.util.DateUtil;
import org.sakaiproject.sms.tool.util.StatusUtils;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIAlternativeTextDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

public class SentSmsDetailProducer implements ViewComponentProducer, ViewParamsReporter {
	
	public static Log log = LogFactory.getLog(SentSmsDetailProducer.class);
	
	public static final String VIEW_ID = "sent-sms-detail";
	
	public String getViewID() {
		return VIEW_ID;
	}
	
	private ExternalLogic externalLogic;
	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	private SmsTaskLogic smsTaskLogic;
	public void setSmsTaskLogic(SmsTaskLogic smsTaskLogic) {
		this.smsTaskLogic = smsTaskLogic;
	}
	
	private UserNavBarRenderer userNavBarRenderer;
	public void setUserNavBarRenderer(UserNavBarRenderer userNavBarRenderer) {
		this.userNavBarRenderer = userNavBarRenderer;
	}
	
	private DateUtil dateUtil;
	public void setDateUtil(DateUtil dateUtil) {
		this.dateUtil = dateUtil;
	}
	
	private StatusUtils statusUtils;
	public void setStatusUtils(StatusUtils statusUtils) {
		this.statusUtils = statusUtils;
	}
	
	private SmsMessageRenderer smsMessageRenderer;
	public void setSmsMessageRenderer(SmsMessageRenderer smsMessageRenderer) {
		this.smsMessageRenderer = smsMessageRenderer;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {
		
		if ( viewparams != null ){
			SmsParams statusParams = (SmsParams) viewparams;
			if ( statusParams != null && statusParams.id != null){
				
				Long smsId = Long.parseLong(statusParams.id);
				SmsTask smsTask = smsTaskLogic.getSmsTask(smsId);
		
				//Top links
				userNavBarRenderer.makeNavBar(tofill, "navIntraTool:", VIEW_ID, null, null);
				
				//Show message
				smsMessageRenderer.renderMessage(smsTask, tofill, "message:");
				
				UIBranchContainer status = UIBranchContainer.make(tofill, "status:");
				String statusCode = smsTask.getStatusCode();
				UILink.make(status, "sms-status", statusUtils.getStatusIcon(statusCode))
					.decorate(new UIAlternativeTextDecorator(statusUtils.getStatusFullName(statusCode)));
				UIOutput.make(status, "sms-status-title", statusUtils.getStatusFullName(statusCode));
				
				UIMessage.make(tofill, "sms-sent", "ui.sent.sms.completed", new Object[] { dateUtil.formatDate(smsTask.getDateProcessed()) });
				UIMessage.make(tofill, "recipients", "ui.sent.sms.recipients", new Object[] { smsTask.getMessagesDelivered(), (smsTask.getGroupSizeActual() == null || smsTask.getGroupSizeActual() == 0) ? smsTask.getGroupSizeEstimate() : smsTask.getGroupSizeActual() });
				
				UIMessage.make(tofill, "recipient-header", "ui.sent.sms.header.recipients");
				UIMessage.make(tofill, "username-header", "ui.sent.sms.header.username");
				UIMessage.make(tofill, "status-header", "ui.sent.sms.header.status");
				
				Set<String> smsUserIds = smsTask.getSakaiUserIds();
				Map<String, String> usernamesMap = externalLogic.getSakaiUsernames(smsUserIds);
				
				Set<SmsMessage> smses = smsTask.getSmsMessages();
				for (SmsMessage sms : smses){
					UIBranchContainer row = UIBranchContainer.make(tofill, "sms-row:");
					String smsUserId = sms.getSakaiUserId();
					if (smsUserId == null || "".equals(smsUserId) ){
						UIOutput.make(row, "sms-recipient", sms.getMobileNumber());
						UIOutput.make(row, "sms-recipient-username", "----");
					}else{
						UIOutput.make(row, "sms-recipient", externalLogic.getSakaiUserSortName(smsUserId));
						UIOutput.make(row, "sms-recipient-username", usernamesMap.get(smsUserId));
					}
					String userStatusCode = sms.getStatusCode();
					UILink.make(row, "sms-recipient-status", statusUtils.getStatusIcon(userStatusCode))
						.decorate(new UIAlternativeTextDecorator(statusUtils.getStatusFullName(userStatusCode)));
				}
				
				UICommand.make(tofill, "back-button", UIMessage.make("sms.general.back"));
				
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

