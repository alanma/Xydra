package org.xydra.xgae.datastore.impl.gae;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.xydra.xgae.datastore.api.STransaction;

import com.google.appengine.api.datastore.Transaction;

public class GTransaction extends RawWrapper<Transaction, STransaction> implements STransaction {

	private GTransaction(final Transaction raw) {
		super(raw);
	}

	public static class WrappedFuture implements Future<STransaction> {

		private final Future<Transaction> raw;

		public WrappedFuture(final Future<Transaction> rawTxn) {
			this.raw = rawTxn;
		}

		@Override
		public boolean cancel(final boolean mayInterruptIfRunning) {
			return this.raw.cancel(mayInterruptIfRunning);
		}

		@Override
		public boolean isCancelled() {
			return this.raw.isCancelled();
		}

		@Override
		public boolean isDone() {
			return this.raw.isDone();
		}

		@Override
		public STransaction get() throws InterruptedException, ExecutionException {
			return GTransaction.wrap(this.raw.get());
		}

		@Override
		public STransaction get(final long timeout, final TimeUnit unit) throws InterruptedException,
				ExecutionException, TimeoutException {
			return GTransaction.wrap(this.raw.get(timeout, unit));
		}

	}

	public static Future<STransaction> wrapFuture(final Future<Transaction> rawTxn) {
		return new WrappedFuture(rawTxn);
	}

	public static GTransaction wrap(final Transaction raw) {
		if (raw == null) {
			return null;
		}

		return new GTransaction(raw);
	}

}
