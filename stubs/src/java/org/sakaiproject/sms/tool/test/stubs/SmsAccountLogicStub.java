package org.sakaiproject.sms.tool.test.stubs;

import java.util.List;

import org.sakaiproject.sms.logic.hibernate.SmsAccountLogic;
import org.sakaiproject.sms.model.hibernate.SmsAccount;

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

	public float getAccountBalance(Long accountId) {
		// TODO Auto-generated method stub
		return 0f;
	}

}
