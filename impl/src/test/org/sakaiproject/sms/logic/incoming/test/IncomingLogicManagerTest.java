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
import java.util.ArrayList;

import junit.framework.TestCase;

import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.incoming.ParsedMessage;
import org.sakaiproject.sms.logic.incoming.impl.SmsIncomingLogicManagerImpl;
import org.sakaiproject.sms.logic.incoming.impl.SmsMessageParserImpl;
import org.sakaiproject.sms.logic.stubs.ExternalLogicStub;
import org.sakaiproject.sms.logic.stubs.commands.CreateSmsCommand;
import org.sakaiproject.sms.logic.stubs.commands.CreateSmsCommandCopy;
import org.sakaiproject.sms.logic.stubs.commands.DeleteSmsCommand;
import org.sakaiproject.sms.logic.stubs.commands.UpdateSmsCommand;
import org.sakaiproject.sms.model.smpp.SmsPatternSearchResult;

/**
 * Unit test for registering of commands
 */
public class IncomingLogicManagerTest extends TestCase {

	private SmsIncomingLogicManagerImpl manager;
	private ExternalLogic externalLogic;

	private CreateSmsCommand createCmd;
	private UpdateSmsCommand updateCmd;
	private DeleteSmsCommand deleteCmd;

	private static String TEST_MOBILE = "1234";
	private static String TEST_SITE = "SakaiSiteID";

	@Override
	public void setUp() {
		manager = new SmsIncomingLogicManagerImpl();
		externalLogic = new ExternalLogicStub();
		manager.setExternalLogic(externalLogic);
		manager.setSmsMessageParser(new SmsMessageParserImpl());

		createCmd = new CreateSmsCommand();
		updateCmd = new UpdateSmsCommand();
		deleteCmd = new DeleteSmsCommand();

		manager.register("TEST", createCmd);
		manager.register("TEST", updateCmd);
		manager.register("TEST", deleteCmd);

	}

	public void testRegisterLogic() {
		assertTrue(manager.isValidCommand("TEST", "CREATE"));
		assertTrue(manager.isValidCommand("TEST", "UPDATE"));
		assertTrue(manager.isValidCommand("TEST", "DELETE"));
		assertFalse(manager.isValidCommand("TEST", "SOMETHING"));
	}

	public void testCaseInsensitivity() {
		assertTrue(manager.isValidCommand("TeST", "cReAtE"));
		assertTrue(manager.isValidCommand("test", "update"));
		assertTrue(manager.isValidCommand("TEST", "delETE"));
		assertFalse(manager.isValidCommand("test", "something"));
	}

	public void testDuplicateReplace() {
		assertTrue(manager.isValidCommand("test", "CREATE"));
		ParsedMessage msg = manager.process("test " + TEST_SITE + " create",
				TEST_MOBILE);
		assertEquals("CREATE", msg.getCommand());
		manager.register("test", new CreateSmsCommandCopy());
		assertTrue(manager.isValidCommand("test", "CREATE"));
	}

	private String loadPropertiesFile(final String name) {
		String fileContents = "";
		try {
			InputStream is = this.getClass().getResourceAsStream("/" + name);
			InputStreamReader input = new InputStreamReader(is);
			BufferedReader jj = new BufferedReader(input);

			while (jj.ready()) {
				fileContents += jj.readLine();

			}

		} catch (Exception e) {

		}
		return fileContents;
	}

	private String[] parseCSVFile(String csv) {

		return csv.split(";");

	}

	public void testProcess() {
		ParsedMessage msg = manager.process("test " + TEST_SITE + " updat",
				TEST_MOBILE);
		assertEquals("UPDATE", msg.getCommand());

	}

	public void testPossibleMatches() {
		String[] validCommands = parseCSVFile(loadPropertiesFile("ValidCommands.txt"));
		String[] commandsToMatch = parseCSVFile(loadPropertiesFile("CommandsToMatch.txt"));
		for (int i = 0; i < commandsToMatch.length; i++) {
			String commandSplit[] = commandsToMatch[i].split("#");
			String command = commandSplit[0];
			SmsPatternSearchResult smsPatternSearchResult = manager
					.getClosestMatch(command, validCommands);
			boolean result = smsPatternSearchResult.getPossibleMatches().size() == Integer
					.parseInt(commandSplit[1]);
			System.out.println("Looking for " + command + " #"
					+ commandSplit[1] + " " + result + " (found "
					+ smsPatternSearchResult.getPossibleMatches().size() + ")");
			assertTrue(result);
		}
	}

	public void testHelpCommand() {
		ParsedMessage msg = manager.process("test " + TEST_SITE + " help",
				TEST_MOBILE);
		assertEquals("TEST will understand CREATE, UPDATE, DELETE", msg
				.getBody_reply());
	}

	public void testGenerateAssistMessage() {
		ArrayList<String> list = new ArrayList<String>();
		list.add("CREATE");
		list.add("UPDATE");
		list.add("DELETE");

		String msg = manager.generateAssistMessage(list, "test");
		assertEquals("test will understand CREATE, UPDATE, DELETE", msg);

	}

	public void testClearCommands() {
		assertTrue(manager.isValidCommand("TEST", "CREATE"));
		assertTrue(manager.isValidCommand("TEST", "UPDATE"));
		assertTrue(manager.isValidCommand("TEST", "DELETE"));
		manager.clearCommands("test");
		assertFalse(manager.isValidCommand("TEST", "CREATE"));
		assertFalse(manager.isValidCommand("TEST", "UPDATE"));
		assertFalse(manager.isValidCommand("TEST", "DELETE"));
	}

}
