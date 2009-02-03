cd tool
copy test-resources\web.xml target\sms_ui\WEB-INF\web.xml /y
mvnDebug -o -Dmaven.test.skip=true jetty:run-war