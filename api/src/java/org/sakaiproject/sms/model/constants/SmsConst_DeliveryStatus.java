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

//TODO move to sms_core project later on

public class SmsConst_DeliveryStatus {

	// Status codes used for both SMS_TASK and SMS_MESSAGE.

	/**
	 * <b>SMS Task</b>: All messages for this task have been sent to the
	 * gateway. Delivery reports are awaited. <br/>
	 * <br/>
	 * <b>SMS Message</b>: The message has been successfully sent to the
	 * gateway. We know nothing more about the message status at this stage.
	 */
	public final static String STATUS_SENT = "S";

	/**
	 * <b>SMS Task</b>: The maximum retry count for the task has been reached,
	 * mark task as failed and do nothing further with it. <br/>
	 * <br/>
	 * <b>SMS Message</b>: The gateway reported that the message could not be
	 * delivered. The mobile number may be invalid or unrouteable. See the
	 * SMSC_DELIVERY_STATUS_CODE for the error code returned by the gateway.
	 */
	public final static String STATUS_FAIL = "F";

	/**
	 * <b>SMS Task</b>: The expiry time for this task passed without it being
	 * delivered successfully. <br/>
	 * <br/>
	 * <b>SMS Message</b>: The expiry time for the task containing this message
	 * passed without the message being delivered.
	 */
	public final static String STATUS_EXPIRE = "X";

	/**
	 * <b>SMS Task</b>: Messages are awaiting delivery to the gateway, for
	 * example because the task is scheduled for delivery at a later date/time. <br/>
	 * <br/>
	 * <b>SMS Message</b>: The message is awaiting delivery to the gateway, for
	 * example because the task for this message originated on a server in a
	 * cluster which is not bound to the gateway.
	 */
	public final static String STATUS_PENDING = "P";

	// SMS_MESSAGE status codes. See also
	// org.sakaiproject.sms.model.hibernate.constants.SmsConst_SmscDeliveryStatus

	/**
	 * <b>SMS Message</b>: The gateway reported that the message was
	 * successfully delivered.
	 */
	public final static String STATUS_DELIVERED = "D";

	/**
	 * <b>SMS Message</b>: No delivery report was received for this message
	 * within the task delivery timeout period (i.e. the task has changed status
	 * to STATUS_TASK_COMPLETED). The mobile number may be valid but the message
	 * could not be delivered (for example if the mobile phone was switched off
	 * or not active on the network). <br/>
	 * <br/>
	 * STATUS_TIMEOUT messages may be changed to STATUS_DELIVERED should a 
	 * delivery report arrive for them.
	 */
	public final static String STATUS_TIMEOUT = "T";

	/**
	 * <b>SMS Message</b>: The task for this message was aborted, and the
	 * message was therefore not sent.
	 */
	public final static String STATUS_ABORT = "A";

	/**
	 * <b>SMS Message</b>: An error occurred when we tried to delivery the
	 * message to the gateway: the gateway returned an error code and did not
	 * accept the message, or there was no response to the submission attempt,
	 * or the gateway was unexpectedly not available. See the FAIL_REASON field
	 * for the message for more detail on the cause.
	 */
	public final static String STATUS_ERROR = "E";

	// SMS_TASK status codes.

	/**
	 * <b>SMS Task</b>: Rescheduled for later delivery.
	 * 
	 * The gateway connection was down when a delivery attempt was made. If the
	 * maximum retry count has not been reached, reschedule the delivery.
	 */
	public final static String STATUS_RETRY = "R";

	/**
	 * <b>SMS Task</b>: There are no outstanding messages to be sent for the
	 * task, and the gateway timeout period for receiving delivery reports has
	 * expired.
	 */
	public final static String STATUS_TASK_COMPLETED = "C";

	/**
	 * <b>SMS Task</b>: The gateway connection went down during the delivery of
	 * task messages. Not all messages could be sent to the gateway. Try to
	 * re-send the remaining messages later.
	 */
	public final static String STATUS_INCOMPLETE = "I";

	/**
	 * <b>SMS Task</b>: Messages for this task are in the process of being sent
	 * to the gateway.
	 */
	public final static String STATUS_BUSY = "B";

}
