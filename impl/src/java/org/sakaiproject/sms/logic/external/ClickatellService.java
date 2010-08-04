/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/sms/sms/trunk/api/src/java/org/sakaiproject/sms/logic/external/ExternalEmailLogic.java $
 * $Id: ExternalEmailLogic.java 67664 2010-05-17 09:09:14Z david.horwitz@uct.ac.za $
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010 The Sakai Foundation
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

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.model.SmsMessage;
import org.sakaiproject.sms.model.constants.SmsConst_DeliveryStatus;

public class ClickatellService implements ExternalMessageSending {

	private final static Log LOG = LogFactory.getLog(ClickatellService.class);
	
	public String sendMessagesToService(Set<SmsMessage> messages) {
		LOG.info("sending messages to clickatell: " + messages.size());
		
		//http://api.clickatell.com/http/sendmsg?user=xxxxx&password=xxxxx&api_id=xxxxx&to=448311234567&text=Meet+me+at+home
		
		return SmsConst_DeliveryStatus.STATUS_SENT;
	}

}
