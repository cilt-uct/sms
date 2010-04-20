/***********************************************************************************
 * TransactionLogResultsRenderer.java
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
import org.sakaiproject.sms.logic.SmsTransactionLogic;
import org.sakaiproject.sms.logic.exception.SmsSearchException;
import org.sakaiproject.sms.model.SmsTransaction;
import org.sakaiproject.sms.tool.constants.SmsUiConstants;
import org.sakaiproject.sms.tool.params.SortPagerViewParams;
import org.sakaiproject.sms.tool.util.NullHandling;
import org.springframework.util.Assert;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;

public class TransactionLogResultsRenderer implements SearchResultsRenderer {

	private static Log LOG = LogFactory
			.getLog(TransactionLogResultsRenderer.class);

	private SearchResultContainer<SmsTransaction> smsTransactions = new SearchResultContainer<SmsTransaction>(
			SmsUiConstants.NO_RESULTS_PAGING_SIZE);

	private SearchFilterBean searchFilterBean;
	private SortHeaderRenderer sortHeaderRenderer;
	private SmsTransactionLogic smsTransactionLogic;

	public void setSmsTransactionLogic(SmsTransactionLogic smsTransactionLogic) {
		this.smsTransactionLogic = smsTransactionLogic;
	}

	public void setSearchFilterBean(SearchFilterBean searchFilterBean) {
		this.searchFilterBean = searchFilterBean;
	}

	public void init() {
		sortHeaderRenderer = new SortHeaderRenderer();
		Assert.notNull(smsTransactionLogic);
	}

	public void createTable(UIContainer tofill, String divID,
			SortPagerViewParams sortViewParams, String viewID) {

		init();

		searchFilterBean.setOrderBy(sortViewParams.sortBy);
		searchFilterBean.setSortDirection(sortViewParams.sortDir);
		setCurrentPage(searchFilterBean, sortViewParams);

		if (searchFilterBean.getNumber() != null
				&& searchFilterBean.getNumber().trim().equals(""))
			searchFilterBean.setNumber(null);

		smsTransactions = null;
		boolean fail = false;
		try {
			smsTransactions = smsTransactionLogic
					.getPagedSmsTransactionsForCriteria(searchFilterBean);
			sortViewParams.current_count = smsTransactions.getNumberOfPages();
		} catch (SmsSearchException e) {
			LOG.error(e);
			fail = true;
		}

		UIJointContainer searchResultsTable = new UIJointContainer(tofill,
				divID, "transaction-log-search-results-component:");
		if (fail)
			UIMessage.make(searchResultsTable, "warning", "GeneralActionError");
		else {
			sortHeaderRenderer.makeSortingLink(searchResultsTable,
					"tableheader-id:", sortViewParams, "id",
					"sms.transaction-log-search-results.id");
			sortHeaderRenderer.makeSortingLink(searchResultsTable,
					"tableheader-account-number:", sortViewParams,
					"smsAccount",
					"sms.transaction-log-search-results.account.no");
			sortHeaderRenderer.makeSortingLink(searchResultsTable,
					"tableheader-transaction-task-id:", sortViewParams,
					"smsTaskId", "sms.transaction-log-search-results.task.id");
			sortHeaderRenderer.makeSortingLink(searchResultsTable,
					"tableheader-transaction-type", sortViewParams,
					"transactionTypeCode",
					"sms.transaction-log-search-results.trans.type");
			sortHeaderRenderer.makeSortingLink(searchResultsTable,
					"tableheader-transaction-description", sortViewParams,
					"description",
					"sms.transaction-log-search-results.trans.description");
			sortHeaderRenderer.makeSortingLink(searchResultsTable,
					"tableheader-transaction-date:", sortViewParams,
					"transactionDate",
					"sms.transaction-log-search-results.trans.date");
			sortHeaderRenderer.makeSortingLink(searchResultsTable,
					"tableheader-transaction-credits:", sortViewParams,
					"transactionCredits",
					"sms.transaction-log-search-results.trans.credits");
			sortHeaderRenderer.makeSortingLink(searchResultsTable,
					"tableheader-account-credits:", sortViewParams, "credits",
					"sms.transaction-log-search-results.account.balanace");

			for (SmsTransaction smsTransaction : smsTransactions
					.getPageResults()) {

				UIBranchContainer row = UIBranchContainer.make(
						searchResultsTable, "dataset:");

				UIOutput.make(row, "row-data-id", NullHandling
						.safeToString(smsTransaction.getId()));
				UIOutput.make(row, "row-data-account-number", NullHandling
						.safeToString(smsTransaction.getSmsAccount().getId()));
				UIOutput.make(row, "row-data-task-id", NullHandling
						.safeToString(smsTransaction.getSmsTaskId()));
				UIOutput.make(row, "row-data-transaction-type", NullHandling
						.safeToString(smsTransaction.getTransactionTypeCode()));
				UIOutput.make(row, "row-data-transaction-description", NullHandling
						.safeToString(smsTransaction.getDescription()));
				UIOutput.make(row, "row-data-transaction-date", NullHandling
						.safeToStringFormated(smsTransaction
								.getTransactionDate()));
				UIOutput.make(row, "row-data-transaction-credits", NullHandling
						.safeToString(smsTransaction.getTransactionCredits()));
				UIOutput.make(row, "row-data-account-credits", NullHandling
						.safeToString(smsTransaction.getCreditBalance()));
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
		return smsTransactions.getTotalResultSetSize();
	}

}
