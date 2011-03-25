/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
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
import java.util.Arrays;
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
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * Default implementation of {@link MobileNumberHelper}. Mobile numbers retrieved
 * from Profile Tool, with user opt-in / opt-out preference from user property.
 * 
 */
public class MobileNumberHelperImpl implements MobileNumberHelper {

	private static final String PREF_SMS_NOTIFICATIONS = "smsnotifications";

	private static final Log LOG = LogFactory
			.getLog(MobileNumberHelperImpl.class);

	private NumberRoutingHelper numberRoutingHelper;
	public void setNumberRoutingHelper(NumberRoutingHelper numberRoutingHelper) {
		this.numberRoutingHelper = numberRoutingHelper;
	}

	private SakaiPersonManager sakaiPersonManager;
	public void setSakaiPersonManager(SakaiPersonManager sakaiPersonManager) {
		this.sakaiPersonManager = sakaiPersonManager;
	}

	private UserDirectoryService userDirectoryService;	
	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	/**
	 * @see MobileNumberHelper#getUserMobileNumber(String)
	 */
	public String getUserMobileNumber(String userid) {
		LOG.debug("getMobileNumber(" + userid + ")");
		
		//first check that the user migh want SMS
		if (!userWantsSms(userid))
			return null;
			
		final SakaiPerson sakaiPerson = sakaiPersonManager.getSakaiPerson(
				userid, sakaiPersonManager.getUserMutableType());
		if (sakaiPerson == null) {
			// this is to be expected as not all Sakai Users have profiles
			LOG.debug("Profile not found for userid: " + userid);
		} else {
			String mobile = numberRoutingHelper.normalizeNumber(sakaiPerson.getMobile());
			if (numberRoutingHelper.isNumberRoutable(mobile)) {
				return mobile;
			} 
		}
		
		return null;
	}

	/**
	 * @see MobileNumberHelper#getUserMobileNumbers(List)
	 */
	public Map<String, String> getUserMobileNumbers(List<String> userids) {
		LOG.debug("getUserMobileNumbers()");
		

		final Map<String, String> userMobileMap = new HashMap<String, String>();
		Set<String> userSet = new HashSet<String>(userids);
		//first strip the users who don't want sms
		userSet = filterUserListForPreference(userSet);
		Map<String, SakaiPerson> userMap = sakaiPersonManager.getSakaiPersons(userSet, sakaiPersonManager.getUserMutableType());
		
		//there'a possibility we have an empty map
		if(userMap== null || userMap.size() == 0)
			return userMobileMap;

		Iterator<Entry<String, SakaiPerson>> selector = userMap.entrySet().iterator();
		while ( selector.hasNext() ) {
			Entry<String, SakaiPerson> pairs = selector.next();
        	SakaiPerson sp = pairs.getValue();
        	if (sp != null) {
        		String mobile = numberRoutingHelper.normalizeNumber(sp.getMobile());
        		if (mobile != null && numberRoutingHelper.isNumberRoutable(mobile)) {
        			userMobileMap.put(pairs.getKey(), mobile);
        		}
        	}
		}
		return userMobileMap;
	}

	/**
	 *@see MobileNumberHelper#getUserMobileNumbers(List)
	 */
	public List<String> getUsersWithMobileNumbers(Set<String> userIds) {
		LOG.debug("getUsersWithMobileNumbers()");
		List<String> result = new ArrayList<String>();
		
        Set<String> usersWhoWantSMS = filterUserListForPreference(userIds);
        LOG.debug("filtered list size is" + usersWhoWantSMS.size());
        if( usersWhoWantSMS.size() > 0){
			Map<String, SakaiPerson> userMobileMap = sakaiPersonManager.getSakaiPersons(usersWhoWantSMS, sakaiPersonManager.getUserMutableType());
			Iterator<Entry<String, SakaiPerson>> selector = userMobileMap.entrySet().iterator();
			while ( selector.hasNext() ) {
	        	Entry<String, SakaiPerson> pairs = selector.next();
	        	SakaiPerson sp = pairs.getValue();
	        	if (sp != null) {
	        		String mobile = numberRoutingHelper.normalizeNumber(sp.getMobile());
	                LOG.debug("got a number " + mobile + " for user " + sp.getUid() );
	        		if (mobile != null && !"".equals(mobile) && numberRoutingHelper.isNumberRoutable(mobile)) {
	        			result.add( sp.getUid() );
	        		}
	        	}
			}
        }
		return result;
	}


