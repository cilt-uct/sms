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
package org.sakaiproject.sms.model.smpp;

/**
 * 
 * @author Etienne@psybergate.co.za
 * 
 */
public class SmsSmppProperties {

	public static final int DEFAULT_TRANSACTION_TIMER_INTERVAL = 30;

	public static final int DEFAULT_SMSC_PORT = 2775;

	public static final int DEFAULT_ENQUIRELINK_TIMEOUT = 60;

	public static final int DEFAULT_BINDTHREAD_TIMER = 5;

	public static final int DEFAULT_SENDING_DELAY = 10;
	

	private byte destAddressNPI;
	private byte destAddressTON;
	private int enquireLinkTimeOut;
	private String SMSCAddress;
	private String SMSCPassword;
	private String SMSCUsername;
	private int SMSCPort;
	private byte priorityFlag;
	private byte protocolId;
	private byte replaceIfPresentFlag;
	private String serviceType;
	private byte smDefaultMsgId;
	private String sourceAddress;
	private byte sourceAddressNPI;
	private byte sourceAddressTON;
	private String systemType;
	private String addressRange;
	private int transactionTimer;
	private int sendingDelay;
	private int bindThreadTimer;
	private boolean bindThisNode = false;
	private String messageEncoding;
	
	//The encoding to use for Messages
	public String getMessageEncoding() {
		return messageEncoding;
	}

	public void setMessageEncoding(String messageEncoding) {
		this.messageEncoding = messageEncoding;
	}

	public boolean isBindThisNode() {
		return bindThisNode;
	}

	public void setBindThisNode(boolean bindThisNode) {
		this.bindThisNode = bindThisNode;
	}

	public byte getDestAddressNPI() {
		return destAddressNPI;
	}

	public void setDestAddressNPI(byte destAddressNPI) {
		this.destAddressNPI = destAddressNPI;
	}

	public byte getDestAddressTON() {
		return destAddressTON;
	}

	public void setDestAddressTON(byte destAddressTON) {
		this.destAddressTON = destAddressTON;
	}

	public int getEnquireLinkTimeOut() {
		return enquireLinkTimeOut;
	}

	public void setEnquireLinkTimeOut(int enquireLinkTimeOut) {
		this.enquireLinkTimeOut = enquireLinkTimeOut;
	}

	public String getSMSCAddress() {
		return SMSCAddress;
	}

	public void setSMSCAddress(String address) {
		SMSCAddress = address;
	}

	public String getSMSCPassword() {
		return SMSCPassword;
	}

	public void setSMSCPassword(String password) {
		SMSCPassword = password;
	}

	public String getSMSCUsername() {
		return SMSCUsername;
	}

	public void setSMSCUsername(String userName) {
		SMSCUsername = userName;
	}

	public int getSMSCPort() {
		return SMSCPort;
	}

	public void setSMSCPort(int port) {
		SMSCPort = port;
	}

	public int getBindThreadTimer() {
		return bindThreadTimer;
	}

	public void setBindThreadTimer(int bindThreadTimer) {
		this.bindThreadTimer = bindThreadTimer;
	}

	public byte getPriorityFlag() {
		return priorityFlag;
	}

	public void setPriorityFlag(byte priorityFlag) {
		this.priorityFlag = priorityFlag;
	}

	public byte getProtocolId() {
		return protocolId;
	}

	public void setProtocolId(byte protocolId) {
		this.protocolId = protocolId;
	}

	public byte getReplaceIfPresentFlag() {
		return replaceIfPresentFlag;
	}

	public void setReplaceIfPresentFlag(byte replaceIfPresentFlag) {
		this.replaceIfPresentFlag = replaceIfPresentFlag;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public byte getSmDefaultMsgId() {
		return smDefaultMsgId;
	}

	public void setSmDefaultMsgId(byte smDefaultMsgId) {
		this.smDefaultMsgId = smDefaultMsgId;
	}

	public String getSourceAddress() {
		return sourceAddress;
	}

	public void setSourceAddress(String sourceAddress) {
		this.sourceAddress = sourceAddress;
	}

	public byte getSourceAddressNPI() {
		return sourceAddressNPI;
	}

	public void setSourceAddressNPI(byte sourceAddressNPI) {
		this.sourceAddressNPI = sourceAddressNPI;
	}

	public byte getSourceAddressTON() {
		return sourceAddressTON;
	}

	public void setSourceAddressTON(byte sourceAddressTON) {
		this.sourceAddressTON = sourceAddressTON;
	}

	public String getSystemType() {
		return systemType;
	}

	public void setSystemType(String systemType) {
		this.systemType = systemType;
	}

	public String getAddressRange() {
		return addressRange;
	}

	public void setAddressRange(String addressRange) {
		this.addressRange = addressRange;
	}

	public int getTransactionTimer() {
		return transactionTimer;
	}

	public void setTransactionTimer(int transactionTimer) {
		this.transactionTimer = transactionTimer;
	}

	public int getSendingDelay() {
		return sendingDelay;
	}

	public void setSendingDelay(int sendingDelay) {
		this.sendingDelay = sendingDelay;
	}
}
