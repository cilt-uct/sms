/***********************************************************************************
 * SMSConfigValidatorTest.java
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

import junit.framework.TestCase;

import org.sakaiproject.sms.model.SmsConfig;
import org.sakaiproject.sms.tool.validators.SmsConfigValidator;
import org.springframework.validation.BindException;

public class SmsConfigValidatorTest extends TestCase {

	private static final String INVALID_EMAIL = "jo@@gmail.com";
	private static final String VALID_EMAIL = "jo@gmail.com";
	private SmsConfigValidator validator;
	private BindException bindException;
	private SmsConfig smsConfig;

	@Override
	protected void setUp() throws Exception {
		validator = new SmsConfigValidator();
		smsConfig = new SmsConfig();
		smsConfig.setDelReportTimeoutDuration(Integer.valueOf(1));
		smsConfig.setPagingSize(Integer.valueOf(20));
		smsConfig.setSmsRetryMaxCount(Integer.valueOf(3));
		smsConfig.setSmsTaskMaxLifeTime(Integer.valueOf(3));
		smsConfig.setSmsRetryScheduleInterval(Integer.valueOf(3));
		bindException = new BindException(smsConfig, "SmsConfig");

		super.setUp();
	}

	public void testCorrectEmail() throws Exception {
		smsConfig.setNotificationEmail(VALID_EMAIL);
		smsConfig.setSendSmsEnabled(true);
		validator.validate(smsConfig, bindException);
		assertFalse(bindException.hasErrors());
	}

	public void testCorrectEmails() throws Exception {
		smsConfig.setNotificationEmail(VALID_EMAIL);
		smsConfig.setSendSmsEnabled(true);
		validator.validate(smsConfig, bindException);
		assertFalse(bindException.hasErrors());
	}

	public void testInvalidEmails() throws Exception {
		smsConfig.setNotificationEmail(INVALID_EMAIL);
		smsConfig.setSendSmsEnabled(true);
		validator.validate(smsConfig, bindException);
		assertTrue(bindException.hasErrors());
	}

	public void testNotificationDisabled() throws Exception {
		smsConfig.setNotificationEmail(VALID_EMAIL);
		smsConfig.setSendSmsEnabled(false);
		validator.validate(smsConfig, bindException);
		assertFalse(bindException.hasErrors());
	}

	public void testPagingSizeNotSet() throws Exception {
		smsConfig.setSendSmsEnabled(false);
		smsConfig.setPagingSize(null);
		validator.validate(smsConfig, bindException);
		assertTrue(bindException.hasErrors());
	}

}