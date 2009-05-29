package org.sakaiproject.sms.tool.renderers;

import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.tool.producers.SmsPermissions;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class UserNavBarRenderer {
	
	private ExternalLogic externalLogic;
	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}
	
	/**
	 * Renderer for the nav bar. If @param currentUserId AND @param currentSiteId are null or invalid, nav bar will not render the permissions link
	 * @param tofill
	 * @param divID
	 * @param currentViewID
	 * @param currentUserId Optional param, can be null
	 * @param currentSiteId Optional param, can be null
	 */
	public void makeNavBar(UIContainer tofill, String divID,
			String currentViewID, String currentUserId, String currentSiteId) {
		if ( (currentSiteId != null && currentUserId !=null ) && externalLogic.isUserAllowedInLocation(currentUserId, ExternalLogic.SITE_UPDATE, currentSiteId )){
		UIJointContainer joint = new UIJointContainer(tofill, divID,
				"sms-navigation:");
		
		renderBranch(joint, "1", currentViewID, SmsPermissions.VIEW_ID,
				"sms.navbar.permissions", false);
		}
	}
	
	private void renderBranch(UIJointContainer joint, String id,
			String currentViewID, String linkViewID, String message,
			boolean renderSeperator) {

		UIBranchContainer cell = UIBranchContainer.make(joint,
				"navigation-cell:", id);

		if (currentViewID != null && currentViewID.equals(linkViewID)) {
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
