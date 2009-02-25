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
import org.sakaiproject.sms.util.SmsStringUtil;

public class SmsIncomingLogicManagerImpl implements SmsIncomingLogicManager {

	// Collection for keeping tool ids with their logic
	private final HashMap<String, IncomingSmsLogic> toolLogicMap = new HashMap<String, IncomingSmsLogic>();
	
	private static Log log = LogFactory.getLog(SmsIncomingLogicManagerImpl.class);
	
	// TODO: Throw exception if no applicable found? Always find closest match now
	public void process(ParsedMessage message) {
		String toolKey = message.getTool().toUpperCase();
		String cmd = message.getCommand().toUpperCase();
		IncomingSmsLogic logic = null;
				
		if (isValidCommand(toolKey, cmd)) { // Everything is valid
			logic = toolLogicMap.get(toolKey);
			cmd = SmsStringUtil.findInArray(logic.getCommandKeys(), message.getCommand());
		} else {
			if (toolLogicMap.containsKey(toolKey)) { // Valid tool but invalid command
				logic = toolLogicMap.get(toolKey);
				cmd = getClosestMatch(cmd, logic.getCommandKeys());

			} else { // Invalid toolKey
				toolKey = getClosestMatch(toolKey, toolLogicMap.keySet().toArray(new String[toolLogicMap.keySet().size()]));
				logic = toolLogicMap.get(toolKey);
				 
				if (isValidCommand(toolKey, message.getCommand())) { // valid command
					cmd = SmsStringUtil.findInArray(logic.getCommandKeys(), message.getCommand());
				} else { // invalid command
					cmd = getClosestMatch(cmd, logic.getCommandKeys());
				}
			}
		}
		
		logic.execute(cmd, message.getSite(), message.getUserID(), message.getBody());
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
			if (Arrays.asList(SmsStringUtil.upperCaseArray(toolLogicMap.get(toolKey).getCommandKeys())).contains(command)) {
				return true;
			} else {
				return false;
			}
		}
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
