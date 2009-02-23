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
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.model.hibernate.SmsMessage;
import org.sakaiproject.sms.model.hibernate.SmsTask;

/**
 * Stub implementation of {@link ExternalLogic} for testing
 *
 */
public class ExternalLogicStub implements ExternalLogic {

	/**
	 * The default sakai_userId to be used in development mode.
	 */
	public static final String SMS_DEV_DEFAULT_SAKAI_USER_ID = "SakaiUserID";

	/**
	 * The default sakai_Site_id to be used in development mode.
	 */
	public static final String SMS_DEV_DEFAULT_SAKAI_SITE_ID = "SakaiSiteID";

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
		return 20;
	}

	public String[] sendEmails(SmsTask smsTask, String from, String[] emails,
			String subject, String message) {
		return emails;
	}

	public String[] sendEmailsToUsers(String from, String[] toUserIds,
			String subject, String message) {

		List<String> sent = new ArrayList<String>();
		for (String userId : toUserIds) {
			sent.add(userId + "@example.com");
		}
		return ((String[]) sent.toArray());
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
		Set<SmsMessage> messages = new HashSet<SmsMessage>();

		String[] users;
		int numberOfMessages = (int) Math.round(Math.random() * 100);
		users = new String[100];
		String[] celnumbers = new String[100];
		for (int i = 0; i < users.length; i++) {
			users[i] = "SakaiUser" + i;
			celnumbers[i] = "+2773"
					+ (int) Math.round(Math.random() * 10000000);
		}
		for (int i = 0; i < numberOfMessages; i++) {
			SmsMessage message = new SmsMessage();
			message.setMobileNumber(celnumbers[(int) Math
					.round(Math.random() * 99)]);
			message.setSakaiUserId(users[(int) Math.round(Math.random() * 99)]);
			message.setSmsTask(smsTask);
			messages.add(message);
		}
		return messages;
	}

	public Set<SmsMessage> getSakaiGroupMembers(SmsTask smsTask,
			boolean getMobileNumbers) {
		return this.generateDummySmsMessages(smsTask);

	}

	// for testing purposes when running jetty
	private void sendEmailViaSmtpServer(String toAddress, String subject,
			String body) {
		String host = "127.0.0.1";
		String from = "it3lmb@nwu.ac.za";

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
			Transport.send(message);
		} catch (AddressException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		// System.out.println("Message Send.....");

	}

	public boolean sendEmail(SmsTask smsTask, String toAddress, String subject,
			String body) {
		sendEmailViaSmtpServer(toAddress, subject, body);
		return true;
	}

	public String getSakaiEmailAddressForUserId(String userId) {
		return "it3es@nwu.ac.za";
	}
}
