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
		
		// Get rid of punctuation
		mobileNumber = mobileNumber.replace("\\s", "").replace("-", "").replace("+", "").
			replace(" ", "").replace("(", "").replace(")", "");

		// Replace international dialling prefix with + if present
		if (mobileNumber.startsWith(intprefix)) {
			mobileNumber = mobileNumber.substring(intprefix.length(), mobileNumber.length());
		} else {
			// Replace local dialling prefix if present with country code 
			if (mobileNumber.startsWith(localprefix)) {
				mobileNumber = countrycode + mobileNumber.substring(localprefix.length(), mobileNumber.length());
			}	
		}
		
		return mobileNumber;
	}
}
