/***********************************************************************************
 * SmsConfigLocatorTest.java
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
package org.sakaiproject.sms.tool.test;

import junit.framework.TestCase;

import org.sakaiproject.sms.model.SmsConfig;
import org.sakaiproject.sms.model.SmsMessage;
import org.sakaiproject.sms.tool.otp.SmsConfigLocator;
import org.sakaiproject.sms.tool.otp.SmsMessageLocator;

public class SmsConfigLocatorTest extends TestCase{

	SmsConfigLocator smsConfigLocator;

	@Override
	public void setUp() {
		smsConfigLocator = new SmsConfigLocator();
	}

	/**
	 * TODO: Complete retrieval of existing {@link SmsMessage} object.
	 * 
	 */
	public void testLocateExistingSmsMsg() {
		// Not possible at the moment
	}
	
	public void testLocateNewSmsMsg(){
		Object obj = smsConfigLocator.locateBean(SmsMessageLocator.NEW_1);
		assertNotNull(obj); // Should retrieve something
		try {
			SmsConfig msg = (SmsConfig) obj;
			assertNull(msg.getId()); // Should not have id yet as it is not
			// persisted
		} catch (Exception e) {
			fail("No exception should be caught"); // In case of possible
			// ClassCastException
		}
	}
}
