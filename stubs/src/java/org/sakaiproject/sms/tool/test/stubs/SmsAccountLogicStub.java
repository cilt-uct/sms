package org.sakaiproject.sms.tool.test.stubs;

import java.util.List;

import org.sakaiproject.sms.logic.SmsAccountLogic;
import org.sakaiproject.sms.model.SmsAccount;

public class SmsAccountLogicStub implements SmsAccountLogic {

	public void deleteSmsAccount(SmsAccount arg0) {
		// TODO Auto-generated method stub

	}

	public List<SmsAccount> getAllSmsAccounts() {
		// TODO Auto-generated method stub
		return null;
	}

	public SmsAccount getSmsAccount(Long id) {

		if (id.equals(1L)) {
			SmsAccount testAccout = new SmsAccount(1L, "", 100L, "Site", "Joe",
					"Joes Account");
			testAccout.setAccountEnabled(true);

			return testAccout;
		}

		return null;
	}

	public SmsAccount getSmsAccount(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public void persistSmsAccount(SmsAccount arg0) {
		// TODO Auto-generated method stub

	}

	public void recalculateAccountBalance(Long arg0, SmsAccount arg1) {
		// TODO Auto-generated method stub

	}

	public double getAccountBalance(double credits) {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<SmsAccount> getSmsAccountsForOwner(String sakaiUserId) {
		// TODO Auto-generated method stub
		return null;
	}


}
