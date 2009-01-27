/***********************************************************************************
 * SmsTaskValidationException.java
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

import java.util.ArrayList;

/**
 * Exception that will be thrown when there are validation exceptions for and
 * SmsTask object
 * 
 * @author julian@psybergate.com
 * @version 1.0
 * @created 23-Jan-2009
 */
public class SmsTaskValidationException extends Exception {

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

	/**
	 * Instantiates a new sms task validation exception.
	 * 
	 * @param errors
	 *            the errors
	 */
	public SmsTaskValidationException(ArrayList<String> errorMessages,
			String msg) {
		super(msg);
		this.errorMessages = errorMessages;
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

	/**
	 * Gets the error messages.
	 * 
	 * @return the error messages
	 */
	public ArrayList<String> getErrorMessages() {
		return errorMessages;
	}

	/**
	 * Checks for error messages.
	 * 
	 * @return true, if successful
	 */
	public boolean hasErrorMessages() {
		return (errorMessages == null || errorMessages.size() == 0);
	}

}
