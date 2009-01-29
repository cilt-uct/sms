/***********************************************************************************
 * ExternalLogicStub.java
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
package org.sakaiproject.sms.logic.stubs;

import org.sakaiproject.sms.logic.external.ExternalLogic;

/**
 * Stub implemenation of {@link ExternalLogic} for testing
 * 
 */
public class ExternalLogicStub implements ExternalLogic {

	@Override
	public String getSakaiMobileNumber(String userID) {
		return "0123456789";
	}

	@Override
	public boolean isUserAdmin(String userId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isUserAllowedInLocation(String userId, String permission,
			String locationId) {
		// TODO Auto-generated method stub
		return true;
	}

}
