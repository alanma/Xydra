package org.xydra.store.impl.gae.ng;

import java.io.Serializable;
import java.util.Iterator;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.Setting;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.impl.memory.SimpleObject;
import org.xydra.core.serialize.SerializedModel;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.json.JsonParser;
import org.xydra.core.serialize.json.JsonSerializer;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.impl.gae.UniCache;
import org.xydra.store.impl.gae.UniCache.CacheEntryHandler;
import org.xydra.store.impl.gae.UniCache.StorageOptions;
import org.xydra.store.impl.gae.snapshot.IGaeSnapshotService;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;


/**
 * The outside (standard) execution context, which is different from the
 * {@link ContextInTxn}.
 * 
 * @author xamde
 */
public class ContextBeforeCommand implements XRevWritableModel,
        CacheEntryHandler<TentativeObjectState> {
	
	private IGaeSnapshotService snapshotService;
	
	public ContextBeforeCommand(@NeverNull XAddress modelAddress, @NeverNull RevisionManager rm,
	        IGaeSnapshotService snapshotService) {
		super();
		this.modelAddress = modelAddress;
		this.rm = rm;
		this.snapshotService = snapshotService;
	}
	
	private RevisionManager rm;
	
	public XReadableModel getModelSnapshot() {
		// TODO using tentative here - good idea?
		long modelRev = getRevisionManager().getInfo().getLastSuccessChange();
		XRevWritableModel modelSnapshot = this.snapshotService.getModelSnapshot(modelRev, false);
		return modelSnapshot;
	}
	
	public boolean isModelExists() {
		return getRevisionManager().getInfo().isModelExists();
	}
	
	/**
	 * @return a copy of all state that shares no mutable references
	 */
	public ContextInTxn forkTxn() {
		return new ContextInTxn(this);
	}
	
	public RevisionManager getRevisionManager() {
		return this.rm;
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
	
	private static String toKey(XAddress objectAddress) {
		return "tos" + objectAddress;
	}
	
	UniCache<TentativeObjectState> cache = new UniCache<TentativeObjectState>(this, "TOS");
	
	private XAddress modelAddress;
	
	// FIXME was 1,false,true before
	@Setting("Where to cache TOS")
	private StorageOptions storeOpts = StorageOptions.create(0, false, true, false);
	
	protected XRevWritableObject deserialize(XAddress modelAddress, String data) {
		JsonParser parser = new JsonParser();
		XydraElement xydraElement = parser.parse(data);
		XRevWritableObject object = SerializedModel.toObjectState(xydraElement, modelAddress);
		return object;
	}
	
	@Override
	public TentativeObjectState fromEntity(Entity entity) {
		long revUsed = (Long)entity.getProperty(USED_REV);
		boolean objectExists = (Boolean)entity.getProperty(OBJECT_EXISTS);
		Text jsonText = (Text)entity.getProperty(JSON);
		String json = jsonText.getValue();
		XRevWritableObject obj = deserialize(this.modelAddress, json);
		
		return new TentativeObjectState(obj, objectExists, revUsed);
	}
	
	@Override
	public TentativeObjectState fromSerializable(Serializable s) {
		return (TentativeObjectState)s;
	}
	
	@CanBeNull
	TentativeObjectState getTentativeObjectState(XID objectId) {
		// look in datastore
		XAddress objectAddress = XX.resolveObject(this.modelAddress, objectId);
		String key = toKey(objectAddress);
		TentativeObjectState tos = this.cache.get(key, this.storeOpts);
		
		// FIXME how to deal with legacy?
		if(tos == null) {
			long tentativeModelRev = this.rm.getInfo().getLastSuccessChange();
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
		return this.rm.getInfo().getLastSuccessChange();
	}
	
	@Override
	public boolean hasObject(XID objectId) {
		return getObject(objectId) != null;
	}
	
	@Override
	public boolean isEmpty() {
		return !iterator().hasNext();
	}
	
	@Override
	public Iterator<XID> iterator() {
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
	public XID getId() {
		return getAddress().getModel();
	}
	
	@Override
	public boolean removeObject(XID objectId) {
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
	public TentativeObjectState createObject(XID objectId) {
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
	
	private XAddress getObjectAddress(XID objectId) {
		return XX.resolveObject(getAddress(), objectId);
	}
	
	@Override
	public TentativeObjectState getObject(XID objectId) {
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
	
}
