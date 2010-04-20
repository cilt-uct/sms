/***********************************************************************************
 * SmsTestActionBean.java
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

package org.sakaiproject.sms.tool.beans;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.logic.smpp.SmsSmpp;
import org.sakaiproject.sms.model.SmsMessage;
import org.sakaiproject.sms.tool.otp.SmsMessageLocator;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

/**
 * The Class SmsTestActionBean.
 */
public class SmsTestActionBean {

	/** The log. */
	private static final Log LOG = LogFactory.getLog(SmsTestActionBean.class);

	/** The sms message locator. */
	private SmsMessageLocator smsMessageLocator;

	/** The sms smpp. */
	private SmsSmpp smsSmpp;

	/** The targetted message list. */
	private TargettedMessageList messages;

	/**
	 * Retrieve the new SmsMessage from the sms message locator Send message to
	 * gateway.
	 * 
	 * @return The ActionResult
	 */
	public String send() {
		// Get the new bean created to send
		final SmsMessage msg = (SmsMessage) smsMessageLocator
				.locateBean(SmsMessageLocator.NEW_1);

		try {
			smsSmpp.sendMessageToGateway(msg);
		} catch (Exception e) {
			// If any exception caught while sending message just give a general
			// send error
			messages.addMessage(new TargettedMessage("sms.errors.send-error",
					null, TargettedMessage.SEVERITY_ERROR));

			LOG.error(e.getMessage(), e);
		}

		return ActionResults.SUCCESS;
	}

	/**
	 * Sets the messages.
	 * 
	 * @param messages
	 *            the new messages
	 */
	public void setMessages(TargettedMessageList messages) {
		this.messages = messages;
	}

	/**
	 * Sets the sms message locator.
	 * 
	 * @param smsMessageLocator
	 *            the new sms message locator
	 */
	public void setSmsMessageLocator(SmsMessageLocator smsMessageLocator) {
		this.smsMessageLocator = smsMessageLocator;
	}

	/**
	 * Sets the sms smpp.
	 * 
	 * @param smsSmpp
	 *            the new sms smpp
	 */
	public void setSmsSmpp(SmsSmpp smsSmpp) {
		this.smsSmpp = smsSmpp;
	}
}
