package org.sakaiproject.sms.logic.stubs;

import org.sakaiproject.sms.logic.external.ExternalLogic;

public class ExternalLogicStub implements ExternalLogic {

	@Override
	public String getSakaiMobileNumber(String userID) {
		// TODO Auto-generated method stub
		return null;
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
