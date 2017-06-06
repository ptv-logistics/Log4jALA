package com.github.ptvlogistics.log4jala;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;


public class ALAThreadFactoryBuilder {
	
	private String nameFormat = null;
	private boolean daemon = false;
	private int priority = Thread.NORM_PRIORITY;

	public ALAThreadFactoryBuilder setNameFormat(String nameFormat) {
		if (nameFormat == null) {
			throw new NullPointerException();
		}
		this.nameFormat = nameFormat;
		return this;
	}

	public ALAThreadFactoryBuilder setDaemon(boolean daemon) {
		this.daemon = daemon;
		return this;
	}

	public ALAThreadFactoryBuilder setPriority(int priority) {
		if (priority < Thread.MIN_PRIORITY) {
			throw new IllegalArgumentException(String.format(
					"Thread priority (%s) must be >= %s", priority,
					Thread.MIN_PRIORITY));
		}

		if (priority > Thread.MAX_PRIORITY) {
			throw new IllegalArgumentException(String.format(
					"Thread priority (%s) must be <= %s", priority,
					Thread.MAX_PRIORITY));
		}

		this.priority = priority;
		return this;
	}

	public ThreadFactory build() {
		return build(this);
	}

	private static ThreadFactory build(ALAThreadFactoryBuilder builder) {
		final String nameFormat = builder.nameFormat;
		final Boolean daemon = builder.daemon;
		final Integer priority = builder.priority;

		final AtomicLong count = new AtomicLong(0);

		return new ThreadFactory() {
			@Override
			public Thread newThread(Runnable runnable) {
				Thread thread = new Thread(runnable);
				if (nameFormat != null) {
					thread.setName(nameFormat + "-" + count.getAndIncrement());
				}
				if (daemon != null) {
					thread.setDaemon(daemon);
				}
				if (priority != null) {
					thread.setPriority(priority);
				}
				return thread;
			}
		};
	}


}
