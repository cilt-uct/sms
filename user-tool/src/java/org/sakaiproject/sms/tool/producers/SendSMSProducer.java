package org.sakaiproject.sms.tool.producers;

import java.util.Calendar;
import java.util.Date;

import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.hibernate.HibernateLogicLocator;
import org.sakaiproject.sms.logic.hibernate.SmsAccountLogic;
import org.sakaiproject.sms.logic.hibernate.SmsTaskLogic;
import org.sakaiproject.sms.model.hibernate.SmsAccount;
import org.sakaiproject.sms.model.hibernate.SmsConfig;
import org.sakaiproject.sms.model.hibernate.SmsTask;
import org.sakaiproject.sms.tool.params.SmsParams;
import org.sakaiproject.sms.tool.renderers.UserNavBarRenderer;
import org.sakaiproject.sms.tool.util.CurrencyUtil;
import org.sakaiproject.sms.tool.util.StatusUtils;

import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBoundString;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UIIDStrategyDecorator;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.evolvers.FormatAwareDateInputEvolver;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

public class SendSMSProducer implements ViewComponentProducer, ViewParamsReporter {
	
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
	
	private UserNavBarRenderer userNavBarRenderer;
	public void setUserNavBarRenderer(UserNavBarRenderer userNavBarRenderer) {
		this.userNavBarRenderer = userNavBarRenderer;
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

	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {
		
		//view variables
		String currentSiteId = externalLogic.getCurrentSiteId();
		String currentUserId = externalLogic.getCurrentUserId();
		SmsAccount smsAccount = smsAccountLogic.getSmsAccount(currentSiteId, currentUserId);
		SmsParams smsParams = (SmsParams) viewparams;
		SmsTask smsTask = new SmsTask();
		if ( smsParams.id != null && ! "".equals(smsParams.id) ){
			smsTask = smsTaskLogic.getSmsTask(Long.parseLong(smsParams.id));
		}
		boolean hasAccount = smsAccount != null;
		boolean hasAccountEnabled = Boolean.FALSE;
		if ( hasAccount ){
			hasAccountEnabled = smsAccount.getAccountEnabled().booleanValue();
		}
		boolean hasCredits = hasAccount && smsAccount.getCredits() != 0;
		boolean isEditing = StatusUtils.statusType_EDIT.equals(smsParams.status);
		
		if (! hasAccount && ! isEditing ){
			UIMessage.make(tofill, "error-account", "ui.error.no.account");
			renderFooter(tofill);
		}else if(! hasAccountEnabled  && ! isEditing ){
			UIMessage.make(tofill, "error-account-disabled", "ui.error.bisabled.account");
			renderFooter(tofill);
		}else{
			if ( hasCredits  || isEditing){
			
			if ( smsParams.id != null && ! "".equals(smsParams.id) ){
				smsTask = smsTaskLogic.getSmsTask(Long.parseLong(smsParams.id));
				//Top links
				userNavBarRenderer.makeNavBar(tofill, "navIntraTool:", null);	
				UIMessage.make(tofill, "sms-header", "ui.edit.sms.header");
			}else{
				//Top links
				userNavBarRenderer.makeNavBar(tofill, "navIntraTool:", VIEW_ID);
				UIMessage.make(tofill, "sms-header", "ui.create.sms.header");
			}
			
			UIForm form = UIForm.make(tofill, "form");
			
			//textarea
			UIInput messageBody = UIInput.make(form, "form-box", null, smsTask.getId() == null ? null : smsTask.getMessageBody());
			messageBody.decorate(new UIIDStrategyDecorator("messageBody"));
			messageBody.decorate(new UIFreeAttributeDecorator("name", "messageBody"));
			
			if (smsTask.getId() == null){
				UIInternalLink.make(form, "form-add-recipients", UIMessage.make("ui.send.message.add"),
					new SmsParams(ChooseRecipientsProducer.VIEW_ID))
					.decorate(new UIIDStrategyDecorator("smsAddRecipients"));
			}else{
				UIInternalLink.make(form, "form-add-recipients", UIMessage.make("ui.send.message.edit"),
						new SmsParams(ChooseRecipientsProducer.VIEW_ID, smsTask.getId() + ""))
					.decorate(new UIIDStrategyDecorator("smsAddRecipients"));	
			}
			
			if ( hasAccount ){
				//Render billing report
				UIOutput.make(tofill, "console-selected", ( smsTask.getGroupSizeEstimate() == null )? 0 + "" : smsTask.getGroupSizeEstimate() + "");
				UIOutput.make(tofill, "console-credits", ( smsTask.getCreditEstimate() == null )? 0 + "" : smsTask.getCreditEstimate() + "");
				UIOutput.make(tofill, "console-cost", ( smsTask.getCostEstimate() == null )? currencyUtil.toServerLocale(0) + "" : currencyUtil.toServerLocale((smsTask.getCostEstimate())) );
			}
			
			dateEvolver.setStyle(FormatAwareDateInputEvolver.DATE_TIME_INPUT);

			UIInput scheduleDate = UIInput.make(form, "smsDatesScheduleDate:", "dummyBean.smsDatesScheduleDate" ); 

			if (smsTask.getDateToSend() == null){
				UIBoundBoolean boolSchedule = UIBoundBoolean.make(form, "booleanSchedule", Boolean.FALSE);
				UIMessage.make(form, "booleanSchedule-label", "ui.send.date.schedule")
					.decorate(new UILabelTargetDecorator(boolSchedule));
				dateEvolver.evolveDateInput(scheduleDate, new Date());
			}else{
				UIBoundBoolean boolSchedule = UIBoundBoolean.make(form, "booleanSchedule", Boolean.TRUE);
				UIMessage.make(form, "booleanSchedule-label", "ui.send.date.schedule")
					.decorate(new UILabelTargetDecorator(boolSchedule));
				dateEvolver.evolveDateInput(scheduleDate, smsTask.getDateToSend());
			}
			

			UIInput expireDate = UIInput.make(form, "smsDatesExpiryDate:", "dummyBean.smsDatesScheduleDate" ); 
			if (smsTask.getDateToExpire() == null){
				UIBoundBoolean boolSchedule = UIBoundBoolean.make(form, "booleanExpiry", Boolean.FALSE);
				UIMessage.make(form, "booleanExpiry-label", "ui.send.date.expiry")
					.decorate(new UILabelTargetDecorator(boolSchedule));
				//Set default expiry time
				SmsConfig siteConfig = hibernateLogicLocator.getSmsConfigLogic()
					.getOrCreateSystemSmsConfig();
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date());
				cal.add(Calendar.SECOND, siteConfig.getSmsTaskMaxLifeTime());
				dateEvolver.evolveDateInput(expireDate, cal.getTime());
			}else{
				UIBoundBoolean boolExpiry = UIBoundBoolean.make(form, "booleanExpiry", Boolean.TRUE);
				UIMessage.make(form, "booleanExpiry-label", "ui.send.date.expiry")
					.decorate(new UILabelTargetDecorator(boolExpiry));
				dateEvolver.evolveDateInput(expireDate, smsTask.getDateToExpire());
			}
			
			if ( smsTask.getId() != null ){
				if( smsTask.getDeliveryEntityList() != null){
					UIInput.make(tofill, "taskdeliveryEntityList", null, toJSONarray(smsTask.getDeliveryEntityList().toArray(new String[] {}))) //turn entity list into a JS Array object
					.decorate(new UIIDStrategyDecorator("taskdeliveryEntityList"));
				}
				if( smsTask.getSakaiUserIds() != null ){
					UIInput.make(tofill, "taskuserIds", null, toJSONarray(smsTask.getSakaiUserIds().toArray(new String[] {}))) //turn user ids into a JS Array object
					.decorate(new UIIDStrategyDecorator("taskuserIds"));
				}
				if( smsTask.getDeliveryMobileNumbersSet() != null ){
					UIInput.make(tofill, "taskdeliveryMobileNumbersSet", null, toJSONarray(smsTask.getDeliveryMobileNumbersSet().toArray(new String[] {})))//turn DeliveryMobileNumbersSet into a JS Array object
					.decorate(new UIIDStrategyDecorator("taskdeliveryMobileNumbersSet"));
				}
				UIInput smsId = UIInput.make(form, "id", null, smsTask.getId() + "");
				smsId.fossilize = false;
				smsId.decorate(new UIIDStrategyDecorator("smsId"));
				
				UIInput.make(form, "taskcopyMe", null, smsTask.getSakaiUserIds().contains(currentUserId) ? Boolean.toString(Boolean.TRUE) : Boolean.toString(Boolean.FALSE))
						.fossilize = false;
			}
			
			UIInput statusType = UIInput.make(form, "statusType", null, smsParams.status == null ? StatusUtils.statusType_NEW : smsParams.status);
			statusType.fossilize = false;
			statusType.decorate(new UIIDStrategyDecorator("statusType"));
			UIInput.make(tofill, "sakaiSiteId", null, currentSiteId)
				.fossilize = false;
			UIInput.make(tofill, "senderUserName", null, externalLogic.getSakaiUserDisplayName(currentUserId))
				.fossilize = false;
			UIInput.make(tofill, "senderUserId", null, currentUserId)  
				.fossilize = false;
			UICommand.make(form, "form-send", getCommandText(smsParams.status), null)
				.decorate(new UIIDStrategyDecorator("smsSend"));
			UIInternalLink.make(form, "goto-home", new SmsParams( MainProducer.VIEW_ID ));
			UICommand.make(form, "back", UIMessage.make("sms.general.cancel"));
		
			}else{
				UIMessage.make(tofill, "error", "ui.error.cannot.create");
				renderFooter(tofill);
			}
		}	
		
	}
	
	private void renderFooter(UIContainer tofill) {
		UICommand.make(tofill, "back2", UIMessage.make("sms.general.back"));
		UIMessage.make(tofill, "error-help", "ui.console.help");
		//IOutput.make(tofill, "error-email"); //TODO show email for credit purchases
	}

	private String toJSONarray(String[] entities) {
		if ( entities != null ){
			String jsonList = "";
			StringBuilder sb = new StringBuilder(jsonList);
			int count = 1;
			for (String entity : entities){
				sb.append(entity);
				//jsonList += entity;
				if ( count == entities.length -1 ){
					sb.append(",");
				}
				count++;
			}
			return sb.toString();
		}
		return null;
	}
	
	private UIBoundString getCommandText( String statusType ){
		if (StatusUtils.statusType_NEW.equals(statusType)){
			return UIMessage.make("sms.general.send");
		}else if (StatusUtils.statusType_EDIT.equals(statusType)){
			return UIMessage.make( "ui.send.save" );
		}else if (StatusUtils.statusType_REUSE.equals(statusType)){
			return UIMessage.make( "ui.send.reused" );
		}else{
			return null;
		}
	}

	public ViewParameters getViewParameters() {
		// TODO Auto-generated method stub
		return new SmsParams();
	}
	
}

