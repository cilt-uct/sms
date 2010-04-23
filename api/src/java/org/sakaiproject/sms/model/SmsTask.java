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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.sakaiproject.sms.model.constants.SmsConst_DeliveryStatus;
import org.sakaiproject.sms.util.DateUtil;

/**
 * A sms task that needs to be processed. For example: send message X to sakai
 * group Y at time Z. When a sms task is processed, a record is inserted into
 * SMS_MESSAGE for each message that must be sent out.
 * 
 * @author Julian Wyngaard
 * @version 1.0
 * @created 19-Nov-2008
 */
public class SmsTask extends BaseModel {

	/** Serial number */
	private static final long serialVersionUID = 1L;

	/** Maximum length of FAIL_REASON field in database */
	private static final int MAX_FAIL_LEN = 200;
	
	/**
	 * Type of task, SO (system originating) and MO (mobile originating).
	 */
	private Integer messageTypeId;

	// Fields relate to size estimates, credit usage and billing
	
	/** The sms account (cost centre) that will pay for the messages. */
	private Long smsAccountId;

	/** The estimated Sakai group size. Calculated when the task is created. */
	private Integer groupSizeEstimate;

	/** The actual Sakai group size. Calculated when the task is processed. */
	private Integer groupSizeActual = null;

	/**
	 * Cost of a credit at time of task creation
	 */
	private double creditCost;

	/**
	 * Approximate number of credits for this task as calculated when the task is
	 * created. The exact credits can only be calculated when the task is
	 * processed and this might happen in the future.
	 */
	private double creditEstimate;

	/**
	 * The number of credits billed to the account for this task. Used when task is COMPLETE.
	 */
	private double creditsBilled;

	/**
	 * Actual number of credits used for this task.
	 */
	private double creditsActual;

	/**
	 * The total number of smsMessages delivered. That is the messages that was
	 * reported as DELIVERED by the SMPP gateway.
	 */
	private int messagesDelivered;

	/**
	 * The total number of smsMessages processed. This is the meesages that was
	 * send to the SMPP gateway.
	 */
	private int messagesProcessed;
	
	// Date/time fields
	
	/** The date-time when the task was successfully create. */
	private Date dateCreated;

	/**
	 * The date-time when the task was last processed. It might be processed a
	 * few times until successful or until the attempt count reaches a
	 * predefined maximum..
	 */
	private Date dateProcessed;

	/**
	 * Post dated for future delivery. If this date is equal to the current time
	 * or null, the message will be processed immediately, if possible.
	 */
	private Date dateToSend;

	/**
	 * The date the task will expire on.It is calculated using maxTimeToLive.
	 */
	private Date dateToExpire;

	// Status information

	/** Current status of this task. See SmsConst_TaskDeliveryStatus */
	private String statusCode;

	/**
	 * Number of delivery attempts until the task is marked as failed. The
	 * processing of the task will fail when the gateway is down or the line is
	 * down.
	 */
	private Integer attemptCount;
	
	/**
	 * The time at which the next attempt to proccess the message shoudl occur
	 */
	private Date nextRetryTime;


	/**
	 * The maximum amount of minutes to allow this task to be pending since it
	 * date-to-deliver. Some tasks like announcements are time critical and is
	 * not relevant when it is sent out too late.
	 */
	private Integer maxTimeToLive;

	/** The reason for a task failing */
	private String failReason;

	// Sender info

	/** The sakai site from where the sms task originated. */
	private String sakaiSiteId;

	/** The sakai tool id from where the sms task originated. */
	private String sakaiToolId;

	/** The sakai tool name from where the sms task originated. */
	private String sakaiToolName;

	/** The Sakai user name of the sender. */
	private String senderUserName;

	/** The sender user id. */
	private String senderUserId;

	// Recipient info
	
	/** The Sakai group who will receive the message, empty if not applicable. */
	private String deliveryGroupId;

	/** The friendly name of the Sakai group. */
	private String deliveryGroupName;

	/** Used for incoming messages. */
	private String deliveryUserId;

	/** List of recipient user ids */
	private Set<String> sakaiUserIdsList = new HashSet<String>();

	/** Comma-separated list of recipient ids in a string */
	private String sakaiUserIds;

	/**
	 * A comma separated list of mobile numbers the internal representation
	 */
	private String deliveryMobileNumbers;

	/**
	 * A comma separated list of delivery group ids
	 */
	private String deliveryEntities;

