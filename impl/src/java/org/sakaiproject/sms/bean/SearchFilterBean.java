/***********************************************************************************
 * SearchFilterBean.java
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
package org.sakaiproject.sms.bean;

import java.util.Date;

import org.sakaiproject.sms.hibernate.model.constants.SmsHibernateConstants;
import org.sakaiproject.sms.util.DateUtil;

// TODO: Auto-generated Javadoc
/**
 * Generic search filter bean object. Will be used for the search screens as a
 * criteria object to house the search parameters
 * 
 * @author Julian Wyngaard
 * @version 1.0
 * @created 19-Nov-2008
 */
public class SearchFilterBean {

	/** The id. */
	private String number;

	/** The status. */
	private String status;

	/** The date from. */
	private Date dateFrom;

	/** The date to. */
	private Date dateTo;

	/** The tool name. */
	private String toolName;

	/** The sender. */
	private String sender;

	/** The current page. */
	private Integer currentPage;

	/** The order by. */
	private String orderBy;

	/** The transaction type. */
	private String transactionType;

	/** The sort direction. */
	private String sortDirection;

	private boolean newSearch;

	/**
	 * Instantiates a new search filter bean.
	 */
	public SearchFilterBean() {
		super();
		newSearch = false;
		dateFrom = DateUtil.getDateFromNow(-5);
		dateTo = new Date();
		currentPage = new Integer(1);
	}

	/**
	 * Instantiates a new search filter bean.
	 * 
	 * @param id
	 *            the id
	 * @param status
	 *            the status
	 * @param dateFrom
	 *            the date from
	 * @param dateTo
	 *            the date to
	 * @param toolName
	 *            the tool name
	 * @param sender
	 *            the sender
	 * @param currentPage
	 *            the current page
	 * @param orderBy
	 *            the order by
	 * @param sortDirection
	 *            the sort direction
	 */
	public SearchFilterBean(String number, String status, Date dateFrom,
			Date dateTo, String toolName, String sender, Integer currentPage,
			String orderBy, String sortDirection) {
		super();
		this.number = number;
		this.status = status;
		this.dateFrom = dateFrom;
		this.dateTo = dateTo;
		this.toolName = toolName;
		this.sender = sender;
		this.currentPage = currentPage;
		this.orderBy = orderBy;
		this.sortDirection = sortDirection;
	}

	/**
	 * Sets the number
	 * 
	 * @return
	 */
	public String getNumber() {
		return number;
	}

	/**
	 * Gets the number
	 * 
	 * @param number
	 */
	public void setNumber(String number) {
		this.number = number;
	}

	/**
	 * Gets the status.
	 * 
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Sets the status.
	 * 
	 * @param status
	 *            the new status
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * Gets the date from.
	 * 
	 * @return the date from
	 */
	public Date getDateFrom() {
		return dateFrom;
	}

	/**
	 * Sets the date from.
	 * 
	 * @param dateFrom
	 *            the new date from
	 */
	public void setDateFrom(Date dateFrom) {
		this.dateFrom = dateFrom;
	}

	/**
	 * Gets the date to.
	 * 
	 * @return the date to
	 */
	public Date getDateTo() {
		return dateTo;
	}

	/**
	 * Sets the date to.
	 * 
	 * @param dateTo
	 *            the new date to
	 */
	public void setDateTo(Date dateTo) {
		this.dateTo = dateTo;
	}

	/**
	 * Gets the tool name.
	 * 
	 * @return the tool name
	 */
	public String getToolName() {
		return toolName;
	}

	/**
	 * Sets the tool name.
	 * 
	 * @param toolName
	 *            the new tool name
	 */
	public void setToolName(String toolName) {
		this.toolName = toolName;
	}

	/**
	 * Gets the sender.
	 * 
	 * @return the sender
	 */
	public String getSender() {
		return sender;
	}

	/**
	 * Sets the sender.
	 * 
	 * @param sender
	 *            the new sender
	 */
	public void setSender(String sender) {
		this.sender = sender;
	}

	/**
	 * Gets the current page.
	 * 
	 * @return the current page
	 */
	public Integer getCurrentPage() {
		return currentPage;
	}

	/**
	 * Sets the current page.
	 * 
	 * @param currentPage
	 *            the new current page
	 */
	public void setCurrentPage(Integer currentPage) {
		this.currentPage = currentPage;
	}

	/**
	 * Gets the order by.
	 * 
	 * @return the order by
	 */
	public String getOrderBy() {
		return orderBy;
	}

	/**
	 * Sets the order by.
	 * 
	 * @param orderBy
	 *            the new order by
	 */
	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	/**
	 * Gets the transaction type.
	 * 
	 * @return the transaction type
	 */
	public String getTransactionType() {
		return transactionType;
	}

	/**
	 * Sets the transaction type.
	 * 
	 * @param transactionType
	 *            the new transaction type
	 */
	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	/**
	 * Gets the sort direction.
	 * 
	 * @return the sort direction
	 */
	public String getSortDirection() {
		return sortDirection;
	}

	/**
	 * Sets the sort direction.
	 * 
	 * @param sortDirection
	 *            the new sort direction
	 */
	public void setSortDirection(String sortDirection) {
		this.sortDirection = sortDirection;
	}

	/**
	 * Fire action. Denotes a new search
	 * 
	 * @return true, if successful
	 */
	public boolean fireAction() {
		setCurrentPage(1);
		setNewSearch(true);
		return true;
	}

	/**
	 * Sort asc.
	 * 
	 * @return true, if successful
	 */
	public boolean sortAsc() {
		if (sortDirection != null && !sortDirection.trim().equals("")) {
			return sortDirection.equals(SmsHibernateConstants.SORT_ASC);
		}
		// default to asc
		return true;
	}

	/**
	 * A boolean to differentiate between a new search or paging
	 * 
	 * @return new search
	 */
	public boolean isNewSearch() {
		return newSearch;
	}

	/**
	 * Set if the search is new
	 * 
	 * @param newSearch
	 */
	public void setNewSearch(boolean newSearch) {
		this.newSearch = newSearch;
	}

	@Override
	public String toString() {
		StringBuffer retStr = new StringBuffer("");

		retStr.append("\n\n----------\n");
		retStr.append("Search criteria\n");
		retStr.append("----------\n");
		retStr.append("number: ").append(number).append("\n");
		retStr.append("status: ").append(status).append("\n");
		retStr.append("dateFrom: ").append(dateFrom).append("\n");
		retStr.append("dateTo: ").append(dateTo).append("\n");
		retStr.append("toolName: ").append(toolName).append("\n");
		retStr.append("sender: ").append(sender).append("\n");
		retStr.append("currentPage: ").append(currentPage).append("\n");
		retStr.append("orderBy: ").append(orderBy).append("\n");
		retStr.append("transactionType: ").append(transactionType).append("\n");
		retStr.append("sortDirection: ").append(sortDirection).append("\n");
		retStr.append("newSearch: ").append(newSearch).append("\n");
		retStr.append("----------\n");
		return retStr.toString();
	}

}
