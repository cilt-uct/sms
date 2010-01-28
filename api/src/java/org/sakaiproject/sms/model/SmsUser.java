package org.sakaiproject.sms.model;
/**
 * SMS user is a Lightwieght Pojo that wraps information about 
 * user on the system
 * @author dhorwitz
 *
 */
public class SmsUser {

	private String userId;
	private String email;
	private String firstName;
	private String Surname;
	private String displayName;
	private String eid;


	public SmsUser(String userId, String email, String firstName,
			String surname, String displayName, String eid) {
		super();
		this.userId = userId;
		this.email = email;
		this.firstName = firstName;
		Surname = surname;
		this.displayName = displayName;
		this.eid = eid;
	}
	
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getSurname() {
		return Surname;
	}
	public void setSurname(String surname) {
		Surname = surname;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	
	public String getEid() {
		return eid;
	}


	public void setEid(String eid) {
		this.eid = eid;
	}

}
