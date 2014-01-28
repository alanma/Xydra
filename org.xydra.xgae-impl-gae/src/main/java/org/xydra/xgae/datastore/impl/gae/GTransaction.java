package org.xydra.xgae.datastore.impl.gae;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.xydra.xgae.datastore.api.STransaction;

import com.google.appengine.api.datastore.Transaction;


public class GTransaction extends RawWrapper<Transaction,STransaction> implements STransaction {
    
    private GTransaction(Transaction raw) {
        super(raw);
    }
    
    public static class WrappedFuture implements Future<STransaction> {
        
        private Future<Transaction> raw;
        
        public WrappedFuture(Future<Transaction> rawTxn) {
            this.raw = rawTxn;
        }
        
        public boolean cancel(boolean mayInterruptIfRunning) {
            return this.raw.cancel(mayInterruptIfRunning);
        }
        
        public boolean isCancelled() {
            return this.raw.isCancelled();
        }
        
        public boolean isDone() {
            return this.raw.isDone();
        }
        
        public STransaction get() throws InterruptedException, ExecutionException {
            return GTransaction.wrap(this.raw.get());
        }
        
        public STransaction get(long timeout, TimeUnit unit) throws InterruptedException,
                ExecutionException, TimeoutException {
            return GTransaction.wrap(this.raw.get(timeout, unit));
        }
        
    }
    
    public static Future<STransaction> wrapFuture(Future<Transaction> rawTxn) {
        return new WrappedFuture(rawTxn);
    }
    
    public static GTransaction wrap(Transaction raw) {
        if(raw == null)
            return null;
        
        return new GTransaction(raw);
    }
    
}
