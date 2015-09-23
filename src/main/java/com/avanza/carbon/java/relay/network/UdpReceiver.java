package com.avanza.carbon.java.relay.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdpReceiver {

	Logger log = LoggerFactory.getLogger(UdpReceiver.class);
	
	private final int port;
	private DatagramSocket serverSocket;
	private byte[] receiveData = new byte[512];
	DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	private Consumer<String> packetConsumer;
	
	private Thread t = new Thread(() -> {
		while (!Thread.interrupted()) {
			try {
				serverSocket.receive(receivePacket);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			String s = new String(receiveData, receivePacket.getOffset(), receivePacket.getLength());
			log.debug("Recived packet: {}", s);
			packetConsumer.accept(s);
		}
	});

	public UdpReceiver(int port, Consumer<String> packetConsumer) {
		t.setName("udp-receiver");
		this.packetConsumer = packetConsumer;
		try {
			serverSocket = new DatagramSocket(port);
			this.port = serverSocket.getLocalPort();
			log.info("Listening on port {}", port);
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
	}

	public void start() {
		t.start();
	}
	
	public void stop() {
		t.interrupt();
		try {
			t.join();
		} catch (InterruptedException e) {
		}
	}

	public int getPort() {
		return port;
	}

}
