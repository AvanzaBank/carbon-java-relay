package com.avanza.carbon.java.relay.network;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avanza.carbon.java.relay.MetricTuple;
import com.avanza.carbon.java.relay.pickle.Pickler;
import com.avanza.carbon.java.relay.util.QueueUtils;

public class Worker implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(Worker.class);

	private final BlockingQueue<String> queue;
	private final CarbonConnection connection;

	private final int BATCH_SIZE = 100;
	private volatile long numSent = 0;
	private volatile long numBrokenLines = 0;
	
	public Worker(CarbonConnection connection, BlockingQueue<String> queue) {
		this.queue = Objects.requireNonNull(queue);
		this.connection = Objects.requireNonNull(connection);
	}

	public void run() {
		while (!Thread.interrupted()) {
			List<String> buffer = new ArrayList<>(BATCH_SIZE);
			int added = QueueUtils.drainUninterruptibly(queue, buffer, BATCH_SIZE, 1, TimeUnit.SECONDS);
			if (added > 0) {
				List<MetricTuple> tuples = convertToTuples(buffer);
				String pickled = Pickler.convertMetricsToGraphitePickleMetricsFormat(tuples);
				int length = pickled.length();
				byte[] header = ByteBuffer.allocate(4).putInt(length).array();
				connection.send(header, pickled);
				numSent += added;
			}
		}
		log.info("Worker interrupted, exiting, connection: " + connection);
	}

	private List<MetricTuple> convertToTuples(List<String> buffer) {
		List<MetricTuple> res = new ArrayList<>(buffer.size());
		for (String string : buffer) {
			try {
				res.add(MetricTuple.fromString(string));
			} catch (Exception e) {
				log.debug("Unexpected metric line: " + string, e);
				numBrokenLines++;
			}
		}
		return res;
	}
	

	public long getNumSent() {
		return numSent;
	}

	public long getNumBrokenLines() {
		return numBrokenLines;
	}

}
