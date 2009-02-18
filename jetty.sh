cp -r stubs/target/classes/org tool/target/sms_ui/WEB-INF/classes
cp pack/src/webapp/WEB-INF/components.xml tool/target/sms_ui/WEB-INF/components.xml 
cd tool
cp test-resources/web.xml target/sms_ui/WEB-INF/web.xml
cp test-resources/external.xml target/sms_ui/WEB-INF/external.xml 
cp test-resources/spring-hibernate.xml target/sms_ui/WEB-INF/spring-hibernate.xml
cp test-resources/sakai-hibernate.xml target/sms_ui/WEB-INF/sakai-hibernate.xml
mvn -o -Dmaven.test.skip=true jetty:run-exploded