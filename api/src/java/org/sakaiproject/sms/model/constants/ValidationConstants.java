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
package org.sakaiproject.sms.model.constants;

/**
 * Constant class to hold key values for validation methods.
 * 
 * @author julian@psybergate.com
 * @version 1.0
 * @created 12-Jan-2009
 */
public class ValidationConstants {

	/** Insuficient credit. */
	public final static String INSUFFICIENT_CREDIT = "sms.errors.task.credit.insufficient";

	/** Message body too long. */
	public final static String MESSAGE_BODY_TOO_LONG = "sms.errors.task.messageBody.tooLong";

	/** Mobile number too long. */
	public final static String MOBILE_NUMBER_TOO_LONG = "sms.errors.message.mobileNumber.tooLong";

	/** Mobile number invalid. */
	public final static String MOBILE_NUMBER_INVALID = "sms.errors.message.mobileNumber.invalid";

	/** Mobile number empty. */
	public final static String MOBILE_NUMBER_EMPTY = "sms.errors.message.mobileNumber.empty";

	/** Message body empty. */
	public final static String MESSAGE_BODY_EMPTY = "sms.errors.task.messageBody.empty";

	/** Sakai site id empty. */
	public final static String TASK_SAKAI_SITE_ID_EMPTY = "sms.errors.task.sakaiSiteId.empty";

	/** Date to send empty. */
	public final static String TASK_DATE_TO_EXPIRE_EMPTY = "sms.errors.task.expire.empty";

	/** Invalid account id */
	public final static String TASK_ACCOUNT_INVALID = "sms.errors.task.account.invalid";

	/** The task account empty. */
	public final static String TASK_ACCOUNT_EMPTY = "sms.errors.task.account.empty";

	/** Date created empty */
	public final static String TASK_DATE_CREATED_EMPTY = "sms.errors.task.dateCreated.empty";

	/** Date to send empty. */
	public final static String TASK_DATE_TO_SEND_EMPTY = "sms.errors.task.dateToSend.empty";

	/** Date to send after expiry. */
	public final static String TASK_DATE_TO_SEND_AFTER_EXPIRY = "sms.errors.task.dateToSend.afterExpiry";

	/** Date to send equal to expiry. */
	public final static String TASK_DATE_TO_SEND_EQUALS_EXPIRY = "sms.errors.task.dateToSend.equalsExpiry";

	/** Status code empty. */
	public final static String TASK_STATUS_CODE_EMPTY = "sms.errors.task.statusCode.empty";

	/** Message type empty. */
	public final static String TASK_MESSAGE_TYPE_EMPTY = "sms.errors.task.messageType.empty";

	/** Sender user name empty. */
	public final static String TASK_SENDER_USER_NAME_EMPTY = "sms.errors.task.senderUserName.empty";

	/** Max time to live invalid. */
	public final static String TASK_MAX_TIME_TO_LIVE_INVALID = "sms.errors.task.maxTimeToLive.invalid";

	/** Delivery group id empty. */
	public final static String TASK_DELIVERY_GROUP_ID_EMPTY = "sms.errors.task.deliveryGroupId.empty";

	/** Message sakai user id empty */
	public final static String MESSAGE_SAKAI_USER_ID_EMPTY = "sms.errors.message.sakaiUserId.empty";

	/** Message status empty. */
	public final static String MESSAGE_STATUS_CODE_EMPTY = "sms.errors.message.statusCode.empty";

	/** Message task id empty. */
	public final static String MESSAGE_TASK_ID_EMPTY = "sms.errors.message.taskId.empty";
	
	/** Date is an incorrect format. */
	public final static String DATE_FORMAT_INCORRECT = "sms.errors.date.format.incorrect";
	
	/** Send date is in the past. */
	public final static String DATE_SEND_IN_PAST = "sms.errors.date.send.inpast";
	
	/** Anonymous user must not view memberships. */
	public final static String USER_ANONYMOUS_CANNOT_VIEW_MEMBERS = "sms.permission-denied.site.anonymous";
	
	/** This user must not view memberships due to permission issues. */
	public final static String USER_CANNOT_VIEW_MEMBERS = "sms.permission-denied.site";
	
	/** This user is not allowed to send an SMS. */
	public final static String USER_NOTALLOWED_SEND_SMS = "sms.errors.task.permission-denied";
	
	/** This user is not allowed to view an SMS. */
	public final static String USER_NOTALLOWED_ACCESS_SMS = "sms.errors.user.read.denied";
	
	/** No recipient is selected. */
	public final static String TASK_RECIPIENTS_EMPTY = "sms.errors.task.recipients.empty";
	
	/** Invalid task specified. */
	public final static String TASK_INVALID = "sms.abort-task.invalid-task";
	
	/** SMS doesn't exist. */
	public final static String TASK_NOEXIST = "sms.errors.task.noexist";
	
	/** SMS sending is disabled for site. */
	public final static String TASK_SEND_DISABLED = "sms.errors.task.sms-send-disabled";
	
	/** SMS receiving is disabled for site. */
	public final static String TASK_INCOMING_DISABLED = "sms.errors.task.sms-incoming-disabled";

}
