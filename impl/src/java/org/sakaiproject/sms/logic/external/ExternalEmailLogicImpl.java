package org.sakaiproject.sms.logic.external;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

import org.apache.commons.validator.routines.EmailValidator;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.emailtemplateservice.service.EmailTemplateService;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExternalEmailLogicImpl implements ExternalEmailLogic {


	/*
	 * Setters and injected services
	 */
	@Setter private EmailService emailService;
	@Setter private ServerConfigurationService serverConfigurationService = null;
	@Setter private EmailTemplateService emailTemplateService;	

	public void init() {
		log.info("init()");
		List<String> templatePaths = new ArrayList<String>();
		templatePaths.add(FILE_TEMPLATE_TASK_STARTED);
		templatePaths.add(FILE_TEMPLATE_TASK_SENT);
		templatePaths.add(FILE_TEMPLATE_TASK_EXCEPTION);
		templatePaths.add(FILE_TEMPLATE_TASK_OVER_QUOTA);
		templatePaths.add(FILE_TEMPLATE_TASK_OVER_QUOTA_MO);
		templatePaths.add(FILE_TEMPLATE_TASK_EXPIRED);
		templatePaths.add(FILE_TEMPLATE_TASK_COMPLETED);
		templatePaths.add(FILE_TEMPLATE_TASK_ABORTED);
		templatePaths.add(FILE_TEMPLATE_TASK_FAILED);
		templatePaths.add(FILE_TEMPLATE_TASK_INSUFICIENT_CREDITS);
		emailTemplateService.processEmailTemplates(templatePaths);

	}

	/*
	 * 
	 * Actual methods
	 * 
	 */
	public boolean sendEmail(String toAddress, String subject,
			String body) {

		log.debug("Sending email to:" + toAddress + " subject:" + subject
				+ " body:" + body);
		String from = "smstesting@sakai";
		sendEmails(from, new String[] { toAddress }, subject, body);
		return true;
	}




	/**
	 * @see ExternalLogic#sendEmails(String, String[], String, String)
	 */
	public String[] sendEmails(String from, String[] emails,
			String subject, String message) {
		InternetAddress fromAddress;
		try {
			fromAddress = new InternetAddress(from);
		} catch (AddressException e) {
			// cannot recover from this failure
			throw new IllegalArgumentException("Invalid from address: " + from,
					e);
		}
		List<String> toEmails = new ArrayList<String>();
		for (String email : emails) {
			if (email != null && !"".equals(email)) {
				toEmails.add(email);
			}
		}

		return sendEmails(fromAddress, toEmails, subject, message);

	}

	/**
	 * Actual sending of e-mail via Sakai email service
	 */
	private String[] sendEmails(InternetAddress fromAddress,
			Collection<String> toEmails, String subject, String message) {

		if (!serverConfigurationService.getBoolean("sms.notify.email", false)) {
			log.debug("Enable notification is disabled (sms.notify.email=false in sakai.properties)");
			return new String[0];
		}

		InternetAddress[] replyTo = new InternetAddress[1];
		List<InternetAddress> listAddresses = new ArrayList<InternetAddress>();
		EmailValidator emailValidator = EmailValidator.getInstance();

		for (Iterator<String> it = toEmails.iterator(); it.hasNext();) {
			String email = it.next();
			try {
				if (emailValidator.isValid(email)) {
					InternetAddress toAddress = new InternetAddress(email);
					listAddresses.add(toAddress);
				}
			} catch (AddressException e) {
				log.warn("Invalid to address: " + email
						+ ", cannot send email", e);
			}
		}

		replyTo[0] = fromAddress;
		InternetAddress[] toAddresses = listAddresses
		.toArray(new InternetAddress[listAddresses.size()]);
		emailService.sendMail(fromAddress, toAddresses, subject, message, null,
				null, null);

		// now we send back the list of people who the email was sent to
		String[] addresses = new String[toAddresses.length];
		for (int i = 0; i < toAddresses.length; i++) {
			addresses[i] = toAddresses[i].getAddress();
		}
		return addresses;
		// return ((String[]) toEmails.toArray());
	}

	public void sendEmailTemplate(String from, List<String> to, String templateKey,
			Map<String, String>replacementValues) {
		if (emailTemplateService == null) return;
		emailTemplateService.sendRenderedMessages(templateKey, to, replacementValues, from , null);

	}



}