	/**
	 * @see MobileNumberHelper#getUserIdsFromMobileNumber(String)
	 */
	
	public List<String> getUserIdsFromMobileNumber(String mobileNumber) {
		LOG.debug("getUserIdsFromMobileNumber(" + mobileNumber + ")");
		final SakaiPerson example = sakaiPersonManager.getPrototype();
		example.setNormalizedMobile(numberRoutingHelper.normalizeNumber(mobileNumber));
		example.setTypeUuid(sakaiPersonManager.getUserMutableType().getUuid());

		final List<String> toReturn = new ArrayList<String>();
		
		final List<SakaiPerson> list = sakaiPersonManager.findSakaiPerson(example);
		
		LOG.debug("Found " + list.size() + " matches against normalized numbers");
		
		for (SakaiPerson person : list) {
			toReturn.add(person.getUid());
		}
		//In cases where this is not set also check 
		final SakaiPerson example1 = sakaiPersonManager.getPrototype();
		example1.setMobile(mobileNumber);
		example.setTypeUuid(sakaiPersonManager.getUserMutableType().getUuid());

		final List<SakaiPerson> list1 = sakaiPersonManager.findSakaiPerson(example1);
		
		LOG.debug("Found " + list1.size() + " matches against non-normalized numbers");
		
		for (SakaiPerson person : list1) {
			toReturn.add(person.getUid());
		}

		return toReturn;
	}
	
	
	private boolean userWantsSms(String userId) {
		LOG.debug("userWantsSms(" + userId);
		if (userId == null)
			return false;
		
		try {
			User u = userDirectoryService.getUser(userId);
			if (u == null)
				return true;
			ResourceProperties rp = u.getProperties();
			return rp.getBooleanProperty(PREF_SMS_NOTIFICATIONS);
		} catch (UserNotDefinedException e) {
			LOG.info("user: " + userId + " does not exist");
		} catch (EntityPropertyNotDefinedException e) {
			LOG.debug("user: " + userId + " has no defined sms preference");
		} catch (EntityPropertyTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}
	
	
	private Set<String> filterUserListForPreference(Set<String> userids) {
		LOG.debug("filterUserListForPreference");
		List<User> users = userDirectoryService.getUsers(userids);
		
		Set<String> ret = new HashSet<String>();
		if (users == null)
			return ret;
		
		LOG.debug("got a list of " + users.size() + " users");
		for (int i = 0; i < users.size(); i++) {
			User u = users.get(i);
			LOG.debug("checking " + u.getEid());
			try {
				boolean wantsSMS = u.getProperties().getBooleanProperty(PREF_SMS_NOTIFICATIONS);
				if (wantsSMS) {
					if (userTypeToSMS(u.getType())) {
						ret.add(u.getId());
					}
				}else{
					LOG.debug("User: " + u.getSortName() + " doesn't want to get SMS messages!");
				}
				
			} catch (EntityPropertyNotDefinedException e) {
				if (userTypeToSMS(u.getType())) {
					ret.add(u.getId());
				}
			} catch (EntityPropertyTypeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		LOG.debug("returning list of " + ret.size());
		return ret;
	}

	/**
	 * There may be certain types of users we don't sms
	 * @param type
	 * @return
	 */
	private boolean userTypeToSMS(String type) {
		String typesToSms = serverConfigurationService.getString("sms.usertypes.allow", null);
		if (typesToSms == null) {
			LOG.debug("No user types defined allowing all");
			return true;
		}
		
		List<String> types = Arrays.asList(typesToSms.split(","));
		if (types.contains(type)) {
			LOG.debug("Type:" + type + " found in prefs list");
			return true;
		}
		return false;
	}

}
