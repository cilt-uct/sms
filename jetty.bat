xcopy stubs\target\classes\org\sakaiproject\sms\*   tool\target\sms_ui\WEB-INF\classes\org\sakaiproject\sms\* /e /y
cd tool

rem make sure the new xml files is newer that that in the jar, also they will be overwritten
touch test-resources\*.xml

copy test-resources\web.xml target\sms_ui\WEB-INF\web.xml /y
copy test-resources\components.xml target\sms_ui\WEB-INF\components.xml /y 


mvn -o -Dmaven.test.skip=true jetty:run-exploded