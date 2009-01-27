package org.sakaiproject.sms.model.hibernate.factory;

import org.sakaiproject.sms.hibernate.logic.impl.HibernateLogicFactory;
import org.sakaiproject.sms.hibernate.logic.impl.exception.SmsAccountNotFoundException;
import org.sakaiproject.sms.hibernate.model.SmsAccount;
import org.sakaiproject.sms.hibernate.model.SmsTransaction;
import org.sakaiproject.sms.hibernate.model.constants.SmsConst_Billing;

public class SmsTransactionFactory {

	// Not in use, but there are unit test for it
	private static SmsTransaction createReserveCreditsTask(Long smsTaskId,
			Long smsAccountId, Integer credits)
			throws SmsAccountNotFoundException {

		SmsAccount smsAccount = getSmsAccount(smsAccountId);

		SmsTransaction smsTransaction = new SmsTransaction();
		smsTransaction.setSmsAccount(smsAccount);
		smsTransaction.setSakaiUserId(smsAccount.getSakaiUserId());
		smsTransaction.setTransactionCredits(credits);
		smsTransaction
				.setTransactionTypeCode(SmsConst_Billing.TRANS_RESERVE_CREDITS);
		smsTransaction.setSmsTaskId(smsTaskId);
		smsTransaction.setBalance(smsAccount.getBalance());

		// TODO : Check to see if this is the case
		smsTransaction.setTransactionAmount(0.0f);

		return smsTransaction;
	}

	// Not in use, but there are unit test for it
	public static SmsTransaction createCancelTask(Long smsTaskId,
			Long smsAccountId) throws SmsAccountNotFoundException {
		SmsAccount smsAccount = getSmsAccount(smsAccountId);

		SmsTransaction smsTransaction = new SmsTransaction();
		smsTransaction.setSmsAccount(smsAccount);
		smsTransaction.setSakaiUserId(smsAccount.getSakaiUserId());
		smsTransaction.setTransactionTypeCode(SmsConst_Billing.TRANS_CANCEL);
		smsTransaction.setSmsTaskId(smsTaskId);

		smsTransaction.setTransactionCredits(0);
		smsTransaction.setTransactionAmount(0.0f);
		smsTransaction.setBalance(0.0f);

		return smsTransaction;
	}

	private static SmsAccount getSmsAccount(Long smsAccountId)
			throws SmsAccountNotFoundException {
		SmsAccount smsAccount = HibernateLogicFactory.getAccountLogic()
				.getSmsAccount(smsAccountId);

		if (smsAccount == null)
			throw new SmsAccountNotFoundException("Account id " + smsAccountId
					+ " does not exsits");
		return smsAccount;
	}

}
