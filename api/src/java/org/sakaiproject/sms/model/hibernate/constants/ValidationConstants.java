/***********************************************************************************
 * ValidationConstants.java
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
package org.sakaiproject.sms.model.hibernate.constants;

// TODO: Auto-generated Javadoc
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

	/** Date to send empty. */
	public final static String TASK_DATE_TO_SEND_AFTER_THAN_EXPIRY = "sms.errors.task.dateToSend.afterExpiry";

	/** Status code empty. */
	public final static String TASK_STATUS_CODE_EMPTY = "sms.errors.task.statusCode.empty";

	/** Message type empty. */
	public final static String TASK_MESSAGE_TYPE_EMPTY = "sms.errors.task.messageType.empty";

	/** Sender user name empty. */
	public final static String TASK_SENDER_USER_NAME_EMPTY = "sms.errors.task.senderUserName.empty";

	/** Max time to live invalid. */
	public final static String TASK_MAX_TIME_TO_LIVE_INVALID = "sms.errors.task.maxTimeToLive.invalid";

	/** Delivery report timeout invalid. */
	public final static String TASK_DELIVERY_REPORT_TIMEOUT_INVALID = "sms.errors.task.deliveryReportTimeout.invalid";

	/** Delivery group id empty. */
	public final static String TASK_DELIVERY_GROUP_ID_EMPTY = "sms.errors.task.deliveryGroupId.empty";

	/** Message sakai user id empty */
	public final static String MESSAGE_SAKAI_USER_ID_EMPTY = "sms.errors.message.sakaiUserId.empty";

	/** Message status empty. */
	public final static String MESSAGE_STATUS_CODE_EMPTY = "sms.errors.message.statusCode.empty";

	/** Message task id empty. */
	public final static String MESSAGE_TASK_ID_EMPTY = "sms.errors.message.taskId.empty";

}
