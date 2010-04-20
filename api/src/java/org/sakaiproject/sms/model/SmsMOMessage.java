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

package org.sakaiproject.sms.model;

public class SmsMOMessage {

	/** The message body */
	private String smsMessagebody;
	
	/** The mobile number from which this message originated */
	private String mobileNumber;

	/**	A unique id for the specific gateway from which this message originated */
	private String smscId;
	
	public String getSmsMessagebody() {
		return smsMessagebody;
	}

	public void setSmsMessagebody(String smsMessagebody) {
		this.smsMessagebody = smsMessagebody;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public void setSmscId(String smscId) {
		this.smscId = smscId;
	}

	public String getSmscId() {
		return smscId;
	}

}
