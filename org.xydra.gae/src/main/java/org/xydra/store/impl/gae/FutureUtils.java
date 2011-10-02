package org.xydra.store.impl.gae;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


public class FutureUtils {
	
	private static final Logger log = LoggerFactory.getLogger(FutureUtils.class);
	
	/**
	 * @param <T> future type
	 * @param t a future
	 * @return value or null
	 */
	public static <T> T waitFor(Future<T> t) {
		while(true) {
			try {
				return t.get();
			} catch(InterruptedException e) {
				log.warn("interrrupted while waiting for datastore get", e);
			} catch(ExecutionException e) {
				return null;
			}
		}
	}
	
	public static <T> Future<T> createCompleted(final T value) {
		return new Future<T>() {
			
			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return false;
			}
			
			@Override
			public boolean isCancelled() {
				return false;
			}
			
			@Override
			public boolean isDone() {
				return true;
			}
			
			@Override
			public T get() {
				return value;
			}
			
			@Override
			public T get(long timeout, TimeUnit unit) {
				return value;
			}
		};
	}
}
