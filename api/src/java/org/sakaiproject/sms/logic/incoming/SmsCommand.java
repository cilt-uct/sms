package org.sakaiproject.sms.logic.incoming;

public interface SmsCommand {
	
	String getCommandKey();
	
	String[] getAliases();
	
	String execute(String siteId, String userId, String body);
}
