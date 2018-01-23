package org.sakaiproject.sms.jobs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.email.api.Attachment;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.emailtemplateservice.model.RenderedTemplate;
import org.sakaiproject.emailtemplateservice.service.EmailTemplateService;
import org.sakaiproject.sms.logic.SmsAccountLogic;
import org.sakaiproject.sms.logic.SmsTransactionLogic;
import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.model.SmsAccount;
import org.sakaiproject.sms.model.SmsTransaction;
import org.sakaiproject.sms.model.SmsUser;

import lombok.extern.slf4j.Slf4j;
/**
 * Notifies account owners of activity on their jobs
 * 
 * @author dhorwitz
 *
 */
@Slf4j
public class AccountActivityNotification implements Job {

	
	private SmsAccountLogic smsAccountLogic;
	private ExternalLogic externalLogic;
	private SmsTransactionLogic smsTransactionLogic;
	private EmailTemplateService emailTemplateService;
	private EmailService emailService;
	
	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}

	public void setEmailTemplateService(EmailTemplateService emailTemplateService) {
		this.emailTemplateService = emailTemplateService;
	}

	public void setSmsAccountLogic(SmsAccountLogic smsAccountLogic) {
		this.smsAccountLogic = smsAccountLogic;
	}

	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}	
	public void setSmsTransactionLogic(SmsTransactionLogic smsTransactionLogic) {
		this.smsTransactionLogic = smsTransactionLogic;
	}

	
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		
		List<SmsAccount> accounts = smsAccountLogic.getAllSmsAccounts();
		for (int i = 0; i < accounts.size(); i++) {
			SmsAccount account = accounts.get(i);
			
			//user to send to
			String sendToUser = null;
			
			//ok we need some info on the users
			String to = account.getNotificationEmail();
			SmsUser userTo = null;
			if ((to == null || to.length() == 0) && account.getOwnerId() != null) {
				//look up the owners email
				userTo = externalLogic.getSmsUser(account.getOwnerId());
				to = userTo.getEmail();
				sendToUser = "/user/" + account.getOwnerId();
			} else {
				//TODO - need to get the userId from the email
				
			}
			
			if (userTo == null) {
				log.warn("can't resolve owner of account " + account.getId().toString() );
				continue;
			}
			
			//construct the transaction list
			List<SmsTransaction> transList = smsTransactionLogic.getSmsTransactionsForAccountId(account.getId());
			StringBuilder csv = new StringBuilder();
			csv.append("\"Date\",\"Description\",\"Credits\",\"Balance\"\r\n");
			for (int q = 0; q < transList.size(); q ++) {
				SmsTransaction transaction = transList.get(q);
				csv.append("\"" + transaction.getTransactionDate() + "\",");
				csv.append("\"" + transaction.getDescription() + "\",");
				csv.append("\"" + transaction.getTransactionCredits() + "\",");
				csv.append("\"" + transaction.getCreditBalance() + "\"\r\n");
			}
			log.debug("going to send sms to: " + to);
			log.debug(csv.toString());
			//we need a file for this
			String filePath = System.getProperty("java.io.tmpdir") + File.separator + "accountstatement.csv";
			log.info(filePath);
			File csvAttach = new File(filePath);
			Writer output = null;
			
			try {
				output = new BufferedWriter(new FileWriter(csvAttach));
				output.write(csv.toString());
			} catch (FileNotFoundException e1) {
				log.warn(e1.getLocalizedMessage(), e1);
			} catch (UnsupportedEncodingException e) {
				log.warn(e.getLocalizedMessage(), e);
			} catch (IOException e) {
				log.warn(e.getLocalizedMessage(), e);
			}
			finally {
				try {
					if (output != null) {
						output.close();
					}
				} catch (IOException e) {
					log.warn(e.getLocalizedMessage(), e);
				}
				
			}
			log.debug("file of size: " + csvAttach.length());
			
			Map<String, String> repVals = new HashMap<String, String>();
			repVals.put("recipientFirst", userTo.getFirstName());
			repVals.put("accountName", account.getAccountName());
			repVals.put("accountId", account.getId().toString());
			repVals.put("currentBalance", String.valueOf(account.getCredits()));
			
			RenderedTemplate template = emailTemplateService.getRenderedTemplateForUser("sms.accountActivity", sendToUser,repVals);
			InternetAddress inetTo[];
			try {
				inetTo = new InternetAddress[]{new InternetAddress(to)};
				InternetAddress inetFrom = new InternetAddress("help@vula.uct.ac.za");
				List<Attachment> attachments = new ArrayList<Attachment>();
				attachments.add(new Attachment(csvAttach, "accountstatement.csv"));
				List<String> headers = new ArrayList<String>();
				String subject = template.getSubject();
				emailService.sendMail(inetFrom, inetTo, subject, template.getRenderedMessage(), null, null, headers, attachments);
			} catch (AddressException e) {
				log.warn(e.getLocalizedMessage(), e);
			}
			
			if (!csvAttach.delete()) {
				log.warn("couldn't delete temp file!");
			}
			
			
			
		}

	}



}
