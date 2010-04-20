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
package org.sakaiproject.sms.logic.exception;

/**
 * General search exception that will be thrown when hibernate search criteria
 * objects with invalid values of some sort.
 * 
 * @author julian@psybergate.com
 * @version 1.0
 * @created 01-Dec-2008
 */
public class SmsSearchException extends Exception {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new sms search exception.
	 */
	public SmsSearchException() {
	}

	/**
	 * Instantiates a new sms search exception.
	 * 
	 * @param msg
	 *            the msg
	 */
	public SmsSearchException(String msg) {
		super(msg);
	}

	/**
	 * Instantiates a new sms search exception.
	 * 
	 * @param e
	 *            the e
	 */
	public SmsSearchException(Exception e) {
		super(e);
	}
}
