package org.sakaiproject.sms.tool.producers;

import java.util.List;

import org.sakaiproject.sms.logic.hibernate.SmsTaskLogic;
import org.sakaiproject.sms.model.hibernate.SmsTask;
import org.sakaiproject.sms.tool.params.SmsParams;
import org.sakaiproject.sms.tool.renderers.UserNavBarRenderer;
import org.sakaiproject.sms.tool.util.DateUtil;
import org.sakaiproject.sms.tool.util.StatusUtils;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIAlternativeTextDecorator;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UIIDStrategyDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

public class ProgressSmsDetailProducer implements ViewComponentProducer, ViewParamsReporter {
	
	public static final String VIEW_ID = "inprogress-sms-detail";
	public static final String const_Inprogress = "inprogress";
	public static final String const_Scheduled = "scheduled";
	
	public String getViewID() {
		return VIEW_ID;
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
	public void setDateFormat(DateUtil dateFormat) {
		this.dateUtil = dateFormat;
	}
	
	private StatusUtils statusUtils;
	public void setStatusUtils(StatusUtils statusUtils) {
		this.statusUtils = statusUtils;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {
		
		if ( viewparams != null ){
			SmsParams statusParams = (SmsParams) viewparams;
			if ( statusParams != null && statusParams.id != null && statusParams.status != null ){
				
				Long smsId = Long.parseLong(statusParams.id);
				SmsTask smsTask = smsTaskLogic.getSmsTask(smsId);
				String statusToShow = statusParams.status;
		
				//Top links
				userNavBarRenderer.makeNavBar(tofill, "navIntraTool:", VIEW_ID);
				
				UIMessage.make(tofill, "message-title", "ui.sent.sms.title");
				UIOutput.make(tofill, "message", smsTask.getMessageBody())
				//keep the id somewhere that the JS can grab and use it for processing actions edit or delete
					.decorate(new UIFreeAttributeDecorator("rel", smsTask.getId().toString()));
				UIMessage.make(tofill, "sms-id", "ui.sent.sms.id", new Object[] { smsId });
				UIMessage.make(tofill, "sms-created", "ui.sent.sms.created", new Object[] { dateUtil.formatDate(smsTask.getDateCreated()) });
				
				UIBranchContainer status = UIBranchContainer.make(tofill, "status:");
				String statusCode = smsTask.getStatusCode();
				UILink.make(status, "sms-status", statusUtils.getStatusIcon(statusCode))
					.decorate(new UIAlternativeTextDecorator(statusUtils.getStatusFullName(statusCode)));
				UIOutput.make(status, "sms-status-title", statusUtils.getStatusFullName(statusCode));
				
				//Insert original user selections
				List<String> smsEntities = smsTask.getDeliveryEntityList();
				for ( String entity : smsEntities){
					UIBranchContainer list = UIBranchContainer.make(tofill, "selections-row:");
					UIOutput.make(list, "selections", entity); //TODO fix this & make it work
				}	
				
				UIMessage.make(tofill, "cost", "ui.inprogress.sms.cost");
				UIMessage.make(tofill, "cost-credits", "ui.inprogress.sms.cost", new Object[] { smsTask.getCreditEstimate() });
				UIMessage.make(tofill, "cost-cost", "ui.inprogress.sms.cost", new Object[] { smsTask.getCostEstimate() });
				
				UIForm form = UIForm.make(tofill, "form");
				/**
				 * The action buttons are handled by JS. RSF is only needed for i18N
				 */
				if ( const_Inprogress.equals(statusToShow)){
					UIMessage.make(tofill, "sms-sent", "ui.inprogress.sms.started", new Object[] { dateUtil.formatDate(smsTask.getDateProcessed()) });
					UIMessage.make(tofill, "delivered", "ui.inprogress.sms.delivered", new Object[] { smsTask.getMessagesProcessed(), smsTask.getGroupSizeActual() });
					UICommand.make(form, "stop", UIMessage.make("sms.general.stop"))
						.decorate(new UIIDStrategyDecorator("smsStop"));
				}else if( const_Scheduled.equals(statusToShow)){
					UIMessage.make(tofill, "sms-sent", "ui.scheduled.sms.started", new Object[] { dateUtil.formatDate(smsTask.getDateProcessed()) });
					UICommand.make(form, "edit", UIMessage.make("sms.general.edit.sms"))
						.decorate(new UIIDStrategyDecorator("smsEdit"));
					UICommand.make(form, "delete", UIMessage.make("sms.general.delete"))
						.decorate(new UIIDStrategyDecorator("smsDelete"));
				}else{
					throw new IllegalArgumentException("Cannot act on this status type: " + statusToShow);
				}
				UIMessage.make(tofill, "sms-sent", "ui.inprogress.sms.finish", new Object[] { dateUtil.formatDate(smsTask.getDateProcessed()) });
				
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

