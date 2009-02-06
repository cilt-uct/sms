cp -r stubs/target/classes/org tool/target/sms_ui/WEB-INF/classes
cd tool
cp test-resources/web.xml target/sms_ui/WEB-INF/web.xml
cp test-resources/components.xml target/sms_ui/WEB-INF/components.xml
mvnDebug -o -Dmaven.test.skip=true jetty:run-war