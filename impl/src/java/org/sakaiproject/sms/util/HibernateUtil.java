/***********************************************************************************
 * HibernateUtil.java
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
package org.sakaiproject.sms.util;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;

/**
 * Configures hibernate with mapping definitions and configuration properties
 * for use in the application. Implements a singleton for creating a hibernate
 * template that creates individual hibernate sessions on a per thread basis.
 * <p>
 * The hibernate session factory is initialised from the database mapping file
 * hibernate-mappings.hbm.xml and from the hibernate configuration properties,
 * hibernate.properties. Both files are expected to be available on the
 * classpath.
 * 
 * @author Julian Wyngaard
 * @version 1.0
 * @created 24-Nov-2008
 */
public class HibernateUtil {

	/**
	 * Hibernate mappings file name.
	 * 
	 * private static final String HIB_MAPPINGS_FILE_NAME =
	 * "/hibernate-mappings.hbm.xml";
	 */

	/**
	 * Hibernate configuration file name.
	 */
	private static final String HIB_PROPERTIES_FILE_NAME = "/hibernate.properties";

	/**
	 * The Constant HIB_TEST_PROPERTIES_FILE_NAME.
	 */
	private static final String HIB_TEST_PROPERTIES_FILE_NAME = "/hibernate-test.properties";

	/**
	 * Location of hibernate.cfg.xml file.
	 */
	private static String CONFIG_FILE_LOCATION = "hibernate.cfg.xml";

	/**
	 * Hibernate session factory - singleton.
	 */
	private static SessionFactory sessionFactory;

	private static Configuration configuration;
	/**
	 * Container for thread-scoped sessions.
	 */
	private static final ThreadLocal<Session> threadSession = new ThreadLocal<Session>();

	private static final ThreadLocal<Transaction> threadTransaction = new ThreadLocal<Transaction>();

	private static final ThreadLocal<Interceptor> threadInterceptor = new ThreadLocal<Interceptor>();

	/** The test configuration. */
	private static boolean testConfiguration = false;

	private static Log LOG = LogFactory.getLog(HibernateUtil.class);

	/**
	 * Loads the given properties file from the classpath.
	 * 
	 * @param file
	 *            file name and path (on the classpath)
	 * 
	 * @return properties initialised from the given file
	 * 
	 * @throws IOException
	 *             if any error occurs locating and/or reading the given file
	 *             from the classpath
	 */

	private static Properties loadPropertiesFromClasspath(String file)
			throws IOException {
		Properties properties = new Properties();
		properties.load(HibernateUtil.class.getResourceAsStream(file));

		return properties;
	}

	/**
	 * Gets the configuration.
	 * 
	 * @return the configuration
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private static Configuration getConfiguration() throws IOException {

		Configuration configuration = new Configuration();

		// load bean mappings
		configuration.configure(CONFIG_FILE_LOCATION);
		// load hibernate propeties
		Properties properties = null;
		if (testConfiguration) {
			properties = loadPropertiesFromClasspath(HIB_TEST_PROPERTIES_FILE_NAME);
		} else {
			properties = loadPropertiesFromClasspath(HIB_PROPERTIES_FILE_NAME);
		}
		configuration.setProperties(properties);

		return configuration;
	}

	/**
	 * Returns the single session factory in the utils. If no session factory
	 * exists, a new one is created.
	 * 
	 * @return hibernate session factory
	 * 
	 * @exception HibernateException
	 *                if any error occurs reading the hibernate configuration
	 *                files
	 */
	private static synchronized SessionFactory getSessionFactory() {
		try {
			if (sessionFactory == null) {
				// configuration = new Configuration();
				sessionFactory = getConfiguration().buildSessionFactory();
			}
			// We could also let Hibernate bind it to JNDI:
			// configuration.configure().buildSessionFactory()
		} catch (Throwable ex) {
			// We have to catch Throwable, otherwise we will miss
			// NoClassDefFoundError and other subclasses of Error

			throw new ExceptionInInitializerError(ex);
		}
		return sessionFactory;
	}

