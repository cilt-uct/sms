/***********************************************************************************
 * SmsMessageParserImpl.java
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
package org.sakaiproject.sms.logic.incoming.test;

import junit.framework.TestCase;

import org.sakaiproject.sms.logic.incoming.ParsedMessage;
import org.sakaiproject.sms.logic.incoming.SmsMessageParser;
import org.sakaiproject.sms.logic.incoming.impl.SmsMessageParserImpl;
import org.sakaiproject.sms.logic.parser.exception.ParseException;

/**
 * Unit test for incoming message parsing
 */
public class ParserTest extends TestCase {

	private final SmsMessageParser parser = new SmsMessageParserImpl();

	public void testParseStandard() throws ParseException {
		ParsedMessage parsed = parser.parseMessage("command site body");
		assertEquals("site", parsed.getSite());
		assertEquals("command", parsed.getCommand());
		assertEquals("body", parsed.getBody());
		assertTrue(parsed.hasBody());
	}

	public void testParseMultipleBody() throws ParseException {
		ParsedMessage parsed = parser
				.parseMessage("command site  body has multiple");
		assertEquals("body has multiple", parsed.getBody());
		assertTrue(parsed.hasBody());
	}

	public void testParseNoBody() throws ParseException {
		ParsedMessage parsed = parser.parseMessage("command site");
		assertEquals("site", parsed.getSite());
		assertEquals("command", parsed.getCommand());
		assertNull(parsed.getBody());
		assertFalse(parsed.hasBody());
	}

	public void testInvalidNull() {
		try {
			ParsedMessage parsed = parser.parseMessage(null);
		} catch (ParseException e) {
			assertNotNull(e);
		}
	}

	public void testInvalidEmpty() {
		try {
			ParsedMessage parsed = parser.parseMessage("");
			fail("should throw exception");
		} catch (ParseException e) {
			assertNotNull(e);
		}
	}

	public void testHelpCommand() throws ParseException {
		ParsedMessage parsed = parser.parseMessage("help");
		assertEquals("help", parsed.getCommand());
		parsed = parser.parseMessage("HELP");
		assertEquals("HELP", parsed.getCommand());
	}
}
