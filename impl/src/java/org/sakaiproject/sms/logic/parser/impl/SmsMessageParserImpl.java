/***********************************************************************************
 * SmsMessageParserImpl.java
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
package org.sakaiproject.sms.logic.parser.impl;

import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.sms.logic.parser.ParsedMessage;
import org.sakaiproject.sms.logic.parser.SmsMessageParser;
import org.sakaiproject.sms.logic.parser.exception.ParseException;

public class SmsMessageParserImpl implements SmsMessageParser {

	private static final String DELIMITERS = " \t\r\n\f";
	
	// Collection for keeping tool ids with their commands
	private final HashMap<String,HashSet<String>> toolCommandsMapping = new HashMap<String,HashSet<String>>();
	
	/**
	 * Parses the message general. Try to figure out the sakai site and user.
	 */
	public ParsedMessage parseMessage(String msgText) throws ParseException {
		if (msgText == null) {
			throw new ParseException("null message supplied");
		}
		
		String[] params = StringUtils.split(msgText, DELIMITERS, 4);
		
		// Must at lease contain tool + site + command
		if (params.length < 3) {
			throw new ParseException("Invalid number of tokens: " + params.length);
		}
		
		if (params.length == 3) {
			return new ParsedMessage(params[0], params[1], params[2]);
		} else {
			return new ParsedMessage(params[0], params[1], params[2], params[3]);
		}
		
	}

	/**
	 * Check for valid pin, sakai site code, mobile number etc.
	 */
	public void validateMessageGeneral() {
		// Make required calls to our external logic
	}

	public void toolRegisterCommands(String sakaiToolId,
			String[] validCommands) {
		// call to this method does not insert duplicates.
		
		String toolKey = sakaiToolId.toUpperCase();
		
		if (!toolCommandsMapping.containsKey(toolKey)) {
			HashSet<String> commands = new HashSet<String>();
			for (String command : validCommands) {
				commands.add(command.toUpperCase());
			}
			toolCommandsMapping.put(toolKey, commands);
		} else {
			// If exists replace
			toolCommandsMapping.remove(toolKey);
			HashSet<String> commands = new HashSet<String>();
			for (String command : validCommands) {
				commands.add(command.toUpperCase());
			}
			toolCommandsMapping.put(toolKey, commands);
		}
	}

	public boolean toolMatchCommand(String sakaiToolId, String smsCommand) {
		if (sakaiToolId == null || smsCommand == null) {
			return false;
		}
			
		return toolCommandsMapping.get(sakaiToolId.toUpperCase()).contains(smsCommand.toUpperCase());
	}

	public void toolProcessCommand(String sakaiToolId, String command,
			String commandSuffix) {
		// TODO Auto-generated method stub

	}
	
	
}
