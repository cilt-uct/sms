package org.sakaiproject.sms.logic.external.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.sakaiproject.sms.logic.external.MobileNumberHelperImpl;

public class MobileNumberHelperTest extends TestCase {
	
	private MobileNumberHelperImpl mobileNumberHelper;
	
	@Override
	public void setUp() {
		mobileNumberHelper = new MobileNumberHelperImpl();
		
	}
	
	public void testGetMobile() {
		String number = mobileNumberHelper.getUserMobileNumber("test");
		assertEquals("0123456789", number);
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
		Map<String,String> numbers = mobileNumberHelper.getUserMobileNumbers(userids);	
		assertEquals(3, numbers.size());
		assertEquals("0123456789", numbers.get("test1"));
		assertEquals("0123456789", numbers.get("test2"));
		assertEquals("0123456789", numbers.get("test3"));
	}
	
	public void testMultipleWithNull() {
		List<String> userids = new ArrayList<String>();
		userids.add("test1");
		userids.add(null);
		userids.add("test3");
		Map<String,String> numbers = mobileNumberHelper.getUserMobileNumbers(userids);	
		assertEquals(3, numbers.size());
		assertEquals("0123456789", numbers.get("test1"));
		assertEquals(null, numbers.get("test2"));
		assertEquals("0123456789", numbers.get("test3"));
	}
}
