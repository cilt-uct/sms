/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.sms.logic.incoming.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.sakaiproject.sms.logic.incoming.ShortMessageCommand;

public class RegisteredCommands {

	// commands in command key - command object pairs
	private final Map<String, ShortMessageCommand> commands = new LinkedHashMap<String, ShortMessageCommand>();
	// aliases in alias-command key pairs
	private final Map<String, String> aliasMap = new HashMap<String, String>();

	public RegisteredCommands() {

	}

	public RegisteredCommands(ShortMessageCommand cmd) {
		addCommand(cmd);
	}

	public Set<String> getCommandKeys() {
		return commands.keySet();
	}

	public void addCommand(ShortMessageCommand cmd) {
		final String commandKey = cmd.getCommandKey().toUpperCase();
		commands.remove(commandKey);
		commands.put(commandKey, cmd);
		buildAliasMap(cmd);
	}

	public String findAliasCommandKey(String alias) {
		return aliasMap.get(alias);
	}

	public ShortMessageCommand getCommand(String commandKey) {
		return commands.get(commandKey.toUpperCase());
	}

	public void removeByCommandKey(String commandKey) {
		commands.remove(commandKey.toUpperCase());
		removeAliasByCommandKey(commandKey.toUpperCase());
	}

	private void buildAliasMap(ShortMessageCommand cmd) {
		final String[] aliases = cmd.getAliases();
		for (String alias : aliases) {
			aliasMap.remove(alias); // remove any previous aliases
			aliasMap.put(alias, cmd.getCommandKey());
		}
	}

	private void removeAliasByCommandKey(String commandKey) {
		final Set<String> aliases = aliasMap.keySet();
		final List<String> toRemove = new ArrayList<String>();

		for (String alias : aliases) {
			final String aliasCommand = aliasMap.get(alias);
			if (aliasCommand.equalsIgnoreCase(commandKey)) {
				toRemove.add(alias);
			}
		}

		for (String alias : toRemove) {
			aliasMap.remove(alias);
		}

	}
	
	public List<ShortMessageCommand> getCommands() {
		List<ShortMessageCommand> ret = new ArrayList<ShortMessageCommand>();
		Iterator<Entry<String, ShortMessageCommand>> it = commands.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, ShortMessageCommand> entry = it.next();
			ShortMessageCommand cmd = entry.getValue();
			ret.add(cmd);
		}
		return ret;
	}
}
