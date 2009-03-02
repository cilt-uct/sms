/***********************************************************************************
 * SmsIncomingLogicManagerImpl.java
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
package org.sakaiproject.sms.logic.incoming.impl;

import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.logic.incoming.IncomingSmsLogic;
import org.sakaiproject.sms.logic.incoming.ParsedMessage;
import org.sakaiproject.sms.logic.incoming.SmsIncomingLogicManager;
import org.sakaiproject.sms.util.SmsStringArrayUtil;

public class SmsIncomingLogicManagerImpl implements SmsIncomingLogicManager {

	// Collection for keeping tool ids with their logic
	private final HashMap<String, IncomingSmsLogic> toolLogicMap = new HashMap<String, IncomingSmsLogic>();
	
	private static Log log = LogFactory.getLog(SmsIncomingLogicManagerImpl.class);
	
	// The help command is valid for all sms-enabled tools 
	private static final String HELP = "HELP";
	
	// TODO: Throw exception if no applicable found?
	public String process(ParsedMessage message) {
		if (toolLogicMap.size() != 0) { // No logic registered
			String toolKey = message.getTool().toUpperCase();
			String cmd = message.getCommand().toUpperCase();
			IncomingSmsLogic logic = null;
					
			if (isValidCommand(toolKey, cmd)) { // Everything is valid
				logic = toolLogicMap.get(toolKey);
				cmd = findValidCommand(message.getCommand(), logic);
			} else {
				if (toolLogicMap.containsKey(toolKey)) { // Valid tool but invalid command
					logic = toolLogicMap.get(toolKey);
					cmd = findValidCommand(cmd, logic);

				} else { // Invalid toolKey
					toolKey = getClosestMatch(toolKey, toolLogicMap.keySet().toArray(new String[toolLogicMap.keySet().size()]));
					logic = toolLogicMap.get(toolKey);
					cmd = findValidCommand(message.getCommand(), logic);
				}
			}
			
			if (HELP.equalsIgnoreCase(cmd)) {
				return generateAssistMessage(toolKey);
			} else {
				return logic.execute(cmd, message.getSite(), message.getUserID(), message.getBody());
			}
		}
		return null;
	}

	/**
	 * Finds valid command EXACTLY as it is specified in command keys or HELP
	 * @return valid command
	 */
	private String findValidCommand(String command, IncomingSmsLogic logic) {
		String aliasedCommand = findAlias(command, logic);
		if (aliasedCommand != null) {
			command = aliasedCommand;
		}
		
		if (HELP.equalsIgnoreCase(command)) {
			return HELP;
		} else {
			String toReturn = SmsStringArrayUtil.findInArray(logic.getCommandKeys(), command);
			
			if (toReturn == null) { // None found in command keys
				String[] values =  SmsStringArrayUtil.copyOf(logic.getCommandKeys(), logic.getCommandKeys().length + 1);
				values[values.length-1] = HELP;
				toReturn = getClosestMatch(command, values);
			}
			
			if (toReturn != null) {
				return toReturn;
			}
			
		}
		// unreachable at the moment because match will always be found
		return HELP;
	}
	
	// Tries to command on alias map (returns command if found)
	private String findAlias(String command, IncomingSmsLogic logic) {
		// ? is default alias for HELP
		if ("?".equals(command)) {
			return HELP;
		}
		
		if (logic.getAliases() != null) {
			return logic.getAliases().get(command.toUpperCase());
		}
		return null;
	}
	
	public void register(String toolKey, IncomingSmsLogic logic) {
		toolKey = toolKey.toUpperCase();
		
		if (!toolLogicMap.containsKey(toolKey)) {
			toolLogicMap.put(toolKey, logic);
			log.debug("Registered tool: " + toolKey);
		} else {
			// If it exists replace
			toolLogicMap.remove(toolKey);
			toolLogicMap.put(toolKey, logic);
			log.debug("Replaced logic for tool: " + toolKey);
		}
		
	}
	
	public boolean isValidCommand(String toolKey, String command) {
		toolKey = toolKey.toUpperCase();
		command = command.toUpperCase();
		if (!toolLogicMap.containsKey(toolKey)) {
			return false;
		} else {
			if (Arrays.asList(SmsStringArrayUtil.upperCaseArray(toolLogicMap.get(toolKey).getCommandKeys())).contains(command) 
					|| HELP.equalsIgnoreCase(command)) { // HELP command is valid by default
				return true;
			} else {
				return false;
			}
		}
	}
	
	public String generateAssistMessage(String toolKey) {
		String[] commands = toolLogicMap.get(toolKey.toUpperCase()).getCommandKeys();
		StringBuilder body = new StringBuilder();
		body.append("Valid commands: \n");
		for (int i=0; i < commands.length; i++) {
			body.append(commands[i]);
			if (i != commands.length-1) {
				body.append(", ");
			}
		}
		// Just cut off extra characters
		return StringUtils.left(body.toString(), 160);
	}

	// Use Levenshtein distance to find closest match
	private String getClosestMatch(String supplied, String[] values) {
		int minDistance = Integer.MAX_VALUE;
		String closestMatch = supplied;
		
		for (String str : values) {
			int strDistance = StringUtils.getLevenshteinDistance(supplied, str);
			if (strDistance < minDistance) {
				closestMatch = str;
				minDistance = strDistance;
			}
		}
		
		return closestMatch;
	}
}
