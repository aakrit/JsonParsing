all: Server.class tomcat

Server.class:
	javac -cp /usr/local/Cellar/tomcat/7.0.42/libexec/lib/servlet-api.jar;~/Desktop/JavaLibs/google-gson-2.2.4/gson-2.2.4.jar JsonParsing/Server.java	

start:
	- catalina stop
	catalina start

tomcat:
	cp JsonParsing/*.class /usr/local/Cellar/tomcat/7.0.42/libexec/webapps/ROOT/WEB-INF/classes/JsonParsing/	

clean:
	rm JsonParsing/*.class 
