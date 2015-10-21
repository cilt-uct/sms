package org.sakaiproject.sms.logic.external;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.sms.model.SmsMessage;
import org.sakaiproject.sms.model.constants.SmsConstants;

public class NumberRoutingHelperImpl implements NumberRoutingHelper {

	private static final Log LOG = LogFactory
	.getLog(NumberRoutingHelper.class);
	
	
	private Map<String, Integer> countryLength = new HashMap<String,Integer>();
	
	private ServerConfigurationService serverConfigurationService = null;

	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	public String getInternationalPrefix() {
		return serverConfigurationService.getString(ExternalLogic.PREF_INT_PREFIX, ExternalLogic.PREF_INT_PREFIX_DEFAULT);
	}
	
	public String getCountryCode() {
		return serverConfigurationService.getString(ExternalLogic.PREF_COUNTRY_CODE, ExternalLogic.PREF_COUNTRY_CODE_DEFAULT);
	}

	public String getLocalPrefix() {
		return serverConfigurationService.getString(ExternalLogic.PREF_LOCAL_PREFIX, ExternalLogic.PREF_LOCAL_PREFIX_DEFAULT);
	}
	
	
	public void init() {
		//TODO move this to spring or config
		
		// We know mobile numbers in South Africa are 11 digits (international form)
		countryLength.put("27", 11);
	}
	
	/** 
	 * Set routing and cost information for this message.
	 * @param message The message to route
	 * @return true if the message is routable, otherwise false.
	 */
	public boolean getRoutingInfo(SmsMessage message) {
		
		if (message == null || !isNumberRoutable(message.getMobileNumber())) {
			return false;
		}
		
		// TODO - generalize with a routing table which maps prefix to gateway and cost. 
		// For now, hardcode some assumptions for a single gateway / Clickatell / South Africa
		
		message.setSmscId(SmsConstants.SMSC_ID);
		
		// Clickatell test number range = 0.3 credits, otherwise 1
		
		if (message.getMobileNumber().startsWith("2799")) {
			message.setCredits(0.3);
		} else {
			message.setCredits(1.0);
		}
		
		return true;
	}
	
	public boolean isNumberRoutable(String mobileNumber) {
		LOG.debug("isNumberRoutable(" + mobileNumber + ")");
		if (mobileNumber == null || "".equals(mobileNumber) || mobileNumber.length() < 3) {
			return false;
		}
		
		String code = mobileNumber.substring(0, 2);
		LOG.debug("country code is " + code);
		if (countryLength.containsKey(code)) {
			if (mobileNumber.startsWith(code) && mobileNumber.length() != countryLength.get(code)) {
				return false;
			}
		}
		
		// At present we only support local delivery (i.e. same country)
		String regex= "^" + getCountryCode() + "[0-9]+$";
		if (mobileNumber.matches(regex)) {
			return true;
		}
		
		return false;
	}

	public String normalizeNumber(String mobileNumber) {
		return normalizeNumber(mobileNumber, getInternationalPrefix(), getLocalPrefix(), getCountryCode());
	}
	
	public String normalizeNumber(String mobileNumber, final String intprefix, final String localprefix, final String countrycode) {

		
		if (mobileNumber == null || "".equals(mobileNumber)) {
			return mobileNumber;
		}
		
		//a space would cause a index out of bounds bellow
		if (mobileNumber.trim().length() == 0) {
			return mobileNumber;
		}
		
		StringBuilder newNum = new StringBuilder();
		
		// Get rid of everything except leading + and 0-9
		if (mobileNumber.trim().charAt(0) == '+') {
			newNum.append('+');
		}
		
		for (char c : mobileNumber.toCharArray()) {
			if (c >= '0' && c <= '9') {
				newNum.append(c);
			}
		}
		
		mobileNumber = newNum.toString();

		// Is this already an international number with a country code?

		if (mobileNumber.startsWith("+")) {
			return mobileNumber.substring(1, mobileNumber.length());
		}

		if (mobileNumber.startsWith(intprefix)) {
			return mobileNumber.substring(intprefix.length(), mobileNumber.length());
		}
		
		if (mobileNumber.length() > 10 && !mobileNumber.startsWith(localprefix)) {
			return mobileNumber;
		}
		
		// Local number

		if (mobileNumber.startsWith(localprefix)) {
			// Replace local dialling prefix with country code
			mobileNumber = countrycode + mobileNumber.substring(localprefix.length(), mobileNumber.length());
		} else {
			// Prefix with country code
			mobileNumber = countrycode + mobileNumber;
		}
		
		return mobileNumber;
	}

	public double getIncomingMessageCost(String smscId) {

		// TODO - currently hardcoded for Clickatell
		return 0.33;
	}
}
