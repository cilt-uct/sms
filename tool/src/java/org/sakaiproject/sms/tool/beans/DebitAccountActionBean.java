package org.sakaiproject.sms.tool.beans;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.api.SmsBilling;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

public class DebitAccountActionBean {

	private static Log LOG = LogFactory.getLog(DebitAccountActionBean.class);
	
	private DebitAccountBean debitAccountBean;
	private SmsBilling smsBilling;
	private TargettedMessageList messages;;
	
		
	public void setMessages(TargettedMessageList messages) {
		this.messages = messages;
	}

	public void setDebitAccountBean(DebitAccountBean debitAccountBean) {
		this.debitAccountBean = debitAccountBean;
	}
	
	public void setSmsBilling(SmsBilling smsBilling) {
		this.smsBilling = smsBilling;
	}

	public void debitAccount(){
		try{			
			smsBilling.debitAccount(debitAccountBean.getAccountId(), debitAccountBean.getAmountToDebit());
		}
		catch (Exception e) {
			
			LOG.error(e);
			
			messages.addMessage(new TargettedMessage(
					"GeneralActionError", null,
					TargettedMessage.SEVERITY_INFO));
			
		}
		messages.addMessage(new TargettedMessage(
				"sms.debit.account.success", null,
				TargettedMessage.SEVERITY_INFO));
	}
}
