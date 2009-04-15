package org.sakaiproject.sms.tool.producers;

import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.hibernate.SmsAccountLogic;
import org.sakaiproject.sms.model.hibernate.SmsAccount;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIIDStrategyDecorator;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class ChooseRecipientsProducer implements ViewComponentProducer {
	
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
	
	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {
		
		//view variables
		String currentSiteId = externalLogic.getCurrentSiteId();
		String currentUserId = externalLogic.getCurrentUserId();
		SmsAccount smsAccount = smsAccountLogic.getSmsAccount(currentSiteId, currentUserId);
		
		
		
		//Check for credits
		if ( smsAccount.getCredits() == null){
			
			String chooseRecipientsOTP = "#{sendSmsBean.";
			
			//Filling tab areas on condition
			fillTabs( tofill, new String[] {"roles", "groups", "names", "numbers"});
			
			UIForm form = UIForm.make(tofill, "form");
			
			UIInput.make(form, "names-box", chooseRecipientsOTP + "selectedNames}");
			
			//copy me checkbox
			UIBoundBoolean copy = UIBoundBoolean.make(form, "copy-me", chooseRecipientsOTP + "notifyMe}");
			UIMessage.make(form, "copy-me-label", "ui.recipients.choose.copy")
				.decorate(new UILabelTargetDecorator(copy));
			UICommand.make(form, "continue", UIMessage.make("ui.recipients.choose.continue"), chooseRecipientsOTP + "saveRecipients}")
				.decorate(new UIIDStrategyDecorator("recipientsCmd"));
			
		}else{
			UIMessage.make(tofill, "error", "ui.error.cannot.create");
			UICommand.make(tofill, "error-back", UIMessage.make("sms.general.cancel"));
			UIMessage.make(tofill, "error-help", "ui.console.help");
			UIOutput.make(tofill, "error-email"); //TODO show email for credit purchases
		}
		
	}

	private void fillTabs(UIContainer tofill, String[] tabs) {
		for ( int i=0; i < tabs.length; i++){
			String tab = tabs[i];
			UIBranchContainer branch = UIBranchContainer.make(tofill, tab + ":");
			UILink.make(branch, tab + "-title", messageLocator.getMessage("ui.recipients.choose.prefix") + messageLocator.getMessage("ui.recipients.choose." + tab + ".title"), null); //TODO check that this preserves the HTML defined href
			UIOutput.make(branch, tab + "-selected", 0 + "")
				.decorate(new UITooltipDecorator(UIMessage.make("ui.recipients.choose.selected.tooltip", new Object[] { messageLocator.getMessage("ui.recipients.choose." + tab + ".title") })));
		}
	}
	
}

