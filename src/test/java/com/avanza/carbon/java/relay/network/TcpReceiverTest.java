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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

import com.avanza.carbon.java.relay.testutil.Poller;
import com.avanza.carbon.java.relay.testutil.Probe;

/**
 * @author Kristoffer Erlandsson
 */
public class TcpReceiverTest {

	// TODO proper shutdown of receiver

	@Test
	public void receivesOne() throws Exception {
		TestReceiver receiver = new TestReceiver();
		try (Socket socket = new Socket("127.0.0.1", receiver.getPort())) {
			sendLine(socket, "foo");
			assertEventually(receivedLines(receiver.received, contains("foo")));
		}
	}

	@Test
	public void receivesMany() throws Exception {
		TestReceiver receiver = new TestReceiver();
		try (Socket socket = new Socket("127.0.0.1", receiver.getPort())) {
			sendLine(socket, "foo");
			sendLine(socket, "bar");
			sendLine(socket, "baz");
			assertEventually(receivedLines(receiver.received, contains("foo", "bar", "baz")));
		}
	}

	@Test
	public void twoConnetionsHandled() throws Exception {
		TestReceiver receiver = new TestReceiver();
		try (Socket socket = new Socket("127.0.0.1", receiver.getPort())) {
			try (Socket socket2 = new Socket("127.0.0.1", receiver.getPort())) {
				sendLine(socket, "foo");
				sendLine(socket2, "bar");
				assertEventually(receivedLines(receiver.received, containsInAnyOrder("foo", "bar")));
			}
		}
	}
	
	@Test
	public void handlesDisconnect() throws Exception {
		TestReceiver receiver = new TestReceiver();
		try (Socket socket = new Socket("127.0.0.1", receiver.getPort())) {
			sendLine(socket, "foo");
		}
		try (Socket socket = new Socket("127.0.0.1", receiver.getPort())) {
			sendLine(socket, "bar");
		}
		assertEventually(receivedLines(receiver.received, contains("foo", "bar")));
	}

	private void sendLine(Socket socket, String line) throws IOException {
		PrintWriter pw = new PrintWriter(socket.getOutputStream());
		pw.println(line);
		pw.flush();
	}

	private void assertEventually(Probe probe) throws InterruptedException {
		new Poller(300, 20).check(probe);

	}

	private Probe receivedLines(List<String> received, Matcher<Iterable<? extends String>> matcher) {
		return new Probe() {

			@Override
			public boolean isSatisfied() {
				return matcher.matches(received);
			}

			@Override
			public void sample() {
			}

			@Override
			public void describeFailureTo(Description description) {
				description.appendText("Expected received lines ").appendDescriptionOf(matcher).appendText(" was ")
						.appendValue(received);
			}

		};
	}

	private static class TestReceiver {
		private final List<String> received = new CopyOnWriteArrayList<>();
		private TcpReceiver tcpReceiver;

		public TestReceiver() {
			tcpReceiver = new TcpReceiver(0, s -> received.add(s));
			tcpReceiver.start();
		}

		public int getPort() {
			return tcpReceiver.getPort();
		}
	}
}
