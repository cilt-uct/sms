/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2009 The Sakai Foundation
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
package org.sakaiproject.sms.logic.command;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entitybroker.util.http.HttpRESTUtils;
import org.sakaiproject.entitybroker.util.http.HttpResponse;
import org.sakaiproject.entitybroker.util.http.HttpRESTUtils.Method;
import org.sakaiproject.sms.logic.incoming.ParsedMessage;
import org.sakaiproject.sms.logic.incoming.ShortMessageCommand;
import org.sakaiproject.sms.model.hibernate.constants.SmsConstants;

/**
 * This executes an external command by invoking an http url.
 * 
 */
public class SmsRestCommand implements ShortMessageCommand {

	private static final Log LOG = LogFactory.getLog(SmsRestCommand.class);

	private String restUrl = null;
	private String commandKey = null;
	private String helpMessage = null;
	private boolean visible = false;
	private boolean enabled = true;
	private boolean requiresSiteId = false;
	private boolean requiresUserId = false;

	public void setCommandKey(String commandKey) {
		this.commandKey = commandKey;
	}

	public String execute(ParsedMessage msg, String messageType, String sourceAddress) {
	
		LOG.debug("Settings for this command: " + 
				" requiresSiteId=" + requiresSiteId + 
				" requiresUserId=" + requiresUserId);
		
		String[] body = msg.getBodyParameters();
		
		if (body.length == 0 || body[0] == null || "".equals(body[0].trim())) {
			return SmsConstants.SMS_MO_EMPTY_REPLY_BODY;
		}
		
		LOG.debug("executing URL: " + restUrl + " : " + body[0]);
		
		Map<String,String> params = new HashMap<String,String>();
		
		params.put("sourceAddress", sourceAddress);
		params.put("message", body[0]);
		params.put("siteId", msg.getSite());
		params.put("siteTitle", msg.getSiteTitle());
		params.put("userId", msg.getIncomingUserId());
		params.put("command", msg.getCommand());
		
		HttpResponse resp = HttpRESTUtils.fireRequest(restUrl, Method.POST, params);
		
		LOG.debug("Return code is: " + resp.getResponseCode());
		
		String returnStr = "";
		
		if (resp.getResponseCode() == 200) {
			returnStr = resp.getResponseBody();
		}
		
		return returnStr;
	}

	public String[] getAliases() {
		return new String[] { };
	}

	public String getCommandKey() {
		return commandKey;
	}

	public int getBodyParameterCount() {
		return 1;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isVisible() {
		return visible;
	}

	public boolean requiresSiteId() {
		return requiresSiteId;
	}

	public boolean canExecute(ParsedMessage message) {
		return true;
	}

	public String getHelpMessage(String messageType) {
		return helpMessage;
	}

	public boolean requiresUserId() {
		return requiresUserId;
	}

	public void setRestUrl(String restUrl) {
		this.restUrl = restUrl;
	}

	public String getRestUrl() {
		return restUrl;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setRequiresSiteId(boolean requiresSiteId) {
		this.requiresSiteId = requiresSiteId;
	}

	public void setRequiresUserId(boolean requiresUserId) {
		this.requiresUserId = requiresUserId;
	}

	public void setHelpMessage(String helpMessage) {
		this.helpMessage = helpMessage;
	}

}
