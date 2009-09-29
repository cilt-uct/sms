package org.sakaiproject.sms.logic.stubs.commands;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.logic.incoming.ParsedMessage;
import org.sakaiproject.sms.logic.incoming.ShortMessageCommand;

public class CreateSmsCommand implements ShortMessageCommand {

	private static final Log LOG = LogFactory.getLog(CreateSmsCommand.class);

	public String execute(ParsedMessage msg, String messageType, String mobileNr) {
	
		String[] body = msg.getBodyParameters();
		String concatBody = "";
		for (String arg : body) {
			if ("".equals(concatBody)) {
				concatBody += arg;
			} else {
				concatBody += "," + arg;
			}
		}
		LOG.debug(getCommandKey() + " command called with parameters: ("
				+ msg.getSite() + ", " + msg.getIncomingUserId() + ", " + concatBody + ")");
		return getCommandKey();
	}

	public String[] getAliases() {
		return new String[] { "C" };
	}

	public String getCommandKey() {
		return "CREATE";
	}

	public String getHelpMessage(String messageType) {
		return getCommandKey() + " HELP";
	}

	public int getBodyParameterCount() {
		return 1;
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isVisible() {
		return true;
	}

	public boolean requiresSiteId() {
		return true;
	}

	public boolean canExecute(ParsedMessage message) {
		return true;
	}

	public boolean requiresUserId() {
		// TODO Auto-generated method stub
		return false;
	}

}
