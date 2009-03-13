/***********************************************************************************
 * ParsedMessage.java
 * Copyright (c) 2008 Sakai Project/Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.sms.logic.incoming;

public class ParsedMessage {

	private String command;
	private String site;
	private final String body;
	private String body_reply;
	private String incomingUserId;

	public ParsedMessage() {
		this.site = null;
		this.command = null;
		this.body = null;
		this.body_reply = null;

	}

	public ParsedMessage(String command) {
		this.site = null;
		this.command = command;
		this.body = null;
		this.body_reply = null;
	}

	public ParsedMessage(String command, String site) {
		this.site = site;
		this.command = command;
		this.body = null;
		this.body_reply = null;

	}

	public ParsedMessage(String command, String site, String body) {
		this.site = site;
		this.command = command;
		this.body = body;
		this.body_reply = null;
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

	public String getBody_reply() {
		return body_reply;
	}

	public void setBody_reply(String body_reply) {
		this.body_reply = body_reply;
	}

	public String getIncomingUserId() {
		return incomingUserId;
	}

	public void setIncomingUserId(String incomingUserId) {
		this.incomingUserId = incomingUserId;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public void setSite(String site) {
		this.site = site;
	}
}
