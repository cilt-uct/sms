/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.sms.logic.incoming;

import org.apache.commons.lang.StringUtils;

public class ParsedMessage {

	private String command = null;
	private String site = null;
	private String siteTitle = null;
	private String body = null;
	private String bodyReply = null;
	private String incomingUserId = null;
	private String incomingUserEid = null;
	private String[] bodyParameters = null;
	private Long accountId = null;
	


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

	/**
	 * This constructor is primarily for unit testing.
	 * @param user
	 * @param command
	 * @param site
	 * @param body
	 * @param paramcount
	 */
	public ParsedMessage(String user, String command, String site, String body, int paramcount) {
		this.incomingUserId = user;
		this.site = site;
		this.command = command;
		this.body = body;
		
		if (body == null || paramcount == 0) {
			this.bodyParameters = new String[0];
		} else {
			this.bodyParameters = StringUtils.split(body, " ", paramcount);			
		}
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

	public void setBodyParameters(String[] bodyParameters) {
		this.bodyParameters = bodyParameters;
	}

	public String[] getBodyParameters() {
		return bodyParameters;
	}
	
	public String toString() {
		return "command: " + command + " user: " + incomingUserId + 
			" site: " + site + " site-title: " + siteTitle + " body: " + body;
	}

	public void setSiteTitle(String siteTitle) {
		this.siteTitle = siteTitle;
	}

	public String getSiteTitle() {
		return siteTitle;
	}

	public void setIncomingUserEid(String incomingUserEid) {
		this.incomingUserEid = incomingUserEid;
	}

	public String getIncomingUserEid() {
		return incomingUserEid;
	}
	
	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}
}
