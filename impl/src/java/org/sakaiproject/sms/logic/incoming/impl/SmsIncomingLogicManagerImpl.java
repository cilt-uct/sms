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
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.incoming.DuplicateCommandKeyException;
import org.sakaiproject.sms.logic.incoming.ParsedMessage;
import org.sakaiproject.sms.logic.incoming.SmsCommand;
import org.sakaiproject.sms.logic.incoming.SmsIncomingLogicManager;
import org.sakaiproject.sms.logic.incoming.SmsMessageParser;
import org.sakaiproject.sms.logic.parser.exception.ParseException;
import org.sakaiproject.sms.model.hibernate.constants.SmsConstants;
import org.sakaiproject.sms.model.smpp.SmsPatternSearchResult;

public class SmsIncomingLogicManagerImpl implements SmsIncomingLogicManager {

	// Collection for keeping tool ids with their logic
	private final HashMap<String, RegisteredCommands> toolCmdsMap = new HashMap<String, RegisteredCommands>();

	private final RegisteredCommands allCommands = new RegisteredCommands();

	private static Log log = LogFactory
			.getLog(SmsIncomingLogicManagerImpl.class);

	private ExternalLogic externalLogic;

	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	private SmsMessageParser smsMessageParser;

	public void setSmsMessageParser(SmsMessageParser smsMessageParser) {
		this.smsMessageParser = smsMessageParser;
	}

	// TODO: Throw exception if no applicable found?
	public ParsedMessage process(String smsMessagebody, String mobileNr) {

		String reply = null;
		ParsedMessage parsedMessage = null;
		String incomingUserID = null;
		SmsPatternSearchResult validCommandMatch = null;

		try {
			parsedMessage = smsMessageParser.parseMessage(smsMessagebody);
			if (toolCmdsMap.size() != 0) { // No tools registered
				String suppliedCommand = parsedMessage.getCommand()
						.toUpperCase();

				validCommandMatch = findValidCommand(suppliedCommand,
						allCommands);

				if ((validCommandMatch.getPattern() != null)
						&& (validCommandMatch.getMatchResult() != null)) {
					if (SmsConstants.HELP.equalsIgnoreCase(validCommandMatch
							.getPattern())) {
						// TODO: Awaiting feedback from UCT about which site to
						// bill for help commands
						parsedMessage.setSite("!admin");
						reply = generateHelpMessage();

					} else if (validCommandMatch.getMatchResult().equals(
							SmsPatternSearchResult.NO_MATCHES)) {
						reply = generateAssistMessage(validCommandMatch
								.getPossibleMatches());
					} else if (validCommandMatch.getMatchResult().equals(
							SmsPatternSearchResult.MORE_THEN_ONE_MATCH)) {
						reply = generateAssistMessage(validCommandMatch
								.getPossibleMatches());
					} else {
						String site = getValidSite(parsedMessage.getSite());
						if (site == null) {
							reply = generateInvalidSiteMessage(parsedMessage
									.getSite());
						} else {
							// I don't think this is a good method of retrieving
							// the user ids
							parsedMessage.setSite(site);
							List<String> userIds = externalLogic
									.getUserIdsFromMobileNumber(mobileNr);
							if (userIds.size() == 0) {
								reply = generateInvalidMobileNrMessage(mobileNr);
							} else {
								incomingUserID = userIds.get(0);
								reply = allCommands.getCommand(
										validCommandMatch.getPattern())
										.execute(site, incomingUserID,
												parsedMessage.getBody());

							}
						}
					}
				} else {
					reply = generateInvalidCommand();
				}
			}

		} catch (ParseException e) {
			parsedMessage = new ParsedMessage();
			reply = generateInvalidCommand();
			// TODO: Awaiting feedback from UCT about which site to bill for
			// invalid commands
			parsedMessage.setSite("!admin");
		}

		parsedMessage.setBody_reply(formatReply(reply));
		parsedMessage.setIncomingUserId(incomingUserID);
		if (validCommandMatch != null) {
			parsedMessage.setCommand(validCommandMatch.getPattern());

		}
		return parsedMessage;
	}

	/**
	 * Returns a valid site
	 * 
	 * @param suppliedSiteId
	 *            as specified by message
	 * @return
	 */
	private String getValidSite(String suppliedSiteId) {
		if (externalLogic.isValidSite(suppliedSiteId)) {
			return suppliedSiteId;
		} else {
			String siteId = externalLogic.getSiteFromAlias(suppliedSiteId);
			if (siteId != null) {
				return siteId;
			} else {
				SmsPatternSearchResult result = getClosestMatch(suppliedSiteId,
						externalLogic.getAllAliasesAsArray());
				if (result.getMatchResult().equals(
						SmsPatternSearchResult.ONE_MATCH)) {
					return externalLogic.getSiteFromAlias(result.getPattern());
				} else {
					return null;
				}
			}
		}

	}

