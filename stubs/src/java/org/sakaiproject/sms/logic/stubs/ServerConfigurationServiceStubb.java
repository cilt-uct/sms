package org.sakaiproject.sms.logic.stubs;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.sakaiproject.component.api.ServerConfigurationService;

public class ServerConfigurationServiceStubb implements ServerConfigurationService {

	public String getAccessPath() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getAccessUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean getBoolean(String name, boolean dflt) {
		return dflt;
	}

	@SuppressWarnings("unchecked")
	public List getDefaultTools(String category) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getGatewaySiteId() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getHelpUrl(String helpContext) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getInt(String name, int dflt) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getLoggedOutUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPortalUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRawProperty(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSakaiHomePath() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getServerId() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getServerIdInstance() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getServerInstance() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getServerName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getServerUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getString(String name) {
		return null;
	}

	public String getString(String name, String dflt) {
		
		return dflt;
	}

	public String[] getStrings(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getToolCategories(String category) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, List<String>> getToolCategoriesAsMap(String category) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	public List getToolOrder(String category) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, String> getToolToCategoryMap(String category) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getToolUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	public List getToolsRequired(String category) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUserHomeUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public Locale[] getSakaiLocales() {
		// TODO Auto-generated method stub
		return null;
	}

	public Locale getLocaleFromString(String localeString) {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> T getConfig(String name, T defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	public ConfigItem getConfigItem(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public ConfigItem registerConfigItem(ConfigItem configItem) {
		// TODO Auto-generated method stub
		return null;
	}

	public void registerListener(ConfigurationListener configurationListener) {
		// TODO Auto-generated method stub
		
	}

	public ConfigData getConfigData() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getCategoryGroups(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getToolGroup(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean toolGroupIsRequired(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean toolGroupIsSelected(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return false;
	}

        public Collection<String> getServerNameAliases() {
                // TODO Auto-generated method stub
                return null;
        }

        public List<String> getStringList(String name, List<String> dflt) {
                // TODO Auto-generated method stub
                return null;
        }

        public List<Pattern> getPatternList(String name, List<String> dflt) {
                // TODO Auto-generated method stub
                return null;
        }

        public Set<String> getCommaSeparatedListAsSet(String key) {
                return null;
        }

        public long getLong(String name, long dflt) {
		return 0;
	}


}
