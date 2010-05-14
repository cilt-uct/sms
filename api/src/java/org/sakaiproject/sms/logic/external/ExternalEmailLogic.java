/**********************************************************************************
 * $URL$
 * $Id$
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

import org.sakaiproject.sms.model.SmsTask;

/**
 * Methods for sending emails 
 * @author dhorwitz
 *
 */
public interface ExternalEmailLogic {
	
	
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

	
	
}
