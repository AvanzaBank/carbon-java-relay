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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LineServer {
	
	private ServerSocket ss;
	Executor executor = Executors.newCachedThreadPool();
	private List<SocketReader> socketReaders = new CopyOnWriteArrayList<>();
	private List<String> receivedLines = new CopyOnWriteArrayList<>();
	
	public LineServer() {
		try {
			ss = new ServerSocket(0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public int getPort() {
		return ss.getLocalPort();
	}
	
	
	public List<String> getReceivedLines() {
		return receivedLines;
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
				SocketReader reader = new SocketReader(accept);
				socketReaders.add(reader);
				executor.execute(reader);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	};
	
	private class SocketReader implements Runnable {

		private final Socket socket;

		public SocketReader(Socket socket) {
			this.socket = socket;
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
			InputStream is = socket.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				receivedLines.add(line);
			}
		}

	}
	
}
