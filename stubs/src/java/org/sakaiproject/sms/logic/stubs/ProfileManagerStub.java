package org.sakaiproject.sms.logic.stubs;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.api.app.profile.Profile;
import org.sakaiproject.api.app.profile.ProfileManager;

public class ProfileManagerStub implements ProfileManager {

	public void destroy() {
		// TODO Auto-generated method stub

	}

	public boolean displayCompleteProfile(Profile arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public List findProfiles(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getInstitutionalPhotoByUserId(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getInstitutionalPhotoByUserId(String arg0, boolean arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public Profile getProfile() {
		// TODO Auto-generated method stub
		return new ProfileStub();
	}

	public Map<String, Profile> getProfiles(Set<String> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Profile getUserProfileById(String arg0) {
		if (arg0 == null) {
			return null;
		}
		return new ProfileStub();
	}

	public void init() {
		// TODO Auto-generated method stub

	}

	public boolean isCurrentUserProfile(Profile arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isDisplayNoPhoto(Profile arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isDisplayPictureURL(Profile arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isDisplayUniversityPhoto(Profile arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isDisplayUniversityPhotoUnavailable(Profile arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isShowSearch() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isShowTool() {
		// TODO Auto-generated method stub
		return false;
	}

	public void save(Profile arg0) {
		// TODO Auto-generated method stub

	}

}
