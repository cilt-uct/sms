package org.sakaiproject.sms.tool.test.stubs;

import java.util.Date;
import java.util.Set;

import org.sakaiproject.sms.logic.exception.SmsAccountNotFoundException;
import org.sakaiproject.sms.logic.exception.SmsInsufficientCreditsException;
import org.sakaiproject.sms.logic.smpp.SmsBilling;
import org.sakaiproject.sms.model.SmsAccount;
import org.sakaiproject.sms.model.SmsMessage;
import org.sakaiproject.sms.model.SmsTask;

public class SmsBillingStub implements SmsBilling {

	public boolean sufficientCredits = false;

	public void allocateCredits(Long arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	public boolean checkSufficientCredits(Long arg0, int arg1) {
		// TODO Auto-generated method stub
		return sufficientCredits;
	}

	public double convertAmountToCredits(double arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double convertCreditsToAmount(double arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void creditAccount(Long arg0, long arg1, String arg2) {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("unchecked")
	public Set getAccTransactions(Long arg0, Date arg1, Date arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	public double getAccountBalance(Long arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getAccountCredits(Long arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Long getAccountID(String arg0, String arg1)
			throws SmsAccountNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	public Set getAllSiteAccounts(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean insertAccount(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean insertTransaction(Long arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	public void recalculateAccountBalance(Long arg0) {
		// TODO Auto-generated method stub

	}

	public void recalculateAccountBalances() {
		// TODO Auto-generated method stub

	}

	public boolean reserveCredits(SmsTask arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean settleCreditDifference(SmsTask arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean checkSufficientCredits(SmsTask arg0) {
		return sufficientCredits;
	}

	public boolean checkSufficientCredits(Long arg0, Integer arg1) {
		return sufficientCredits;
	}

	public boolean debitLateMessage(SmsMessage smsMessage) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean cancelPendingRequest(Long arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public String getCancelCode() {
		return "TCAN";
	}

	public String getCancelReserveCode() {
		return "RCAN";
	}

	public String getCreditAccountCode() {
		return "CRED";
	}

	public String getDebitLateMessageCode() {
		return "LATE";
	}

	public String getReserveCreditsCode() {
		return "RES";
	}

	public String getSettleDifferenceCode() {
		return "RSET";
	}

	public boolean checkSufficientCredits(SmsTask smsTask,
			boolean overDraftCheck) {
		// TODO Auto-generated method stub
		return sufficientCredits;
	}

	public boolean debitLateMessages(SmsTask smsTask, int credits) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean settleCreditDifference(SmsTask smsTask, int creditEstimate,
			int actualCreditsUsed) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean checkSufficientCredits(Long accountID,
			double creditsRequired, boolean overDraftCheck) {
		// TODO Auto-generated method stub
		return sufficientCredits;
	}

	public void creditAccount(Long accountId, double creditsToCredit,
			String Description) {
		// TODO Auto-generated method stub
		
	}

	public boolean debitLateMessages(SmsTask smsTask, double credits) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean settleCreditDifference(SmsTask smsTask,
			double creditEstimate, double actualCreditsUsed) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean debitIncomingMessage(SmsAccount account, double credits, Long replyTaskId) {
		// TODO Auto-generated method stub
		return false;
	}

	public String getIncomingMessageCode() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void transferAccountCredits(Long fromAccount, Long toAccount,
			double credits) throws SmsInsufficientCreditsException {
		// TODO Auto-generated method stub
		
	}

}
