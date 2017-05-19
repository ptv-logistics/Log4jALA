package com.github.ptvlogistics.log4jala;

import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 * Test Log4jALAAppender
 *
 */
public class Test {

	public static void main(String[] args) {
		try {

			Logger logger = Logger.getLogger("Log4jALALogger");

			for (int i = 0; i < 100; i++) {
				HashMap<String, Object> logMessage = new HashMap<String, Object>();
				logMessage.put("id", String.format("log-%d", i));
				logMessage.put("message", String.format("test-%d", i));
				logger.info(logMessage);
			}

			Thread.sleep((long) 300000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
