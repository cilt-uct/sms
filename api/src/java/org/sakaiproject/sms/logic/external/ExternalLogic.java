/**********************************************************************************
 * $URL:$
 * $Id:$
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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.sakaiproject.sms.model.hibernate.SmsMessage;
import org.sakaiproject.sms.model.hibernate.SmsTask;
import org.sakaiproject.sms.model.smpp.SmsSmppProperties;
import org.sakaiproject.user.api.User;

/**
 * Interface to logic which is external to SMS.
 */
public interface ExternalLogic {

	// Events for task actions
	
	public final static String SMS_EVENT_TASK_CREATE = "sms.task.new";
	public final static String SMS_EVENT_TASK_REVISE = "sms.task.revise";
	public final static String SMS_EVENT_TASK_DELETE = "sms.task.delete";

	public final static String SMS_EVENT_ACCOUNT_CREATE = "sms.account.new";
	public final static String SMS_EVENT_ACCOUNT_DELETE = "sms.account.delete";
	public final static String SMS_EVENT_ACCOUNT_REVISE = "sms.account.revise";
	public final static String SMS_EVENT_ACCOUNT_CREDIT = "sms.account.credit";
	
	// Permissions for SMS

	/**
	 * Allow sending of messages in site.
	 * Implies Create a new Sms task in current site
	 * 
	 */
	public final static String SMS_SEND = "sms.send";
	
	/**
	 * Preference key for international dialing prefix (default is 27)
	 */
	public static final String PREF_INTERNATIONAL_PREFIX = "sms.interNationalPrefix";
	
	/**
	 * The default value for the international dialing prefix
	 */
	public static final String PREF_INTERNATIONAL_PREFIX_DEFAULT = "27";
	
	/**
	 * Check if this user has super admin access
	 * 
	 * @param userId
	 *            the internal user id (not username)
	 * @return true if the user has admin access, false otherwise
	 */
	public boolean isUserAdmin(String userId);

	/**
	 * @return the current sakai user id (not username)
	 */
	public String getCurrentUserId();

	/**
	 * @return the current sakai site id
	 */
	public String getCurrentSiteId();

	/**
	 * Check if a user has a specified permission within a context, primarily a
	 * convenience method and passthrough
	 * 
	 * @param userId
	 *            the internal user id (not username)
	 * @param permission
	 *            a permission string constant
	 * @param locationId
	 *            a unique id which represents the current location of the user
	 *            (entity reference)
	 * @return true if allowed, false otherwise
	 */
	public boolean isUserAllowedInLocation(String userId, String permission,
			String locationId);

	/**
	 * Retrieves mobile number for user
	 * 
	 * @param userID
	 *            the internal user id (not username)
	 * 
	 * @return mobile number for user
	 */
	public String getSakaiMobileNumber(String userID);

	/**
	 * Send e-mail to array of e-mail addresses.
	 * 
	 * @param from
	 *            from address to be used
	 * @param subject
	 *            subject of e-mail
	 * @param message
	 *            message of e-mail
	 * @param smsTask
	 *            the sms task
	 * @param emails
	 *            the emails
	 * 
	 * @return an array of email addresses that this message was sent to
	 */
	public String[] sendEmails(SmsTask smsTask, String from, String[] emails,
			String subject, String message);

	/**
	 * Return the actual list of message constructed for each user in the group
	 * 
	 * @param smsTask
	 * @param reference
	 * @return
	 */
	public Set<SmsMessage> getSakaiGroupMembers(SmsTask smsTask,
			boolean getMobileNumbers);

	/**
	 * Retrieve the friendly Sakai Display Name. Defaults to Sakai username if
	 * no display name is set.
	 * 
	 * @return Display name or username
	 */
	public String getSakaiUserDisplayName(String userId);

	/**
	 *returns the email address of the specific user
	 * 
	 * @param userId
	 * @return
	 */
	public String getSakaiEmailAddressForUserId(String userId);

	/**
	 * Get the display id for the current user.
	 * 
	 * @return display id
	 * 
	 */

