package org.xydra.store.impl.gae.ng;

import java.io.Serializable;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.base.XAddress;
import org.xydra.base.XX;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
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


public class TentativeSnapshotManagerAtomic implements ITentativeSnapshotManager,
        CacheEntryHandler<TentativeObjectSnapshot> {
	
	private RevisionManager revisionManager;
	
	private IGaeSnapshotService snapshotService;
	
	public TentativeSnapshotManagerAtomic(@NeverNull XAddress modelAddress,
	        RevisionManager revisionManager, IGaeSnapshotService snapshotService) {
		this.modelAddress = modelAddress;
		this.revisionManager = revisionManager;
		this.snapshotService = snapshotService;
	}
	
	@Override
	public XReadableModel getModelSnapshot() {
		// TODO using tentative here - good idea?
		long modelRev = this.revisionManager.getInfo().getLastSuccessChange();
		XRevWritableModel modelSnapshot = this.snapshotService.getModelSnapshot(modelRev, false);
		return modelSnapshot;
	}
	
	private static final String JSON = "json";
	
	private static final String USED_REV = "usedRev";
	
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
	
	UniCache<TentativeObjectSnapshot> cache = new UniCache<TentativeObjectSnapshot>(this);
	
	private XAddress modelAddress;
	
	private StorageOptions storeOpts = StorageOptions.create(false, false, true);
	
	protected XRevWritableObject deserialize(XAddress modelAddress, String data) {
		JsonParser parser = new JsonParser();
		XydraElement xydraElement = parser.parse(data);
		XRevWritableObject object = SerializedModel.toObjectState(xydraElement, modelAddress);
		return object;
	}
	
	@Override
	public TentativeObjectSnapshot fromEntity(Entity entity) {
		long revUsed = (Long)entity.getProperty(USED_REV);
		Text jsonText = (Text)entity.getProperty(JSON);
		XRevWritableObject obj = null;
		if(jsonText != null) {
			String json = jsonText.getValue();
			obj = deserialize(this.modelAddress, json);
		}
		
		Key key = entity.getKey();
		XAddress objectAddress = fromKey(key);
		return new TentativeObjectSnapshot(obj, objectAddress, revUsed);
	}
	
	@Override
	public TentativeObjectSnapshot fromSerializable(Serializable s) {
		return (TentativeObjectSnapshot)s;
	}
	
	@Override
	public @CanBeNull
	TentativeObjectSnapshot getTentativeObjectSnapshot(XAddress objectAddress) {
		// look in datastore
		String key = toKey(objectAddress);
		TentativeObjectSnapshot tos = this.cache.get(key, this.storeOpts);
		
		// FIXME how to deal with legacy?
		if(tos == null) {
			long tentativeModelRev = this.revisionManager.getInfo().getLastSuccessChange();
			tos = new TentativeObjectSnapshot(null, objectAddress, tentativeModelRev);
			
			// // compute one & store it
			// // IMPROVE calculate smarter
			// XRevWritableObject objectSnapshot =
			// this.snapshotService.getObjectSnapshot(modelRev,
			// true, objectAddress.getObject());
			// object = new TentativeObjectSnapshot(objectSnapshot,
			// objectAddress, modelRev);
			saveTentativeObjectSnapshot(tos);
		}
		
		// does not hold: XyAssert.xyAssert(tos.getModelRevision() >= 0, tos);
		
		return tos;
	}
	
	public void saveTentativeObjectSnapshot(@NeverNull TentativeObjectSnapshot tos) {
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
	public Entity toEntity(Key datastoreKey, @CanBeNull TentativeObjectSnapshot tos) {
		Entity e = new Entity(datastoreKey);
		e.setUnindexedProperty(USED_REV, tos.getModelRevision());
		if(tos.isObjectExists()) {
			String json = serialize(tos);
			Text jsonText = new Text(json);
			e.setUnindexedProperty(JSON, jsonText);
		} else {
			e.setUnindexedProperty(JSON, null);
		}
		return e;
	}
	
	@Override
	public Serializable toSerializable(TentativeObjectSnapshot entry) {
		return entry;
	}
	
	@Override
	public long getModelRevision() {
		return this.revisionManager.getInfo().getLastSuccessChange();
	}
	
}
