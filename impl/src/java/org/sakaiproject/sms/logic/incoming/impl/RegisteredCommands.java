package org.sakaiproject.sms.logic.incoming.impl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.sms.logic.incoming.SmsCommand;

public class RegisteredCommands {
	
	// commands in command key - command object pairs
	private final Map<String, SmsCommand> commands = new LinkedHashMap<String, SmsCommand>();
	// aliases in alias-command key pairs
	private final Map<String, String> aliasMap = new HashMap<String, String>();
	
	
	public RegisteredCommands(SmsCommand cmd) {
		addCommand(cmd);
	}
	
	public Set<String> getCommandKeys() {
		return commands.keySet();
	}
	
	public void addCommand(SmsCommand cmd) {
		String commandKey = cmd.getCommandKey().toUpperCase();
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
	
	private void buildAliasMap(SmsCommand cmd) {
		String[] aliases = cmd.getAliases();
		for (String alias : aliases) {
			aliasMap.remove(alias); // remove any previous aliases
			aliasMap.put(alias, cmd.getCommandKey());
		}
	}
}
