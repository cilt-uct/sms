/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.sms.logic.smpp.impl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.bean.AlertNotification;
import org.jsmpp.bean.Alphabet;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.DataCoding;
import org.jsmpp.bean.DataSm;
import org.jsmpp.bean.DeliverSm;
import org.jsmpp.bean.DeliveryReceipt;
import org.jsmpp.bean.ESMClass;
import org.jsmpp.bean.GeneralDataCoding;
import org.jsmpp.bean.MessageClass;
import org.jsmpp.bean.MessageType;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.RegisteredDelivery;
import org.jsmpp.bean.SMSCDeliveryReceipt;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.extra.SessionState;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.DataSmResult;
import org.jsmpp.session.MessageReceiverListener;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.session.SessionStateListener;
import org.jsmpp.util.AbsoluteTimeFormatter;
import org.jsmpp.util.DeliveryReceiptState;
import org.jsmpp.util.InvalidDeliveryReceiptException;
import org.jsmpp.util.TimeFormatter;
import org.sakaiproject.sms.logic.HibernateLogicLocator;
import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.external.NumberRoutingHelper;
import org.sakaiproject.sms.logic.smpp.SmsCore;
import org.sakaiproject.sms.logic.smpp.SmsSmpp;
import org.sakaiproject.sms.logic.smpp.exception.PropertyZeroOrSmallerException;
import org.sakaiproject.sms.model.SmsMOMessage;
import org.sakaiproject.sms.model.SmsMessage;
import org.sakaiproject.sms.model.constants.SmsConst_DeliveryStatus;
import org.sakaiproject.sms.model.constants.SmsConst_SmscDeliveryStatus;
import org.sakaiproject.sms.model.constants.SmsConstants;
import org.sakaiproject.sms.model.smpp.SmsSmppProperties;
import org.sakaiproject.sms.util.GsmCharset;

public class SmsSmppImpl implements SmsSmpp {

	private final static Log LOG = LogFactory.getLog(SmsSmppImpl.class);
	private Map<DeliveryReceiptState, Integer> smsDeliveryStatus = null;
	private final Properties properties = new Properties();
	private static TimeFormatter timeFormatter = new AbsoluteTimeFormatter();

	private final ThreadGroup moReceivingThread = new ThreadGroup(
			SmsConstants.SMS_MO_RECEIVING_THREAD_GROUP);

	private Queue<SmsMOMessage> receivedMOmessages = new ConcurrentLinkedQueue<SmsMOMessage>();
	private Queue<DeliverSm> receivedDeliveryReports = new ConcurrentLinkedQueue<DeliverSm>();
	private SMPPSession session = new SMPPSession();
	private boolean disconnectGateWayCalled = false;
	private BindThread reBindThread = null;

	private Object rebindLock = new Object();
	
	private boolean gatewayBound = false;

	private boolean appserverShuttingDown = false;

	MOmessageQueueThread mOmessageQueueThread = null;

	DeliveryReportQueueThread deliveryReportQueueThread = null;

	private SmsSmppProperties smsSmppProperties = null;

	private static final boolean ALLOW_PROCESS_REMOTELY = false;

	private NumberRoutingHelper numberRoutingHelper;

	private GsmCharset gsm = new GsmCharset();

	public void setNumberRoutingHelper(NumberRoutingHelper numberRoutingHelper) {
		this.numberRoutingHelper = numberRoutingHelper;
	}

	private HibernateLogicLocator hibernateLogicLocator;

	public HibernateLogicLocator getHibernateLogicLocator() {
		return hibernateLogicLocator;
	}

	public SmsCore getSmsCore() {
		return smsCore;
	}

	public void setSmsCore(SmsCore smsCore) {
		this.smsCore = smsCore;
	}

	private SmsCore smsCore = null;

	public void setHibernateLogicLocator(
			HibernateLogicLocator hibernateLogicLocator) {
		this.hibernateLogicLocator = hibernateLogicLocator;
	}

	// provides access to the session for the units.
	public SMPPSession getSession() {
		return session;
	}

	private class BindThread implements Runnable {

		public boolean allDone = false;
		private Thread thread = null;

		public Thread getThread() {
			return thread;
		}

		public void setThread(Thread thread) {
			this.thread = thread;
		}

		BindThread() {
			setThread(new Thread(this));
			getThread().start();
		}

		public void run() {
			work();
		}

		public void stop() {
			getThread().interrupt();
		}

		public void work() {
			while (true) {
				if (allDone) {
					LOG.debug("This thread is done.");
					return;
				}
				try {
					LOG.info("Trying to rebind");
					connectToGateway();
					Thread.sleep(smsSmppProperties.getBindThreadTimer());
				} catch (InterruptedException e) {

				} catch (Exception e) {
					LOG.error("BindThread encountered an error:");
					LOG.error(e.getMessage());
				}

			}
		}
	}

	private class MOmessageQueueThread implements Runnable {

		private static final int INITIAL_MO_SLEEP = 1000*60*2;
		boolean allDone = false;

		MOmessageQueueThread() {
			final Thread thread = new Thread(this);
			thread.start();
		}

