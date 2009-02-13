package org.sakaiproject.sms.model.hibernate.constants;

/**
 * Used with SmsTransaction.transactionTypeCode
 * 
 * @author louis@psybergate.co.za
 * 
 * 
 */
public class SmsConst_Billing {
	public static final String TRANS_RESERVE_CREDITS = "RES";

	public static final String TRANS_SETTLE_DIFFERENCE = "RSET";

	public static final String TRANS_CANCEL_RESERVE = "RCAN";

	public static final String TRANS_CANCEL = "TCAN";

	public static final String TRANS_CREDIT_ACCOUNT = "CRED";

	public static final String TRANS_DEBIT_LATE_MESSAGE = "LATE";

}