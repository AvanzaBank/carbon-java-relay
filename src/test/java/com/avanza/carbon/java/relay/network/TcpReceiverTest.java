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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
		NetworkTestUtils.sendTcpMessages("127.0.0.1", receiver.getPort(), "foo\n");
		assertEventually(receivedLines(receiver.received, contains("foo")));
	}

	@Test
	public void receivesMany() throws Exception {
		TestReceiver receiver = new TestReceiver();
		NetworkTestUtils.sendTcpMessages("127.0.0.1", receiver.getPort(), "foo\n", "bar\n", "baz\n");
		assertEventually(receivedLines(receiver.received, contains("foo", "bar", "baz")));
	}

	@Test
	public void parallelConnectionsHandled() throws Exception {
		ExecutorService pool = Executors.newCachedThreadPool();
		TestReceiver receiver = new TestReceiver();
		// We assume enough concurrency so that this creates some parallel connections.
		pool.execute(() -> NetworkTestUtils.sendTcpMessages("127.0.0.1", receiver.getPort(), "foo\n"));
		pool.execute(() -> NetworkTestUtils.sendTcpMessages("127.0.0.1", receiver.getPort(), "bar\n"));
		pool.execute(() -> NetworkTestUtils.sendTcpMessages("127.0.0.1", receiver.getPort(), "baz\n"));
		pool.execute(() -> NetworkTestUtils.sendTcpMessages("127.0.0.1", receiver.getPort(), "foobar\n"));
		assertEventually(receivedLines(receiver.received, containsInAnyOrder("foo", "bar", "baz", "foobar")));
		pool.shutdown();
	}
	
	@Test
	public void handlesDisconnect() throws Exception {
		TestReceiver receiver = new TestReceiver();
		NetworkTestUtils.sendTcpMessages("127.0.0.1", receiver.getPort(), "foo\n");
		// NetworkTestUtils disconnects between each call
		NetworkTestUtils.sendTcpMessages("127.0.0.1", receiver.getPort(), "bar\n");
		assertEventually(receivedLines(receiver.received, contains("foo", "bar")));
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
