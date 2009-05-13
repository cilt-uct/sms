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
	 * @param b 
	 */
	public void makeNavBar(UIContainer tofill, String divID,
			String currentViewID) {
		// TODO: Phase 2 may have permissions setting that can be rendered here	
	}
	public void makeNavBar(UIContainer tofill, String divID,
			String currentViewID, boolean hasCredits) {
	}
}
