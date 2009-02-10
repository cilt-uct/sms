/***********************************************************************************
 * MobileNumberHelperImpl.java
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
package org.sakaiproject.sms.logic.external;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.sakaiproject.api.app.profile.Profile;
import org.sakaiproject.api.app.profile.ProfileManager;

/**
 * Default implementation of {@link MobileNumberHelper}
 * Mobile numbers retrieved from Profile Tool
 *
 */
public class MobileNumberHelperImpl implements MobileNumberHelper {
	
	private static final Logger LOG = Logger.getLogger(MobileNumberHelperImpl.class);
	private ProfileManager profileManager;
	
	public void setProfileManager(ProfileManager profileManager) {
		this.profileManager = profileManager;
	}
	
	/**
	 * @see MobileNumberHelper#getUserMobileNumber(String)
	 */
	public String getUserMobileNumber(String userid) {
		Profile profile = profileManager.getUserProfileById(userid);
		if (profile != null) {
			return profile.getSakaiPerson().getMobile();
		} else {
			LOG.error("Profile not found for userid: " + userid);
			return null;
		}
	}

	/**
	 * @see MobileNumberHelper#getUserMobileNumbers(List)
	 */
	public Map<String, String> getUserMobileNumbers(List<String> userids) {
		Map<String, String> userMobileMap = new HashMap<String, String>();
		for (String userid : userids) {
			userMobileMap.put(userid, getUserMobileNumber(userid));
		}
		return userMobileMap;
	}
}
