/***********************************************************************************
 * SmsTaskLocatorTest.java
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

import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.smpp.SmsCore;
import org.sakaiproject.sms.logic.stubs.ExternalLogicStub;
import org.sakaiproject.sms.model.SmsTask;
import org.sakaiproject.sms.tool.otp.SmsTaskLocator;
import org.sakaiproject.sms.tool.test.stubs.SmsCoreStub;

public class SmsTaskLocatorTest extends TestCase {

	private SmsTaskLocator smsTaskLocator;
	private SmsCore smsCore;
	private ExternalLogic externalLogic;

	/**
	 * setUp to run at start of every test
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	public void setUp() {
		smsTaskLocator = new SmsTaskLocator();
		smsCore = new SmsCoreStub();
		externalLogic = new ExternalLogicStub();

		smsTaskLocator.setSmsCore(smsCore);
		smsTaskLocator.setExternalLogic(externalLogic);
	}

	/**
	 * Test clearing of beans
	 */
	public void testClearBeans() {
		smsTaskLocator.locateBean(SmsTaskLocator.NEW_1);
		assertTrue(smsTaskLocator.containsNew());
		smsTaskLocator.clearBeans();
		assertFalse(smsTaskLocator.containsNew());
	}

	/**
	 * Test containsNew method
	 */
	public void testContainsNew() {
		assertFalse(smsTaskLocator.containsNew());
		smsTaskLocator.locateBean(SmsTaskLocator.NEW_1);
		assertTrue(smsTaskLocator.containsNew());
	}

	/**
	 * Test locateBean method for new bean
	 */
	public void testLocateNew() {
		assertFalse(smsTaskLocator.containsNew());
		SmsTask smsTask = (SmsTask) smsTaskLocator
				.locateBean(SmsTaskLocator.NEW_1);
		assertNotNull(smsTask);
	}
}
