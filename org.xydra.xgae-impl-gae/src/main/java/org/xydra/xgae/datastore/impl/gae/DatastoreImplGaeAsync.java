package org.xydra.xgae.datastore.impl.gae;

import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.xydra.annotations.ModificationOperation;
import org.xydra.index.TransformerTool;
import org.xydra.index.iterator.ITransformer;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.sharedutils.XyAssert;
import org.xydra.xgae.annotations.XGaeOperation;
import org.xydra.xgae.datastore.api.IDatastoreAsync;
import org.xydra.xgae.datastore.api.SEntity;
import org.xydra.xgae.datastore.api.SKey;
import org.xydra.xgae.datastore.api.SPreparedQuery;
import org.xydra.xgae.datastore.api.STransaction;
import org.xydra.xgae.gaeutils.GaeTestfixer;
import org.xydra.xgae.util.FutureUtils;
import org.xydra.xgae.util.XGaeDebugHelper;
import org.xydra.xgae.util.XGaeDebugHelper.Timing;

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;


public class DatastoreImplGaeAsync extends DatastoreImplGaeBase implements IDatastoreAsync {
    
    private static abstract class DebugFuture<K, V> implements Future<V> {
        
        private Future<V> f;
        private K key;
        
        public DebugFuture(K key, Future<V> f) {
            this.key = key;
            this.f = f;
        }
        
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return this.f.cancel(mayInterruptIfRunning);
        }
        
        @Override
        public boolean isCancelled() {
            return this.f.isCancelled();
        }
        
        @Override
        public boolean isDone() {
            return this.f.isDone();
        }
        
        @Override
        public V get() throws InterruptedException, ExecutionException {
            V result = this.f.get();
            log(this.key, result);
            return result;
        }
        
        abstract void log(K key, V result);
        
