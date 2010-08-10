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

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entitybroker.util.http.HttpRESTUtils;
import org.sakaiproject.entitybroker.util.http.HttpResponse;
import org.sakaiproject.entitybroker.util.http.HttpRESTUtils.Method;
import org.sakaiproject.sms.logic.HibernateLogicLocator;
import org.sakaiproject.sms.model.SmsMessage;
import org.sakaiproject.sms.model.constants.SmsConst_DeliveryStatus;
import org.sakaiproject.sms.model.constants.SmsConst_SmscDeliveryStatus;

public class ClickatellService implements ExternalMessageSending {

	private final static Log LOG = LogFactory.getLog(ClickatellService.class);


	private static final String CLICKATELL_API_URL = "http://api.clickatell.com/http";
	
	
	private HibernateLogicLocator hibernateLogicLocator;

	public void setHibernateLogicLocator(HibernateLogicLocator hibernateLogicLocator) {
		this.hibernateLogicLocator = hibernateLogicLocator;
	}

	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	public String sendMessagesToService(Set<SmsMessage> messages) {
		LOG.debug("sending messages to clickatell: " + messages.size());
		Iterator<SmsMessage> messageI = messages.iterator();
		while (messageI.hasNext()) {
			SmsMessage message = messageI.next();
			
			//http://api.clickatell.com/http/sendmsg?user=xxxxx&password=xxxxx&api_id=xxxxx&to=448311234567&text=Meet+me+at+home
			Map<String, String> params = getCoreParams();
			
			//from the message
			params.put("to", message.getMobileNumber());
			params.put("text", message.getMessageBody());
			
			
			HttpResponse response = HttpRESTUtils.fireRequest( CLICKATELL_API_URL + "/sendmsg", Method.POST, params);
			LOG.debug(response.responseCode);
			String body = response.responseBody;
			LOG.debug(body);
			if (body != null && body.startsWith("ID:")) {
				String id = body.substring(body.indexOf(":") +1).trim() ;
				LOG.debug("got id of " + id + " len:" + id.length());
				message.setDateDelivered(new Date());
				message.setDateSent(new Date());
				message.setSubmitResult(true);
				message.setSmscMessageId(id);
				message.setStatusCode(SmsConst_DeliveryStatus.STATUS_SENT);
				message.setSmscDeliveryStatusCode(SmsConst_SmscDeliveryStatus.ENROUTE);
				//for now just set this to 1
				message.setCredits(1);
				
			} else {
				message.setStatusCode(SmsConst_DeliveryStatus.STATUS_ERROR);
				LOG.warn("message sending failed: " + body);
				message.setFailReason(body);
			}

			hibernateLogicLocator.getSmsMessageLogic().persistSmsMessage(message);
		}
		

		
		return SmsConst_DeliveryStatus.STATUS_SENT;
	}

	private Map<String, String> getCoreParams() {
		Map<String,String> params = new HashMap<String,String>();
		params.put("user", serverConfigurationService.getString("sms.clickatell.user"));
		params.put("password", serverConfigurationService.getString("sms.clickatell.password"));
		params.put("api_id", serverConfigurationService.getString("sms.clickatell.apiid"));
		return params;
	}

	public void updateMessageStatuses(List<SmsMessage> messages) {
		Iterator<SmsMessage> iter = messages.iterator();
		while (iter.hasNext()) {
			SmsMessage message = iter.next();
			Map<String, String> params = getCoreParams();
			params.put("apimsgid", message.getSmscMessageId());
			HttpResponse response2 = HttpRESTUtils.fireRequest( CLICKATELL_API_URL + "/getmsgcharge", Method.POST, params);
			String body = response2.responseBody;
			LOG.debug("body" + body);
			if (body != null && body.contains("charge")) {
				String charge = body.substring(body.indexOf("charge:") + 8, body.indexOf("charge:") + 10 ).trim();
				LOG.debug("charge: " + charge);
				
				String status = body.substring(body.indexOf("status:") + 8).trim();
				LOG.debug("status: "  + status);
				if ("002".equals(status)) {
					LOG.debug("message is queued");
				} else if ("003".equals(status) || "004".equals(status)) {
					LOG.debug("message has been delivered");
					message.setSmscDeliveryStatusCode(SmsConst_SmscDeliveryStatus.DELIVERED);
					message.setStatusCode(SmsConst_DeliveryStatus.STATUS_DELIVERED);
					message.setCredits(Double.valueOf(charge));
					
					hibernateLogicLocator.getSmsMessageLogic().persistSmsMessage(message);
				}
			}
			
			//apiMsgId: 92e54913c737fe742cc7d09cdf419d66 charge: 1 status: 002
			
			
		}
	}

}
