/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/search/trunk/search-impl/impl/src/java/org/sakaiproject/search/component/ComponentManagerBean.java $
 * $Id: ComponentManagerBean.java 59685 2009-04-03 23:36:24Z arwhyte@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c)  2008, 2009, 2010 The Sakai Foundation
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

package org.sakaiproject.sms.logic.external;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPConstraints;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPJSSESecureSocketFactory;
import com.novell.ldap.LDAPSearchConstraints;
import com.novell.ldap.LDAPSearchResults;
import com.novell.ldap.LDAPSocketFactory;

public class LdapMobileNumberHelper extends MobileNumberHelperImpl {
	private static Log LOG = LogFactory.getLog(LdapMobileNumberHelper.class);

	private String ldapHost = ""; //address of ldap server
	private int ldapPort = 389; //port to connect to ldap server on
	private String basePath = ""; //base path to start lookups on
	private boolean secureConnection = false; //whether or not we are using SSL
	private int operationTimeout = 5000; //default timeout for operations (in ms)
	
	private UserDirectoryService userDirectoryService;
	
	private NumberRoutingHelper numberRoutingHelper;
	/* Hashmap of attribute mappings */
	private Map<String, String> attributeMappings = new HashMap<String, String>();

	private MemoryService memoryService;
	private Cache userCache;
	
	private boolean checkProviderFirst = true;

	private String ldapUser = null;

	private String ldapPassword = null;
	
	
	public void init() {
		LOG.info("init");
		if(isSecureConnection()){
			LOG.debug("Keystore is at: " + System.getenv("javax.net.ssl.trustStore"));
			LDAPSocketFactory ssf = new LDAPJSSESecureSocketFactory();
			LDAPConnection.setSocketFactory(ssf);
		}
		userCache = memoryService.newCache("org.sakaiproject.sms.logic.external.LdapMobileNumberHelper.userCache");
	}

