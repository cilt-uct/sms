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

import java.util.Map;


/**
 *	Interface for tools to implement to register commands for incoming messages
 *  Registered with {@link SmsIncomingLogicManager}
 */
public interface IncomingSmsLogic {

	/**
	 * The key to identify commands for this tool
	 *
	 * @return array of {@link String} values
	 */
	public String[] getCommandKeys();
	
	/**
	 * Return a map of aliases in alias-command pairs
	 * 
	 * @return
	 */
	public Map<String, String> getAliases();
	
	 /**
	  * Method to run when incoming SMS is matched with this tool
	  *
	  * @param command
	  * @param siteId
	  * @param userId
	  * @param body
	  * 
	  * @return message to reply
	  */
	public String execute(String command, String siteId, String userId, String body);
}
