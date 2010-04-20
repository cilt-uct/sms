/***********************************************************************************
 * PagerViewParams.java
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

import org.sakaiproject.sms.logic.SmsConfigLogic;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class PagerViewParams extends SimpleViewParameters {

	public SmsConfigLogic smsConfigLogic;

	public int current_start = 1;
	public int current_count = 1;

	public PagerViewParams() {
	}

	public PagerViewParams(String viewId) {
		super(viewId);
	}

	public PagerViewParams(String viewId, int currentStart, int currentCount) {
		super(viewId);
		this.current_start = currentStart;
		this.current_count = currentCount;
	}

	@Override
	public String getParseSpec() {
		// include a comma delimited list of the public properties in this class
		return super.getParseSpec() + ",current_start,current_count";
	}

	public void setSmsConfigLogic(SmsConfigLogic smsConfigLogic) {
		this.smsConfigLogic = smsConfigLogic;
	}
}