	public String getCurrentUserDisplayId();

	/**
	 * Check if id supplied is a valid site id
	 * 
	 * @param siteId
	 *            Unique id of a site
	 * @return true if valid, false otherwise
	 */
	public boolean isValidSite(String siteId);

	/**
	 * Check if id supplied is a valid user id
	 * 
	 * @param userId
	 *            Unique id of a user
	 * @return true if valid, false otherwise
	 */
	public boolean isValidUser(String userId);

	/**
	 * Get the sakai contacts email.
	 * 
	 * @return sakaiSiteContactEmail
	 */
	public String getSakaiSiteContactEmail();

	/**
	 * Send email.
	 * 
	 * @param smsTask
	 *            the sms task
	 * @param toAddress
	 *            the to address
	 * @param subject
	 *            the subject
	 * @param body
	 *            the body
	 * 
	 * @return true, if successful
	 */
	public boolean sendEmail(SmsTask smsTask, String toAddress, String subject,
			String body);

	/**
	 * Gets the smsppProperties from the sakai.properties
	 * 
	 * @param smsSmppProperties
	 * @return
	 */
	public SmsSmppProperties getSmppProperties(SmsSmppProperties smsSmppProperties);

	/**
	 * Returns siteId from alias (null if none found)
	 * 
	 * @param alias
	 * @return
	 */
	public String getSiteFromAlias(String alias);

	/**
	 * Returns all aliases as String array
	 * 
	 * @return
	 */
	public String[] getAllAliasesAsArray();

	/**
	 * Retrieve userIds with supplied mobile number
	 * 
	 * @param mobileNumber
	 * @return
	 */
	List<String> getUserIdsFromMobileNumber(String mobileNumber);

	public TimeZone getLocalTimeZone();

	public String getSakaiUserSortName(String sakaiUserId);

	public Map<String, String> getSakaiRolesForSite(String siteId);

	/**
	 * Get site group in title alphabetical order
	 * @param siteId
	 * @return groupId as key and title as Value
	 */
	public Map<String, String> getSakaiGroupsForSite(String siteId);

	public String getSakaiGroupNameFromId(String siteId, String groupId);

	public String getEntityRealIdFromRefByKey(String entity, String key);

	public String getEntityPrefix(String entity);

	public String getSmsContactEmail();

	/**
	 * Sets up the proper session attributes for the benefit of the sms
	 * permissions producer
	 * 
	 * @param permissionPrefix
	 *            this tools permission prefix with a dot eg. 'sms.' for the sms
	 *            tool.
	 */
	public void setUpSessionPermissions(String permissionPrefix);

	/**
	 * Filter out site members who do not have mobile numbers in their profile and inactive users.
	 * @param siteId
	 * 			  the site id
	 * @return
	 * 			  A list of users 
	 */
	public List<User> getUsersWithMobileNumbersOnly(String siteId);

	/**
	 * Check if a user has permission to update a site
	 * 
	 * @param userId
	 *            the internal user id (not username)
	 * @param locationId
	 *            a unique id which represents the current location of the user
	 *            (entity reference)
	 * @return true if allowed, false otherwise
	 */
	public boolean isUserAllowedSiteUpdate(String userId, String locationId);
	
	public Map<String, String> getSakaiUsernames(Set<String> userIds);
	
	/**
	 * Is this node set to bind to the gateway
	 * @return
	 */
	public boolean isNodeBindToGateway();
	
	/**
	 * The international prefix for the server (eg 27 for south africa)
	 * @return
	 */
	public String getLocalInternationalPrefix();

	/**
	 * Get a list of sakai users sortnames with corresponding UUIds.
	 * @param sakaiUserIds
	 * @return each map entry has a userId and corresponding sortname
	 */
	public Map<String, String> getSakaiUserDisplayNames(Set<String> sakaiUserIds);
	
	/**
	 * Log an event
	 * @return 
	 */
	public void postEvent(String event, String ref, String context);

}
