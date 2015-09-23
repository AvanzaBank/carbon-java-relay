package com.avanza.carbon.java.relay.util;

import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kristoffer Erlandsson
 */
public class LoggingUncaughtExceptionHandler implements UncaughtExceptionHandler {
	
	private static final Logger log = LoggerFactory.getLogger(LoggingUncaughtExceptionHandler.class);

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		log.error("Uncaught exception in thread " + t.getName(), e);
	}

}
