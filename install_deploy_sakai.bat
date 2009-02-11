rem cd c:\java\projects\sakai_2.6_min\

call mvn -Dmaven.test.failure.ignore=true clean install sakai:deploy -Dmaven.tomcat.home=C:\java\apache-tomcat-5.5.27
pause