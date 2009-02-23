/***********************************************************************************
 * SmsMessageParserImpl.java
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
package org.sakaiproject.sms.logic.smpp.impl;

import org.sakaiproject.sms.logic.smpp.SmsMessageParser;
import org.sakaiproject.sms.model.hibernate.SmsTask;

public class SmsMessageParserImpl implements SmsMessageParser {

	/**
	 * The sakai site id of the tool that will handle the message. We need to
	 * know this to get the billing account.
	 */
	private String sakaiSiteId;

	/**
	 * The sakai user that sent the message. Note: What about non-sakai users ?
	 */
	private String sakaiUserId;

	/**
	 * The sakai user pin. May be null if the user did not supply a pin and the
	 * request is low security.
	 */
	private String sakaiUserPin;
	// 

	/**
	 * The sms task that will be parsed. The task must be set before using the
	 * class
	 */
	private SmsTask smsTask;

	/**
	 * Parses the message general. Try to figure out the sakai site and user.
	 */
	public void parseMessageGeneral() {
		// set sakaiSiteId and sakaiUserId, if not possible to figure out, then
		// these vars must be null
	}

	/**
	 * Check for valid pin, sakai site code, mobile number etc.
	 */
	public void validateMessageGeneral() {
		// Make required calls to our external logic
	}

	public void toolRegisterCommandList(String sakaiToolId,
			String[] validCommands) {
		// Add sakaiToolId and validCommands to a hash map, make sure a second
		// call to this method does not insert duplicates.
	}

	public void toolMatchCommand(String sakaiToolId, String smsCommand) {
	}

	public void toolProcessCommand(String sakaiToolId, String command,
			String commandSuffix) {
		// TODO Auto-generated method stub

	}

	public SmsTask getSmsTask() {
		return smsTask;
	}

	public void setSmsTask(SmsTask smsTask) {
		this.smsTask = smsTask;
	}

	public String getSakaiSiteId() {
		return sakaiSiteId;
	}

	public void setSakaiSiteId(String sakaiSiteId) {
		this.sakaiSiteId = sakaiSiteId;
	}

	public String getSakaiUserId() {
		return sakaiUserId;
	}

	public void setSakaiUserId(String sakaiUserId) {
		this.sakaiUserId = sakaiUserId;
	}

}
