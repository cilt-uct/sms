package org.sakaiproject.sms.tool.validators;

import org.sakaiproject.sms.logic.SmsAccountLogic;
import org.sakaiproject.sms.model.SmsAccount;
import org.sakaiproject.sms.tool.beans.CreditAccountBean;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class CreditAccountValidator implements Validator {

	private SmsAccountLogic smsAccountLogic;

	public void setSmsAccountLogic(SmsAccountLogic smsAccountLogic) {
		this.smsAccountLogic = smsAccountLogic;
	}

	@SuppressWarnings("unchecked")
	public boolean supports(Class clazz) {
		if (CreditAccountBean.class.equals(clazz.getClass())) {
			return true;
		}
		return false;
	}

	public void validate(Object target, Errors errors) {
		CreditAccountBean account = (CreditAccountBean) target;

		if (account.getAccountId() != null) {
			// check account exists
			SmsAccount smsAccount = smsAccountLogic.getSmsAccount(account
					.getAccountId());

			if (smsAccount == null) {
				errors.reject("sms.credit.account.errors.no.account");
			}
		} else {
			errors.reject("sms.errors.accountId.empty");
		}

	}

}
