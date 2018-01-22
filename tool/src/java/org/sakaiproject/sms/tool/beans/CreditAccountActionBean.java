package org.sakaiproject.sms.tool.beans;

import org.sakaiproject.sms.logic.smpp.SmsBilling;

import lombok.extern.slf4j.Slf4j;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

@Slf4j
public class CreditAccountActionBean {


	private CreditAccountBean creditAccountBean;
	private SmsBilling smsBilling;
	private TargettedMessageList messages;;

	public void setMessages(TargettedMessageList messages) {
		this.messages = messages;
	}

	public void setCreditAccountBean(CreditAccountBean creditAccountBean) {
		this.creditAccountBean = creditAccountBean;
	}

	public void setSmsBilling(SmsBilling smsBilling) {
		this.smsBilling = smsBilling;
	}

	public void creditAccount() {
		try {
			smsBilling.creditAccount(creditAccountBean.getAccountId(),
					Double.valueOf(creditAccountBean.getCreditsToCredit()), creditAccountBean.getDescription());
		} catch (Exception e) {

			log.error(e.getMessage(), e);

			messages.addMessage(new TargettedMessage("GeneralActionError",
					null, TargettedMessage.SEVERITY_INFO));

		}
		messages.addMessage(new TargettedMessage("sms.credit.account.success",
				null, TargettedMessage.SEVERITY_INFO));
	}
}
