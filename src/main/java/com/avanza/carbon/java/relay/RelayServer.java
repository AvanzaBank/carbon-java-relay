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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avanza.carbon.java.relay.conf.CarbonConfigReader;
import com.avanza.carbon.java.relay.conf.RelayConfig;
import com.avanza.carbon.java.relay.network.CarbonConnection;
import com.avanza.carbon.java.relay.network.CarbonEndpoint;
import com.avanza.carbon.java.relay.network.PacketHandler;
import com.avanza.carbon.java.relay.network.TcpReceiver;
import com.avanza.carbon.java.relay.network.UdpReceiver;
import com.avanza.carbon.java.relay.network.Worker;
import com.avanza.carbon.java.relay.selfmetrics.SelfMonitoring;
import com.avanza.carbon.java.relay.util.LoggingUncaughtExceptionHandler;
import com.avanza.carbon.java.relay.util.NamedThreadFactory;

/**
 * @author Kristoffer Erlandsson
 */
public class RelayServer {

	private static final int QUEUE_SIZE = 1000000;
	private final Logger log = LoggerFactory.getLogger(RelayServer.class);
	ExecutorService workerExecutor = Executors.newCachedThreadPool(new NamedThreadFactory("relay-worker"));
	private RelayConfig config;
	
	public static void main(String args[]) throws Exception {
		validateArgs(args);
		String fileName = args[0];
		RelayConfig config = getConfig(fileName);
		System.setProperty("javaCarbonRelayLogDir", config.getLogDir());
		Thread.setDefaultUncaughtExceptionHandler(new LoggingUncaughtExceptionHandler());
		new RelayServer(config).start();
	}

	public RelayServer(RelayConfig config) {
		this.config = config;
	}

	public void start() throws Exception {
		log.info("Listen port: {}, destinations: {}", config.getUdpListenPort(), config.getDestinations());
		BlockingQueue<String> queue = new ArrayBlockingQueue<>(QUEUE_SIZE);
		PacketHandler packetHandler = new PacketHandler(queue);
		UdpReceiver udpReceiver = new UdpReceiver(config.getUdpListenPort(), s -> packetHandler.handlePacket(s));
		TcpReceiver tcpReceiver = new TcpReceiver(config.getLineReceiverPort(), s -> packetHandler.handlePacket(s));
		List<Worker> workers = createWorkers(config, queue);
		udpReceiver.start();
		tcpReceiver.start();
		workers.stream().forEach(worker -> workerExecutor.execute(worker));
		new SelfMonitoring(packetHandler, workers).start();
	}

	private List<Worker> createWorkers(RelayConfig config, BlockingQueue<String> queue) {
		List<Worker> workers = new ArrayList<>();
		for (CarbonEndpoint endpoint : config.getDestinations()) {
			workers.add(new Worker(new CarbonConnection(endpoint.getAdress(), endpoint.getPort()), queue));
		}
		return workers;
	}

	private static void validateArgs(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: java -jar <jarfile> <carbon config file>");
			System.exit(1);
		}
	}

	private static RelayConfig getConfig(String fileName) throws FileNotFoundException, IOException {
		FileInputStream inputStream = new FileInputStream(fileName);
		CarbonConfigReader carbonConfigReader = new CarbonConfigReader(inputStream);
		RelayConfig config = carbonConfigReader.readConfig();
		inputStream.close();
		return config;
	}

}
