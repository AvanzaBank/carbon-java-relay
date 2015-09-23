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
