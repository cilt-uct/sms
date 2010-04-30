package org.sakaiproject.sms.tool.producers;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.logic.incoming.ShortMessageCommand;
import org.sakaiproject.sms.logic.incoming.SmsIncomingLogicManager;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class RegisteredCommandProducer implements ViewComponentProducer {

	public static final String VIEW_ID="registeredCommands";
	public String getViewID() {
		return VIEW_ID;
	}
	
	private static final Log log = LogFactory.getLog(RegisteredCommandProducer.class);

	private SmsIncomingLogicManager smsIncomingLogicManager;
	public void setSmsIncomingLogicManager(
			SmsIncomingLogicManager smsIncomingLogicManager) {
		this.smsIncomingLogicManager = smsIncomingLogicManager;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {
		
		List<ShortMessageCommand> commands = smsIncomingLogicManager.getAllCommands();
		for(int i =0; i < commands.size(); i++) {
			ShortMessageCommand smc = commands.get(i);
			if (smc != null) {
				log.info(smc.getCommandKey());
				UIBranchContainer row = UIBranchContainer.make(tofill, "commands:");
				UIOutput.make(row, "command", smc.getCommandKey());
				UIOutput.make(row, "help", smc.getHelpMessage(null));
				String visible = "reg.false";
				if (smc.isEnabled()) {
					visible = "reg.true";
				}
				
				UIMessage.make(row, "isVisible", visible);
				
				String enabled = "reg.false";
				if (smc.isEnabled()) {
					enabled = "reg.true";
				}
				UIMessage.make(row, "isEnabled", enabled);
			}
		}

	}

	
	
}
