/***********************************************************************************
 * ReportHandlerHook.java
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

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.tool.params.DownloadReportViewParams;

import uk.org.ponder.rsf.processor.HandlerHook;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class ReportHandlerHook implements HandlerHook {

	private static Log log = LogFactory.getLog(ReportHandlerHook.class);

	private CsvExportBean csvExportBean;
	private HttpServletResponse response;
	private ViewParameters viewparams;

	public void setViewparams(ViewParameters viewparams) {
		this.viewparams = viewparams;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	public void setCsvExportBean(CsvExportBean csvExportBean) {
		this.csvExportBean = csvExportBean;
	}

	public boolean handle() {

		if (viewparams instanceof DownloadReportViewParams) {
			log
					.debug("Handing viewparams and response off to the csvExportBean");
			return csvExportBean.createCsv(
					(DownloadReportViewParams) viewparams, response);
		}
		return false;
	}
}