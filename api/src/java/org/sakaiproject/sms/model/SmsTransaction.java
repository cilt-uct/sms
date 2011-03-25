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

import java.util.Date;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.sakaiproject.sms.util.DateUtil;

/**
 * Transactions linked to a specific sms account. When a sms task is created, a
 * transaction will be inserted indicating that credits are reserved. When the
 * task is processed, the actual credits will be calculated and the difference
 * will be settled with another transaction.
 * 
 * @author Julian Wyngaard
 * @version 1.0
 * @created 19-Nov-2008
 */
public class SmsTransaction extends BaseModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 5340407908089192124L;

	/**
	 * The amount of credits available in the account. 1 sms= 1 credit.
	 */
	private double creditBalance;

	/** The sakai user id. This is the user who request the sms task. */
	private String sakaiUserId;

	/** The transaction credits. */
	private double transactionCredits;

	/** The transaction date. */
	private Date transactionDate;

	/** The transaction type code. See SmsConst_Billing */
	private String transactionTypeCode;

	/** The Sms account. */
	private SmsAccount smsAccount;

	/** The sms task id. */
	private Long smsTaskId;
	
	/** The transaction description */
	private String description;
	
	/**
	 * Instantiates a new sms transaction.
	 */
	public SmsTransaction() {
		super();

	}

	/**
	 * Instantiates a new sms transaction.
	 */
	public SmsTransaction(double credits, String sakaiUserId,
			double transactionCredits, Date transactionDate,
			String transactionTypeCode) {
		super();
		this.creditBalance = credits;
		this.sakaiUserId = sakaiUserId;

		this.transactionCredits = transactionCredits;
		this.transactionDate = transactionDate;
		this.transactionTypeCode = transactionTypeCode;

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
	 * Gets the transaction credits.
	 * 
	 * @return the transaction credits
	 */
	public double getTransactionCredits() {
		return transactionCredits;
	}

	/**
	 * Gets the transaction date.
	 * 
	 * @return the transaction date
	 */
	public Date getTransactionDate() {
		return transactionDate;
	}

	/**
	 * Gets the transaction type code.
	 * 
	 * @return the transaction type code
	 */
	public String getTransactionTypeCode() {
		return transactionTypeCode;
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
	 * Sets the transaction credits.
	 * 
	 * @param transactionCredits
	 *            the new transaction credits
	 */
	public void setTransactionCredits(double transactionCredits) {
		this.transactionCredits = transactionCredits;
	}

	/**
	 * Sets the transaction date.
	 * 
	 * @param transactionDate
	 *            the new transaction date
	 */
	public void setTransactionDate(Date transactionDate) {
		this.transactionDate = DateUtil.getUsableDate(transactionDate);
	}

	/**
	 * Sets the transaction type code.
	 * 
	 * @param transactionTypeCode
	 *            the new transaction type code
	 */
	public void setTransactionTypeCode(String transactionTypeCode) {
		this.transactionTypeCode = transactionTypeCode;
	}

	/**
	 * Gets the sms account.
	 * 
	 * @return the sms account
	 */
	public SmsAccount getSmsAccount() {
		return smsAccount;
	}

	/**
	 * Sets the sms account.
	 * 
	 * @param smsAccount
	 *            the new sms account
	 */
	public void setSmsAccount(SmsAccount smsAccount) {
		this.smsAccount = smsAccount;
	}

	/**
	 * Gets the sms task id.
	 * 
	 * @return the sms task
	 */
	public Long getSmsTaskId() {
		return smsTaskId;
	}

	/**
	 * Sets the sms task id.
	 * 
	 * @param smsTask
	 *            the new sms task
	 */
	public void setSmsTaskId(Long smsTaskId) {
		this.smsTaskId = smsTaskId;
	}

	/**
	 * Get the transaction description
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the transaction description
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(17, 37, this, false,
				SmsTransaction.class);		
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof SmsTransaction))
			return false;
		SmsTransaction other = (SmsTransaction) obj;
		if (creditBalance != other.creditBalance) {
				return false;
		}
	
		if (sakaiUserId == null) {
			if (other.sakaiUserId != null)
				return false;
		} else if (!sakaiUserId.equals(other.sakaiUserId))
			return false;
		if (smsTaskId == null) {
			if (other.smsTaskId != null)
				return false;
		} else if (!smsTaskId.equals(other.smsTaskId))
			return false;
		if (transactionCredits != other.transactionCredits) {
				return false;
		} 
		if (transactionDate == null) {
			if (other.transactionDate != null)
				return false;
		} else if (!transactionDate.equals(other.transactionDate))
			return false;
		if (transactionTypeCode == null) {
			if (other.transactionTypeCode != null)
				return false;
		} else if (!transactionTypeCode.equals(other.transactionTypeCode))
			return false;
		return true;
	}

	public double getCreditBalance() {
		return creditBalance;
	}

	public void setCreditBalance(double creditBalance) {
		this.creditBalance = creditBalance;
	}

}