        @Override
        public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
                TimeoutException {
            V result = this.f.get(timeout, unit);
            log(this.key, result);
            return result;
        }
        
    }
    
    /**
     * Almost copy&paste from {@link FutureUtils.TransformingFuture}
     * 
     * @author xamde
     * 
     * @param <K>
     * @param <I>
     * @param <O>
     */
    private static abstract class TransformingDebugFuture<K, I, O> implements Future<O> {
        
        private Future<I> f;
        private K key;
        private ITransformer<I,O> transformer;
        
        public TransformingDebugFuture(K key, Future<I> f, ITransformer<I,O> transformer) {
            this.key = key;
            this.f = f;
            this.transformer = transformer;
        }
        
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return this.f.cancel(mayInterruptIfRunning);
        }
        
        @Override
        public boolean isCancelled() {
            return this.f.isCancelled();
        }
        
        @Override
        public boolean isDone() {
            return this.f.isDone();
        }
        
        @Override
        public O get() throws InterruptedException, ExecutionException {
            I in = this.f.get();
            O out = this.transformer.transform(in);
            log(this.key, out);
            return out;
        }
        
        abstract void log(K key, O result);
        
        @Override
        public O get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
                TimeoutException {
            I in = this.f.get(timeout, unit);
            O out = this.transformer.transform(in);
            log(this.key, out);
            return out;
        }
        
    }
    
    private static class FutureDeleteEntity extends DebugFuture<SKey,Void> {
        public FutureDeleteEntity(SKey key, Future<Void> f) {
            super(key, f);
        }
        
        @Override
        void log(SKey key, Void result) {
            log.debug(XGaeDebugHelper.dataPut(DATASTORE_NAME, XGaeDebugHelper.toString(key), null,
                    Timing.Finished));
        }
    }
    
    private static class FutureGetEntity extends DebugFuture<SKey,SEntity> {
        public FutureGetEntity(SKey key, Future<Entity> f) {
            super(key, new FutureUtils.TransformingFuture<Entity,SEntity>(f,
                    new ITransformer<Entity,SEntity>() {
                        
                        @Override
                        public SEntity transform(Entity in) {
                            return GEntity.wrap(in);
                        }
                    }));
        }
        
        @Override
        void log(SKey key, SEntity result) {
            log.debug(XGaeDebugHelper.dataGet(DATASTORE_NAME, XGaeDebugHelper.toString(key),
                    result, Timing.Finished));
        }
    }
    
    /**
     * Print debug info at moment of retrieval
     * 
     * @author xamde
     */
    private static class FutureGetEntities extends
            TransformingDebugFuture<Collection<SKey>,Map<Key,Entity>,Map<SKey,SEntity>> {
        
        public FutureGetEntities(Collection<SKey> keys, Future<Map<Key,Entity>> f,
                ITransformer<Map<Key,Entity>,Map<SKey,SEntity>> transformer) {
            super(keys, f, transformer);
        }
        
        @Override
        void log(Collection<SKey> key, Map<SKey,SEntity> result) {
            log.debug(XGaeDebugHelper.dataGet(DATASTORE_NAME, key, result, Timing.Finished));
        }
    }
    
    private static class FuturePutEntities extends
            TransformingDebugFuture<Iterable<SEntity>,List<Key>,List<SKey>> {
        public FuturePutEntities(Iterable<SEntity> it, Future<List<Key>> f,
                ITransformer<List<Key>,List<SKey>> transformer) {
            super(it, f, transformer);
        }
        
        @Override
        void log(Iterable<SEntity> key, List<SKey> result) {
            log.debug(XGaeDebugHelper.dataPut(DATASTORE_NAME, "manyKeys", key, Timing.Finished));
        }
    }
    
    private static class FuturePutEntity extends DebugFuture<SEntity,SKey> {
        public FuturePutEntity(SEntity key, Future<Key> f) {
            super(key, new FutureUtils.TransformingFuture<Key,SKey>(f,
                    new ITransformer<Key,SKey>() {
                        
                        @Override
                        public SKey transform(Key in) {
                            return GKey.wrap(in);
                        }
                    }));
        }
        
        @Override
        void log(SEntity key, SKey result) {
            log.debug(XGaeDebugHelper.dataPut(DATASTORE_NAME, XGaeDebugHelper.toString(result),
                    key, Timing.Finished));
        }
    }
    
    private static final Logger log = LoggerFactory.getLogger(DatastoreImplGaeAsync.class);
    
    private AsyncDatastoreService asyncDatastore;
    
    public static final String DATASTORE_NAME = "[#DSa]";
    
    /** compile-time flag to enable trace-level logging of all data-operations */
    private static final boolean TRACE_CALLS = false;
    
    @Override
    @ModificationOperation
    public Future<STransaction> beginTransaction() {
        log.debug("-- begin Transaction --");
        makeSureDatestoreServiceIsInitialised();
        return GTransaction.wrapFuture(this.asyncDatastore.beginTransaction());
    }
    
    @Override
    @ModificationOperation
    public Future<Void> deleteEntity(SKey key) {
        return deleteEntity(key, null);
    }
    
    @Override
    @ModificationOperation
    public Future<Void> deleteEntity(SKey key, STransaction txn) {
        assert key != null;
        log.debug(XGaeDebugHelper.dataPut(DATASTORE_NAME, key.toString(), null, Timing.Started));
        makeSureDatestoreServiceIsInitialised();
        
        Future<Void> future = this.asyncDatastore.delete((Transaction)txn.raw(), (Key)key.raw());
        return new FutureDeleteEntity(key, future);
    }
    
    @Override
    @ModificationOperation
    public void endTransaction(STransaction txn) throws ConcurrentModificationException {
        log.debug("-- end Transaction --");
        makeSureDatestoreServiceIsInitialised();
        ((Transaction)txn.raw()).commit();
    }
    
    @Override
    public Future<Map<SKey,SEntity>> getEntities(Collection<SKey> keys) {
        return getEntities(keys, null);
    }
    
    @Override
    @XGaeOperation(datastoreRead = true)
    public Future<Map<SKey,SEntity>> getEntities(Collection<SKey> keys, STransaction txn) {
        assert keys != null;
        makeSureDatestoreServiceIsInitialised();
        
        ITransformer<Map<Key,Entity>,Map<SKey,SEntity>> transformer = new ITransformer<Map<Key,Entity>,Map<SKey,SEntity>>() {
            
            @Override
            public Map<SKey,SEntity> transform(Map<Key,Entity> in) {
                return TransformerTool.transformMapKeyAndValues(in, GKey.TRANSFOMER_KEY_SKEY,
                        GEntity.TRANSFOMER_ENTITY_SENTITY);
            }
        };
        
        if(keys.isEmpty()) {
            if(TRACE_CALLS) {
                Map<Key,Entity> emptyMap = Collections.emptyMap();
                Future<Map<Key,Entity>> result = FutureUtils.createCompleted(emptyMap);
                return new FutureGetEntities(keys, result, transformer);
            } else {
                Map<SKey,SEntity> emptyMap = Collections.emptyMap();
                Future<Map<SKey,SEntity>> result = FutureUtils.createCompleted(emptyMap);
                return result;
            }
        } else {
            Future<Map<Key,Entity>> rawResult = this.asyncDatastore.get((Transaction)txn.raw(),
                    GKey.unwrap(keys));
            if(TRACE_CALLS) {
                return new FutureGetEntities(keys, rawResult, transformer);
            } else {
                return new FutureUtils.TransformingFuture<Map<Key,Entity>,Map<SKey,SEntity>>(
                        rawResult, transformer);
            }
        }
    }
    
    @Override
    @XGaeOperation(datastoreRead = true)
    public Future<SEntity> getEntity(SKey key) {
        return getEntity(key, null);
    }
    
    @Override
    @XGaeOperation(datastoreRead = true)
    public Future<SEntity> getEntity(SKey key, STransaction txn) {
        assert key != null;
        makeSureDatestoreServiceIsInitialised();
        Future<Entity> e = this.asyncDatastore.get((Transaction)txn.raw(), (Key)key.raw());
        return new FutureGetEntity(key, e);
    }
    
    private void makeSureDatestoreServiceIsInitialised() {
        GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
        if(this.asyncDatastore == null) {
            log.debug(XGaeDebugHelper.init(DATASTORE_NAME));
            this.asyncDatastore = DatastoreServiceFactory.getAsyncDatastoreService();
        }
    }
    
    @Override
    public SPreparedQuery prepareRangeQuery(String kind, boolean keysOnly, String lowName,
            String highName) {
        return prepareRangeQuery(kind, true, lowName, highName, null);
    }
    
    /** @see DatastoreService#prepare(Query) */
    @Override
    public SPreparedQuery prepareRangeQuery(String kind, boolean keysOnly, String lowName,
            String highName, STransaction txn) {
        Query query = createRangeQuery(kind, keysOnly, lowName, highName);
        assert query != null;
        return GPreparedQuery.wrap(this.asyncDatastore.prepare((Transaction)txn.raw(), query));
    }
    
    @Override
    @ModificationOperation
    public Future<List<SKey>> putEntities(Iterable<SEntity> it) {
        XyAssert.xyAssert(it != null, "iterable is null");
        assert it != null;
        log.debug(XGaeDebugHelper.dataPut(DATASTORE_NAME, "entities", "many", Timing.Now));
        makeSureDatestoreServiceIsInitialised();
        Future<List<Key>> f = this.asyncDatastore.put(
        
        TransformerTool.transformIterable(it,
        
        new ITransformer<SEntity,Entity>() {
            
            @Override
            public Entity transform(SEntity in) {
                return (Entity)in.raw();
            }
        })
        
        );
        
        ITransformer<List<Key>,List<SKey>> transformer = new ITransformer<List<Key>,List<SKey>>() {
            
            @Override
            public List<SKey> transform(List<Key> in) {
                return TransformerTool.transformListEntries(in, GKey.TRANSFOMER_KEY_SKEY);
            }
        };
        
        if(TRACE_CALLS) {
            return new FuturePutEntities(it, f, transformer);
        } else {
            return new FutureUtils.TransformingFuture<List<Key>,List<SKey>>(f, transformer);
        }
        
    }
    
    @Override
    public Future<SKey> putEntity(SEntity entity) {
        return putEntity(entity, null);
    }
    
    @Override
    @ModificationOperation
    public Future<SKey> putEntity(SEntity entity, STransaction txn) {
        XyAssert.xyAssert(entity != null, "entity is null");
        assert entity != null;
        log.debug(XGaeDebugHelper.dataPut(DATASTORE_NAME,
                XGaeDebugHelper.toString(entity.getKey()), entity, Timing.Started));
        makeSureDatestoreServiceIsInitialised();
        
        Future<Key> f = this.asyncDatastore.put((Transaction)txn.raw(), (Entity)entity.raw());
        
        if(TRACE_CALLS) {
            return new FutureUtils.TransformingFuture<Key,SKey>(f, new ITransformer<Key,SKey>() {
                
                @Override
                public SKey transform(Key in) {
                    return GKey.wrap(in);
                }
            });
        } else {
            return new FuturePutEntity(entity, f);
        }
    }
    
    @Override
    public boolean isTransactionsActive() {
        return !this.asyncDatastore.getActiveTransactions().isEmpty();
    }
    
    @Override
    public String getDatastoreName() {
        return DATASTORE_NAME;
    }
    
}
