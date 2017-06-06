package com.github.ptvlogistics.log4jala;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

public class HTTPDataCollector {

	private String workspaceId;
	private String sharedKey;
	private byte[] sharedKeyBytes;
	private ExecutorService executorService;
	private ObjectMapper jsonMapper = new ObjectMapper();
	private Log4jALAAppender appender;
	private String proxyHost;
	private Integer proxyPort;

	/**
	 * Wrapper for reporting custom JSON events to Azure Log Analytics
	 *
	 * @param workspaceId
	 *            the azure log analytics workspace id OMS Portal
	 *            Overview/Settings/Connected Sources
	 * @param sharedKey
	 *            the primary Key OMS Portal Overview/Settings/Connected Sources
	 * @param asyncThreadPoolSize
	 *            appender thread pool size for async execution
	 * @param appender
	 *            the appender to call the fail over error log method for
	 *            internal errors
	 */
	public HTTPDataCollector(String workspaceId, String sharedKey, int asyncThreadPoolSize, Log4jALAAppender appender,
			String proxyHost, Integer proxyPort) {
		this.appender = appender;
		if(executorService == null || executorService.isTerminated()){
			final ThreadFactory threadFactory = new ALAThreadFactoryBuilder()
			        .setNameFormat("AlaHTTDataCollector-Thread")
			        .setDaemon(true)
			        .build();
			executorService = Executors.newFixedThreadPool(asyncThreadPoolSize, threadFactory);
		}
		this.workspaceId = workspaceId;
		this.sharedKey = sharedKey;
		this.sharedKeyBytes = Base64.getDecoder().decode(this.sharedKey);
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
	}

	/**
	 * Collect a JSON log to Azure Log Analytics
	 *
	 * @param logType
	 *            Name of the Type of Log. Can be any name you want to appear on
	 *            Azure Log Analytics.
	 * @param objectToSerialize
	 *            Object to serialize and collect.
	 * @param apiVersion
	 *            Api Version
	 * @param timeGeneratedPropertyName
	 *            Name of the field in the json object which contains the
	 *            timestamp ISO 8601-Format (jjjj-mm-ttThh:mm:ssZ)
	 */
	public void collect(final String logType, final Object objectToSerialize, final String apiVersion,
			final String timeGeneratedPropertyName) {

		executorService.execute(new Runnable() {
			public void run() {
				try {
					sendHTTPDataCollectorAPIRequest(logType, jsonMapper.writeValueAsString(objectToSerialize),
							apiVersion, timeGeneratedPropertyName);
				} catch (Exception e) {
					appender.logError(
							"Unexpected exception while sending data to Azure Log Analytics by the HTTP Data Collector API.",
							e);
				}
			}
		});
	}

	/**
	 * Collect a JSON log to Azure Log Analytics
	 *
	 * @param logType
	 *            Name of the Type of Log. Can be any name you want to appear on
	 *            Azure Log Analytics.
	 * @param jsonPayload
	 *            OJSON string. Can be an array or single object.
	 * @param apiVersion
	 *            Api Version
	 * @param timeGeneratedPropertyName
	 *            Name of the field in the json object which contains the
	 *            timestamp ISO 8601-Format (jjjj-mm-ttThh:mm:ssZ)
	 * @throws Exception
	 *             throws exception
	 */
	public void collect(String logType, String jsonPayload, String apiVersion, String timeGeneratedPropertyName)
			throws Exception {
		executorService.execute(new Runnable() {
			public void run() {
				try {
					sendHTTPDataCollectorAPIRequest(logType, jsonPayload, apiVersion, timeGeneratedPropertyName);
				} catch (Exception e) {
					appender.logError(
							"Unexpected exception while sending data to Azure Log Analytics by the HTTP Data Collector API.",
							e);
				}
			}
		});

	}

	/**
	 * Send a JSON log to Azure Log Analytics
	 *
	 * @param logType
	 *            Name of the Type of Log. Can be any name you want to appear on
	 *            Azure Log Analytics.
	 * @param jsonPayload
	 *            OJSON string. Can be an array or single object.
	 * @param apiVersion
	 *            Api Version
	 * @param timeGeneratedPropertyName
	 *            Name of the field in the json object which contains the
	 *            timestamp ISO 8601-Format (jjjj-mm-ttThh:mm:ssZ)
	 * @throws Exception
	 *             throws exception
	 */
	public void sendHTTPDataCollectorAPIRequest(String logType, String jsonPayload, String apiVersion,
			String timeGeneratedPropertyName) throws Exception {
		CloseableHttpClient client = null;
		try {
			String url = "https://" + this.workspaceId + ".ods.opinsights.azure.com/api/logs?api-version=" + apiVersion;
			
			// Wed, 17 May 2017 15:47:51 GMT
			String rfcDate = java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneId.of("GMT")));
			
