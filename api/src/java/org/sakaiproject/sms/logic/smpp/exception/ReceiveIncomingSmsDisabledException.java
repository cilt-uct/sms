package org.sakaiproject.sms.logic.smpp.exception;

/***********************************************************************************
 * ReceiveIncomingSmsDisabledException.java
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

import java.util.ArrayList;

import org.sakaiproject.sms.model.hibernate.SmsTask;

/**
 * Exception that will be thrown when the receiving of incoming sms messages is
 * disabled for the site.
 *
 * @author Etienne@psybergate.com
 * @version 1.0
 * @created 5-March-2009
 */
public class ReceiveIncomingSmsDisabledException extends Exception {

	/** The error messages. */
	private ArrayList<String> errorMessages;

	/** The Constant newLineChar. */
	private final static String newLineChar = "\n";

	/** The Constant lineSpace. */
	private final static String lineSpace = " ";

	/** The Constant format. */
	private final static String format = newLineChar + lineSpace;

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	public ReceiveIncomingSmsDisabledException(SmsTask smsTask) {
		super("The receiving of incoming sms messages is disabled for site :"
				+ smsTask.getSakaiSiteId().toString());
	}

	/**
	 * Gets the error messages as a block string.
	 *
	 * @return the error messages as a block string.
	 */
	public String getErrorMessagesAsBlock() {
		StringBuilder sb = new StringBuilder();
		for (String error : errorMessages) {
			sb.append(format).append(error);
		}
		return sb.toString();
	}
}
