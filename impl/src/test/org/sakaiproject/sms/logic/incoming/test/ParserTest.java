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

import static org.junit.Assert.*;
import org.junit.Test;

import org.sakaiproject.sms.logic.incoming.ParsedMessage;
import org.sakaiproject.sms.logic.incoming.SmsMessageParser;
import org.sakaiproject.sms.logic.incoming.impl.SmsMessageParserImpl;
import org.sakaiproject.sms.logic.parser.exception.ParseException;

/**
 * Unit test for incoming message parsing
 */
public class ParserTest{

	private final SmsMessageParser parser = new SmsMessageParserImpl();

    @Test
	public void testParseStandard() throws ParseException {
		
		ParsedMessage parsed = new ParsedMessage();
		parsed.setBody("command site body");
		
		parser.parseCommand(parsed);
		assertEquals("command", parsed.getCommand());

		parser.parseSite(parsed);
		assertEquals("site", parsed.getSite());

		parser.parseBody(parsed, 1, true);
		assertEquals("body", parsed.getBodyParameters()[0]);
	}

    @Test
	public void testParseStandardNoSite() throws ParseException {
		
		ParsedMessage parsed = new ParsedMessage();
		parsed.setBody("command body");
		
		parser.parseCommand(parsed);
		assertEquals("command", parsed.getCommand());

		parser.parseBody(parsed, 1, false);
		assertEquals("body", parsed.getBodyParameters()[0]);
	}

    @Test
	public void testParseMultipleBody() throws ParseException {

		ParsedMessage parsed = new ParsedMessage();
		parsed.setBody("command site  body has multiple");
		parser.parseBody(parsed, 2, true);
		assertEquals("body", parsed.getBodyParameters()[0]);
		assertEquals("has multiple", parsed.getBodyParameters()[1]);
		
		parser.parseBody(parsed, 3, true);
		assertEquals("body", parsed.getBodyParameters()[0]);
		assertEquals("has", parsed.getBodyParameters()[1]);
		assertEquals("multiple", parsed.getBodyParameters()[2]);
		
	}

    @Test
	public void testParseMultipleNoSite() throws ParseException {
		ParsedMessage parsed = new ParsedMessage();
		parsed.setBody("command body has multiple");
		
		parser.parseBody(parsed, 2, false);
		assertEquals("body", parsed.getBodyParameters()[0]);
		assertEquals("has multiple", parsed.getBodyParameters()[1]);
		
		parser.parseBody(parsed, 3, false);
		assertEquals("body", parsed.getBodyParameters()[0]);
		assertEquals("has", parsed.getBodyParameters()[1]);
		assertEquals("multiple", parsed.getBodyParameters()[2]);		
	}

    @Test
	public void testSingleCommand() throws ParseException {
		ParsedMessage parsed = new ParsedMessage();
		parsed.setBody("command");

		parser.parseCommand(parsed);

		assertEquals("command", parsed.getCommand());
	}

    @Test
	public void testParseNoBody() throws ParseException {
		ParsedMessage parsed = new ParsedMessage();
		parsed.setBody("command site");

		parser.parseCommand(parsed);
		parser.parseSite(parsed);

		assertEquals("site", parsed.getSite());
		assertEquals("command", parsed.getCommand());
	}

    @Test
	public void testMissingParams() throws ParseException {
		ParsedMessage parsed = new ParsedMessage();
		parsed.setBody("command site");

		parser.parseCommand(parsed);
		parser.parseSite(parsed);
		assertEquals("site", parsed.getSite());
		assertEquals("command", parsed.getCommand());
		
		try {
			parser.parseBody(parsed, 1, true);			
			fail("should throw exception");
		} catch (ParseException e) {
			assertNotNull(e);
			return;
		}
		
		assertTrue("should have failed", false);
	}

    @Test
	public void testInvalidNull() {
		try {
			ParsedMessage parsed = new ParsedMessage();
			parser.parseCommand(parsed);
			parser.parseSite(parsed);			
		} catch (ParseException e) {
			assertNotNull(e);
		}
	}

    @Test
	public void testInvalidEmpty() {
		try {
			ParsedMessage parsed = new ParsedMessage();
			parsed.setBody("");
			parser.parseCommand(parsed);
			parser.parseSite(parsed);			
			fail("should throw exception");
		} catch (ParseException e) {
			assertNotNull(e);
		}
	}

    @Test
	public void testHelpCommand() throws ParseException {

		ParsedMessage parsed = new ParsedMessage();
		
		parsed.setBody("help");
		parser.parseCommand(parsed);
		assertEquals("help", parsed.getCommand());

		parsed.setBody("HELP");
		parser.parseCommand(parsed);
		assertEquals("HELP", parsed.getCommand());
	}

    @Test
	public void testParseBodyInvalid() {

		ParsedMessage parsed = new ParsedMessage();		
		parsed.setBody("command site t t");
	
		try {
			parser.parseBody(parsed, 3, true);
			fail("Should throw exception");
		} catch (ParseException pe) {
			assertNotNull(pe);
		}

	}
	
}
