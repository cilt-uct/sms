package org.sakaiproject.sms.util;

public class SmsMessageUtil {

	/** Return sanitized message body, stripped of unprintable characters.
	 * 
	 * @param smsMessageBody
	 * @return The sanitized message body.
	 */
	public static String sanitizeMessageBody(String smsMessageBody) {
		
		if (smsMessageBody == null)
			return null;
		
		// TODO replace high-order quotes etc. with standard ASCII
		
		return smsMessageBody.replaceAll("\n", " ").replaceAll("\\p{Cntrl}", "").trim();
	}
	

}
