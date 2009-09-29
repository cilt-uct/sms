package org.sakaiproject.sms.logic.stubs.commands;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.logic.incoming.ParsedMessage;
import org.sakaiproject.sms.logic.incoming.ShortMessageCommand;

public class MultipleBodySmsCommand implements ShortMessageCommand {

	private static final Log LOG = LogFactory
			.getLog(MultipleBodySmsCommand.class);

	public String param1;
	public String param2;

	public String execute(ParsedMessage msg, String messageType, String mobileNr) {

		String[] body = msg.getBodyParameters();

		LOG.debug(getCommandKey() + " command called with parameters: ("
				+ msg.getSite() + ", " + msg.getIncomingUserId() + ", " + body[0] + ", " + body[1]
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

	public String getHelpMessage(String messageType) {
		return getCommandKey() + " HELP";
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
