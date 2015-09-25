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
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @author Kristoffer Erlandsson
 */
public class NetworkTestUtils {

	public static void sendUdpMessages(String host, int port, String ... msgs) {
		try (DatagramSocket clientSocket = new DatagramSocket()) {
			Arrays.stream(msgs).forEach(m -> trySendUdpMessage(host, port, clientSocket, m));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	private static void trySendUdpMessage(String host, int port, DatagramSocket socket, String msg){
		try {
			socket.send(createPacket(host, port, msg));
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


	public static void sendTcpMessages(String host, int tcpPort, String ... msgs) {
		try (Socket socket = new Socket(host, tcpPort)) {
			Arrays.stream(msgs).forEach(m -> trySendTcpMessage(socket, m));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static void trySendTcpMessage(Socket socket, String msg) {
		try {
			PrintWriter pw = new PrintWriter(socket.getOutputStream());
			pw.print(msg);
			pw.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
}
