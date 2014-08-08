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

import org.junit.BeforeClass;
import org.sakaiproject.sms.dao.StandaloneSmsDaoImpl;
import org.sakaiproject.sms.logic.HibernateLogicLocator;
import org.sakaiproject.sms.logic.impl.hibernate.SmsAccountLogicImpl;
import org.sakaiproject.sms.logic.impl.hibernate.SmsConfigLogicImpl;
import org.sakaiproject.sms.logic.impl.hibernate.SmsMessageLogicImpl;
import org.sakaiproject.sms.logic.impl.hibernate.SmsTaskLogicImpl;
import org.sakaiproject.sms.logic.impl.hibernate.SmsTransactionLogicImpl;
import org.sakaiproject.sms.logic.smpp.impl.SmsBillingImpl;
import org.sakaiproject.sms.logic.stubs.ExternalLogicStub;

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
public abstract class AbstractBaseTestCase {

	public static HibernateLogicLocator hibernateLogicLocator;
	public static StandaloneSmsDaoImpl smsDao;
	public static SmsBillingImpl smsBilling;

	// Tells HibernateUtil to use the test configuration files
	@BeforeClass
    // In http://junit.org/apidocs/org/junit/BeforeClass.html it states:
    //The @BeforeClass methods of superclasses will be run before those of the current class
    // So it is fine to have a BeforeClass here and in subclasses.
    public static void setupBeforeTests() {
        //shortcut
        if(hibernateLogicLocator != null)
            return;
        
		hibernateLogicLocator = new HibernateLogicLocator();
		smsDao = new StandaloneSmsDaoImpl("hibernate-test.properties");
		smsBilling = new SmsBillingImpl();
		smsBilling.setHibernateLogicLocator(hibernateLogicLocator);
		smsBilling.init();

		SmsAccountLogicImpl smsAccountLogicImpl = new SmsAccountLogicImpl();
		smsAccountLogicImpl.setHibernateLogicLocator(hibernateLogicLocator);
		smsAccountLogicImpl.setSmsDao(smsDao);
		smsAccountLogicImpl.setExternalLogic(new ExternalLogicStub());

		hibernateLogicLocator.setSmsAccountLogic(smsAccountLogicImpl);

		SmsTaskLogicImpl smsTaskLogicImpl = new SmsTaskLogicImpl();
		smsTaskLogicImpl.setSmsDao(smsDao);
		smsTaskLogicImpl.setHibernateLogicLocator(hibernateLogicLocator);
		smsTaskLogicImpl.setExternalLogic(new ExternalLogicStub());

		hibernateLogicLocator.setSmsTaskLogic(smsTaskLogicImpl);

		SmsConfigLogicImpl smsConfigLogicImpl = new SmsConfigLogicImpl();
		smsConfigLogicImpl.setSmsDao(smsDao);
		hibernateLogicLocator.setSmsConfigLogic((smsConfigLogicImpl));

		SmsMessageLogicImpl smsMessageLogicImpl = new SmsMessageLogicImpl();
		smsMessageLogicImpl.setHibernateLogicLocator(hibernateLogicLocator);
		smsMessageLogicImpl.setSmsDao(smsDao);
		smsMessageLogicImpl.setExternalLogic(new ExternalLogicStub());
		hibernateLogicLocator.setSmsMessageLogic(smsMessageLogicImpl);

		SmsTransactionLogicImpl smsTransactionLogicImpl = new SmsTransactionLogicImpl();
		smsTransactionLogicImpl.setHibernateLogicLocator(hibernateLogicLocator);
		smsTransactionLogicImpl.setSmsDao(smsDao);
		smsTransactionLogicImpl.setSmsBilling(smsBilling);

		hibernateLogicLocator.setSmsTransactionLogic(smsTransactionLogicImpl);

		hibernateLogicLocator.setExternalLogic(new ExternalLogicStub());
	}
}
