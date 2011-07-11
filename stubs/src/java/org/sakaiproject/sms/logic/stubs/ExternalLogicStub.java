/***********************************************************************************
 * ExternalLogicStub.java
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
package org.sakaiproject.sms.logic.stubs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.incoming.ParsedMessage;
import org.sakaiproject.sms.logic.incoming.ShortMessageCommand;
import org.sakaiproject.sms.model.SmsMessage;
import org.sakaiproject.sms.model.SmsTask;
import org.sakaiproject.sms.model.SmsUser;
import org.sakaiproject.sms.model.constants.SmsConstants;
import org.sakaiproject.sms.model.smpp.SmsSmppProperties;
import org.sakaiproject.user.api.User;

/**
 * Stub implementation of {@link ExternalLogic} for testing
 * 
 */
public class ExternalLogicStub implements ExternalLogic {

	private static final Log LOG = LogFactory.getLog(ExternalLogicStub.class);
	/**
	 * The default sakai_userId to be used in development mode.
	 */
	public static final String SMS_DEV_DEFAULT_SAKAI_USER_ID = "SakaiUserID";

	/**
	 * The default sakai_Site_id to be used in development mode.
	 */
	public static final String SMS_DEV_DEFAULT_SAKAI_SITE_ID = SmsConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID;

	public String getSakaiMobileNumber(String userID) {
		return "0123456789";
	}

	public boolean isUserAdmin(String userId) {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean isUserAllowedInLocation(String userId, String permission,
			String locationId) {
		// TODO Auto-generated method stub
		return true;
	}

	public int getGroupMemberCount(String reference) {
		return 10;
	}

	public String[] sendEmails(SmsTask smsTask, String from, String[] emails,
			String subject, String message) {
		return emails;
	}

	public String[] sendEmailsToUsers(String from, String[] toUserIds,
			String subject, String message) {

		return toUserIds;
	}

	public String getCurrentSiteId() {
		return SMS_DEV_DEFAULT_SAKAI_SITE_ID;
	}

	public String getCurrentUserId() {
		return SMS_DEV_DEFAULT_SAKAI_USER_ID;
	}

	public String getCurrentUserDisplayId() {
		return "StubCurrentUserDisplayId";
	}

	public boolean isValidSite(String siteId) {
		return SMS_DEV_DEFAULT_SAKAI_SITE_ID.equals(siteId);
	}

	public boolean isValidUser(String userId) {
		return SMS_DEV_DEFAULT_SAKAI_USER_ID.equals(userId);
	}

	public String getSakaiUserDisplayName(String userId) {
		return "John Smith";
	}

	public String getSakaiSiteContactEmail() {
		return "louis@nwu.ac.za";

	}

	/**
	 * So when running in jetty we generate a random number of users with random
	 * mobile numbers. * @param smsTask
	 * 
	 * @return
	 */
	private Set<SmsMessage> generateDummySmsMessages(SmsTask smsTask) {
		final Set<SmsMessage> messages = new HashSet<SmsMessage>();

		String[] users;
		final int numberOfMessages = (int) Math.round(Math.random() * 10);
		users = new String[10];
		String[] celnumbers = new String[10];
		for (int i = 0; i < users.length; i++) {
			users[i] = "SakaiUser" + i;
			celnumbers[i] = "+2773"
					+ (int) Math.round(Math.random() * 10000000);
		}
		for (int i = 0; i < numberOfMessages; i++) {
			final SmsMessage message = new SmsMessage();
			message.setMobileNumber(celnumbers[(int) Math
					.round(Math.random() * 9)]);
			message.setSakaiUserId(users[(int) Math.round(Math.random() * 9)]);
			message.setSmsTask(smsTask);
			messages.add(message);
		}
		return messages;
	}

	public Set<SmsMessage> getSakaiGroupMembers(SmsTask smsTask,
			boolean getMobileNumbers) {
		return this.generateDummySmsMessages(smsTask);

	}

	// for testing purposes when running sms in jetty
	private void sendEmailViaSmtpServer(String toAddress, String subject,
			String body) {
		final String host = "127.0.0.1";
		final String from = "it3lmb@nwu.ac.za";

		LOG.debug("Sending email to:" + toAddress + " subject:" + subject
				+ " body:" + body);
		// Get system properties
		Properties properties = System.getProperties();

		// Setup mail server
		properties.setProperty("mail.smtp.host", host);
		// properties.setProperty("mail.smtp.auth", "true");
		// properties.setProperty("mail.smtp.user", mailUser);
		// properties.setProperty("password", mailPassword);

		Session session = Session.getDefaultInstance(properties);
		MimeMessage message = new MimeMessage(session);
		try {
			message.setFrom(new InternetAddress(from));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(
					toAddress));
			message.setSubject(subject);
			message.setText(body);
			// Transport.send(message); disable email in jetty deployments
		} catch (AddressException e) {
			LOG.error(e.getMessage(), e);
		} catch (MessagingException e) {
			LOG.error(e.getMessage(), e);
		}
		// System.out.println("Message Send.....");

	}

