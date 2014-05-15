package org.sakaiproject.sms.logic.stubs;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Stack;

import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.time.api.Time;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;

public class ResourcePropertiesStub implements ResourceProperties {

	private static final long serialVersionUID = 1L;

	public void addAll(ResourceProperties arg0) {
		// TODO Auto-generated method stub

	}

	public void addAll(Properties arg0) {
		// TODO Auto-generated method stub

	}

	public void addProperty(String arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	public void addPropertyToList(String arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	public void clear() {
		// TODO Auto-generated method stub

	}

	public Object get(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean getBooleanProperty(String arg0)
			throws EntityPropertyNotDefinedException,
			EntityPropertyTypeException {
		// TODO Auto-generated method stub
		return true;
	}

	public ContentHandler getContentHander() {
		// TODO Auto-generated method stub
		return null;
	}

	public long getLongProperty(String arg0)
			throws EntityPropertyNotDefinedException,
			EntityPropertyTypeException {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getNamePropAssignmentDeleted() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropCalendarLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropCalendarType() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropChatRoom() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropCollectionBodyQuota() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropContentLength() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropContentType() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropCopyright() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropCopyrightAlert() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropCopyrightChoice() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropCreationDate() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropCreator() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropDisplayName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropIsCollection() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropModifiedBy() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropModifiedDate() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropNewAssignmentCheckAddDueDate() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropNewAssignmentCheckAutoAnnounce() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropReplyStyle() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropStructObjType() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropSubmissionPreviousFeedbackComment() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropSubmissionPreviousFeedbackText() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropSubmissionPreviousGrades() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropSubmissionScaledPreviousGrades() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamePropTo() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProperty(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPropertyFormatted(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	public List getPropertyList(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	public Iterator getPropertyNames() {
		// TODO Auto-generated method stub
		return null;
	}

	public Time getTimeProperty(String arg0)
			throws EntityPropertyNotDefinedException,
			EntityPropertyTypeException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getTypeUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isLiveProperty(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeProperty(String arg0) {
		// TODO Auto-generated method stub

	}

	public void set(ResourceProperties arg0) {
		// TODO Auto-generated method stub

	}


	public Date getDateProperty(String arg0)
			throws EntityPropertyNotDefinedException,
			EntityPropertyTypeException {
		// TODO Auto-generated method stub
		return null;
	}

	public Element toXml(Document arg0, Stack<Element> arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
