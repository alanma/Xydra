package org.xydra.store.impl.gae.ng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.Setting;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.impl.memory.SimpleObject;
import org.xydra.core.XX;
import org.xydra.core.serialize.SerializedModel;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.json.JsonParser;
import org.xydra.core.serialize.json.JsonSerializer;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.impl.gae.GaeTestFixer_LocalPart;
import org.xydra.store.impl.gae.GaeTestfixer;
import org.xydra.store.impl.gae.GaeUtils2;
import org.xydra.store.impl.gae.SyncDatastore;
import org.xydra.store.impl.gae.UniCache;
import org.xydra.store.impl.gae.UniCache.CacheEntryHandler;
import org.xydra.store.impl.gae.UniCache.StorageOptions;
import org.xydra.store.impl.gae.snapshot.IGaeSnapshotService;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Text;


/**
 * The outside (standard) execution context, which is different from the
 * {@link ContextInTxn}.
 * 
 * @author xamde
 */
public class ContextBeforeCommand implements XRevWritableModel,
        CacheEntryHandler<TentativeObjectState> {
    
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
    
    public boolean isModelExists() {
        return getInfo().isModelExists();
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
    
    private static final String JSON = "json";
    
    private static final String USED_REV = "usedRev";
    
    private static final String OBJECT_EXISTS = "exists";
    
    @SuppressWarnings("unused")
    private static XAddress fromKey(Key key) {
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
    
    UniCache<TentativeObjectState> cache = new UniCache<TentativeObjectState>(this, KIND_TOS);
    
    private XAddress modelAddress;
    
    // FIXME was 1,false,true before
    @Setting("Where to cache TOS")
    private StorageOptions storeOpts = StorageOptions.create(0, false, true, false);
    
    protected static XRevWritableObject deserialize(XAddress modelAddress, String data) {
        JsonParser parser = new JsonParser();
        XydraElement xydraElement = parser.parse(data);
        XRevWritableObject object = SerializedModel.toObjectState(xydraElement, modelAddress);
        return object;
    }
    
    @Override
    public TentativeObjectState fromEntity(Entity entity) {
        return fromEntity_static(entity, this.modelAddress);
    }
    
    public static TentativeObjectState fromEntity_static(Entity entity, XAddress modelAddress) {
        long revUsed = (Long)entity.getProperty(USED_REV);
        boolean objectExists = (Boolean)entity.getProperty(OBJECT_EXISTS);
        Text jsonText = (Text)entity.getProperty(JSON);
        String json = jsonText.getValue();
        try {
            XRevWritableObject obj = deserialize(modelAddress, json);
            return new TentativeObjectState(obj, objectExists, revUsed);
        } catch(Throwable e) {
            throw new RuntimeException("Could not deserialize TOS with key = '" + entity.getKey()
                    + "'", e);
        }
        
    }
    
    @Override
    public TentativeObjectState fromSerializable(Serializable s) {
        return (TentativeObjectState)s;
    }
    
    public static List<TentativeObjectState> getAllTentativeObjectStatesOfModel(
            XAddress modelAddress) {
        
        /* Make sure to keep in sync with #toKey(..) */
        String keyPrefix = "tos/" + modelAddress.getRepository() + "/" + modelAddress.getModel();
        
        Query query = new Query(KIND_TOS);
        List<Filter> subFilters = new ArrayList<Filter>(2);
        
        subFilters.add(
        
        new Query.FilterPredicate(GaeUtils2.KEY, FilterOperator.GREATER_THAN_OR_EQUAL, KeyFactory
                .createKey(KIND_TOS, keyPrefix)));
        
        subFilters.add(
        
        new Query.FilterPredicate(GaeUtils2.KEY, FilterOperator.LESS_THAN_OR_EQUAL, KeyFactory
                .createKey(KIND_TOS, keyPrefix + GaeUtils2.LAST_UNICODE_CHAR)));
        
        query.setFilter(Query.CompositeFilterOperator.and(subFilters));
        
        PreparedQuery prepQuery = SyncDatastore.prepareQuery(query);
        
        log.info("Firing query " + prepQuery.toString());
        
        List<Entity> entityList = prepQuery.asList(FetchOptions.Builder.withChunkSize(128));
        
        List<TentativeObjectState> tosList = new ArrayList<TentativeObjectState>(entityList.size());
        log.info("got " + entityList.size() + " results");
        
        for(Entity entity : entityList) {
            TentativeObjectState tos = fromEntity_static(entity, modelAddress);
            tosList.add(tos);
        }
        
        return tosList;
    }
    
    public static void main(String[] args) {
        GaeTestfixer.enable();
        GaeTestFixer_LocalPart.initialiseHelperAndAttachToCurrentThread();
        getAllTentativeObjectStatesOfModel(XX
                .toAddress("/gae-data/a00024529-BB2E-4B97-AE12-13B67C9D68ED-tasks"));
    }
    
    @CanBeNull
    TentativeObjectState getTentativeObjectState(XId objectId) {
        // look in datastore
        XAddress objectAddress = XX.resolveObject(this.modelAddress, objectId);
        String key = toKey(objectAddress);
        TentativeObjectState tos = this.cache.get(key, this.storeOpts);
        
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
        this.cache.put(key, tos, this.storeOpts);
    }
    
    protected String serialize(XReadableObject object) {
        // set up corresponding serialiser & parser
        JsonSerializer serializer = new JsonSerializer();
        
        // serialise with revisions
        XydraOut out = serializer.create();
        out.enableWhitespace(false, false);
        SerializedModel.serialize(object, out);
        
        String data = out.getData();
        return data;
    }
    
    @Override
    public Entity toEntity(Key datastoreKey, @CanBeNull TentativeObjectState tos) {
        Entity e = new Entity(datastoreKey);
        e.setUnindexedProperty(USED_REV, tos.getModelRevision());
        String json = serialize(tos);
        Text jsonText = new Text(json);
        e.setUnindexedProperty(JSON, jsonText);
        e.setUnindexedProperty(OBJECT_EXISTS, tos.exists());
        return e;
    }
    
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
        return isModelExists();
    }
    
}
