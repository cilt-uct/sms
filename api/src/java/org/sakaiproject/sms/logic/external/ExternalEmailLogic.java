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

import java.util.List;
import java.util.Map;


/**
 * Methods for sending emails 
 * @author dhorwitz
 *
 */
public interface ExternalEmailLogic {
	

	/**
	 * The template key for a task in progress
	 */
	public static final String TEMPLATE_TASK_STARTED = "template.taskStarted";
	
	/**
	 * Template for task sent notification
	 */
	public static final String TEMPLATE_TASK_SENT = "template.taskSent";
	
	
	/**
	 * Template for task exception notification
	 */
	public static final String TEMPLATE_TASK_EXCEPTION = "template.taskException";
	
	/**
	 * Template for task expired notification
	 */
	public static final String TEMPLATE_TASK_EXPIRED = "template.taskExpired";
	
	/**
	 * Template for task over quota
	 */
	public static final String TEMPLATE_TASK_OVER_QUOTA="template.taskOverQuota";
	
	
	/**
	 * Template for task over quota MO
	 */
	public static final String TEMPLATE_TASK_OVER_QUOTA_MO="template.taskOverQuotaMO";
	
	
	/**
	 * Template for task Completed
	 */
	public static final String TEMPLATE_TASK_COMPLETED="template.taskCompleted";


	/**
	 * Template for task Aborted
	 */
	public static final String TEMPLATE_TASK_ABORTED = "template.taskAborted";
	
	
	/**
	 * Template for task Failed 
	 */
	public static final String TEMPLATE_TASK_FAILED = "template.taskFailed";
	
	
	/**
	 * Template for task Failed
	 */
	public static final String TEMPLATE_TASK_INSUFICIENT_CREDITS = "template.taskInsuficientCredits";
	
	
	/**
	 * Send email.
	 * 
	 * @param toAddress
	 *            the to address
	 * @param subject
	 *            the subject
	 * @param body
	 *            the body
	 * 
	 * @return true, if successful
	 */
	public boolean sendEmail(String toAddress, String subject,
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
	 * @param emails
	 *            the emails
	 * 
	 * @return an array of email addresses that this message was sent to
	 */
	public String[] sendEmails(String from, String[] emails,
			String subject, String message);

	/**
	 *  Send a predefined email template to users 
	 * @param from
	 * @param to
	 * @param templateKey
	 * @param replacementValues
	 */
	public void sendEmailTemplate(String from, List<String> userRefsTo, String templateKey, Map<String, String> replacementValues);
	
}
