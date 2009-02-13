package org.sakaiproject.sms.tool.producers;

import org.sakaiproject.sms.tool.renderers.NavBarRenderer;
import org.sakaiproject.sms.tool.util.MessageFixupHelper;

import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class CreditAccountProducer implements ViewComponentProducer {

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

	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {

		init();
		navBarRenderer.makeNavBar(tofill, "navIntraTool:", VIEW_ID);

		UIMessage.make(tofill, "page-title", "sms.credit.account.title");
		UIMessage.make(tofill, "sms-credit-account-heading",
				"sms.credit.account.title");

		UIForm form = UIForm.make(tofill, "credit-account-form");

		UIMessage.make(form, "account-id-label", "sms.credit.account.id");
		UIInput.make(form, "account-id-input", "#{creditAccountBean.accountId}");

		UIMessage
				.make(form, "account-amount-label", "sms.credit.account.amount");

		UIInput.make(form, "account-amount-input",
				"#{creditAccountBean.creditsToCredit}");
		UICommand.make(form, "save-btn",
				"#{creditAccountActionBean.creditAccount}");
	}
}