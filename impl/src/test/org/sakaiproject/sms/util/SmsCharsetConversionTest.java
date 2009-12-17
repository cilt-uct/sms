/***********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 * Copyright (c) 2009 Sakai Project/Sakai Foundation
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

import junit.framework.TestCase;

public class SmsCharsetConversionTest extends TestCase {
	
	/**
	 * Test conversion of GSM charset to UTF
	 * See http://unicode.org/Public/MAPPINGS/ETSI/GSM0338.TXT
	 */
	public void testGsmToUtf() {

		GsmCharset gsm = new GsmCharset();

		// Single char
		byte[] gsmChars1 = { 0x20 };
		String utfMsg1 = gsm.gsmToUtf(gsmChars1);
		assertEquals(" ", utfMsg1);
		
		// Straight through mapping
		String asciiMsg = "hello, these are all normal ASCII chars";
		String utfMsg2 = gsm.gsmToUtf(asciiMsg.getBytes());
		assertEquals(asciiMsg, utfMsg2);
		
		// Invalid character (high-order)
		byte[] gsmChars3 = { -1 };
		String utfMsg3 = gsm.gsmToUtf(gsmChars3);
		assertEquals("", utfMsg3);

		// Invalid extended character
		byte[] gsmChars4 = { 0x1b, 0x15 };
		String utfMsg4 = gsm.gsmToUtf(gsmChars4);
		assertEquals("", utfMsg4);

		// Valid extended character: {
		byte[] gsmChars5 = { 0x1b, 0x28 };
		String utfMsg5 = gsm.gsmToUtf(gsmChars5);
		assertEquals("{", utfMsg5);

	}
	
}
