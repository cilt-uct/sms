package org.sakaiproject.sms.model.hibernate.constants;

/**
 * These are the message status codes as defined by the SMPP protocol.
 */

public class SmsConst_SmscDeliveryStatus {

	public final static int ACCEPTED = 6;

	public final static int DELETED = 4;

	public final static int DELIVERED = 2;

	public final static int ENROUTE = 1;

	public final static int EXPIRED = 3;

	public final static int REJECTED = 8;

	public final static int SKIPPED = 9;

	public final static int UNDELIVERA = 5;

	public final static int UNKNOWN = 7;

}
