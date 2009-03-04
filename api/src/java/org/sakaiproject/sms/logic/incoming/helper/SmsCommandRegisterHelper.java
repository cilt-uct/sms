package org.sakaiproject.sms.logic.incoming.helper;

import java.util.List;

import org.sakaiproject.sms.logic.incoming.SmsCommand;
import org.sakaiproject.sms.logic.incoming.SmsIncomingLogicManager;

public class SmsCommandRegisterHelper {
	
	private String toolKey;
	private SmsIncomingLogicManager incomingLogicManager;
	private List<SmsCommand> commands;
	
	public void init() {
		if (incomingLogicManager == null) {
			throw new IllegalStateException("SmsIncomingLogicManager must be set");
		}
		if (toolKey == null) {
			throw new IllegalStateException("ToolKey must be set");
		}
			
		incomingLogicManager.clearCommands(toolKey);
		for (SmsCommand command : commands) {
			incomingLogicManager.register(toolKey, command);
		}
	}
	
	public void setToolKey(String toolKey) {
		this.toolKey = toolKey;
	}
	
	public void setIncomingLogicManager(
			SmsIncomingLogicManager incomingLogicManager) {
		this.incomingLogicManager = incomingLogicManager;
	}
	public void setCommands(List<SmsCommand> commands) {
		this.commands = commands;
	}
	
	
}
