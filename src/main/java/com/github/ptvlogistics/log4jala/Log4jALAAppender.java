package com.github.ptvlogistics.log4jala;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

public class Log4jALAAppender extends AppenderSkeleton {

	private String workspaceId;
	private String sharedKey;
	private String logType;
	private String azureApiVersion;
	private String component;
	private String version;
	private String environment;
	private int threadPoolSize;
	private String proxyHost;
	private Integer proxyPort;
	private Boolean appendLogger;
	private Boolean appendLogLevel;
	private static HTTPDataCollector httpDataCollector;
	private LoggingEventSerializer serializer;

	public String getWorkspaceId() {
		return workspaceId;
	}

	public void setWorkspaceId(String workspaceId) {
		this.workspaceId = workspaceId;
	}

	public String getSharedKey() {
		return sharedKey;
	}

	public void setSharedKey(String sharedKey) {
		this.sharedKey = sharedKey;
	}

	public String getLogType() {
		return logType;
	}

	public void setLogType(String logType) {
		this.logType = logType;
	}

	public String getAzureApiVersion() {
		return azureApiVersion;
	}

	public void setAzureApiVersion(String azureApiVersion) {
		this.azureApiVersion = azureApiVersion;
	}

	public String getComponent() {
		return component;
	}

	public void setComponent(String component) {
		this.component = component;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public int getThreadPoolSize() {
		return threadPoolSize;
	}

	public void setThreadPoolSize(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public Integer getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(Integer proxyPort) {
		this.proxyPort = proxyPort;
	}
	
	public boolean getAppendLogger() {
		return appendLogger == null ? true : appendLogger;
	}

	public void setAppendLogger(boolean appendLogger) {
		this.appendLogger = appendLogger;
	}

	public boolean getAppendLogLevel() {
		return appendLogLevel == null ? true : appendLogLevel;
	}

	public void setAppendLogLevel(boolean appendLogLevel) {
		this.appendLogLevel = appendLogLevel;
	}


	/**
	 * @see org.apache.log4j.AppenderSkeleton#activateOptions()
	 */
	@Override
	public void activateOptions() {
		try {
			// Close previous connections if reactivating
			if (httpDataCollector != null) {
				close();
			}

			if (StringUtils.isEmpty(workspaceId)) {
				throw new Exception(
						String.format("the Log4jALAAppender property workspaceId [%s] shouldn't be empty (log4j.xml)",
								this.workspaceId));
			}
			if (StringUtils.isEmpty(sharedKey)) {
				throw new Exception(String.format(
						"the Log4jALAAppender property sharedKey [%s] shouldn't be empty (log4j.xml)", this.sharedKey));
			}
			if (StringUtils.isEmpty(logType)) {
				throw new Exception(String.format(
						"the Log4jALAAppender property logType [%s] shouldn't be empty (log4j.xml)", this.logType));
			}

			serializer = new LoggingEventSerializer();

			httpDataCollector = new HTTPDataCollector(this.workspaceId, this.sharedKey,
					this.threadPoolSize <= 0 ? 1000 : this.threadPoolSize, this, this.proxyHost, this.proxyPort);

		} catch (Exception e) {
			errorHandler.error("Unexpected exception while initialising HTTPDataCollector.", e,
					ErrorCode.GENERIC_FAILURE);
		}
	}

	@Override
	protected void append(LoggingEvent loggingEvent) {
		try {
			if (httpDataCollector != null) {
				String content = serializer
						.serializeLoggingEvents(new ArrayList<LoggingEvent>(Arrays.asList(loggingEvent)), this);
				// Info(content);

				httpDataCollector.collect(logType, content,
						StringUtils.isEmpty(azureApiVersion) ? "2016-04-01" : azureApiVersion, "DateValue");
			} else {
				errorHandler.error("Couldn't append log message during the HTTPDataCollector isn't initialized.");
			}
		} catch (Exception ex) {
			logError(String.format("Unable to send data to Azure Log Analytics: %1s", ex.getMessage()), ex);
		}

	}

	@Override
	public void close() {

	}

	public void logError(String message, Exception e) {
		errorHandler.error(message, e, ErrorCode.GENERIC_FAILURE);

	}

	@Override
	public boolean requiresLayout() {
		return false;
	}
	
	@Override
	public void	setErrorHandler(ErrorHandler errorHandler) {
		super.setErrorHandler(errorHandler);
	}

	@Override
	public ErrorHandler	getErrorHandler() {
		return super.getErrorHandler();
	}

}
