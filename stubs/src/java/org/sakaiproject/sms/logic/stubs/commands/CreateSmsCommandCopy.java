package org.sakaiproject.sms.logic.stubs.commands;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.logic.incoming.ParsedMessage;
import org.sakaiproject.sms.logic.incoming.ShortMessageCommand;

public class CreateSmsCommandCopy implements ShortMessageCommand {

	private static final Log LOG = LogFactory
			.getLog(CreateSmsCommandCopy.class);

	public String execute(ParsedMessage msg, String messageType, String mobileNr) {
		
		String[] body = msg.getBodyParameters();
		
		LOG.debug(getCommandKey() + " command called with parameters: ("
				+ msg.getSite() + ", " + msg.getIncomingUserId() + ", " + body[0] + ")");
		return getCommandKey() + " COPY";
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
		return false;
	}

	public boolean canExecute(ParsedMessage message) {
		return true;
	}

	public boolean requiresUserId() {
		// TODO Auto-generated method stub
		return false;
	}
}
