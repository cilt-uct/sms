/***********************************************************************************
 * SearchResultsRenderer.java
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

import org.sakaiproject.sms.bean.SearchFilterBean;
import org.sakaiproject.sms.tool.params.SortPagerViewParams;

import uk.org.ponder.rsf.components.UIContainer;

public interface SearchResultsRenderer {

	public void createTable(UIContainer tofill, String divID, SortPagerViewParams sortViewParams, String viewID);
	public void setSearchFilterBean(SearchFilterBean searchFilterBean);
	public Long getTotalNumberOfRowsReturned();
}