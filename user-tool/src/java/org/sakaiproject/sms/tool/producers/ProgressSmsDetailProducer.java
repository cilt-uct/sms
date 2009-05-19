package org.sakaiproject.sms.tool.producers;

import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.hibernate.HibernateLogicLocator;
import org.sakaiproject.sms.logic.hibernate.SmsTaskLogic;
import org.sakaiproject.sms.model.hibernate.SmsConfig;
import org.sakaiproject.sms.model.hibernate.SmsTask;
import org.sakaiproject.sms.model.hibernate.constants.SmsConst_DeliveryStatus;
import org.sakaiproject.sms.tool.params.SmsParams;
import org.sakaiproject.sms.tool.renderers.SavedSelectionsRenderer;
import org.sakaiproject.sms.tool.renderers.SmsMessageRenderer;
import org.sakaiproject.sms.tool.renderers.UserNavBarRenderer;
import org.sakaiproject.sms.tool.util.DateUtil;
import org.sakaiproject.sms.tool.util.StatusUtils;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIAlternativeTextDecorator;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UIIDStrategyDecorator;
import uk.org.ponder.rsf.request.EarlyRequestParser;
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
	public void setDateUtil(DateUtil dateUtil) {
		this.dateUtil = dateUtil;
	}
	
	private StatusUtils statusUtils;
	public void setStatusUtils(StatusUtils statusUtils) {
		this.statusUtils = statusUtils;
	}
	
	private ExternalLogic externalLogic;
	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}
	
	private HibernateLogicLocator hibernateLogicLocator;
	public void setHibernateLogicLocator(
			HibernateLogicLocator hibernateLogicLocator) {
		this.hibernateLogicLocator = hibernateLogicLocator;
	}
	
	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
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
				
				//Show message
				smsMessageRenderer.renderMessage(smsTask, tofill, "message:");
				
				UIBranchContainer status = UIBranchContainer.make(tofill, "status:");
				String statusCode = smsTask.getStatusCode();
				UILink.make(status, "sms-status", statusUtils.getStatusIcon(statusCode))
					.decorate(new UIAlternativeTextDecorator(statusUtils.getStatusFullName(statusCode)));
				SmsConfig siteConfig = hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSystemSmsConfig();
				UIOutput.make(status, "sms-status-title", statusUtils.getStatusFullName(statusCode));
				
				UIOutput.make(tofill, "sms-status-retries", "Processing attempt number: "+smsTask.getAttemptCount()+" of "+siteConfig.getSmsRetryMaxCount()); //TODO: stick this in message.prop
				
				//Insert original user selections
				savedSelectionsRenderer.renderSelections(smsTask, tofill, "savedSelections:");
				
				UIMessage.make(tofill, "cost", "ui.inprogress.sms.cost.title");
				UIMessage.make(tofill, "cost-credits", "ui.inprogress.sms.credits", new Object[] { smsTask.getCreditEstimate() });
				UIMessage.make(tofill, "cost-cost", "ui.inprogress.sms.cost", new Object[] { smsTask.getCostEstimate() });
				
				UIForm form = UIForm.make(tofill, "form", new SmsParams(SendSMSProducer.VIEW_ID, smsId.toString(), const_Scheduled.equals(statusToShow)? StatusUtils.statusType_EDIT : StatusUtils.statusType_REUSE));
				form.type = EarlyRequestParser.RENDER_REQUEST;
				//keep the id somewhere that the JS can grab and use it for processing actions edit or delete
				UIInput.make(tofill, "smsId", null, smsTask.getId() + "")
				.decorate(new UIIDStrategyDecorator("smsId"));
				/**
				 * The action buttons are handled by JS. RSF is only needed for i18N
				 */
				if ( const_Inprogress.equals(statusToShow)){
					UIMessage.make(tofill, "sms-started", "ui.inprogress.sms.started", new Object[] { dateUtil.formatDate(smsTask.getDateToSend()) });
					UIMessage.make(tofill, "delivered", "ui.inprogress.sms.delivered", new Object[] { smsTask.getMessagesDelivered(), smsTask.getGroupSizeActual() == null ? smsTask.getGroupSizeEstimate() : smsTask.getGroupSizeActual() });
					UICommand.make(form, "stop", UIMessage.make("sms.general.stop"))
						.decorate(new UIIDStrategyDecorator("smsStop"));
					UIMessage.make(tofill, "actionAbort", "ui.action.confirm.sms.abort", new String[] { smsTask.getMessageBody() });
					UIInput.make(form, "abortCode", null, SmsConst_DeliveryStatus.STATUS_ABORT)
						.decorate(new UIIDStrategyDecorator("abortCode"));
					UIInput.make(form, "abortMessage", null, messageLocator.getMessage("ui.task.aborted", new Object[] { externalLogic.getSakaiUserDisplayName(externalLogic.getCurrentUserId()) }) )
						.decorate(new UIIDStrategyDecorator("abortMessage"));
				}else if( const_Scheduled.equals(statusToShow)){
					UIMessage.make(tofill, "sms-started", "ui.scheduled.sms.started", new Object[] { dateUtil.formatDate(smsTask.getDateToSend()) });
					UICommand.make(form, "edit", UIMessage.make("sms.general.edit.sms"))
						.decorate(new UIIDStrategyDecorator("smsEdit"));
					UICommand.make(form, "delete", UIMessage.make("sms.general.delete"))
						.decorate(new UIIDStrategyDecorator("smsDelete"));
					UIMessage.make(tofill, "actionDelete", "ui.action.confirm.sms.delete", new String[] { smsTask.getMessageBody() });
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

