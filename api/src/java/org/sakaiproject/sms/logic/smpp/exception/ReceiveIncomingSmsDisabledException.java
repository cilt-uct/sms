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
package org.sakaiproject.sms.logic.smpp.exception;

/***********************************************************************************
 * ReceiveIncomingSmsDisabledException.java
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

import org.sakaiproject.sms.model.SmsTask;

/**
 * Exception that will be thrown when the receiving of incoming sms messages is
 * disabled for the site.
 * 
 * @author Etienne@psybergate.com
 * @version 1.0
 * @created 5-March-2009
 */
public class ReceiveIncomingSmsDisabledException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ReceiveIncomingSmsDisabledException(final SmsTask smsTask) {
		super("The receiving of incoming sms messages is disabled for site: "
				+ smsTask.getSakaiSiteId());
	}

}
