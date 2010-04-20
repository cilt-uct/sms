/***********************************************************************************
 * CsvExportBean.java
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
package org.sakaiproject.sms.tool.reporting;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.bean.SearchFilterBean;
import org.sakaiproject.sms.logic.HibernateLogicLocator;
import org.sakaiproject.sms.logic.exception.SmsSearchException;
import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.tool.params.DownloadReportViewParams;
import org.sakaiproject.sms.tool.producers.MessageLogProducer;
import org.sakaiproject.sms.tool.producers.TaskListProducer;
import org.sakaiproject.sms.tool.producers.TransactionLogProducer;
import org.sakaiproject.sms.tool.util.BeanToCSVReflector;
import org.sakaiproject.sms.tool.util.SakaiDateFormat;

public class CsvExportBean {

	private static Log log = LogFactory.getLog(CsvExportBean.class);
	private final Map<String, CsvExportStrategy> csvExporters = new TreeMap<String, CsvExportStrategy>();
	private SakaiDateFormat sakaiDateFormat;

	private HibernateLogicLocator hibernateLogicLocator;

	public HibernateLogicLocator getHibernateLogicLocator() {
		return hibernateLogicLocator;
	}

	public void setHibernateLogicLocator(
			HibernateLogicLocator hibernateLogicLocator) {
		this.hibernateLogicLocator = hibernateLogicLocator;
	}

	public void setSakaiDateFormat(SakaiDateFormat dateFormat) {
		this.sakaiDateFormat = dateFormat;
	}

	
	private ExternalLogic externalLogic;	
	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	public CsvExportBean() {
		csvExporters.put(TaskListProducer.VIEW_ID, new SmsTaskExportStrategy());
		csvExporters.put(MessageLogProducer.VIEW_ID,
				new SmsMessageExportStrategy());
		csvExporters.put(TransactionLogProducer.VIEW_ID,
				new SmsTransactionLogExportStrategy());
	}

	public boolean createCsv(DownloadReportViewParams viewparams,
			HttpServletResponse response) {

		if (log.isInfoEnabled())
			log.info("Create csv data for view " + viewparams.sourceView);

		try {
			createResponse(response, viewparams);
		} catch (IOException e) {
			log.error("Failed to create csv output" + e);
			throw new RuntimeException("Failed to create csv output", e);
		}

		return true;
	}

	private void createResponse(HttpServletResponse response,
			DownloadReportViewParams viewparams) throws IOException {

		SearchFilterBean searchFilterBean = viewparams
				.extractSearchFilter(sakaiDateFormat.getSakaiDateFormat());
		csvExporters.get(viewparams.sourceView).createCsvResponse(response,
				searchFilterBean, externalLogic);
	}

	private abstract class CsvExportStrategy {

		protected BeanToCSVReflector beanToCSVReflector = new BeanToCSVReflector();

		public void createCsvResponse(HttpServletResponse response,
				SearchFilterBean searchFilterBean, ExternalLogic externalLogic) {

			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

			// Set the response headers
			response.setHeader("Content-disposition", "inline");
			response.setContentType("text/csv");

			response.setHeader("Content-Disposition", "attachment; filename="
					+ getFileNamePrefix() + sdf.format(date) + ".csv");

			//set the external logic in the reflector
			beanToCSVReflector.setExternalLogic(externalLogic);
			
			List<?> pageResults = null;
			try {
				pageResults = getCriteriaResults(searchFilterBean);
			} catch (SmsSearchException e) {
				throw new RuntimeException("Failed to obtain search results", e);
			}

			String csvText = beanToCSVReflector.toCSV(pageResults,
					getCsvColumns());

			try {
				ServletOutputStream outputStream = response.getOutputStream();
				outputStream.print(csvText);
			} catch (IOException e) {
				throw new RuntimeException(
						"Failed to write csv to output stream");
			}
		}

		protected abstract String[] getCsvColumns();

		protected abstract List<?> getCriteriaResults(
				SearchFilterBean searchFilterBean) throws SmsSearchException;

		protected abstract String getFileNamePrefix();
	}

	private final class SmsTaskExportStrategy extends CsvExportStrategy {

		private final String[] taskListColumns = new String[] { "id",
				"creditEstimate", "dateCreated", "dateProcessed", "dateToSend",
				"deliveryGroupId", "deliveryGroupName", "deliveryUserId",
				"groupSizeActual", "groupSizeEstimate", "costEstimate",
				"messageBody", "messageTypeId", "attemptCount", "sakaiSiteId",
				"sakaiToolId", "sakaiToolName", "senderUserName",
				"smsAccountId", "statusCode", "maxTimeToLive" };

		@Override
		public String[] getCsvColumns() {
			return taskListColumns;
		}

		@Override
		public List<?> getCriteriaResults(SearchFilterBean searchFilterBean)
				throws SmsSearchException {
			return hibernateLogicLocator.getSmsTaskLogic()
					.getAllSmsTasksForCriteria(searchFilterBean);
		}

		@Override
		protected String getFileNamePrefix() {
			return "smsTask";
		}
	}

	private final class SmsMessageExportStrategy extends CsvExportStrategy {

		private final String[] messageColumns = new String[] { "id",
				"dateDelivered", "mobileNumber", "sakaiUserId",
				"smscMessageId", "statusCode", "submitResult",
				"smscDeliveryStatusCode", "smscId" };

		@Override
		public String[] getCsvColumns() {
			return messageColumns;
		}

		@Override
		public List<?> getCriteriaResults(SearchFilterBean searchFilterBean)
				throws SmsSearchException {
			return hibernateLogicLocator.getSmsMessageLogic()
					.getAllSmsMessagesForCriteria(searchFilterBean);
		}

		@Override
		protected String getFileNamePrefix() {
			return "smsMessage";
		}
	}

	private final class SmsTransactionLogExportStrategy extends
			CsvExportStrategy {

		private final String[] transactionLogColumns = new String[] { "id",
				"sakaiUserId", "transactionCredits", "transactionDate",
				"transactionTypeCode", "smsTaskId", "description" };

		@Override
		public String[] getCsvColumns() {
			return transactionLogColumns;
		}

		@Override
		public List<?> getCriteriaResults(SearchFilterBean searchFilterBean)
				throws SmsSearchException {
			return hibernateLogicLocator.getSmsTransactionLogic()
					.getAllSmsTransactionsForCriteria(searchFilterBean);
		}

		@Override
		protected String getFileNamePrefix() {
			return "smsTransactionLog";
		}
	}
}