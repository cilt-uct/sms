package org.sakaiproject.sms.jobs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.email.api.Attachment;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.emailtemplateservice.model.RenderedTemplate;
import org.sakaiproject.emailtemplateservice.service.EmailTemplateService;
import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.hibernate.SmsAccountLogic;
import org.sakaiproject.sms.logic.hibernate.SmsTransactionLogic;
import org.sakaiproject.sms.model.hibernate.SmsAccount;
import org.sakaiproject.sms.model.hibernate.SmsTransaction;
/**
 * Notifies account owners of activity on their jobs
 * 
 * @author dhorwitz
 *
 */
public class AccountActivityNotification implements Job {
	private static final Log LOG = LogFactory.getLog(AccountActivityNotification.class);
	
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
			
			//ok we need some info on the users
			String to = account.getNotificationEmail();
			if ((to == null || to.length() == 0) && account.getOwnerId() != null) {
				//look up the owners email 
				to = externalLogic.getSakaiEmailAddressForUserId(account.getOwnerId()); 
			}
			
			
			
			//construct the transaction list
			List<SmsTransaction> transList = smsTransactionLogic.getSmsTransactionsForAccountId(account.getId());
			StringBuilder csv = new StringBuilder();
			for (int q = 0; q < transList.size(); q ++) {
				SmsTransaction transaction = transList.get(q);
				csv.append("\"" + transaction.getTransactionDate() + "\",");
				csv.append("\"" + transaction.getDescription() + "\",");
				csv.append("\"" + transaction.getTransactionCredits() + "\",");
				csv.append("\"" + transaction.getCreditBalance() + "\"\r\n");
			}
			LOG.info("going to send sms to: " + to);
			LOG.info(csv.toString());
			//we need a file for this
			File csvAttach = new File("/tmp/accountstatement.csv");
			Writer output = null;
			
			try {
				output = new BufferedWriter(new FileWriter(csvAttach));
				output.write(csv.toString());
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally {
				try {
					if (output != null) {
						output.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			LOG.info("file of size: " + csvAttach.length());
			
			Map<String, String> repVals = new HashMap<String, String>();
			RenderedTemplate template = emailTemplateService.getRenderedTemplateForUser("sms.accountActivity", "/user/" + account.getOwnerId(),repVals);
			InternetAddress inetTo[];
			try {
				inetTo = new InternetAddress[]{new InternetAddress(to)};
				InternetAddress inetFrom = new InternetAddress("help@vula.uct.ac.za");
				List<Attachment> attachments = new ArrayList<Attachment>();
				attachments.add(new Attachment(csvAttach, "accountstatement.csv"));
				List<String> headers = new ArrayList<String>();
				String subject = template.getSubject();
				emailService.sendMail(inetFrom, inetTo, subject, template.getHtmlMessage(), null, null, headers, attachments);
			} catch (AddressException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (!csvAttach.delete()) {
				LOG.warn("couldn't delete temp file!");
			}
			
			
			
		}

	}



}
