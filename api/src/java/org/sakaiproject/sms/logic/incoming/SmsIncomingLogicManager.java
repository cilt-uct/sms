/***********************************************************************************
 * SmsIncomingLogicManager.java
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
package org.sakaiproject.sms.logic.incoming;

import java.util.ArrayList;

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
	 *            {@link SmsCommand} to register for tool
	 */
	public void register(String toolKey, SmsCommand command);

	/**
	 * Clears all commands for tool
	 * 
	 * @param toolKey
	 */
	public void clearCommands(String toolKey);

	/**
	 * Process a {@link ParsedMessage} Finds and executes registered
	 * {@link SmsCommand}
	 * 
	 * Try to find a tool command that closest match the command as entered my
	 * the sender. Go through the hash map of commands for the specific sakai
	 * tool and find the best match using our string match algorithm. The idea
	 * is based on http://en.wikipedia.org/wiki/Levenshtein_distance. If no
	 * single command is found then we may send back a list of possible commands
	 * to the user.
	 * 
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
	 * Generate help message for a user then sent an invalid command to us.
	 * 
	 * @param toolKey
	 *            tool to generate message for
	 * @return the assist message
	 */
	public String generateAssistMessage(ArrayList<String> matches);

	/**
	 * Genereate help message for specific tool
	 * 
	 * @param tool
	 * @return
	 */
	public String generateAssistMessage(String tool);

	/**
	 * Return the closest matching string in the values array. So the command
	 * HELP can be given as H, HE or HELP but not ELP
	 * 
	 * @param valueToMatch
	 * @param values
	 * @return
	 */
	public SmsPatternSearchResult getClosestMatch(String valueToMatch,
			String[] values);

}
