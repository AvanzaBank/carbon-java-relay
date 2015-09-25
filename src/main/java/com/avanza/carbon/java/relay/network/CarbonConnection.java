/*
 * Copyright 2015 Avanza Bank AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.avanza.carbon.java.relay.network;

import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connection to a carbon-cache instance. Is responsible for reconnection when a
 * connection is lost.
 * 
 * Currently data is discarded if an error occurs while sending.
 * 
 * @author Kristoffer Erlandsson
 */
public class CarbonConnection {

	private static final int RECONNECTION_DELAY = 3000;

	private final Logger log = LoggerFactory.getLogger(CarbonConnection.class);

	private String host;
	private int port;
	private Socket socket;

	private int reconnectionDelay;

	public CarbonConnection(String host, int port) {
		this(host, port, RECONNECTION_DELAY);
	}
	
	public CarbonConnection(String host, int port, int reconnectionDelay) {
		this.host = Objects.requireNonNull(host);
		this.port = port;
		if (reconnectionDelay < 0) {
			throw new IllegalArgumentException("reconnectionDelay must be >= 0");
		}
		this.reconnectionDelay = reconnectionDelay;
	}

	public void send(String msg) {
		send(new byte[0], msg);
	}

	public void send(byte[] header, String msg) {
		for (boolean connected = ensureConnected(); !connected; connected = ensureConnected()) {
			sleep();
		}
		doSend(header, msg);
	}

	private void sleep() {
		try {
			Thread.sleep(reconnectionDelay);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private void doSend(byte[] header, String msg) {
		try {
			socket.getOutputStream().write(header);
			socket.getOutputStream().write(msg.getBytes());
		} catch (IOException e) {
			log.warn("Exception when sending, this message: '" + msg + "', discarding:", e);
			close();
		}
	}

	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			log.debug("Exception when closing: ", e);
		}
		socket = null;
	}

	private boolean ensureConnected() {
		if (socket == null) {
			try {
				socket = new Socket(host, port);
				log.info("Connected to {}:{}", host, port);
			} catch (Exception e) {
				log.warn("Failed to connect", e);
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return "CarbonConnection [host=" + host + ", port=" + port + "]";
	}

}
