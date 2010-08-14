package org.sakaiproject.sms.logic.stubs;

import java.util.Date;
import java.util.Stack;

import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserEdit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class UserStubb implements UserEdit {

	private String id;

	public void restrictEditEmail() {
		// TODO Auto-generated method stub

	}

	public void restrictEditFirstName() {
		// TODO Auto-generated method stub

	}

	public void restrictEditLastName() {
		// TODO Auto-generated method stub

	}

	public void restrictEditPassword() {
		// TODO Auto-generated method stub

	}

	public void restrictEditType() {
		// TODO Auto-generated method stub

	}

	public void setEid(String arg0) {
		this.id = arg0;

	}

	public void setEmail(String arg0) {
		// TODO Auto-generated method stub

	}

	public void setFirstName(String arg0) {
		// TODO Auto-generated method stub

	}

	public void setId(String arg0) {
		this.id = arg0;

	}

	public void setLastName(String arg0) {
		// TODO Auto-generated method stub

	}

	public void setPassword(String arg0) {
		// TODO Auto-generated method stub

	}

	public void setType(String arg0) {
		// TODO Auto-generated method stub

	}

	public boolean checkPassword(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public User getCreatedBy() {
		// TODO Auto-generated method stub
		return null;
	}

	public Time getCreatedTime() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Date getCreatedDate() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDisplayId() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDisplayName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getEid() {
		// TODO Auto-generated method stub
		return id;
	}

	public String getEmail() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getFirstName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLastName() {
		// TODO Auto-generated method stub
		return null;
	}

	public User getModifiedBy() {
		// TODO Auto-generated method stub
		return null;
	}

	public Time getModifiedTime() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public Date getModifiedDate() {
		// TODO Auto-generated method stub
		return null;
	}


	public String getSortName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getId() {
		// TODO Auto-generated method stub
		return id;
	}

	public ResourceProperties getProperties() {
		ResourceProperties rp = new ResourcePropertiesStub();
		return rp;
	}

	public String getReference() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getReference(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUrl(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@SuppressWarnings("unchecked")
	public Element toXml(Document arg0, Stack arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	public ResourcePropertiesEdit getPropertiesEdit() {
		return null;
	}

	public boolean isActiveEdit() {
		// TODO Auto-generated method stub
		return false;
	}

	public void restrictEditEid() {
		// TODO Auto-generated method stub
		
	}

}
