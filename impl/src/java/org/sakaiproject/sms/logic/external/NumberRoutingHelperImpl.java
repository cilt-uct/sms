package org.sakaiproject.sms.logic.external;

import org.sakaiproject.component.api.ServerConfigurationService;

public class NumberRoutingHelperImpl implements NumberRoutingHelper {

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
	
	public boolean isNumberRoutable(String mobileNumber) {
		if (mobileNumber == null || "".equals(mobileNumber)) {
			return false;
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
		
		StringBuilder newNum = new StringBuilder();
		
		// Get rid of everything except leading + and 0-9
		if (mobileNumber.trim().startsWith("+")) {
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
}
