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

import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.sakaiproject.sms.util.DateUtil;

/**
 * Each Sakai site will have its own set of accounts for billing outgoing
 * messages. A specific user may also have a account.
 *
 * @author Julian Wyngaard
 * @version 1.0
 * @created 19-Nov-2008
 */
public class SmsAccount extends BaseModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The number of credits (possible fractional) available in the account.
	 */
	private double credits;

	/**
	 * The message type, will be incoming (MO) or outgoing (SO), currently only
	 * SO.
	 */
	private String messageTypeCode;

	/** Some accounts will be allowed to have a overdraft limit. */
	private double overdraftLimit;

	/** The account must be linked to either an Sakai site or a Sakai user. */
	private String sakaiSiteId;

	/** The account must be linked to either an Sakai site or a Sakai user. */
	private String sakaiUserId;

	/** The sms transactions. */
	private transient Set<SmsTransaction> smsTransactions = new HashSet<SmsTransaction>();

	/** The account name. */
	private String accountName;

	/**
	 * The date from when this account is active. The default is the date is was
	 * created in database.
	 */
	private Date startdate;

	/**
	 * Date account is closed. End-dated accounts are not listed and cannot be
	 * used, even if they contain funds.
	 */
	private Date enddate;
	
	private String enddateStr;

	/**
	 * Specifies if the sms account is enabled or disabled .If disabled no
	 * message sending is possible.
	 */
	private Boolean accountEnabled;
	
	
	private String ownerId;

	/**
	 * Get the user id of the owner of this account
	 * @return
	 */
	public String getOwnerId() {
		return ownerId;
	}

	/**
	 * Set the userId of the owner of this account
	 * @param ownerId
	 */
	public void setOwnerId(String ownerId) {
		if (ownerId != null) {
			this.ownerId = ownerId.trim();
		} else {
			this.ownerId = null;
		}
	}

	/**
	 * Gets the account status.
	 *
	 * @return
	 */
	public Boolean getAccountEnabled() {
		return accountEnabled;
	}

	/**
	 * Sets the account status.
	 *
	 * @param accountEnabled
	 */
	public void setAccountEnabled(Boolean accountEnabled) {
		this.accountEnabled = accountEnabled;
	}

	/**
	 * Instantiates a new sms account.
	 */
	public SmsAccount() {
		this.startdate = new Date(System.currentTimeMillis());

	}

	/**
	 * The email address that will receive the notification email
	 */
	private String notificationEmail;
	
	@Deprecated
	public String getNotificationEmail() {
		return notificationEmail;
	}

	@Deprecated
	public void setNotificationEmail(String notificationEmail) {
		this.notificationEmail = notificationEmail;
	}

	
	private String notificationUserId;
	
	/**
	 * The user who notifications should go to
	 * @return
	 */
	public String getNotificationUserId() {
		return notificationUserId;
	}

	/**
	 * Se the user who notifications should go to
	 * @param notificationUserId
	 */
	public void setNotificationUserId(String notificationUserId) {
		this.notificationUserId = notificationUserId;
	}

	/**
	 * Instantiates a new sms account.
	 */
	public SmsAccount(double credits, String messageTypeCode,
			Long overdraftLimit, String sakaiSiteId, String sakaiUserId,
			String accountName) {
		super();
		this.credits = credits;
		this.messageTypeCode = messageTypeCode;
		this.overdraftLimit = overdraftLimit;
		this.sakaiSiteId = sakaiSiteId;
		this.sakaiUserId = sakaiUserId;
		this.accountName = accountName;
		this.startdate = new Date(System.currentTimeMillis());
	}

	public Date getStartdate() {
		return startdate;
	}

	public void setStartdate(Date startdate) {
		this.startdate = startdate;
	}

	public Date getEnddate() {
		return enddate;
	}

	public void setEnddate(Date enddate) {
		this.enddate = enddate;
	}	

	public void setEnddateStr(String value) {
		try {
			Date parsedDate = DateUtil.getISO8601SimpleDateFormat().parse(value);
			if (parsedDate != null) {
				enddate = parsedDate;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public String getEnddateStr() {
		if(enddate != null){
			return DateUtil.getISO8601SimpleDateFormat().format(enddate);
		}
		return null;
	}

	/**
	 * Gets the message type code.
	 *
	 * @return the message type code
	 */
	public String getMessageTypeCode() {
		return messageTypeCode;
	}

	/**
	 * Gets the overdraft limit.
	 *
	 * @return the overdraft limit
	 */
	public double getOverdraftLimit() {
		return overdraftLimit;
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
	 * Gets the sakai user id.
	 *
	 * @return the sakai user id
	 */
	public String getSakaiUserId() {
		return sakaiUserId;
	}

	/**
	 * Sets the message type code.
	 *
	 * @param messageTypeCode
	 *            the new message type code
	 */
	public void setMessageTypeCode(String messageTypeCode) {
		this.messageTypeCode = messageTypeCode;
	}

	/**
	 * Sets the overdraft limit.
	 *
	 * @param overdraftLimit
	 *            the new overdraft limit
	 */
	public void setOverdraftLimit(double overdraftLimit) {
		this.overdraftLimit = overdraftLimit;
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
	 * Sets the sakai user id.
	 *
	 * @param sakaiUserId
	 *            the new sakai user id
	 */
	public void setSakaiUserId(String sakaiUserId) {
		this.sakaiUserId = sakaiUserId;
	}

	/**
	 * Gets the sms transactions.
	 *
	 * @return the sms transactions
	 */
	@SuppressWarnings("unused")
	private Set<SmsTransaction> getSmsTransactions() {
		return smsTransactions;
	}

	/**
	 * Sets the sms transactions.
	 *
	 * @param smsTransactions
	 *            the new sms transactions
	 */
	@SuppressWarnings("unused")
	private void setSmsTransactions(Set<SmsTransaction> smsTransactions) {
		this.smsTransactions = smsTransactions;
	}

	/**
	 * Gets the account name.
	 *
	 * @return the account name
	 */
	public String getAccountName() {
		return accountName;
	}

	/**
	 * Sets the account name.
	 *
	 * @param accountName
	 *            the new account name
	 */
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public double getCredits() {
		return credits;
	}

	public void setCredits(double credits) {
		this.credits = credits;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(17, 37, this, false,
				SmsAccount.class);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof SmsAccount))
			return false;
		SmsAccount other = (SmsAccount) obj;
		if (accountName == null) {
			if (other.accountName != null)
				return false;
		} else if (!accountName.equals(other.accountName))
			return false;
		if (messageTypeCode == null) {
			if (other.messageTypeCode != null)
				return false;
		} else if (!messageTypeCode.equals(other.messageTypeCode))
			return false;
		if (overdraftLimit != other.overdraftLimit) {
				return false;
		}
		if (sakaiSiteId == null) {
			if (other.sakaiSiteId != null)
				return false;
		} else if (!sakaiSiteId.equals(other.sakaiSiteId))
			return false;
		if (sakaiUserId == null) {
			if (other.sakaiUserId != null)
				return false;
		} else if (!sakaiUserId.equals(other.sakaiUserId))
			return false;
		return true;
	}


}
