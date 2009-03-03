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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.logic.incoming.IncomingSmsLogic;
import org.sakaiproject.sms.logic.incoming.ParsedMessage;
import org.sakaiproject.sms.logic.incoming.SmsIncomingLogicManager;
import org.sakaiproject.sms.model.smpp.SmsPatternSearchResult;
import org.sakaiproject.sms.util.SmsStringArrayUtil;

public class SmsIncomingLogicManagerImpl implements SmsIncomingLogicManager {

	// Collection for keeping tool ids with their logic
	private final HashMap<String, IncomingSmsLogic> toolLogicMap = new HashMap<String, IncomingSmsLogic>();

	private static Log log = LogFactory
			.getLog(SmsIncomingLogicManagerImpl.class);

	// The help command is valid for all sms-enabled tools
	private static final String HELP = "HELP";

	// TODO: Throw exception if no applicable found?
	public String process(ParsedMessage message) {
		if (toolLogicMap.size() != 0) { // No logic registered
			String toolKey = message.getTool().toUpperCase();
			SmsPatternSearchResult smsPatternSearchResult = new SmsPatternSearchResult();
			String cmd = message.getCommand().toUpperCase();
			IncomingSmsLogic logic = null;
			if (isValidCommand(toolKey, cmd)) { // Everything is valid
				logic = toolLogicMap.get(toolKey);
				smsPatternSearchResult = findValidCommand(cmd, logic);
			} else {
				if (toolLogicMap.containsKey(toolKey)) { // Valid tool but
					// invalid command
					logic = toolLogicMap.get(toolKey);
					smsPatternSearchResult = findValidCommand(cmd, logic);

				} else { // Invalid toolKey
					toolKey = getClosestMatch(
							toolKey,
							toolLogicMap.keySet().toArray(
									new String[toolLogicMap.keySet().size()]))
							.getPattern();
					if (toolKey != null) {
						logic = toolLogicMap.get(toolKey);
						smsPatternSearchResult = findValidCommand(cmd, logic);
					} else {
						return generateAssistMessage(smsPatternSearchResult
								.getPossibleMatches(), toolKey);
					}

				}
			}
			if (HELP.equalsIgnoreCase(cmd)) {
				return generateAssistMessage(smsPatternSearchResult
						.getPossibleMatches(), toolKey);
			} else if (smsPatternSearchResult.getMatchResult().equals(
					SmsPatternSearchResult.NO_MATCHES)) {
				return generateAssistMessage(smsPatternSearchResult
						.getPossibleMatches(), toolKey);
			} else if (smsPatternSearchResult.getMatchResult().equals(
					SmsPatternSearchResult.MORE_THEN_ONE_MATCH)) {
				return generateAssistMessage(smsPatternSearchResult
						.getPossibleMatches(), toolKey);
			} else {
				return logic.execute(smsPatternSearchResult.getPattern(),
						message.getSite(), message.getUserID(), message
								.getBody());

			}
		}
		return null;
	}

	/**
	 * Returns an ArrayList of possible strings that match the requested
	 * command.
	 *
	 * @param valueToMatch
	 * @param values
	 * @return
	 */
	private ArrayList<String> getPossibleMatches(String valueToMatch,
			String[] values) {
		ArrayList<String> returnVals = new ArrayList<String>();
		String returnVal = null;
		// We first check for matching parts.
		ArrayList<String> matchedValues = new ArrayList<String>();
		for (String str : values) {
			if (str.indexOf(valueToMatch) != -1) {
				matchedValues.add(str);
			}
		}
		if (matchedValues.size() > 0) {
			values = matchedValues.toArray(new String[matchedValues.size()]);
		}
		// We calculate the largest string's length to be used as weights.
		int largestString = 0;
		for (String str : values) {
			if (str.length() > largestString) {
				largestString = str.length();
			}

		}
		// We loop through each command and pattern. Left hand matching
		// chars score more points.If the first chars dont match we break the loop.
		int maxStringScore = 0;

		for (String str : values) {
			boolean skipCommand=false;
			int patternScore = 0;
			char[] commandChars = str.toCharArray();
			char[] validCommands = valueToMatch.toCharArray();
			int startChar = 0;
			for (int i = 0; i < validCommands.length; i++) {
				if(skipCommand){
					break;
				}
				for (int e = startChar; e < commandChars.length; e++) {
					if(i==0 && validCommands[0] != commandChars[0]){
						skipCommand=true;
						break;
					}
					if (validCommands[i] == commandChars[e]) {
						startChar = e + 1;
						patternScore += (largestString - (e));
						break;

					}

				}

			}
			if (patternScore != 0 && patternScore == maxStringScore) {
				returnVals.add(str);
			}

			if (patternScore > maxStringScore) {

				maxStringScore = patternScore;
				returnVal = str;
			}
		}
		if (returnVal != null) {
			returnVals.add(returnVal);
		}
		return returnVals;
	}

	/**
	 * Finds valid command EXACTLY as it is specified in command keys or HELP
	 *
	 * @return valid command
	 */
	private SmsPatternSearchResult findValidCommand(String command,
			IncomingSmsLogic logic) {
		SmsPatternSearchResult smsPatternSearchResult = null;
		String aliasedCommand = findAlias(command, logic);
		if (aliasedCommand != null) {
			command = aliasedCommand;
		}

		if (HELP.equalsIgnoreCase(command)) {
			return new SmsPatternSearchResult("HELP");
		} else {
			String toReturn = SmsStringArrayUtil.findInArray(logic
					.getCommandKeys(), command);

			if (toReturn == null) { // None found in command keys
				String[] values = SmsStringArrayUtil.copyOf(logic
						.getCommandKeys(), logic.getCommandKeys().length + 1);
				values[values.length - 1] = HELP;
				smsPatternSearchResult = getClosestMatch(command, values);
			} else {
				smsPatternSearchResult = new SmsPatternSearchResult(command);

			}

			if (smsPatternSearchResult != null) {
				return smsPatternSearchResult;
			}

		}
		// unreachable at the moment because match will always be found
		return new SmsPatternSearchResult("HELP");
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
			if (Arrays.asList(
					SmsStringArrayUtil.upperCaseArray(toolLogicMap.get(toolKey)
							.getCommandKeys())).contains(command)
					|| HELP.equalsIgnoreCase(command)) { // HELP command is
				// valid by default
				return true;
			} else {
				return false;
			}
		}
	}

	public String generateAssistMessage(ArrayList<String> matches,
			String toolKey) {
		String[] commands = null;
		StringBuilder body = new StringBuilder();

		if (toolKey == null) {
			body.append("Invalid toolname.");
		} else {
			body.append("Valid commands: \n");

			if (matches == null ||matches.size()==0 || matches.contains(HELP)) {
				commands = toolLogicMap.get(toolKey.toUpperCase())
						.getCommandKeys();
			} else {
				commands = matches.toArray(new String[matches.size()]);
			}

			for (int i = 0; i < commands.length; i++) {
				body.append(commands[i]);
				if (i != commands.length - 1) {
					body.append(", ");
				}
			}
		}

		// Just cut off extra characters
		return StringUtils.left(body.toString(), 160);
	}

	/**
	 *
	 * @param valueToMatch
	 * @param values
	 * @return
	 */
	public SmsPatternSearchResult getClosestMatch(String valueToMatch,
			String[] values) {
		SmsPatternSearchResult SmsPatternSearchResult = new SmsPatternSearchResult();
		ArrayList<String> possibleMatches = getPossibleMatches(valueToMatch,
				values);

		SmsPatternSearchResult.setPossibleMatches(possibleMatches);
		return SmsPatternSearchResult;

	}
}
