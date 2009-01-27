package org.sakaiproject.sms.logic.hibernate.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.sakaiproject.sms.hibernate.util.HibernateUtil;

/**
 * Run all tests.
 */
public class SmsTestSuite {

	/**
	 * Suite.
	 * 
	 * @return the test
	 */
	public static Test suite() {
		HibernateUtil.createSchema();
		TestSuite suite = new TestSuite(
				"Test for org.sakaiproject.sms.hibernate.test");

		suite.addTestSuite(SmsAccountTest.class);
		suite.addTestSuite(SmsTransactionTest.class);
		suite.addTestSuite(SmsTaskTest.class);
		suite.addTestSuite(SmsConfigTest.class);
		suite.addTestSuite(SmsMessageTest.class);
		suite.addTestSuite(SmsDatabaseStressTest.class);
		suite.addTestSuite(SmsPropertyReaderTest.class);

		return suite;
	}

}
