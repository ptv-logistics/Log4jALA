# Log4jALA

Log4j appender fo Azure Log Analytics (ALA)... sending data to Azure Log Analytics with the [HTTP Data Collector API](https://docs.microsoft.com/en-us/azure/log-analytics/log-analytics-data-collector-api).
The data will also be logged/sent asynchronously for high performance and to avoid blocking the caller thread.

## Get it

You can obtain this project as a [Maven Artifact](https://repo1.maven.org/maven2/com/github/ptv-logistics/log4jala) **coming soon**

```xml
	<dependency>
		<groupId>com.github.ptv-logistics</groupId>
		<artifactId>log4jala</artifactId>
		<version>1.0.0-SNAPSHOT</version>
	</dependency>
``` 

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



## Issues

Keep in mind that this library won't assure that your JSON payloads are being indexed, it will make sure that the HTTP Data Collection API [responds an Accept](https://azure.microsoft.com/en-us/documentation/articles/log-analytics-data-collector-api/#return-codes) but there is no way (right now) to know when has the payload been indexed completely... see also [SLA for Log Analytics](https://azure.microsoft.com/en-gb/support/legal/sla/log-analytics/v1_1/)