			//fixed RFC_1123_DATE_TIME date if the day of month hasn't a leading 0
			//to avoid a 403 forbidden during the ALA request
			String[] rfcSplitted = rfcDate.split("\\s+");
			if(rfcSplitted[1].length() == 1){
				rfcSplitted[1] = "0" + rfcSplitted[1];
				rfcDate = String.join(" ", rfcSplitted);
			}
			

			String signature = hashSignature("POST", jsonPayload.length(), "application/json", rfcDate, "/api/logs");

			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (certificate, authType) -> true)
					.build();

			if(this.proxyHost == null || this.proxyPort == null ){
				client = HttpClients.custom().setSSLContext(sslContext).setSSLHostnameVerifier(new NoopHostnameVerifier())
						.setRetryHandler((exception, executionCount,
								context) -> executionCount < (Integer) context.getAttribute("retry.count")).build();
			}else{
				client = HttpClients.custom().setSSLContext(sslContext).setSSLHostnameVerifier(new NoopHostnameVerifier())
						.setRetryHandler((exception, executionCount,
								context) -> executionCount < (Integer) context.getAttribute("retry.count"))
						.setProxy(new HttpHost(this.proxyHost, this.proxyPort))
						/*
						 * .setServiceUnavailableRetryStrategy(new
						 * ServiceUnavailableRetryStrategy() {
						 * 
						 * @Override public boolean retryRequest(final HttpResponse
						 * response, final int executionCount, final HttpContext
						 * context) { int statusCode =
						 * response.getStatusLine().getStatusCode(); return
						 * statusCode == 403 && executionCount < 5; }
						 * 
						 * @Override public long getRetryInterval() { return 0; } })
						 */
						.build();
			}

			HttpClientContext clientContext = HttpClientContext.create();
			clientContext.setAttribute("retry.count", 6);

			HttpPost httpPost = new HttpPost(url);
			
			StringEntity entity = new StringEntity(jsonPayload, "UTF8");
			entity.setContentType("application/json");
			httpPost.setEntity(entity);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");
			httpPost.setHeader("Log-Type", logType);
			httpPost.setHeader("x-ms-date", rfcDate);
			httpPost.setHeader("Authorization", signature);
			if (StringUtils.isNotEmpty(timeGeneratedPropertyName)) {
				httpPost.setHeader("time-generated-field", timeGeneratedPropertyName);
			}

			CloseableHttpResponse response = client.execute(httpPost);
			if (response.getStatusLine().getStatusCode() != 200) {
				appender.logError(String.format("Warn sendHTTPDataCollectorAPIRequest [%s] [%s]",response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()), new Exception("warn"));
			}

		} catch (Exception e) {
			appender.logError("Error sendHTTPDataCollectorAPIRequest", e);
		} finally {

			if (client != null) {
				client.close();
			}
	
		}
	}

	
	public void close() {
		if(executorService != null){
			executorService.shutdown();
			try {
			    if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
			        executorService.shutdownNow();
			    } 
			} catch (InterruptedException e) {
			    executorService.shutdownNow();
			}
		}
	}

	/**
	 * create SHA256 signature hash
	 *
	 */
	private String hashSignature(String method, int contentLength, String contentType, String date, String resource)
			throws Exception {
		String stringtoHash = method + "\n" + contentLength + "\n" + contentType + "\nx-ms-date:" + date + "\n"
				+ resource;
		byte[] bytesToHash = stringtoHash.getBytes(StandardCharsets.UTF_8);

		Mac sha256_HMAC = Mac.getInstance("HmacSHA256");

		SecretKeySpec secret_key = new SecretKeySpec(this.sharedKeyBytes, "HmacSHA256");
		sha256_HMAC.init(secret_key);

		byte[] calculatedHash = sha256_HMAC.doFinal(bytesToHash);
		String stringHash = Base64.getEncoder().encodeToString(calculatedHash);
		return "SharedKey " + this.workspaceId + ":" + stringHash;
	}

}
