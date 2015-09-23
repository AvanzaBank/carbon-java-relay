package com.avanza.carbon.java.relay.network;

import java.io.IOException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
