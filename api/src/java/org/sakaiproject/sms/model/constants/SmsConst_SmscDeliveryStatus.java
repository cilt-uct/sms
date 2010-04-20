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
 * These are the message status codes as defined by the SMPP protocol.
 */

public class SmsConst_SmscDeliveryStatus {

	public final static int ACCEPTED = 6;

	public final static int DELETED = 4;

	public final static int DELIVERED = 2;

	public final static int ENROUTE = 1;

	public final static int EXPIRED = 3;

	public final static int REJECTED = 8;

	public final static int SKIPPED = 9;

	public final static int UNDELIVERA = 5;

	public final static int UNKNOWN = 7;

}
