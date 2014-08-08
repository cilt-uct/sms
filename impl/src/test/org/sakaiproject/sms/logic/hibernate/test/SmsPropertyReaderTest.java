package org.sakaiproject.sms.logic.hibernate.test;

import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sakaiproject.sms.model.constants.SmsConstants;
import org.sakaiproject.sms.util.AbstractBaseTestCase;
import org.sakaiproject.sms.util.SmsPropertyReader;

/**
 * The Class SmsAccountTest. Do some basic crud functions on the account table.
 */
public class SmsPropertyReaderTest extends AbstractBaseTestCase {

	@BeforeClass
	public static void beforeClass(){
		if (!SmsConstants.isDbSchemaCreated) {
			smsDao.createSchema();
			SmsConstants.isDbSchemaCreated = true;
		}
	}
	/** The TES t_ propert y_ name. */
	private final static String TEST_PROPERTY_NAME = "unitTestProperty";

	/** The TES t_ propert y_ value. */
	private final static String TEST_PROPERTY_VALUE = "value";

	/**
	 * Test get message_ found.
	 */
    @Test
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
    @Test
	public void testGetMessage_NotFound() {
		String propertyValue = SmsPropertyReader.getProperty("non-existent");
		assertFalse("Property file not found", propertyValue
				.equals(SmsConstants.PROPERTY_FILE_NOT_FOUND));
		assertTrue("Property not found", propertyValue
				.equals(SmsConstants.PROPERTY_NOT_FOUND));
	}

}
