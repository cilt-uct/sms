package org.sakaiproject.sms.logic.incoming.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.sms.logic.incoming.SmsCommand;

public class RegisteredCommands {

	// commands in command key - command object pairs
	private final Map<String, SmsCommand> commands = new LinkedHashMap<String, SmsCommand>();
	// aliases in alias-command key pairs
	private final Map<String, String> aliasMap = new HashMap<String, String>();

	public RegisteredCommands() {

	}

	public RegisteredCommands(SmsCommand cmd) {
		addCommand(cmd);
	}

	public Set<String> getCommandKeys() {
		return commands.keySet();
	}

	public void addCommand(SmsCommand cmd) {
		final String commandKey = cmd.getCommandKey().toUpperCase();
		commands.remove(commandKey);
		commands.put(commandKey, cmd);
		buildAliasMap(cmd);
	}

	public String findAliasCommandKey(String alias) {
		return aliasMap.get(alias);
	}

	public SmsCommand getCommand(String commandKey) {
		return commands.get(commandKey.toUpperCase());
	}

	public void removeByCommandKey(String commandKey) {
		commands.remove(commandKey.toUpperCase());
		removeAliasByCommandKey(commandKey.toUpperCase());
	}

	private void buildAliasMap(SmsCommand cmd) {
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
}
