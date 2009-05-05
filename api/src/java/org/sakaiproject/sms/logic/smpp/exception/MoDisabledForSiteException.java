package org.sakaiproject.sms.logic.smpp.exception;

public class MoDisabledForSiteException extends Exception {

	private static final long serialVersionUID = 1L;

	public MoDisabledForSiteException(String siteID) {
		super("MO receiving is disabled for site :" + siteID);
	}

}