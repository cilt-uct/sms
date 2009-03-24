/***********************************************************************************
 * SmsCommand.java
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
 * Interface for incoming SMS commands to be implemented by specific tool
 */
public interface SmsCommand {

	/**
	 * Keyword for command
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
	 * Execute method to run when incoming SMS matches command
	 * 
	 * @param siteId
	 * @param userId
	 * @param body
	 * @return
	 */
	String execute(String siteId, String userId, String body);

	/**
	 * Help message to use when no parameters are given
	 * 
	 * @return
	 */
	String getHelpMessage();
}
