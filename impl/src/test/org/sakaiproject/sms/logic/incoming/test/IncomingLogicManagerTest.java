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
package org.sakaiproject.sms.logic.incoming.test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.incoming.DuplicateCommandKeyException;
import org.sakaiproject.sms.logic.incoming.ParsedMessage;
import org.sakaiproject.sms.logic.incoming.ShortMessageCommand;
import org.sakaiproject.sms.logic.incoming.impl.SmsIncomingLogicManagerImpl;
import org.sakaiproject.sms.logic.incoming.impl.SmsMessageParserImpl;
import org.sakaiproject.sms.logic.stubs.ExternalLogicStub;
import org.sakaiproject.sms.logic.stubs.commands.CreateSmsCommand;
import org.sakaiproject.sms.logic.stubs.commands.CreateSmsCommandCopy;
import org.sakaiproject.sms.logic.stubs.commands.DeleteSmsCommand;
import org.sakaiproject.sms.logic.stubs.commands.HiddenSmsCommand;
import org.sakaiproject.sms.logic.stubs.commands.MultipleBodySmsCommand;
import org.sakaiproject.sms.logic.stubs.commands.UpdateSmsCommand;
import org.sakaiproject.sms.model.hibernate.constants.SmsConstants;
import org.sakaiproject.sms.model.smpp.SmsPatternSearchResult;

/**
 * Unit test for registering of commands.This unit test must be run from
 * Eclipse.
 */
public class IncomingLogicManagerTest extends TestCase {

	private SmsIncomingLogicManagerImpl manager;
	private ExternalLogic externalLogic;

	private CreateSmsCommand createCmd;
	private UpdateSmsCommand updateCmd;
	private DeleteSmsCommand deleteCmd;
	private MultipleBodySmsCommand multipleCmd;
	private HiddenSmsCommand hiddenCmd;

	private static String TEST_MOBILE = "1234";
	private static String TEST_SITE = SmsConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID;

	@Override
	public void setUp() {
		manager = new SmsIncomingLogicManagerImpl();
		externalLogic = new ExternalLogicStub();
		manager.setExternalLogic(externalLogic);
		manager.setSmsMessageParser(new SmsMessageParserImpl());
		createCmd = new CreateSmsCommand();
		updateCmd = new UpdateSmsCommand();
		deleteCmd = new DeleteSmsCommand();
		multipleCmd = new MultipleBodySmsCommand();
		hiddenCmd = new HiddenSmsCommand();

		manager.register("TEST", createCmd);
		manager.register("TEST", updateCmd);
		manager.register("TEST", deleteCmd);
		manager.register("TEST", multipleCmd);

	}

	public void testRegisterLogic() {
		assertTrue(manager.isValidCommand("CREATE"));
		assertTrue(manager.isValidCommand("UPDATE"));
		assertTrue(manager.isValidCommand("DELETE"));
		assertFalse(manager.isValidCommand("SOMETHING"));
	}

	public void testCaseInsensitivity() {
		assertTrue(manager.isValidCommand("cReAtE"));
		assertTrue(manager.isValidCommand("update"));
		assertTrue(manager.isValidCommand("delETE"));
		assertFalse(manager.isValidCommand("something"));
	}

	public void testDuplicateNotReplace() {
		ParsedMessage msg = manager.process("create test body", TEST_MOBILE);
		assertTrue(manager.isValidCommand("CREATE"));
		assertEquals("CREATE", msg.getCommand());
		try {
			manager.register("test", new CreateSmsCommandCopy());
			fail("Should throw exception");
		} catch (DuplicateCommandKeyException de) {
			assertNotNull(de);
		}

		assertTrue(manager.isValidCommand("CREATE"));
		assertEquals("CREATE", manager.process("create " + TEST_SITE + " body",
				TEST_MOBILE).getBodyReply());
	}

	private String loadPropertiesFile(final String name) {
		String fileContents = "";
		try {
			InputStream is = IncomingLogicManagerTest.class.getClassLoader()
					.getResourceAsStream(name);
			InputStreamReader input = new InputStreamReader(is);
			BufferedReader bufferedReader = new BufferedReader(input);

			while (bufferedReader.ready()) {
				fileContents += bufferedReader.readLine();

			}
			bufferedReader.close();

		} catch (Exception e) {

		}
		return fileContents;
	}

	private String[] parseCSVFile(String csv) {

		return csv.split(";");

	}

	public void testProcess() {
		ParsedMessage msg = manager.process("updat test", TEST_MOBILE);
		assertEquals("UPDATE", msg.getCommand());

	}

