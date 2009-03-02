package org.sakaiproject.sms.logic.stubs;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.logic.incoming.IncomingSmsLogic;
import org.sakaiproject.sms.logic.incoming.SmsIncomingLogicManager;

public class TestIncomingSmsLogic implements IncomingSmsLogic {

	private String[] commandKeys = new String[]{"CREATE", "UPDATE", "DELETE"};
	
	private static Log log = LogFactory.getLog(TestIncomingSmsLogic.class);
	
	private String lastExecuted;
	private SmsIncomingLogicManager incomingLogicManager;	
	
	public void setIncomingLogicManager(SmsIncomingLogicManager incomingLogicManager) {
		this.incomingLogicManager = incomingLogicManager;
	}
	
	public void init() {
		incomingLogicManager.register("stub", this);
	}
	
	public String execute(String command, String siteId, String userId,
			String body) {
		lastExecuted = command;
		log.debug(command + " command called with parameters: (" + siteId + ", " + userId + ", " + body + ")");
		return "Stub called, received command " + command + " for site " + siteId;
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

	public Map<String, String> getAliases() {
		Map<String, String> aliasMap = new HashMap<String, String>();
		aliasMap.put("C", "CREATE");
		aliasMap.put("U", "UPDATE");
		aliasMap.put("D", "DELETE");
		return aliasMap;
	}
}