	public boolean sendEmail(SmsTask smsTask, String toAddress, String subject,
			String body) {
		sendEmailViaSmtpServer(toAddress, subject, body);
		return true;
	}

	public String getSakaiEmailAddressForUserId(String userId) {
		return "louis@nwu.ac.za";

	}

	public SmsSmppProperties getSmppProperties() {
		return null;
	}

	public String getSiteFromAlias(String alias) {
		return SMS_DEV_DEFAULT_SAKAI_SITE_ID;
	}

	public String[] getAllAliasesAsArray() {
		return new String[] { SMS_DEV_DEFAULT_SAKAI_SITE_ID };
	}

	public List<String> getUserIdsFromMobileNumber(String mobileNumber) {
		List<String> list = new ArrayList<String>();
		list.add(SMS_DEV_DEFAULT_SAKAI_USER_ID);
		return list;
	}

	public TimeZone getLocalTimeZone() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSakaiUserSortName(String sakaiUserId) {
		// TODO Auto-generated method stub
		return "--------";
	}

	public Map<String, String> getSakaiGroupsForSite(String siteId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, String> getSakaiRolesForSite(String siteId) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSakaiGroupNameFromId(String siteId, String groupId) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getEntityRealIdFromRefByKey(String entity, String key) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getEntityPrefix(String entity) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSmsContactEmail() {
		// TODO Auto-generated method stub
		return null;
	}

	public SmsSmppProperties getSmppProperties(
			SmsSmppProperties smsSmppProperties) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, String> getUsersWithMobileNumbersOnly() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setUpSessionPermissions(String permissionPrefix) {
		// TODO Auto-generated method stub

	}

	public boolean isUserAllowedSiteUpdate(String userId, String locationId) {
		// TODO Auto-generated method stub
		return false;
	}

	public Map<String, User> getSakaiUsers(Set<String> userIds) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<User> getUsersWithMobileNumbersOnly(String ref) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isBindThisNode() {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean isNodeBindToGateway() {
		return true;
	}

	public Map<String, String> getSakaiUserDisplayNames(Set<String> sakaiUserIds) {
		// TODO Auto-generated method stub
		return null;
	}

	public void postEvent(String event, String ref, String context) {
		return;
	}

	public Set<String> getUserIdsFromSmsMessages(Collection<SmsMessage> messages) {
		return null;
	}

	public List<String> getAllSiteAliases() {
		
		List<String> siteAliases = new ArrayList<String>();
		siteAliases.add(SMS_DEV_DEFAULT_SAKAI_SITE_ID);
		return siteAliases;
	}

	public String getLocalisedString(String key, Locale locale) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLocalisedString(String key, Locale locale,
			Object[] replacementValues) {
		// TODO Auto-generated method stub
		return null;
	}

	public Locale getUserLocale(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSiteReferenceFromId(String siteId) {
		return "/site/" + siteId;
	}

	public String executeCommand(ShortMessageCommand command, ParsedMessage msg, String mobileNr) {
		return command.execute(msg, ShortMessageCommand.MESSAGE_TYPE_SMS, mobileNr);
	}

	public String getBestUserMatch(String siteId, List<String> userIds,
			ShortMessageCommand cmd) {

		return userIds.isEmpty() ? null : userIds.get(0);
	}

	public String getSiteTitle(String siteId) {
		return "Site Title";
	}

	public String getUserEidFromId(String userId) {
		return userId;
	}

	public boolean userExists(String userId) {
		return true;
	}

	public SmsUser getSmsUser(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSakaiUserRefFromId(String id) {
		// TODO Auto-generated method stub
		return null;
	}

}
