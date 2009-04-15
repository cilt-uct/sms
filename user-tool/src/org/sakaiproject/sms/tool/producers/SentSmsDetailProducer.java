package org.sakaiproject.sms.tool.producers;

import java.util.Set;

import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.hibernate.SmsTaskLogic;
import org.sakaiproject.sms.model.hibernate.SmsMessage;
import org.sakaiproject.sms.model.hibernate.SmsTask;
import org.sakaiproject.sms.tool.params.SmsStatusParams;
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

public class SentSmsDetailProducer implements ViewComponentProducer {
	
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
	public void setStatusIcons(StatusUtils statusIcons) {
		this.statusUtils = statusIcons;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {
		
		if ( viewparams != null ){
			SmsStatusParams statusParams = (SmsStatusParams) viewparams;
			if ( statusParams != null && statusParams.id != null){
				
				Long smsId = Long.parseLong(statusParams.id);
				SmsTask smsTask = smsTaskLogic.getSmsTask(smsId);
		
				//Top links
				userNavBarRenderer.makeNavBar(tofill, "navIntraTool:", VIEW_ID);
				
				UIMessage.make(tofill, "message-title", "ui.sent.sms.title");
				UIOutput.make(tofill, "message", smsTask.getMessageBody());
				UIMessage.make(tofill, "sms-id", "ui.sent.sms.id", new Object[] { smsId });
				UIMessage.make(tofill, "sms-created", "ui.sent.sms.created", new Object[] { dateUtil.formatDate(smsTask.getDateCreated()) });
				
				UIBranchContainer status = UIBranchContainer.make(tofill, "status:");
				String statusCode = smsTask.getStatusCode();
				UILink.make(status, "sms-status", statusUtils.getStatusIcon(statusCode))
					.decorate(new UIAlternativeTextDecorator(statusUtils.getStatusFullName(statusCode)));
				UIOutput.make(status, "sms-status-title", statusUtils.getStatusFullName(statusCode));
				
				UIMessage.make(tofill, "sms-sent", "ui.sent.sms.completed", new Object[] { dateUtil.formatDate(smsTask.getDateProcessed()) });
				UIMessage.make(tofill, "recipients", "ui.sent.sms.recipients", new Object[] { smsTask.getMessagesDelivered(), smsTask.getGroupSizeActual() });
				
				UILink.make(tofill, "recipient-header", UIMessage.make("ui.sent.sms.header.recipients"), "#");
				UILink.make(tofill, "status-header", UIMessage.make("ui.sent.sms.header.status"), "#");
				
				Set<SmsMessage> smses = smsTask.getSmsMessages();
				for (SmsMessage sms : smses){
					UIBranchContainer row = UIBranchContainer.make(tofill, "sms-row:");
					UIOutput.make(row, "sms-recipient", externalLogic.getSakaiUserSortName(sms.getSakaiUserId()));
					String userStatusCode = sms.getStatusCode();
					UILink.make(status, "sms-recipient-status", statusUtils.getStatusIcon(userStatusCode))
						.decorate(new UIAlternativeTextDecorator(statusUtils.getStatusFullName(userStatusCode)));
				}
				
				UICommand.make(tofill, "back-button", UIMessage.make("sms.general.back"));
				
			}else{
				//TODO: show error message since sms.id() is not specified
			}
		}
		
	
	}

}

