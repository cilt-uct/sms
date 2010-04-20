/***********************************************************************************
 * TaskListResultsRenderer.java
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
import org.sakaiproject.sms.logic.SmsTaskLogic;
import org.sakaiproject.sms.logic.exception.SmsSearchException;
import org.sakaiproject.sms.model.SmsTask;
import org.sakaiproject.sms.model.constants.SmsConst_DeliveryStatus;
import org.sakaiproject.sms.tool.constants.SmsUiConstants;
import org.sakaiproject.sms.tool.params.IdParams;
import org.sakaiproject.sms.tool.params.SortPagerViewParams;
import org.sakaiproject.sms.tool.producers.AbortTaskProducer;
import org.sakaiproject.sms.tool.producers.TaskListPopupProducer;
import org.sakaiproject.sms.tool.util.NullHandling;
import org.springframework.util.Assert;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInitBlock;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIIDStrategyDecorator;

public class TaskListResultsRenderer implements SearchResultsRenderer {

	private static Log LOG = LogFactory.getLog(TaskListResultsRenderer.class);

	private SearchResultContainer<SmsTask> smsTaskList = new SearchResultContainer<SmsTask>(
			SmsUiConstants.NO_RESULTS_PAGING_SIZE);

	private SearchFilterBean searchFilterBean;
	private SortHeaderRenderer sortHeaderRenderer;
	private SmsTaskLogic smsTaskLogic;

	public void setSmsTaskLogic(SmsTaskLogic smsTaskLogic) {
		this.smsTaskLogic = smsTaskLogic;
	}

	public void setSearchFilterBean(SearchFilterBean searchFilterBean) {
		this.searchFilterBean = searchFilterBean;
	}

	public void init() {
		sortHeaderRenderer = new SortHeaderRenderer();
		Assert.notNull(smsTaskLogic);
	}

	public void createTable(UIContainer tofill, String divID,
			SortPagerViewParams sortViewParams, String viewID) {

		init();

		searchFilterBean.setOrderBy(sortViewParams.sortBy);
		searchFilterBean.setSortDirection(sortViewParams.sortDir);
		setCurrentPage(searchFilterBean, sortViewParams);

		boolean fail = false;
		try {
			smsTaskList = smsTaskLogic
					.getPagedSmsTasksForCriteria(searchFilterBean);
			sortViewParams.current_count = smsTaskList.getNumberOfPages();
		} catch (SmsSearchException e) {
			LOG.error(e);
			fail = true;
		}

		UIJointContainer searchResultsTable = new UIJointContainer(tofill,
				divID, "task-search-results-component:");

		if (fail)
			UIMessage.make(searchResultsTable, "warning", "GeneralActionError");
		else {
			sortHeaderRenderer.makeSortingLink(searchResultsTable,
					"tableheader-task-id:", sortViewParams, "id",
					"sms.task-list-search-results.id");
			sortHeaderRenderer.makeSortingLink(searchResultsTable,
					"tableheader-size-estimate:", sortViewParams,
					"groupSizeEstimate",
					"sms.task-list-search-results.size.estimate");
			sortHeaderRenderer.makeSortingLink(searchResultsTable,
					"tableheader-size-actual:", sortViewParams,
					"groupSizeActual",
					"sms.task-list-search-results.size.actual");
			sortHeaderRenderer.makeSortingLink(searchResultsTable,
					"tableheader-messages-processed:", sortViewParams,
					"messagesProcessed",
					"sms.task-list-search-results.messages-processed");
			sortHeaderRenderer.makeSortingLink(searchResultsTable,
					"tableheader-messages-delivered:", sortViewParams,
					"messagesDelivered",
					"sms.task-list-search-results.messages-delivered");
			sortHeaderRenderer.makeSortingLink(searchResultsTable,
					"tableheader-tool-name:", sortViewParams, "sakaiToolName",
					"sms.task-list-search-results.tool.name");
			sortHeaderRenderer.makeSortingLink(searchResultsTable,
					"tableheader-sender:", sortViewParams, "senderUserName",
					"sms.task-list-search-results.sender");
			sortHeaderRenderer.makeSortingLink(searchResultsTable,
					"tableheader-message:", sortViewParams, "messageBody",
					"sms.task-list-search-results.messagae");
			sortHeaderRenderer.makeSortingLink(searchResultsTable,
					"tableheader-process-date:", sortViewParams,
					"dateProcessed",
					"sms.task-list-search-results.process.date");
			sortHeaderRenderer.makeSortingLink(searchResultsTable,
					"tableheader-status:", sortViewParams, "statusCode",
					"sms.task-list-search-results.status");

			int id = 0;
			for (SmsTask smsTask : smsTaskList.getPageResults()) {

				UIBranchContainer row = UIBranchContainer.make(
						searchResultsTable, "dataset:");

				UIInternalLink link = UIInternalLink.make(row, "popup-link",
						new IdParams(TaskListPopupProducer.VIEW_ID, smsTask
								.getId().toString()));
				link.decorate(new UIIDStrategyDecorator(String.valueOf(id)
						+ "link"));

				UIOutput idRow = UIOutput.make(row, "row-data-id", NullHandling
						.safeToString(smsTask.getId()));
				idRow.decorate(new UIIDStrategyDecorator(String.valueOf(id)));
				UIOutput.make(row, "row-data-size-estimate", NullHandling
						.safeToString(smsTask.getGroupSizeEstimate()));
				UIOutput.make(row, "row-data-size-actual", NullHandling
						.safeToString(smsTask.getGroupSizeActual()));
				UIOutput.make(row, "row-data-messages-processed", NullHandling
						.safeToString(smsTask.getMessagesProcessed()));
				UIOutput.make(row, "row-data-messages-delivered", NullHandling
						.safeToString(smsTask.getMessagesDelivered()));
				UIOutput.make(row, "row-data-tool-name", NullHandling
						.safeToString(smsTask.getSakaiToolName()));
				UIOutput.make(row, "row-data-sender", NullHandling
						.safeToString(smsTask.getSenderUserName()));
				UIOutput.make(row, "row-data-message", NullHandling
						.safeTruncatedToString(smsTask.getMessageBody(), 20));
				UIOutput.make(row, "row-data-process-date", NullHandling
						.safeToStringFormated(smsTask.getDateProcessed()));
				UIOutput.make(row, "row-data-status", NullHandling
						.safeToString(smsTask.getStatusCode()));

				if (SmsConst_DeliveryStatus.STATUS_PENDING.equals(smsTask
						.getStatusCode())) {
					UIInternalLink abortLink = UIInternalLink.make(row,
							"abort-link", new IdParams(
									AbortTaskProducer.VIEW_ID, smsTask.getId()
											.toString()));
					UIMessage abortButton = UIMessage.make(row, "abort-button",
							"sms.task-list-search-results.abort");
					UIInitBlock.make(row, "make-button-link",
							"make_button_call_link", new Object[] {
									abortButton, abortLink });
				}
				id++;
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
		return smsTaskList.getTotalResultSetSize();
	}
}
