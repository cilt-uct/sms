/**********************************************************************************
 * $URL$
 * $Id$
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

public class SMSCommandException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4159234276800538390L;

	
	private int responceCode;
	
	private boolean isTransient;

	
	public SMSCommandException() {
		
	}
	
	
	public SMSCommandException(int code, boolean isTransient) {
		this.isTransient = isTransient;
		this.responceCode = code;
	}	
	
	
	public boolean isTransient() {
		return isTransient;
	}

	public void setTransient(boolean isTransient) {
		this.isTransient = isTransient;
	}
	
}
