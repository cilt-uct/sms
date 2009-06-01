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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.api.common.edu.person.SakaiPersonManager;

/**
 * Default implementation of {@link MobileNumberHelper} Mobile numbers retrieved
 * from Profile Tool
 * 
 */
public class MobileNumberHelperImpl implements MobileNumberHelper {

	private static final Log LOG = LogFactory
			.getLog(MobileNumberHelperImpl.class);

	private SakaiPersonManager sakaiPersonManager;

	public void setSakaiPersonManager(SakaiPersonManager sakaiPersonManager) {
		this.sakaiPersonManager = sakaiPersonManager;
	}

	/**
	 * @see MobileNumberHelper#getUserMobileNumber(String)
	 */
	public String getUserMobileNumber(String userid) {
		LOG.debug("getMobileNumber(" + userid + ")");
		final SakaiPerson sakaiPerson = sakaiPersonManager.getSakaiPerson(
				userid, sakaiPersonManager.getUserMutableType());
		if (sakaiPerson == null) {
			// this is to be expected not all Sakai Users have profiles
			LOG.debug("Profile not found for userid: " + userid);
			return null;
		} else {
			return sakaiPerson.getMobile();

		}
	}

	/**
	 * @see MobileNumberHelper#getUserMobileNumbers(List)
	 */
	public Map<String, String> getUserMobileNumbers(List<String> userids) {
		LOG.debug("getUserMobileNumbers()");
		final Map<String, String> userMobileMap = new HashMap<String, String>();
		for (String userid : userids) {
			userMobileMap.put(userid, getUserMobileNumber(userid));
		}
		return userMobileMap;
	}

	/**
	 *@see MobileNumberHelper#getUserMobileNumbers(List)
	 */
	public List<String> getUsersWithMobileNumbers(Set<String> userIds) {
		Map<String, SakaiPerson> userMobileMap = new HashMap<String, SakaiPerson>();
		List<String> result = new ArrayList<String>();
		userMobileMap = sakaiPersonManager.getSakaiPersons(userIds, sakaiPersonManager.getUserMutableType());
		Iterator<Entry<String, SakaiPerson>> selector = userMobileMap.entrySet().iterator();
		while ( selector.hasNext() ) {
        	Entry<String, SakaiPerson> pairs = selector.next();
        	SakaiPerson sp = pairs.getValue();
        	if (sp != null && sp.getMobile() != null ) {
        		result.add( sp.getUid() );
    		}
		}
		return result;
	}

	/**
	 * @see MobileNumberHelper#getUserIdsFromMobileNumber(String)
	 */
	@SuppressWarnings("unchecked")
	public List<String> getUserIdsFromMobileNumber(String mobileNumber) {
		LOG.debug("getUserIdsFromMobileNumber(" + mobileNumber);
		final SakaiPerson example = sakaiPersonManager.getPrototype();
		example.setMobile(mobileNumber);

		final List<SakaiPerson> list = sakaiPersonManager
				.findSakaiPerson(example);
		final List<String> toReturn = new ArrayList<String>();
		for (SakaiPerson person : list) {
			toReturn.add(person.getUid());
		}

		return toReturn;
	}
}
