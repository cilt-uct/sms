/***********************************************************************************
 * SmsStringArrayUtilTest.java
 * Copyright (c) 2008 Sakai Project/Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.sms.util;

import org.junit.Assert;

import junit.framework.TestCase;

public class SmsStringArrayUtilTest extends TestCase {
	
	public void testToUppercase() {
		String[] arr = new String[] {"aa", "bBB ", "cc-cc"};
		String[] returned = SmsStringArrayUtil.upperCaseArray(arr);
		Assert.assertTrue("AA".equals(returned[0]));
		Assert.assertTrue("BBB ".equals(returned[1]));
		Assert.assertTrue("CC-CC".equals(returned[2]));
	}
	
	public void testFindInArray() {
		String returned = SmsStringArrayUtil.findInArray(new String[] {"AA", "BB", "CC"}, "aA");
		Assert.assertEquals("AA", returned);
		
		returned = SmsStringArrayUtil.findInArray(new String[] {"AA", "BB", "CC"}, null);
		Assert.assertEquals(null, returned);
		
		returned = SmsStringArrayUtil.findInArray(null, "AA");
		Assert.assertEquals(null, returned);
		
		returned = SmsStringArrayUtil.findInArray(new String[] {"AA", "BB", "CC"}, "BB");
		Assert.assertEquals("BB", returned);
	}
	
	public void testCopyOf() {
		String[] copy = SmsStringArrayUtil.copyOf(new String[]{"A", "B"}, 3);
		Assert.assertEquals(3, copy.length);
		Assert.assertEquals("A", copy[0]);
		Assert.assertEquals("B", copy[1]);
		Assert.assertEquals(null, copy[2]);
		
		copy = SmsStringArrayUtil.copyOf(new String[]{"A", "B", "C", "D"}, 3);
		Assert.assertEquals(3, copy.length);
		
	}
}
