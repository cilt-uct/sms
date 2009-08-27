package org.sakaiproject.sms.logic.external.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;


import org.sakaiproject.sms.logic.external.MobileNumberHelperImpl;
import org.sakaiproject.sms.logic.external.NumberRoutingHelperImpl;
import org.sakaiproject.sms.logic.stubs.SakaiPersonManagerStub;
import org.sakaiproject.sms.logic.stubs.ServerConfigurationServiceStubb;
import org.sakaiproject.sms.logic.stubs.UserDirectoryServiceStub;

public class MobileNumberHelperTest extends TestCase {

	private MobileNumberHelperImpl mobileNumberHelper;
	private NumberRoutingHelperImpl numberRoutingHelper;
	
	@Override
	public void setUp() {
		
		numberRoutingHelper = new NumberRoutingHelperImpl();
		numberRoutingHelper.setServerConfigurationService(new ServerConfigurationServiceStubb());
		
		mobileNumberHelper = new MobileNumberHelperImpl();
		mobileNumberHelper.setSakaiPersonManager(new SakaiPersonManagerStub());
		mobileNumberHelper.setUserDirectoryService(new UserDirectoryServiceStub());
		mobileNumberHelper.setServerConfigurationService(new ServerConfigurationServiceStubb());
		mobileNumberHelper.setNumberRoutingHelper(numberRoutingHelper);
	}

	public void testGetMobile() {
		String number = mobileNumberHelper.getUserMobileNumber("test");
		assertEquals("27123456789", number);
	}

	public void testNullGetMobile() {
		String number = mobileNumberHelper.getUserMobileNumber(null);
		assertEquals(null, number);
	}

	public void testMultiple() {
		List<String> userids = new ArrayList<String>();
		userids.add("test1");
		userids.add("test2");
		userids.add("test3");
		Map<String, String> numbers = mobileNumberHelper
				.getUserMobileNumbers(userids);
		assertEquals(3, numbers.size());
		assertEquals("27123456789", numbers.get("test1"));
		assertEquals("27123456789", numbers.get("test2"));
		assertEquals("27123456789", numbers.get("test3"));
	}

	public void testMultipleWithNull() {
		List<String> userids = new ArrayList<String>();
		userids.add("test1");
		userids.add(null);
		userids.add("test3");
		Map<String, String> numbers = mobileNumberHelper
				.getUserMobileNumbers(userids);
		assertEquals(2, numbers.size());
		assertEquals("27123456789", numbers.get("test1"));
		assertEquals(null, numbers.get("test2"));
		assertEquals("27123456789", numbers.get("test3"));
	}
}
