package org.sakaiproject.sms.tool.producers;

import org.sakaiproject.sms.logic.external.ExternalLogic;

import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import org.sakaiproject.sms.tool.renderers.UserNavBarRenderer;

public class CreditTransferProducer implements ViewComponentProducer {
	
	public static final String VIEW_ID = "credit-transfer";

	public String getViewID() {
		return VIEW_ID;
	}
	
	private ExternalLogic externalLogic;
	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
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
		
		//Top links
		userNavBarRenderer.makeNavBar(tofill, "navIntraTool:", VIEW_ID, currentUserId, currentSiteId);
		
		UILink imgFetch = UILink.make(tofill, "imgFetch");
		//imgFetch.decorators( new UIAlternativeTextDecorator("ui.transfer.image.fetch"));
		
		UILink imgWorking = UILink.make(tofill, "imgWorking");
		//imgWorking.decorators( new UIAlternativeTextDecorator("ui.transfer.image.working"));
		
	}

}
