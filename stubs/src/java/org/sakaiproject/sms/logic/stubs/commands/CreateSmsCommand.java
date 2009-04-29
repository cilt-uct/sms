package org.sakaiproject.sms.logic.stubs.commands;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.logic.incoming.SmsCommand;

public class CreateSmsCommand implements SmsCommand {

	private static Log log = LogFactory.getLog(CreateSmsCommand.class);

	public String execute(String siteId, String userId, String mobileNr, String... body) {
		log.debug(getCommandKey() + " command called with parameters: ("
				+ siteId + ", " + userId + ", " + body + ")");
		return getCommandKey();
	}

	public String[] getAliases() {
		return new String[] { "C" };
	}

	public String getCommandKey() {
		return "CREATE";
	}

	public String getHelpMessage() {
		return getCommandKey() + " HELP";
	}

	public int getBodyParameterCount() {
		return 1;
	}

}
