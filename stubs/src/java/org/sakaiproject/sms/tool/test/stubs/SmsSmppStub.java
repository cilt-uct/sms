/***********************************************************************************
 * SmsSmppStub.java
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
package org.sakaiproject.sms.tool.test.stubs;

import java.util.Set;

import org.sakaiproject.sms.logic.smpp.SmsSmpp;
import org.sakaiproject.sms.model.SmsMessage;

/**
 * The Class SmsSmppStub. Stub implementation of {@link SmsSmpp} with minimal
 * implementation, used for testing
 */
public class SmsSmppStub implements SmsSmpp {

	/**
	 * Public field to force RunTimeException from method
	 */
	public boolean forceException;

	/**
	 * Constant to set debug Info to if called
	 */
	public static final String CALLED = "called";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.sms.api.SmsSmpp#connectToGateway()
	 */
	public boolean connectToGateway() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.sms.api.SmsSmpp#disconnectGateWay()
	 */
	public void disconnectGateWay() {
	}

	public void enableDebugInformation(boolean arg0) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.sms.api.SmsSmpp#getConnectionStatus()
	 */
	public boolean getConnectionStatus() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.sms.api.SmsSmpp#getGatewayInfo()
	 */
	public String getGatewayInfo() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.sms.api.SmsSmpp#processMessageRemotely()
	 */
	public void processMessageRemotely() {
	}

	public String sendMessagesToGateway(Set<SmsMessage> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sakaiproject.sms.api.SmsSmpp#sendMessagesToGateway(org.sakaiproject
	 * .sms.hibernate.model.SmsMessage[])
	 */
	public SmsMessage[] sendMessagesToGateway(SmsMessage[] arg0) {
		return new SmsMessage[] {};
	}

	/**
	 * Test method that sets debugInfo as "called"
	 * 
	 * @see org.sakaiproject.sms.logic.smpp.SmsSmpp#sendMessageToGateway(org.sakaiproject.sms.model.hibernate.model.SmsMessage)
	 */
	public SmsMessage sendMessageToGateway(SmsMessage msg) {
		msg.setDebugInfo(CALLED);

		if (forceException) {
			throw new RuntimeException();
		}

		return msg;
	}

	public boolean notifyDeliveryReportRemotely(SmsMessage arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean processOutgoingMessageRemotely(SmsMessage arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}
