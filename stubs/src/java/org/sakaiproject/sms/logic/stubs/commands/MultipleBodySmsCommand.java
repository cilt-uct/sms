package org.sakaiproject.sms.logic.stubs.commands;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.logic.incoming.SmsCommand;

public class MultipleBodySmsCommand implements SmsCommand {

	private static final Log LOG = LogFactory
			.getLog(MultipleBodySmsCommand.class);

	public String param1;
	public String param2;

	public String execute(String siteId, String userId, String mobileNr,
			String... body) {
		LOG.debug(getCommandKey() + " command called with parameters: ("
				+ siteId + ", " + userId + ", " + body[0] + ", " + body[1]
				+ ")");
		param1 = body[0];
		param2 = body[1];
		return getCommandKey();
	}

	public String[] getAliases() {
		return new String[] { "M" };
	}

	public int getBodyParameterCount() {
		return 2;
	}

	public String getCommandKey() {
		return "MULTIPLE";
	}

	public String getHelpMessage() {
		return getCommandKey() + " HELP";
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isVisible() {
		return true;
	}

	public boolean requiresSiteId() {
		// TODO Auto-generated method stub
		return false;
	}

}
