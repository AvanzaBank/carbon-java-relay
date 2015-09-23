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

import static org.hamcrest.Matchers.*;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.Charset;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import com.avanza.carbon.java.relay.testutil.Poller;
import com.avanza.carbon.java.relay.testutil.Probe;

public class UdpReceiverTest {

	private volatile String receivedPacket = null;
	private UdpReceiver receiver;
	InetAddress address;
	DatagramSocket clientSocket;

	@Before
	public void setUp() {
		try {
			address = InetAddress.getByName("127.0.0.1");
			clientSocket = new DatagramSocket();
			receivedPacket = null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void sentMessageIsReceived() throws Exception {
		int port = startReceiver();
		clientSocket.send(createPacket(port, "test 12 12"));
		assertEventually(equalTo("test 12 12"));
	}
	
	@Test
	public void twoMessageAreReceived() throws Exception {
		int port = startReceiver();
		clientSocket.send(createPacket(port, "test 12 12"));
		assertEventually(equalTo("test 12 12"));
		clientSocket.send(createPacket(port, "test1 123 1212"));
		assertEventually(equalTo("test1 123 1212"));
	}

	private DatagramPacket createPacket(int port, String msg) {
		byte[] messageBytes = msg.getBytes(Charset.forName("UTF-8"));
		DatagramPacket sendPacket = new DatagramPacket(messageBytes, messageBytes.length, address, port);
		return sendPacket;
	}

	private int startReceiver() {
		receiver = new UdpReceiver(0, packet -> receivedPacket = packet);
		receiver.start();
		int port = receiver.getPort();
		return port;
	}

	private void assertEventually(Matcher<String> matcher) throws Exception {
		new Poller(100, 10).check(new Probe() {
			
			@Override
			public void sample() {
				
			}
			
			@Override
			public boolean isSatisfied() {
				return matcher.matches(receivedPacket);
			}
			
			@Override
			public void describeFailureTo(Description description) {
				description.appendText("Expected string ").appendDescriptionOf(matcher).appendText(" was ").appendValue(receivedPacket);
			}
		});
	}

}
