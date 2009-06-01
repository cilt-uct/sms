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

	private String command = null;
	private String site = null;
	private String body = null;
	private String bodyReply = null;
	private String incomingUserId = null;

	public ParsedMessage() {

	}

	public ParsedMessage(String command) {

		this.command = command;

	}

	public ParsedMessage(String command, String site) {
		this.site = site;
		this.command = command;

	}

	public ParsedMessage(String command, String site, String body) {
		this.site = site;
		this.command = command;
		this.body = body;

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

	public void setBody(String body) {
		this.body = body;
	}

	public String getBodyReply() {
		return bodyReply;
	}

	public void setBodyReply(String bodyReply) {
		this.bodyReply = bodyReply;
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
