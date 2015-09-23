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

import java.util.Objects;
import java.util.concurrent.BlockingQueue;

/**
 * @author Kristoffer Erlandsson
 */
public class PacketHandler {
	
	private final BlockingQueue<String> queue;
	private volatile long numDiscardedPackets = 0;
	private volatile long numReceivedPackets = 0;

	public PacketHandler(BlockingQueue<String> queue) {
		this.queue = Objects.requireNonNull(queue);
	}

	public void handlePacket(String packet) {
		boolean res = queue.offer(packet);
		numReceivedPackets++;
		if (!res) {
			numDiscardedPackets++;
		}
	}

	public long getNumReceivedPackets() {
		return numReceivedPackets;
	}

	public long getNumDiscardedPackets() {
		return numDiscardedPackets;
	}
	
	public int getQueueSize() {
		return queue.size();
	}

}
