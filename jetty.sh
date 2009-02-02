cd tool
cp test-resources/web.xml target/sms_ui/WEB-INF/web.xml 
mvn -o -Dmaven.test.skip=true jetty:run-exploded