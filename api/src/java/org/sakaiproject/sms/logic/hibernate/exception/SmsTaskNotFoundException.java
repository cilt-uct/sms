package org.sakaiproject.sms.logic.hibernate.exception;

public class SmsTaskNotFoundException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * Instantiates a new sms  Task not found  exception.
	 */
	public SmsTaskNotFoundException() {
	}

	/**
	 * Instantiates a new sms Task not found exception.
	 * 
	 * @param msg
	 *            the msg
	 */
	public SmsTaskNotFoundException(String msg) {
		super(msg);
	}

	/**
	 * Instantiates a new sms Task not found exception.
	 * 
	 * @param e
	 *            the e
	 */
	public SmsTaskNotFoundException(Exception e) {
		super(e);
	}

}
