package org.sakaiproject.sms.logic.impl.hibernate.exception;

/**
 * A exception class that indicates an account does not exists for a partucluar action
 */
public class SmsAccountNotFoundException extends Exception{

	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a account not found exception.
	 */
	public SmsAccountNotFoundException() {
	}

	/**
	 * Instantiates a account not found exception.
	 * 
	 * @param msg
	 *            the msg
	 */
	public SmsAccountNotFoundException(String msg) {
		super(msg);
	}

	/**
	 * Instantiates a account not found exception.
	 * 
	 * @param e
	 *            the exception  
	 */
	public SmsAccountNotFoundException(Exception e) {
		super(e);
	}

	
	
}
