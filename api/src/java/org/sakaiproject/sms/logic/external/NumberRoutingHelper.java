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

import org.sakaiproject.sms.model.SmsMessage;

/**
 * Helper methods to give information about the routing of numbers
 * @author dhorwitz
 *
 */
public interface NumberRoutingHelper {

	/**
	 * The prefix used to dial an international number from within the local country (default "00").
	 * For example the UK number "+44 987 123 4567" may be dialed as "00 44 987 123 4567". 
	 * @return
	 */
	public String getInternationalPrefix();
	
	/**
	 * The international direct dialling code for the local country (ITU E164 country code),
	 * e.g. 1 = US/Canada, 27 = South Africa, 44 = United Kingdom, 61 = Australia  
	 * @return
	 */
	public String getCountryCode();
	
	/**
	 * The  prefix used to dial local mobile numbers (not in use in all countries).
	 * For example for a South African mobile number +27 82 123 4567 which may
	 * be dialled from within South Africa as 082 123 4567, the local prefix is "0". 
	 * @return The local country prefix.
	 */
	public String getLocalPrefix();

	/**
	 * Is the supplied number routable through known gateways on this system? 
	 * Expects a normalized number in international form.
	 * If the mobile number is not normalized, first normalize it with 
	 * MobileNumberHelper.normalizeNumber() before passing it to this function.
	 * @param mobileNumber the mobile number
	 * @return true if the number is routable, false if not.
	 */
	public boolean isNumberRoutable(String mobileNumber);
	
	/**
	 * Normalize a mobile number: remove spaces and punctuation, and convert to 
	 * international form. Depends on local settings for international dialling prefix, 
	 * local dialling prefix and country code. For example in South Africa (int prefix = 00, 
	 * local prefix = 0, country code = 44), 082 123 4567 = 27821234567, 
	 * 00 44 (123) 456-7890 = 441234567890, +27-82-123-4567 = 27821234567 
	 * @param mobileNumber A mobile number in any form
	 * @return The normalized number in international form
	 */
	public String normalizeNumber(String mobileNumber);

	/** 
	 * Set routing (SMSC ID) and cost information (credits) for this message, in the
	 * respective message fields.
	 * @param message The message to route
	 * @return true if the message is routable, otherwise false.
	 */
	public boolean getRoutingInfo(SmsMessage message);

	/**
	 * Get routing cost in credits per incoming message for the given SMSC
	 * @param smscId
	 * @return
	 */
	public double getIncomingMessageCost(String smscId);
	
}
