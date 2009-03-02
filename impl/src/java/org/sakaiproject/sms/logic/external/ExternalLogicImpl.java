/***********************************************************************************
 * ExternalLogic.java
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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.EmailValidator;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sms.model.hibernate.SmsMessage;
import org.sakaiproject.sms.model.hibernate.SmsTask;
import org.sakaiproject.sms.model.hibernate.constants.SmsHibernateConstants;
import org.sakaiproject.sms.model.smpp.SmsSmppProperties;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * Implementation of {@link ExternalLogic} with Sakai-specific code commented
 * out for the moment
 * 
 */
public class ExternalLogicImpl implements ExternalLogic {

	private static Log log = LogFactory.getLog(ExternalLogicImpl.class);

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

	public void init() {
		log.debug("init");
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
			String context = toolManager.getCurrentPlacement().getContext();
			location = context;
			// Site s = siteService.getSite( context );
			// location = s.getReference(); // get the entity reference to the
			// site
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

		log.debug("isUserAllowedInLocation(" + userId + ", " + permission + ","
				+ locationId + ")");
		Boolean allowed = true;
		if (userId != null)
			allowed = securityService.unlock(userId, permission, locationId);
		else
			allowed = securityService.unlock(permission, locationId);

		log.debug("allowed: " + allowed);

		return allowed;
	}

	public String getSakaiMobileNumber(String userId) {
		return mobileNumberHelper.getUserMobileNumber(userId);
	}

	/**
	 * Sets up session for userId if this is anonymous session
	 */
	private void setupSession(String userId) {
		// Get current session (if no session NonPortableSession will be created
		// in default implementation)
		Session session = sessionManager.getCurrentSession();

		// If session is anonymous
		if (session.getUserId() == null) {
			session.setUserId(userId);
			try {
				session.setUserEid(userDirectoryService.getUser(userId)
						.getEid());
			} catch (UserNotDefinedException e) {
				log.error(e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Set<Object> getMembersForEntityRef(String entityReference) {
		Set members = new HashSet<Object>();
		Object obj = entityBroker.fetchEntity(entityReference);

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

		} else if (obj instanceof AuthzGroup) { // Any other authz group
			AuthzGroup group = (AuthzGroup) obj;
			members.addAll(group.getMembers());
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

		log.info("Getting group members for : " + entityReference + " (size = "
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
				if (mobileNumber == null) {
					addMemberToDelList = false;
					if (SmsHibernateConstants.SMS_DEV_MODE) {
						mobileNumber = "9999999"; // for testing
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
		if (smsTask.getDeliveryEntityList() != null) {
			// list of references to groups, roles etc.
			for (String reference : smsTask.getDeliveryEntityList()) {
				messages.addAll(getSakaiEntityMembersAsMessages(smsTask,
						reference, getMobileNumbers));
			}
		} else if (smsTask.getDeliveryGroupId() != null) {
			// a single group reference
			messages.addAll(getSakaiEntityMembersAsMessages(smsTask, smsTask
					.getDeliveryGroupId(), getMobileNumbers));

		} else if (smsTask.getDeliveryMobileNumbersSet() != null) {
			// a list of mobile numbers, not necessarily from sakai users
			for (String mobileNumber : smsTask.getDeliveryMobileNumbersSet()) {
				SmsMessage message = new SmsMessage();
				message.setMobileNumber(mobileNumber);
				message.setSmsTask(smsTask);
				messages.add(message);
			}
		} else if (smsTask.getDeliveryUserId() != null) {
			// a single sakai user id, for incoming messages
			messages.addAll(getSakaiEntityMembersAsMessages(smsTask, smsTask
					.getDeliveryUserId(), getMobileNumbers));
		}
		return messages;

	}

	public String getSakaiUserDisplayName(String userId) {
		String result = null;
		if (isValidUser(userId)) { // user may be null or not a Sakai user
			try {
				result = userDirectoryService.getUser(userId).getDisplayName();
			} catch (UserNotDefinedException e) {
				log
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
				log.error("Invalid to address: " + email
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
			e.printStackTrace();
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

		log.debug("Sending email to:" + toAddress + " subject:" + subject
				+ " body:" + body);
		String from = "smstesting@sakai";
		sendEmails(smsTask, from, new String[] { toAddress }, subject, body);
		return true;
	}

	public String getSakaiEmailAddressForUserId(String userId) {
		try {
			return userDirectoryService.getUser(userId).getEmail();
		} catch (UserNotDefinedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	public SmsSmppProperties getSmppProperties(
			SmsSmppProperties smsSmppProperties) {
		try {
			smsSmppProperties.setSMSCAdress(serverConfigurationService
					.getString("sms.SMSCAdress"));
			smsSmppProperties.setSMSCPort(Integer
					.valueOf(serverConfigurationService
							.getString("sms.SMSCPort")));
			smsSmppProperties.setSMSCUsername(serverConfigurationService
					.getString("sms.SMSCUserName"));
			smsSmppProperties.setSMSCPassword(serverConfigurationService
					.getString("sms.SMSCPassword"));
		} catch (Exception e) {
			// smpp properties is not set up in sakai.properties, so we are
			// going to use smpp.properties
		}
		return smsSmppProperties;

	}
}
