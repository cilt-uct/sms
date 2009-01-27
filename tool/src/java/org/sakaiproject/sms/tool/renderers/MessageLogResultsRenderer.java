/***********************************************************************************
 * MessageLogResultsRenderer.java
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

package org.sakaiproject.sms.tool.renderers;

import org.sakaiproject.sms.bean.SearchFilterBean;
import org.sakaiproject.sms.bean.SearchResultContainer;
import org.sakaiproject.sms.logic.hibernate.SmsMessageLogic;
import org.sakaiproject.sms.logic.hibernate.exception.SmsSearchException;
import org.sakaiproject.sms.model.hibernate.SmsMessage;
import org.sakaiproject.sms.tool.constants.SmsUiConstants;
import org.sakaiproject.sms.tool.params.SortPagerViewParams;
import org.sakaiproject.sms.tool.util.NullHandling;
import org.springframework.util.Assert;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;

public class MessageLogResultsRenderer implements SearchResultsRenderer {

	private final static org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(MessageLogResultsRenderer.class);

	private SearchResultContainer<SmsMessage> smsMessageList = new SearchResultContainer<SmsMessage>(SmsUiConstants.NO_RESULTS_PAGING_SIZE);
	
	private SearchFilterBean searchFilterBean;
	private SortHeaderRenderer sortHeaderRenderer;
	private SmsMessageLogic smsMessageLogic;

	public void setSmsMessageLogic(SmsMessageLogic smsMessageLogic) {
		this.smsMessageLogic = smsMessageLogic;
	}

	public void setSearchFilterBean(SearchFilterBean searchFilterBean) {
		this.searchFilterBean = searchFilterBean;
	}

	public void init() {
		sortHeaderRenderer = new SortHeaderRenderer();
		Assert.notNull(smsMessageLogic);
	}

	public void createTable(UIContainer tofill, String divID,
			SortPagerViewParams sortViewParams, String viewID) {

		init();

		searchFilterBean.setOrderBy(sortViewParams.sortBy);
		searchFilterBean.setSortDirection(sortViewParams.sortDir);
		setCurrentPage(searchFilterBean, sortViewParams);

		boolean fail = false;
		try {
				smsMessageList = smsMessageLogic.getPagedSmsMessagesForCriteria(searchFilterBean);
				sortViewParams.current_count = smsMessageList.getNumberOfPages();
		} catch (SmsSearchException e) {
			LOG.error(e);
			fail = true;
		}

		UIJointContainer searchResultsTable = new UIJointContainer(tofill,
				divID, "message-log-search-results-component:");
		if (fail)
			UIMessage.make(searchResultsTable, "warning", "GeneralActionError");
		else {
			sortHeaderRenderer.makeSortingLink(searchResultsTable,
					"tableheader-group:", sortViewParams,
					"smsTask.deliveryGroupId",
					"sms.message-log-search-results.account.group");
			sortHeaderRenderer.makeSortingLink(searchResultsTable,
					"tableheader-tool-name:", sortViewParams,
					"smsTask.sakaiToolName",
					"sms.message-log-search-results.account.tool.name");
			sortHeaderRenderer.makeSortingLink(searchResultsTable,
					"tableheader-sender:", sortViewParams,
					"smsTask.senderUserName",
					"sms.message-log-search-results.account.sender");
			sortHeaderRenderer.makeSortingLink(searchResultsTable,
					"tableheader-receiver:", sortViewParams,
					"smsTask.deliveryGroupName",
					"sms.message-log-search-results.account.reciever");
			sortHeaderRenderer.makeSortingLink(searchResultsTable,
					"tableheader-mobile-number:", sortViewParams,
					"mobileNumber",
					"sms.message-log-search-results.account.mobile.number");
			sortHeaderRenderer.makeSortingLink(searchResultsTable,
					"tableheader-date-processed:", sortViewParams,
					"dateDelivered",
					"sms.message-log-search-results.account.date.delivered");
			sortHeaderRenderer.makeSortingLink(searchResultsTable,
					"tableheader-status:", sortViewParams,
					"smsTask.statusCode",
					"sms.message-log-search-results.account.Status");

			for (SmsMessage smsMessage : smsMessageList.getPageResults()) {

				UIBranchContainer row = UIBranchContainer.make(
						searchResultsTable, "dataset:");

				UIOutput.make(row, "row-data-group", NullHandling
						.safeToString(smsMessage.getSmsTask().getDeliveryGroupId()));
				UIOutput.make(row, "row-data-tool-name", NullHandling
						.safeToString(smsMessage.getSmsTask().getSakaiToolName()));
				UIOutput.make(row, "row-data-sender", NullHandling
						.safeToString(smsMessage.getSmsTask().getSenderUserName()));
				UIOutput.make(row, "row-data-receiver", NullHandling
						.safeToString(smsMessage.getSakaiUserId()));
				UIOutput.make(row, "row-data-mobile-number", NullHandling
						.safeToString(smsMessage.getMobileNumber()));
				UIOutput.make(row, "row-data-date-processed", NullHandling
						.safeToStringFormated(smsMessage.getDateDelivered()));
				UIOutput.make(row, "row-data-status", NullHandling
						.safeToString(smsMessage.getStatusCode()));
			}
		}
	}

	private void setCurrentPage(SearchFilterBean searchBean, SortPagerViewParams sortViewParams) {
		
		//new search
		if(searchBean.isNewSearch()){
			sortViewParams.current_start = 1;
			searchBean.setNewSearch(false);
		}
		else//paging
			searchBean.setCurrentPage(sortViewParams.current_start);	
	}

	public Long getTotalNumberOfRowsReturned() {
		return smsMessageList.getTotalResultSetSize();
	}

}