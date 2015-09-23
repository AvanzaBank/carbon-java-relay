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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avanza.carbon.java.relay.util.NamedThreadFactory;

/**
 * @author Kristoffer Erlandsson
 */
public class TcpReceiver {

	private static Logger log = LoggerFactory.getLogger(TcpReceiver.class);
	private int port;
	private ServerSocket serverSocket;
	private Executor executor = Executors.newCachedThreadPool(new NamedThreadFactory("tcp-receiver", false));
	private Consumer<String> consumer;

	public TcpReceiver(int port, Consumer<String> consumer) {
		this.consumer = Objects.requireNonNull(consumer);
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		this.port = serverSocket.getLocalPort();
	}

	private Runnable acceptor = () -> {
		while (true) {
			try {
				Socket accepted = serverSocket.accept();
				log.debug("Accepted connection, local port: {}", accepted.getLocalPort());
				SocketReader reader = new SocketReader(accepted, consumer);
				executor.execute(reader);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	};

	public void start() {
		executor.execute(acceptor);
	}
	
	public int getPort() {
		return port;
	}

	private class SocketReader implements Runnable {

		private Socket socket;
		private Consumer<String> consumer;

		public SocketReader(Socket socket, Consumer<String> consumer) {
			this.socket = socket;
			this.consumer = consumer;
		}

		public void run() {
			try {
				consumeLines();
			} catch (IOException e) {
				closeSocket();
				log.debug("Exception when consuming lines", e);
			}
		}

		private void consumeLines() throws IOException {
			InputStream is = socket.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				log.debug("Received line '{}'", line);
				consumer.accept(line);
			}
			log.info("End of stream for socket on port {}", socket.getLocalPort());
			closeSocket();
		}

		private void closeSocket() {
			try {
				socket.close();
			} catch (IOException e) {
				log.debug("Exception when closing socket", e);
			}
			
		}
	}

}