	/**
	 * The sms messages for this task. This will be generated when the task is
	 * processed.
	 */
	private transient Set<SmsMessage> smsMessages = new HashSet<SmsMessage>();

	// Message content

	/** The message body. Already validated for character set, length etc. */
	private String messageBody;

	/** The message reply body. Already validated for character set, length etc. */
	private String messageReplyBody;

	// Methods
	
	public Set<String> getSakaiUserIdsList() {

		if (sakaiUserIds == null) {
			sakaiUserIdsList.clear();
			return sakaiUserIdsList;
		}
		final StringTokenizer stringTokenizer = new StringTokenizer(
				sakaiUserIds, ",");

		while (stringTokenizer.hasMoreTokens()) {
			sakaiUserIdsList.add(stringTokenizer.nextToken());
		}
		return sakaiUserIdsList;
	}

	public void setSakaiUserIdsList(Set<String> sakaiUserIdsList) {
		if (sakaiUserIdsList != null) {
			final StringBuffer buffer = new StringBuffer();
			int number = 1;
			for (String deliveryIds : sakaiUserIdsList) {

				buffer.append(deliveryIds);
				if (number < sakaiUserIdsList.size()) {
					buffer.append(',');
				}
				number++;
			}
			sakaiUserIds = buffer.toString();
		}
	}

	public String getSakaiUserIds() {
		return sakaiUserIds;
	}

	public void setSakaiUserIds(String sakaiUserIds) {
		this.sakaiUserIds = sakaiUserIds;
	}

	public double getCreditCost() {
		return creditCost;
	}

	public void setCreditCost(double creditCost) {
		this.creditCost = creditCost;
	}

	public int getMessagesProcessed() {
		return messagesProcessed;
	}

	public void setMessagesProcessed(int messagesProcessed) {
		this.messagesProcessed = messagesProcessed;
	}

	/**
	 * Gets the date the task will expire on.
	 * 
	 * @return
	 */
	public Date getDateToExpire() {
		return dateToExpire;
	}

	/**
	 * Sets the date the task will expire on.
	 * 
	 * @param dateToExpire
	 */
	public void setDateToExpire(Date dateToExpire) {
		this.dateToExpire = DateUtil.getUsableDate(dateToExpire);
	}

