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
import static org.junit.Assert.*;

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
	
	public void testIsEncodeableInGsm0338() {

		GsmCharset gsm = new GsmCharset();

		// Straight ASCII
		assertTrue(gsm.isEncodeableInGsm0338("Hello everyone!"));
		
		// Characters with non-ASCII representations in GSM
		assertTrue(gsm.isEncodeableInGsm0338("@${[|]}"));
		
		// Some unusual chars OK in GSM
		String okInGsm = new String("abc" + "\u20AC" + "\u00A5" + "\u007E" + "\u00F6");
		assertTrue(gsm.isEncodeableInGsm0338(okInGsm));
		
		// Unicode not represented in GSM
		String notOkInGsm = new String("from google.cn: " + "\u52a0");
		assertFalse(gsm.isEncodeableInGsm0338(notOkInGsm));
	}
	
	public void testUtfToGsm() {

		GsmCharset gsm = new GsmCharset();

		String messageText = "@${[|]}";	
		byte[] messageBytes = gsm.utfToGsm(messageText);
		byte[] gsmBytes = { 0x00, 0x02, 0x1b, 0x28, 0x1b, 0x3c, 0x1b, 0x40, 0x1b, 0x3e, 0x1b, 0x29};
		assertArrayEquals(gsmBytes, messageBytes);

		String asciiMsg = "hello, these are all normal ASCII chars";
		assertArrayEquals(asciiMsg.getBytes(), gsm.utfToGsm(asciiMsg));
		
		// Some unusual chars OK in GSM
		String okInGsm = new String("ABC" + "\u20AC" + "\u00A5" + "\u007E" + "\u00F6");
		byte[] gsmBytes2 = { 0x41, 0x42, 0x43, 0x1b, 0x65, 0x03, 0x1b, 0x3d, 0x7c};
		assertArrayEquals(gsmBytes2, gsm.utfToGsm(okInGsm));

	}
}
