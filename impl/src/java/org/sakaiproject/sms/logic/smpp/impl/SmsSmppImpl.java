/***********************************************************************************
 * SmsSmppImpl.java
 * Copyright (c) 2008 Sakai Project/Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.osedu.org/licenses/ECL-2.0
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
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Level;
import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.bean.Address;
import org.jsmpp.bean.AlertNotification;
import org.jsmpp.bean.Alphabet;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.DeliverSm;
import org.jsmpp.bean.DeliveryReceipt;
import org.jsmpp.bean.ESMClass;
import org.jsmpp.bean.GeneralDataCoding;
import org.jsmpp.bean.MessageClass;
import org.jsmpp.bean.MessageType;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.RegisteredDelivery;
import org.jsmpp.bean.ReplaceIfPresentFlag;
import org.jsmpp.bean.SMSCDeliveryReceipt;
import org.jsmpp.bean.SubmitMultiResult;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.extra.SessionState;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.MessageReceiverListener;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.session.SessionStateListener;
import org.jsmpp.util.AbsoluteTimeFormatter;
import org.jsmpp.util.DeliveryReceiptState;
import org.jsmpp.util.InvalidDeliveryReceiptException;
import org.jsmpp.util.TimeFormatter;
import org.sakaiproject.sms.logic.hibernate.HibernateLogicLocator;
import org.sakaiproject.sms.logic.smpp.SmsCore;
import org.sakaiproject.sms.logic.smpp.SmsSmpp;
import org.sakaiproject.sms.logic.smpp.exception.PropertyZeroOrSmallerException;
import org.sakaiproject.sms.model.hibernate.SmsMOMessage;
import org.sakaiproject.sms.model.hibernate.SmsMessage;
import org.sakaiproject.sms.model.hibernate.constants.SmsConst_DeliveryStatus;
import org.sakaiproject.sms.model.hibernate.constants.SmsConst_SmscDeliveryStatus;
import org.sakaiproject.sms.model.hibernate.constants.SmsConstants;
import org.sakaiproject.sms.model.smpp.SmsSmppProperties;

public class SmsSmppImpl implements SmsSmpp {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(SmsSmppImpl.class);
	private HashMap<DeliveryReceiptState, Integer> smsDeliveryStatus = null;
	private final Properties properties = new Properties();
	private static TimeFormatter timeFormatter = new AbsoluteTimeFormatter();

	private final ThreadGroup moReceivingThread = new ThreadGroup(
			SmsConstants.SMS_MO_RECEIVING_THREAD_GROUP);
	private final ThreadGroup deliveryReportThreadGroup = new ThreadGroup(
			SmsConstants.SMS_DELIVERY_REPORT_THREAD_GROUP);

	ArrayList<SmsMOMessage> receivedMOmessages = new ArrayList<SmsMOMessage>();
	ArrayList<DeliverSm> receivedDeliveryReports = new ArrayList<DeliverSm>();
	private SMPPSession session = new SMPPSession();
	private boolean disconnectGateWayCalled;
	private BindThread bindTest;

	private boolean gatewayBound = false;

	MOmessageQueueThread mOmessageQueueThread = new MOmessageQueueThread();

	DeliveryReportQueueThread deliveryReportQueueThread = new DeliveryReportQueueThread();

	private SmsSmppProperties smsSmppProperties = null;

	private static final boolean ALLOW_PROCESS_REMOTELY = false;

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

		boolean allDone = false;

		BindThread() {
			Thread t = new Thread(this);
			t.start();
		}

		public void run() {
			Work();
		}

		public void Work() {
			while (true) {
				if (allDone) {
					return;
				}
				try {
					Thread.sleep(smsSmppProperties.getBindThreadTimer());
				} catch (InterruptedException e) {

					e.printStackTrace();
				}
				LOG.info("Trying to rebind");
				connectToGateway();

			}
		}
	}

	private class MOmessageQueueThread implements Runnable {

		boolean allDone = false;

		MOmessageQueueThread() {

			Thread t = new Thread(this);
			t.start();
		}

		public void run() {
			Work();
		}

		public void Work() {
			while (!allDone) {
				try {

					ArrayList<SmsMOMessage> currentMOmessages = receivedMOmessages;
					for (int i = 0; i < currentMOmessages.size(); i++) {

						if (moReceivingThread.activeCount() <= SmsConstants.SMS_MO_MAX_THREAD_COUNT) {
							SmsMOMessage smsMOmessage = currentMOmessages
									.get(i);
							receivedMOmessages.remove(smsMOmessage);

							new MOProcessThread(smsMOmessage, moReceivingThread);
						}

					}

					Thread.sleep(1000);

				} catch (Exception e) {

				}
			}
		}
	}

	private class MOProcessThread implements Runnable {

		boolean allDone = false;
		SmsMOMessage smsMOmessage;
		ThreadGroup threadGroup;

		MOProcessThread(SmsMOMessage smsMOmessage, ThreadGroup threadGroup) {
			this.smsMOmessage = smsMOmessage;
			this.threadGroup = threadGroup;
			Thread t = new Thread(threadGroup, this);
			t.start();
		}

		public void run() {
			Work();
		}

		public void Work() {

			LOG.info("Processing MO-Message queue of "
					+ receivedMOmessages.size() + " messages there is "
					+ moReceivingThread.activeCount() + " threads running");
			smsCore.processIncomingMessage(smsMOmessage.getSmsMessagebody(),
					smsMOmessage.getMobileNumber());
		}
	}

	private class DeliveryReportQueueThread implements Runnable {

		boolean allDone = false;

		DeliveryReportQueueThread() {

			Thread t = new Thread(this);
			t.start();
		}

		public void run() {
			Work();
		}

		public void Work() {
			while (!allDone) {
				try {

					ArrayList<DeliverSm> currentDeliveryReports = receivedDeliveryReports;
					for (int i = 0; i < currentDeliveryReports.size(); i++) {
						if (deliveryReportThreadGroup.activeCount() <= SmsConstants.SMS_DELIVERY_REPORT_MAX_THREAD_COUNT) {
							DeliverSm deliverSm = currentDeliveryReports.get(i);
							receivedDeliveryReports.remove(deliverSm);
							new DeliveryReportProcessThread(deliverSm,
									deliveryReportThreadGroup);
						}

					}

					Thread.sleep(1000);

				} catch (Exception e) {

				}
			}
		}
	}

	private class DeliveryReportProcessThread implements Runnable {

		boolean allDone = false;
		DeliverSm deliverSm;
		ThreadGroup threadGroup;

		DeliveryReportProcessThread(DeliverSm deliverSm, ThreadGroup threadGroup) {
			this.deliverSm = deliverSm;
			this.threadGroup = threadGroup;
			Thread t = new Thread(threadGroup, this);
			t.start();
		}

		public void run() {
			Work();
		}

		public void Work() {

			LOG.debug("Processing Delivery Report queue of "
					+ receivedDeliveryReports.size() + " messages");

			handelDeliveryReport(deliverSm);

		}
	}

	/**
	 * This listener will receive delivery reports as well as incoming messages
	 * from the smpp gateway. When we are binded to the gateway, this listener
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
		}

		public void onAcceptDeliverSm(DeliverSm deliverSm)
				throws ProcessRequestException {

			if (MessageType.SMSC_DEL_RECEIPT.containedIn(deliverSm
					.getEsmClass())) {

				receivedDeliveryReports.add(deliverSm);

			} else {
				LOG.info("Received MO message from: "
						+ deliverSm.getSourceAddr() + " adding it into queue.");
				SmsMOMessage moMessage = new SmsMOMessage();
				moMessage.setMobileNumber(deliverSm.getSourceAddr());
				String messageBody = "";
				if (deliverSm.getShortMessage() != null) {
					messageBody = new String(deliverSm.getShortMessage());
				} else {
					// persone sended a blank sms
					messageBody = SmsConstants.SMS_MO_EMPTY_REPLY_BODY;
				}
				moMessage.setSmsMessagebody(messageBody);
				receivedMOmessages.add(moMessage);
			}
		}
	}

	private void handelDeliveryReport(DeliverSm deliverSm) {
		try {

			DeliveryReceipt deliveryReceipt = deliverSm
					.getShortMessageAsDeliveryReceipt();

			// for future use.
			if (ALLOW_PROCESS_REMOTELY) {
				notifyDeliveryReportRemotely(deliveryReceipt);
				return;
			}
			LOG.info("Receiving delivery receipt for message '"
					+ deliveryReceipt.getId() + " ' from "
					+ deliverSm.getSourceAddr() + " to "
					+ deliverSm.getDestAddress() + " : " + deliveryReceipt);
			SmsMessage smsMessage = hibernateLogicLocator.getSmsMessageLogic()
					.getSmsMessageBySmscMessageId(deliveryReceipt.getId(),
							SmsConstants.SMSC_ID);
			if (smsMessage == null) {
				for (int i = 0; i < 5; i++) {
					LOG.warn("SMSC_DEL_RECEIPT retry " + i
							+ " out of 5 for messageSmscID"
							+ deliveryReceipt.getId());
					smsMessage = hibernateLogicLocator.getSmsMessageLogic()
							.getSmsMessageBySmscMessageId(
									deliveryReceipt.getId(),
									SmsConstants.SMSC_ID);
					if (smsMessage != null) {
						break;
					}
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			}
			if (smsMessage != null) {
				smsMessage.setSmscDeliveryStatusCode(smsDeliveryStatus
						.get((deliveryReceipt.getFinalStatus())));

				smsMessage
						.setDateDelivered(new Date(System.currentTimeMillis()));

				if (smsMessage.getStatusCode().equals(
						SmsConst_DeliveryStatus.STATUS_TIMEOUT)) {
					smsMessage
							.setStatusCode(SmsConst_DeliveryStatus.STATUS_LATE);

				} else {

					if (smsDeliveryStatus.get(deliveryReceipt.getFinalStatus()) != SmsConst_SmscDeliveryStatus.DELIVERED) {
						smsMessage
								.setStatusCode(SmsConst_DeliveryStatus.STATUS_FAIL);
					} else {
						smsMessage
								.setStatusCode(SmsConst_DeliveryStatus.STATUS_DELIVERED);
						hibernateLogicLocator.getSmsTaskLogic()
								.incrementMessagesDelivered(
										smsMessage.getSmsTask());
					}
				}
				hibernateLogicLocator.getSmsTaskLogic()
						.incrementMessagesProcessed(smsMessage.getSmsTask());
				hibernateLogicLocator.getSmsMessageLogic().persistSmsMessage(
						smsMessage);

			} else {
				LOG
						.error("Delivery report received for message not in database. MessageSMSCID="
								+ deliveryReceipt.getId());
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

			LOG.info("Binding to " + smsSmppProperties.getSMSCAddress()
					+ " on port " + smsSmppProperties.getSMSCPort()
					+ " with Username " + smsSmppProperties.getSMSCUsername());
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
				if (bindTest != null) {
					bindTest.allDone = true;
					bindTest = null;
				}
				gatewayBound = true;
				LOG.info("EnquireLinkTimer is set to "
						+ smsSmppProperties.getEnquireLinkTimeOut()
						+ " miliseconds");
				session.setEnquireLinkTimer(smsSmppProperties
						.getEnquireLinkTimeOut());

				gatewayBound = true;
				LOG.info("TransactionTimer is set to "
						+ smsSmppProperties.getTransactionTimer()
						+ " miliseconds");
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
							if (arg0.equals(SessionState.CLOSED)) {
								if (bindTest == null) {
									bindTest = new BindThread();
								}
							}
						}
					}
				});
				LOG.info("Bind successfull");
			} catch (Exception e) {
				LOG.error("Bind operation failed. " + e);
				gatewayBound = false;
				session.unbindAndClose();
				if (bindTest == null) {
					LOG.info("Starting Binding thread");
					bindTest = new BindThread();
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
		bindTest.allDone = true;
		bindTest = null;
		mOmessageQueueThread.allDone = true;
		mOmessageQueueThread = null;
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
					+ String.valueOf(smsSmppProperties.getSMSCPort()));
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
		connectToGateway();
		setupStatusBridge();
		LOG.debug("SmsSmpp implementation is started");
	}

	public void destroy() {
		LOG.debug("Attempting to shut down SmsSmpp");
		disconnectGateWay();
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

		try {
			smsSmppProperties = hibernateLogicLocator.getExternalLogic()
					.getSmppProperties();
			if (smsSmppProperties == null) {
				smsSmppProperties = new SmsSmppProperties();

				smsSmppProperties.setSMSCAddress(properties.getProperty(
						"SMSCAddress").trim());
				try {
					smsSmppProperties.setSMSCPort(Integer.parseInt(properties
							.getProperty("SMSCPort").trim()));
					if (smsSmppProperties.getSMSCPort() <= 0) {

						throw new PropertyZeroOrSmallerException("SMSCPort");
					}

				} catch (PropertyZeroOrSmallerException e) {
					LOG.error(e);
					LOG.warn("SMSC Port defaulting to port "
							+ SmsSmppProperties.DEFAULT_SMSC_PORT);

					smsSmppProperties
							.setSMSCPort(SmsSmppProperties.DEFAULT_SMSC_PORT);

				} catch (NumberFormatException e) {

					LOG.error(e);
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
			smsSmppProperties.setSystemType(properties
					.getProperty("systemType").trim());
			smsSmppProperties.setServiceType(properties.getProperty(
					"serviceType").trim());
			smsSmppProperties.setSourceAddress(properties.getProperty(
					"sourceAddress").trim());
			smsSmppProperties.setSourceAddressNPI((Byte.parseByte(properties
					.getProperty("sourceAddressNPI").trim())));
			smsSmppProperties.setSourceAddressTON((Byte.parseByte(properties
					.getProperty("sourceAddressTON").trim())));

			smsSmppProperties.setDestAddressNPI((Byte.parseByte(properties
					.getProperty("destAddressNPI").trim())));
			smsSmppProperties.setDestAddressTON((Byte.parseByte(properties
					.getProperty("destAddressTON").trim())));

			smsSmppProperties.setProtocolId(Byte.parseByte(properties
					.getProperty("protocolId").trim()));
			smsSmppProperties.setPriorityFlag(Byte.parseByte(properties
					.getProperty("priorityFlag").trim()));
			smsSmppProperties.setReplaceIfPresentFlag(Byte.parseByte(properties
					.getProperty("replaceIfPresentFlag").trim()));
			smsSmppProperties.setSmDefaultMsgId(Byte.parseByte(properties
					.getProperty("smDefaultMsgId").trim()));

			try {

				smsSmppProperties.setEnquireLinkTimeOut(Integer
						.parseInt(properties.getProperty(
								"enquireLinkTimeOutSeconds").trim()) * 1000);
				if (smsSmppProperties.getEnquireLinkTimeOut() <= 0) {

					throw new PropertyZeroOrSmallerException(
							"EnquireLinkTimeOut");
				}
			} catch (PropertyZeroOrSmallerException e) {
				LOG.error(e);
				LOG.warn("EnquireLinkTimeOut defaulting to  "
						+ SmsSmppProperties.DEFAULT_ENQUIRELINK_TIMEOUT
						+ " seconds");

				smsSmppProperties
						.setEnquireLinkTimeOut(SmsSmppProperties.DEFAULT_ENQUIRELINK_TIMEOUT * 1000);

			} catch (NumberFormatException e) {
				LOG.error(e);
				LOG.warn("EnquireLinkTimeOut defaulting to  "
						+ SmsSmppProperties.DEFAULT_ENQUIRELINK_TIMEOUT
						+ " seconds");

				smsSmppProperties
						.setEnquireLinkTimeOut(SmsSmppProperties.DEFAULT_ENQUIRELINK_TIMEOUT * 1000);
			}
			try {
				smsSmppProperties.setBindThreadTimer(Integer
						.parseInt(properties.getProperty(
								"bindThreadTimerSeconds").trim()) * 1000);
				if (smsSmppProperties.getBindThreadTimer() <= 0) {

					throw new PropertyZeroOrSmallerException("BindThreadTimer");
				}
			} catch (PropertyZeroOrSmallerException e) {
				LOG.error(e);
				LOG.warn("BindThreadTimer defaulting to  "
						+ SmsSmppProperties.DEFAULT_BINDTHREAD_TIMER
						+ " seconds");

				smsSmppProperties
						.setBindThreadTimer((SmsSmppProperties.DEFAULT_BINDTHREAD_TIMER) * 1000);

			} catch (NumberFormatException e) {
				LOG.error(e);
				LOG.warn("BindThreadTimer defaulting to  "
						+ SmsSmppProperties.DEFAULT_BINDTHREAD_TIMER
						+ " seconds");

				smsSmppProperties
						.setBindThreadTimer(SmsSmppProperties.DEFAULT_BINDTHREAD_TIMER * 1000);
			}

			smsSmppProperties.setAddressRange(properties.getProperty(
					"addressRange").trim());

			try {
				smsSmppProperties.setTransactionTimer(Integer
						.parseInt(properties.getProperty("transactionTimer")
								.trim()) * 1000);
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
				LOG.warn("SendingDelay defaulting to  "
						+ SmsSmppProperties.DEFAULT_SENDING_DELAY
						+ " milliseconds");

				smsSmppProperties
						.setSendingDelay(SmsSmppProperties.DEFAULT_SENDING_DELAY);

			} catch (NumberFormatException e) {
				LOG.error(e);
				LOG.warn("SendingDelay defaulting to  "
						+ SmsSmppProperties.DEFAULT_SENDING_DELAY
						+ " milliseconds");

				smsSmppProperties
						.setSendingDelay(SmsSmppProperties.DEFAULT_SENDING_DELAY);
			}

		}

		catch (Exception e) {
			LOG.error("Failed to load all properties", e);
		}

	}

	private void loadPropertiesFile() {

		try {
			InputStream is = this.getClass().getResourceAsStream(
					"/smpp.properties");
			if (is != null) {
				properties.load(is);
			} else {
				properties.load((new FileInputStream("smpp.properties")));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send a list of Bulk messages to the gateway. This method is not
	 * implemented because it does not return a list of message id's. The
	 * optional parameters are causing an exception.
	 */
	public SmsMessage[] sendBulkMessagesToGateway(SmsMessage[] messages) {

		String messageBody = "";
		Address[] addresses = new Address[messages.length];
		String[] mobileNumbers = new String[messages.length];
		for (int i = 0; i < messages.length; i++) {
			messageBody = messages[i].getMessageBody();
			mobileNumbers[i] = messages[i].getMobileNumber();
			addresses[i] = new Address(TypeOfNumber.valueOf(smsSmppProperties
					.getDestAddressTON()), NumberingPlanIndicator
					.valueOf(smsSmppProperties.getDestAddressNPI()),
					messages[i].getMobileNumber());
		}
		try {
			SubmitMultiResult submitMultiResult = session.submitMultiple(
					smsSmppProperties.getServiceType(), TypeOfNumber
							.valueOf(smsSmppProperties.getSourceAddressTON()),
					NumberingPlanIndicator.valueOf(smsSmppProperties
							.getSourceAddressNPI()), smsSmppProperties
							.getSourceAddress(), addresses, new ESMClass(),
					smsSmppProperties.getProtocolId(), smsSmppProperties
							.getPriorityFlag(), timeFormatter
							.format(new Date()), null, new RegisteredDelivery(
							SMSCDeliveryReceipt.SUCCESS_FAILURE),
					new ReplaceIfPresentFlag(smsSmppProperties
							.getReplaceIfPresentFlag()), new GeneralDataCoding(
							false, true, MessageClass.CLASS1,
							Alphabet.ALPHA_DEFAULT), smsSmppProperties
							.getSmDefaultMsgId(), messageBody.getBytes(), null);

		} catch (PDUException e) {
			LOG.error(e);

		} catch (ResponseTimeoutException e) {
			LOG.error(e);

		} catch (InvalidResponseException e) {
			LOG.error(e);

		} catch (NegativeResponseException e) {
			LOG.error(e);

		} catch (IOException e) {
			LOG.error(e);

		} catch (NullPointerException e) {
			e.printStackTrace();
			LOG.error(e);
		}
		return messages;
	}

	/**
	 * Send a list of messages one-by-one to the gateway. Abort if the gateway
	 * connection is down or when gateway returns an error and mark relevant
	 * messages as failed. Return message statuses (not reports) back to caller.
	 * 
	 * @return
	 */
	public String sendMessagesToGateway(Set<SmsMessage> messages) {
		String status = null;
		if (!gatewayBound) {
			return SmsConst_DeliveryStatus.STATUS_RETRY;

		}

		for (SmsMessage message : messages) {
			if (!gatewayBound) {
				return (SmsConst_DeliveryStatus.STATUS_INCOMPLETE);

			}
			try {
				Thread.sleep(smsSmppProperties.getSendingDelay());
			} catch (InterruptedException e) {
				LOG.error(e);
			}
			message = sendMessageToGateway(message);
			if (!message.getStatusCode().equals(
					SmsConst_DeliveryStatus.STATUS_SENT)) {
				status = (SmsConst_DeliveryStatus.STATUS_INCOMPLETE);
			}

		}
		if (status == null) {
			return (SmsConst_DeliveryStatus.STATUS_SENT);
		} else {
			return status;
		}
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
			LOG.error("Sms Gateway is not bound sending failed");
			message.setFailReason("Sms Gateway is not bound");
			message.setStatusCode(SmsConst_DeliveryStatus.STATUS_ERROR);
			return message;
		}
		String messageText = message.getSmsTask().getMessageBody();

		if (message.getSmsTask().getMessageTypeId().equals(
				SmsConstants.MESSAGE_TYPE_MOBILE_ORIGINATING)) {
			messageText = message.getMessageReplyBody();
		}

		// Continue to send message to gateway.
		try {
			if (messageText == null) {
				throw new NullPointerException(
						"SMS Message body text may not be empty.");
			}
			String messageId = session.submitShortMessage(smsSmppProperties
					.getServiceType(), TypeOfNumber.valueOf(smsSmppProperties
					.getSourceAddressTON()), NumberingPlanIndicator
					.valueOf(smsSmppProperties.getSourceAddressNPI()),
					smsSmppProperties.getSourceAddress(), TypeOfNumber
							.valueOf(smsSmppProperties.getSourceAddressTON()),
					NumberingPlanIndicator.valueOf(smsSmppProperties
							.getSourceAddressNPI()), message.getMobileNumber(),
					new ESMClass(), smsSmppProperties.getProtocolId(),
					smsSmppProperties.getPriorityFlag(), timeFormatter
							.format(new Date()), null, new RegisteredDelivery(
							SMSCDeliveryReceipt.SUCCESS_FAILURE),
					smsSmppProperties.getReplaceIfPresentFlag(),
					new GeneralDataCoding(false, true, MessageClass.CLASS1,
							Alphabet.ALPHA_DEFAULT), smsSmppProperties
							.getSmDefaultMsgId(), messageText.getBytes());
			message.setSmscMessageId(messageId);

			message.setSubmitResult(true);
			message.setSmscId(SmsConstants.SMSC_ID);
			message.setStatusCode(SmsConst_DeliveryStatus.STATUS_SENT);
			message
					.setSmscDeliveryStatusCode(SmsConst_SmscDeliveryStatus.ENROUTE);

			LOG.info("Message submitted, smsc_id = " + messageId
					+ " MessageID = " + message.getId() + " TaskID = "
					+ message.getSmsTask().getId());
		} catch (PDUException e) {
			// Invalid PDU parameter
			message.setFailReason(e.getMessage());
			message.setStatusCode(SmsConst_DeliveryStatus.STATUS_ERROR);
			hibernateLogicLocator.getSmsTaskLogic().incrementMessagesProcessed(
					message.getSmsTask());

			LOG.error(e);

		} catch (ResponseTimeoutException e) {
			// Response timeout
			message.setFailReason(e.getMessage());
			message.setStatusCode(SmsConst_DeliveryStatus.STATUS_ERROR);
			hibernateLogicLocator.getSmsTaskLogic().incrementMessagesProcessed(
					message.getSmsTask());
			LOG.error(e);

		} catch (InvalidResponseException e) {
			// Invalid response
			message.setFailReason(e.getMessage());
			message.setStatusCode(SmsConst_DeliveryStatus.STATUS_ERROR);
			hibernateLogicLocator.getSmsTaskLogic().incrementMessagesProcessed(
					message.getSmsTask());
			LOG.error(e);

		} catch (NegativeResponseException e) {
			// Receiving negative response (non-zero command_status)
			message.setFailReason(e.getMessage());
			message.setStatusCode(SmsConst_DeliveryStatus.STATUS_ERROR);
			hibernateLogicLocator.getSmsTaskLogic().incrementMessagesProcessed(
					message.getSmsTask());

		} catch (IOException e) {
			message.setFailReason(e.getMessage());
			message.setStatusCode(SmsConst_DeliveryStatus.STATUS_ERROR);
			hibernateLogicLocator.getSmsTaskLogic().incrementMessagesProcessed(
					message.getSmsTask());
			LOG.error(e);

		}
		hibernateLogicLocator.getSmsMessageLogic().persistSmsMessage(message);
		return message;
	}

	public void setLogLevel(Level level) {
		LOG.setLevel(level);

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
			String line;
			while ((line = rd.readLine()) != null) {
				// Process line...
			}
			wr.close();
			rd.close();

		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public boolean notifyDeliveryReportRemotely(SmsMessage smsMessage) {
		// TODO To be discussed with UCT
		return false;
	}

	/**
	 * Counts all the acctive threads in a threadGroup
	 * 
	 * @param threadgroup
	 * @return
	 */
	private int getThreadCount(ThreadGroup threadgroup) {
		return threadgroup.activeCount();

	}
}
