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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

public class NetworkTestUtils {

	public static void sendUdpMessage(String host, int port, String msg) {
		try (DatagramSocket clientSocket = new DatagramSocket()) {
			clientSocket.send(createPacket(host, port, msg));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static DatagramPacket createPacket(String host, int port, String msg) {
		byte[] messageBytes = msg.getBytes(Charset.forName("UTF-8"));
		try {
			return new DatagramPacket(messageBytes, messageBytes.length, InetAddress.getByName(host), port);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}
}
