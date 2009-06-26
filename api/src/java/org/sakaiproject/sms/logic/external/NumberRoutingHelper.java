package org.sakaiproject.sms.logic.external;

/**
 * Helper methods to give information about the routing of numbers
 * @author dhorwitz
 *
 */
public interface NumberRoutingHelper {

	/**
	 * Is the supplied number routable?
	 * @param mobileNumber the mobile number
	 * @return true if the number is routable false if not
	 */
	public boolean isNumberRoutable(String mobileNumber);
	
	
}
