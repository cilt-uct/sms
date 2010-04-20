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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.logic.incoming.ParsedMessage;
import org.sakaiproject.sms.logic.incoming.ShortMessageCommand;
import org.sakaiproject.sms.model.constants.SmsConstants;

/**
 * This returns the same message back to the sender. Useful for testing character set 
 * encoding/decoding issues and verifying connectivity.
 * 
 */
public class SmsPingCommand implements ShortMessageCommand {

	private static final Log LOG = LogFactory.getLog(SmsPingCommand.class);

	public String execute(ParsedMessage msg, String messageType, String sourceAddress) {
	
		String[] body = msg.getBodyParameters();
		
		if (body.length == 0 || body[0] == null || "".equals(body[0].trim())) {
			return SmsConstants.SMS_MO_EMPTY_REPLY_BODY;
		}
		
		LOG.debug("ping-pong to " + sourceAddress + ": " + body[0]);
		
		return "pong " + body[0];
	}

	public String[] getAliases() {
		return new String[] { "P" };
	}

	public String getCommandKey() {
		return "PING";
	}

	public int getBodyParameterCount() {
		return 1;
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isVisible() {
		return false;
	}

	public boolean requiresSiteId() {
		return false;
	}

	public boolean canExecute(ParsedMessage message) {
		return true;
	}

	public String getHelpMessage(String messageType) {
		return "PING any message text";
	}

	public boolean requiresUserId() {
		return false;
	}

}