	// Format reply to be returned
	private String formatReply(String reply) {
		if (reply == null) {
			return null;
		}
		// Just cut off extra characters
		return StringUtils.left(reply.toString(), SmsConstants.MAX_SMS_LENGTH);
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
		valueToMatch = valueToMatch.toUpperCase();
		ArrayList<String> returnVals = new ArrayList<String>();
		String returnVal = null;
		// We first check for matching parts.
		ArrayList<String> matchedValues = new ArrayList<String>();
		for (String str : values) {
			str = str.toUpperCase();
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
			str = str.toUpperCase();
			if (str.length() > largestString) {
				largestString = str.length();
			}

		}
		// We loop through each command and pattern. Left hand matching
		// chars score more points.If the first chars dont match we break the
		// loop.
		int maxStringScore = 0;

		for (String str : values) {
			str = str.toUpperCase();
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

		if (SmsConstants.HELP.equalsIgnoreCase(suppliedKey)) {
			return new SmsPatternSearchResult(SmsConstants.HELP);
		} else {
			if (commands.getCommand(suppliedKey) == null) { // None found in
				// command keys
				Set<String> commandKeys = commands.getCommandKeys();
				String[] validCommands = commandKeys
						.toArray(new String[commandKeys.size()]);
				// HELP must must not be used for closest search
				// String[] validCommands =
				// SmsStringArrayUtil.copyOf(commandKeys
				// .toArray(new String[commandKeys.size()]), commandKeys
				// .size() + 1);
				// validCommands[validCommands.length - 1] = SmsConstants.HELP;
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
		return new SmsPatternSearchResult(SmsConstants.HELP);
	}

	// Tries to command on alias map (returns command if found)
	private String findAlias(String supplied, RegisteredCommands commands) {
		// ? is default alias for HELP
		if ("?".equals(supplied)) {
			return SmsConstants.HELP;
		}

		return commands.findAliasCommandKey(supplied);
	}

	public void register(String toolKey, SmsCommand command) {
		toolKey = toolKey.toUpperCase();

		if (SmsConstants.HELP.equalsIgnoreCase(command.getCommandKey())) {
			log.error(SmsConstants.HELP + " is a reserved command key");
		} else if (allCommands.getCommandKeys().contains(
				command.getCommandKey().toUpperCase())) {
			throw new DuplicateCommandKeyException(command.getCommandKey()
					.toUpperCase());
		} else {
			// Toolkey not yet registered
			if (!toolCmdsMap.containsKey(toolKey)) {
				toolCmdsMap.put(toolKey, new RegisteredCommands(command));
				log.debug("Registered tool: " + toolKey);
			} else {
				// If it tool exist
				RegisteredCommands commands = toolCmdsMap.get(toolKey);
				// Add to set of commands
				commands.addCommand(command);
			}
			log.info("Registered command " + command.getCommandKey()
					+ " for tool " + toolKey);
			allCommands.addCommand(command);
		}
	}

	public void clearCommands(String toolKey) {
		toolKey = toolKey.toUpperCase();
		if (toolCmdsMap.containsKey(toolKey)) {
			RegisteredCommands commands = toolCmdsMap.get(toolKey);
			for (String commandKey : commands.getCommandKeys()) {
				allCommands.removeByCommandKey(commandKey);
			}
			toolCmdsMap.remove(toolKey);
		}
	}

	public boolean isValidCommand(String command) {
		command = command.toUpperCase();
		if (allCommands.getCommandKeys().contains(command)
				|| SmsConstants.HELP.equalsIgnoreCase(command)) { // HELP
			// command
			// is
			// valid by default
			return true;
		} else {
			return false;
		}

	}

	private String generateInvalidSiteMessage(String site) {
		return "Invalid site (" + site + ") supplied";
	}

	private String generateInvalidMobileNrMessage(String mobileNr) {
		return "Invalid mobile number (" + mobileNr + ") used";
	}

	private String generateInvalidCommand() {
		return "Invalid command supplied. Please sms help for valid commands.";
	}

	private String generateHelpMessage() {
		StringBuilder body = new StringBuilder();
		body.append("Valid commands: ");

		Iterator<String> i = allCommands.getCommandKeys().iterator();
		while (i.hasNext()) {
			String command = i.next();
			body.append(command);
			if (i.hasNext()) {
				body.append(", ");
			}
		}

		return body.toString();
	}

	public String generateAssistMessage(ArrayList<String> matches) {
		Collection<String> commands = null;
		StringBuilder body = new StringBuilder();

		body.append("Possible matches: ");
		if (matches == null || matches.size() == 0
				|| matches.contains(SmsConstants.HELP)) {
			// TODO: not sure what to do here
		} else {
			commands = matches;
		}
		Iterator<String> i = commands.iterator();
		while (i.hasNext()) {
			String command = i.next();
			body.append(command);
			if (i.hasNext()) {
				body.append(", ");
			}
		}

		return body.toString();
	}

	public String generateAssistMessage(String tool) {
		if (tool == null || !toolCmdsMap.containsKey(tool.toUpperCase())) {
			return "Invalid tool";
		} else {
			StringBuilder body = new StringBuilder();
			body.append("Possible matches: ");
			RegisteredCommands commands = toolCmdsMap.get(tool.toUpperCase());
			Iterator<String> i = commands.getCommandKeys().iterator();
			while (i.hasNext()) {
				String command = i.next();
				body.append(command);
				if (i.hasNext()) {
					body.append(", ");
				}
			}

			return body.toString();
		}

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

	// private Map<String, SmsCommand> getAllRegisteredCommands() {
	// Set<String> toolKeys = toolCmdsMap.keySet();
	// Map<String, SmsCommand> cmds = new HashMap<String, SmsCommand>();
	// for (String key : toolKeys) {
	// Map<String, SmsCommand> toolCmds = toolCmdsMap.get(key)
	// .getCommands();
	// Set<String> commandKeys = toolCmds.keySet();
	// for (String commandKey : commandKeys) {
	// SmsCommand command = toolCmds.get(commandKey);
	// cmds.put(command.getCommandKey(), command);
	// }
	// }
	// return cmds;
	// }
}
