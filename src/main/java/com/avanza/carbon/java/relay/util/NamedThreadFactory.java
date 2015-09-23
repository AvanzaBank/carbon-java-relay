package com.avanza.carbon.java.relay.util;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread factory for creating named threads. Threads are daemon threads per default.
 * 
 * @author Kristoffer Erlandsson
 */
public class NamedThreadFactory implements ThreadFactory {

	private final ThreadGroup group;
	private final AtomicInteger threadNumber = new AtomicInteger(1);
	private final String namePrefix;
	private boolean daemon;

	/**
	 * Creates a daemon thread with the specified name prefix. Thread names will be namePrefix-<threadId>. Thread ID is
	 * incremented each time a thread is created using this factory.
	 * 
	 * @param namePrefix
	 *            not null.
	 */
	public NamedThreadFactory(String namePrefix) {
		this.namePrefix = Objects.requireNonNull(namePrefix);
		group = getThreadGroup();
		daemon = true;
	}

	/**
	 * Creates a thread with the specified daemon mode.
	 */
	public NamedThreadFactory(String namePrefix, boolean daemon) {
		this(namePrefix);
		this.daemon = daemon;
	}

	private ThreadGroup getThreadGroup() {
		// Done in the same way as java.util.concurrent.Executors.DefaultThreadFactory.
		SecurityManager s = System.getSecurityManager();
		return (s != null) ? s.getThreadGroup() :
				Thread.currentThread().getThreadGroup();
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(group, r, namePrefix + "-" + threadNumber.getAndIncrement());
		t.setDaemon(daemon);
		return t;
	}
}
