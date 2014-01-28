package org.xydra.xgae.datastore.impl.gae;

import org.xydra.xgae.datastore.api.IDatastore;
import org.xydra.xgae.datastore.api.IDatastoreAsync;
import org.xydra.xgae.datastore.api.IDatastoreSync;
import org.xydra.xgae.datastore.api.SEntity;
import org.xydra.xgae.datastore.api.SKey;
import org.xydra.xgae.datastore.api.SText;
import org.xydra.xgae.gaeutils.AboutAppEngine;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;


public class DatastoreImplGae implements IDatastore {
    
    private IDatastoreSync syncInstance;
    
    private IDatastoreAsync asyncInstance;
    
    public synchronized IDatastoreSync sync() {
        // TODO implement factory pattern
        if(this.syncInstance == null) {
            this.syncInstance = new DatastoreImplGaeSync();
        }
        return this.syncInstance;
    }
    
    public synchronized IDatastoreAsync async() {
        // TODO implement factory pattern
        if(this.asyncInstance == null) {
            this.asyncInstance = new DatastoreImplGaeAsync();
        }
        return this.asyncInstance;
    }
    
    public SEntity createEntity(SKey key) {
        Entity e = new Entity((Key)key.raw());
        return GEntity.wrap(e);
    }
    
    public SEntity createEntity(String kind, String name) {
        return createEntity(createKey(kind, name));
    }
    
    @Override
    public SText createText(String value) {
        return GText.wrap(new Text(value));
    }
    
    @Override
    public SKey createKey(String kind, String name) {
        return GKey.wrap(KeyFactory.createKey(kind, name));
    }
    
    @Override
    public boolean canWriteDataStore() {
        return AboutAppEngine.canWriteDataStore();
    }
    
}
