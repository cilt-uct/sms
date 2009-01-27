/***********************************************************************************
 * SmsTransaction.java
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

package org.sakaiproject.sms.model.hibernate;

import java.util.Date;

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

	/** The running account balance in currency. */
	private Float balance;

	/** The sakai user id. This is the user who request the sms task. */
	private String sakaiUserId;

	/** The transaction amount. */
	private Float transactionAmount;

	/** The transaction credits. */
	private Integer transactionCredits;

	/** The transaction date. */
	private Date transactionDate;

	/** The transaction type code. See SmsConst_Billing */
	private String transactionTypeCode;

	/** The Sms account. */
	private SmsAccount smsAccount;

	/** The sms task id. */
	private Long smsTaskId;

	/**
	 * Instantiates a new sms transaction.
	 */
	public SmsTransaction() {

	}

	/**
	 * Instantiates a new sms transaction.
	 */
	public SmsTransaction(Float balance, String sakaiUserId,
			Float transactionAmount, Integer transactionCredits,
			Date transactionDate, String transactionTypeCode) {
		super();
		this.balance = balance;
		this.sakaiUserId = sakaiUserId;
		this.transactionAmount = transactionAmount;
		this.transactionCredits = transactionCredits;
		this.transactionDate = transactionDate;
		this.transactionTypeCode = transactionTypeCode;

	}

	/**
	 * Gets the balance.
	 * 
	 * @return the balance
	 */
	public Float getBalance() {
		return balance;
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
	public Integer getTransactionCredits() {
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
	 * Sets the balance.
	 * 
	 * @param balance
	 *            the new balance
	 */
	public void setBalance(Float balance) {
		this.balance = balance;
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
	 * Sets the transaction amount.
	 * 
	 * @param transactionAmount
	 *            the new transaction amount
	 */
	public void setTransactionAmount(Float transactionAmount) {
		this.transactionAmount = transactionAmount;
	}

	/**
	 * Sets the transaction credits.
	 * 
	 * @param transactionCredits
	 *            the new transaction credits
	 */
	public void setTransactionCredits(Integer transactionCredits) {
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
	 * Gets the transaction amount.
	 * 
	 * @return the transaction amount
	 */
	public Float getTransactionAmount() {
		return transactionAmount;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		// int result = super.hashCode();
		int result = 43;
		result = prime * result + ((balance == null) ? 0 : balance.hashCode());
		result = prime * result
				+ ((sakaiUserId == null) ? 0 : sakaiUserId.hashCode());
		result = prime
				* result
				+ ((transactionAmount == null) ? 0 : transactionAmount
						.hashCode());
		result = prime
				* result
				+ ((transactionCredits == null) ? 0 : transactionCredits
						.hashCode());
		result = prime * result
				+ ((transactionDate == null) ? 0 : transactionDate.hashCode());
		result = prime
				* result
				+ ((transactionTypeCode == null) ? 0 : transactionTypeCode
						.hashCode());
		result = prime * result
				+ ((smsTaskId == null) ? 0 : smsTaskId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof SmsTransaction))
			return false;
		SmsTransaction other = (SmsTransaction) obj;
		if (balance == null) {
			if (other.balance != null)
				return false;
		} else if (!balance.equals(other.balance))
			return false;
		if (sakaiUserId == null) {
			if (other.sakaiUserId != null)
				return false;
		} else if (!sakaiUserId.equals(other.sakaiUserId))
			return false;
		if (transactionAmount == null) {
			if (other.transactionAmount != null)
				return false;
		} else if (!transactionAmount.equals(other.transactionAmount))
			return false;
		if (transactionCredits == null) {
			if (other.transactionCredits != null)
				return false;
		} else if (!transactionCredits.equals(other.transactionCredits))
			return false;
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
		if (smsTaskId == null) {
			if (other.smsTaskId != null)
				return false;
		} else if (!smsTaskId.equals(other.smsTaskId))
			return false;
		return true;
	}

}
