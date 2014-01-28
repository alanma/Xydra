package org.xydra.store.impl.gae.ng;

import org.xydra.base.XAddress;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.core.serialize.SerializedModel;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.json.JsonParser;
import org.xydra.core.serialize.json.JsonSerializer;
import org.xydra.store.impl.utils.DebugFormatter;
import org.xydra.xgae.XGae;
import org.xydra.xgae.datastore.api.SEntity;
import org.xydra.xgae.datastore.api.SKey;
import org.xydra.xgae.datastore.api.SText;


public class TosUtils {
    
    private static final String JSON = "json";
    
    private static final String OBJECT_EXISTS = "exists";
    
    private static final String USED_REV = "usedRev";
    
    static XRevWritableObject deserialize(XAddress modelAddress, String data) {
        JsonParser parser = new JsonParser();
        XydraElement xydraElement = parser.parse(data);
        XRevWritableObject object = SerializedModel.toObjectState(xydraElement, modelAddress);
        return object;
    }
    
    public static TentativeObjectState fromEntity_static(SEntity entity, XAddress modelAddress) {
        assert entity != null;
        assert entity.hasAttribute(USED_REV) : "no USED_REV found in " + entity.getKey().raw()
                + " " + DebugFormatter.format(entity.raw());
        long revUsed = (Long)entity.getAttribute(USED_REV);
        boolean objectExists = (Boolean)entity.getAttribute(OBJECT_EXISTS);
        SText jsonText = (SText)entity.getAttribute(JSON);
        String json = jsonText.getValue();
        try {
            XRevWritableObject obj = deserialize(modelAddress, json);
            return new TentativeObjectState(obj, objectExists, revUsed);
        } catch(Throwable e) {
            throw new RuntimeException("Could not deserialize TOS with key = '" + entity.getKey()
                    + "'", e);
        }
    }
    
    static String serialize(XReadableObject object) {
        // set up corresponding serialiser & parser
        JsonSerializer serializer = new JsonSerializer();
        
        // serialise with revisions
        XydraOut out = serializer.create();
        out.enableWhitespace(false, false);
        SerializedModel.serialize(object, out);
        
        String data = out.getData();
        return data;
    }
    
    public static SEntity toEntity(SKey datastoreKey, TentativeObjectState tos) {
        SEntity e = XGae.get().datastore().createEntity(datastoreKey);
        
        e.setAttribute(USED_REV, tos.getModelRevision());
        assert e.hasAttribute(USED_REV);
        
        String json = serialize(tos);
        SText jsonText = XGae.get().datastore().createText(json);
        e.setAttribute(JSON, jsonText);
        
        e.setAttribute(OBJECT_EXISTS, tos.exists());
        
        return e;
    }
    
}
