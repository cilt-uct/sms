/***********************************************************************************
 * SmsAccountValidatorTest.java
 * Copyright (c) 2008 Sakai Project/Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.sms.tool.test;

import java.util.Date;

import junit.framework.TestCase;

import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.stubs.ExternalLogicStub;
import org.sakaiproject.sms.model.SmsAccount;
import org.sakaiproject.sms.tool.constants.SmsUiConstants;
import org.sakaiproject.sms.tool.validators.SmsAccountValidator;
import org.springframework.validation.BindException;

/**
 * The Class SmsAccountValidatorTest. Runs tests for {@link SmsAccount}
 * validation that is run by {@link SmsAccountValidator}
 */
public class SmsAccountValidatorTest extends TestCase {

	private SmsAccountValidator validator;
	private BindException errors;
	private SmsAccount account;
	private ExternalLogic externalLogic;

	private static String ACCOUNT_NAME_FIELD = "accountName";
	private static String SAKAI_SITE_ID_FIELD = "sakaiSiteId";
	private static String SAKAI_USER_ID_FIELD = "sakaiUserId";

	private static int VALID_MAX_FIELD_SIZE = 99;

	private String generateString(int length) {
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sb.append("x");
		}

		return sb.toString();
	}

	/**
	 * Run before every test
	 *
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	public void setUp() {
		externalLogic = new ExternalLogicStub();
		validator = new SmsAccountValidator();
		validator.setExternalLogic(externalLogic);
		account = new SmsAccount();
		account.setAccountEnabled(true);
		account.setAccountName("account name");
		account
				.setSakaiSiteId(externalLogic.getCurrentSiteId());
		account
				.setSakaiUserId(externalLogic.getCurrentUserId());
		account.setStartdate(new Date());
		account.setEnddate(new Date());
		account.setCredits(100L);
		account.setOverdraftLimit(10L);
		account.setMessageTypeCode(SmsUiConstants.MESSAGE_TYPE_CODE);
		errors = new BindException(account, "SmsAccount");

	}

	/**
	 * Test empty account name
	 */
	public void testAccountName_empty() {
		account.setAccountName("");
		validator.validate(account, errors);
		assertTrue(errors.hasFieldErrors(ACCOUNT_NAME_FIELD));
		assertEquals("sms.errors.accountName.empty", errors.getFieldError()
				.getCode());
	}

	/**
	 * Test accountName with large size
	 */
	public void testAccountName_invalid() {
		account.setAccountName(generateString(VALID_MAX_FIELD_SIZE + 1));
		validator.validate(account, errors);
		assertTrue(errors.hasFieldErrors(ACCOUNT_NAME_FIELD));
		assertEquals("sms.errors.accountName.tooLong", errors.getFieldError()
				.getCode());
	}

	/**
	 * Test null account name
	 */
	public void testAccountName_null() {
		account.setAccountName(null);
		validator.validate(account, errors);
		assertTrue(errors.hasFieldErrors(ACCOUNT_NAME_FIELD));
		assertEquals("sms.errors.accountName.empty", errors.getFieldError()
				.getCode());
	}

	/**
	 * Test accountName with max size
	 */
	public void testAccountName_valid() {
		account.setAccountName(generateString(VALID_MAX_FIELD_SIZE));
		validator.validate(account, errors);
		assertFalse(errors.hasFieldErrors(ACCOUNT_NAME_FIELD));
	}

	/**
	 * Test default values of test is all valid
	 */
	public void testAllValid() {
		validator.validate(account, errors);
		assertFalse(errors.hasErrors());
	}

	/**
	 * If MessageTypeCode is null, none of the validation should run We are
	 * actually dependant on this behaviour for the datepicker + cancel button
	 * to work on the Account page
	 */
	public void testMessageTypeCode_null() {
		account.setMessageTypeCode(null);
		account.setCredits(0);
		account.setAccountName(null);
		account.setOverdraftLimit(0);
		account.setSakaiSiteId(null);
		validator.validate(account, errors);
		assertFalse(errors.hasErrors());
		assertFalse(errors.hasFieldErrors());
	}

	/**
	 * Test SakaiSiteId with invalid
	 */
	public void testSakaiSiteId_invalid() {
		account.setSakaiSiteId("not valid");
		validator.validate(account, errors);
		assertTrue(errors.hasFieldErrors(SAKAI_SITE_ID_FIELD));
		assertEquals("sms.errors.sakaiSiteId.invalid", errors.getFieldError()
				.getCode());
	}

	/**
	 * Test SakaiSiteId with large size
	 */
	public void testSakaiSiteId_tooLong() {
		account.setSakaiSiteId(generateString(VALID_MAX_FIELD_SIZE + 1));
		validator.validate(account, errors);
		assertTrue(errors.hasFieldErrors(SAKAI_SITE_ID_FIELD));
		assertEquals("sms.errors.sakaiSiteId.tooLong", errors.getFieldError()
				.getCode());
	}

	/**
	 * Test null OverdraftLimit (not applicable at the moment)
	 */
	// public void testOverdraftLimit_null() {
	// account.setOverdraftLimit(null);
	// validator.validate(account, errors);
	// assertTrue(errors.hasFieldErrors(OVERDRAFT_LIMIT_FIELD));
	// assertEquals("sms.errors.overdraftLimit.invalid", errors
	// .getFieldError().getCode());
	//
	// }
	/**
	 * Test empty SakaiSiteId and SakaiUserId
	 */
	public void testSakaiSiteUserId_empty() {
		account.setSakaiSiteId("");
		account.setSakaiUserId("");
		validator.validate(account, errors);
		assertTrue(errors.hasGlobalErrors());
		assertEquals("sms.errors.site-user-id.empty", errors.getGlobalError()
				.getCode());
	}

	/**
	 * Test null SakaiSiteId + SakaiUserId
	 */
	public void testSakaiSiteUserId_null() {
		account.setSakaiSiteId(null);
		account.setSakaiUserId(null);
		validator.validate(account, errors);
		assertTrue(errors.hasGlobalErrors());
		assertEquals("sms.errors.site-user-id.empty", errors.getGlobalError()
				.getCode());

	}

	/**
	 * Sakai site id specified but user id null
	 */
	public void testSakaiSiteUserId_valid1() {
		account
				.setSakaiSiteId(externalLogic.getCurrentUserId());
		account.setSakaiUserId(null);
		validator.validate(account, errors);
		assertFalse(errors.hasGlobalErrors());
	}

	/**
	 * Sakai user id specified but site id null
	 */
	public void testSakaiSiteUserId_valid2() {
		account.setSakaiSiteId(null);
		account
				.setSakaiUserId(externalLogic.getCurrentUserId());
		validator.validate(account, errors);
		assertFalse(errors.hasGlobalErrors());
	}

	/**
	 * Sakai user id and site id specified
	 */
	public void testSakaiSiteUserId_valid3() {
		account
				.setSakaiSiteId(externalLogic.getCurrentSiteId());
		account
				.setSakaiUserId(externalLogic.getCurrentUserId());
		validator.validate(account, errors);
		assertFalse(errors.hasGlobalErrors());
	}

	/**
	 * Test SakaiSiteId with max size
	 */
	// TODO: Uncomment when specific site no longer needed
	// public void testSakaiSiteId_valid() {
	// account.setSakaiSiteId(generateString(VALID_MAX_FIELD_SIZE));
	// validator.validate(account, errors);
	// assertFalse(errors.hasFieldErrors(SAKAI_SITE_ID_FIELD));
	// }
	/**
	 * Test empty SakaiUserId
	 */
	public void testSakaiUserId_empty() {
		account.setSakaiUserId("");
		validator.validate(account, errors);
		assertFalse(errors.hasFieldErrors(SAKAI_USER_ID_FIELD));
	}

	/**
	 * Test SakaiUserId with invalid
	 */
	public void testSakaiUserId_invalid() {
		account.setSakaiUserId("not valid");
		validator.validate(account, errors);
		assertTrue(errors.hasFieldErrors(SAKAI_USER_ID_FIELD));
		assertEquals("sms.errors.sakaiUserId.invalid", errors.getFieldError()
				.getCode());
	}

	/**
	 * Test null SakaiUserId
	 */
	public void testSakaiUserId_null() {
		account.setSakaiUserId(null);
		validator.validate(account, errors);
		assertFalse(errors.hasFieldErrors(SAKAI_USER_ID_FIELD));
	}

	/**
	 * Test SakaiUserId with large size
	 */
	public void testSakaiUserId_tooLong() {
		account.setSakaiUserId(generateString(VALID_MAX_FIELD_SIZE + 1));
		validator.validate(account, errors);
		assertTrue(errors.hasFieldErrors(SAKAI_USER_ID_FIELD));
		assertEquals("sms.errors.sakaiUserId.tooLong", errors.getFieldError()
				.getCode());
	}

	public void testSakaiUserId_valid() {
		account
				.setSakaiUserId(externalLogic.getCurrentUserId());
		validator.validate(account, errors);
		assertFalse(errors.hasFieldErrors(SAKAI_USER_ID_FIELD));
	}

	/**
	 * Test SakaiUserId with max size
	 */
	// TODO: Uncomment when valid values not hardcoded
	// public void testSakaiUserId_maxsize() {
	// account.setSakaiUserId(generateString(VALID_MAX_FIELD_SIZE));
	// validator.validate(account, errors);
	// assertFalse(errors.hasFieldErrors(SAKAI_USER_ID_FIELD));
	// }
}
