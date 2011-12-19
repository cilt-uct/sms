package org.sakaiproject.sms.logic.stubs;

import java.util.Collection;
import java.util.List;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.user.api.User;

public class SecurityServiceStubb implements SecurityService {

	public void clearAdvisors() {
		// TODO Auto-generated method stub

	}

	public void clearUserEffectiveRole(String arg0) {
		// TODO Auto-generated method stub

	}

	public void clearUserEffectiveRoles() {
		// TODO Auto-generated method stub

	}

	public String getUserEffectiveRole(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasAdvisors() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isSuperUser() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isSuperUser(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public SecurityAdvisor popAdvisor() {
		// TODO Auto-generated method stub
		return null;
	}

	public void pushAdvisor(SecurityAdvisor arg0) {
		// TODO Auto-generated method stub

	}

	public boolean setUserEffectiveRole(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean unlock(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean unlock(User arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean unlock(String arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	

	public List<User> unlockUsers(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public SecurityAdvisor popAdvisor(SecurityAdvisor arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean unlock(String arg0, String arg1, String arg2,
			Collection<String> arg3) {
		// TODO Auto-generated method stub
		return false;
	}

}
