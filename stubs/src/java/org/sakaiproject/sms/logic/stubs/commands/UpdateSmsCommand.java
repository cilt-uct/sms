package org.sakaiproject.sms.logic.stubs.commands;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.logic.incoming.SmsCommand;

public class UpdateSmsCommand implements SmsCommand {

	private static Log log = LogFactory.getLog(UpdateSmsCommand.class);

	public String execute(String siteId, String userId, String mobileNr,
			String... body) {
		log.debug(getCommandKey() + " command called with parameters: ("
				+ siteId + ", " + userId + ", " + body + ")");
		return getCommandKey();
	}

	public String[] getAliases() {
		return new String[] { "U" };
	}

	public String getCommandKey() {
		return "UPDATE";
	}

	public String getHelpMessage() {
		return getCommandKey() + " HELP";
	}

	public int getBodyParameterCount() {
		return 1;
	}

	public boolean isEnabled() {
		return true;
	}
}
