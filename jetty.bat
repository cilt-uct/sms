cd tool
copy test-resources\web.xml target\sms_ui\WEB-INF\web.xml /y
mvn -o -Dmaven.test.skip=true jetty:run-exploded