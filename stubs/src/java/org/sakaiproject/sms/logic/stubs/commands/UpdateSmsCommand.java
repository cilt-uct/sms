package org.sakaiproject.sms.logic.stubs.commands;

import org.sakaiproject.sms.logic.incoming.ParsedMessage;
import org.sakaiproject.sms.logic.incoming.ShortMessageCommand;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateSmsCommand implements ShortMessageCommand {


	public String execute(ParsedMessage msg, String messageType, String mobileNr) {
		String concatBody = "";
		String[] body = msg.getBodyParameters();

		for (String arg : body) {
			if ("".equals(concatBody)) {
				concatBody += arg;
			} else {
				concatBody += "," + arg;
			}
		}
		log.debug(getCommandKey() + " command called with parameters: ("
				+ msg.getSite() + ", " + msg.getIncomingUserId() + ", " + body[0] + ")");
		return getCommandKey();
	}

	public String[] getAliases() {
		return new String[] { "U" };
	}

	public String getCommandKey() {
		return "UPDATE";
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
