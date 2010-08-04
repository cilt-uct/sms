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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.logic.HibernateLogicLocator;
import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.external.ExternalMessageSending;
import org.sakaiproject.sms.logic.smpp.SmsCore;
import org.sakaiproject.sms.logic.smpp.SmsScheduler;
import org.sakaiproject.sms.model.SmsConfig;
import org.springframework.util.Assert;

public class SmsSchedulerImpl implements SmsScheduler {

	private SmsConfig smsConfig = null;

	public SmsConfig getSmsConfig() {
		return smsConfig;
	}

	public void setSmsConfig(SmsConfig smsConfig) {
		this.smsConfig = smsConfig;
	}

	private boolean schedulerEnabled = true;// for unit testing only

	private static final Log LOG = LogFactory.getLog(SmsSchedulerImpl.class);

	public SmsCore smsCore = null;

	public SmsSchedulerThread smsSchedulerThread = null;

	private ExternalLogic externalLogic;

	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	private HibernateLogicLocator hibernateLogicLocator = null;

	public HibernateLogicLocator getHibernateLogicLocator() {
		return hibernateLogicLocator;
	}

	public void setHibernateLogicLocator(
			HibernateLogicLocator hibernateLogicLocator) {
		this.hibernateLogicLocator = hibernateLogicLocator;
	}

	public ExternalMessageSending externalMessageSending;	
	public void setExternalMessageSending(
			ExternalMessageSending externalMessageSending) {
		this.externalMessageSending = externalMessageSending;
	}
	
	public void init() {
		Assert.notNull(smsCore);
		setSmsConfig(hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSystemSmsConfig());
		LOG.info("externalLogic: " + externalMessageSending);
		if (externalLogic.isNodeBindToGateway() || externalMessageSending != null) {
			smsSchedulerThread = new SmsSchedulerThread();
			LOG.info("init() - scheduler started");
		} else {	
			LOG.info("init() - this node not binding to SMPP gateway, scheduler NOT started");
		}
	}

	public void destroy() {
		smsSchedulerThread.stopScheduler = true;
		smsSchedulerThread.stop();
		smsSchedulerThread = null;

	}

	public SmsCore getSmsCore() {
		return smsCore;
	}

	public void setSmsCore(SmsCore smsCore) {
		this.smsCore = smsCore;
	}

	private class SmsSchedulerThread implements Runnable {

		public boolean stopScheduler = false;

		private Thread thread;

		public Thread getThread() {
			return thread;
		}

		public void setThread(Thread thread) {
			this.thread = thread;
		}

		SmsSchedulerThread() {
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

			while (!stopScheduler) {

				// Run scheduled processes
				try {
					smsCore.processMOTasks();
					smsCore.processSOTasks();
					smsCore.processTimedOutDeliveryReports();
					smsCore.checkAndSetTasksCompleted();
					smsCore.adjustLateDeliveryBilling();
				} catch (Exception e) {
					LOG.error("SoScheduler encountered an error : "
							+ e.getMessage(), e);
				}
				
				// Pause a while
				try {
					Thread.sleep(getSmsConfig().getSchedulerInterval() * 1000L);
				} catch (InterruptedException e) {
					; // ignore
				}
				
			}
		}
	}

	public void setInterval(int seconds) {
		getSmsConfig().setSchedulerInterval(seconds);
	}

	public void startSmsScheduler() {
		smsSchedulerThread = new SmsSchedulerThread();

	}

	public void stopSmsScheduler() {
		smsSchedulerThread.stopScheduler = true;

	}

	public boolean isSchedulerEnabled() {
		return schedulerEnabled;
	}

	public void setSchedulerEnabled(boolean schedulerEnabled) {
		this.schedulerEnabled = schedulerEnabled;
	}

}
