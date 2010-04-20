package org.sakaiproject.sms.tool.producers;

import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.logic.HibernateLogicLocator;
import org.sakaiproject.sms.logic.SmsMessageLogic;
import org.sakaiproject.sms.logic.SmsTaskLogic;
import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.model.SmsConfig;
import org.sakaiproject.sms.model.SmsMessage;
import org.sakaiproject.sms.model.SmsTask;
import org.sakaiproject.sms.model.constants.SmsConst_DeliveryStatus;
import org.sakaiproject.sms.tool.params.SmsParams;
import org.sakaiproject.sms.tool.renderers.SavedSelectionsRenderer;
import org.sakaiproject.sms.tool.renderers.SmsMessageRenderer;
import org.sakaiproject.sms.tool.util.CurrencyUtil;
import org.sakaiproject.sms.tool.util.DateUtil;
import org.sakaiproject.sms.tool.util.StatusUtils;
import org.sakaiproject.user.api.User;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIAlternativeTextDecorator;
import uk.org.ponder.rsf.components.decorators.UIIDStrategyDecorator;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.request.EarlyRequestParser;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

public class SmsDetailProducer implements ViewComponentProducer, ViewParamsReporter {
	
public static final String VIEW_ID = "sms";
	
	public static Log log = LogFactory.getLog(SmsDetailProducer.class);
		
	public String getViewID() {
		return VIEW_ID;
	}

	private SmsMessageLogic smsMessageLogic = null;
	public void setSmsMessageLogic(SmsMessageLogic smsMessageLogic) {
		this.smsMessageLogic = smsMessageLogic;
	}

	private ExternalLogic externalLogic = null;
	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	private SmsTaskLogic smsTaskLogic = null;
	public void setSmsTaskLogic(SmsTaskLogic smsTaskLogic) {
		this.smsTaskLogic = smsTaskLogic;
	}

	private DateUtil dateUtil = null;
	public void setDateUtil(DateUtil dateUtil) {
		this.dateUtil = dateUtil;
	}

	private StatusUtils statusUtils = null;
	public void setStatusUtils(StatusUtils statusUtils) {
		this.statusUtils = statusUtils;
	}
	
	private CurrencyUtil currencyUtil = null;
	public void setCurrencyUtil(CurrencyUtil currencyUtil) {
		this.currencyUtil = currencyUtil;
	}

	private SmsMessageRenderer smsMessageRenderer = null;
	public void setSmsMessageRenderer(SmsMessageRenderer smsMessageRenderer) {
		this.smsMessageRenderer = smsMessageRenderer;
	}
	
	private HibernateLogicLocator hibernateLogicLocator = null;
	public void setHibernateLogicLocator(
			HibernateLogicLocator hibernateLogicLocator) {
		this.hibernateLogicLocator = hibernateLogicLocator;
	}
	
