/***********************************************************************************
 * ExternalLogic.java
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
package org.sakaiproject.sms.logic.impl.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.logic.hibernate.ExternalLogic;

// Sakai imports
//import org.sakaiproject.authz.api.FunctionManager;
//import org.sakaiproject.authz.api.SecurityService;

/**
 * Implementation of {@link ExternalLogic} with Sakai-specific code commented
 * out for the moment
 * 
 */
public class ExternalLogicImpl implements ExternalLogic {

	private static Log log = LogFactory.getLog(ExternalLogicImpl.class);

	// private FunctionManager functionManager;
	// private SecurityService securityService;

	/**
	 * Leave this as protected to try and prevent the random instantiation of
	 * this class.
	 * <p>
	 * Use LogicFactory.java to get instances of logic classes.
	 */
	protected ExternalLogicImpl() {

	}

	public void init() {
		log.debug("init");
		// register Sakai permissions for this tool

		// functionManager.registerFunction(SMS_ACCOUNT_VIEW);
		// functionManager.registerFunction(SMS_ACCOUNT_CREATE);
		// functionManager.registerFunction(SMS_ACCOUNT_EDIT);
		// functionManager.registerFunction(SMS_CONFIG_SITE);
		// functionManager.registerFunction(SMS_CONFIG_SYSTEM);
		// functionManager.registerFunction(SMS_TASK_CREATE);
		// functionManager.registerFunction(SMS_TASK_VIEW);
		// functionManager.registerFunction(SMS_MESSAGE_VIEW);
		// functionManager.registerFunction(SMS_TRANSACTION_VIEW);
	}

	/**
	 * @see ExternalLogic#isUserAdmin(String)
	 */
	public boolean isUserAdmin(String userId) {
		// return securityService.isSuperUser(userId);
		return true;
	}

	/**
	 * At the moment always returning true
	 * 
	 * @see ExternalLogic#isUserAllowedInLocation(String, String, String)
	 */
	public boolean isUserAllowedInLocation(String userId, String permission,
			String locationId) {

		log.debug("isUserAllowedInLocation(" + userId + ", " + permission + ","
				+ locationId + ")");
		Boolean allowed = true;
		// if (userId != null)
		// allowed = securityService.unlock(userId, permission, locationId);
		// else
		// allowed = securityService.unlock(permission, locationId);

		log.debug("allowed: " + allowed);

		return allowed;
	}

	// public void setSecurityService(SecurityService securityService) {
	// this.securityService = securityService;
	// }

	// public void setFunctionManager(FunctionManager functionManager) {
	// this.functionManager = functionManager;
	// }
}
