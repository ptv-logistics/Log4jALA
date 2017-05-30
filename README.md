# Log4jALA

Log4j appender fo Azure Log Analytics (ALA)... sending data to Azure Log Analytics with the [HTTP Data Collector API](https://docs.microsoft.com/en-us/azure/log-analytics/log-analytics-data-collector-api).
The data will also be logged/sent asynchronously for high performance and to avoid blocking the caller thread.

## Get it

Requirements
------------

* Java 1.8+


Maven projects
--------------

If your project is building using **Apache Maven 2** or above, put this artifact in your `pom.xml`
to import the jar:

You can obtain this project as [Maven SNAPSHOT](https://oss.sonatype.org/content/repositories/snapshots/com/github/ptv-logistics/log4jala/1.0.3-SNAPSHOT/)

```xml
	<dependency>
		<groupId>com.github.ptv-logistics</groupId>
		<artifactId>log4jala</artifactId>
		<version>1.0.3-SNAPSHOT</version>
	</dependency>
``` 

or as [Maven RELEASE](https://oss.sonatype.org/content/repositories/releases/com/github/ptv-logistics/log4jala/1.0.2/) 

```xml
	<dependency>
		<groupId>com.github.ptv-logistics</groupId>
		<artifactId>log4jala</artifactId>
		<version>1.0.2</version>
	</dependency>
``` 


The repository necessary to get the artifact is:

```xml
    <repository>
            <id>central</id>
			<name>Central Repository</name>
            <url>https://oss.sonatype.org/content/repositories/releases</url>
    </repository>
```

SNAPSHOT

```xml
    <repository>
            <id>centralsnaphot</id>
			<name>Central SNAPSHOT Repository</name>
            <url>https://oss.sonatype.org/content/repositories/snapshots</url>
    </repository>
```


Dependencies 
------------

e.g. to use the appender in a servlet container e.g. tomcat please copy the following jar files to the lib dir:

*  log4jala-*.jar mentioned above
* [log4j-1.2.17.jar](https://repo1.maven.org/maven2/log4j/log4j/1.2.17/log4j-1.2.17.jar)
* [jackson-databind-2.8.8.1.jar](https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.8.8.1/jackson-databind-2.8.8.1.jar)
* [jackson-annotations-2.8.0.jar](https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.8.0/jackson-annotations-2.8.0.jar)
* [jackson-core-2.8.8.jar](https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.8.8/jackson-core-2.8.8.jar)
* [httpclient-4.5.3.jar](https://repo1.maven.org/maven2/org/apache/httpcomponents/httpclient/4.5.3/httpclient-4.5.3.jar)
* [commons-lang3-3.5.jar](https://repo1.maven.org/maven2/org/apache/commons/commons-lang3/3.5/commons-lang3-3.5.jar)
* [commons-codec-1.9.jar](https://repo1.maven.org/maven2/commons-codec/commons-codec/1.9/commons-codec-1.9.jar)
* [commons-logging-1.2.jar](https://repo1.maven.org/maven2/commons-logging/commons-logging/1.2/commons-logging-1.2.jar)
* [httpcore-4.4.6.jar](https://repo1.maven.org/maven2/org/apache/httpcomponents/httpcore/4.4.6/httpcore-4.4.6.jar)



Or reference it and use it according to the [License](./LICENSE).

## Use it

This example is also available as a [AppTest.java](https://github.com/ptv-logistics/Log4jALA/blob/master/src/test/java/com/github/ptvlogistics/log4jala/AppTest.java):

```java

...
...
    public void testApp()
    {
		try {

			Logger logger = Logger.getLogger("Log4jALALogger");

			for (int i = 0; i < 100; i++) {
				HashMap<String, Object> logMessage = new HashMap<String, Object>();
				logMessage.put("id", String.format("log-%d", i));
				logMessage.put("message", String.format("test-%d", i));
				logger.info(logMessage);
			}

			Thread.sleep((long) 20000);
			assertTrue( true );
		} catch (InterruptedException e) {
			assertTrue( false );
			e.printStackTrace();
		}

    }
...
...


``` 

## Example Log4j Configuration file

This configuration is also available as a [log4j.xml](https://github.com/ptv-logistics/Log4jALA/blob/master/src/test/resources/log4j.xml):


```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE log4j:configuration SYSTEM "log4j.dtd">
<log4j:configuration>
 
    <appender name="Log4jALAAppender" class="com.github.ptvlogistics.log4jala.Log4jALAAppender">
      <param name="workspaceId" value="YOUR_WORKSPACE_ID"/>
      <param name="sharedKey" value="YOUR_SHARED_KEY"/>
      <param name="logType" value="YOUR_LOG_TYPE"/>
	  <!-- optional environment: e.g. vm/computer-name or ip
      <param name="environment" value="YOUR_VM_IP_COMPUTERNAME"/>
	  -->
 	  <!-- optional environment: e.g. vm/computer-name or ip
      <param name="component" value="YOUR_COMPONENT_NAME"/>
 	  -->
 	  <!-- optional version of the component
      <param name="version" value="YOUR_COMPONENT_VERSION"/>
 	  -->
      <!-- optional proxy host
      <param name="proxyHost" value="YOUR_PROXY_HOST"/>
	  -->
      <!-- optional proxy port
      <param name="proxyPort" value="YOUR_PROXY_PORT"/>
	  -->
      <!-- optional appendLogger to enable/disable sending the logger info 
           to Azure Log Analytics (default true)
      <param name="appendLogger" value="true"/>
	  -->
      <!-- optional appendLogLevel to enable/disable sending the log level
           to Azure Log Analytics (default true)
      <param name="appendLogLevel" value="true"/>
	  -->

      <param name="azureApiVersion" value="2016-04-01"/>
      <filter class="org.apache.log4j.varia.LevelRangeFilter">
        <param name="levelMin" value="INFO"/>
        <param name="levelMax" value="FATAL"/>
      </filter>
   </appender>
 
  <logger name="Log4jALALogger" additivity="false">
    <appender-ref ref="Log4jALAAppender" />
  </logger>
   
</log4j:configuration>
``` 

or log4j property file:

```
log4j.appender.ala=com.github.ptvlogistics.log4jala.Log4jALAAppender
log4j.appender.ala.layout=org.apache.log4j.PatternLayout
log4j.appender.ala.workspaceId=YOUR_WORKSPACE_ID
log4j.appender.ala.sharedKey=YOUR_SHARED_KEY
log4j.appender.ala.logType=YOUR_LOG_TYPE
#optional environment: e.g. vm/computer-name or ip
#log4j.appender.ala.environment=IAAXSEUWE000002
#optional component: e.g. API or MW
#log4j.appender.ala.component=API
#optional version of the component
#log4j.appender.ala.version=1.0.4
#optional proxy host
#log4j.appender.ala.proxyHost=YOUR_PROXY_HOST
#optional proxy port
#log4j.appender.ala.proxyPort=YOUR_PROXY_PORT
#optional appendLogger to enable/disable sending the logger info 
#to Azure Log Analytics (default true)
#log4j.appender.ala.appendLogger=true
#optional appendLogLevel to enable/disable sending the log level 
#to Azure Log Analytics (default true)
#log4j.appender.ala.appendLogLevel=true
...
...
log4j.logger.com.YOURPACKAGE=INFO,ala
log4j.rootLogger=ERROR,ala
```


## Issues

Keep in mind that this library won't assure that your JSON payloads are being indexed, it will make sure that the HTTP Data Collection API [responds an Accept](https://azure.microsoft.com/en-us/documentation/articles/log-analytics-data-collector-api/#return-codes) but there is no way (right now) to know when has the payload been indexed completely... see also [SLA for Log Analytics](https://azure.microsoft.com/en-gb/support/legal/sla/log-analytics/v1_1/)
