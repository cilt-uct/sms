/***********************************************************************************
 * SmsSchedulerThreadingTest.java
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
 * This class will create 2 Sms schedulers.The will run together processing the
 * inserted tasks without interfering with each other .The test is successful if
 * after a minute there is no more tasks to process.
 * 
 * @author Etienne@psyberate.co.za
 * 
 */
public class SmsSchedulerThreadingTest extends AbstractBaseTestCase {
	/**
	 * Standard main() and suite() methods.
	 * 
	 * @param args
	 *            the args
	 */
	public static void main(String[] args) {
		String[] name = { SmsSchedulerThreadingTest.class.getName() };
		junit.textui.TestRunner.main(name);
	}

	/**
	 * Suite.
	 * 
	 * @return the test
	 */
	public static Test suite() {
		return new TestSuite(SmsSchedulerThreadingTest.class);
	}

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
		SmsSchedulerThread smsThread1, smsThread2;
		smsThread1 = new SmsSchedulerThread("Session1");
		smsThread2 = new SmsSchedulerThread("Session2");
		// pass that instance to the MTTR
		TestRunnable[] trs = { smsThread1, smsThread2 };
		MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(trs);

		// kickstarts the MTTR & fires off threads
		mttr.runTestRunnables();
		System.out.println("Done");

	}
}