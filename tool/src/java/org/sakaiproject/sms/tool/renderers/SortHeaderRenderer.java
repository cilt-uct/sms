
/***********************************************************************************
 * SortHeaderRenderer.java
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
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class SortHeaderRenderer {
	
	public static final String BULLET_UP_IMG_SRC = "../images/sortascending.gif";
    public static final String BULLET_DOWN_IMG_SRC = "../images/sortdescending.gif";
    
    /**
     * Creates linked sorting header
     * 
     * @param tofill		{@link UIContainer} to fill
     * @param divID			id of div
     * @param viewparams	view parameters
     * @param sort_by		{@link SortByConstants}
     * @param link_text		Text of link 
     */
    public void makeSortingLink (UIContainer tofill, String divID, ViewParameters viewparams, String sort_by, String link_text){
	    	
    	SortPagerViewParams params = (SortPagerViewParams) viewparams;
    	UIJointContainer joint = new UIJointContainer(tofill, divID, "sortHeader:", ""+1);

    	//Link Text
    	UIMessage.make(joint, "text", link_text);
    	if (params.sortBy.equals(sort_by)){
    		UILink.make(joint, "arrow", (params.sortDir.equals(SmsConstants.SORT_ASC) ? BULLET_UP_IMG_SRC : BULLET_DOWN_IMG_SRC));
    	}

    	//Add Link and modify params
    	String newSortDir = (params.sortBy.equals(sort_by) ? (params.sortDir.equals(SmsConstants.SORT_ASC) 
    			? SmsConstants.SORT_DESC
    			: SmsConstants.SORT_ASC) : SmsConstants.SORT_ASC);
    	
    	ViewParameters new_params = viewparams.copyBase();
    	((SortPagerViewParams)new_params).sortBy = sort_by;
    	((SortPagerViewParams)new_params).sortDir = newSortDir;

    	UIInternalLink link = UIInternalLink.make(joint, "link", new_params);
    	link.decorators = new DecoratorList(new UITooltipDecorator(UIMessage.make(link_text + "-tooltip")));
    }

}