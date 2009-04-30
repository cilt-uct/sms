package org.sakaiproject.sms.tool.renderers;

import org.sakaiproject.sms.tool.producers.MainProducer;
import org.sakaiproject.sms.tool.producers.SendSMSProducer;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class UserNavBarRenderer {
	
	
	/**
	 * Renders navigation bar
	 * 
	 * @param tofill
	 *            {@link UIContainer} to fill
	 * @param divID
	 *            ID of div
	 * @param currentViewID
	 *            View ID currently being viewed
	 */
	public void makeNavBar(UIContainer tofill, String divID,
			String currentViewID) {
		// TODO: Take permissions into account
		UIJointContainer joint = new UIJointContainer(tofill, divID,
				"sms-navigation:");

		renderBranch(joint, "1", currentViewID, MainProducer.VIEW_ID,
				"sms.navbar.messages", true);
		renderBranch(joint, "2", currentViewID, SendSMSProducer.VIEW_ID,
				"sms.navbar.new", false);
	}

	private void renderBranch(UIJointContainer joint, String id,
			String currentViewID, String linkViewID, String message,
			boolean renderSeperator) {

		UIBranchContainer cell = UIBranchContainer.make(joint,
				"navigation-cell:", id);

		if (currentViewID.equals(linkViewID)) {
			UIMessage.make(cell, "item-text", message);
		} else {
			UIInternalLink.make(cell, "item-link", UIMessage.make(message),
					new SimpleViewParameters(linkViewID));
		}

		if (renderSeperator) {
			UIOutput.make(cell, "item-separator");
		}
	}
}