		public void run() {
			work();
			LOG.debug("Finished.");
		}

		public void work() {
			//we need to slow down the Initial run to avoid proccesing messages before commands are registered
			try {
				LOG.info("sleeping for " + INITIAL_MO_SLEEP + "ms to avoid binding before task registration");
				Thread.sleep(INITIAL_MO_SLEEP);
			} catch (InterruptedException e1) {
				LOG.debug("sleep interupeted");
			}
			while (!allDone) {
				try {

					if (LOG.isDebugEnabled()) {
						int qsize = receivedMOmessages.size();
						if (qsize > 0) {
							LOG.debug("Processing MO-Message queue of "
									+ qsize + " messages with "
									+ moReceivingThread.activeCount() + " threads running");
						}
					}
					
					if (moReceivingThread.activeCount() <= SmsConstants.SMS_MO_MAX_THREAD_COUNT) {
						
						SmsMOMessage smsMOmessage = receivedMOmessages.poll();
						
						if (smsMOmessage != null) {
							new MOProcessThread(smsMOmessage, moReceivingThread);
						} else {
							// Wait for more MO messages to be available
							Thread.sleep(1000);
						}
						
					} else {
						// Wait for some delivery threads to finish
						Thread.sleep(50);						
					}

				} catch (Exception e) {
					LOG.error("MOmessageQueueThread encountered an error:");
					LOG.error(e.getMessage());
				}
				
			} // (!allDone)
		}
	}

	private class MOProcessThread implements Runnable {

		SmsMOMessage smsMOmessage;

		MOProcessThread(final SmsMOMessage smsMOmessage,
				final ThreadGroup threadGroup) {
			this.smsMOmessage = smsMOmessage;

			Thread thread = new Thread(threadGroup, this);
			thread.start();
		}

		public void run() {
			work();
		}

		public void work() {
			smsCore.processIncomingMessage(smsMOmessage);
		}
	}

	private class DeliveryReportQueueThread implements Runnable {

		boolean allDone = false;

		DeliveryReportQueueThread() {

			Thread thread = new Thread(this);
			thread.start();
		}

		public void run() {
			work();
		}

		public void work() {
			while (!allDone) {
				try {

					if (LOG.isDebugEnabled()) {
						int qsize = receivedDeliveryReports.size();
						if (qsize >= 10 && qsize % 10 == 0)
							LOG.debug("Delivery report queue has " + qsize
									+ " message(s)");
					}

					DeliverSm deliverSm = receivedDeliveryReports.poll();

					if (deliverSm != null) {
						handleDeliveryReport(deliverSm);
					} else {
						Thread.sleep(1000);
					}

				} catch (Exception e) {
					LOG.error("DeliveryReportQueueThread encountered an error", e);
				}
			}
		}
	}

	/**
	 * This listener will receive delivery reports as well as incoming messages
	 * from the smpp gateway. When we are bound to the gateway, this listener
	 * will receive tcp packets form the gateway. Note that any of the listeners
	 * running on a ip address, will receive reports and not just the session
	 * that sent them!
	 * 
	 * @author etienne@psybergate.co.za
	 * 
	 */
	private class MessageReceiverListenerImpl implements
			MessageReceiverListener {
		public void onAcceptAlertNotification(
				AlertNotification alertNotification) {
			// not implemented
		}

		public void onAcceptDeliverSm(DeliverSm deliverSm)
				throws ProcessRequestException {

			if (MessageType.SMSC_DEL_RECEIPT.containedIn(deliverSm
					.getEsmClass())) {

				LOG.debug("Queuing delivery receipt from: "
						+ deliverSm.getSourceAddr());

				receivedDeliveryReports.add(deliverSm);

			} else {
				LOG.info("Queuing MO message from: " + deliverSm.getSourceAddr() + 
						" with data coding " + deliverSm.getDataCoding());
				SmsMOMessage moMessage = new SmsMOMessage();
				
				// TODO - when/if we have multiple listeners, record this connection's SMSC ID
				moMessage.setSmscId(SmsConstants.SMSC_ID);

				moMessage.setMobileNumber(deliverSm.getSourceAddr());
				String messageBody = "";
				if (deliverSm.getShortMessage() == null) {

					// person sent a blank sms
					messageBody = SmsConstants.SMS_MO_EMPTY_REPLY_BODY;
					
				} else {
					
					// Alphabet is defined in bits 2 and 3 of data coding: GSM 03.38 Version 5.3.0
					
					int alphabet = ((deliverSm.getDataCoding() & 12) >> 2);

					LOG.debug("Message alphabet is " + alphabet);
					
					if (alphabet == 0 )  {
						// GSM
						messageBody =  String.valueOf(gsm.gsmToUtf(deliverSm.getShortMessage()));
					} else if (alphabet == 2) {
						// UTF-16
						try {
							messageBody = new String(deliverSm.getShortMessage(), "UTF-16BE");
						} catch (UnsupportedEncodingException e) {
							LOG.warn("Unsupported encoding UTF-16BE");
							messageBody = new String(deliverSm.getShortMessage());
						}
					} else {
						// 8 bit or Reserved
						messageBody = new String(deliverSm.getShortMessage());
					}

					LOG.debug("message body: " + messageBody);
					
				}
				moMessage.setSmsMessagebody(messageBody);
				receivedMOmessages.add(moMessage);
			}
		}

		public DataSmResult onAcceptDataSm(DataSm dataSm,
				org.jsmpp.session.Session source)
				throws ProcessRequestException {
			// not implemented
			return null;
		}
	}

