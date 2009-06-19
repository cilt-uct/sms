package org.sakaiproject.sms.logic.external.test;

import junit.framework.TestCase;

import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.smpp.impl.SmsBillingImpl;
import org.sakaiproject.sms.logic.smpp.impl.SmsCoreImpl;
import org.sakaiproject.sms.logic.stubs.ExternalLogicStub;
import org.sakaiproject.sms.model.hibernate.SmsTask;
import org.sakaiproject.sms.model.hibernate.constants.SmsConstants;

/**
 * The Class SmsSendEmailTest. Just test if we can send email outside sakai when
 * running in jetty
 */
public class SmsSendEmailTest extends TestCase {
	private ExternalLogic externalLogic;

	public void setUp() {
		externalLogic = new ExternalLogicStub();
	}

	// will only work for our smpt gateway here, so do not test by default
	public void testSendMail() {
		boolean skipTest = true;
		if (!skipTest) {
			SmsCoreImpl smsCore = new SmsCoreImpl();
			smsCore.smsBilling = new SmsBillingImpl();
			smsCore.getHibernateLogicLocator().setExternalLogic(
					(new ExternalLogicStub()));
			SmsTask task = smsCore.getPreliminaryTestTask(
					SmsConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID,
					SmsConstants.SMS_DEV_DEFAULT_SAKAI_USER_ID);
			externalLogic.sendEmail(task, "it3lmb@nwu.ac.za", "testing..",
					"working!");
		}
	}
}
