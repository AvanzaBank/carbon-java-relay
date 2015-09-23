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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kristoffer Erlandsson
 */
public class CarbonConnection {

	private final Logger log = LoggerFactory.getLogger(CarbonConnection.class);

	private String host;
	private int port;
	private Socket socket;

	public CarbonConnection(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void send(String msg) {
		send(new byte[0], msg);
	}
	
	public void send(byte[] header, String msg) {
		// TODO delay before reconnecting to avoid super frequent connects when the connection is down.
		boolean connected = ensureConnected();
		while (!connected) {
			ensureConnected();
		}
		doSend(header, msg);
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
