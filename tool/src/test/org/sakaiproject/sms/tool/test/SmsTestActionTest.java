/***********************************************************************************
 * SmsTestActionTest.java
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

import org.sakaiproject.sms.tool.beans.SmsTestActionBean;
import org.sakaiproject.sms.tool.otp.SmsMessageLocator;
import org.sakaiproject.sms.tool.test.stubs.SmsSmppStub;

import uk.org.ponder.messageutil.TargettedMessageList;

/**
 * Test class for {@link SmsTestActionBean}
 * 
 */
public class SmsTestActionTest extends TestCase {

	private SmsTestActionBean smsTestAction;
	private SmsSmppStub smsSmppStub;
	private SmsMessageLocator smsMessageLocator;
	private TargettedMessageList targettedMessageList;

	/**
	 * setUp to run at start of every test
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	public void setUp() {
		smsTestAction = new SmsTestActionBean();
		smsSmppStub = new SmsSmppStub();
		smsMessageLocator = new SmsMessageLocator();
		targettedMessageList = new TargettedMessageList();

		smsTestAction.setMessages(targettedMessageList);
		smsTestAction.setSmsSmpp(smsSmppStub);
		smsTestAction.setSmsMessageLocator(smsMessageLocator);
	}
		
	/**
	 * Test calling send method
	 */
//	public void testSendAction() {
//		String result = smsTestAction.send();
//		assertEquals(ActionResults.SUCCESS, result);
//		Object obj = smsMessageLocator.locateBean(SmsMessageLocator.NEW_1);
//		assertNotNull(obj);
//		SmsMessage msg = (SmsMessage) obj;
//		assertEquals(SmsSmppStub.CALLED, msg.getDebugInfo());
//	}
//
//	/**
//	 * Test calling send method with exception thrown by Smpp
//	 */
//	public void testSendActionWithException() {
//		smsSmppStub.forceException = true;
//		String result = smsTestAction.send();
//		// At the moment same actionResult still returned
//		assertEquals(ActionResults.SUCCESS, result);
//		Object obj = smsMessageLocator.locateBean(SmsMessageLocator.NEW_1);
//		assertNotNull(obj);
//		SmsMessage msg = (SmsMessage) obj;
//		assertEquals(SmsSmppStub.CALLED, msg.getDebugInfo());
//
//		// Check that error is written to TargettedMessageList
//		assertTrue(targettedMessageList.size() == 1);
//		assertEquals("sms.errors.send-error", targettedMessageList.messageAt(0)
//				.acquireMessageCode());
//	}
		
		public void test(){
			
		}
		
}
