/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/sms/sms/trunk/api/src/java/org/sakaiproject/sms/logic/external/ExternalEmailLogic.java $
 * $Id: ExternalEmailLogic.java 67664 2010-05-17 09:09:14Z david.horwitz@uct.ac.za $
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010 The Sakai Foundation
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

package org.sakaiproject.sms.logic.external;

import java.util.List;
import java.util.Set;

import org.sakaiproject.sms.model.SmsMessage;

/**
 * This is an optional interface to implement to send messages to an external non-smpp
 * gateway
 * @author dhorwitz
 *
 */
public interface ExternalMessageSending {

	/**
	 * Send a list of messages to the external service
	 * @param messages
	 * @return
	 */
	public String sendMessagesToService(Set<SmsMessage> messages);
	
	
	/**
	 * Query the external API to get updated message Statuses and Costs
	 * @param messages
	 */
	public void updateMessageStatuses(List<SmsMessage> messages);
}
