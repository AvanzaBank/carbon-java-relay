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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.hamcrest.Matcher;
import org.junit.Test;

import com.avanza.carbon.java.relay.testutil.Poller;
import com.avanza.carbon.java.relay.testutil.Probe;

public class CarbonConnectionTest {

	@Test
	public void sendsMessageAsLine() throws Exception {
		LineServer server = new LineServer();
		server.start();
		CarbonConnection cc = new CarbonConnection("127.0.0.1", server.getPort());
		cc.send("foo\n");
		assertEventually(receivedLines(server, contains("foo")));
	}

	@Test
	public void sendsTwoMessages() throws Exception {
		LineServer server = new LineServer();
		server.start();
		CarbonConnection cc = new CarbonConnection("127.0.0.1", server.getPort());
		cc.send("foo\n");
		cc.send("bar\n");
		assertEventually(receivedLines(server, contains("foo", "bar")));
	}

	@Test
	public void sendsMessageAfterDisconnect() throws Exception {
		LineServer server = new LineServer();
		server.start();
		CarbonConnection cc = new CarbonConnection("127.0.0.1", server.getPort());
		cc.send("foo\n");
		assertEventually(receivedLines(server, contains("foo")));
		server.disconnectAllReaders();
		// We lose some data in the void when disconnecting, just ensure we get the last lines.
		cc.send("bar\n");
		cc.send("bar\n");
		cc.send("bar\n");
		cc.send("bar2\n");
		cc.send("bar3\n");
		assertEventually(receivedLines(server, hasItems("foo", "bar2", "bar3")));
	}
	
	@Test
	public void connectsLazily() throws Exception {
		ExecutorService pool = Executors.newCachedThreadPool();
		int port = NetworkTestUtils.findFreeListeningPort();
		CarbonConnection cc = new CarbonConnection("127.0.0.1", port, 10);
		pool.execute(() -> cc.send("foo\n"));
		LineServer server = new LineServer(port);
		server.start();
		assertEventually(receivedLines(server, hasItems("foo")));
		pool.shutdown();
	}

	private Probe receivedLines(LineServer server, Matcher<?> matcher) {
		return new Probe() {

			private List<String> lines;

			@Override
			public boolean isSatisfied() {
				return matcher.matches(lines);
			}

			@Override
			public void sample() {
				lines = server.getReceivedLines();
			}

			@Override
			public void describeFailureTo(org.hamcrest.Description description) {
				description.appendText("Expected received lines ").appendDescriptionOf(matcher).appendText(" was ")
						.appendValue(lines);
			}

		};
	}

	private void assertEventually(Probe probe) throws Exception {
		new Poller(1500, 20).check(probe);
	}

}
