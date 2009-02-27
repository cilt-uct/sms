package org.sakaiproject.sms.logic.external.test;

import junit.framework.TestCase;

import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.smpp.impl.SmsBillingImpl;
import org.sakaiproject.sms.logic.smpp.impl.SmsCoreImpl;
import org.sakaiproject.sms.logic.stubs.ExternalLogicStub;
import org.sakaiproject.sms.model.hibernate.SmsTask;

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
			SmsTask task = smsCore.getPreliminaryTestTask();
			externalLogic.sendEmail(task, "it3lmb@nwu.ac.za", "testing..",
					"working!");
		}
	}
}
