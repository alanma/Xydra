package org.xydra.xgae.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.xydra.index.iterator.ITransformer;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

public class FutureUtils {

	private static final Logger log = LoggerFactory.getLogger(FutureUtils.class);

	/**
	 * @param <T>
	 *            future type
	 * @param t
	 *            a future
	 * @return value or null
	 */
	public static <T> T waitFor(final Future<T> t) {
		while (true) {
			try {
				return t.get();
			} catch (final InterruptedException e) {
				log.warn("interrrupted while waiting for datastore get", e);
			} catch (final ExecutionException e) {
				return null;
			}
		}
	}

	public static <T> Future<T> createCompleted(final T value) {
		return new Future<T>() {

			@Override
			public boolean cancel(final boolean mayInterruptIfRunning) {
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
			public T get(final long timeout, final TimeUnit unit) {
				return value;
			}
		};
	}

	public static class TransformingFuture<I, O> implements Future<O> {

		@Override
		public boolean cancel(final boolean mayInterruptIfRunning) {
			return this.in.cancel(mayInterruptIfRunning);
		}

		@Override
		public boolean isCancelled() {
			return this.in.isCancelled();
		}

		@Override
		public boolean isDone() {
			return this.in.isDone();
		}

		@Override
		public O get() throws InterruptedException, ExecutionException {
			final I inValue = this.in.get();
			return this.transformer.transform(inValue);
		}

		@Override
		public O get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException,
				TimeoutException {
			final I inValue = this.in.get(timeout, unit);
			return this.transformer.transform(inValue);
		}

		private final Future<I> in;

		private final ITransformer<I, O> transformer;

		public TransformingFuture(final Future<I> in, final ITransformer<I, O> transformer) {
			super();
			this.in = in;
			this.transformer = transformer;
		}

	}
}
