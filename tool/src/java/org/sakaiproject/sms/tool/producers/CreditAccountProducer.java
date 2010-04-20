package org.sakaiproject.sms.tool.producers;

import org.sakaiproject.sms.logic.SmsAccountLogic;
import org.sakaiproject.sms.model.SmsAccount;
import org.sakaiproject.sms.tool.params.IdParams;
import org.sakaiproject.sms.tool.renderers.NavBarRenderer;
import org.sakaiproject.sms.tool.util.MessageFixupHelper;

import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

public class CreditAccountProducer implements ViewComponentProducer, ViewParamsReporter {

	public static final String VIEW_ID = "credit_account";

	private MessageFixupHelper messageFixupHelper;
	private NavBarRenderer navBarRenderer;

	public void setMessageFixupHelper(MessageFixupHelper messageFixupHelper) {
		this.messageFixupHelper = messageFixupHelper;
	}

	public void setNavBarRenderer(NavBarRenderer navBarRenderer) {
		this.navBarRenderer = navBarRenderer;
	}

	public void init() {
		messageFixupHelper.fixupMessages("account-id-input",
		"account-amount-input");
	}

	public String getViewID() {
		return VIEW_ID;
	}

	private SmsAccountLogic smsAccountLogic;	
	public void setSmsAccountLogic(SmsAccountLogic smsAccountLogic) {
		this.smsAccountLogic = smsAccountLogic;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {

		init();
		SmsAccount account = null;
		IdParams idp = (IdParams)viewparams;
		String id = idp.id;
		if (id != null) {
			account = smsAccountLogic.getSmsAccount(Long.valueOf(id));
		}
			
		
		navBarRenderer.makeNavBar(tofill, "navIntraTool:", VIEW_ID);

		UIMessage.make(tofill, "page-title", "sms.credit.account.title");
		UIMessage.make(tofill, "sms-credit-account-heading","sms.credit.account.title");

		UIForm form = UIForm.make(tofill, "credit-account-form");
		
		
		if(account == null) {
			UIMessage.make(form, "account-id-label", "sms.credit.account.id");
			UIInput.make(form, "account-id-input", "#{creditAccountBean.accountId}");
		} else {
			form.addParameter(new UIELBinding("#{creditAccountBean.accountId}", 	account.getId().toString()));
		}

		UIMessage.make(form, "account-amount-label", "sms.credit.account.amount");
		UIInput.make(form, "account-amount-input","#{creditAccountBean.creditsToCredit}");

		UIMessage.make(form, "account-description-label", "sms.credit.account.description");
		UIInput.make(form, "account-description-input","#{creditAccountBean.description}");

		UICommand.make(form, "save-btn","#{creditAccountActionBean.creditAccount}");
	}

	public ViewParameters getViewParameters() {
		return new IdParams();
	}
}