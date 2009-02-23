/***********************************************************************************
 * CommandRegisterTest.java
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
package org.sakaiproject.sms.logic.parser.test;

import junit.framework.TestCase;

import org.sakaiproject.sms.logic.parser.impl.SmsMessageParserImpl;

/**
 * Unit test for registering of commands
 */
public class CommandRegisterTest extends TestCase {
	
	private final SmsMessageParserImpl parser = new SmsMessageParserImpl();
	
	public void testRegisterCommands() {
		parser.toolRegisterCommands("toolid1", new String[] {"save", "copy", "delete"});
		assertEquals("SAVE", parser.toolMatchCommand("toolid1", "save"));
		assertEquals("COPY", parser.toolMatchCommand("toolid1", "copy"));
		assertEquals("DELETE", parser.toolMatchCommand("toolid1", "delete"));
		assertFalse(parser.toolMatchCommand("toolid1", "someting").equals("SOMETHING"));
	}
	
	public void testDuplicateReplace() {
		parser.toolRegisterCommands("toolid1", new String[] {"save", "copy", "delete"});
		parser.toolRegisterCommands("toolid1", new String[] {"something"});
		assertFalse(parser.toolMatchCommand("toolid1", "save").equals("SAVE"));
		assertFalse(parser.toolMatchCommand("toolid1", "copy").equals("COPY"));
		assertFalse(parser.toolMatchCommand("toolid1", "delete").equals("DELETE"));		
		assertTrue(parser.toolMatchCommand("toolid1", "something").equals("SOMETHING"));
	}
	
	public void testMatchToolTypo() {
		parser.toolRegisterCommands("toolid1", new String[] {"save", "copy", "delete"});
		assertEquals("SAVE", parser.toolMatchCommand("toolid", "save"));
	}
	
	public void testMatchCommandTypo() {
		parser.toolRegisterCommands("toolid1", new String[] {"save", "copy", "delete"});
		assertEquals("SAVE", parser.toolMatchCommand("toolid1", "savve"));
	}
	
	
}
