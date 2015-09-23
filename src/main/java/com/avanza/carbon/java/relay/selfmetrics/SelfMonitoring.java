package com.avanza.carbon.java.relay.selfmetrics;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.avanza.carbon.java.relay.network.PacketHandler;
import com.avanza.carbon.java.relay.network.Worker;
import com.avanza.carbon.java.relay.util.NamedThreadFactory;

public class SelfMonitoring {
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("self-monitoring-thread"));
	
	private final PacketHandler packetHandler;
	private final List<Worker> workers;
	private final static String METRIC_KEY = "carbon.java_relay";
	
	public SelfMonitoring(PacketHandler packetHandler, List<Worker> workers) {
		this.packetHandler = Objects.requireNonNull(packetHandler);
		this.workers = Objects.requireNonNull(workers);
	}
	
	public void start() {
		executor.scheduleAtFixedRate(this::sendMetrics, 0, 60, TimeUnit.SECONDS);
	}
	
	public void sendMetrics() {
		sendMetric(METRIC_KEY + ".discardedPackets", packetHandler.getNumDiscardedPackets());
		sendMetric(METRIC_KEY + ".receivedPackets", packetHandler.getNumReceivedPackets());
		sendMetric(METRIC_KEY + ".queueSize", packetHandler.getQueueSize());
		for (int i = 0; i < workers.size(); i++) {
			Worker worker = workers.get(i);
			sendMetric(METRIC_KEY + ".worker" + i + ".num_sent", worker.getNumSent());
			sendMetric(METRIC_KEY + ".worker" + i + ".broken_lines", worker.getNumBrokenLines());
		}
	}
	
	private void sendMetric(String key, long value) {
		String line = String.format("%s %s %s", key, value, System.currentTimeMillis() / 1000);
		packetHandler.handlePacket(line);
	}
	
	
}
