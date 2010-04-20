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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.bean.SearchFilterBean;
import org.sakaiproject.sms.bean.SearchResultContainer;
import org.sakaiproject.sms.logic.SmsMessageLogic;
import org.sakaiproject.sms.logic.exception.SmsSearchException;
import org.sakaiproject.sms.model.SmsMessage;
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

	private static Log LOG = LogFactory.getLog(MessageLogResultsRenderer.class);

	private SearchResultContainer<SmsMessage> smsMessageList = new SearchResultContainer<SmsMessage>(
			SmsUiConstants.NO_RESULTS_PAGING_SIZE);

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
			smsMessageList = smsMessageLogic
					.getPagedSmsMessagesForCriteria(searchFilterBean);
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
					"tableheader-id:", sortViewParams, "id",
					"sms.message-log-search-results.account.id");
			sortHeaderRenderer.makeSortingLink(searchResultsTable,
					"tableheader-task-id:", sortViewParams, "smsTask.id",
					"sms.message-log-search-results.account.task.id");
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

			sortHeaderRenderer.makeSortingLink(searchResultsTable,
					"tableheader-smsc-status:", sortViewParams,
					"smscDeliveryStatusCode",
					"sms.message-log-search-results.account.smsc.status");

			for (SmsMessage smsMessage : smsMessageList.getPageResults()) {

				UIBranchContainer row = UIBranchContainer.make(
						searchResultsTable, "dataset:");
				UIOutput.make(row, "row-data-id", NullHandling
						.safeToString(smsMessage.getId()));
				UIOutput.make(row, "row-data-task-id", NullHandling
						.safeToString(smsMessage.getSmsTask().getId()));
				UIOutput.make(row, "row-data-receiver", NullHandling
						.safeToString(smsMessage.getSakaiUserId()));
				UIOutput.make(row, "row-data-mobile-number", NullHandling
						.safeToString(smsMessage.getMobileNumber()));
				UIOutput.make(row, "row-data-date-processed", NullHandling
						.safeToStringFormated(smsMessage.getDateDelivered()));
				UIOutput.make(row, "row-data-status", NullHandling
						.safeToString(smsMessage.getStatusCode()));
				UIOutput.make(row, "row-data-smsc-status", NullHandling
						.safeToString(smsMessage.getSmscDeliveryStatusCode()));
			}
		}
	}

	private void setCurrentPage(SearchFilterBean searchBean,
			SortPagerViewParams sortViewParams) {

		// new search
		if (searchBean.isNewSearch()) {
			sortViewParams.current_start = 1;
			searchBean.setNewSearch(false);
		} else
			// paging
			searchBean.setCurrentPage(sortViewParams.current_start);
	}

	public Long getTotalNumberOfRowsReturned() {
		return smsMessageList.getTotalResultSetSize();
	}

}