/***********************************************************************************
 * TablePagerRenderer.java
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


import org.sakaiproject.sms.model.constants.SmsConstants;
import org.sakaiproject.sms.tool.params.SortPagerViewParams;

import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class TablePagerRenderer {

	public Integer currentStart = 1;
	public Integer currentCount = 1;
	
	public void createPager(UIContainer tofill, String divID, SortPagerViewParams sortParams, String viewId, Long totalnumberOfRowsReturned) {	
		
		this.currentStart = sortParams.current_start;
		this.currentCount = sortParams.current_count;
		
		UIJointContainer joint = new UIJointContainer(tofill, divID, "table-pager-component:", ""+1);
	
		if(totalnumberOfRowsReturned.intValue() >= SmsConstants.READ_LIMIT){
			UIOutput.make(joint, "number-of-rows", "Number of rows: " + totalnumberOfRowsReturned 
					+" (Limited to first " + SmsConstants.READ_LIMIT  + " rows)");
		}
		else{
			UIOutput.make(joint, "number-of-rows", "Number of rows: " + totalnumberOfRowsReturned);	
		}
		
		ViewParameters new_params = sortParams.copyBase();
		
		if(currentStart > 1){
			((SortPagerViewParams)new_params).current_start -= 1;
			UIInternalLink.make(joint, "prev", new_params);
		}
		
		UIOutput.make(joint, "page-count", " Page " + (currentStart)  + " of " + (currentCount) + " ");

		if(!currentStart.equals(currentCount)){
			new_params = sortParams.copyBase();		
			((SortPagerViewParams)new_params).current_start += 1;
			UIInternalLink.make(joint, "next", new_params);
		}
	}
}