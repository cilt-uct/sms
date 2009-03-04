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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.logic.incoming.ParsedMessage;
import org.sakaiproject.sms.logic.incoming.SmsCommand;
import org.sakaiproject.sms.logic.incoming.SmsIncomingLogicManager;
import org.sakaiproject.sms.model.hibernate.constants.SmsHibernateConstants;
import org.sakaiproject.sms.model.smpp.SmsPatternSearchResult;
import org.sakaiproject.sms.util.SmsStringArrayUtil;

public class SmsIncomingLogicManagerImpl implements SmsIncomingLogicManager {

	// Collection for keeping tool ids with their logic
	private final HashMap<String, RegisteredCommands> toolCmdsMap = new HashMap<String, RegisteredCommands>();

	private static Log log = LogFactory
			.getLog(SmsIncomingLogicManagerImpl.class);

	// The help command is valid for all sms-enabled tools
	private static final String HELP = "HELP";

	// TODO: Throw exception if no applicable found?
	public String process(ParsedMessage message) {
		String reply = null;
		if (toolCmdsMap.size() != 0) { // No tools registered
			String toolKey = message.getTool().toUpperCase();
			SmsPatternSearchResult smsPatternSearchResult = new SmsPatternSearchResult();
			String suppliedCommand = message.getCommand().toUpperCase();
			RegisteredCommands registered = null;

			if (isValidCommand(toolKey, suppliedCommand)) { // Everything is
				// valid
				registered = toolCmdsMap.get(toolKey);
				smsPatternSearchResult = findValidCommand(suppliedCommand,
						registered);
			} else {
				if (toolCmdsMap.containsKey(toolKey)) { // Valid tool but
					// invalid command
					registered = toolCmdsMap.get(toolKey);
					smsPatternSearchResult = findValidCommand(suppliedCommand,
							registered);

				} else { // Invalid toolKey
					toolKey = getClosestMatch(
							toolKey,
							toolCmdsMap.keySet().toArray(
									new String[toolCmdsMap.keySet().size()]))
							.getPattern();
					if (toolKey != null) {
						registered = toolCmdsMap.get(toolKey);
						smsPatternSearchResult = findValidCommand(
								suppliedCommand, registered);
					} else {
						reply = generateAssistMessage(smsPatternSearchResult
								.getPossibleMatches(), toolKey);
					}

				}
			}
			if (HELP.equalsIgnoreCase(smsPatternSearchResult.getPattern())) {
				reply = generateAssistMessage(smsPatternSearchResult
						.getPossibleMatches(), toolKey);
			} else if (smsPatternSearchResult.getMatchResult().equals(
					SmsPatternSearchResult.NO_MATCHES)) {
				reply = generateAssistMessage(smsPatternSearchResult
						.getPossibleMatches(), toolKey);
			} else if (smsPatternSearchResult.getMatchResult().equals(
					SmsPatternSearchResult.MORE_THEN_ONE_MATCH)) {
				reply = generateAssistMessage(smsPatternSearchResult
						.getPossibleMatches(), toolKey);
			} else {
				reply = registered.getCommand(
						smsPatternSearchResult.getPattern()).execute(
						message.getSite(), message.getUserId(),
						message.getBody());
			}
		}
		return formatReply(reply);
	}

	// Format reply to be returned
	private String formatReply(String reply) {
		if (reply == null) {
			return null;
		}
		// Just cut off extra characters
		return StringUtils.left(reply.toString(),
				SmsHibernateConstants.MAX_SMS_LENGTH);
	}

	/**
	 * Returns an ArrayList of possible strings that match the requested
	 * command. The idea is based on the Levenshtein distance. Our algorithm
	 * assign the highest scores to matches on the left hand side of the word.
	 * And if the first letter does not match, then its a 0% match for the word.
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
		// chars score more points.If the first chars dont match we break the
		// loop.
		int maxStringScore = 0;

		for (String str : values) {
			boolean skipCommand = false;
			int patternScore = 0;
			char[] commandChars = str.toCharArray();
			char[] validCommands = valueToMatch.toCharArray();
			int startChar = 0;
			for (int i = 0; i < validCommands.length; i++) {
				if (skipCommand) {
					break;
				}
				for (int e = startChar; e < commandChars.length; e++) {
					if (i == 0 && validCommands[0] != commandChars[0]) {
						skipCommand = true;
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
	private SmsPatternSearchResult findValidCommand(String suppliedKey,
			RegisteredCommands commands) {
		SmsPatternSearchResult smsPatternSearchResult = null;
		String aliasedCommand = findAlias(suppliedKey, commands);
		if (aliasedCommand != null) {
			suppliedKey = aliasedCommand;
		}

		if (HELP.equalsIgnoreCase(suppliedKey)) {
			return new SmsPatternSearchResult("HELP");
		} else {
			if (commands.getCommand(suppliedKey) == null) { // None found in
				// command keys
				Set<String> commandKeys = commands.getCommandKeys();
				String[] validCommands = SmsStringArrayUtil.copyOf(commandKeys
						.toArray(new String[commandKeys.size()]), commandKeys
						.size() + 1);
				validCommands[validCommands.length - 1] = HELP;
				smsPatternSearchResult = getClosestMatch(suppliedKey,
						validCommands);
			} else {
				smsPatternSearchResult = new SmsPatternSearchResult(suppliedKey);
			}

			if (smsPatternSearchResult != null) {
				return smsPatternSearchResult;
			}

		}
		// unreachable at the moment because match will always be found
		return new SmsPatternSearchResult("HELP");
	}

	// Tries to command on alias map (returns command if found)
	private String findAlias(String supplied, RegisteredCommands commands) {
		// ? is default alias for HELP
		if ("?".equals(supplied)) {
			return HELP;
		}

		return commands.findAliasCommandKey(supplied);
	}

	public void register(String toolKey, SmsCommand command) {
		toolKey = toolKey.toUpperCase();

		// Toolkey not yet registered
		if (!toolCmdsMap.containsKey(toolKey)) {
			toolCmdsMap.put(toolKey, new RegisteredCommands(command));
			log.debug("Registered tool: " + toolKey);
		} else {
			// If it tool exist
			RegisteredCommands commands = toolCmdsMap.get(toolKey);
			// Add to set of commands
			commands.addCommand(command);
			log.debug("Added command " + command.getCommandKey()
					+ " logic for tool: " + toolKey);
		}

	}

	public void clearCommands(String toolKey) {
		toolKey = toolKey.toUpperCase();
		if (toolCmdsMap.containsKey(toolKey)) {
			toolCmdsMap.remove(toolKey);
		}
	}

	public boolean isValidCommand(String toolKey, String command) {
		toolKey = toolKey.toUpperCase();
		command = command.toUpperCase();
		if (!toolCmdsMap.containsKey(toolKey)) {
			return false;
		} else {
			if (toolCmdsMap.get(toolKey).getCommandKeys().contains(command)
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
		Collection<String> commands = null;
		StringBuilder body = new StringBuilder();

		if (toolKey == null) {
			body.append("Invalid toolname.");
		} else {
			body.append(toolKey + " will understand ");
			if (matches == null || matches.size() == 0
					|| matches.contains(HELP)) {
				commands = toolCmdsMap.get(toolKey.toUpperCase())
						.getCommandKeys();
			} else {
				commands = matches;
			}
			Iterator<String> i = commands.iterator();
			while (i.hasNext()) {
				String command = i.next();
				body.append(command);
				if (i.hasNext()) {
					body.append(",");
				}
			}
		}

		return body.toString();
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