	public List<String> getUserIdsFromMobileNumber(String mobileNumber) {
		//don't accept wildcards
		if (mobileNumber.equals("*")) 
		{
			return null;
		}

		// create new ldap connection
		LDAPConnection conn = new LDAPConnection();	
		LDAPConstraints cons = new LDAPConstraints();

		cons.setTimeLimit(operationTimeout);			
		conn.setConstraints(cons);

		// filter to find user
		String sFilter = (String)attributeMappings.get("mobileNumber") + "=" + mobileNumber;

		// string array of attribs to get from the directory
		String[] attrList = new String[] {	
				attributeMappings.get("distinguishedName"),
				attributeMappings.get("cn"),
				"objectClass",
				attributeMappings.get("mobileNumber")
		};


		// connect to ldap server
		String mobile = null;
		List<String> eidList = new ArrayList<String>();
		try {
			conn.connect( ldapHost, ldapPort );


			// get entry from directory
			List<LDAPEntry> userEntries = getEntriesFromDirectory(sFilter,attrList,conn);
			
			for (int i = 0; i < userEntries.size(); i++) {
				LDAPEntry userEntry = userEntries.get(i);
				//mobile No is"
				mobile = userEntry.getAttribute("mobileNumber").getStringValue();
				if (mobile != null && mobile.trim().length() > 0) {
					eidList.add(userEntry.getAttribute("cn").getStringValue());
				}
			}

		} catch (LDAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally {
			if (conn.isConnected()) {
				try {
					conn.disconnect();
				} catch (LDAPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		//we have a list of eids we need to convert it to internal id's
		
		return convertEidsToIds(eidList);
	}

	
	private List<String> convertEidsToIds(List<String> eidList) {
		List<String> ret = new ArrayList<String>();
		for (int i = 0; i < eidList.size(); i++) {
			String id;
			try {
				id = userDirectoryService.getUserId(eidList.get(i));
				if (id != null) {
					ret.add(id);
				}
			} catch (UserNotDefinedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return ret;
	}

	public String getUserMobileNumber(String userid) {
		String ret = null;
		if (checkProviderFirst) {
			ret = getUserMobileNumberFromLdap(userid);
			if (ret == null) {
				ret = super.getUserMobileNumber(userid);
			}
		} else {
			ret = super.getUserMobileNumber(userid);
			if (ret == null) {
				ret = getUserMobileNumberFromLdap(userid);
			}

		}



		return ret;
	}

	private String getUserMobileNumberFromLdap(String userid) {
		
		//don't accept wildcards
		if (userid.equals("*")) 
		{
			return null;
		}
		
		//is this in the cache?
		String number = (String)userCache.get(userid);
		if (number != null) {
			return number;
		}
		
		String userEid = null;
		try {
			userEid = userDirectoryService.getUserEid(userid);
		} catch (UserNotDefinedException e1) {
			return null;
		}
		
		// create new ldap connection
		LDAPConnection conn = new LDAPConnection();	
		LDAPConstraints cons = new LDAPConstraints();

		cons.setTimeLimit(operationTimeout);			
		conn.setConstraints(cons);

		// filter to find user
		String sFilter = (String)attributeMappings.get("login") + "=" + userEid	;

		// string array of attribs to get from the directory
		String[] attrList = new String[] {	
				attributeMappings.get("distinguishedName"),
				"objectClass",
				attributeMappings.get("mobileNumber")
		};


		// connect to ldap server
		String mobile = null;
		try {
			conn.connect( ldapHost, ldapPort );
			LOG.debug("going to search for: " + sFilter);

			// get entry from directory
			LDAPEntry userEntry = getEntryFromDirectory(sFilter,attrList,conn);
			if (userEntry != null) {
				//mobile No is"
				LOG.debug("got an ldap entry for " + userEid);
				LDAPAttribute attr = userEntry.getAttribute(attributeMappings.get("mobileNumber"));
				if (attr != null) {
					String ldapNumber = attr.getStringValue();

					mobile = numberRoutingHelper.normalizeNumber(ldapNumber);
					userCache.put(userid, mobile);
				}
			} else {
				LOG.debug("this user has no ldap entry: " + userEid);
				userCache.put(userid, null);
			}

		} catch (LDAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally {
			if (conn.isConnected()) {
				try {
					conn.disconnect();
				} catch (LDAPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}


		return mobile;
	}

	public Map<String, String> getUserMobileNumbers(List<String> userids) {
		Map<String, String> ret = new HashMap<String, String>();
		for (int i = 0; i < userids.size(); i++) {
			String userId = userids.get(i);
			String number = numberRoutingHelper.normalizeNumber(getUserMobileNumber(userId));
			ret.put(userId, number);
		}

		return ret;
	}

	public List<String> getUsersWithMobileNumbers(Set<String> userids) {
		LOG.debug("getUsersWithMobileNumbers");
		List<String> ret = new ArrayList<String>();
		Iterator<String> iter = userids.iterator();
		while (iter.hasNext()) {
			String userId = iter.next();
			String number = getUserMobileNumber(userId);
			if (number != null) {
				ret.add(userId);
			}
		}

		return ret;
	}


	/**
	 * @return Returns the secureConnection.
	 */
	private boolean isSecureConnection() {
		return secureConnection;
	}

	/**
	 * @param secureConnection The secureConnection to set.
	 */
	public void setSecureConnection(boolean secureConnection) {
		this.secureConnection = secureConnection;
	}

	/**
	 * @return Returns the basePath.
	 */
	public String getBasePath() {
		return basePath;
	}
	/**
	 * @param basePath The basePath to set.
	 */
	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}



	// search the directory to get an entry
	private LDAPEntry getEntryFromDirectory(String searchFilter, String[] attribs, LDAPConnection conn)
	throws LDAPException
	{
		LDAPEntry nextEntry = null;
		LDAPSearchConstraints cons = new LDAPSearchConstraints();
		cons.setDereference(LDAPSearchConstraints.DEREF_NEVER);		
		cons.setTimeLimit(operationTimeout);
		
		if (ldapUser != null && ldapPassword != null) {
			try {
				conn.bind(LDAPConnection.LDAP_V3, this.ldapUser, ldapPassword.getBytes("utf8"));
			} catch (UnsupportedEncodingException e) {
				LOG.error("failed to encode user password");
			}
		}
		
		LOG.debug("seaching for " + searchFilter + " in " + getBasePath());
		LDAPSearchResults searchResults =
			conn.search(getBasePath(),
					LDAPConnection.SCOPE_SUB,
					searchFilter,
					attribs,
					false,
					cons);

		LOG.debug("found: " + searchResults.getCount() + " results");
		if(searchResults.hasMore()){
			nextEntry = searchResults.next();            
		}

		return nextEntry;
	}
	
	
	private List<LDAPEntry> getEntriesFromDirectory(String searchFilter, String[] attribs, LDAPConnection conn)
	throws LDAPException
	{
		
		if (ldapUser != null && ldapPassword != null) {
			try {
				conn.bind(LDAPConnection.LDAP_V3, this.ldapUser, ldapPassword.getBytes("utf8"));
			} catch (UnsupportedEncodingException e) {
				LOG.error("failed to encode user password");
			}
		}
		LDAPSearchConstraints cons = new LDAPSearchConstraints();
		cons.setDereference(LDAPSearchConstraints.DEREF_NEVER);		
		cons.setTimeLimit(operationTimeout);

		LDAPSearchResults searchResults =
			conn.search(getBasePath(),
					LDAPConnection.SCOPE_SUB,
					searchFilter,
					attribs,
					false,
					cons);
		List<LDAPEntry> ret = new ArrayList<LDAPEntry>();
		while (searchResults.hasMore()) {
		
			ret.add(searchResults.next());            
		
		}

		return ret;
	}

	/**
	 * @param attributeMappings The attributeMappings to set.
	 */
	public void setAttributeMappings(Map<String, String> attributeMappings) {
		this.attributeMappings = (Map)attributeMappings;
	}

	/**
	 * @param operationTimeout The operationTimeout to set.
	 */
	public void setOperationTimeout(int operationTimeout) {
		this.operationTimeout = operationTimeout;
	}

	/**
	 * @param ldapPort The ldapPort to set.
	 */
	public void setLdapPort(int ldapPort) {
		this.ldapPort = ldapPort;
	}
	/**
	 * @param ldapHost The ldapHost to set.
	 */
	public void setLdapHost(String ldapHost) {
		this.ldapHost = ldapHost;
	}
	
	
	public void setNumberRoutingHelper(NumberRoutingHelper numberRoutingHelper) {
		this.numberRoutingHelper = numberRoutingHelper;
		super.setNumberRoutingHelper(numberRoutingHelper);
	}

	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
		super.setUserDirectoryService(userDirectoryService);
	}

	public void setMemoryService(MemoryService memoryService) {
		this.memoryService = memoryService;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setLdapUser(String ldapUser) {
		this.ldapUser = ldapUser;
	}


	/**
	 * {@inheritDoc}
	 */
	public void setLdapPassword(String ldapPassword) {
		this.ldapPassword = ldapPassword;
	}
	


}
