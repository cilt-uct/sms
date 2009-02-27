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

/**
 * Registers {@link IncomingSmsLogic} to process incoming messages
 */
public interface SmsIncomingLogicManager {
	
	/**
	 * Registers a {@link IncomingSmsLogic} for specified tool
	 * 
	 * @param toolKey unique key for tool
	 * @param logic {@link IncomingSmsLogic} to register for tool
	 */
	public void register(String toolKey, IncomingSmsLogic logic);
	
	/**
	 * Process a {@link ParsedMessage}
	 * Finds and executes registered {@link IncomingSmsLogic}
	 * 
	 * Try to find a tool command that closest match the command as entered my
	 * the sender. Go through the hash map of commands for the specific sakai
	 * tool and find the best match using Levenshtein distance. See
	 * http://en.wikipedia.org/wiki/Levenshtein_distance. User might want to
	 * plug in another algorithm like soundex. Of no single command is closest
	 * then we must send back a list of possible command to the user.
	 * 
	 * 
	 * @param message the incoming message received
	 */
	public String process(ParsedMessage message);
	
	/**
	 * Check if command is valid for tool
	 * (Does not do closest match)
	 * 
	 * @param toolKey tool for command
	 * @param command the command
	 * @return true if command is valid, false if not
	 */
	public boolean isValidCommand(String toolKey, String command);
	
	/**
	 * Generate help message for a registered tool
	 * 
	 * @param toolKey tool to generate message for
	 * @return the assist message
	 */
	public String generateAssistMessage(String toolKey);
	
}
