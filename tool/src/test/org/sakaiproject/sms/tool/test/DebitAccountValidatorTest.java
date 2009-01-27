package org.sakaiproject.sms.tool.test;

import junit.framework.TestCase;

import org.sakaiproject.sms.beans.DebitAccountBean;
import org.sakaiproject.sms.tool.test.stubs.SmsAccountLogicStub;
import org.sakaiproject.sms.validators.DebitAccountValidator;
import org.springframework.validation.BindException;

public class DebitAccountValidatorTest extends TestCase{

	private DebitAccountValidator validator;
	private BindException bindException;
	private DebitAccountBean debitAccountBean;

	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		validator = new DebitAccountValidator();
		debitAccountBean = new DebitAccountBean();
		bindException = new BindException(debitAccountBean, "SmsConfig");
		
		validator.setSmsAccountLogic(new SmsAccountLogicStub());
	}

	public void testAccountDoesNotExsist() throws Exception {
		debitAccountBean.setAccountId(-10L);
		debitAccountBean.setAmountToDebit(10f);
		validator.validate(debitAccountBean, bindException);
		assertTrue(bindException.hasErrors());
		assertEquals("sms.debit.account.errors.no.account", bindException.getGlobalError().getCode());
		
	}
	
	public void testNegativeDebitAmount() throws Exception {
		debitAccountBean.setAccountId(1L);
		debitAccountBean.setAmountToDebit(-10f);
		validator.validate(debitAccountBean, bindException);
		assertTrue(bindException.hasErrors());
		assertEquals("sms.errors.amountToDebit.empty", bindException.getFieldError().getCode());
	}
	
	public void testValidationPasses() throws Exception {
		debitAccountBean.setAccountId(1L);
		debitAccountBean.setAmountToDebit(20f);
		validator.validate(debitAccountBean, bindException);
		assertFalse(bindException.hasErrors());
	}
}