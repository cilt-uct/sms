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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.EmailValidator;
import org.sakaiproject.alias.api.Alias;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.PermissionsHelper;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sms.model.hibernate.SmsMessage;
import org.sakaiproject.sms.model.hibernate.SmsTask;
import org.sakaiproject.sms.model.hibernate.constants.SmsConstants;
import org.sakaiproject.sms.model.smpp.SmsSmppProperties;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * Implementation of {@link ExternalLogic} for Sakai-specific code.
 */
public class ExternalLogicImpl implements ExternalLogic {

	private static final Log LOG = LogFactory.getLog(ExternalLogicImpl.class);

	public final static String NO_LOCATION = "noLocationAvailable";

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

	private EmailService emailService;

	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
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
				if (role.equals(member.getRole().getId())) {
					members.add(member);
				}
			}

		} else { // Any other authz group
			String groupId = EntityReference.getIdFromRefByKey(entityReference, "group");
			if ( groupId != null ){
				AuthzGroup group = siteService.findGroup(groupId);
				if(group != null){
					LOG.debug("Found group " + group.getDescription() + " with id " + groupId + " of size: " + group.getMembers().size());
					members.addAll(group.getMembers());
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
				if (mobileNumber == null || mobileNumber.equals("")) {
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

		if (smsTask.getId() == null) {
			LOG.debug("Getting recipient numbers for preliminary task ");
		} else {
			LOG.debug("Getting recipient numbers for task " + smsTask.getId());
		}
		Set<SmsMessage> messages = new HashSet<SmsMessage>();
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
				if (mobileNr != null && !mobileNr.equals("")) {
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

	/**
	 * @see ExternalLogic#sendEmails(String, String[], String, String)
	 */
	public String[] sendEmails(SmsTask smsTask, String from, String[] emails,
			String subject, String message) {
		InternetAddress fromAddress;
		try {
			fromAddress = new InternetAddress(from);
		} catch (AddressException e) {
			// cannot recover from this failure
			throw new IllegalArgumentException("Invalid from address: " + from,
					e);
		}
		List<String> toEmails = new ArrayList<String>();
		for (String email : emails) {
			if (email != null && !"".equals(email)) {
				toEmails.add(email);
			}
		}

		return sendEmails(smsTask, fromAddress, toEmails, subject, message);
		// return sendEmails(null, Arrays.asList(emails), subject, message);
	}

	/**
	 * Actual sending of e-mail via sakai email service
	 */
	private String[] sendEmails(SmsTask smsTask, InternetAddress fromAddress,
			Collection<String> toEmails, String subject, String message) {

		if (!serverConfigurationService.getBoolean("sms.notify.email", false)) {
			LOG.debug("Enable notification is disabled (sms.notify.email=false in sakai.properties)");
			return new String[0];
		}
		
		InternetAddress[] replyTo = new InternetAddress[1];
		List<InternetAddress> listAddresses = new ArrayList<InternetAddress>();
		EmailValidator emailValidator = EmailValidator.getInstance();

		for (Iterator<String> it = toEmails.iterator(); it.hasNext();) {
			String email = it.next();
			try {
				if (emailValidator.isValid(email)) {
					InternetAddress toAddress = new InternetAddress(email);
					listAddresses.add(toAddress);
				}
			} catch (AddressException e) {
				LOG.warn("Invalid to address: " + email
						+ ", cannot send email", e);
			}
		}

		replyTo[0] = fromAddress;
		InternetAddress[] toAddresses = listAddresses
		.toArray(new InternetAddress[listAddresses.size()]);
		setupSession(smsTask.getSenderUserId());
		emailService.sendMail(fromAddress, toAddresses, subject, message, null,
				null, null);

		// now we send back the list of people who the email was sent to
		String[] addresses = new String[toAddresses.length];
		for (int i = 0; i < toAddresses.length; i++) {
			addresses[i] = toAddresses[i].getAddress();
		}
		return addresses;
		// return ((String[]) toEmails.toArray());
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
		return siteService.siteExists(siteId);
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

	public String getSakaiSiteContactEmail() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean sendEmail(SmsTask smsTask, String toAddress, String subject,
			String body) {

		LOG.debug("Sending email to:" + toAddress + " subject:" + subject
				+ " body:" + body);
		String from = "smstesting@sakai";
		sendEmails(smsTask, from, new String[] { toAddress }, subject, body);
		return true;
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
		if (smscAddress == null || smscAddress.equals("")) {
			LOG.debug("sms.SMSCAddress not found");
		} else {
			smsSmppProperties.setSMSCAddress(smscAddress);
		}

		String smscPort = serverConfigurationService.getString(
		"sms.SMSCPort").trim();

		if (smscPort == null || smscPort.equals("")) {
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

		if (smscUserName == null || smscUserName.equals("")) {
			LOG.debug("sms.SMSCUserName not found");
		} else {
			smsSmppProperties.setSMSCUsername(smscUserName);
		}

		String smscPassword = serverConfigurationService.getString(
		"sms.SMSCPassword").trim();

		if (smscPassword == null || smscPassword.equals("")) {
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
		if (systemType == null || systemType.equals("")) {
			LOG.debug("systemType not found");
		} else {
			smsSmppProperties.setSystemType(systemType);
		}
		
		LOG.debug("Read properties from ServerConfigurationService");

		return smsSmppProperties;
	}

	public String getSiteFromAlias(String alias) {
		String target;
		try {
			target = aliasService.getTarget(alias);
			if (isValidSite(target)) {
				return target;
			} else {
				return null;
			}
		} catch (IdUnusedException e) {
			LOG.error("Undefined alias used: " + alias);
			return null;
		}

	}

	public String[] getAllAliasesAsArray() {
		List<Alias> aliases = aliasService.getAliases(1, aliasService
				.countAliases());
		String[] toReturn = new String[aliases.size()];
		for (int i = 0; i < aliases.size(); i++) {
			toReturn[i] = aliases.get(i).getId();
		}
		return toReturn;
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
	        if( allMembers.size() >0){
				for (Member member : allMembers) {
					if ( member.isActive() ) {
						activeUserIds.add(member.getUserId());
					}
				}
	        }
			if( activeUserIds.size() > 0 ){
				userIds = mobileNumberHelper.getUsersWithMobileNumbers( activeUserIds );
			}
			if( userIds.size() > 0 ){
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
		if (userIds != null && userIds.size() > 0) {
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
		if (sakaiUserIds != null && sakaiUserIds.size() > 0) {
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
		
		EventTrackingService.post(EventTrackingService.newEvent(event, ref, context, true, NotificationService.NOTI_NONE));
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

}
