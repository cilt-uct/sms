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

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.sakaiproject.sms.model.smpp.SmsPatternSearchResult;

/**
 * Registers {@link IncomingSmsLogic} to process incoming messages
 */
public interface SmsIncomingLogicManager {

	/**
	 * Registers a {@link IncomingSmsLogic} for specified tool
	 * 
	 * @param toolKey
	 *            unique key for tool
	 * @param command
	 *            {@link ShortMessageCommand} to register for tool
	 */
	public void register(String toolKey, ShortMessageCommand command);

	/**
	 * Clears all commands for tool
	 * 
	 * @param toolKey
	 */
	public void clearCommands(String toolKey);

	/**
	 * Process a {@link ParsedMessage} Finds and executes registered
	 * {@link ShortMessageCommand}
	 * 
	 * Try to find a tool command that closest match the command as entered my
	 * the sender. Go through the hash map of commands for the specific sakai
	 * tool and find the best match using our string match algorithm. The idea
	 * is based on http://en.wikipedia.org/wiki/Levenshtein_distance. If no
	 * single command is found then we may send back a list of possible commands
	 * to the user.
	 * 
	 * @param message
	 *            the incoming message received
	 */
	public ParsedMessage process(String smsMessagebody, String mobileNr);

	/**
	 * Check if command is valid for tool
	 * 
	 * @param command
	 *            the command
	 * @return true if command is valid, false if not
	 */
	public boolean isValidCommand(String command);

	/**
	 * Generate help message for a user when the command is unmatched or ambiguous
	 * 
	 * @param toolKey
	 *            tool to generate message for
	 * @param the locale
	 * @return the assist message
	 */
	public String generateAssistMessage(String command, List<String> matches, Locale locale);

	/**
	 * Return the closest matching string in the values array. So the command
	 * HELP can be given as H, HE or HELP but not ELP
	 * 
	 * @param valueToMatch
	 * @param values
	 * @return
	 */
	public SmsPatternSearchResult getClosestMatch(String valueToMatch,
			List<String> values);
	
	/**
	 * Get all the commands registered with the gateway
	 * @return a map containing the command and its tool
	 */
	public List<ShortMessageCommand> getAllCommands();

}
