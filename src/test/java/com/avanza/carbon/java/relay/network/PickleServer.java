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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import com.avanza.carbon.java.relay.MetricTuple;

/**
 * Simple network server for receiving pickled metrics. Used to simulate a
 * receiving carbon-cache instance.
 * 
 * @author Kristoffer Erlandsson
 */
public class PickleServer {

	private ServerSocket ss;
	Executor executor = Executors.newCachedThreadPool();
	private List<MetricTuple> receivedMetrics = new CopyOnWriteArrayList<>();
	private List<SocketReader> socketReaders = new CopyOnWriteArrayList<>();

	public PickleServer(int port) {
		try {
			ss = new ServerSocket(port);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public int getPort() {
		return ss.getLocalPort();
	}

	public List<MetricTuple> getReceivedMetrics() {
		return receivedMetrics;
	}

	public void disconnectAllReaders() {
		for (SocketReader socketReader : socketReaders) {
			socketReader.disconnect();
		}
		socketReaders.clear();
	}

	public void start() {
		executor.execute(acceptor);
	}

	private Runnable acceptor = () -> {
		while (true) {
			try {
				Socket accept = ss.accept();
				SocketReader reader = new SocketReader(accept, s -> receivedMetrics.addAll(convertToMetrics(s)));
				socketReaders.add(reader);
				executor.execute(reader);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	};

	private List<MetricTuple> convertToMetrics(String pickled) {
		List<MetricTuple> l = new ArrayList<>();
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(pickled).useDelimiter("[SL(']");
		while (true) {
			// Naive unpickler that only supports our own stripped pickle format.
			try {
				scanner.next();
				scanner.next();
				scanner.next();
				String key = scanner.next();
				scanner.next();
				scanner.next();
				long timestamp = scanner.nextLong();
				scanner.next();
				scanner.next();
				String valueString = scanner.next();
				BigDecimal value = new BigDecimal(valueString);
				l.add(new MetricTuple(key, value, timestamp));
			} catch (NoSuchElementException e) {
				break;
			}
		}
		return l;

	}

	private class SocketReader implements Runnable {

		private final Socket socket;
		private Consumer<String> consumer;

		public SocketReader(Socket socket, Consumer<String> conusmer) {
			this.socket = socket;
			this.consumer = conusmer;
		}

		public void disconnect() {
			try {
				socket.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void run() {
			try {
				readLoop();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		private void readLoop() throws IOException {
			byte[] buffer = new byte[4096];
			InputStream is = socket.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			byte[] header = new byte[4];
			while (true) {
				if (bis.read(header) == -1) {
					return;
				}
				int length = ByteBuffer.wrap(header).getInt();
				if (bis.read(buffer, 0, length) == -1) {
					return;
				}
				String msg = new String(buffer, 0, length);
				consumer.accept(msg);
			}
		}
	}
	
}