	/**
	 * Instantiates a new sms task.
	 */
	public SmsTask() {
		super();
		this.statusCode = SmsConst_DeliveryStatus.STATUS_PENDING;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, false, SmsTask.class);

	}

	/**
	 * Gets the attempt count.
	 * 
	 * @return the attempt count
	 */
	public Integer getAttemptCount() {
		return attemptCount;
	}

	/**
	 * Get the next time the task should be attempted to deliver
	 * @return
	 */
	public Date getNextRetryTime() {
		return nextRetryTime;
	}

	/**
	 * Get the next time the task should be attempted to deliver
	 * @return
	 */
	public void setNextRetryTime(Date nextRetryTime) {
		this.nextRetryTime = nextRetryTime;
	}

	/**
	 * Return an estimate of the cost for this task, rounded to 2 decimal places
	 * @return Estimated cost
	 */
	public double getCostEstimate() {
		return Math.round(creditCost * creditEstimate * 100) * 0.01;
	}

	/**
	 * Gets the credit estimate.
	 * 
	 * @return the credit estimate
	 */
	public double getCreditEstimate() {
		return creditEstimate;
	}

	/**
	 * Gets the date created.
	 * 
	 * @return the date created
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * Gets the date processed.
	 * 
	 * @return the date processed
	 */
	public Date getDateProcessed() {
		return dateProcessed;
	}

	/**
	 * Gets the date to send. Will return the year as 2200 if this is a draft
	 * task.
	 * 
	 * @return the date to send
	 */
	public Date getDateToSend() {
		return dateToSend;
	}

	/**
	 * Gets the delivery group id.
	 * 
	 * @return the delivery group id
	 */
	public String getDeliveryGroupId() {
		return deliveryGroupId;
	}

	/**
	 * Gets the delivery group name.
	 * 
	 * @return the delivery group name
	 */
	public String getDeliveryGroupName() {
		return deliveryGroupName;
	}

	/**
	 * Gets the delivery user id.
	 * 
	 * @return the delivery user id
	 */
	public String getDeliveryUserId() {
		return deliveryUserId;
	}

	/**
	 * Gets the actual group size (set when delivery first starts).
	 * 
	 * @return the number of recipients, or null if delivery has not yet started.
	 */
	public Integer getGroupSizeActual() {
		return groupSizeActual;
	}

	/**
	 * Gets the group size estimate.
	 * 
	 * @return the group size estimate
	 */
	public Integer getGroupSizeEstimate() {
		return groupSizeEstimate;
	}

	/**
	 * Gets the max time to live.
	 * <p>
	 * NB: This is in minutes
	 * 
	 * @return the max time to live
	 */
	public Integer getMaxTimeToLive() {
		return maxTimeToLive;
	}

	/**
	 * Get the message body. Will return '-------' if this is a draft task.
	 * 
	 * @return the message body
	 */
	public String getMessageBody() {
		return messageBody;
	}

	/**
	 * Gets the messages with smsc status.
	 * 
	 * @param smscStatus
	 *            the smsc status
	 * 
	 * @return the messages with smsc status
	 */
	public Set<SmsMessage> getMessagesWithSmscStatus(int smscStatus) {
		final Set<SmsMessage> filtered = new HashSet<SmsMessage>();
		if (smsMessages != null) {
			for (SmsMessage message : smsMessages) {
				if (message.getSmscDeliveryStatusCode() != null
						&& message.getSmscDeliveryStatusCode().equals(
								smscStatus)) {
					filtered.add(message);
				}
			}
		}
		return filtered;

	}

	/**
	 * Gets the messages with status.
	 * 
	 * @param status
	 *            the status
	 * 
	 * @return the messages with status
	 */
	public Set<SmsMessage> getMessagesWithStatus(String status) {
		final Set<SmsMessage> filtered = new HashSet<SmsMessage>();
		if (smsMessages != null) {
			for (SmsMessage message : smsMessages) {
				if (message.getStatusCode().equals(status)) {
					filtered.add(message);
				}
			}
		}
		return filtered;
	}

	/**
	 * Gets the message type id.
	 * 
	 * @return the message type id
	 */
	public Integer getMessageTypeId() {
		return messageTypeId;
	}

	/**
	 * Gets the sakai site id.
	 * 
	 * @return the sakai site id
	 */
	public String getSakaiSiteId() {
		return sakaiSiteId;
	}

	/**
	 * Gets the sakai tool id.
	 * 
	 * @return the sakai tool id
	 */
	public String getSakaiToolId() {
		return sakaiToolId;
	}

	/**
	 * Gets the sakai tool name.
	 * 
	 * @return the sakai tool name
	 */
	public String getSakaiToolName() {
		return sakaiToolName;
	}

	/**
	 * Gets the sender's Sakai display name.
	 * 
	 * @return the sender user name
	 */
	public String getSenderUserName() {
		return senderUserName;
	}

	/**
	 * Gets the sms account id.
	 * 
	 * @return the sms account id
	 */
	public Long getSmsAccountId() {
		return smsAccountId;
	}

	/**
	 * Gets the sms messages.
	 * 
	 * @return the sms messages
	 */
	public Set<SmsMessage> getSmsMessages() {
		return smsMessages;
	}

	/**
	 * Gets the status code.
	 * 
	 * @return the status code
	 */
	public String getStatusCode() {
		return statusCode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(17, 37, this, false,
				SmsTask.class);
	}

	/**
	 * Reschedules the date to send the message.
	 * 
	 * @param dateToSend
	 *            the date to send
	 * @deprecated rather set a retry time to resend as this will reset the UI scheduling
	 */
	public void rescheduleDateToSend(Date dateToSend) {
		this.setDateToSend(dateToSend);
	}

	/**
	 * Sets the attempt count.
	 * 
	 * @param attemptCount
	 *            the new attempt count
	 */
	public void setAttemptCount(Integer attemptCount) {
		this.attemptCount = attemptCount;
	}

	/**
	 * Sets the credit estimate.
	 * 
	 * @param creditEstimate
	 *            the new credit estimate
	 */
	public void setCreditEstimate(double creditEstimate) {
		this.creditEstimate = creditEstimate;
	}

	/**
	 * Sets the date created.
	 * 
	 * @param dateCreated
	 *            the new date created
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = DateUtil.getUsableDate(dateCreated);
	}

	/**
	 * Sets the date processed.
	 * 
	 * @param dateProcessed
	 *            the new date processed
	 */
	public void setDateProcessed(Date dateProcessed) {
		this.dateProcessed = DateUtil.getUsableDate(dateProcessed);
	}

	/**
	 * Sets the date to send.
	 * 
	 * @param dateToSend
	 *            the new date to send
	 */
	public void setDateToSend(Date dateToSend) {
		this.dateToSend = DateUtil.getUsableDate(dateToSend);
	}

	/**
	 * Sets the delivery group id.
	 * 
	 * @param deliveryGroupId
	 *            the new delivery group id
	 */
	public void setDeliveryGroupId(String deliveryGroupId) {
		this.deliveryGroupId = deliveryGroupId;
	}

	/**
	 * Sets the delivery group name.
	 * 
	 * @param deliveryGroupName
	 *            the new delivery group name
	 */
	public void setDeliveryGroupName(String deliveryGroupName) {
		this.deliveryGroupName = deliveryGroupName;
	}

	/**
	 * Sets the delivery user id.
	 * 
	 * @param deliveryUserId
	 *            the new delivery user id
	 */
	public void setDeliveryUserId(String deliveryUserId) {
		this.deliveryUserId = deliveryUserId;
	}

	/**
	 * Sets the group size actual.
	 * 
	 * @param groupSizeActual
	 *            the new group size actual
	 */
	public void setGroupSizeActual(Integer groupSizeActual) {
		this.groupSizeActual = groupSizeActual;
	}

	/**
	 * Sets the group size estimate.
	 * 
	 * @param groupSizeEstimate
	 *            the new group size estimate
	 */
	public void setGroupSizeEstimate(Integer groupSizeEstimate) {
		this.groupSizeEstimate = groupSizeEstimate;
	}

	/**
	 * Sets the max time to live.
	 * <p>
	 * NB: This is in minutes
	 * 
	 * @param maxTimeToLive
	 *            the new max time to live
	 */
	public void setMaxTimeToLive(Integer maxTimeToLive) {
		this.maxTimeToLive = maxTimeToLive;
	}

	/**
	 * Sets the message body.
	 * 
	 * @param messageBody
	 *            the new message body
	 */
	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}

	/**
	 * Sets the message type id.
	 * 
	 * @param messageTypeId
	 *            the new message type id
	 */
	public void setMessageTypeId(Integer messageTypeId) {
		this.messageTypeId = messageTypeId;
	}

	/**
	 * Sets the sakai site id.
	 * 
	 * @param sakaiSiteId
	 *            the new sakai site id
	 */
	public void setSakaiSiteId(String sakaiSiteId) {
		this.sakaiSiteId = sakaiSiteId;
	}

	/**
	 * Sets the sakai tool id.
	 * 
	 * @param sakaiToolId
	 *            the new sakai tool id
	 */
	public void setSakaiToolId(String sakaiToolId) {
		this.sakaiToolId = sakaiToolId;
	}

	/**
	 * Sets the sakai tool name.
	 * 
	 * @param sakaiToolName
	 *            the new sakai tool name
	 */
	public void setSakaiToolName(String sakaiToolName) {
		this.sakaiToolName = sakaiToolName;
	}

	/**
	 * Sets the sender user name.
	 * 
	 * @param senderUserName
	 *            the new sender user name
	 */
	public void setSenderUserName(String senderUserName) {
		this.senderUserName = senderUserName;
	}

	/**
	 * Sets the sms account id.
	 * 
	 * @param smsAccountId
	 *            the new sms account id
	 */
	public void setSmsAccountId(Long smsAccountId) {
		this.smsAccountId = smsAccountId;
	}

	/**
	 * Sets the sms messages.
	 * 
	 * @param smsMessages
	 *            the new sms messages
	 */
	public void setSmsMessages(Set<SmsMessage> smsMessages) {
		this.smsMessages = smsMessages;
	}

	/**
	 * Sets the sms messages on a this SmsTask object.
	 * 
	 * @param smsMessages
	 *            the sms messages
	 */
	public void setSmsMessagesOnTask(Set<SmsMessage> smsMessages) {
		if (smsMessages != null) {
			for (SmsMessage message : smsMessages) {
				this.smsMessages.add(message);
			}
		}
	}

	/**
	 * Sets the status code.
	 * 
	 * @param statusCode
	 *            the new status code
	 */
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	/**
	 * Get the delivery mobile numbers internal method for hibernate
	 * 
	 * @return
	 */
	String getDeliveryMobileNumbers() {
		return deliveryMobileNumbers;
	}

	/**
	 * Sets the delivery mobile numbers internal method for hibernate
	 * 
	 * @param deliveryMobileNumbers
	 */
	void setDeliveryMobileNumbers(String deliveryMobileNumbers) {
		this.deliveryMobileNumbers = deliveryMobileNumbers;
	}

	/**
	 * @return A set of delivery mobile numbers
	 */
	public Set<String> getDeliveryMobileNumbersSet() {
		if (deliveryMobileNumbers == null) {
			return null;
		}

		final Set<String> deliveryMobileNumbersSet = new HashSet<String>();
		final StringTokenizer stringTokenizer = new StringTokenizer(
				deliveryMobileNumbers, ",");

		while (stringTokenizer.hasMoreTokens()) {
			deliveryMobileNumbersSet.add(stringTokenizer.nextToken());
		}

		return deliveryMobileNumbersSet;
	}

	/**
	 * set the mobile numbers set
	 */
	public void setDeliveryMobileNumbersSet(Set<String> mobileNumbers) {
		if (mobileNumbers != null) {
			final StringBuffer buffer = new StringBuffer();
			int number = 1;
			for (String mobileNumber : mobileNumbers) {

				buffer.append(mobileNumber);
				if (number < mobileNumbers.size()) {
					buffer.append(',');
				}
				number++;
			}
			
			deliveryMobileNumbers = buffer.toString();
		}
	}

	/**
	 * Get the delivery group IDs internal method for hibernate
	 */
	String getDeliveryEntities() {
		return deliveryEntities;
	}

	/**
	 * Set the delivery group IDs internal method for hibernate
	 */
	void setDeliveryEntities(String deliveryEntites) {
		this.deliveryEntities = deliveryEntites;
	}

	/**
	 * Set the delivery group IDs
	 */
	public void setDeliveryEntityList(List<String> deliveryEntityList) {
		if (deliveryEntityList != null) {
			final StringBuffer buffer = new StringBuffer();
			int number = 1;
			for (String deliveryIds : deliveryEntityList) {

				buffer.append(deliveryIds);
				if (number < deliveryEntityList.size()) {
					buffer.append(',');
				}
				number++;
			}
			deliveryEntities = buffer.toString();
		}
	}

	/**
	 * Get the delivery group IDs
	 */
	public List<String> getDeliveryEntityList() {
		if (deliveryEntities == null) {
			return null;
		}
		final List<String> deliveryEntityList = new ArrayList<String>();
		final StringTokenizer stringTokenizer = new StringTokenizer(
				deliveryEntities, ",");

		while (stringTokenizer.hasMoreTokens()) {
			deliveryEntityList.add(stringTokenizer.nextToken());
		}
		return deliveryEntityList;
	}

	/**
	 * Sets the fail reason.
	 * 
	 * @param failReason
	 *            the new fail reason
	 */
	public void setFailReason(String failReason) {
		
		if (failReason != null && failReason.length() > MAX_FAIL_LEN) {
			this.failReason = failReason.substring(0, MAX_FAIL_LEN);			
		} else {
			this.failReason = failReason;
		}
	}

	/**
	 * Gets the fail reason.
	 * 
	 * @return the fail reason
	 */
	public String getFailReason() {
		return failReason;
	}

	/**
	 * Gets the sender user id.
	 * 
	 * @return the sender user id
	 */
	public String getSenderUserId() {
		return senderUserId;
	}

	/**
	 * Sets the sender user id.
	 * 
	 * @param senderUserId
	 *            the new sender user id
	 */
	public void setSenderUserId(String senderUserId) {
		this.senderUserId = senderUserId;
	}

	/**
	 *Gets the messages delivered on the task
	 * 
	 * @return
	 */
	public int getMessagesDelivered() {
		return messagesDelivered;
	}

	/**
	 * Sets the messages delivered on the task
	 * 
	 * @param messagesDelivered
	 */
	public void setMessagesDelivered(int messagesDelivered) {
		this.messagesDelivered = messagesDelivered;
	}

	public String getMessageReplyBody() {
		return messageReplyBody;
	}

	public void setMessageReplyBody(String messageReplyBody) {
		this.messageReplyBody = messageReplyBody;
	}

	public void setBilledCredits(double billedCredits) {
		this.creditsBilled = billedCredits;
	}

	public double getBilledCredits() {
		return creditsBilled;
	}

	public void setCreditsActual(double creditsActual) {
		this.creditsActual = creditsActual;
	}

	public double getCreditsActual() {
		return creditsActual;
	}
}
