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

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LineServer {
	
	private ServerSocket ss;
	Executor executor = Executors.newCachedThreadPool();
	private StringBuffer stringBuffer = new StringBuffer();
	private List<SocketReader> socketReaders = new CopyOnWriteArrayList<>();
	
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
		String allReceived = stringBuffer.toString();
		String[] lines = allReceived.split("\n");
		List<String> l = new ArrayList<>(Arrays.asList(lines));
		// If the last received does not end with newline it is not complete and should not be included.
		if (!allReceived.endsWith("\n")) {
			l.remove(l.size() - 1);
		}
		return l;
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
				doIt();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		private void doIt() throws IOException {
			InputStream is = socket.getInputStream();
			byte[] buffer = new byte[1024];
			for (int numRead = 0; numRead != -1; numRead = is.read(buffer, 0, buffer.length)) {
				String s = new String(buffer, 0, numRead);
				stringBuffer.append(s);
			}
		}

	}
	
}