	private SavedSelectionsRenderer savedSelectionsRenderer = null;
	public void setSavedSelectionsRenderer(
			SavedSelectionsRenderer savedSelectionsRenderer) {
		this.savedSelectionsRenderer = savedSelectionsRenderer;
	}
	

	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {
		
		// get number format for default locale
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(1);
		
		if (viewparams != null) {
			SmsParams statusParams = (SmsParams) viewparams;
			if (statusParams != null && statusParams.id != null) {

				Long smsId = 0l;
				try{
					smsId = Long.parseLong(statusParams.id);
				}catch (NumberFormatException exception){
					//TODO
					throw new IllegalArgumentException("Supplied task parameter is invalid: " + statusParams.id);
				}
				SmsTask smsTask = smsTaskLogic.getSmsTask(smsId);
				if(smsTask == null){
					//TODO: show message
					log.warn("SMS not found with id: " + statusParams.id);
				} else {

					String currentUserId = externalLogic.getCurrentUserId();
					String currentSiteId = externalLogic.getCurrentSiteId();

					// Check this tool
					
					if (!"sakai.sms.user".equals(smsTask.getSakaiToolId())) {
						log.warn("SMS requested from another tool: " + statusParams.id);
						return;
					}
					
					// Check this site
					if (!currentSiteId.equals(smsTask.getSakaiSiteId())) {
						log.warn("SMS requested from another site: " + statusParams.id);
						return;
					}
											
					//Get status of the task to determine which UI to render
					String statusCode = smsTask.getStatusCode();
					String statusToShow = statusUtils.getStatusUIKey(statusCode);
					String statusFullName = statusUtils.getStatusFullName(statusCode);
					log.debug("Task " + smsId + " is: " + statusFullName);
					//Show Task message
					smsMessageRenderer.renderMessage(smsTask, tofill, "sms-message:");
					
					if(StatusUtils.key_phone.equals(statusToShow) || StatusUtils.key_time.equals(statusToShow)){
						
					//***Start Inprogress/Scheduled Task rendering
						UIBranchContainer wrapper = UIBranchContainer.make(tofill, "active:");
						UIBranchContainer status = UIBranchContainer.make(wrapper, "status:");
						
						UILink.make(status, "sms-status", statusUtils.getTaskStatusIcon(statusCode))
							.decorate(new UIAlternativeTextDecorator(statusFullName));
						SmsConfig siteConfig = hibernateLogicLocator.getSmsConfigLogic()
						.getOrCreateSystemSmsConfig();
						
						UIOutput statusText = UIOutput.make(status, "sms-status-title", statusFullName);
						
						UIMessage.make(wrapper, "sms-status-retries", "ui.inprogress.retries", new Object[] { smsTask.getAttemptCount(), siteConfig.getSmsRetryMaxCount() } );
						
						//Insert original user selections
						savedSelectionsRenderer.renderSelections(smsTask, wrapper, "savedSelections:");
						
						UIMessage.make(wrapper, "cost", "ui.inprogress.sms.cost.title");
						UIOutput.make(wrapper, "cost-credits", nf.format(smsTask.getCreditEstimate()));
						
						UIOutput.make(wrapper, "cost-cost", currencyUtil.toServerLocale(smsTask.getCostEstimate()) );
						
						UIForm form = UIForm.make(wrapper, "form", new SmsParams(SendSMSProducer.VIEW_ID, smsId.toString(), StatusUtils.key_time.equals(statusToShow)? StatusUtils.statusType_EDIT : StatusUtils.statusType_REUSE));
						form.type = EarlyRequestParser.RENDER_REQUEST;
						//keep the id somewhere that the JS can grab and use it for processing actions edit or delete
						UIInput.make(wrapper, "smsId", null, smsTask.getId() + "")
						.decorate(new UIIDStrategyDecorator("smsId"));
						UIInput.make(wrapper, "statusToShow", null, statusToShow)
						.decorate(new UIIDStrategyDecorator("statusToShow"));
						
						/**
						 * The action buttons are handled by JS. RSF is only needed for i18N
						 */
						if ( StatusUtils.key_phone.equals(statusToShow)){
							statusText.decorate(new UIStyleDecorator("smsGreenish"));
							UIMessage.make(wrapper, "sms-started", "ui.inprogress.sms.started", new Object[] { dateUtil.formatDate(smsTask.getDateToSend()) });
							UIMessage.make(wrapper, "delivered", "ui.inprogress.sms.delivered", new Object[] { smsTask.getMessagesDelivered(), (smsTask.getGroupSizeActual() == null || smsTask.getGroupSizeActual() == 0) ? smsTask.getGroupSizeEstimate() : smsTask.getGroupSizeActual() });
						}else if( StatusUtils.key_time.equals(statusToShow)){
							statusText.decorate(new UIStyleDecorator("smsOrange"));
							UIMessage.make(wrapper, "sms-started", "ui.scheduled.sms.started", new Object[] { dateUtil.formatDate(smsTask.getDateToSend()) });
							//Check permissions before rendering control buttons
							if ( externalLogic.isUserAllowedInLocation(currentUserId, ExternalLogic.SMS_SEND, currentSiteId ) ) {
								UICommand.make(form, "edit", UIMessage.make("sms.general.edit.sms"))
								.decorate(new UIIDStrategyDecorator("smsEdit"));
								UICommand.make(form, "delete", UIMessage.make("sms.general.delete"));
					        	UIMessage.make(wrapper, "actionDelete", "ui.action.confirm.sms.delete", new String[] { smsTask.getMessageBody() });
							}
						}else{
							throw new IllegalArgumentException("Cannot act on this status type: " + statusToShow);
						}
						UIMessage.make(wrapper, "sms-finish", "ui.inprogress.sms.finish", new Object[] { dateUtil.formatDate(smsTask.getDateToExpire()) });
						
						UICommand.make(form, "back", UIMessage.make("sms.general.back"));
					//***End Inprogress/Scheduled Task rendering
						
					}else if(StatusUtils.key_cross.equals(statusToShow)){
						
					//***Start Failed Task Rendering
						UIBranchContainer wrapper = UIBranchContainer.make(tofill, "failed:");
						UIBranchContainer statusFailed = UIBranchContainer.make(wrapper, "status:");
						UILink.make(statusFailed, "sms-status", statusUtils.getTaskStatusIcon(statusCode))
							.decorate(new UIAlternativeTextDecorator(statusFullName));
						UIOutput.make(statusFailed, "sms-status-title", statusFullName);
						
						UIMessage.make(wrapper, "sms-sent", "ui.failed.sms.expired", new Object[] { dateUtil.formatDate(smsTask.getDateProcessed()) });
						
						if ( smsTask.getFailReason() != null && ! "".equals(smsTask.getFailReason()) ){
							UIMessage.make(wrapper, "reason", "ui.failed.sms.reason", 
								new Object[] { smsTask.getFailReason() });
						}
						
						UIMessage.make(wrapper, "recipients", "ui.failed.sms.recipients", new Object[]{ smsTask.getMessagesDelivered() });
		
						UIForm formFailed = UIForm.make(wrapper, "editForm", new SmsParams(SendSMSProducer.VIEW_ID, smsId.toString(), StatusUtils.statusType_REUSE));
						formFailed.type = EarlyRequestParser.RENDER_REQUEST;
						
						//keep the id somewhere that the JS can grab and use it for processing actions edit or delete
						UIInput.make(formFailed, "smsId", null, smsId + "")
							.decorate(new UIIDStrategyDecorator("smsId"));
						/**
						 * These 3 action buttons are handled by JS. RSF is only needed for i18N
						 */
						//Check permissions before rendering control buttons
						if ( externalLogic.isUserAllowedInLocation(currentUserId, ExternalLogic.SMS_SEND, currentSiteId ) ) {
					        UICommand.make(formFailed, "edit", UIMessage.make("sms.general.editandsend"))
								.decorate(new UIIDStrategyDecorator("smsEdit"));
					        UICommand.make(formFailed, "delete", UIMessage.make("sms.general.delete"))
								.decorate(new UIIDStrategyDecorator("smsDelete"));
						}
						UICommand.make(formFailed, "back", UIMessage.make("sms.general.back"));
						
						UIMessage.make(wrapper, "actionDelete", "ui.action.confirm.sms.delete", new String[] { smsTask.getMessageBody() });
					//***End Failed Task rendering
						
					}else if(StatusUtils.key_tick.equals(statusToShow)){
						
					//***Start Sent Task rendering
						UIBranchContainer wrapper = UIBranchContainer.make(tofill, "sent:");
						UIBranchContainer statusSent = UIBranchContainer.make(wrapper,
								"status:");
						UILink.make(statusSent, "sms-status",
								statusUtils.getTaskStatusIcon(statusCode)).decorate(
								new UIAlternativeTextDecorator(statusFullName));
						UIOutput.make(statusSent, "sms-status-title", statusFullName);
				
						UIMessage.make(wrapper, "sms-sent", "ui.sent.sms.completed",
								new Object[] { dateUtil.formatDate(smsTask
										.getDateProcessed()) });
						UIMessage
								.make(
										wrapper,
										"recipients",
										"ui.sent.sms.recipients",
										new Object[] {
												smsTask.getMessagesDelivered(),
												(smsTask.getGroupSizeActual() == null || smsTask
														.getGroupSizeActual() == 0) ? smsTask
														.getGroupSizeEstimate()
														: smsTask.getGroupSizeActual() });
				
						UIMessage.make(wrapper, "recipient-header",
								"ui.sent.sms.header.recipients");
						UIMessage.make(wrapper, "username-header",
								"ui.sent.sms.header.username");
						UIMessage.make(wrapper, "status-header",
								"ui.sent.sms.header.status");
						
						List<SmsMessage> smsMessages = smsMessageLogic.getSmsMessagesForTask(smsTask.getId());
						
						Set<String> smsUserIds = externalLogic.getUserIdsFromSmsMessages(smsMessages);
						Map<String, User> userMap = externalLogic.getSakaiUsers(smsUserIds);
						
						for (SmsMessage sms : smsMessages) {
							UIBranchContainer row = UIBranchContainer.make(wrapper,
									"sms-row:");
							String smsUserId = sms.getSakaiUserId();
							
							if (smsUserId == null || "".equals(smsUserId)) {
								UIOutput.make(row, "sms-recipient", sms.getMobileNumber() == null? "----" : sms.getMobileNumber());
								UIOutput.make(row, "sms-recipient-username", "----");
							} else {
								String smsUserName = "----";
								String smsUserSortName = "----";
								//populate names from userMap value
								User user = userMap.get(smsUserId); 
								if( user != null ){
									smsUserName = user.getDisplayId();
									smsUserSortName = user.getSortName();
								}

								UIOutput.make(row, "sms-recipient", smsUserSortName);
								UIOutput.make(row, "sms-recipient-username", smsUserName);
							}
							String messageStatusCode = sms.getStatusCode();
							UILink statusIcon = UILink.make(row, "sms-recipient-status", statusUtils.getMessageStatusIcon(messageStatusCode));
							//show alt and tooltips for status detail on status icon
							DecoratorList iconDecorators = new DecoratorList();
							String messageFullStatus = statusUtils.getStatusFullName(messageStatusCode);
							iconDecorators.add(new UIAlternativeTextDecorator(messageFullStatus));
							if( SmsConst_DeliveryStatus.STATUS_DELIVERED.equals(sms.getStatusCode()) && sms.getDateDelivered() != null ){
								iconDecorators.add(new UITooltipDecorator(messageFullStatus + ": " + dateUtil.formatDate(sms.getDateDelivered())));
							}else if( SmsConst_DeliveryStatus.STATUS_SENT.equals(sms.getStatusCode()) && sms.getDateSent() != null ){
								iconDecorators.add(new UITooltipDecorator(messageFullStatus + ": " + dateUtil.formatDate(sms.getDateSent())));
							}else {
								iconDecorators.add(new UITooltipDecorator(messageFullStatus));
							}
							statusIcon.decorators = iconDecorators;
							
						}
						UIMessage.make(wrapper, "back-button", 
								("sms.general.back"));
					//***End Sent Task rendering
					}else{
						//TODO: sms was found but ststus is not recognised. show an error
						log.debug("Task " + smsId + "was found but its status is not recognised.");
					}
				}
			}else {
			// TODO: show error message since sms.id() is not specified
				log.debug("No sms task id was specified.");
			}
		}

	}

	public ViewParameters getViewParameters() {
		return new SmsParams();
	}
}
