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
 * A exception class that indicates an account does not have enough credits for a particular action
 */
public class SmsInsufficientCreditsException extends Exception{

	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a account not found exception.
	 */
	public SmsInsufficientCreditsException() {
	}

	/**
	 * Instantiates a account not found exception.
	 * 
	 * @param msg
	 *            the msg
	 */
	public SmsInsufficientCreditsException(String msg) {
		super(msg);
	}

	/**
	 * Instantiates a account not found exception.
	 * 
	 * @param e
	 *            the exception  
	 */
	public SmsInsufficientCreditsException(Exception e) {
		super(e);
	}

	
	
}
