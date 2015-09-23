package com.avanza.carbon.java.relay.network;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;

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
