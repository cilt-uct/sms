package org.sakaiproject.sms.tool.producers;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.hibernate.SmsAccountLogic;
import org.sakaiproject.sms.logic.hibernate.SmsTaskLogic;
import org.sakaiproject.sms.model.hibernate.SmsAccount;
import org.sakaiproject.sms.model.hibernate.SmsTask;
import org.sakaiproject.sms.model.hibernate.constants.SmsConst_DeliveryStatus;

import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class MainProducer implements ViewComponentProducer, DefaultView {
	
	public static final String VIEW_ID = "index";
	
	public static final String statusIconDirectory = "/library/image/silk/";
	public static final String statusIconExtension = ".gif";
	
	public String getViewID() {
		return VIEW_ID;
	}
	
	private ExternalLogic externalLogic;
	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	private SmsAccountLogic smsAccountLogic;
	public void SmsAccountLogic(SmsAccountLogic smsAccountLogic) {
		this.smsAccountLogic = smsAccountLogic;
	}
	
	private SmsTaskLogic smsTaskLogic;
	public void setSmsTaskLogic(SmsTaskLogic smsTaskLogic) {
		this.smsTaskLogic = smsTaskLogic;
	}
	
	private LocaleGetter localegetter;
	public void setLocaleGetter(LocaleGetter localegetter) {
		this.localegetter = localegetter;
	}
	
	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {
		
		//view variables
		String currentSiteId = externalLogic.getCurrentSiteId();
		String currentUserId = externalLogic.getCurrentUserId();
		SmsAccount smsAccount = smsAccountLogic.getSmsAccount(currentSiteId, currentUserId);
		List<SmsTask> smsTasks = smsTaskLogic.getAllSmsTask();
		
		//Top links
		if (externalLogic.isUserAdmin(currentUserId)){
			UIInternalLink.make(tofill, "link-admin", UIMessage.make("sms.navbar.system-config"), new SimpleViewParameters(TaskListProducer.VIEW_ID));
		}
		
		//Render console summary
		if ( smsAccount.getCredits() == null){
			UIMessage.make(tofill, "console-credits", "ui.console.credits.available", new Object[] {smsAccount.getCredits().toString()});
			UIMessage.make(tofill, "console-credits", "ui.console.value", new Object[] {smsAccount.getCredits().toString()}); //TODO: How to calculate value of credits
		}else{
			UIMessage.make(tofill, "console-credits", "ui.console.credits.none");
		}
		UIMessage.make(tofill, "console-help", "ui.console.help");
		UIOutput.make(tofill, "console-email"); //TODO show email for credit purchases
		
		if ( smsTasks.size() > 0 ){
			
			// fix for broken en_ZA locale in JRE http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6488119
			Locale M_locale = null;
			String langLoc[] = localegetter.get().toString().split("_");
			if ( langLoc.length >= 2 ) {
				if ("en".equals(langLoc[0]) && "ZA".equals(langLoc[1]))
					M_locale = new Locale("en", "GB");
				else
					M_locale = new Locale(langLoc[0], langLoc[1]);
			} else{
				M_locale = new Locale(langLoc[0]);
			}

			DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
					DateFormat.SHORT, M_locale);
			TimeZone tz = externalLogic.getLocalTimeZone();
			df.setTimeZone(tz);
			
			UIOutput.make(tofill, "tasks-table");
			fillTableHeaders( tofill, new String[] {"message", "status", "sender", "time", "recipients", "cost"});

			//show table rows
			for (SmsTask sms : smsTasks){
				UIBranchContainer row = UIBranchContainer.make(tofill, "task-row:");
				UIInternalLink.make(row, "task-message", sms.getMessageBody(), new SimpleViewParameters(MainProducer.VIEW_ID)); //TODO Link to sms-specific report producer
				Map<String, String> statusLibrary = getStatusLibrary();
				String status = statusLibrary.get(sms.getStatusCode());
				UILink statusIcon = UILink.make(row, "task-status", statusIconDirectory + status + statusIconExtension);
				statusIcon.decorate(new UIFreeAttributeDecorator("alt", status)); //TODO generate user friendly status messages and show them in the alt attribute
				UIOutput.make(row, "task-sender", sms.getSenderUserName());
				UIOutput.make(row, "task-time", df.format(sms.getDateToSend()));
				UIMessage.make(row, "task-recipients", "ui.task.recipents", new Object[] {sms.getMessagesDelivered(), sms.getGroupSizeActual()}); //TODO Verify that these sms variables give what's expected
				UIOutput.make(row, "task-cost", sms.getCreditCost() + "");				
			}
		}else
		{
			UIMessage.make(tofill, "tasks-none", "ui.error.notasks");
		}		
	}
	private void fillTableHeaders(UIContainer tofill, String[] headers) {
		// Render table headers
		for (int i=0; i < headers.length; i++){
			String header = headers[i];
			UILink.make(tofill, "tasks-" + header, UIMessage.make("ui.tasks.headers." + header), "#")
				.decorate(new UITooltipDecorator("ui.tasks.headers." + header + ".tooltip"));
		}
	}
	
	//populate status icon library
	public Map<String, String> getStatusLibrary(){
		Map<String, String> lib = new HashMap<String, String>();
		lib.put(SmsConst_DeliveryStatus.STATUS_ABORT, "");
		lib.put(SmsConst_DeliveryStatus.STATUS_BUSY, "");
		lib.put(SmsConst_DeliveryStatus.STATUS_DELIVERED, "");
		lib.put(SmsConst_DeliveryStatus.STATUS_ERROR, "");
		lib.put(SmsConst_DeliveryStatus.STATUS_EXPIRE, "");
		lib.put(SmsConst_DeliveryStatus.STATUS_FAIL, "");
		lib.put(SmsConst_DeliveryStatus.STATUS_INCOMPLETE, "");
		lib.put(SmsConst_DeliveryStatus.STATUS_LATE, "");
		lib.put(SmsConst_DeliveryStatus.STATUS_PENDING, "");
		lib.put(SmsConst_DeliveryStatus.STATUS_RETRY, "");
		lib.put(SmsConst_DeliveryStatus.STATUS_SENT, "");
		lib.put(SmsConst_DeliveryStatus.STATUS_TASK_COMPLETED, "");
		lib.put(SmsConst_DeliveryStatus.STATUS_TIMEOUT, "");	
		return lib;	
	}
}

