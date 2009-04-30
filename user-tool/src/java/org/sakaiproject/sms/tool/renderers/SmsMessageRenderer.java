package org.sakaiproject.sms.tool.renderers;

import org.sakaiproject.sms.model.hibernate.SmsTask;
import org.sakaiproject.sms.tool.producers.MainProducer;
import org.sakaiproject.sms.tool.producers.SendSMSProducer;
import org.sakaiproject.sms.tool.util.DateUtil;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class SmsMessageRenderer {
	
	private DateUtil dateUtil;
	public void setDateUtil(DateUtil dateUtil) {
		this.dateUtil = dateUtil;
	}
	
	public void renderMessage(SmsTask smsTask, UIContainer tofill, String parentDiv){
		UIJointContainer message = new UIJointContainer(tofill, parentDiv, "renderMessage:");
		UIMessage.make(message, "message-title", "ui.sent.sms.title");
		UIOutput.make(message, "message", smsTask.getMessageBody())
		//keep the id somewhere that the JS can grab and use it for processing actions edit or delete
				.decorate(new UIFreeAttributeDecorator("rel", smsTask.getId().toString()));
		UIMessage.make(message, "sms-id", "ui.sent.sms.id", new Object[] { smsTask.getId() });
		UIMessage.make(message, "sms-created", "ui.sent.sms.created", new Object[] { dateUtil.formatDate(smsTask.getDateCreated()) });
	}
}
