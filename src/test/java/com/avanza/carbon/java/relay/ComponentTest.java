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
package com.avanza.carbon.java.relay;

import static org.hamcrest.Matchers.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import com.avanza.carbon.java.relay.conf.RelayConfig;
import com.avanza.carbon.java.relay.network.CarbonEndpoint;
import com.avanza.carbon.java.relay.network.NetworkTestUtils;
import com.avanza.carbon.java.relay.network.PickleServer;
import com.avanza.carbon.java.relay.testutil.Poller;
import com.avanza.carbon.java.relay.testutil.Probe;

public class ComponentTest {

	// TODO better shutdown of stuff, now we just leave threads running and
	// ports open.

	PickleServer pickleServer;
	private RelayServer server;

	@Before
	public void setup() {
		pickleServer = new PickleServer(0);
		pickleServer.start();
		server = startServerOnRandomPortWithRetries(pickleServer.getPort());
		try {
			server.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void receivesOneUdpMetric() throws Exception {
		NetworkTestUtils.sendUdpMessage("127.0.0.1", server.getUdpPort(), "foo.bar 3.12 2");
		assertEventually(receivedMetrics(pickleServer, contains(metric("foo.bar", "3.12", 2))));
	}

	@Test
	public void receivesManyUdpMetrics() throws Exception {
		NetworkTestUtils.sendUdpMessage("127.0.0.1", server.getUdpPort(), "foo.bar 3.12 2");
		NetworkTestUtils.sendUdpMessage("127.0.0.1", server.getUdpPort(), "foo.bar.baz 5.11 4444");
		NetworkTestUtils.sendUdpMessage("127.0.0.1", server.getUdpPort(), "foo.bar.baz 5.10 444");
		assertEventually(receivedMetrics(pickleServer, containsInAnyOrder(metric("foo.bar", "3.12", 2),
				metric("foo.bar.baz", "5.11", 4444), metric("foo.bar.baz", "5.10", 444))));
	}

	private MetricTuple metric(String key, String value, int timestamp) {
		return new MetricTuple(key, bd(value), timestamp);
	}

	private BigDecimal bd(String d) {
		return new BigDecimal(d);
	}

	private RelayServer startServerOnRandomPortWithRetries(int serverPort) {
		Random r = new Random();
		RuntimeException ex = null;
		for (int i = 0; i < 10; i++) {
			int port = r.nextInt(60000) + 5000;
			RelayConfig config = new RelayConfig(port, port, Arrays.asList(new CarbonEndpoint("127.0.0.1", serverPort)),
					"target");
			try {
				return new RelayServer(config);
			} catch (RuntimeException e) {
				ex = e;
			}
		}
		throw new RuntimeException("failed to start server after many tries", ex);
	}

	private Probe receivedMetrics(PickleServer pickleServer, Matcher<Iterable<? extends MetricTuple>> matcher) {
		return new Probe() {

			private List<MetricTuple> receivedMetrics;

			@Override
			public void sample() {
				receivedMetrics = pickleServer.getReceivedMetrics();
			}

			@Override
			public boolean isSatisfied() {
				return matcher.matches(receivedMetrics);
			}

			@Override
			public void describeFailureTo(Description description) {
				description.appendText("Expected received metrics ").appendDescriptionOf(matcher).appendText(" was ")
						.appendValue(receivedMetrics);
			}
		};
	}

	private void assertEventually(Probe probe) throws InterruptedException {
		new Poller(1500, 20).check(probe);
	}
}
