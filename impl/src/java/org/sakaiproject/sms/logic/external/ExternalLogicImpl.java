/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009 The Sakai Foundation
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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.alias.api.Alias;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.PermissionsHelper;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.i18n.InternationalizedMessages;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sms.logic.incoming.ParsedMessage;
import org.sakaiproject.sms.logic.incoming.ShortMessageCommand;
import org.sakaiproject.sms.model.SmsMessage;
import org.sakaiproject.sms.model.SmsTask;
import org.sakaiproject.sms.model.SmsUser;
import org.sakaiproject.sms.model.constants.SmsConstants;
import org.sakaiproject.sms.model.smpp.SmsSmppProperties;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;

/**
 * Implementation of {@link ExternalLogic} for Sakai-specific code.
 */
public class ExternalLogicImpl implements ExternalLogic {

	private static final Log LOG = LogFactory.getLog(ExternalLogicImpl.class);

	public final static String NO_LOCATION = "noLocationAvailable";

	private static final String SMS_BUNDLE = "messages";

	private ServerConfigurationService serverConfigurationService = null;

	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	private FunctionManager functionManager;

	public void setFunctionManager(FunctionManager functionManager) {
		this.functionManager = functionManager;
	}

	private SessionManager sessionManager;

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	private ToolManager toolManager;

	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}

	private SecurityService securityService;

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	private SiteService siteService;

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	private UserDirectoryService userDirectoryService;

	public void setUserDirectoryService(
			UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	private EntityBroker entityBroker;

	public void setEntityBroker(EntityBroker entityBroker) {
		this.entityBroker = entityBroker;
	}


	private MobileNumberHelper mobileNumberHelper;

	public void setMobileNumberHelper(MobileNumberHelper mobileNumberHelper) {
		this.mobileNumberHelper = mobileNumberHelper;
	}

	private NumberRoutingHelper numberRoutingHelper;
	
	public void setNumberRoutingHelper(NumberRoutingHelper numberRoutingHelper) {
		this.numberRoutingHelper = numberRoutingHelper;
	}

	private AliasService aliasService;

	public void setAliasService(AliasService aliasService) {
		this.aliasService = aliasService;
	}

	private TimeService timeService;

	public void setTimeService(TimeService ts) {
		timeService = ts;
	}

	public EventTrackingService eventTrackingService;
	public void setEventTrackingService(EventTrackingService eventTrackingService) {
		this.eventTrackingService = eventTrackingService;
	}

	private PreferencesService preferencesService;
	public void setPreferencesService(PreferencesService ps) {
		preferencesService = ps;
	}

	public void init() {
		LOG.debug("init");
		// register Sakai permissions for this tool

		functionManager.registerFunction(SMS_SEND);
	}

	/**
	 * @see ExternalLogic#getCurrentUserId()
	 */
	public String getCurrentUserId() {
		return sessionManager.getCurrentSessionUserId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.sms.logic.external.ExternalLogic#getCurrentSiteId()
	 */
	public String getCurrentSiteId() {
		return getCurrentLocationId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sakaiproject.sms.logic.external.ExternalLogic#getCurrentLocationId()
	 */
	public String getCurrentLocationId() {
		String location = null;
		try {
			location = toolManager.getCurrentPlacement().getContext();
		} catch (Exception e) {
			// sakai failed to get us a location so we can assume we are not
			// inside the portal
			return NO_LOCATION;
		}
		if (location == null) {
			location = NO_LOCATION;
		}
		return location;
	}

	/**
	 * @see ExternalLogic#isUserAdmin(String)
	 */
	public boolean isUserAdmin(String userId) {
		return securityService.isSuperUser(userId);
	}

	/**
	 * @see ExternalLogic#isUserAllowedInLocation(String, String, String)
	 */
	public boolean isUserAllowedInLocation(String userId, String permission,
			String locationId) {

		LOG.debug("isUserAllowedInLocation(" + userId + ", " + permission
				+ ", " + locationId + ")");

		if (permission == null || locationId == null) {
			return false;
		}
		final String locationRef = locationId.startsWith(Entity.SEPARATOR) ? locationId
				: siteService.siteReference(locationId);

		Boolean allowed;
		
		if (userId == null) {
			allowed = securityService.unlock(permission, locationRef);
		} else {
			allowed = securityService.unlock(userId, permission, locationRef);
		}
		LOG.debug("allowed: " + allowed);

		return allowed;
	}

	public String getSakaiMobileNumber(String userId) {
		LOG.debug("Getting mobile number for userid " + userId);
		return mobileNumberHelper.getUserMobileNumber(userId);
	}

	/**
	 * Sets up session for userId if this is anonymous session
	 */
	private void setupSession(String userId) {
		// Get current session (if no session NonPortableSession will be created
		// in default implementation)
		final Session session = sessionManager.getCurrentSession();

		// If session is anonymous
		if (session.getUserId() == null) {
			session.setUserId(userId);
			try {
				session.setUserEid(userDirectoryService.getUser(userId)
						.getEid());
			} catch (UserNotDefinedException e) {
				LOG.error(e.getMessage(), e);
			}
		}
		
	}

	private Set<Object> getMembersForEntityRef(String entityReference) {
		final Set<Object> members = new HashSet<Object>();
		
		// if in the format /site/123/role/something
		if ("site".equals(EntityReference.getPrefix(entityReference))
				&& EntityReference.getIdFromRefByKey(entityReference, "role") != null) {

			String role = EntityReference.getIdFromRefByKey(entityReference,
			"role");
			String siteRef = "/"
				+ EntityReference.getPrefix(entityReference)
				+ "/"
				+ EntityReference
				.getIdFromRefByKey(entityReference, "site");

			// Fetch the site
			AuthzGroup group = (AuthzGroup) entityBroker.fetchEntity(siteRef);
			Set<Member> allMembers = group.getMembers();

			// Only add if it has corresponding role
			for (Member member : allMembers) {
				if (role.equals(member.getRole().getId()) && member.isActive()) {
					members.add(member);
				}
			}

		} else { // Any other authz group
			String groupId = EntityReference.getIdFromRefByKey(entityReference, "group");
			if ( groupId != null ){
				AuthzGroup group = siteService.findGroup(groupId);
				if(group != null){
					Set<Member> allMembers = group.getMembers();
					for (Member member : allMembers) {
						//only add active members
						if (member.isActive()) {
							members.add(member);
						}
					}
					
				}else{
					LOG.debug("Could not find group with id: " + groupId);
				}
			}
		}
		return members;
	}

	@SuppressWarnings("unchecked")
	private Set<SmsMessage> getSakaiEntityMembersAsMessages(SmsTask smsTask,
			String entityReference, boolean getMobileNumbers) {
		boolean addMemberToDelList;
		Set<SmsMessage> messages = new HashSet<SmsMessage>();

		setupSession(smsTask.getSenderUserId());
		Set members = getMembersForEntityRef(entityReference);

		LOG.debug("Getting group members for : " + entityReference + " (size = "
				+ members.size() + ")");
		for (Object oObject : members) {
			addMemberToDelList = true;
			SmsMessage message = new SmsMessage();
			if (oObject instanceof Member) {
				message.setSakaiUserId(((Member) oObject).getUserId());
			} else {
				message.setSakaiUserId("*"); // for testing
			}
			if (getMobileNumbers) {
				String mobileNumber = getSakaiMobileNumber(message
						.getSakaiUserId());
				if (mobileNumber == null || "".equals(mobileNumber)) {
					addMemberToDelList = false;
					if (SmsConstants.SMS_DEV_MODE) {
						mobileNumber = "0731876135"; // for testing
					}
				}
				message.setMobileNumber(mobileNumber);
			}
			if (addMemberToDelList) {
				message.setSmsTask(smsTask);
				messages.add(message);
			}
		}
		return messages;
	}

	public Set<SmsMessage> getSakaiGroupMembers(SmsTask smsTask,
			boolean getMobileNumbers) {

		Set<SmsMessage> messages = new HashSet<SmsMessage>();

		if (smsTask == null) {
			return messages;
		}
		
		if (smsTask.getId() == null) {
			LOG.debug("Getting recipient numbers for preliminary task ");
		} else {
			LOG.debug("Getting recipient numbers for task " + smsTask.getId());
		}
		
		if (smsTask.getDeliveryEntityList() != null) {
			LOG.debug("Adding numbers for entity reference list");
			// list of references to groups, roles etc.
			for (String reference : smsTask.getDeliveryEntityList()) {
				messages.addAll(getSakaiEntityMembersAsMessages(smsTask,
						reference, getMobileNumbers));
			}
		}
		if (smsTask.getDeliveryGroupId() != null) {
			LOG.debug("Adding numbers for single group id");
			// a single group reference
			messages.addAll(getSakaiEntityMembersAsMessages(smsTask, smsTask
					.getDeliveryGroupId(), getMobileNumbers));

		}
		if (smsTask.getDeliveryMobileNumbersSet() != null) {
			LOG.debug("Adding numbers from list of mobile numbers");

			// a list of mobile numbers, not necessarily from Sakai users
			for (String mobileNumber : smsTask.getDeliveryMobileNumbersSet()) {
				
				String normalizedNumber = numberRoutingHelper.normalizeNumber(mobileNumber);
				
				// TODO At some point we may wish to exclude numbers here from users
				// who have this number set in their profile and have opted-out of 
				// SMS notification.
				
				if (numberRoutingHelper.isNumberRoutable(normalizedNumber)) {
					SmsMessage message = new SmsMessage();
					message.setMobileNumber(normalizedNumber);
					message.setSmsTask(smsTask);
					messages.add(message);
				}
			}
		}
		if (smsTask.getDeliveryUserId() != null) {
			// a single sakai user id, for incoming messages
			messages.addAll(getSakaiEntityMembersAsMessages(smsTask, smsTask
					.getDeliveryUserId(), getMobileNumbers));
		}
		if (smsTask.getSakaiUserIds() != null) {
			LOG.debug("Adding numbers from list of user ids");
			for (String userId : smsTask.getSakaiUserIdsList()) {
				String mobileNr = getSakaiMobileNumber(userId);
				if (mobileNr != null && !"".equals(mobileNr)) {
					SmsMessage message = new SmsMessage();
					message.setMobileNumber(mobileNr);
					message.setSmsTask(smsTask);
					message.setSakaiUserId(userId);
					messages.add(message);
				}
			}

		}

		if (LOG.isDebugEnabled()) {
			if (messages.isEmpty()) {
				LOG.debug("Message list for task " + smsTask.getId()
						+ " is empty.");
			} else {
				for (SmsMessage message : messages) {
					if (smsTask.getId() == null) {
						LOG.debug("Returning message for preliminary task "
								+ " userid=" + message.getSakaiUserId()
								+ " number=" + message.getMobileNumber());
					} else {
						LOG.debug("Returning message for  task: "
								+ smsTask.getId() + " userid="
								+ message.getSakaiUserId() + " number="
								+ message.getMobileNumber());
					}

				}
			}
		}
		return messages;
	}

	public String getSakaiUserDisplayName(String userId) {
		String result = null;
		if (isValidUser(userId)) { // user may NOT be null or not a Sakai user
			try {
				result = userDirectoryService.getUser(userId).getDisplayName();
				if ("".equals(result) || result == null) { // Display Name does
					// not exits. Get
					// the username
					result = userDirectoryService.getUser(userId)
					.getDisplayId() == null ? userDirectoryService
							.getUser(userId).getEid() : userDirectoryService
							.getUser(userId).getDisplayId();
				}
			} catch (UserNotDefinedException e) {
				LOG
				.warn("Cannot getSakaiUserDisplayName for user id "
						+ userId);
			}
		}
		return result;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sakaiproject.sms.logic.external.ExternalLogic#getUserDisplayName()
	 */
	public String getCurrentUserDisplayId() {
		String userId = getCurrentUserId();
		String name = null;
		try {
			name = userDirectoryService.getUserEid(userId);
		} catch (UserNotDefinedException e) {
			LOG.error(e.getMessage(), e);
			return null;
		}
		return name;
	}

	/**
	 * @see ExternalLogic#isValidSite(String)
	 */
	public boolean isValidSite(String siteId) {
		return (siteId == null) ? false : siteService.siteExists(siteId);
	}

	/**
	 * @see ExternalLogic#isValidSite(String)
	 */
	private String getSiteId(String siteRef) {
		
		// TODO - possible recognize mailarchive aliases here
		
		String siteprefix = SiteService.REFERENCE_ROOT + Entity.SEPARATOR;
		
		if (siteRef == null) {
			return null;
		}
		
		if (!siteRef.startsWith(siteprefix)) {
			return null;
		}
		
		return siteRef.substring(siteprefix.length());
	}

	/**
	 * @see ExternalLogic#isValidUser(String)
	 */
	public boolean isValidUser(String userId) {
		try {
			userDirectoryService.getUser(userId);
			return true;
		} catch (UserNotDefinedException e) {
			return false;
		}
	}



	public String getSakaiEmailAddressForUserId(String userId) {
		try {
			return userDirectoryService.getUser(userId).getEmail();
		} catch (UserNotDefinedException e) {
			LOG.error(e.getMessage(), e);
		}
		return null;

	}

	public SmsSmppProperties getSmppProperties(SmsSmppProperties smsSmppProperties) {

		LOG.debug("Reading properties from ServerConfigurationService");

		String smscAddress = serverConfigurationService.getString(
		"sms.SMSCAddress").trim();
		if (smscAddress == null || "".equals(smscAddress)) {
			LOG.debug("sms.SMSCAddress not found");
		} else {
			smsSmppProperties.setSMSCAddress(smscAddress);
		}

		String smscPort = serverConfigurationService.getString(
		"sms.SMSCPort").trim();

		if (smscPort == null || "".equals(smscPort)) {
			LOG.debug("sms.SMSCPort not found");
		} else {
			try {
				smsSmppProperties.setSMSCPort(Integer.valueOf(smscPort));
			}
			catch (NumberFormatException nfe) {
				LOG.error("Value supplied for sms.SMSPort is not a valid Interger: " + smscPort);
			}

		}

		String smscUserName = serverConfigurationService.getString(
		"sms.SMSCUserName").trim();

		if (smscUserName == null || "".equals(smscUserName)) {
			LOG.debug("sms.SMSCUserName not found");
		} else {
			smsSmppProperties.setSMSCUsername(smscUserName);
		}

		String smscPassword = serverConfigurationService.getString(
		"sms.SMSCPassword").trim();

		if (smscPassword == null || "".equals(smscPassword)) {
			LOG.debug("sms.SMSCPassword not found");
		} else {
			smsSmppProperties.setSMSCPassword(smscPassword);
		}

		boolean bind = serverConfigurationService.getBoolean("sms.BindThisNode", true);
		smsSmppProperties.setBindThisNode(bind);

		int sendingDelay = serverConfigurationService.getInt("sms.sendingDelay", -1);
		if (sendingDelay >= 0) {
			smsSmppProperties.setSendingDelay(sendingDelay);
		}
		
		String systemType = serverConfigurationService.getString("sms.systemType").trim();
		if (systemType == null || "".equals(systemType)) {
			LOG.debug("systemType not found");
		} else {
			smsSmppProperties.setSystemType(systemType);
		}
		
		String messageEncoding =  serverConfigurationService.getString("sms.messageEncoding").trim();
		if (messageEncoding != null && !"".equals(messageEncoding)) {
			smsSmppProperties.setMessageEncoding(messageEncoding);
		}

		String addressRange =  serverConfigurationService.getString("sms.addressRange").trim();
		if (addressRange != null && !"".equals(addressRange)) {
			smsSmppProperties.setAddressRange(addressRange);
		}

		String sourceAddress =  serverConfigurationService.getString("sms.sourceAddress").trim();
		if (sourceAddress != null && !"".equals(sourceAddress)) {
			smsSmppProperties.setSourceAddress(sourceAddress);
		}
		
		String sourceAddressTON = serverConfigurationService.getString("sms.sourceAddressTon");
		if (sourceAddressTON != null && sourceAddressTON.trim().length() > 0) {
			smsSmppProperties.setSourceAddressTON(Byte.parseByte(sourceAddressTON));
		}
		
		String destAddressTON = serverConfigurationService.getString("sms.destinationAddressTon");
		if (destAddressTON != null && destAddressTON.trim().length() > 0) {
			smsSmppProperties.setDestAddressTON(Byte.parseByte(destAddressTON.trim()));
		}
		
		String serviceType = serverConfigurationService.getString("sms.serviceType");
		if (serviceType != null || serverConfigurationService.getBoolean("sms.useNullServiceType", false)) {
			smsSmppProperties.setServiceType(serviceType);
		}
		
		LOG.debug("Read properties from ServerConfigurationService");

		return smsSmppProperties;
	}

	public String getSiteFromAlias(String alias) {
		
		// TODO - support an SMS-specific aliasing scheme as well
		
		try {
			String target = getSiteId(aliasService.getTarget(alias));
			
			if (isValidSite(target)) {
				LOG.debug("Found site from alias " + alias + ": " + target);
				return target;
			} else {
				LOG.debug("Invalid site from alias " + alias + ": " + target);
				return null;				
			}
			
		} catch (IdUnusedException e) {
			LOG.info("Undefined alias used: " + alias);
			return null;
		}

	}

	
	public List<String> getAllSiteAliases() {
		
		List<String> siteAliases = new ArrayList<String>();
		
		List<Alias> aliases = aliasService.getAliases(1, aliasService.countAliases());
		
		// TODO - add an sms-specific aliasing scheme which can be set somewhere in the UI
		
		for (Alias alias : aliases) {
			if (alias.getTarget() != null &&
				alias.getTarget().startsWith(SiteService.REFERENCE_ROOT + Entity.SEPARATOR)) {
				siteAliases.add(alias.getId());
			}
		}
		
		return siteAliases;
	}

	public List<String> getUserIdsFromMobileNumber(String mobileNumber) {
		return mobileNumberHelper.getUserIdsFromMobileNumber(mobileNumber);
	}

	public TimeZone getLocalTimeZone() {
		return timeService.getLocalTimeZone();
	}

	public String getSakaiUserSortName(String sakaiUserId) {
		String result = null;
		if (isValidUser(sakaiUserId)) { // user may be null or not a Sakai user
			try {
				result = userDirectoryService.getUser(sakaiUserId)
				.getSortName();
			} catch (UserNotDefinedException e) {
				LOG.warn("Cannot getSakaiUserSortName for user id "
						+ sakaiUserId);
			}
		}
		return result;
	}

	
	public Map<String, String> getSakaiGroupsForSite(String siteId) {
		//Using a {@link LinkedHashMap} to preserve the sorting order we will do later
		Map<String, String> groups = new LinkedHashMap<String, String>();
		try {
			Collection<Group> groupsCollection = siteService.getSite(siteId)
			.getGroups();
			List<Group> groupsList = new ArrayList<Group>(groupsCollection);
			//Sort groups by title alphabetical order
			Collections.sort(groupsList, new GroupNameComparator());
			for (Group grp : groupsList) {
				groups.put(grp.getReference(), grp.getTitle());
			}
		} catch (IdUnusedException e) {
			LOG.error(e.getMessage(), e);
		}
		return groups;
	}

	
	public Map<String, String> getSakaiRolesForSite(String siteId) {
		Map<String, String> roles = new HashMap<String, String>();
		try {
			Set<Role> rolesCollection = siteService.getSite(siteId).getRoles();
			for (Role r : rolesCollection) {
				String siteReference = siteService.getSite(siteId)
				.getReference();
				roles.put(siteReference + "/role/" + r.getId(), r.getId());
			}
		} catch (IdUnusedException e) {
			LOG.error(e.getMessage(), e);
		}
		return roles;
	}

	public String getSakaiGroupNameFromId(String siteId, String groupId) {
		if (!"".equals(groupId) && !"".equals(siteId) && groupId != null
				&& siteId != null) {
			try {
				Site site = siteService.getSite(siteId);
				Group group = site.getGroup(groupId);
				return group.getTitle();
			} catch (IdUnusedException e) {
				LOG.warn("Group: " + groupId + " was not found in site: "
						+ siteId);
			}
		}
		return null;
	}

	public String getEntityRealIdFromRefByKey(String entity, String key) {
		if (entity != null && key != null && !"".equals(entity)
				&& !"".equals(key)) {
			return EntityReference.getIdFromRefByKey(entity, key);
		}
		return null;
	}

	public String getEntityPrefix(String entity) {
		if (entity != null && !"".equals(entity)) {
			return EntityReference.getPrefix(entity);
		}
		return null;
	}

	public String getSmsContactEmail() {
		String email = serverConfigurationService.getString("sms.support",
				serverConfigurationService.getString("support.email"));
		return email;
	}

	public void setUpSessionPermissions(String permissionPrefix) {
		try {
			Site site = siteService.getSite(getCurrentLocationId());
			ToolSession session = sessionManager.getCurrentToolSession();
			session.setAttribute(PermissionsHelper.TARGET_REF, site
					.getReference());
			session.setAttribute(PermissionsHelper.DESCRIPTION,
					"Set SMS permissions for " + site.getTitle());
			session.setAttribute(PermissionsHelper.PREFIX, permissionPrefix); // set
			// some
			// instruction
			// text
			// and
			// the
			// prefix
			// of
			// the
			// permissions
			// it
			// should
			// handle.
		} catch (IdUnusedException e) {
			LOG.warn("Site not found for id: " + getCurrentLocationId());
		}
	}

	
	public List<User> getUsersWithMobileNumbersOnly(String siteId) {
		List<String> userIds = new ArrayList<String>();
		List<User> users = new ArrayList<User>();

		try {
			Site site = siteService.getSite(siteId);

			// Fetch the site
			LOG.debug( "Fetching members for site:" + siteId );
			Set<Member> allMembers = site.getMembers();
			Set<String> activeUserIds = new HashSet<String>();

			// Only record user id if member is flagged as active
			for (Member member : allMembers) {
				if ( member.isActive() ) {
					activeUserIds.add(member.getUserId());
					
				}
			}
			LOG.debug("got a list of " + activeUserIds.size() + " active users");
			if( !activeUserIds.isEmpty() ){
				userIds = mobileNumberHelper.getUsersWithMobileNumbers( activeUserIds );
				LOG.debug("got a list of: " + userIds.size() + " active users with mobile numbers");
			}
			if( !userIds.isEmpty() ){
				users = userDirectoryService.getUsers(userIds);
			}
		} catch (IdUnusedException e) {
			LOG.warn("Site not found for id: "+ getCurrentLocationId());
		}
		return users;
	}

	public boolean isUserAllowedSiteUpdate(String userId, String locationId) {
		return isUserAllowedInLocation(userId, SiteService.SECURE_UPDATE_SITE,
				locationId);
	}

	
	public Map<String, User> getSakaiUsers(Set<String> userIds) {
		Map<String, User> userMap = new HashMap<String, User>();
		if (userIds != null && !userIds.isEmpty()) {
			List<User> users = userDirectoryService.getUsers(userIds);
			for (User user : users) {
				userMap.put(user.getId(), user);
			}
		}
		return userMap;
	}

	public boolean isNodeBindToGateway() {
		return serverConfigurationService.getBoolean("sms.BindThisNode", true);
	}

	
	public Map<String, String> getSakaiUserDisplayNames(Set<String> sakaiUserIds) {
		Map<String, String> usernames = new HashMap<String, String>();
		if (sakaiUserIds != null && !sakaiUserIds.isEmpty()) {
			List<User> users = userDirectoryService.getUsers(sakaiUserIds);
			for (User user : users) {
				usernames.put(user.getId(), user.getSortName());
			}
		}
		return usernames;
	}

	public void postEvent(String event, String ref, String context) {

		/* If using this code with 2-5-x, use this instead (but events will lack context information): 
			EventTrackingService.post(EventTrackingService.newEvent(event, ref, true, NotificationService.NOTI_NONE));
		*/
		
		eventTrackingService.post(eventTrackingService.newEvent(event, ref, context, true, NotificationService.NOTI_NONE));
	}
	
	public static class GroupNameComparator implements Comparator<Group> {
        public static final long serialVersionUID = 31L;
        public int compare(Group o1, Group o2) {
            return o1.getTitle().compareTo(o2.getTitle());
        }
    }

	public Set<String> getUserIdsFromSmsMessages(Collection<SmsMessage> messages) {
		Set<String> smsUserIds = new HashSet<String>();
		for (SmsMessage message : messages){
			String userId = message.getSakaiUserId();
			if ( userId != null ){
				smsUserIds.add(userId);
			}
		}
		return smsUserIds;
	}

	public Locale getUserLocale(String userId) {
	      Locale loc = null;
	      Preferences prefs = preferencesService.getPreferences(userId);
	      ResourceProperties locProps = prefs.getProperties(InternationalizedMessages.APPLICATION_ID);
	      String localeString = locProps.getProperty(InternationalizedMessages.LOCALE_KEY);

	      if (localeString != null)
	      {			String[] locValues = localeString.split("_");
	      if (locValues.length > 1)
	         loc = new Locale(locValues[0], locValues[1]); // language, country
	      else if (locValues.length == 1) 
	         loc = new Locale(locValues[0]); // just language
	      }
	      //the user has no preference set - get the system default
	      if (loc == null ) {
	         String lang = System.getProperty("user.language");
	         String region = System.getProperty("user.region");

	         if (region != null) {
	            LOG.debug("getting system locale for: " + lang + "_" + region);
	            loc = new Locale(lang,region);
	         } else { 
	            LOG.debug("getting system locale for: " + lang );
	            loc = new Locale(lang);
	         }
	      }

	      return loc;
	}

	public String getLocalisedString(String key, Locale locale) {
		final ResourceLoader rb = new ResourceLoader(SMS_BUNDLE);
		if (locale != null) {
			rb.setContextLocale(locale);
		}
    	return rb.getString(key);
	}

	public String getLocalisedString(String key, Locale locale,
			Object[] replacementValues) {
		final ResourceLoader rb = new ResourceLoader(SMS_BUNDLE);
		if (locale != null) {
			rb.setContextLocale(locale);
		}   	
		return rb.getFormattedMessage(key, replacementValues);
	}

	public String getSiteReferenceFromId(String siteId) {
		return siteService.siteReference(siteId);
	}

	public String executeCommand(ShortMessageCommand command, ParsedMessage message, String mobileNumber) {

		String reply = null;
		final String finalSiteId = message.getSite();
		String oldId = null;
		String oldEid = null;
		Session session = null;
		
		LOG.debug("Executing command: " + command.getCommandKey() + " for " + mobileNumber + " with " + message);
				
		if (message.getIncomingUserId() != null) {

			// Otherwise set up a session. Incoming delivery currently takes place in its own thread,
			// but for safety we'll set up and restore the session when done. This should not ever be
			// invoked from a user request thread.

			session = sessionManager.getCurrentSession();
	
			oldId = session.getUserId();
			oldEid = session.getUserEid();
	
			// If session is not anonymous
			String userId = message.getIncomingUserId();
			session.setUserId(userId);
	
			try {
				User u = userDirectoryService.getUser(userId);
				session.setUserEid(u.getEid());
				message.setIncomingUserEid(u.getEid());
			} catch (UserNotDefinedException e) {
				// Shouldn't ever happen as we should be passed a valid user
				LOG.error(e.getMessage(), e);
			}
				
			LOG.debug("Set session for command execution to userid: " + sessionManager.getCurrentSessionUserId());
		}

		// Set up a security advisor for the case where the user is anonymous
		// (unmatched mobile number), or the command does not require a userId
		// (in which case a user who is not a member of the site is still
		// acceptable). In this case the command handler is responsible for enforcing
		// appropriate security (i.e. deciding whether anonymous access is allowed, 
		// and if so to what).
	
		try {
			securityService.pushAdvisor(new SecurityAdvisor() {
				public SecurityAdvice isAllowed(String userId, String function, String reference) {
					if (reference != null && finalSiteId != null 
							&& SiteService.SITE_VISIT.equals(function) 
							&& reference.equals(getSiteReferenceFromId(finalSiteId))) {
						return SecurityAdvice.ALLOWED;
					}
					return SecurityAdvice.PASS;
				}
			});

			// Set site title for convenience of the command
			if (message.getSite() != null) {
				message.setSiteTitle(getSiteTitle(message.getSite()));
			}
			
			// Execute
			reply = command.execute(message, ShortMessageCommand.MESSAGE_TYPE_SMS, mobileNumber);

		} catch (Exception e) {
			LOG.warn("Error executing incoming SMS command: ", e);
		} finally {
			securityService.popAdvisor();
		}

		// Clear session if not anonymous
		if (session != null) {
			
			if (oldId == null) {
				session.clear();
			} else {
				session.setUserId(oldId);
				session.setUserId(oldEid);
			}
		}
		
		return reply;
	}

	public String getBestUserMatch(String siteId, List<String> userIds,
			ShortMessageCommand cmd) {
		
		LOG.debug("Finding users with access to site " + siteId + " from list " + userIds);
		
		// Find users who have access to the site
		
		List<String> usersWithAccess = new ArrayList<String>();
		
		for (String userId: userIds) {
			if (securityService.unlock(userId, SiteService.SITE_VISIT, siteService.siteReference(siteId))) {
				usersWithAccess.add(userId);
			}
		}

		// TODO resolve multiple users with access by a priority order (account or role),
		// and/or a cmd.canExecute(String siteId, String userId) method.
		
		if (!usersWithAccess.isEmpty()) {
			return usersWithAccess.get(0);
		}
		
		// Anonymous allowed?
		if (!cmd.requiresUserId() && !userIds.isEmpty()) {
			return userIds.get(0);
		}
		
		// None of the target set had access
		
		return null;
	}

	public String getSiteTitle(String siteId) {
		
		String title = null;
		
		try {
			Site s = siteService.getSite(siteId);
			title = s.getTitle();
		} catch (IdUnusedException e) {
			LOG.debug("Unknown site id: " + siteId);
		}
				
		return title;
	}

	public String getUserEidFromId(String userId) {
		try {
			return userDirectoryService.getUserEid(userId);
		} catch (UserNotDefinedException e) {
			LOG.debug("no eid found for userId: " + userId);
		}
		return userId;
	}

	public boolean userExists(String userId) {
		try {
			userDirectoryService.getUser(userId);
			return true;
		} catch (UserNotDefinedException e) {
			LOG.debug("userId: " + userId + " not found");
		}
		return false;
	}

	
	public SmsUser getSmsUser(String userId) {
		try {
			User u = userDirectoryService.getUser(userId);
			return new SmsUser(userId, u.getEmail(), u.getFirstName(), u.getLastName(), u.getDisplayName(), u.getEid());
		} catch (UserNotDefinedException e) {
			LOG.debug("user not found for id: " + userId);
		}
		return null;
	}

	public String getSakaiUserRefFromId(String id) {
		return userDirectoryService.userReference(id);
	}


}
