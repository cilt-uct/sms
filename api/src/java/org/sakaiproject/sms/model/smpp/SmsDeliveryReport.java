/***********************************************************************************
 * SmsDeliveryReport.java
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
package org.sakaiproject.sms.model.smpp;

/**
 * The delivery report from the gateway. This will be matched up with the
 * original outgoing message.
 * 
 * @author louis@psybergate.com
 * @version 1.0
 * @created 12-Nov-2008
 */
public class SmsDeliveryReport {

	// One of the above constants
	private int deliveryStatus;

	// The unique ID of the message
	private long kMessageID;

	private String smscID;

	public int getDeliveryStatus() {
		return deliveryStatus;
	}

	public long getKMessageID() {
		return kMessageID;
	}

	public String getSmscID() {
		return smscID;
	}

	public void setDeliveryStatus(int deliveryStatus) {
		this.deliveryStatus = deliveryStatus;
	}

	public void setKMessageID(long messageID) {
		kMessageID = messageID;
	}

	public void setSmscID(String smscID) {
		this.smscID = smscID;
	}

}
