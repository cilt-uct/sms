/***********************************************************************************
 * SmppThreadingTest.java
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

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner;
import net.sourceforge.groboutils.junit.v1.TestRunnable;

import org.sakaiproject.sms.model.constants.SmsConstants;
import org.sakaiproject.sms.util.AbstractBaseTestCase;

/**
 * This unit test will create 2 separate but concurrent connections (sessions)
 * to the gateway simulator. Each thread will pass on messages. It will then
 * hang on and wait for sms delivery reports to come in. The test will fail if
 * delivery reports are missing. It will also fail if a message could not be
 * sent to the gateway. The number of messages to be sent can be changed for
 * each thread. The delay between message transmissions can also be set. The
 * default is 100 ms.
 * 
 * Due to limitation in the simulator, do not set session1_message_count and
 * session1_message_count higher than 1000 each. Also give the simulator 30
 * seconds or so to process messages before re-running this unit test.
 * 
 * NB NOTE: The gateway will send delivery report to ALL listeners that uses the
 * same source mobile number. That means to all tomcat instances on all Sakai
 * servers will receive delivery reports when one of them send messages out.
 * thread. The gateway simply sends the reports to the ip address.
 */
public class SmppThreadingTest extends AbstractBaseTestCase {
	/**
	 * Standard main() and suite() methods.
	 * 
	 * @param args
	 *            the args
	 */
	public static void main(String[] args) {
		String[] name = { SmppThreadingTest.class.getName() };
		junit.textui.TestRunner.main(name);
	}

	/**
	 * Suite.
	 * 
	 * @return the test
	 */
	public static Test suite() {
		return new TestSuite(SmppThreadingTest.class);
	}

	private final int delay_between_messages = 1; // ms

	private final int session1_message_count = 200;

	private final int session2_message_count = 200;

	static {
		if (!SmsConstants.isDbSchemaCreated) {
			smsDao.createSchema();
			SmsConstants.isDbSchemaCreated = true;
		}
	}

	/**
	 * You use the MultiThreadedTestRunner in your test cases. The MTTR takes an
	 * array of TestRunnable objects as parameters in its constructor.
	 * 
	 * After you have built the MTTR, you run it with a call to the
	 * runTestRunnables() method.
	 * 
	 * @throws Throwable
	 *             the throwable
	 */
	public void testConcurrency() throws Throwable {

		// instantiate the TestRunnable classes
		SmppThread smsThread1, smsThread2;

		smsThread1 = new SmppThread("session 1", session1_message_count,
				delay_between_messages);
		smsThread2 = new SmppThread("session 2", session2_message_count,
				delay_between_messages);

		// pass that instance to the MTTR
		TestRunnable[] trs = { smsThread1, smsThread2 };
		MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(trs);

		// kickstarts the MTTR & fires off threads
		mttr.runTestRunnables();
		int message_count = session1_message_count + session2_message_count;

		System.out.println("Done " + message_count);

	}
}