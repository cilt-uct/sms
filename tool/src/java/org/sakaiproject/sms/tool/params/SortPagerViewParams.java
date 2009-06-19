/***********************************************************************************
 * SortPagerViewParams.java
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

public class SortPagerViewParams extends PagerViewParams {

	public String sortBy;
	public String sortDir;
	public String viewtype; // View type must correspond with ListViewType
	// option
	public String currentStart;
	public String currentCount;

	public SortPagerViewParams(String viewId) {
		super(viewId);
	}

	public SortPagerViewParams(String viewId, String sort_by, String sort_dir,
			String viewtype) {
		super(viewId);
		this.sortBy = sort_by;
		this.sortDir = sort_dir;
		this.viewtype = viewtype;
	}

	public SortPagerViewParams(String viewId, String sort_by, String sort_dir,
			String viewtype, int currentStart, int currentCount) {
		super(viewId, currentStart, currentCount);
		this.sortBy = sort_by;
		this.sortDir = sort_dir;
		this.viewtype = viewtype;
	}

	public SortPagerViewParams() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getParseSpec() {
		// include a comma delimited list of the public properties in this class
		return super.getParseSpec()
				+ ",sortBy,sortDir,viewtype,currentStart,currentCount";
	}
}