package com.github.ptvlogistics.log4jala;

import java.util.HashMap;

import org.apache.log4j.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
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
}
