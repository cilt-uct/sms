/***********************************************************************************
 * MessageCatelogTest.java
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
package org.sakaiproject.sms.logic.smpp.test;

import org.sakaiproject.sms.hibernate.model.constants.SmsHibernateConstants;
import org.sakaiproject.sms.hibernate.util.AbstractBaseTestCase;
import org.sakaiproject.sms.hibernate.util.HibernateUtil;
import org.sakaiproject.sms.smpp.util.MessageCatelog;

/**
 * Test case to test the MessageCatelog class functionality
 * 
 * @author julian@psybergate.com
 * @version 1.0
 * @created 15-Jan-2009
 */
public class MessageCatelogTest extends AbstractBaseTestCase {

	static {
		System.out.println("Static setUp");
		HibernateUtil.createSchema();
	}

	/**
	 * Test message catelog.
	 */
	public void testMessageCatelog() {
		String subject = null;
		String body = null;
		try {
			subject = MessageCatelog.getMessage(
					"messages.notificationSubjectStarted", "654");
			body = MessageCatelog.getMessage(
					"messages.notificationBodyStarted", "35", "R12.45");
		} catch (Exception e) {
			fail(e.getMessage());
		}
		System.out.println("Subject" + subject);
		System.out.println("Body" + body);
	}

	public void testMessageCatelog_MessageNotFound() {
		String message = null;
		try {
			message = MessageCatelog.getMessage("messages.nonexistent");
			assertTrue(message
					.equals(SmsHibernateConstants.CATELOG_MESSAGE_NOT_FOUND));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
}
