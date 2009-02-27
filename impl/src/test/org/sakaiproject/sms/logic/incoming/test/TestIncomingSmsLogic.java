package org.sakaiproject.sms.logic.incoming.test;

import org.sakaiproject.sms.logic.incoming.IncomingSmsLogic;

public class TestIncomingSmsLogic implements IncomingSmsLogic {

	private String[] commandKeys = new String[]{"CREATE", "UPDATE", "DELETE"};
	
	private String lastExecuted;
	
	public String execute(String command, String siteId, String userId,
			String body) {
		return command;
	}

	public String[] getCommandKeys() {
		return commandKeys;
	}
	
	public void setCommandKeys(String[] commandKeys) {
		this.commandKeys = commandKeys;
	}
	
	public String getLastExecuted() {
		return lastExecuted;
	}
}
