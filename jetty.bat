@echo off

xcopy stubs\target\classes\org\sakaiproject\sms\*   tool\target\sms_ui\WEB-INF\classes\org\sakaiproject\sms\* /e /y
copy pack\src\webapp\WEB-INF\components.xml tool\target\sms_ui\WEB-INF\components.xml /y 

cd tool

rem make sure the new xml files is newer that that in the jar, also they will be overwritten
touch test-resources\web.xml
touch test-resources\external.xml
touch test-resources\spring-hibernate.xml
touch test-resources\sakai-hibernate.xml

copy test-resources\web.xml target\sms_ui\WEB-INF\web.xml /y
copy test-resources\external.xml target\sms_ui\WEB-INF\external.xml /y 
copy test-resources\spring-hibernate.xml target\sms_ui\WEB-INF\spring-hibernate.xml /y
copy test-resources\sakai-hibernate.xml target\sms_ui\WEB-INF\sakai-hibernate.xml /y


mvn  -Dmaven.test.skip=true jetty:run-exploded
