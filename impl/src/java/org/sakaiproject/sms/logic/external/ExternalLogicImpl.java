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

import java.util.Arrays;
import java.util.Collection;

import javax.mail.internet.InternetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.site.api.SiteService;
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
	 * @see {@link ExternalLogic#getGroupMemberCount(String)}
	 */
	public int getGroupMemberCount(String reference) {
		System.out.println(reference);
		AuthzGroup group = (AuthzGroup) entityBroker.fetchEntity(reference);
		if (group == null) {
			return 15; // for testing
		}
		return group.getMembers().size();
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
	 * @see ExternalLogic#sendEmailsToUsers(String, String[], String, String)
	 */
	public String[] sendEmailsToUsers(String from, String[] toUserIds,
			String subject, String message) {
		// InternetAddress fromAddress;
		// try {
		// fromAddress = new InternetAddress(from);
		// } catch (AddressException e) {
		// // cannot recover from this failure
		// throw new IllegalArgumentException("Invalid from address: " + from,
		// e);
		// }
		//
		// List<User> l = new ArrayList<User>(); // fill this with users
		// for (int i = 0; i < toUserIds.length; i++) {
		// User user = null;
		// try {
		// user = userDirectoryService.getUser(toUserIds[i]);
		// } catch (UserNotDefinedException e) {
		// log.debug("Cannot find user object by id:" + toUserIds[i]);
		// try {
		// user = userDirectoryService.getUserByEid(toUserIds[i]);
		// } catch (UserNotDefinedException e1) {
		// log.error(
		// "Invalid user: Cannot find user object by id or eid:"
		// + toUserIds[i], e1);
		// }
		// }
		// l.add(user);
		// }
		//
		// // email address validity is checked at entry but value can be null
		// List<String> toEmails = new ArrayList<String>();
		// for (ListIterator<User> iterator = l.listIterator();
		// iterator.hasNext();) {
		// User u = iterator.next();
		// if (u.getEmail() == null || "".equals(u.getEmail())) {
		// iterator.remove();
		// log.warn("sendEmails: Could not get an email address for "
		// + u.getDisplayName() + " (" + u.getId() + ")");
		// } else {
		// toEmails.add(u.getEmail());
		// }
		// }
		//
		// if (l == null || l.size() <= 0) {
		// log
		// .warn("No users with email addresses found in the provided userIds cannot send email so exiting");
		// return new String[] {};
		// }

		// return sendEmails(fromAddress, toEmails, subject, message);
		return sendEmails(null,
				new String[] { "1@example.com", "2@example.com" }, subject,
				message);
	}

	/**
	 * @see ExternalLogic#sendEmails(String, String[], String, String)
	 */
	public String[] sendEmails(String from, String[] emails, String subject,
			String message) {
		// InternetAddress fromAddress;
		// try {
		// fromAddress = new InternetAddress(from);
		// } catch (AddressException e) {
		// // cannot recover from this failure
		// throw new IllegalArgumentException("Invalid from address: " + from,
		// e);
		// }
		// List<String> toEmails = new ArrayList<String>();
		// for (String email : emails) {
		// if (email != null && !"".equals(email)) {
		// toEmails.add(email);
		// }
		// }

		// return sendEmails(fromAddress, toEmails, subject, message);
		return sendEmails(null, Arrays.asList(emails), subject, message);
	}

	/**
	 * Actual sending of e-mail
	 */
	private String[] sendEmails(InternetAddress fromAddress,
			Collection<String> toEmails, String subject, String message) {
		// InternetAddress[] replyTo = new InternetAddress[1];
		// List<InternetAddress> listAddresses = new
		// ArrayList<InternetAddress>();
		// EmailValidator emailValidator = EmailValidator.getInstance();
		//
		// for (Iterator<String> it = toEmails.iterator(); it.hasNext();) {
		// String email = it.next();
		// try {
		// if (emailValidator.isValid(email)) {
		// InternetAddress toAddress = new InternetAddress(email);
		// listAddresses.add(toAddress);
		// }
		// } catch (AddressException e) {
		// log.error("Invalid to address: " + email
		// + ", cannot send email", e);
		// }
		// }
		//
		// replyTo[0] = fromAddress;
		// InternetAddress[] toAddresses = listAddresses
		// .toArray(new InternetAddress[listAddresses.size()]);
		// emailService.sendMail(fromAddress, toAddresses, subject, message,
		// null,
		// null, null);
		//
		// // now we send back the list of people who the email was sent to
		// String[] addresses = new String[toAddresses.length];
		// for (int i = 0; i < toAddresses.length; i++) {
		// addresses[i] = toAddresses[i].getAddress();
		// }
		// return addresses;
		return ((String[]) toEmails.toArray());
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

}
