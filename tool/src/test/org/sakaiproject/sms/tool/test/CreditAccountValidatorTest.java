package org.sakaiproject.sms.tool.test;

import junit.framework.TestCase;

import org.sakaiproject.sms.tool.beans.CreditAccountBean;
import org.sakaiproject.sms.tool.test.stubs.SmsAccountLogicStub;
import org.sakaiproject.sms.tool.validators.CreditAccountValidator;
import org.springframework.validation.BindException;

public class CreditAccountValidatorTest extends TestCase {

	private CreditAccountValidator validator;
	private BindException bindException;
	private CreditAccountBean creditAccountBean;

	@Override
	protected void setUp() {
		validator = new CreditAccountValidator();
		creditAccountBean = new CreditAccountBean();
		bindException = new BindException(creditAccountBean, "SmsConfig");

		validator.setSmsAccountLogic(new SmsAccountLogicStub());
	}

	public void testAccountDoesNotExsist() {
		creditAccountBean.setAccountId(-10L);
		creditAccountBean.setCreditsToCredit(10D);
		validator.validate(creditAccountBean, bindException);
		assertTrue(bindException.hasErrors());
		assertEquals("sms.credit.account.errors.no.account", bindException
				.getGlobalError().getCode());

	}

	public void testValidationPasses() {
		creditAccountBean.setAccountId(1L);
		creditAccountBean.setCreditsToCredit(20D);
		validator.validate(creditAccountBean, bindException);
		assertFalse(bindException.hasErrors());
	}
}