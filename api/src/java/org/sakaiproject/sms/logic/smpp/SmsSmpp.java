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
package org.sakaiproject.sms.logic.smpp;

import java.util.Set;

import org.sakaiproject.sms.model.SmsMessage;

/**
 * Handle all logic regarding SMPP gateway communication.
 * 
 * @author etienne@psybergate.co.za
 * 
 */
public interface SmsSmpp {

	/**
	 * Establish a connection to the gateway (bind). The connection will be kept
	 * open for the lifetime of the session. Concurrent connections will be
	 * possible from other smpp services. The status of the connection will be
	 * checked before sending a message, and a auto-bind will be made if
	 * possible.
	 */

	public boolean connectToGateway();

	/**
	 * Unbind from the gateway. If disconnected, no message sending will be
	 * possible. For unit testing purposes.
	 */
	public void disconnectGateWay();

	/**
	 * Return the status of this connection to the gateway.
	 */
	public boolean getConnectionStatus();

	/**
	 * Get some info about the remote gateway.
	 */
	public String getGatewayInfo();

	/**
	 * Send a list of messages to the gateway. Abort if the gateway connection
	 * is down or gateway returns an error and mark relevant messages as failed.
	 * Return message statuses back to caller.
	 * 
	 * @param messages
	 * @return
	 */
	public String sendMessagesToGateway(Set<SmsMessage> messages);

	/**
	 * Send one message to the SMS gateway. Return result code to caller.
	 * 
	 * @param message
	 * @return
	 */
	public SmsMessage sendMessageToGateway(SmsMessage message);

}
