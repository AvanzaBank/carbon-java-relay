package com.avanza.carbon.java.relay.testutil;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Simple test class for some load testing.
 * 
 * @author Kristoffer Erlandsson (krierl)
 */
public class MessageFlooder {
	
	private static Random random = new Random();

	public static void main(String[] args) throws Exception {
		ExecutorService pool = Executors.newCachedThreadPool();
		for (int i = 0; i < 1; i++) {
			pool.execute(() -> send());
		}
	}

	private static void send() {
		try {
			trySend();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void trySend() throws Exception {
		DatagramSocket socket = new DatagramSocket();
		InetAddress adress = InetAddress.getByName("localhost");
		int port = 55555;
		int count = 0;
		for (;;) {
			Thread.sleep(10);
			count++;
			if (count % 10000 == 0) {
				System.out.print(".");
			}
			if (count % 100000 == 0) {
				System.out.println();
			}
			long time = System.currentTimeMillis() / 1000;
			int val1 = random.nextInt(20);
			String key = String.format("krierltest2.val%s", val1);
			String msg = key + " " + random.nextInt(2000) + " " + time;
			byte[] messageBytes = msg.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(messageBytes, messageBytes.length, adress, port);
			socket.send(sendPacket);
		}
	}
}
