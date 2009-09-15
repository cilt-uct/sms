/***********************************************************************************
 * MessageLogProducer.java
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

package org.sakaiproject.sms.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.sms.tool.beans.ActionResults;

import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class MessageLogProducer extends AbstractSearchListProducer {

	public static final String VIEW_ID = "MessageLog";

	@Override
	public String getTitleMessage() {
		return "sms.view-results-message-log.name";
	}

	@Override
	public String getViewID() {
		return VIEW_ID;
	}

	@Override
	public String getDefaultSortColumn() {
		return "dateDelivered";
	}

	public List<NavigationCase> reportNavigationCases() {
		List<NavigationCase> list = new ArrayList<NavigationCase>();
		list.add(new NavigationCase(ActionResults.RESET,
				new SimpleViewParameters(MessageLogProducer.VIEW_ID)));
		return list;
	}

}
