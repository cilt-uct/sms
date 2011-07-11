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

/**
 * Interface for incoming short message commands to be implemented by specific tool
 */
public interface ShortMessageCommand {

	public static final String MESSAGE_TYPE_SMS = "sms";
	public static final String MESSAGE_TYPE_XMPP = "xmpp";
	public static final String MESSAGE_TYPE_TWITTER = "twitter";

	/**
	 * Keyword for command. The command keyword must begin with a letter or digit
	 * (as defined by Character.isLetterOrDigit). For example "ABC" and "2XY" are valid commands,
	 * but "+pqr" is not. 
	 * 
	 * @return
	 */
	String getCommandKey();

	/**
	 * Valid aliases for command
	 * 
	 * @return
	 */
	String[] getAliases();

	/**
	 * Execute method to run when incoming message matches command. The method will be called
	 * within an appropriate security context: if the user is anonymous, with a security advisor
	 * allowing "site.visit" for the site, if the user is identified, a session for the user.
	 * Implementations should nevertheless be cautious as to which operations are permitted,
	 * as the sourceAddress may be the only method of authentication.
	 * 
	 * @param message
	 * @param messageType The type of message
	 * @param sourceAddress A source address of the appropriate type for messageType
	 * @return
	 */
	String execute(ParsedMessage message, String messageType, String sourceAddress);

	/**
	 * Can the message be executed? Can be used to verify permissions to resolve ambiguous
	 * user:site matches, for example. If in doubt, return true.
	 */
	boolean canExecute(ParsedMessage message);
	
	/**
	 * Help message to use when no parameters are given for the given message type
	 * 
	 * @return
	 */
	String getHelpMessage(String messageType);

	/**
	 * Return the number of parameters expected in the body, excluding the command and site
	 * 
	 * @return
	 */
	int getBodyParameterCount();

	/**
	 * Returns if the command is enabled.
	 * 
	 * @return
	 */
	boolean isEnabled();
	
	/**
	 * Specifies if the command must be on help list or return help message
	 * 
	 * @return
	 */
	boolean isVisible();
	
	/**
	 * Does this command require a siteId to be specified?
	 * @return
	 */
	boolean requiresSiteId();
	
	/**
	 * Does this command require a userId to be specified? If true and requiresSiteId() is true,
	 * then it implies that the user must also be able to visit the site. Set this to false for
	 * commands which allow anonymous posting or information retrieval depending on tool configuration.
	 * @return
	 */
	boolean requiresUserId();

}
