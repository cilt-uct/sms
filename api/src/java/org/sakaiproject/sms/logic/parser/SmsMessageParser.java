/***********************************************************************************
 * SmsMessageParser.java
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
package org.sakaiproject.sms.logic.parser;

import org.sakaiproject.sms.logic.parser.exception.ParseException;

/**
 * This Class will handle all validation and parsing of MO(Mobile Originating)
 * messages Tool specific parsing and validation will be done from the tool
 * itself. The first line of the Sms may contain a sakai user pin. A typical
 * command might be: QNA SITE1 POST The message to post
 * 
 * To use this class: (1) instantiate with a SmsTask, (2) call
 * parseMessageGeneral (3) call validateMessageGeneral
 * 
 * 
 * @author wilhelm@psybergate.co.za, louis@psybergate.co.za
 */
public interface SmsMessageParser {

	/**
	 * Parses the text of the message. Try to figure out the sakai site and user.
	 * Usually called from the Sakai Sms service itself.
	 */
	public ParsedMessage parseMessage(String msgText) throws ParseException;

	/**
	 * Check for valid pin, sakai site code, mobile number etc. Usually called
	 * from the Sakai Sms service itself.
	 */
	public void validateMessageGeneral();

	/**
	 * Each sms enabled sakai tool must send over a list of valid commands that
	 * a user may use to talk to it. Tools must call this only once.
	 * 
	 * @param sakaiToolId
	 *            the sakai tool id the makes the request
	 * @param validCommands
	 *            the valid commands for eg. POST READ HELP
	 */
	public void toolRegisterCommands(String sakaiToolId,
			String[] validCommands);

	/**
	 * 
	 * 
	 * Try to find a tool command that closest match the command as entered my
	 * the sender. Go through the hash map of commands for the specific sakai
	 * tool and find the best match using Levenshtein distance. See
	 * http://en.wikipedia.org/wiki/Levenshtein_distance. User might want to
	 * plug in another algorithm like soundex. Of no single command is closest
	 * then we must send back a list of possible command to the user.
	 * 
	 * @param sakaiToolId
	 *            the sakai tool id
	 * @param smsCommand
	 *            the sms command
	 */
	// CHANGED TO JUST RETURN BOOLEAN FOR TESTING AT THE MOMENT
	public boolean toolMatchCommand(String sakaiToolId, String smsCommand);

	
	/**
	 * The specific tool must override this method and use its own logic to
	 * process the command.
	 * 
	 * @param sakaiToolId
	 *            the sakai tool id
	 * @param command
	 *            the command
	 * @param commandSuffix
	 *            the command suffix, the optional string following the command
	 */
	public void toolProcessCommand(String sakaiToolId, String command,
			String commandSuffix);
}
