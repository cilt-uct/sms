package org.sakaiproject.sms.tool.producers;

import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.hibernate.SmsAccountLogic;
import org.sakaiproject.sms.model.hibernate.SmsAccount;

import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class SendSMSProducer implements ViewComponentProducer {
	
	public static final String VIEW_ID = "create-sms";
	
	public String getViewID() {
		return VIEW_ID;
	}
	
	private ExternalLogic externalLogic;
	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	private SmsAccountLogic smsAccountLogic;
	public void SmsAccountLogic(SmsAccountLogic smsAccountLogic) {
		this.smsAccountLogic = smsAccountLogic;
	}
	
	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {
		
		//view variables
		String currentSiteId = externalLogic.getCurrentSiteId();
		String currentUserId = externalLogic.getCurrentUserId();
		SmsAccount smsAccount = smsAccountLogic.getSmsAccount(currentSiteId, currentUserId);
		
		//Top links
		if (externalLogic.isUserAdmin(currentUserId)){
			UIInternalLink.make(tofill, "link-admin", UIMessage.make("sms.navbar.system-config"), new SimpleViewParameters(TaskListProducer.VIEW_ID));
		}
		
		//Check for credits
		if ( smsAccount.getCredits() == null){
			
			String sendOTP = "#{sendSmsBean.";
			
			UIForm form = UIForm.make(tofill, "form");
			
			//textarea
			UIInput.make(form, "form-box", sendOTP + "messageBody}");
			
			UIInternalLink.make(form, "form-add-recipients", UIMessage.make(""), new SimpleViewParameters(SendSMSProducer.VIEW_ID)); //TODO link to choose-recip. view
			
			//mini report console
			UIMessage.make(tofill, "console-credits", "ui.console.credits.available", new Object[] {smsAccount.getCredits().toString()});
			UIMessage.make(tofill, "console-credits", "ui.console.value", new Object[] {smsAccount.getCredits().toString()}); //TODO: How to calculate value of credits
			UIMessage.make(tofill, "console-help", "ui.console.help");
			UIOutput.make(tofill, "console-email"); //TODO show email for credit purchases
			
			//TODO Add dateTime pickers
			
			//notify me checkbox
			UIBoundBoolean notify = UIBoundBoolean.make(form, "form-notify", sendOTP + "notifyMe}");
			UIMessage.make(form, "form-notify-target", "ui.send.notify")
				.decorate(new UILabelTargetDecorator(notify));
			
			UICommand.make(form, "form-send", UIMessage.make("sms.general.send"), sendOTP + "saveTask}");
			
		}else{
			
			UIMessage.make(tofill, "error", "ui.error.cannot.create");
			UIInternalLink.make(tofill, "error-back", UIMessage.make("sms.general.back"), new SimpleViewParameters(MainProducer.VIEW_ID));
			UIMessage.make(tofill, "error-help", "ui.console.help");
			UIOutput.make(tofill, "error-email"); //TODO show email for credit purchases
		}
		
		
		
	}
	
}

