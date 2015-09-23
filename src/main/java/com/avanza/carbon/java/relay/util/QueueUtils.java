package com.avanza.carbon.java.relay.util;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class QueueUtils {
	/**
	 * Copied from Google guavas queue utils Will drain the queue until the
	 * specified amount of units has been taken, or until the timeout is reached
	 */
	public static <T> int drainUninterruptibly(BlockingQueue<T> q, Collection<T> buffer, int numElements,
			long timeout, TimeUnit unit) {
		long deadline = System.nanoTime() + unit.toNanos(timeout);
		int added = 0;
		boolean interrupted = false;
		try {
			while (added < numElements) {
				added += q.drainTo(buffer, numElements - added);
				if (added < numElements) {
					T e;
					while (true) {
						try {
							e = q.poll(deadline - System.nanoTime(), TimeUnit.NANOSECONDS);
							break;
						} catch (InterruptedException ex) {
							interrupted = true; // note interruption and retry
						}
					}
					if (e == null) {
						break; // we already waited enough, and there are no
								// more elements in sight
					}
					buffer.add(e);
					added++;
				}
			}
		} finally {
			if (interrupted) {
				Thread.currentThread().interrupt();
			}
		}
		return added;
	}

}
