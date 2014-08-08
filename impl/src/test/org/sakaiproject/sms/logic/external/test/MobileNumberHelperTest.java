package org.sakaiproject.sms.logic.external.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.sakaiproject.sms.logic.external.MobileNumberHelperImpl;
import org.sakaiproject.sms.logic.external.NumberRoutingHelperImpl;
import org.sakaiproject.sms.logic.stubs.SakaiPersonManagerStub;
import org.sakaiproject.sms.logic.stubs.ServerConfigurationServiceStubb;
import org.sakaiproject.sms.logic.stubs.UserDirectoryServiceStub;

public class MobileNumberHelperTest {

	private MobileNumberHelperImpl mobileNumberHelper;
	private NumberRoutingHelperImpl numberRoutingHelper;
	
	@Before
	public void setUp() {
		numberRoutingHelper = new NumberRoutingHelperImpl();
		numberRoutingHelper.setServerConfigurationService(new ServerConfigurationServiceStubb());
		mobileNumberHelper = new MobileNumberHelperImpl();
		mobileNumberHelper.setSakaiPersonManager(new SakaiPersonManagerStub());
		mobileNumberHelper.setUserDirectoryService(new UserDirectoryServiceStub());
		mobileNumberHelper.setServerConfigurationService(new ServerConfigurationServiceStubb());
		mobileNumberHelper.setNumberRoutingHelper(numberRoutingHelper);
	}

    @Test
	public void testGetMobile() {
		String number = mobileNumberHelper.getUserMobileNumber("test");
		assertEquals("27123456789", number);
	}

    @Test
	public void testNullGetMobile() {
		String number = mobileNumberHelper.getUserMobileNumber(null);
		assertEquals(null, number);
	}

    @Test
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

    @Test
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
	
    @Test
	public void testNormalizeNumber() {
		
		// South Africa (00 / 0 / 27)
		
		assertEquals("27831234567", numberRoutingHelper.normalizeNumber("0831234567", "00", "0", "27"));    
		assertEquals("27831234567", numberRoutingHelper.normalizeNumber("083-123-4567", "00", "0", "27"));
		assertEquals("27831234567", numberRoutingHelper.normalizeNumber("083 123-4567", "00", "0", "27"));
		assertEquals("27831234567", numberRoutingHelper.normalizeNumber("083/123,4567", "00", "0", "27"));
		assertEquals("27831234567", numberRoutingHelper.normalizeNumber("083.123.4567", "00", "0", "27"));
		assertEquals("27831234567", numberRoutingHelper.normalizeNumber("083 123 4567", "00", "0", "27"));
		assertEquals("27831234567", numberRoutingHelper.normalizeNumber("+27 83 123-4567", "00", "0", "27"));
		assertEquals("27831234567", numberRoutingHelper.normalizeNumber("+27+83-123-4567", "00", "0", "27"));
		assertEquals("27831234567", numberRoutingHelper.normalizeNumber("+27 83 123-4567", "00", "0", "27"));
		assertEquals("27831234567", numberRoutingHelper.normalizeNumber("+27 83 123-4567", "00", "0", "27"));
		assertEquals("447831234567", numberRoutingHelper.normalizeNumber("00 44 783 123-4567", "00", "0", "27"));
		assertEquals("447831234567", numberRoutingHelper.normalizeNumber("+44 783 123-4567", "00", "0", "27"));

		// US (00 / 1 / 1)
		
		assertEquals("12121234567", numberRoutingHelper.normalizeNumber("212 123 4567", "00", "1", "1"));
		assertEquals("12121234567", numberRoutingHelper.normalizeNumber("+1 212 123 4567", "00", "1", "1"));
		assertEquals("12121234567", numberRoutingHelper.normalizeNumber("+1 212.123.4567", "00", "1", "1"));
		assertEquals("12121234567", numberRoutingHelper.normalizeNumber("+1 (212) 123 4567", "00", "1", "1"));
		assertEquals("12121234567", numberRoutingHelper.normalizeNumber("1 (212) 123 4567", "00", "1", "1"));
		assertEquals("12121234567", numberRoutingHelper.normalizeNumber("1-212-123-4567", "00", "1", "1"));
		assertEquals("27831234567", numberRoutingHelper.normalizeNumber("+27 83 123-4567", "00", "1", "1"));
		assertEquals("447831234567", numberRoutingHelper.normalizeNumber("00 44 783 123-4567", "00", "1", "1"));

		// UK (00 / 0 / 44)
		
		assertEquals("12121234567", numberRoutingHelper.normalizeNumber("+1 212 123 4567", "00", "0", "44"));
		assertEquals("27831234567", numberRoutingHelper.normalizeNumber("+27 83 123-4567", "00", "0", "44"));
		assertEquals("12121234567", numberRoutingHelper.normalizeNumber("00 1 212 123 4567", "00", "0", "44"));
		assertEquals("27831234567", numberRoutingHelper.normalizeNumber("00 27 83 123-4567", "00", "0", "44"));
		assertEquals("447831234567", numberRoutingHelper.normalizeNumber("0783 123-4567", "00", "0", "44"));
		
		// Numbers already in canonical international form should stay unchanged
		
		assertEquals("447831234567", numberRoutingHelper.normalizeNumber("44 783 123 4567", "00", "0", "44"));
		assertEquals("447831234567", numberRoutingHelper.normalizeNumber("44 783 123 4567", "00", "0", "27"));
		assertEquals("447831234567", numberRoutingHelper.normalizeNumber("44 783 123 4567", "00", "1", "1"));
		assertEquals("27831234567", numberRoutingHelper.normalizeNumber("27 83 123-4567", "00", "0", "27"));
		assertEquals("27831234567", numberRoutingHelper.normalizeNumber("27831234567", "00", "0", "27"));
		
		
	}
}