	public void testPossibleMatches() {
		List<String> validCommands = Arrays.asList(parseCSVFile(loadPropertiesFile("ValidCommands.txt")));
		List<String> commandsToMatch = Arrays.asList(parseCSVFile(loadPropertiesFile("CommandsToMatch.txt")));
		for (String commandToMatch : commandsToMatch) {
			String commandSplit[] = commandToMatch.split("#");
			String command = commandSplit[0].toUpperCase();
			SmsPatternSearchResult smsPatternSearchResult = manager
					.getClosestMatch(command, validCommands);
			boolean result = smsPatternSearchResult.getPossibleMatches().size() == Integer
					.parseInt(commandSplit[1]);
			// System.out.println("Looking for " + command + " #"
			//  + commandSplit[1] + " " + result + " (found "
			//  + smsPatternSearchResult.getPossibleMatches().size() + ")");
			assertTrue(result);
		}
	}
	/* not sure how we can test with resourceloader
	public void testHelpCommand() {
		assertTrue(manager.isValidCommand("help"));
		ParsedMessage msg = manager.process("help", TEST_MOBILE);
		assertEquals("Valid commands: CREATE, UPDATE, DELETE, MULTIPLE", msg
				.getBodyReply());
	}

	
	public void testGenerateAssistMessage() {
		ArrayList<String> list = new ArrayList<String>();
		list.add("CREATE");
		list.add("UPDATE");
		list.add("DELETE");

		String msg = manager.generateAssistMessage(list, null);
		assertEquals("Possible matches: CREATE, UPDATE, DELETE", msg);

	}
	*/

	public void testClearCommands() {
		assertTrue(manager.isValidCommand("CREATE"));
		assertTrue(manager.isValidCommand("UPDATE"));
		assertTrue(manager.isValidCommand("DELETE"));
		manager.clearCommands("test");
		assertFalse(manager.isValidCommand("CREATE"));
		assertFalse(manager.isValidCommand("UPDATE"));
		assertFalse(manager.isValidCommand("DELETE"));
	}

	public void testCommandHelpMessage() {
		ParsedMessage msg = manager.process("CREATE " + TEST_SITE, TEST_MOBILE);
		System.out.println("expected help: " + createCmd.getHelpMessage(ShortMessageCommand.MESSAGE_TYPE_SMS));
		System.out.println("actual reply: " + msg.getBodyReply());
		assertEquals(createCmd.getHelpMessage(ShortMessageCommand.MESSAGE_TYPE_SMS), msg.getBodyReply());

		msg = manager.process("UPDATE", TEST_MOBILE);
		assertEquals(updateCmd.getHelpMessage(ShortMessageCommand.MESSAGE_TYPE_SMS), msg.getBodyReply());
	}

	public void testMultipleBody() {
		ParsedMessage msg = manager.process("MULTIPLE " + TEST_SITE
				+ " PARAM1 PARAM2", TEST_MOBILE);
		assertEquals("MULTIPLE", msg.getBodyReply());
		assertEquals("PARAM1", multipleCmd.param1);
		assertEquals("PARAM2", multipleCmd.param2);
	}

	public void testMultipleBodyInvalid() {
		ParsedMessage msg = manager.process(
				"MULTIPLE " + TEST_SITE + " PARAM1", TEST_MOBILE);
		assertEquals("MULTIPLE HELP", msg.getBodyReply());
	}

	/**
	 * Hidden command must not show up command list
	 */
	/*
	public void testHiddenSmsCommandHelp() {
		manager.register("TEST", hiddenCmd);
		ParsedMessage msg = manager.process("help", TEST_MOBILE);
		assertEquals("Valid commands: CREATE, UPDATE, DELETE, MULTIPLE", msg
				.getBodyReply());
	}
*/
	/**
	 * Hidden commands must not return help
	 */
	public void testHiddenSmsCommandNullBodyNoMessage() {
		manager.register("TEST", hiddenCmd);
		ParsedMessage msg = manager.process("hidden " + TEST_SITE, TEST_MOBILE);
		assertEquals(null, msg.getBodyReply());
	}

	/**
	 * Hidden commands must not return help
	 */
	public void testHiddenSmsCommandNotEnoughBodyNoMessage() {
		manager.register("TEST", hiddenCmd);
		ParsedMessage msg = manager.process("hidden " + TEST_SITE + " param1",
				TEST_MOBILE);
		assertEquals(null, msg.getBodyReply());
	}

	/**
	 * Hidden commands must be processed
	 */
	public void testHiddenSmsCommandValidProcess() {
		manager.register("TEST", hiddenCmd);
		ParsedMessage msg = manager.process("hidden " + TEST_SITE
				+ " param1 param2", TEST_MOBILE);
		assertEquals("HIDDEN", msg.getCommand());
	}

	/**
	 * Tests "" incomming commmands
	 */
	public void testBlankMessage() {
		ParsedMessage msg = manager.process("", TEST_MOBILE);
		assertEquals(null, msg.getCommand());
	}

	/**
	 * Tests null incomming commmands
	 */
	public void testNullMessage() {
		ParsedMessage msg = manager.process(null, TEST_MOBILE);
		assertEquals(null, msg.getCommand());
	}

	/**
	 * Tests null incomming commmands
	 */
	public void testInvalidMessage() {
		ParsedMessage msg = manager.process("ssdfsdfsdfsdfsdfsdfsdfSDF",
				TEST_MOBILE);
		assertEquals(null, msg.getCommand());
	}

}
