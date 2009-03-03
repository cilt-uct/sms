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
import org.sakaiproject.sms.logic.incoming.ParsedMessage;
import org.sakaiproject.sms.logic.incoming.SmsIncomingLogicManager;
import org.sakaiproject.sms.logic.incoming.impl.SmsIncomingLogicManagerImpl;
import org.sakaiproject.sms.logic.stubs.TestIncomingSmsLogic;
import org.sakaiproject.sms.model.smpp.SmsPatternSearchResult;

/**
 * Unit test for registering of commands
 */
public class IncomingLogicManagerTest extends TestCase {

	private String validCommands;
	private String smsedCommands;
	private SmsIncomingLogicManager manager;
	private TestIncomingSmsLogic logic;

	@Override
	public void setUp() {
		manager = new SmsIncomingLogicManagerImpl();
		logic = new TestIncomingSmsLogic();
	}

	public void testRegisterLogic() {
		manager.register("TEST", logic);
		assertTrue(manager.isValidCommand("TEST", "CREATE"));
		assertTrue(manager.isValidCommand("TEST", "UPDATE"));
		assertTrue(manager.isValidCommand("TEST", "DELETE"));
		assertFalse(manager.isValidCommand("TEST", "SOMETHING"));
	}

	public void testCaseInsensitivity() {
		manager.register("test", logic);
		assertTrue(manager.isValidCommand("TeST", "cReAtE"));
		assertTrue(manager.isValidCommand("test", "update"));
		assertTrue(manager.isValidCommand("TEST", "delETE"));
		assertFalse(manager.isValidCommand("test", "something"));
	}

	public void testDuplicateReplace() {
		manager.register("test", logic);
		assertFalse(manager.isValidCommand("test", "something"));
		TestIncomingSmsLogic newLogic = new TestIncomingSmsLogic();
		newLogic.setCommandKeys(new String[] { "SOMETHING" });
		manager.register("test", newLogic);
		assertTrue(manager.isValidCommand("test", "something"));
		assertFalse(manager.isValidCommand("test", "CREATE"));
		assertFalse(manager.isValidCommand("test", "UPDATE"));
		assertFalse(manager.isValidCommand("test", "DELETE"));
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
		manager.register("test", logic);

		ParsedMessage msg = new ParsedMessage("test", "site", "userId", "create");
		manager.process(msg);
		assertEquals("CREATE", logic.getLastExecuted());

		msg = new ParsedMessage("test", "site", "userId", "updat");
		manager.process(msg);
		assertEquals("UPDATE", logic.getLastExecuted());

		msg = new ParsedMessage("test", "site", "userId", "DELET");
		manager.process(msg);
		assertEquals("DELETE", logic.getLastExecuted());

	}



	public void testPossibleMatches() {
		String[] validCommands = parseCSVFile(loadPropertiesFile("ValidCommands.txt"));
		String[] commandsToMatch = parseCSVFile(loadPropertiesFile("CommandsToMatch.txt"));
		for (int i = 0; i < commandsToMatch.length; i++) {
			String commandSplit[] = commandsToMatch[i].split("#");
			String command = commandSplit[0];
			SmsPatternSearchResult smsPatternSearchResult = manager
					.getClosestMatch(command, validCommands);
			assertTrue(smsPatternSearchResult.getPossibleMatches().size() == Integer
					.parseInt(commandSplit[1]));
		}
	}

	public void testHelpCommand() {
		manager.register("test", logic);
		assertTrue(manager.isValidCommand("test", "help"));
		ParsedMessage msg = new ParsedMessage("test", "site", "userId", "help");
		String value = manager.process(msg);
		assertEquals("Valid commands: \nCREATE, UPDATE, DELETE", value);

	}

	public void testGenerateAssistMessage() {
		manager.register("test", logic);
		ArrayList list = new ArrayList();
		list.add("CREATE");
		list.add("UPDATE");
		list.add("DELETE");

		String msg = manager.generateAssistMessage(list, "test");
		assertEquals("Valid commands: \nCREATE, UPDATE, DELETE", msg);

	}

}
