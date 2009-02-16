/***********************************************************************************
 * AbstractBaseTestCase.java - created by Sakai App Builder -AZ
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
package org.sakaiproject.sms.util;

import org.sakaiproject.sms.logic.impl.hibernate.HibernateLogicLocator;
import org.sakaiproject.sms.logic.impl.hibernate.SmsAccountLogicImpl;
import org.sakaiproject.sms.logic.impl.hibernate.SmsConfigLogicImpl;
import org.sakaiproject.sms.logic.impl.hibernate.SmsMessageLogicImpl;
import org.sakaiproject.sms.logic.impl.hibernate.SmsTaskLogicImpl;
import org.sakaiproject.sms.logic.impl.hibernate.SmsTransactionLogicImpl;

import junit.framework.TestCase;

// TODO: Auto-generated Javadoc
/**
 * Abstract base class for all JUnit test cases. This allows for unit testing
 * initialization and can also be used to add extra functionality to all
 * subclasses.
 * <p>
 * This class makes use of a static block for one time initialization of
 * Hibernate test configuration.
 *
 * @author Julian Wyngaard
 * @version 1.0
 * @created 05-Dec-2008
 */
public abstract class AbstractBaseTestCase extends TestCase {

	public static HibernateLogicLocator hibernateLogicLocator;

	// Tells HibernateUtil to use the test configuration files
	static {
		HibernateUtil.setTestConfiguration(true);
		hibernateLogicLocator = new HibernateLogicLocator();

		SmsAccountLogicImpl smsAccountLogicImpl = new SmsAccountLogicImpl();
		smsAccountLogicImpl.setHibernateLogicLocator(hibernateLogicLocator);

		SmsTaskLogicImpl smsTaskLogicImpl = new SmsTaskLogicImpl();

		hibernateLogicLocator.setSmsTaskLogic(smsTaskLogicImpl);

		SmsConfigLogicImpl smsConfigLogicImpl = new SmsConfigLogicImpl();

		hibernateLogicLocator.setSmsAccountLogic(smsAccountLogicImpl);

		hibernateLogicLocator.setSmsConfigLogic((smsConfigLogicImpl));

		SmsMessageLogicImpl smsMessageLogicImpl = new SmsMessageLogicImpl();
		smsMessageLogicImpl.setHibernateLogicLocator(hibernateLogicLocator);
		hibernateLogicLocator.setSmsMessageLogic(smsMessageLogicImpl);

		SmsTransactionLogicImpl smsTransactionLogicImpl = new SmsTransactionLogicImpl();
		smsTransactionLogicImpl.setHibernateLogicLocator(hibernateLogicLocator);
		hibernateLogicLocator.setSmsTransactionLogic(smsTransactionLogicImpl);
	}

	/**
	 * Instantiates a new abstract base test case.
	 */
	public AbstractBaseTestCase() {

	}

	/**
	 * Instantiates a new abstract base test case.
	 *
	 * @param name
	 *            the name
	 */
	public AbstractBaseTestCase(String name) {
		super(name);
	}

	/**
	 * Abstract method will be implemented by all subclasses forcing this method
	 * to be called. This will be used to call
	 * <code> HibernateUtil.setTestConfiguration(true) and HibernateUtil.createSchema() </code>
	 * and for any other one time setup.
	 * <p>
	 * We realise that there are correct ways to implement one time setup but
	 * none of which allow one time setup for each test case when run from as
	 * suite.
	 */
	public abstract void testOnetimeSetup();
}
