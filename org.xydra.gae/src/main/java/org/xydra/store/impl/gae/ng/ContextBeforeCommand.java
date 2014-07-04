package org.xydra.store.impl.gae.ng;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.Setting;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.impl.XExistsReadable;
import org.xydra.base.rmof.impl.memory.SimpleObject;
import org.xydra.core.XX;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.impl.gae.GaeUtils2;
import org.xydra.store.impl.gae.UniCache;
import org.xydra.store.impl.gae.UniCache.CacheEntryHandler;
import org.xydra.store.impl.gae.UniCache.StorageOptions;
import org.xydra.store.impl.gae.snapshot.IGaeSnapshotService;
import org.xydra.xgae.XGae;
import org.xydra.xgae.datastore.api.SEntity;
import org.xydra.xgae.datastore.api.SKey;
import org.xydra.xgae.datastore.api.SPreparedQuery;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.io.Serializable;


/**
 * The outside (standard) execution context, which is different from the
 * {@link ContextInTxn}.
 * 
 * @author xamde
 */
public class ContextBeforeCommand implements XRevWritableModel,
        CacheEntryHandler<TentativeObjectState>, XExistsReadable {
    
    private static final Logger log = LoggerFactory.getLogger(ContextBeforeCommand.class);
    
    private IGaeSnapshotService snapshotService;
    
    public ContextBeforeCommand(@NeverNull XAddress modelAddress, @NeverNull GaeModelRevInfo info,
            IGaeSnapshotService snapshotService) {
        super();
        this.modelAddress = modelAddress;
        this.info = info;
        this.snapshotService = snapshotService;
    }
    
    private GaeModelRevInfo info;
    
    public XReadableModel getModelSnapshot() {
        // TODO using tentative here - good idea?
        long modelRev = getInfo().getLastSuccessChange();
        XRevWritableModel modelSnapshot = this.snapshotService.getModelSnapshot(modelRev, false);
        return modelSnapshot;
    }
    
    /**
     * @return a copy of all state that shares no mutable references
     */
    public ContextInTxn forkTxn() {
        return new ContextInTxn(this);
    }
    
    public GaeModelRevInfo getInfo() {
        return this.info;
    }
    
    @SuppressWarnings("unused")
    private static XAddress fromKey(SKey key) {
        String localName = key.getName();
        XyAssert.xyAssert(localName.startsWith("tos"));
        localName = localName.substring("tos".length());
        XAddress address = XX.toAddress(localName);
        return address;
    }
    
    /**
     * Internally used also to generate the key prefix
     * 
     * @param objectOrModelAddress
     * @return key
     */
    private static String toKey(XAddress objectOrModelAddress) {
        return "tos" + objectOrModelAddress;
    }
    
    public static final String KIND_TOS = "TOS";
    
    UniCache<TentativeObjectState> tosCache = new UniCache<TentativeObjectState>(this, KIND_TOS);
    
    private XAddress modelAddress;
    
    // FIXME was 1,false,true before - makes tests fail
    @Setting("Where to cache TOS")
    private StorageOptions storeOpts = StorageOptions.create(0, false, true, false);
    
    /** implement {@link CacheEntryHandler} */
    @Override
    public TentativeObjectState fromEntity(SEntity entity) {
        return TosUtils.fromEntity_static(entity, this.modelAddress);
    }
    
    /** implement {@link CacheEntryHandler} */
    @Override
    public TentativeObjectState fromSerializable(Serializable s) {
        return (TentativeObjectState)s;
    }
    
    public static List<TentativeObjectState> getAllTentativeObjectStatesOfModel(
            XAddress modelAddress) {
        
        /* Make sure to keep in sync with #toKey(..) */
        String keyPrefix = "tos/" + modelAddress.getRepository() + "/" + modelAddress.getModel();
        
        // Query query = new Query(KIND_TOS);
        
        // List<Filter> subFilters = new ArrayList<Filter>(2);
        
        // subFilters.add(
        //
        // new Query.FilterPredicate(GaeUtils2.KEY,
        // FilterOperator.GREATER_THAN_OR_EQUAL, KeyFactory
        // .createKey(KIND_TOS, keyPrefix)));
        //
        // subFilters.add(
        // new Query.FilterPredicate(GaeUtils2.KEY,
        // FilterOperator.LESS_THAN_OR_EQUAL, KeyFactory
        
        // .createKey(KIND_TOS, keyPrefix + GaeUtils2.LAST_UNICODE_CHAR)));
        //
        // query.setFilter(Query.CompositeFilterOperator.and(subFilters));
        
        // PreparedQuery prepQuery = SyncDatastore.prepareQuery(query);
        
        // log.info("Firing query " + prepQuery.toString());
        
        SPreparedQuery preparedQuery = XGae
                .get()
                .datastore()
                .sync()
                .prepareRangeQuery(KIND_TOS, false, keyPrefix,
                        keyPrefix + GaeUtils2.LAST_UNICODE_CHAR);
        
        log.info("Firing query " + preparedQuery.toString());
        preparedQuery.setChunkSize(128);
        List<SEntity> entityList = preparedQuery.asList();
        
        List<TentativeObjectState> tosList = new ArrayList<TentativeObjectState>(entityList.size());
        log.info("got " + entityList.size() + " results");
        
        for(SEntity entity : entityList) {
            TentativeObjectState tos = TosUtils.fromEntity_static(entity, modelAddress);
            tosList.add(tos);
        }
        
        return tosList;
    }
    
    @CanBeNull
    TentativeObjectState getTentativeObjectState(XId objectId) {
        // look in datastore
        XAddress objectAddress = XX.resolveObject(this.modelAddress, objectId);
        String key = toKey(objectAddress);
        TentativeObjectState tos = this.tosCache.get(key, this.storeOpts);
        
        // FIXME how to deal with legacy?
        if(tos == null) {
            long tentativeModelRev = getInfo().getLastSuccessChange();
            SimpleObject simpleObject = new SimpleObject(objectAddress);
            simpleObject.setRevisionNumber(tentativeModelRev);
            tos = new TentativeObjectState(simpleObject, false, tentativeModelRev);
            
            // // compute one & store it
            // // IMPROVE calculate smarter
            // XRevWritableObject objectSnapshot =
            // this.snapshotService.getObjectSnapshot(modelRev,
            // true, objectAddress.getObject());
            // object = new TentativeObjectSnapshot(objectSnapshot,
            // objectAddress, modelRev);
            
            saveTentativeObjectState(tos);
        }
        
        // does not hold: XyAssert.xyAssert(tos.getModelRevision() >= 0, tos);
        
        return tos;
    }
    
    void saveTentativeObjectState(@NeverNull TentativeObjectState tos) {
        XyAssert.xyAssert(tos != null);
        assert tos != null;
        
        String key = toKey(tos.getAddress());
        this.tosCache.put(key, tos, this.storeOpts);
    }
    
    /** implement {@link CacheEntryHandler} */
    @Override
    public SEntity toEntity(SKey datastoreKey, @CanBeNull TentativeObjectState tos) {
        return TosUtils.toEntity(datastoreKey, tos);
    }
    
    /** implement {@link CacheEntryHandler} */
    @Override
    public Serializable toSerializable(TentativeObjectState entry) {
        return entry;
    }
    
    @Override
    public long getRevisionNumber() {
        // TODO tentative: good idea?
        return getInfo().getLastSuccessChange();
    }
    
    @Override
    public boolean hasObject(XId objectId) {
        return getObject(objectId) != null;
    }
    
    @Override
    public boolean isEmpty() {
        return !iterator().hasNext();
    }
    
    @Override
    public Iterator<XId> iterator() {
        return this.snapshotService.getModelSnapshot(getRevisionNumber(), false).iterator();
    }
    
    @Override
    public XType getType() {
        return XType.XMODEL;
    }
    
    @Override
    public XAddress getAddress() {
        return this.modelAddress;
    }
    
    @Override
    public XId getId() {
        return getAddress().getModel();
    }
    
    @Override
    public boolean removeObject(XId objectId) {
        TentativeObjectState tos = getTentativeObjectState(objectId);
        tos.setObjectExists(false);
        // FIXME which model rev?
        tos.setModelRev(getRevisionNumber());
        saveTentativeObjectState(tos);
        return true;
    }
    
    @Override
    public void addObject(XRevWritableObject object) {
        TentativeObjectState tos = getTentativeObjectState(object.getId());
        tos.setObjectExists(true);
        tos.setObjectState(object);
        // FIXME which model rev?
        tos.setModelRev(getRevisionNumber());
        saveTentativeObjectState(tos);
    }
    
    @Override
    public TentativeObjectState createObject(XId objectId) {
        TentativeObjectState object = getObject(objectId);
        if(object == null) {
            SimpleObject simpleObject = new SimpleObject(getObjectAddress(objectId));
            object = new TentativeObjectState(simpleObject, true, getRevisionNumber());
        } else {
            object.setObjectExists(true);
            object.setModelRev(getRevisionNumber());
        }
        saveTentativeObjectState(object);
        return object;
    }
    
    private XAddress getObjectAddress(XId objectId) {
        return XX.resolveObject(getAddress(), objectId);
    }
    
    @Override
    public TentativeObjectState getObject(XId objectId) {
        TentativeObjectState tos = getTentativeObjectState(objectId);
        if(tos == null) {
            // TODO really?
            return null;
        }
        if(!tos.exists()) {
            return null;
        }
        return tos;
    }
    
    @Override
    public void setRevisionNumber(long rev) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String toString() {
        return "ctxBefore @" + this.modelAddress + " r" + this.getRevisionNumber();
    }
    
    @Override
    public boolean exists() {
        return getInfo().isModelExists();
    }
    
}
