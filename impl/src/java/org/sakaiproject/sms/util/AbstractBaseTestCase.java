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

	// Tells HibernateUtil to use the test configuration files
	static {
		HibernateUtil.setTestConfiguration(true);
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

}
