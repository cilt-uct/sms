/***********************************************************************************
 * DownloadReportViewParams.java
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
package org.sakaiproject.sms.tool.params;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.sakaiproject.sms.bean.SearchFilterBean;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class DownloadReportViewParams extends SimpleViewParameters {

	public String sourceView;

	public String number;
	public String status;
	public Integer messageTypeId;
	public String taskId;
	public String dateFrom;
	public String dateTo;
	public String toolName;
	public String sender;
	public Integer currentPage;
	public String orderBy;
	public String transactionType;
	public String sortDirection;

	public DownloadReportViewParams() {
	}

	public DownloadReportViewParams(String viewID, String sourceView,
			String taskId, String dateFrom, String dateTo, String number,
			String orderBy, String sender, String sortDirection, String status,
			Integer messageTypeId, String toolName, String transactionType) {
		super();
		this.viewID = viewID;
		this.messageTypeId = messageTypeId;
		this.sourceView = sourceView;
		this.taskId = taskId;
		this.dateFrom = dateFrom;
		this.dateTo = dateTo;
		this.number = number;
		this.orderBy = orderBy;
		this.sender = sender;
		this.sortDirection = sortDirection;
		this.status = status;
		this.toolName = toolName;
		this.transactionType = transactionType;
	}

	public SearchFilterBean extractSearchFilter(SimpleDateFormat dateFormat) {

		Date dateFromDate = null;
		Date dateToDate = null;
		try {
			if (!"".equals(dateFrom)) {
				dateFromDate = dateFormat.parse(dateFrom);
			}
			if (!"".equals(dateTo)) {
				dateToDate = dateFormat.parse(dateTo);
			}
		} catch (ParseException e) {
			throw new RuntimeException("Failed to parse date string");
		}

		return new SearchFilterBean(number, messageTypeId, status, taskId,
				dateFromDate, dateToDate, toolName, sender, Integer.valueOf(1),
				orderBy, sortDirection);
	}

}