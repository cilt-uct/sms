/***********************************************************************************
 * SmppTestSuite.java
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
package org.sakaiproject.sms.logic.smpp.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Run all tests.
 */
public class SmppTestSuite {

	/**
	 * Suite.
	 * 
	 * @return the test
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.sakaiproject.sms.smpp.test");
		// $JUnit-BEGIN$
		suite.addTestSuite(SmsCoreTest.class);
		suite.addTestSuite(SmppAPITest.class);
		suite.addTestSuite(TaskValidatorTest.class);
		// suite.addTestSuite(MessageValidatorTest.class);
		suite.addTestSuite(MessageCatelogTest.class);
		// $JUnit-END$
		return suite;
	}

}
