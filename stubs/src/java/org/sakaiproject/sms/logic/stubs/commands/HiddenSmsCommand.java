package org.sakaiproject.sms.logic.stubs.commands;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.logic.incoming.ParsedMessage;
import org.sakaiproject.sms.logic.incoming.SmsCommand;

public class HiddenSmsCommand implements SmsCommand {

	private static final Log LOG = LogFactory.getLog(HiddenSmsCommand.class);

	public String execute(ParsedMessage msg, String mobileNr) {
		String concatBody = "";
		String[] body = msg.getBodyParameters();

		for (String arg : body) {
			if (concatBody.equals("")) {
				concatBody += arg;
			} else {
				concatBody += "," + arg;
			}
		}

		LOG.debug(getCommandKey() + " command called with parameters: ("
				+ msg.getSite() + ", " + msg.getIncomingUserId() + ", " + body[0] + ")");
		return getCommandKey();
	}

	public String[] getAliases() {
		return new String[] { "D" };
	}

	public String getCommandKey() {
		return "HIDDEN";
	}

	public String getHelpMessage() {
		return getCommandKey() + " HELP";
	}

	public int getBodyParameterCount() {
		return 2;
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isVisible() {
		return false;
	}

	public boolean requiresSiteId() {
		return true;
	}
}
