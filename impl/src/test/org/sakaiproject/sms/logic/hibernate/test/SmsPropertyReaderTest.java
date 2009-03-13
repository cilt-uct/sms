package org.sakaiproject.sms.logic.hibernate.test;

import org.sakaiproject.sms.dao.StandaloneSmsDaoImpl;
import org.sakaiproject.sms.model.hibernate.constants.SmsConstants;
import org.sakaiproject.sms.util.AbstractBaseTestCase;
import org.sakaiproject.sms.util.SmsPropertyReader;

/**
 * The Class SmsAccountTest. Do some basic crud functions on the account table.
 */
public class SmsPropertyReaderTest extends AbstractBaseTestCase {

	/** The TES t_ propert y_ name. */
	private final String TEST_PROPERTY_NAME = "unitTestProperty";

	/** The TES t_ propert y_ value. */
	private final String TEST_PROPERTY_VALUE = "value";

	/**
	 * Instantiates a new sms account test.
	 */
	public SmsPropertyReaderTest() {
	}

	/**
	 * Instantiates a new sms account test.
	 *
	 * @param name
	 *            the name
	 */
	public SmsPropertyReaderTest(String name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.sms.util.AbstractBaseTestCase#testOnetimeSetup()
	 */
	@Override
	public void testOnetimeSetup() {
		StandaloneSmsDaoImpl.createSchema();
	}

	/**
	 * Test get message_ found.
	 */
	public void testGetMessage_Found() {
		String propertyValue = SmsPropertyReader
				.getProperty(TEST_PROPERTY_NAME);
		assertFalse("Property file not found", propertyValue
				.equals(SmsConstants.PROPERTY_FILE_NOT_FOUND));
		assertFalse("Property not found", propertyValue
				.equals(SmsConstants.PROPERTY_NOT_FOUND));
		assertTrue(propertyValue.equals(TEST_PROPERTY_VALUE));
	}

	/**
	 * Test get message_ not found.
	 */
	public void testGetMessage_NotFound() {
		String propertyValue = SmsPropertyReader.getProperty("non-existent");
		assertFalse("Property file not found", propertyValue
				.equals(SmsConstants.PROPERTY_FILE_NOT_FOUND));
		assertTrue("Property not found", propertyValue
				.equals(SmsConstants.PROPERTY_NOT_FOUND));
	}

}
