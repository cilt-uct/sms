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

/**
 * Interface to logic which is external to SMS
 */
public interface ExternalLogic {

	// Permissions for SMS

	// Allow sending of messages in site.
	// Implies Create a new Sms task in current site
	public final static String SMS_SEND = "sms.send";

	// Includes: SMS configuration for current site, View/Edit/Delete any
	// pending task in the site
	// (note that this requires a task to have an associated site/context)
	public final static String SMS_MANAGE = "sms.manage";

	// View list of accounts
	public final static String SMS_ACCOUNTS_VIEW = "sms.accounts.view";

	// Add new account (site or member account)
	public final static String SMS_ACCOUNT_CREATE = "sms.account.create";

	// Edit/change all accounts
	public final static String SMS_ACCOUNT_EDIT = "sms.account.edit";

	// View list of account transactions
	public final static String SMS_TRANSACTION_VIEW = "sms.transaction.view";

	// View all Sms messages with statuses
	public final static String SMS_MESSAGE_VIEW_ALL = "sms.messages.view.all";

	// View own Sms messages
	public final static String SMS_MESSAGE_VIEW_OWN = "sms.messages.view.own";

	/**
	 * Check if this user has super admin access
	 * 
	 * @param userId
	 *            the internal user id (not username)
	 * @return true if the user has admin access, false otherwise
	 */
	public boolean isUserAdmin(String userId);

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
	 * Send e-mails to users ids
	 * 
	 * @param from
	 *            from address to be used
	 * @param toUserIds
	 *            array of sakai user ids
	 * @param subject
	 *            subject of e-mail
	 * @param message
	 *            message of e-mail
	 * @return an array of email addresses that this message was sent to
	 */
	public String[] sendEmailsToUsers(String from, String[] toUserIds,
			String subject, String message);

	/**
	 * Send e-mail to array of e-mail addresses
	 * 
	 * @param from
	 *            from address to be used
	 * @param toEmails
	 *            array of e-mail addresses
	 * @param subject
	 *            subject of e-mail
	 * @param message
	 *            message of e-mail
	 * @return an array of email addresses that this message was sent to
	 */
	public String[] sendEmails(String from, String[] emails, String subject,
			String message);

	/**
	 * Get current number of members in a specific site
	 * 
	 * @param siteId
	 *            unique site id
	 * @return an integer with current number of members in site
	 */
	public int getSiteMemberCount(String siteId);

}
