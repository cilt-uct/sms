ABOUT THE SMPP SERVICE
======================
The SMPP service is responsible for all communication to and from the gateway. 
If the gateway cannot be contacted, message delivery will fail and a retry will be made later on until a maximum 
retry attempt (currently 5) is reached. When delivery reports is received via the smpp listener, the corresponding message status 
will be updated in the sms_message table. If a message delivery report times out, the message will be marked as failed. Multiple gateway connections
is possible, if the gateway allows it. 


NOTES:
======
* Note that this service is currently in its own Eclipse project. It will be merged into the main project later on.
* Make sure to set the simulator IP and port before using the sms service. See SMSCport in smpp.properties and SMPP_PORT in smppsim.props
* In order to to complete task 31 (Integrate SMS service API) we had to write some of the core implementations in Delivery 4. 
  These are in a generic way so that we can test the smpp service thoroughly.
 

SETTING DATABASE INDEXES
========================

mysql_database_indexes.sql must be run after the initial build to create the indexes.If the script is not run performance issues might occur during high sms usage.


SETTING SMPP PROPERTIES
=======================
The smpp service can be configured by editing smpp.properties. This file contain gateway credentials and must therefore be stored in a 
secure location. The important settings are:

* SMSCaddress: The IP address of the gateway or simulator
* SMSCport: The port to use on the gateway.
* SMSCUsername: Username to bind with gateway
* SMSCPassword: Password to bind with gateway
* enquireLinkTimeOutSecondes: Interval in seconds  to poll the gateway to keep the session binded. 
* bindThreadTimerSeconds: Interval in seconds trying to reconnect if connection is down. 
* sourceAddress: The unique id of the Sakai instance to whom the gateway will reply


RUNNING THE UNIT TEST
============================
The test can be run from Eclipse or via the command line with: mvn test -Dtest=*
Also note that SmsHibernateConstants.SMS_DEV_MODE must be set to true when running these test. 

1.  Start up the smpp simulator: 
1.1 Build the sms_smpp_sim project by running: mvn clean install
1.2 Start simulator by running startsmppsim.bat or startsmppsim.sh
1.3 The simulator can also be started on a separate machine
1.4 See the README.TXT in the sms_smpp_sim project

2.1 Build the sms_smpp project: mvn clean install
2.2 Check that the simulator and test are running on the same port:
2.3 See SMSCport in smpp.properties and SMPP_PORT in smppsim.props
2.4 Also make sure of the simulator ip address with SMSCaddress in smpp.properties 
2.5 Run SmppAPITest.java. This will test the basic smpp gateway API.
2.6 Run SmsSmppImplTest.java to test the sending of messages to gateway.
2.7 Run SmppThreadingTest.java to test concurrent gateway connections with concurrent sending of messages.
