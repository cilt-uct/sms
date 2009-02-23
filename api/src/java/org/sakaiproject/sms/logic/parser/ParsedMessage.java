package org.sakaiproject.sms.logic.parser;

public class ParsedMessage {
	
	private final String tool;
	private final String site;
	private final String command;
	private final String body;
	
	public ParsedMessage(String tool, String site, String command) {
		this.tool = tool;
		this.site = site;
		this.command = command;
		this.body = null;
	}
	
	public ParsedMessage(String tool, String site, String command, String body) {
		this.tool = tool;
		this.site = site;
		this.command = command;
		this.body = body;
	}

	public String getTool() {
		return tool;
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
