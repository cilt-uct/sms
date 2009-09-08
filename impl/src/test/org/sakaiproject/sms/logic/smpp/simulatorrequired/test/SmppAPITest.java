/***********************************************************************************
 * SmppAPITest.java
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
 * limitations under the License.s
 *
 **********************************************************************************/
package org.sakaiproject.sms.logic.smpp.simulatorrequired.test;

import org.sakaiproject.sms.util.AbstractBaseTestCase;

/**
 * Test some api function on the smpp api. For example successful connect and
 * disconnect to the remote gateway. Both group and single message sending is
 * tested. It also waits for all the smmp delivery reports to come in and
 * verifies that all deliveries was successful.
 */
public class SmppAPITest extends AbstractBaseTestCase {

	public SmppAPITest() {

	}

	public void testme() {
		for (int i = 0; i < 400; i++) {

			System.out.println("0" + Math.round((Math.random() * 1000000000)));

		}

	}

	public SmppAPITest(String name) {
		super(name);
	}

	/**
	 * This is an helper method to insert a dummy smsTask into the Database. The
	 * sakaiID is used to identify the temp task.
	 * 
	 * @param sakaiID
	 * @param status
	 * @param dateToSend
	 * @param attemptCount
	 * @return
	 */
}