/***********************************************************************************
 * SmsStatusParams.java
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

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

/**
 * General view parameter for passing an ID
 * 
 */
public class SmsParams extends SimpleViewParameters {
	public String id;
	public String status;

	public SmsParams() {
	}

	public SmsParams(String viewid) {
		this.viewID = viewid;
	}

	public SmsParams(String viewid, String id) {
		this.viewID = viewid;
		this.id = id;
	}

	public SmsParams(String viewid, String id, String status) {
		this.viewID = viewid;
		this.id = id;
		this.status = status;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
