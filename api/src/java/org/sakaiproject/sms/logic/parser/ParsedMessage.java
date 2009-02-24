package org.sakaiproject.sms.logic.parser;

public class ParsedMessage {

	private final String tool;
	private final String site;
	private final String command;
	private final String body;
	private final String userID;

	public ParsedMessage(String tool, String site, String command, String userID) {
		this.tool = tool;
		this.site = site;
		this.command = command;
		this.body = null;
		this.userID = userID;
	}

	public ParsedMessage(String tool, String site, String command, String body,
			String userID) {
		this.tool = tool;
		this.site = site;
		this.command = command;
		this.body = body;
		this.userID = userID;
	}

	public String getTool() {
		return tool;
	}

	public String getUserID() {
		return userID;
	}

	public String getSite() {
		return site;
	}

	public String getCommand() {
		return command;
	}

	public String getBody() {
		return body;
	}

	public boolean hasBody() {
		return (body != null);
	}

}