	/**
	 * Creates a database schema
	 */
	public static void createSchema() {
		try {
			new SchemaExport(getConfiguration()).create(false, true);
		} catch (IOException e) {
			e.printStackTrace();
			throw new HibernateException("Error reading hibernate properties: "
					+ e.getMessage(), e);
		}
	}

	/**
	 * Returns the hibernate session for the current thread.
	 * 
	 * @return the hibernate session for the current thread
	 * 
	 */
	public static Session getSession() {
		Session s = threadSession.get();
		try {
			if (s == null) {
				if (getInterceptor() != null) {
					s = getSessionFactory().openSession(getInterceptor());
				} else {
					s = getSessionFactory().openSession();
				}
				threadSession.set(s);
			}
		} catch (HibernateException ex) {
			LOG.error("HibernateException: " + ex);
		}
		return s;

	}

	/**
	 * Rebuild the SessionFactory with the given Hibernate Configuration.
	 * 
	 * @param cfg
	 */
	public static void rebuildSessionFactory(Configuration cfg) {

		synchronized (sessionFactory) {
			try {
				sessionFactory = cfg.buildSessionFactory();
				configuration = cfg;
			} catch (Exception ex) {
				LOG.error("HibernateException: " + ex);
			}
		}
	}

	public static void closeSession() {
		try {
			Session s = threadSession.get();
			threadSession.set(null);
			if (s != null && s.isOpen()) {
				s.close();
			}
		} catch (HibernateException ex) {
			LOG.error("HibernateException: " + ex);
		}

	}

	/**
	 * Sets the test configuration.
	 * 
	 * @param testConfiguration
	 *            the new test configuration
	 */
	public static void setTestConfiguration(boolean testConfiguration) {
		HibernateUtil.testConfiguration = testConfiguration;
	}

	/**
	 * Register a Hibernate interceptor with the current thread.
	 * <p>
	 * Every Session opened is opened with this interceptor after registration.
	 * Has no effect if the current Session of the thread is already open,
	 * effective on next close()/getSession().
	 */
	public static void registerInterceptor(Interceptor interceptor) {
		threadInterceptor.set(interceptor);
	}

	private static Interceptor getInterceptor() {
		Interceptor interceptor = threadInterceptor.get();
		return interceptor;
	}

	/**
	 * Start a new database transaction.
	 * 
	 */
	public static void beginTransaction() {
		Transaction tx = threadTransaction.get();
		try {
			if (tx == null) {
				tx = getSession().beginTransaction();
				threadTransaction.set(tx);
			}
		} catch (HibernateException ex) {
			LOG.error("HibernateException: " + ex);
		}
	}

	/**
	 * Commit the database transaction.
	 */
	public static void commitTransaction() {
		Transaction tx = threadTransaction.get();
		try {
			if (tx != null && !tx.wasCommitted() && !tx.wasRolledBack()) {
				tx.commit();
			}
			threadTransaction.set(null);
		} catch (HibernateException ex) {
			rollbackTransaction();
			LOG.error("HibernateException: " + ex);
		}
	}

	/**
	 * Rollback the database transaction.
	 */
	public static void rollbackTransaction() {
		Transaction tx = threadTransaction.get();
		try {
			threadTransaction.set(null);
			if (tx != null && !tx.wasCommitted() && !tx.wasRolledBack()) {
				tx.rollback();
			}
		} catch (HibernateException ex) {
			LOG.error("HibernateException: " + ex);
		} finally {
			closeSession();
		}
	}

	/**
	 * Reconnects a Hibernate Session to the current Thread.
	 * 
	 * @param session
	 *            The Hibernate Session to be reconnected.
	 */
	public static void reconnect(Session session) {
		try {
			session.reconnect();
			threadSession.set(session);
		} catch (HibernateException ex) {
			LOG.error("HibernateException: " + ex);
		}
	}

	/**
	 * Disconnect and return Session from current Thread.
	 * 
	 * @return Session the disconnected Session
	 */
	public static Session disconnectSession() {
		Session session = getSession();
		try {
			threadSession.set(null);
			if (session.isConnected() && session.isOpen())
				session.disconnect();
		} catch (HibernateException ex) {
			LOG.error("HibernateException: " + ex);
		}
		return session;
	}

}