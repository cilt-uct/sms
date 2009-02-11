package org.sakaiproject.sms.tool.validators;

import org.sakaiproject.sms.logic.hibernate.SmsAccountLogic;
import org.sakaiproject.sms.model.hibernate.SmsAccount;
import org.sakaiproject.sms.tool.beans.DebitAccountBean;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class DebitAccountValidator implements Validator {

	private SmsAccountLogic smsAccountLogic;

	public void setSmsAccountLogic(SmsAccountLogic smsAccountLogic) {
		this.smsAccountLogic = smsAccountLogic;
	}

	@SuppressWarnings("unchecked")
	public boolean supports(Class clazz) {
		if (DebitAccountBean.class.equals(clazz.getClass())) {
			return true;
		}
		return false;
	}

	public void validate(Object target, Errors errors) {
		DebitAccountBean account = (DebitAccountBean) target;

		if (account.getAccountId() != null) {
			// check account exists
			SmsAccount smsAccount = smsAccountLogic.getSmsAccount(account
					.getAccountId());

			if (smsAccount == null) {
				errors.reject("sms.debit.account.errors.no.account");
			}
		} else {
			errors.reject("sms.errors.accountId.empty");
		}

		if (account.getCreditsToDebit() == null) {
			errors.reject("sms.errors.creditsToDebit.invalid");
		} else if (account.getCreditsToDebit() < 0) {
			errors.rejectValue("creditsToDebit",
					"sms.errors.creditsToDebit.empty");
		} else {

		}

	}

}