	private void handleDeliveryReport(DeliverSm deliverSm) {
		try {
			boolean incrementMessagesDelivered = false;

			DeliveryReceipt deliveryReceipt = deliverSm
					.getShortMessageAsDeliveryReceipt();

			// for future use.
			if (ALLOW_PROCESS_REMOTELY) {
				notifyDeliveryReportRemotely(deliveryReceipt);
				return;
			}
			LOG.info("Processing delivery receipt from "
					+ deliverSm.getSourceAddr() + " : " + deliveryReceipt);
			SmsMessage smsMsg = hibernateLogicLocator.getSmsMessageLogic()
					.getSmsMessageBySmscMessageId(deliveryReceipt.getId(),
							SmsConstants.SMSC_ID);
			if (smsMsg == null) {
				for (int i = 0; i < 5; i++) {
					LOG.warn("SMSC_DEL_RECEIPT retry " + i
							+ " out of 5 for messageSmscID"
							+ deliveryReceipt.getId());
					smsMsg = hibernateLogicLocator.getSmsMessageLogic()
							.getSmsMessageBySmscMessageId(
									deliveryReceipt.getId(),
									SmsConstants.SMSC_ID);
					if (smsMsg != null) {
						break;
					}
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						LOG.error(e.getMessage(), e);
					}
				}

			}
			
			if (smsMsg == null) {
				LOG.error("Delivery report received for message not in database. MessageSMSCID="
								+ deliveryReceipt.getId());
			} else {

				Session session = null;
				Transaction tx = null;

				// SMS-128/113 : lock the message row
				SmsMessage smsMessage = null;

				try {
					session = getHibernateLogicLocator().getSmsMessageLogic()
							.getNewHibernateSession();
					tx = session.beginTransaction();

					smsMessage = (SmsMessage) session.get(SmsMessage.class,
							smsMsg.getId(), LockMode.UPGRADE);
					smsMessage.setSmscDeliveryStatusCode(smsDeliveryStatus
							.get((deliveryReceipt.getFinalStatus())));

					// Set the delivery time
					if (deliveryReceipt.getDoneDate() != null) {
						// seeing this currently has only minute precision it
						// may appear to be before the sent date
						if (deliveryReceipt.getDoneDate().before(
								smsMessage.getDateSent())) {
							smsMessage.setDateDelivered(new Date(System.currentTimeMillis()));
						} else {
							smsMessage.setDateDelivered(deliveryReceipt.getDoneDate());
						}
					} else {
						smsMessage.setDateDelivered(new Date(System.currentTimeMillis()));
					}

					// TODO - if we could identify a 'routing error' return code from Clickatell
					// here, then we'd reverse the credit cost for the message.
					
					// Set the delivery status
					if (smsDeliveryStatus.get(deliveryReceipt
							.getFinalStatus()) != SmsConst_SmscDeliveryStatus.DELIVERED) {
						smsMessage.setStatusCode(SmsConst_DeliveryStatus.STATUS_FAIL);
					} else {
						smsMessage.setStatusCode(SmsConst_DeliveryStatus.STATUS_DELIVERED);
						incrementMessagesDelivered = true;
					}

					// Update in db
					session.update(smsMessage);
					tx.commit();
					session.close();
				} catch (HibernateException e) {
					LOG.error("Error handling delivery report: "
									+ deliverSm, e);
					if (tx != null) {
						tx.rollback();
					}
					if (session != null) {
						session.close();
					}
				}

				if (smsMessage != null) {
					hibernateLogicLocator.getSmsTaskLogic().incrementMessageCounts(
						smsMessage.getSmsTask(), false, incrementMessagesDelivered);
				}
			}
		} catch (InvalidDeliveryReceiptException e) {
			LOG.error("Failed getting delivery receipt" + e);
		}
	}

	/**
	 * Bind to the remote gateway using a username and password. If the
	 * connection is dropped, this service will try and reconnect and specified
	 * intervals.
	 * 
	 * @return
	 */
	private boolean bind() {

		if (!gatewayBound) {

			if (!smsSmppProperties.isBindThisNode()) {
				LOG.info("This node is set not to connect to an SMPP gateway");
				return false;
			}

			LOG.info("Binding to " + smsSmppProperties.getSMSCAddress()
					+ " on port " + smsSmppProperties.getSMSCPort()
					+ " with username " + smsSmppProperties.getSMSCUsername());

			try {
				session = new SMPPSession();
				session.connectAndBind(smsSmppProperties.getSMSCAddress(),
						smsSmppProperties.getSMSCPort(), new BindParameter(
								BindType.BIND_TRX, smsSmppProperties
										.getSMSCUsername(), smsSmppProperties
										.getSMSCPassword(), smsSmppProperties
										.getSystemType(), TypeOfNumber
										.valueOf(smsSmppProperties
												.getDestAddressTON()),
								NumberingPlanIndicator
										.valueOf(smsSmppProperties
												.getDestAddressNPI()),
								smsSmppProperties.getAddressRange()));
				
				synchronized (rebindLock) {
					if (reBindThread != null) {
						reBindThread.allDone = true;
						reBindThread = null;
					}
				}
				
				gatewayBound = true;
				LOG.info("EnquireLinkTimer is set to "
						+ smsSmppProperties.getEnquireLinkTimeOut()
						+ " milliseconds");
				session.setEnquireLinkTimer(smsSmppProperties
						.getEnquireLinkTimeOut());

				gatewayBound = true;
				LOG.info("TransactionTimer is set to "
						+ smsSmppProperties.getTransactionTimer()
						+ " milliseconds");
				session.setTransactionTimer(smsSmppProperties
						.getTransactionTimer());
				session
						.setMessageReceiverListener(new MessageReceiverListenerImpl());
				session.addSessionStateListener(new SessionStateListener() {

					public void onStateChange(SessionState arg0,
							SessionState arg1, Object arg2) {

						if ((arg0.equals(SessionState.CLOSED) || arg0
								.equals(SessionState.UNBOUND))
								&& (!disconnectGateWayCalled)) {
							LOG.warn("SMSC session lost Status-" + arg0);
							gatewayBound = false;
							session.unbindAndClose();
							if (arg0.equals(SessionState.CLOSED)
									&& !appserverShuttingDown
									&& reBindThread == null) {

								synchronized (rebindLock) {
									reBindThread = new BindThread();
								}
							}
						}
					}
				});
				LOG.info("Bind successful");
				hibernateLogicLocator.getExternalLogic().postEvent(ExternalLogic.SMS_EVENT_SMPP_BIND, 
					smsSmppProperties.getSMSCAddress() + ":" + smsSmppProperties.getSMSCPort(), null);

			} catch (Exception e) {
				LOG.error("Bind operation failed. " + e);
				gatewayBound = false;
				session.unbindAndClose();
				synchronized (rebindLock) {
					if (!appserverShuttingDown && reBindThread == null) {
						LOG.info("Starting Binding thread");
						reBindThread = new BindThread();
					}
				}
			}
		}
		return gatewayBound;
	}

	/**
	 * Establish a connection the the gateway (bind). The connection will be
	 * kept open for the lifetime of the session. Concurrent connections will be
	 * possible from other smpp services. The status of the connection will be
	 * checked before sending a message, and an auto-bind will be made if
	 * possible.
	 */
	public boolean connectToGateway() {
		disconnectGateWayCalled = false;
		return bind();
	}

	/**
	 * Unbind from the gateway. If disconnected, no message sending will be
	 * possible. For unit testing purposes.
	 */
	public void disconnectGateWay() {

		disconnectGateWayCalled = true;
		session.unbindAndClose();
		gatewayBound = false;

	}

	/**
	 * Return the status of this connection to the gateway.
	 */
	public boolean getConnectionStatus() {

		if (gatewayBound) {
			LOG.info("The server is currently binded to "
					+ smsSmppProperties.getSMSCAddress() + "  "
					+ smsSmppProperties.getSMSCPort());
		} else {
			LOG.info("The server is not currently binded");
		}

		return gatewayBound;
	}

	/**
	 * Get some info from the remote gateway.
	 */
	public String getGatewayInfo() {
		String gatewayInfo = "Session bound as = " + session.getSessionState()
				+ "\n";
		gatewayInfo += "EnquireLinkTimer = " + session.getEnquireLinkTimer()
				/ 1000 + " seconds \n";
		gatewayInfo += "SessionID is 	 = " + session.getSessionId() + "\n";
		return gatewayInfo;
	}

	public void init() {
		LOG.info("init()");

		loadPropertiesFile();
		loadSmsSmppProperties();

		//if (smsSmppProperties.isBindThisNode()) {
			mOmessageQueueThread = new MOmessageQueueThread();
			deliveryReportQueueThread = new DeliveryReportQueueThread();
			
	//	}
		
		connectToGateway();
		setupStatusBridge();
		
		LOG.debug("SmsSmpp implementation is started");
	}

	public void destroy() {
		LOG.info("destroy()");
		disconnectGateWay();
		appserverShuttingDown = true;
		if (reBindThread != null) {
			LOG.debug("Stopping Bind Thread....");
			reBindThread.allDone = true;
			reBindThread.stop();
			reBindThread = null;
			LOG.debug("Stopped....");

		}
		if (mOmessageQueueThread != null) {
			LOG.debug("Stopping MO MessageQueueThread....");
			mOmessageQueueThread.allDone = true;
			mOmessageQueueThread = null;
			LOG.debug("Stopped....");

		}
		if (deliveryReportQueueThread != null) {
			LOG.debug("Stopping DeliveryReportQueueThread....");
			deliveryReportQueueThread.allDone = true;
			deliveryReportQueueThread = null;
			LOG.debug("Stopped....");

		}

	}

	/**
	 * Matches up the statuses from the JSMPP API to our local statuses.
	 */
	private void setupStatusBridge() {
		/*
		 * The hashMap that stores the JSMPP statuses as the key and our local
		 * statuses as the value.
		 */
		smsDeliveryStatus = new HashMap<DeliveryReceiptState, Integer>();

		smsDeliveryStatus.put(DeliveryReceiptState.ACCEPTD,
				SmsConst_SmscDeliveryStatus.ACCEPTED);
		smsDeliveryStatus.put(DeliveryReceiptState.DELETED,
				SmsConst_SmscDeliveryStatus.DELETED);
		smsDeliveryStatus.put(DeliveryReceiptState.DELIVRD,
				SmsConst_SmscDeliveryStatus.DELIVERED);
		smsDeliveryStatus.put(DeliveryReceiptState.EXPIRED,
				SmsConst_SmscDeliveryStatus.EXPIRED);
		smsDeliveryStatus.put(DeliveryReceiptState.UNDELIV,
				SmsConst_SmscDeliveryStatus.UNDELIVERA);
		smsDeliveryStatus.put(DeliveryReceiptState.UNKNOWN,
				SmsConst_SmscDeliveryStatus.UNKNOWN);
		smsDeliveryStatus.put(DeliveryReceiptState.REJECTD,
				SmsConst_SmscDeliveryStatus.REJECTED);
	}

	/**
	 * Read some smpp properties from a file. These properties can be changed as
	 * required. If the gateway information is not found in sakai.properties,
	 * then we read it from our own smpp.properties. Please note that this info
	 * should rather be in sakai.properties.
	 */
	private void loadSmsSmppProperties() {

		if (smsSmppProperties == null) {
			smsSmppProperties = new SmsSmppProperties();

			smsSmppProperties.setMessageEncoding(properties.getProperty(
					"MessageEncoding").trim());
			
			smsSmppProperties.setSMSCAddress(properties.getProperty(
					"SMSCAddress").trim());
			try {
				smsSmppProperties.setSMSCPort(Integer.parseInt(properties
						.getProperty("SMSCPort").trim()));
				if (smsSmppProperties.getSMSCPort() <= 0) {

					throw new PropertyZeroOrSmallerException("SMSCPort");
				}

			} catch (PropertyZeroOrSmallerException e) {
				LOG.error(e.getMessage(), e);
				LOG.warn("SMSC Port defaulting to port "
						+ SmsSmppProperties.DEFAULT_SMSC_PORT);

				smsSmppProperties
						.setSMSCPort(SmsSmppProperties.DEFAULT_SMSC_PORT);

			} catch (NumberFormatException e) {

				LOG.error(e.getMessage(), e);
				LOG.warn("SMSC Port defaulting to port "
						+ SmsSmppProperties.DEFAULT_SMSC_PORT);

				smsSmppProperties
						.setSMSCPort(SmsSmppProperties.DEFAULT_SMSC_PORT);

			}
			smsSmppProperties.setSMSCUsername(properties.getProperty(
					"SMSCUserName").trim());

			smsSmppProperties.setSMSCPassword(properties.getProperty(
					"SMSCPassword").trim());
		}
		smsSmppProperties.setBindThreadTimer(5 * 1000);
		smsSmppProperties.setSystemType(properties.getProperty("systemType")
				.trim());
		smsSmppProperties.setServiceType(properties.getProperty("serviceType")
				.trim());
		smsSmppProperties.setSourceAddress(properties.getProperty(
				"sourceAddress").trim());
		smsSmppProperties.setSourceAddressNPI(Byte.parseByte(properties
				.getProperty("sourceAddressNPI").trim()));
		smsSmppProperties.setSourceAddressTON(Byte.parseByte(properties
				.getProperty("sourceAddressTON").trim()));

		smsSmppProperties.setDestAddressNPI(Byte.parseByte(properties
				.getProperty("destAddressNPI").trim()));
		smsSmppProperties.setDestAddressTON(Byte.parseByte(properties
				.getProperty("destAddressTON").trim()));

		smsSmppProperties.setProtocolId(Byte.parseByte(properties.getProperty(
				"protocolId").trim()));
		smsSmppProperties.setPriorityFlag(Byte.parseByte(properties
				.getProperty("priorityFlag").trim()));
		smsSmppProperties.setReplaceIfPresentFlag(Byte.parseByte(properties
				.getProperty("replaceIfPresentFlag").trim()));
		smsSmppProperties.setSmDefaultMsgId(Byte.parseByte(properties
				.getProperty("smDefaultMsgId").trim()));

		try {

			smsSmppProperties.setEnquireLinkTimeOut(Integer.parseInt(properties
					.getProperty("enquireLinkTimeOutSeconds").trim()) * 1000);
			if (smsSmppProperties.getEnquireLinkTimeOut() <= 0) {

				throw new PropertyZeroOrSmallerException("EnquireLinkTimeOut");
			}
		} catch (PropertyZeroOrSmallerException e) {
			LOG.error(e.getMessage(), e);
			LOG.warn("EnquireLinkTimeOut defaulting to  "
					+ SmsSmppProperties.DEFAULT_ENQUIRELINK_TIMEOUT
					+ " seconds");

			smsSmppProperties
					.setEnquireLinkTimeOut(SmsSmppProperties.DEFAULT_ENQUIRELINK_TIMEOUT * 1000);

		} catch (NumberFormatException e) {
			LOG.error(e.getMessage(), e);
			LOG.warn("EnquireLinkTimeOut defaulting to  "
					+ SmsSmppProperties.DEFAULT_ENQUIRELINK_TIMEOUT
					+ " seconds");

			smsSmppProperties
					.setEnquireLinkTimeOut(SmsSmppProperties.DEFAULT_ENQUIRELINK_TIMEOUT * 1000);
		}
		try {
			smsSmppProperties.setBindThreadTimer(Integer.parseInt(properties
					.getProperty("bindThreadTimerSeconds").trim()) * 1000);
			if (smsSmppProperties.getBindThreadTimer() <= 0) {

				throw new PropertyZeroOrSmallerException("BindThreadTimer");
			}
		} catch (PropertyZeroOrSmallerException e) {
			LOG.error(e);
			LOG.warn("BindThreadTimer defaulting to  "
					+ SmsSmppProperties.DEFAULT_BINDTHREAD_TIMER + " seconds");

			smsSmppProperties
					.setBindThreadTimer((SmsSmppProperties.DEFAULT_BINDTHREAD_TIMER) * 1000);

		} catch (NumberFormatException e) {
			LOG.error(e);
			LOG.warn("BindThreadTimer defaulting to  "
					+ SmsSmppProperties.DEFAULT_BINDTHREAD_TIMER + " seconds");

			smsSmppProperties
					.setBindThreadTimer(SmsSmppProperties.DEFAULT_BINDTHREAD_TIMER * 1000);
		}

		smsSmppProperties.setAddressRange(properties
				.getProperty("addressRange").trim());

		try {
			smsSmppProperties.setTransactionTimer(Integer.parseInt(properties
					.getProperty("transactionTimer").trim()) * 1000);
			if (smsSmppProperties.getTransactionTimer() <= 0) {

				throw new PropertyZeroOrSmallerException("TransactionTimer");
			}
		} catch (PropertyZeroOrSmallerException e) {
			LOG.error(e);
			LOG.warn("TransactionTimer defaulting to  "
					+ SmsSmppProperties.DEFAULT_TRANSACTION_TIMER_INTERVAL
					+ " seconds");

			smsSmppProperties
					.setTransactionTimer(SmsSmppProperties.DEFAULT_TRANSACTION_TIMER_INTERVAL * 1000);

		} catch (NumberFormatException e) {
			LOG.error(e);
			LOG.warn("TransactionTimer defaulting to  "
					+ SmsSmppProperties.DEFAULT_TRANSACTION_TIMER_INTERVAL
					+ " seconds");

			smsSmppProperties
					.setTransactionTimer(SmsSmppProperties.DEFAULT_TRANSACTION_TIMER_INTERVAL * 1000);
		}

		try {
			smsSmppProperties.setSendingDelay(Integer.parseInt(properties
					.getProperty("sendingDelay").trim()));
			if (smsSmppProperties.getSendingDelay() <= 0) {

				throw new PropertyZeroOrSmallerException("SendingDelay");
			}
		} catch (PropertyZeroOrSmallerException e) {
			LOG.error(e);
			LOG
					.warn("SendingDelay defaulting to  "
							+ SmsSmppProperties.DEFAULT_SENDING_DELAY
							+ " milliseconds");

			smsSmppProperties
					.setSendingDelay(SmsSmppProperties.DEFAULT_SENDING_DELAY);

		} catch (NumberFormatException e) {
			LOG.error(e);
			LOG
					.warn("SendingDelay defaulting to  "
							+ SmsSmppProperties.DEFAULT_SENDING_DELAY
							+ " milliseconds");

			smsSmppProperties
					.setSendingDelay(SmsSmppProperties.DEFAULT_SENDING_DELAY);
		}

		// get the overides of defaults from Sakai properties
		smsSmppProperties = hibernateLogicLocator.getExternalLogic()
				.getSmppProperties(smsSmppProperties);

	}

	private void loadPropertiesFile() {

		try {
			InputStream inputStream = this.getClass().getResourceAsStream(
					"/smpp.properties");
			if (inputStream == null) {
				final FileInputStream fileInputStream = new FileInputStream(
						"smpp.properties");
				properties.load(fileInputStream);
				if (fileInputStream != null) {
					fileInputStream.close();
				}
			} else {
				properties.load(inputStream);
				inputStream.close();
			}
		} catch (FileNotFoundException e) {
			LOG.error(e.getMessage(), e);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	/**
	 * Send a list of messages one-by-one to the gateway. Abort if the gateway
	 * connection is down or when gateway returns an error and mark relevant
	 * messages as failed. Return message statuses (not reports) back to caller.
	 * 
	 * @return
	 */
	public String sendMessagesToGateway(Set<SmsMessage> messages) {

		boolean retries = false;
		
		if (!gatewayBound) {
			return SmsConst_DeliveryStatus.STATUS_RETRY;
		}

		for (SmsMessage message : messages) {
			long timeStart = System.currentTimeMillis();
			SmsMessage deliverMessage = sendMessageToGateway(message);
			long timeToSend = System.currentTimeMillis() - timeStart;
			LOG.debug("Message processed in " + timeToSend + "ms");
			// Because we get back a different object, copy across the fields we need
			
			message.setStatusCode(deliverMessage.getStatusCode());
			message.setCredits(deliverMessage.getCredits());
			
			// If we get back a status code that would be a retry, then
			// set task status to incomplete
			
			if (!message.getStatusCode().equals(SmsConst_DeliveryStatus.STATUS_SENT) &&
				!message.getStatusCode().equals(SmsConst_DeliveryStatus.STATUS_ERROR)) {
				
				// Transient error in message delivery for at least one message
				retries = true;
			}

			if (!gatewayBound) {
				// Can't send any more, so bail out now
				return SmsConst_DeliveryStatus.STATUS_INCOMPLETE;
			}
			
			try {
				//seeing as we may have slow sending we need to prevent this slowing down submission too much
				long sendDelay = smsSmppProperties.getSendingDelay() - timeToSend;
				if (sendDelay > 0 ) {
					Thread.sleep(sendDelay);
				}
			} catch (InterruptedException e) {
				LOG.error(e);
			}
			
		}
		
		return retries ? SmsConst_DeliveryStatus.STATUS_INCOMPLETE : SmsConst_DeliveryStatus.STATUS_SENT;
	}

	/**
	 * This is a future function that could allow an external system to receive
	 * the delivery report and handle it accordingly. See a code example in
	 * processOutgoingMessageRemotely.
	 * 
	 * @param deliveryReceipt
	 * @return
	 */
	private SmsMessage notifyDeliveryReportRemotely(
			DeliveryReceipt deliveryReceipt) {

		return null;
	}

	/**
	 * Send one message to the SMS gateway. Return result code to caller.
	 */
	public SmsMessage sendMessageToGateway(SmsMessage message) {
		
		// Process remotely
		if (ALLOW_PROCESS_REMOTELY) {
			processOutgoingMessageRemotely(message);
			return message;
		}

		// Not gateway bound
		if (!gatewayBound) {
			LOG.debug("Sms gateway not bound on this node queueing message");
			return message;
		}

		long timeStart = System.currentTimeMillis();
		String messageText = message.getSmsTask().getMessageBody();

		if (message.getSmsTask().getMessageTypeId().equals(
				SmsConstants.MESSAGE_TYPE_MOBILE_ORIGINATING)) {
			messageText = message.getMessageReplyBody();
		}

		if (messageText == null) {
			throw new IllegalArgumentException(
					"SMS Message body text may not be empty.");
		}

		Session hibernateSession = getHibernateLogicLocator().getSmsMessageLogic()
				.getNewHibernateSession();
		
		if (hibernateSession == null) {
			LOG.error("Cannot open database session");
			return message;
		}

		// Lock the message in the db
		
		Transaction tx = hibernateSession.beginTransaction();
		message = (SmsMessage) hibernateSession.get(SmsMessage.class, message.getId(),
				LockMode.UPGRADE);

		// Continue to send message to gateway.

		message.setDateSent(new Date());

		try {
		
			// Record which gateway this message is being sent to
			if (numberRoutingHelper.getRoutingInfo(message)) {

				// Set the encoding
				
				Alphabet alphabet = null;
				byte[] messageBytes = null;
				
				if (gsm.isEncodeableInGsm0338(messageText)) {
					// Encode in GSM
					alphabet = Alphabet.ALPHA_DEFAULT;
					messageBytes = gsm.utfToGsm(messageText);
				} else {
					// Encode in UTF
					alphabet = Alphabet.ALPHA_UCS2;
					messageBytes = messageText.getBytes("UTF-16BE");
				}
				
				// TODO - When should we use Alphabet.ALPHA_8_BIT ?
				
				DataCoding messageEncoding = new GeneralDataCoding(false, false, MessageClass.CLASS0, alphabet);
				LOG.debug("Encoding is " + messageEncoding);

				/*
				// For testing failure cases not generated by the simulator
				
				if (message.getMobileNumber().endsWith("9")) {
					throw new IOException("lost connection test");
				}

				if (message.getMobileNumber().endsWith("8")) {
					throw new Exception("generic excep test");
				}

				if (message.getMobileNumber().endsWith("7")) {
					throw new InvalidResponseException("ire excep test");
				}
				
				if (message.getMobileNumber().endsWith("6")) {
					throw new NegativeResponseException(99);
				}
			
				if (message.getMobileNumber().endsWith("5")) {
					throw new ResponseTimeoutException("rte excep test");
				}

				if (message.getMobileNumber().endsWith("4")) {
					throw new ResponseTimeoutException("io excep test");
				}
				*/

				// Send the message

				String messageId = session.submitShortMessage(smsSmppProperties
						.getServiceType(), TypeOfNumber.valueOf(smsSmppProperties
						.getSourceAddressTON()), NumberingPlanIndicator
						.valueOf(smsSmppProperties.getSourceAddressNPI()),
						smsSmppProperties.getSourceAddress(), TypeOfNumber
								.valueOf(smsSmppProperties.getDestAddressTON()),
						NumberingPlanIndicator.valueOf(smsSmppProperties
								.getDestAddressNPI()), message.getMobileNumber(),
						new ESMClass(), smsSmppProperties.getProtocolId(),
						smsSmppProperties.getPriorityFlag(), null, null, new RegisteredDelivery(
								SMSCDeliveryReceipt.SUCCESS_FAILURE),
						smsSmppProperties.getReplaceIfPresentFlag(),
						messageEncoding, smsSmppProperties
								.getSmDefaultMsgId(), messageBytes);

				message.setSmscMessageId(messageId);
				message.setSubmitResult(true);
				message.setStatusCode(SmsConst_DeliveryStatus.STATUS_SENT);
				message.setSmscDeliveryStatusCode(SmsConst_SmscDeliveryStatus.ENROUTE);
				long timeToSend = System.currentTimeMillis() - timeStart;
				LOG.info("Message submitted, smsc_id = " + messageId
						+ " MessageID = " + message.getId()
						+ " Number = " + message.getMobileNumber()
						+ " TaskID = " + message.getSmsTask().getId()
						+ " in " + timeToSend + " ms");
			
			} else {
				// number is unroutable
				message.setStatusCode(SmsConst_DeliveryStatus.STATUS_ERROR);
				message.setFailReason("Number is unroutable");
			}

		} catch (PDUException e) {
			
			// Invalid PDU parameter
			message.setFailReason(e.getMessage());
			message.setStatusCode(SmsConst_DeliveryStatus.STATUS_ERROR);
			LOG.error(e);

		} catch (ResponseTimeoutException e) {

			// Response timeout
			message.setFailReason(e.getMessage());
			message.setStatusCode(SmsConst_DeliveryStatus.STATUS_ERROR);
			LOG.error(e);

		} catch (InvalidResponseException e) {

			// Invalid response
			message.setFailReason(e.getMessage());
			message.setStatusCode(SmsConst_DeliveryStatus.STATUS_ERROR);
			LOG.error(e);

		} catch (NegativeResponseException e) {

			// Receiving negative response (non-zero command_status)
			message.setFailReason(e.getMessage());
			message.setStatusCode(SmsConst_DeliveryStatus.STATUS_ERROR);
			message.setSmscDeliveryStatusCode(e.getCommandStatus());

		} catch (IOException e) {
			
			// Probably a dropped connection / network issue. Transient.	
			LOG.warn("I/O error delivering message to gateway: "+ e);

		} catch (Exception e) {
			
			// Unknown cause. Assume this is permanent.		
			message.setFailReason(e.getMessage());
			message.setStatusCode(SmsConst_DeliveryStatus.STATUS_ERROR);
			
			LOG.error("Unknown error delivering message to gateway: ", e);

		} finally {
			
			try {
				hibernateSession.update(message);
				tx.commit();
			} catch (ConstraintViolationException cve) {
				// This should not happen unless the SMSC returns duplicate message IDs
				// (which can happen in some testing / simulator scenarios)
				LOG.warn("Cannot update SMSC id for message id=" + message.getId() + ". Delivery status will not be recorded.");
				message.setSmscMessageId(null);
				hibernateSession.update(message);
				tx.commit();
			} finally {
				hibernateSession.close();	
			}
		}
		
		return message;
	}

	/**
	 * Outgoing sms messages may be processed (delivered) by an external
	 * service. We simply pass the messages on to that service via http POST for
	 * now. By default disabled.
	 * <p>
	 * NB: This is just example code of a possible implementation. The remote
	 * service will need to handle the delivery reports. Other possible solution
	 * is to use web services.
	 * 
	 * @param smsMessage
	 * @return
	 */
	public boolean processOutgoingMessageRemotely(SmsMessage smsMessage) {
		try {
			// Construct data
			String data = URLEncoder.encode("messageBody", "UTF-8") + "="
					+ URLEncoder.encode(smsMessage.getMessageBody(), "UTF-8");
			data += "&" + URLEncoder.encode("mobileNumber", "UTF-8") + "="
					+ URLEncoder.encode(smsMessage.getMobileNumber(), "UTF-8");

			// Send data
			URL url = new URL("http://hostname:80/process.php");
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			OutputStreamWriter wr = new OutputStreamWriter(conn
					.getOutputStream());
			wr.write(data);
			wr.flush();

			// Remote service can also return a smscMessage id and perhaps a
			// status code.
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn
					.getInputStream()));
			while (rd.readLine() != null) {
				// Process line...
			}
			wr.close();
			rd.close();

		} catch (UnsupportedEncodingException e) {
			return false;
		} catch (MalformedURLException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public boolean notifyDeliveryReportRemotely(SmsMessage smsMessage) {
		// TODO To be discussed with UCT
		return false;
	}

}
